package com.bn.parameter;

import java.util.List;

/**
 * Created by 13273 on 2017/7/24.
 *Preference类用于存储一些基本数据和设置到内存以便全局使用
 */

public class Preference {

    public static final String serverIP = "10.129.62.250";

    //0#账号 ,1#密码 ,2#grade, 3#college, 4#major, 5#name, 6#nickname, 7#gender, 8#tel, 9#picture
    public static List<String> dataInfoList;
    public static final String appDataPath = "data/data/com.bn.smartclass_android/files/";
    public static final String sculptureFile = "sculpture.jpg";
    public static final String webPath ="SmartClass";
    public static final String filePrvider = "com.bn.person.provider";

    /**
     * 设置中的各项参数保存在以下对象中
     */
    public static int PPTFrames;
    public static boolean isAutoPhoneMute;
    public static boolean isShieldComments;
    public static boolean isShieldAds;
    public static boolean isAutoAddWrongSet;


    /**
     * 头像加载失败标志位
     */
    public static boolean downloadBitmapFailure = false;
}
