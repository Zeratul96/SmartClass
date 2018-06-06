package com.bn.question;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.smartclass_android.R;
import com.bn.tool_package.ImageTools;
import com.bn.tool_package.ListViewHeight;
import com.bn.util.MyListViewAdapter;
import com.bn.util.NetConnectionUtil;

/**
 * Created by 13273 on 2017/8/12.
 * 我的评论中点击详情展示的内容
 */

public class CommentDetailActivity extends Activity{

    //信息排列顺序0#question_type 1#question_subject 2#question_content 3#selection_A 4#selection_B 5#selection_C
    // 6#selection_D 7#question_answer 8#question_analysis 9#question_id 10#user_id 11#question_status 12#question_picture

    /*题目内容显示*/
    LayoutInflater inflater;
    LinearLayout mainContent;
    String[] questionInfo;
    Bitmap bitmap;
    Bitmap[] sculptures;
    ImageView questionImg;
    Handler myHandler;
    Thread netThread;
    ListView commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getIntent().getExtras();
        questionInfo = bundle.getStringArray(BundleConstant.INFO_STR);//获取学科所有题目信息

        //获得布局映射器
        inflater = LayoutInflater.from(CommentDetailActivity.this);
        //创建Handler线程
        createHandlerThread();
        //设置该ContentView内容
        showQuestionContent();

        findViewById(R.id.backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentDetailActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(netThread==null)
            createNetThread(null,true);

        netThread.start();
        netThread=null;
        System.gc();
    }

    private void createNetThread(final String url,final boolean skipImage)
    {

        netThread = new Thread(new Runnable() {
            @Override
            public void run() {

                if(!skipImage)
                    downloadImage(url);

                if(!Preference.isShieldComments)
                    initCommentData();
            }
        });
    }

    /**
     * 搜索题目评论数据
     */
    private void initCommentData()
    {
        String result = NetConnectionUtil.queryQuestionCommentInQuestion(questionInfo[9]);
        Message message = new Message();
        if(result!=null&&!result.equals(Constant.NO_RECORD_FOUND)&&!result.equals(Constant.SERVER_CONNECTION_ERROR)&&result.length()>0)
        {
            String[] comments = result.split("\\|");

            Bundle bundle = new Bundle();
            bundle.putStringArray(BundleConstant.INFO_STR, comments);
            message.setData(bundle);
            message.what = HandlerConstant.COMMENTS_LOAD_SUCCEED;

            //搜索成功继续加载头像
            System.gc();
            int bitmapLength = (comments.length>10)?10:comments.length;
            sculptures = new Bitmap[bitmapLength];
            for(int i=0;i<bitmapLength;i++)
            {
                System.gc();
                byte[] picData = NetConnectionUtil.downLoadPicture("ImageData/student/"+comments[i].split("<#>")[2]+".jpg");
                if(picData!=null)
                    sculptures[i] = ImageTools.getBitmapFromByteArray(picData);
                else sculptures[i] = BitmapFactory.decodeResource(getResources(),R.drawable.blank_circle);
            }
        }
        else if(result!=null&&result.equals(Constant.NO_RECORD_FOUND))
            message.what = HandlerConstant.NO_COMMENT_FOUND;

        else
            message.what = HandlerConstant.CONNECTION_FAILURE;

        myHandler.sendMessage(message);
    }

    /**
     * 初始化题目评论
     */
    private void initQuestionComment(final String[] infoData, final int mode)
    {

        MyListViewAdapter baseAdapter = null;

        if(mode==HandlerConstant.COMMENTS_LOAD_SUCCEED)
        {
            final int length = (infoData.length>10)?10:infoData.length;
            baseAdapter = new MyListViewAdapter(length) {
                @Override
                public View getView(int i, View view, ViewGroup viewGroup)
                {

                    String[] piece = infoData[i].split("<#>");

                    //0#comment_id  1#question_id  2#student_id  3#content  4#date  5#student_nickname
                    LinearLayout content = (LinearLayout) view;
                    if(view==null)
                        content = (LinearLayout)
                                inflater.inflate(R.layout.comment_list,null).findViewById(R.id.comment_list_layout);

                    if(i==length-1){
                        LinearLayout cutLine = (LinearLayout) content.getChildAt(2);
                        cutLine.setVisibility(View.GONE);
                    }

                    //拿到文本区
                    ImageView sculpture = (ImageView) content.findViewById(R.id.sculpture);
                    sculpture.setImageBitmap(sculptures[i]);

                    TextView userName = (TextView) content.findViewById(R.id.user_name);
                    TextView dateText = (TextView) content.findViewById(R.id.date);
                    TextView comment = (TextView) content.findViewById(R.id.comment);
                    userName.setText(piece[5]);
                    dateText.setText(piece[4]);
                    comment.setText(piece[3]);

                    return content;
                }
            };
        }
        else
        {
            baseAdapter = new MyListViewAdapter(1) {
                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {

                    LinearLayout mainContent = (LinearLayout)
                            inflater.inflate(R.layout.blank_comment_list, null).findViewById(R.id.blank_comment_layout);

                    TextView textView = (TextView) mainContent.getChildAt(0);

                    if(mode==HandlerConstant.COMMENTS_LOAD_FAILURE)
                        textView.setText("评论加载失败");
                    else if(mode==HandlerConstant.NO_COMMENT_FOUND)
                        textView.setText("暂无评论");

                    return mainContent;
                }
            };

        }

        commentList.setAdapter(baseAdapter);
        ListViewHeight.setListViewHeight(commentList);
    }


    /**
     * 用于设置每道题的界面
     */
    private void showQuestionContent()
    {
        //用映射器获得布局
        if(questionInfo[0].equals("选择题"))
            mainContent = (LinearLayout) inflater.inflate(R.layout.question_choice, null);

        else if(questionInfo[0].equals("填空题"))
            mainContent = (LinearLayout) inflater.inflate(R.layout.question_fillment, null);

        else
            mainContent = (LinearLayout) inflater.inflate(R.layout.question_judge, null);

        //Activity标题（学科显示）
        FrameLayout title = (FrameLayout)mainContent.getChildAt(0);
        TextView titleName = (TextView)title.getChildAt(0);
        titleName.setText(questionInfo[1]);

        commentList = (ListView) mainContent.findViewById(R.id.list_for_comment);
        if(Preference.isShieldComments)
        {
            commentList.setVisibility(View.GONE);
            mainContent.findViewById(R.id.view_comments).setVisibility(View.GONE);
            mainContent.findViewById(R.id.newest_comments).setVisibility(View.GONE);
            mainContent.findViewById(R.id.comment_view).setVisibility(View.GONE);
        }


        //展示题目内容
        TextView question = (TextView) mainContent.findViewById(R.id.question);
        question.setText(questionInfo[2]);
        if(questionInfo[0].equals("选择题"))
        {
            TextView selectionA = (TextView) mainContent.findViewById(R.id.a_content);
            TextView selectionB = (TextView) mainContent.findViewById(R.id.b_content);
            TextView selectionC = (TextView) mainContent.findViewById(R.id.c_content);
            TextView selectionD = (TextView) mainContent.findViewById(R.id.d_content);

            selectionA.setText(questionInfo[3]);
            selectionB.setText(questionInfo[4]);
            selectionC.setText(questionInfo[5]);
            selectionD.setText(questionInfo[6]);

            if(questionInfo[7].equals("0"))
            {
                ImageView imageView = (ImageView) mainContent.findViewById(R.id.a_view);
                imageView.setBackgroundResource(R.drawable.green_circle);
            }
            else if(questionInfo[7].equals("1"))
            {
                ImageView imageView = (ImageView) mainContent.findViewById(R.id.b_view);
                imageView.setBackgroundResource(R.drawable.green_circle);
            }
            else if(questionInfo[7].equals("2"))
            {
                ImageView imageView = (ImageView) mainContent.findViewById(R.id.c_view);
                imageView.setBackgroundResource(R.drawable.green_circle);
            }
            else
            {
                ImageView imageView = (ImageView) mainContent.findViewById(R.id.d_view);
                imageView.setBackgroundResource(R.drawable.green_circle);
            }

        }
        else if(questionInfo[0].equals("判断题"))
        {
            if(questionInfo[7].equals("0"))
            {
                ImageView imageView = (ImageView) mainContent.findViewById(R.id.right_view);
                imageView.setBackgroundResource(R.drawable.green_circle);
            }
            else
            {
                ImageView imageView = (ImageView) mainContent.findViewById(R.id.wrong_view);
                imageView.setBackgroundResource(R.drawable.green_circle);
            }

        }
        else
        {
            mainContent.findViewById(R.id.view1).setVisibility(View.GONE);
            mainContent.findViewById(R.id.writer_layout).setVisibility(View.GONE);
            mainContent.findViewById(R.id.view2).setVisibility(View.GONE);
            mainContent.findViewById(R.id.btn_view).setVisibility(View.GONE);
            mainContent.findViewById(R.id.btn_layout).setVisibility(View.GONE);
            mainContent.findViewById(R.id.view3).setVisibility(View.GONE);

            TextView textView = (TextView) mainContent.findViewById(R.id.standard_answer);
            textView.setText(questionInfo[7]);
        }

        TextView analysis = (TextView) mainContent.findViewById(R.id.analysis);
        analysis.setText(questionInfo[8]);

        mainContent.findViewById(R.id.tools).setVisibility(View.GONE);

        questionImg = (ImageView) mainContent.findViewById(R.id.question_image);
        if(questionInfo.length<13)
        {
            questionImg.setVisibility(View.GONE);
            createNetThread(null,true);
        }
        else
            createNetThread(questionInfo[12], false);


        setContentView(mainContent);
    }

    /**
     * 创建此Activity的Handler线程
     */
    private void createHandlerThread()
    {
        myHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                switch (msg.what)
                {
                    case HandlerConstant.DOWNLOAD_PICTURE_SUCCEED:
                        questionImg.setImageBitmap(bitmap);
                        break;

                    case HandlerConstant.COMMENTS_LOAD_SUCCEED:
                        initQuestionComment
                                (msg.getData().getStringArray(BundleConstant.INFO_STR ),HandlerConstant.COMMENTS_LOAD_SUCCEED);
                        break;


                    case HandlerConstant.COMMENTS_LOAD_FAILURE:
                        initQuestionComment
                                (null,HandlerConstant.COMMENTS_LOAD_FAILURE);
                        break;


                    case HandlerConstant.NO_COMMENT_FOUND:
                        initQuestionComment
                                (null,HandlerConstant.NO_COMMENT_FOUND);
                        break;
                }
            }
        };
    }


    /**
     * 下载所需要的题目图片
     * @param url：图片URL
     */
    private void downloadImage(final String url)
    {
        byte[] imgData = NetConnectionUtil.downLoadPicture(url);

        if(imgData!=null)
        {
            bitmap = ImageTools.getBitmapFromByteArray(imgData);

            Message message = new Message();
            message.what = HandlerConstant.DOWNLOAD_PICTURE_SUCCEED;
            myHandler.sendMessage(message);
        }

    }

}
