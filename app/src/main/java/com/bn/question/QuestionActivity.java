package com.bn.question;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.bn.database.DataBaseUtil;
import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.smartclass_android.R;
import com.bn.tool_package.ImageTools;
import com.bn.tool_package.ListViewHeight;
import com.bn.util.MyListViewAdapter;
import com.bn.util.NetConnectionUtil;

public class QuestionActivity extends Activity implements OnClickListener{

    //信息排列顺序0#question_type 1#question_subject 2#question_content 3#selection_A 4#selection_B 5#selection_C
    // 6#selection_D 7#question_answer 8#question_analysis 9#question_id 10#user_id 11#question_status 12#question_picture

    /*题目内容显示*/
    LayoutInflater inflater;
    LinearLayout mainContent;
    String[] questionInfo,piece;
    Bitmap bitmap;
    Bitmap[] sculptures;
    ImageView questionImg;
    LinearLayout[] rows;
    ImageView[] selectedView;
    Handler myHandler;
    ListView commentList;
    Thread netThread;

    /*作答与答案*/
    LinearLayout reference,analysisLayout;
    int keyIndex;
    String keyStr;

    //答案背景颜色
    LinearLayout[] questionBg;

    /*题目翻页*/
    int questionPage,maxPage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getIntent().getExtras();
        questionInfo = bundle.getStringArray(BundleConstant.INFO_STR);//获取学科所有题目信息

        if(questionInfo!=null)
            maxPage = questionInfo.length;//学科所有的题目数量

        //获得布局映射器
        inflater = LayoutInflater.from(QuestionActivity.this);
        //创建Handler线程
        createHandlerThread();
        //设置该ContentView内容
        showQuestionContent(questionPage);
        //将所有与题目作答相关内容添加监听
        setAnswerListener();
        //初始化工具栏
        initToolsBar();

        findViewById(R.id.backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!netThread.isAlive()||netThread==null){
            createNetThread(null,true);
            netThread.start();
        }

        System.gc();
    }

    /**
     *加载评论和下载图片放到一个线程防止线程脏读
     * @param url:图片URL地址
     * @param skipImage：跳过图片下载
     */
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
        String result = NetConnectionUtil.queryQuestionCommentInQuestion(questionInfo[questionPage].split("<#>")[9]);
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
        MyListViewAdapter baseAdapter;

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
     * 创建此Activity的Handler线程
     */
    private void createHandlerThread() {
        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
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
     * 用于设置每道题的界面
     * @param i:题目编号
     */
    private void showQuestionContent(int i)
    {

        piece = questionInfo[i].split("<#>");

        //用映射器获得布局
        if(piece[0].equals("选择题"))
            mainContent = (LinearLayout) inflater.inflate(R.layout.question_choice, null);

        else if(piece[0].equals("填空题"))
            mainContent = (LinearLayout) inflater.inflate(R.layout.question_fillment, null);

        else
            mainContent = (LinearLayout) inflater.inflate(R.layout.question_judge, null);

        //Activity标题（学科显示）
        FrameLayout title = (FrameLayout)mainContent.getChildAt(0);
        TextView titleName = (TextView)title.getChildAt(0);
        titleName.setText(piece[1]);


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
        question.setText(piece[2]);
        if(piece[0].equals("选择题"))
        {
            TextView selectionA = (TextView) mainContent.findViewById(R.id.a_content);
            TextView selectionB = (TextView) mainContent.findViewById(R.id.b_content);
            TextView selectionC = (TextView) mainContent.findViewById(R.id.c_content);
            TextView selectionD = (TextView) mainContent.findViewById(R.id.d_content);

            selectionA.setText(piece[3]);
            selectionB.setText(piece[4]);
            selectionC.setText(piece[5]);
            selectionD.setText(piece[6]);
        }

        questionImg = (ImageView) mainContent.findViewById(R.id.question_image);
        if(piece.length<13)
        {
            questionImg.setVisibility(View.GONE);
            createNetThread(null,true);
        }
        else
            createNetThread(piece[12],false);

        //在未答题前隐藏题目解析
        analysisLayout = (LinearLayout) mainContent.findViewById(R.id.analysis_layout);
        analysisLayout.setVisibility(View.GONE);
        if(piece[0].equals("填空题")){
            reference = (LinearLayout) mainContent.findViewById(R.id.reference_layout);
            reference.setVisibility(View.GONE);
        }

        setContentView(mainContent);
    }


    /**
     * 下载所需要的题目图片 初始化评论
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

    //设置每个选项监听器并且获取标准答案（填空题直接进行正确错误判断）
    private void setAnswerListener()
    {
        if(piece[0].equals("选择题"))
        {

            rows = new LinearLayout[]
                    {
                        (LinearLayout) mainContent.findViewById(R.id.A),(LinearLayout) mainContent.findViewById(R.id.B),
                        (LinearLayout) mainContent.findViewById(R.id.C), (LinearLayout) mainContent.findViewById(R.id.D)
                    };
            for(int i=0;i<4;i++)
                rows[i].setOnClickListener(this);

            selectedView = new ImageView[]
                    {
                        (ImageView) mainContent.findViewById(R.id.a_view), (ImageView) mainContent.findViewById(R.id.b_view),
                        (ImageView) mainContent.findViewById(R.id.c_view), (ImageView) mainContent.findViewById(R.id.d_view)
                    };

            if(piece[7].equals("A")) keyIndex = 0;
            else if(piece[7].equals("B")) keyIndex = 1;
            else if(piece[7].equals("C")) keyIndex = 2;
            else keyIndex = 3;

        }//选择题结束

        else if(piece[0].equals("判断题"))
        {
           rows = new LinearLayout[]
                   {
                        (LinearLayout) mainContent.findViewById(R.id.right),(LinearLayout) mainContent.findViewById(R.id.wrong),
                   };

            rows[0].setOnClickListener(this);
            rows[1].setOnClickListener(this);

            selectedView = new ImageView[]
                    {
                        (ImageView) mainContent.findViewById(R.id.right_view), (ImageView) mainContent.findViewById(R.id.wrong_view)
                    };

            if(piece[7].equals("正确")) keyIndex = 0;
            else keyIndex = 1;

        }//判断题结束

        else
        {
            keyStr = piece[7];

            Button doneBtn = (Button) mainContent.findViewById(R.id.done_btn);
            doneBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText writerEdit = (EditText) mainContent.findViewById(R.id.answer);
                    afterCheck(writerEdit.getText().toString().equals(keyStr));
                }
            });

        }//填空题结束
    }


    /**
     * 点击事件的回调。仅限判断题、选择题
     * @param view:控件
     */
    @Override
    public void onClick(View view)
    {
        if(view==rows[0])
            afterCheck(changeSelectedImg(0));
        else if(view==rows[1])
            afterCheck(changeSelectedImg(1));
        else if(view==rows[2])
            afterCheck(changeSelectedImg(2));
        else
            afterCheck(changeSelectedImg(3));
    }

    /**
     *根据对错改变勾的形状
     * 选择答案之后移除监听
     */
    private boolean changeSelectedImg(int i)
    {
        boolean isCorrect = false;
        if(piece[0].equals("选择题"))
        {
            if(i==keyIndex){
                selectedView[i].setBackgroundResource(R.drawable.green_circle);
                rows[i].setBackgroundColor(getResources().getColor(R.color.correct_color));
                isCorrect = true;
            }else {
                if(i!=-1)
                {
                    selectedView[i].setBackgroundResource(R.drawable.red_circle);
                    rows[i].setBackgroundColor(getResources().getColor(R.color.fault_color));
                }

                selectedView[keyIndex].setBackgroundResource(R.drawable.green_circle);
            }

            for(int m=0;m<4;m++)
                rows[m].setOnClickListener(null);
        }

        else
        {
            if(i==keyIndex){
                selectedView[i].setBackgroundResource(R.drawable.green_circle);
                rows[i].setBackgroundColor(getResources().getColor(R.color.correct_color));
                isCorrect = true;
            }
            else{
                if(i!=-1)
                {
                    selectedView[i].setBackgroundResource(R.drawable.red_circle);
                    rows[i].setBackgroundColor(getResources().getColor(R.color.fault_color));
                }


                selectedView[keyIndex].setBackgroundResource(R.drawable.green_circle);
            }

            for(int m=0;m<2;m++)
                rows[m].setOnClickListener(null);

        }

        return isCorrect;
    }


    /**
     * 判断完对错的操作
     * @param isCorrect:是否正确
     */
    private void afterCheck(boolean isCorrect)
    {
        //如果回答正确跳至下一题
        if(isCorrect)
        {
            questionPage++;
            if(questionPage<maxPage)
            {
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        skipQuestion();
                    }
                },500);
            }

            else Toast.makeText(this, "你已经刷完该门课的所有题目啦，快去休息休息吧。", Toast.LENGTH_SHORT).show();
        }

        //回答错误显示试题分析
        else
        {
            showQuestionAnalysis();
            if(Preference.isAutoAddWrongSet)
                addToWrongSet();
        }

    }

    /**
     * 跳转题目
     */
    private void skipQuestion()
    {
        showQuestionContent(questionPage);
        netThread.start();
        setAnswerListener();
        initToolsBar();

        findViewById(R.id.backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionActivity.this.finish();
            }
        });

        System.gc();
    }

    /**
     * 显示答案及答案解析
     */
    private void showQuestionAnalysis()
    {
        if(piece[0].equals("填空题"))
        {
            findViewById(R.id.answer).setEnabled(false);
            //将两个部件消失
            findViewById(R.id.btn_view).setVisibility(View.GONE);
            findViewById(R.id.btn_layout).setVisibility(View.GONE);

            reference.setVisibility(View.VISIBLE);
            TextView textView = (TextView) findViewById(R.id.standard_answer);
            textView.setText(keyStr);
        }

        analysisLayout.setVisibility(View.VISIBLE);
        TextView analysisTextView = (TextView) analysisLayout.getChildAt(3);
        analysisTextView.setText(piece[8]);
    }

    /**
     * 初始化底下的工具栏按钮
     */
    private void initToolsBar()
    {
        //上一题
        findViewById(R.id.back_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(questionPage==0)
                    Toast.makeText(QuestionActivity.this, "别按啦，这就是第一道题目喔。", Toast.LENGTH_SHORT).show();
                else
                {
                    questionPage--;
                    skipQuestion();
                }

            }
        });

        //添加进错题集
        findViewById(R.id.add_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                addToWrongSet();
                Toast.makeText(QuestionActivity.this, "添加至错题集成功", Toast.LENGTH_SHORT).show();
            }
        });


        //展示答案
        findViewById(R.id.answer_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!piece[0].equals("填空题"))
                    changeSelectedImg(-1);

                showQuestionAnalysis();
            }
        });



        //进行评论
        findViewById(R.id.comment_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString(BundleConstant.INFO_STR, questionInfo[questionPage].split("<#>")[9]);
                startActivity(new Intent(QuestionActivity.this, CommentQuestionActivity.class).putExtras(bundle));

            }
        });


        //下一题
        findViewById(R.id.forward_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(questionPage==maxPage-1)
                    Toast.makeText(QuestionActivity.this, "别按啦，这已经是最后一道题目啦。", Toast.LENGTH_SHORT).show();

                else
                {
                    questionPage++;
                    skipQuestion();
                }
            }
        });
    }

    /**
     * 添加到错题集
     */
    private void addToWrongSet()
    {
        String[] values = new String[]
                {
                        "'"+piece[9]+"'",
                        "'"+piece[1]+"'"
                };

        DataBaseUtil.createOrOpenDataBase(Constant.WRONG_SET);
        DataBaseUtil.insert("insert into wrong_set values("+values[0]+","+values[1]+")");
        DataBaseUtil.closeDatabase();
    }

}
