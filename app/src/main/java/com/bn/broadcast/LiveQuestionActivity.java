package com.bn.broadcast;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.database.DataBaseUtil;
import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.smartclass_android.R;
import com.bn.tool_package.ImageTools;
import com.bn.util.NetConnectionUtil;

public class LiveQuestionActivity extends Activity implements View.OnClickListener{



    String[] allQuestionInfo;
    String[] piece;
    Bitmap bitmap ;
    ImageView questionImg;
    LinearLayout mainContent;
    Handler myHandler;

    LinearLayout[] rows;
    ImageView[] selectedView;

    //作答信息
    String answerStr;
    LinearLayout reference;
    LinearLayout analysisLayout;

    //答案信息
    int keyIndex;
    String keyStr;

    //题目数量变量
    int questionNumber;
    int currentQuestionNumber;

    public static boolean continueGetInfo = true;
    String questionInfo = "";



    //int intervalTime = (4*(Preference.PPTFrames+1))*1000;

    AudioManager audioManager;

    public static LiveQuestionActivity liveQuestionActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();

        questionInfo=bundle.getString(BundleConstant.QUERTION_INFO);
        getBroadcastInfo();

        //静音部分
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        liveQuestionActivity =this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Preference.isAutoPhoneMute)
            setPhoneMute();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(Preference.isAutoPhoneMute)
            cancelPhoneMute();
    }

    //设置静音模式
    private void setPhoneMute()
    {
        audioManager.setStreamVolume(AudioManager.MODE_NORMAL, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.MODE_RINGTONE, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    //取消静音恢复声音到默认大小
    private void cancelPhoneMute()
    {
        audioManager.setStreamVolume(AudioManager.MODE_NORMAL,
                3,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.MODE_RINGTONE,
                4, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    /**
     * 获得直播消息
     */
    private void getBroadcastInfo()
    {
        myHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                switch (msg.what)
                {
                    case HandlerConstant.OPERATION_SUCCEED:
                        Bundle bundle = msg.getData();

                        allQuestionInfo = bundle.getString(BundleConstant.INFO_STR).split("\\|");

                        if(allQuestionInfo[0].equals(Constant.SERVER_CONNECTION_ERROR)||allQuestionInfo[0].length()==0)
                        {
                            Toast.makeText(LiveQuestionActivity.this, "抱歉，读取题目发生错误。", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        questionNumber = allQuestionInfo.length;
                        showQuestionContent(currentQuestionNumber);
                        setAnswerListener();
                        break;

                    case HandlerConstant.CONNECTION_FAILURE:
                        //直播结束注意还原TeacherServer  pptImgData
                        Toast.makeText(LiveQuestionActivity.this, "糟糕，连接不上老师了，直播结束。", Toast.LENGTH_SHORT).show();
                        LiveQuestionActivity.this.finish();
                        break;

                    case HandlerConstant.UPDATE_UI:
                        questionImg.setImageBitmap(bitmap);
                        break;

                    //case HandlerConstant.DO_NOTHING:break;
                }
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {

                    Message message = new Message();

                    //如果是从PPT界面跳转过来 说明还没有加载习题数据 需要加载习题数据
                    if(questionInfo!=null&&!questionInfo.equals(Constant.NO_RECORD_FOUND))
                    {
                        Bundle bundle = new Bundle();
                        bundle.putString(BundleConstant.INFO_STR, getQuestionInfo());
                        message.setData(bundle);
                        message.what = HandlerConstant.OPERATION_SUCCEED;
                    }else
                    {
                        message.what = HandlerConstant.CONNECTION_FAILURE;
                    }
                    myHandler.sendMessage(message);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获得习题包数据
     */
    private String getQuestionInfo()
    {
        return NetConnectionUtil.queryQuestionInPackage(questionInfo.split("\\|")[0]);
    }

    /**
     * 下载题目图片
     * @param url:图片URL
     */
    private void downloadPicture(final String url)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] pictureData = NetConnectionUtil.downLoadPicture(url);
                if(pictureData!=null)
                {
                    bitmap = ImageTools.getBitmapFromByteArray(pictureData);
                    System.gc();
                    Message message = new Message();
                    message.what = HandlerConstant.UPDATE_UI;
                    myHandler.sendMessage(message);
                }
            }
        }).start();
    }

    //展示Layout
    private void showQuestionContent(int page)
    {
        //获得布局映射器
        LayoutInflater inflater = LayoutInflater.from(LiveQuestionActivity.this);

        String[] strings = allQuestionInfo[page].split("<#>");

        //信息排列顺序0#question_type 1#question_subject 2#question_content 3#selection_A 4#selection_B 5#selection_C
        // 6#selection_D 7#question_answer 8#question_analysis 9#question_id 10#user_id 11#question_status 12#question_picture
        if(strings[12]!=null&&strings[12].length()>0)
            piece = new String[]
                    {
                        strings[6],strings[2],strings[3],strings[8],strings[9],strings[10],strings[11],strings[7],
                        strings[4],strings[0],strings[1],strings[5],strings[12]
                    };
        else
            piece = new String[]
                    {
                        strings[6],strings[2],strings[3],strings[8],strings[9],strings[10],strings[11],strings[7],
                        strings[4],strings[0],strings[1],strings[5]
                    };



        //用映射器获得布局
        if(piece[0].equals("选择题"))
            mainContent = (LinearLayout) inflater.inflate(R.layout.question_choice, null);

        else if(piece[0].equals("填空题"))
            mainContent = (LinearLayout) inflater.inflate(R.layout.question_fillment, null);

        else
            mainContent = (LinearLayout) inflater.inflate(R.layout.question_judge, null);

        mainContent.setKeepScreenOn(true);
        //Activity标题（学科显示）
        FrameLayout title = (FrameLayout)mainContent.getChildAt(0);
        TextView titleName = (TextView)title.getChildAt(0);
        titleName.setText(piece[1]);

        //把×和工具栏删除
        ImageView cross = (ImageView) title.getChildAt(1);
        cross.setVisibility(View.GONE);

        mainContent.findViewById(R.id.tools).setVisibility(View.GONE);


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
            questionImg.setVisibility(View.GONE);
        else
            downloadPicture(piece[12]);

        //隐藏题目解析
        analysisLayout = (LinearLayout) mainContent.findViewById(R.id.analysis_layout);
        analysisLayout.setVisibility(View.GONE);
        if(piece[0].equals("填空题")){
            reference = (LinearLayout) mainContent.findViewById(R.id.reference_layout);
            reference.setVisibility(View.GONE);
        }

        setContentView(mainContent);
    }


    //设置每个选项监听器并且获取答案
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

            if(piece[7].equals("A"))
                keyIndex = 0;
            else if(piece[7].equals("B"))
                keyIndex = 1;
            else if(piece[7].equals("C"))
                keyIndex = 2;
            else keyIndex = 3;
        }
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

            if(piece[7].equals("正确"))
                keyIndex = 0;
            else
                keyIndex = 1;
        }
        else
        {
            keyStr = piece[7];

            final Button doneBtn = (Button) mainContent.findViewById(R.id.done_btn);
            doneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText writerEdit = (EditText) mainContent.findViewById(R.id.answer);
                    afterCheck(writerEdit.getText().toString().equals(keyStr));
                    doneBtn.setOnClickListener(null);
                }
            });
        }
    }

    public void onClick(View view)
    {
        if(view==rows[0])
        {
            afterCheck(changeSelectedImg(0));
        }
        else if(view==rows[1])
        {
            afterCheck(changeSelectedImg(1));
        }
        else if(view==rows[2])
        {
            afterCheck(changeSelectedImg(2));
        }
        else
        {
            afterCheck(changeSelectedImg(3));
        }
    }

    /**
     *根据选择改变形状
     * 选择答案之后移除监听
     */
    private boolean changeSelectedImg(int i)
    {
        boolean isCorrect = false;
        if(piece[0].equals("选择题"))
        {
            selectedView[i].setBackgroundResource(R.drawable.blue_circle);

            if(i==keyIndex)
                isCorrect = true;

            for(int m=0;m<4;m++)
                rows[m].setOnClickListener(null);
        }
        else
        {
            if(i==keyIndex)
                isCorrect = true;

            selectedView[i].setBackgroundResource(R.drawable.blue_circle);

            for(int m=0;m<2;m++)
                rows[m].setOnClickListener(null);
        }

        answerStr = i+"";

        return isCorrect;
    }

    /**
     * 再检查完是否答对之后开始向总服务器上传结果以便老师掌握正确率
     * 同时如果开启自动加入错题集模式答错之后该题直接加入错题集
     * @param isCorrect:表示这道题是否答对
     */
    private void afterCheck(boolean isCorrect)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append((piece[0].equals("填空题"))?"填空题|":answerStr+"|");
        sb.append((isCorrect)?"1|":"0|");
        sb.append(questionInfo.split("\\|")[1]+"#"+piece[9]);

        new Thread(new Runnable() {
            @Override
            public void run() {
                NetConnectionUtil.updateQuizResult(sb.toString());
            }
        }).start();

        //错题自动添加进错题集
        if(!isCorrect&&Preference.isAutoAddWrongSet)
            addToWrongSet();

        skipQuestion();
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


    /**
     * 跳转到下一题
     */
    private void skipQuestion()
    {
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(++currentQuestionNumber<questionNumber)
                {
                    showQuestionContent(currentQuestionNumber);
                    setAnswerListener();
                }
            }
        },500);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            if(!continueGetInfo)
            {
                LiveQuestionActivity.this.finish();
            }
        }
        return false;
    }

}
