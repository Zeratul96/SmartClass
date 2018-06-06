package com.bn.main;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.question.QuestionSetSubjectActivity;
import com.bn.question.WrongSetSubjectActivity;
import com.bn.smartclass_android.R;
import com.bn.tool_package.ImageTools;
import com.bn.util.MyListViewAdapter;
import com.bn.util.NetConnectionUtil;
import com.bn.tool_package.ListViewHeight;

/**
 * Created by 13273 on 2017/7/22.
 *
 */

public class ResourceFragment extends Fragment implements OnClickListener{

    LayoutInflater inflater;
    Bitmap[] bitmap = new Bitmap[6];
    public static boolean needReload;//网络状态失败情况下需要重新加载

    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.resource_layout, container, false);
        this.inflater = inflater;

        view.findViewById(R.id.question_set).setOnClickListener(this);
        view.findViewById(R.id.wrong_set).setOnClickListener(this);
        needReload=false;

        if(!Preference.isShieldAds)
            initOnlineClass();

        if(Preference.isShieldAds)
        {
            view.findViewById(R.id.class_view1).setVisibility(View.GONE);
            view.findViewById(R.id.class_title).setVisibility(View.GONE);
            view.findViewById(R.id.class_view2).setVisibility(View.GONE);
            view.findViewById(R.id.list_for_online).setVisibility(View.GONE);
        }
        return view;
    }

    private void initOnlineClass()
    {
        final ListView listView = (ListView) view.findViewById(R.id.list_for_online);

        final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle bundle = msg.getData();
                String[] info = bundle.getStringArray(BundleConstant.INFO_STR);

                switch (msg.what)
                {
                    case HandlerConstant.OPERATION_SUCCEED:
                        initOnlineClassList(listView, info, true);
                        break;

                    case HandlerConstant.CONNECTION_FAILURE:
                        initOnlineClassList(listView, null, false);
                        break;
                }
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String info = NetConnectionUtil.Online_Class();

                Message message = new Message();
                if(!info.startsWith(Constant.SERVER_CONNECTION_ERROR)&&info.length()>0)
                    message.what = HandlerConstant.OPERATION_SUCCEED;

                else {
                    message.what = HandlerConstant.CONNECTION_FAILURE;
                    needReload = true;
                }

                String[] ss = info.split("\\|");
                Bundle bundle = new Bundle();
                bundle.putStringArray(BundleConstant.INFO_STR, ss);
                message.setData(bundle);

                //如果网络通畅则加载图片
                if(message.what==HandlerConstant.OPERATION_SUCCEED)
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
                handler.sendMessage(message);

            }
        }).start();
    }


    /**
     *
     * @param list:表示listView对象
     * @param infoStr:表示信息，信息从自己新建的线程获得
     * @param hasConnection:表示网络连接是否正常
     */
    private void initOnlineClassList(ListView list, final String[] infoStr, final boolean hasConnection)
    {

        MyListViewAdapter baseAdapter = new MyListViewAdapter(6) {
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                LinearLayout content = (LinearLayout)view;

                if(content==null) {
                    content = (LinearLayout)
                            (inflater.inflate(R.layout.online_class_list, null)).findViewById(R.id.online_list_layout);
                }

                LinearLayout textContent = (LinearLayout) content.getChildAt(0);

                //去除最后一列的线
                if(i==5){
                    LinearLayout line = (LinearLayout) content.getChildAt(2);
                    line.setVisibility(View.INVISIBLE);
                }

                ImageView imageView = (ImageView) textContent.getChildAt(0);

                LinearLayout text = (LinearLayout) textContent.getChildAt(1);
                TextView name = (TextView) text.getChildAt(1);
                TextView link = (TextView) text.getChildAt(2);


                if(hasConnection)
                {
                    String[] texts = infoStr[i].split("<#>");
                    name.setText(texts[1]);
                    link.setText(texts[2]);

                    if(bitmap[i]!=null){

                        imageView.setImageBitmap(bitmap[i]);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    }

                }
                else
                {
                    name.setText("");
                    link.setText("");
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
                Intent intent= new Intent(MainActivity.mainActivity,OnLineClassActivity.class);
                intent.putExtra("mes",infoStr[i].split("<#>")[2]+"");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.question_set)
            startActivity(new Intent(MainActivity.mainActivity, QuestionSetSubjectActivity.class));

        else
            startActivity(new Intent(MainActivity.mainActivity, WrongSetSubjectActivity.class));
    }
}
