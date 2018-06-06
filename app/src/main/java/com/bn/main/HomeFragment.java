package com.bn.main;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.bn.broadcast.LiveQuestionActivity;
import com.bn.broadcast.PPTActivity;
import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.smartclass_android.R;
import com.bn.tool_package.ImageTools;
import com.bn.tool_package.ListViewHeight;
import com.bn.tool_package.TimeTools;
import com.bn.util.MyListViewAdapter;
import com.bn.util.NetConnectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 13273 on 2017/7/22.
 *
 */

public class HomeFragment extends Fragment {

    LayoutInflater inflater;
    Bitmap[] bitmap = new Bitmap[6];
    ConvenientBanner convenientBanner;
    public static boolean needReload;
    String[] campaignInfo;
    View view;

    Bitmap[] schoolCampaignBitmaps;
    String[] courseInfo;

    Handler myHandler;

    ListView articleListView;
    ListView todayListView;

    private List<Integer> localImgList = new ArrayList<>();



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        view = inflater.inflate(R.layout.home_layout, container, false);
        this.inflater = inflater;
        convenientBanner = (ConvenientBanner) view.findViewById(R.id.convenientBanner);
        convenientBanner.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                    String mes = campaignInfo[position].split("<#>")[0];
                    Bundle bundle = new Bundle();
                    bundle.putString("mes", mes);
                    startActivity(new Intent(MainActivity.mainActivity,SchoolCampaignActivity.class).putExtras(bundle));
            }
        });
        articleListView = (ListView) view.findViewById(R.id.list_for_article);
        todayListView = (ListView) view.findViewById(R.id.list_for_course);

        view.findViewById(R.id.create_notes).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.mainActivity, QuickNoteActivity.class));
            }
        });
        //每次载入这个类先把重载标志位置为false 后面有一项加载失败改为true
        needReload = false;

        /**
         * UI线程负责初始化校园活动 课表 文章
         * UI线程包括一个Handler和一个Thread
         * Thread负责联网获取数据 Handler负责绘制UI
         *
         */
        uiThreads();
        /**
         * 根据屏蔽情况屏蔽界面
         */
        if(Preference.isShieldAds)
        {
            view.findViewById(R.id.convenientBanner).setVisibility(View.GONE);
            view.findViewById(R.id.con_view).setVisibility(View.GONE);
            view.findViewById(R.id.article_view).setVisibility(View.GONE);
            view.findViewById(R.id.article_title).setVisibility(View.GONE);
            view.findViewById(R.id.art_view2).setVisibility(View.GONE);
            view.findViewById(R.id.list_for_article).setVisibility(View.GONE);
        }
        return view;
    }

    private void uiThreads()
    {
        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                switch (msg.what)
                {
                    case HandlerConstant.CAMPAIGN_SUCCEED:
                        initConvenientBanner();
                        break;

                    case HandlerConstant.CAMPAIGN_FAILURE:
                        break;

                    case HandlerConstant.COURSE_SUCCEED:
                        Bundle courseBundle = msg.getData();
                        courseInfo = courseBundle.getStringArray(BundleConstant.TODAY_COURSE_INFO);
                        initTodayCourseList(todayListView, courseInfo, true);
                        break;

                    case HandlerConstant.COURSE_FAILURE:
                        initTodayCourseList(todayListView, new String[]{""}, false);
                        break;

                    case HandlerConstant.ARTICLE_SUCCEED:
                        Bundle bundle = msg.getData();
                        String[] info = bundle.getStringArray(BundleConstant.ARTICLE_INFO);
                        initArticleList(articleListView, info, true);
                        break;

                    case HandlerConstant.ARTICLE_FAILURE:
                        initArticleList(articleListView, null, false);
                        break;

                    case HandlerConstant.MESSAGE_INFO:
                        Toast.makeText(MainActivity.mainActivity, "没有到上课时间，不能进入课堂。", Toast.LENGTH_SHORT).show();
                        break;

                    case HandlerConstant.NOT_IN_LIVE:
                        Toast.makeText(MainActivity.mainActivity, "老师还没开始直播哟，请耐心等待一会儿吧。", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                if(!Preference.isShieldAds)
                    downloadCampaign();

                queryTodayCourse();

                if(!Preference.isShieldAds)
                    downloadArticle();

            }
        }).start();
    }


    private void downloadCampaign()
    {
        //campaign_id, campaign_title, campaign_content, campaign_picture
        String result = NetConnectionUtil.SchoolCampaign();

        Message message = new Message();

        if(!result.equals(Constant.SERVER_CONNECTION_ERROR)&&result.length()>0)
        {
           campaignInfo = result.split("\\|");
            int length = campaignInfo.length;
            String[] schoolCampaignURL = new String[length];
            for(int i=0;i<length;i++){
                String[] piece = campaignInfo[i].split("<#>");
                schoolCampaignURL[i] = piece[3];
            }

            //下载图片
            boolean lostImg = false;
            schoolCampaignBitmaps = new Bitmap[length];
            for(int i=0;i<length;i++){
                byte[] picData = NetConnectionUtil.downLoadPicture(schoolCampaignURL[i]);
                if(picData!=null)
                    schoolCampaignBitmaps[i] = ImageTools.getBitmapFromByteArray(picData);
                else
                {
                    schoolCampaignBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.blank_white);
                    lostImg = true;
                }
            }
            if(lostImg) needReload = true;

            message.what = HandlerConstant.CAMPAIGN_SUCCEED;
        }

        else
        {
            message.what = HandlerConstant.CAMPAIGN_FAILURE;
            needReload = true;
        }

        myHandler.sendMessage(message);
    }

    private void queryTodayCourse()
    {
        String result = null;

        if(Preference.dataInfoList!=null)
            result = NetConnectionUtil.queryTodayCourse(Preference.dataInfoList.get(0));

        //若加载到今天课程 并且今天有课程 则启动后台直播Service
        if(result!=null&&!result.equals(Constant.NO_RECORD_FOUND)&&result.length()>0)
        {
            if(!isServiceRunning(MainActivity.mainActivity,"com.bn.main.OnlineClassService"))
            {
                Bundle bundle = new Bundle();
                bundle.putString(BundleConstant.TODAY_COURSE_INFO,result);
                Intent intent = new Intent(MainActivity.mainActivity, OnlineClassService.class);
                intent.putExtras(bundle);
                MainActivity.mainActivity.startService(intent);
            }

        }

        Message message = new Message();

        if(result!=null&&!result.equals(Constant.SERVER_CONNECTION_ERROR)&&result.length()>0)
        {
            message.what = HandlerConstant.COURSE_SUCCEED;
            String[] info = result.split("\\|");

            //处理并且包装信息
            Bundle bundle = new Bundle();
            bundle.putStringArray(BundleConstant.TODAY_COURSE_INFO, info);
            message.setData(bundle);
        }
        else {
            message.what = HandlerConstant.COURSE_FAILURE;
            needReload = true;
        }


        myHandler.sendMessage(message);
    }

    private void downloadArticle()
    {
        String info = NetConnectionUtil.article();

        Message message = new Message();
        if(!info.startsWith(Constant.SERVER_CONNECTION_ERROR)&&info.length()>0) {
            message.what = HandlerConstant.ARTICLE_SUCCEED;
        }
        else {
            message.what = HandlerConstant.ARTICLE_FAILURE;
            needReload = true;
        }

        String[] ss = info.split("\\|");
        Bundle bundle = new Bundle();
        bundle.putStringArray(BundleConstant.ARTICLE_INFO, ss);
        message.setData(bundle);

        //如果网络通畅则加载图片
        if(message.what==HandlerConstant.ARTICLE_SUCCEED)
        {
            String[] url = new String[6];
            for(int i=0;i<6;i++)
            {
                String[] piece = ss[i].split("<#>");
                url[i] = piece[3];
            }

            for(int i=0;i<6;i++)
            {
                byte[] picData = NetConnectionUtil.downLoadPicture(url[i]);
                if(picData!=null){
                    bitmap[i] = ImageTools.getBitmapFromByteArray(picData);
                }
                else needReload = true;
            }
        }
        myHandler.sendMessage(message);
    }


    private void initArticleList(ListView list, final String[] infoStr, final boolean hasConnection)
    {
        MyListViewAdapter baseAdapter = new MyListViewAdapter(6) {
            @Override
            public View getView(int i, View view, ViewGroup viewGroup)
            {
                LinearLayout content = (LinearLayout)view;

                if(content==null) {
                    content = (LinearLayout)
                            (inflater.inflate(R.layout.online_class_list, null)).findViewById(R.id.online_list_layout);
                }

                LinearLayout textContent = (LinearLayout) content.getChildAt(0);

                //去除最后一列的线
                if(i==5){
                    LinearLayout line = (LinearLayout) content.getChildAt(2);
                    line.setVisibility(View.GONE);
                }

                ImageView imageView = (ImageView) textContent.getChildAt(0);

                LinearLayout text = (LinearLayout) textContent.getChildAt(1);
                TextView name = (TextView) text.getChildAt(1);
                TextView articles = (TextView) text.getChildAt(2);


                if(hasConnection)
                {
                    String[] texts = infoStr[i].split("<#>");
                    name.setText(texts[1]);
                    articles.setText(texts[2].substring(0,30)+"…");

                    if(bitmap[i]!=null){
                        imageView.setImageBitmap(bitmap[i]);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                }
                else
                {
                    name.setText("");
                    articles.setText("");
                }

                return content;
            }
        };

        list.setAdapter(baseAdapter);
        ListViewHeight.setListViewHeight(list);

        if(!hasConnection) return;
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.mainActivity,ArticleActivity.class);
                intent.putExtra("mes",infoStr[i].split("<#>")[0]+"");
                startActivity(intent);
            }
        });

    }

    private void initConvenientBanner() {
        for (int i = 0; i < schoolCampaignBitmaps.length; i++) {
            localImgList.add(i);
        }
        convenientBanner.setPages(new CBViewHolderCreator<LocalImageHolderView>() {
            @Override
            public LocalImageHolderView createHolder() {
                return new LocalImageHolderView();
            }
        },localImgList) //设置需要切换的View
                .setPointViewVisible(true)//设置指示器是否可见
                //设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
                .setPageIndicator(new int[]{R.drawable.d1,R.drawable.d2})//0表示没选中的圆圈 1表示选中了的圆圈
                //设置指示器位置（左、中、右）
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .startTurning(3000)//设置自动切换（同时设置了切换时间间隔）
                .setManualPageable(true);  //设置手动影响（设置了该项无法手动切换）
    }

    private void initTodayCourseList(ListView list, final String[] infoStr, final boolean hasConnection)
    {
        //0#course_name   1#subject_id  2#time_of_day  3#begin_time  4#end_time
        final int length = infoStr.length;
        MyListViewAdapter baseAdapter = new MyListViewAdapter(length)
        {
            @Override
            public View getView(int i, View view, ViewGroup viewGroup)
            {
                LinearLayout content = (LinearLayout) view;
                if(content==null)
                    content = (LinearLayout)
                            (inflater.inflate(R.layout.today_course_list, null)).findViewById(R.id.course_layout);

                //去除最后一列的线
                if(i==length-1)
                {
                    LinearLayout line = (LinearLayout) content.getChildAt(1);
                    line.setVisibility(View.GONE);
                }

                //获取所有的文本框
                TextView timeOfDay = (TextView) content.findViewById(R.id.time_of_day);
                TextView courseName = (TextView) content.findViewById(R.id.course_name);
                TextView courseTime = (TextView) content.findViewById(R.id.course_time);

                if(hasConnection)
                {
                    if(!infoStr[0].equals(Constant.NO_RECORD_FOUND))//今天有课
                    {
                        String[] piece = infoStr[i].split("<#>");
                        timeOfDay.setText("时段"+piece[2]);
                        courseName.setText(piece[0]);
                        courseTime.setText(piece[3]+"-"+piece[4]);

                        //intoClass.setTag(i);//待删除
                        //intoClass.setOnClickListener(HomeFragment.this);
                    }
                    else//今天没课
                    {
                        timeOfDay.setText("今天没有课哟");
                    }
                }
                else
                {
                    timeOfDay.setText("");
                    courseName.setText("");
                    courseTime.setText("");
                }
                return content;
            }
        };
        list.setAdapter(baseAdapter);
        ListViewHeight.setListViewHeight(list);
    }


    /**
     * initConvenientBanner的工具类
     */
    public class LocalImageHolderView implements Holder<Integer>
    {

        private ImageView imageView;
        @Override
        public View createView(Context context) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }
        @Override
        public void UpdateUI(Context context, int position, Integer data) {
            imageView.setImageBitmap(schoolCampaignBitmaps[data]);
        }
    }


//    @Override
//    public void onClick(View view)
//    {
//        final int selectedPosition = (int)view.getTag();
//
//        new Thread(new Runnable()
//        {
//            @Override
//            public void run() {
//                //0#course_name   1#subject_id  2#time_of_day  3#begin_time  4#end_time
//                String[] piece = courseInfo[selectedPosition].split("<#>");
//                int beginNumber = Integer.parseInt("1"+piece[3].split(":")[0]+piece[3].split(":")[1]);
//                int endNumber = Integer.parseInt("1"+piece[4].split(":")[0]+piece[4].split(":")[1]);
//                if((checkWhetherCouldIntoClass(beginNumber, endNumber)))
//                {
//                    String IPAddress = NetConnectionUtil.queryIPAddress(piece[1]);
//                    if(IPAddress==null||IPAddress.length()==0)
//                    {
//                        Message message = new Message();
//                        message.what = HandlerConstant.NOT_IN_LIVE;
//                        myHandler.sendMessage(message);
//                        return;
//                    }
//
//                    int mode = 1;
//                    Bundle bundle = new Bundle();
//                    bundle.putBoolean(BundleConstant.SHORT_STR, true);
//                    if(mode==Constant.MODE_PPT||mode == -1)
//                        startActivity(new Intent(MainActivity.mainActivity, PPTActivity.class).putExtras(bundle));
//                    else
//                        startActivity(new Intent(MainActivity.mainActivity, LiveQuestionActivity.class).putExtras(bundle));
//                }
//                else
//                {
//                    Message message = new Message();
//                    message.what = HandlerConstant.MESSAGE_INFO;
//                    myHandler.sendMessage(message);
//                }
//            }
//        }).start();
//    }

    public static boolean isServiceRunning(Context context, String ServiceName){
        if(("").equals(ServiceName)||ServiceName==null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for(int i = 0; i <runningService.size(); i++)
        {
            if(runningService.get(i).service.getClassName().toString().equals(ServiceName))
            {
                return true;
            }
         }
        return false;
        }
}
