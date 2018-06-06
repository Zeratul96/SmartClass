package com.bn.onlineclient;

import com.bn.main.MainActivity;
import com.bn.parameter.Preference;

import java.util.Scanner;

/**
 * Created by 卢欢 on 2017/9/28.
 */

public class ClientMain {
    static ClientAgentThread cat;
    static String uid;
    static boolean flag=true;
    static Scanner sc;

    public static void toOnlineClass(String IpAdress)
    {
        uid= Preference.dataInfoList.get(0);
        while(uid.trim().length()==0)
        {
            System.out.println("提示>用户名不允许为空！");
            uid=Preference.dataInfoList.get(0);
        }
        System.out.println("提示>登录中...");
        cat=new ClientAgentThread(uid,IpAdress, MainActivity.mainActivity);
        cat.start();
        try
        {
            while(flag)
            {//循环接受用户输入
                String s=sc.nextLine();
                if(s.equals("quit"))
                {//如果用户输入quit
                    //发送下线请求消息
                    IOUtilCommonSocket.writeString("<#XX#>"+uid, cat.out);
                    break;
                }
                else
                {
                    //否则发送正常聊天消息
                    IOUtilCommonSocket.writeString("<#MSG#>"+uid+"|"+s+" ", cat.out);
                }
            }
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
        }
    }
}
