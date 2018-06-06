package com.bn.broadcast;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bn.onlineclient.ClientAgentThread;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.smartclass_android.R;
import com.bn.tool_package.ImageTools;

import static java.lang.Thread.sleep;

public class PPTActivity extends Activity {

    ImageView pptImg;

    public static boolean continueGetInfo = true;
    int intervalTime = (2*(Preference.PPTFrames+1))*1000;

    AudioManager audioManager;

    public static PPTActivity pptActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置为全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.ppt_layout);


        pptImg = (ImageView) findViewById(R.id.ppt_img);
        pptImg.setKeepScreenOn(true);

        pptActivity =this;
        getBroadcastInfo();

        //获取音频管理 静音部分
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
    //设置静音模式
    private void setPhoneMute() {
        audioManager.setStreamVolume(AudioManager.MODE_NORMAL, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);//普通音频
        audioManager.setStreamVolume(AudioManager.MODE_RINGTONE, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);//响铃
    }
    //取消静音恢复声音到默认大小
    private void cancelPhoneMute() {
        audioManager.setStreamVolume(AudioManager.MODE_NORMAL,
                3,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setStreamVolume(AudioManager.MODE_RINGTONE,
                4, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    private void getBroadcastInfo()
    {
        final Handler myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.OPERATION_SUCCEED:
                        if(ClientAgentThread.realData!=null) {
                            pptImg.setImageBitmap(ImageTools.getBitmapFromByteArray(ClientAgentThread.realData));
                        }else {
                            pptImg.setImageResource(R.drawable.welcome);
                        }
                        System.gc();
                        break;
                    case HandlerConstant.CONNECTION_FAILURE:
                        //直播结束注意还原TeacherServer  pptImgData  questionID
                        Toast.makeText(PPTActivity.this, "糟糕，连接不上老师了，直播结束。", Toast.LENGTH_SHORT).show();
                        PPTActivity.this.finish();
                        break;
                }
            }
        };
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    while (continueGetInfo) {
                        System.gc();
                        Message message = new Message();//data= ClientAgentThread.image;
                        if(ClientAgentThread.realData!=null) message.what = HandlerConstant.OPERATION_SUCCEED;
                            else {
                                message.what = HandlerConstant.CONNECTION_FAILURE;
                                continueGetInfo = false;
                            }
                        myHandler.sendMessage(message);
                       sleep(intervalTime);
                    }
                } catch (Exception e) {e.printStackTrace();}
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            if(!continueGetInfo) {PPTActivity.this.finish();}
        }
        return false;
    }
}
