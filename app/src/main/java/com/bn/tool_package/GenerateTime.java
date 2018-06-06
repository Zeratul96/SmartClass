package com.bn.tool_package;

/**
 * Created by 13273 on 2017/7/26.
 *
 */

import java.text.SimpleDateFormat;
import java.util.Date;

public class GenerateTime {

    public static String generateNumberByTime()
    {
        String time = "";
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        time = dateFormat.format(now.getTime());

        return time;
    }

    public static String getCurrentYear()
    {
        String year="";
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        year = dateFormat.format(now.getTime());

        return year;
    }
}
