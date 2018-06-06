package com.bn.onlineclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.bn.broadcast.LiveQuestionActivity;
import com.bn.broadcast.PPTActivity;
import com.bn.main.MainActivity;
import com.bn.main.OnlineClassService;
import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by 卢欢 on 2017/9/28.
 */

public class ClientAgentThread extends Thread {
    Socket sc;
    InputStream in;
    OutputStream out;
    boolean flag=true;
    Context context;
    public static byte[] image;
    public static byte[] realData;

    public ClientAgentThread(String uid, String IpAddress, Context c)
    {
        try {
            //发送Socket连接请求
            sc=new Socket(IpAddress,10056);
            this.context=c;
            //获取输入输出流
            in=sc.getInputStream();
            out=sc.getOutputStream();
            //发送上线请求消息
            IOUtilCommonSocket.writeString("<#SX#>"+uid, out);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try {
            loop:while(flag) {
                //接收长度
                int length=IOUtilCommonSocket.readIntNI(in);
                //读取此次的数据字节
                byte[] data=IOUtilCommonSocket.readBytes(in,length);
                //取前四个字节看类型
                byte[] tb={data[0],data[1],data[2],data[3]};
                int type=ConvertUtilCommonSocket.fromBytesToInt(tb);
                //取出数据字节
                realData= Arrays.copyOfRange(data, 4,data.length);
                switch(type)
                {
                    case 0://字符串
                        String msgStr=ConvertUtilCommonSocket.fromBytesToString(realData);
                        if(msgStr.startsWith("<#QuestionInfo#>")) {
                            Toast.makeText(context,"登录成功",Toast.LENGTH_SHORT);
                            //System.out.println("提示>登录成功");
                            if(Constant.BROADCAST_MODE==Constant.MODE_PPT) {
                                PPTActivity.pptActivity.finish();
                            }
                            Constant.BROADCAST_MODE=Constant.MODE_QUESTION;


                            String[] mes = msgStr.split("#>");

                            Bundle bundle = new Bundle();
                            bundle.putString(BundleConstant.QUERTION_INFO,mes[1]);
                            Intent intent = new Intent(MainActivity.mainActivity,LiveQuestionActivity.class);
                            intent.putExtras(bundle);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                        } else if(msgStr.startsWith("<#YHMC#>")) {
                            Toast.makeText(context,"用户名重",Toast.LENGTH_SHORT);
                            flag=false;
                            ClientMain.flag=false;
                            ClientMain.sc.close();
                            break loop;
                        }
                        else if(msgStr.startsWith("<#XX#>"))
                        {
                            int broadMode =Integer.parseInt(msgStr.split("#>")[1]);
                            if(broadMode == Constant.MODE_PPT){
                                PPTActivity.pptActivity.finish();
                            }else {LiveQuestionActivity.liveQuestionActivity.finish();}
                            OnlineClassService.gotIp=false;
                            Constant.TEACHER_IP="";
                            Toast.makeText(context,"下线成功",Toast.LENGTH_SHORT);
                            flag=false;
                            break loop;
                        }
                        break;
                    case 2:  //若是直播图片
                        //取出实际数据字节序列
                        image=realData;
                        if(OnlineClassService.isGotIp||Constant.BROADCAST_MODE==Constant.MODE_QUESTION)
                        {
                            if(Constant.BROADCAST_MODE==Constant.MODE_QUESTION)
                            {
                                LiveQuestionActivity.liveQuestionActivity.finish();
                            }
                            Constant.BROADCAST_MODE=Constant.MODE_PPT;
                            OnlineClassService.isGotIp=false;
                            Intent intent = new Intent(MainActivity.mainActivity,PPTActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                        break;
                }
            }
            in.close();
            out.close();
            sc.close();
            System.out.println("客户端通讯完毕......");
        }catch(Exception e){
            System.out.println(e.toString());
            ClientMain.flag=false;
            ClientMain.sc.close();
        }
    }
}
