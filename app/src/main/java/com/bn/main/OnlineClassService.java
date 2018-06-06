package com.bn.main;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.bn.onlineclient.ClientMain;
import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.OnlineClassConstant;
import com.bn.tool_package.TimeTools;
import com.bn.util.NetConnectionUtil;

import static java.lang.Thread.sleep;

/**
 * Created by 卢欢 on 2017/9/14.
 */

public class OnlineClassService extends Service {
    String todayCourse;
    //当前时段符合上课时间的 课程名称
    String onlineClassName = "";
    OnlineThread onlineThread;
    boolean isOnline = false;
    public static boolean gotIp = false;
    public static boolean isGotIp =false;
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        todayCourse= bundle.getString(BundleConstant.TODAY_COURSE_INFO);

        onlineThread = new OnlineThread();
        onlineThread.start();

        GetOnlineClass();
        System.gc();
        return super.onStartCommand(intent, flags, startId);
    }
    private class OnlineThread extends Thread {
        Handler hd;
        @Override
        public void run() {
            Looper.prepare();
            hd = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case OnlineClassConstant.IS_ONLINE:
                             ClientMain.toOnlineClass(Constant.TEACHER_IP);
                            break;
                        case OnlineClassConstant.DO_NOTHING:
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }
    private void GetOnlineClass() {
        new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    while (!isOnline) {
                        sleep(5000);
                        Message message = new Message();
                        String[] course = todayCourse.split("\\|");//result = C程序设计<#>C程序设计-03<#>1<#>00:00<#>23:10
                        String[] courseMes;
                        for (int i = 0; i < course.length; i++) {
                            courseMes = course[i].split("<#>");
                            int beginNumebr = Integer.parseInt("1" + courseMes[3].split(":")[0] + courseMes[3].split(":")[1]);
                            int endNumber = Integer.parseInt("1" + courseMes[4].split(":")[0] + courseMes[4].split(":")[1]);
                            if (checkWhetherCouldIntoClass(beginNumebr, endNumber)) {
                                onlineClassName = courseMes[1];
                                break;
                            }
                        }
                        if(onlineClassName!=null && onlineClassName.length()>0) {
                            if(!gotIp) {
                                Constant.TEACHER_IP = NetConnectionUtil.queryIPAddress(onlineClassName);
                                if (Constant.TEACHER_IP.equals("0") &&todayCourse.length() > 0) {
                                    message.what = OnlineClassConstant.DO_NOTHING;
                                    onlineThread.hd.sendMessage(message);
                                }else{
                                    if(!Constant.TEACHER_IP.equals("<#ServerConnectionError#>")) {
                                        isGotIp =true;
                                        gotIp=true;
                                        message.what = OnlineClassConstant.IS_ONLINE;
                                        onlineThread.hd.sendMessage(message);
                                    }else {
                                        message.what = OnlineClassConstant.DO_NOTHING;
                                        onlineThread.hd.sendMessage(message);
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception e){e.printStackTrace();}
            }
        }).start();
    }

    /**
     * 检查是否可以进入教室
     * 核心算法：将开始时间结束时间转化为一个五位数（1+小时数+分钟数）
     * 如果当前时间大于开始时间小于结束时间则可以进入课堂
     * @param beginTime:该堂课开始时间
     * @param endTime:该堂课结束时间
     */
    private boolean checkWhetherCouldIntoClass(int beginTime, int endTime) {
        int nowTime = Integer.parseInt("1"+ TimeTools.getCurrentTimeInNumber());
        return (beginTime<=nowTime&&nowTime<endTime);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
