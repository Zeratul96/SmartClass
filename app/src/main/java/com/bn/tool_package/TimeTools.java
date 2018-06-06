package com.bn.tool_package;

/**
 * Created by 13273 on 2017/7/26.
 *
 */

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeTools {

    public static String generateNumberByTime()
    {
        String time = "";
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        time = dateFormat.format(now.getTime());

        return time;
    }

    public static String generateContentFormatTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return dateFormat.format(new Date().getTime());
    }

    public static String getCurrentTimeInNumber()
    {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
        return dateFormat.format(now.getTime());
    }
}
