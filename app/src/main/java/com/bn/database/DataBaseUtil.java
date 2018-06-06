package com.bn.database;

/**
 * Created by 13273 on 2017/4/19.
 *
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bn.parameter.Constant;

import java.util.ArrayList;
import java.util.List;

public class DataBaseUtil
{

    private static SQLiteDatabase sqLiteDatabase;

    //打开或者创建数据库
    public static void createOrOpenDataBase(int table)
    {

        try
        {
            sqLiteDatabase=SQLiteDatabase.openDatabase
                    (
                            "/data/data/com.bn.smartclass_android/databases",
                            null,
                            SQLiteDatabase.OPEN_READWRITE|SQLiteDatabase.CREATE_IF_NECESSARY
                    );

            String sql ;
            if(table==Constant.NOTES)
            {
                sql=
                        "create table if not exists notes(" +
                                "primary_key char(50) primary key,"+
                                "create_date char(30)," +
                                "content text" +
                                ")";
            }
            else
            {
                sql =
                        "create table if not exists wrong_set("+
                                "question_id varchar(50) primary key,"+
                                "subject varchar(50)" +
                                ")";
            }

            sqLiteDatabase.execSQL(sql);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    //关闭数据库
    public static void closeDatabase()
    {
        try
        {
            sqLiteDatabase.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //数据库插入信息
    public static void insert(String insertSQLStr)
    {
        try
        {
            sqLiteDatabase.execSQL(insertSQLStr);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //删除记录的方法
    public static void delete(String deleteSQLStr)
    {
        try
        {
            sqLiteDatabase.execSQL(deleteSQLStr);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //修改记录的方法
    public static void update(String updateSQLStr)
    {
        try
        {
            sqLiteDatabase.execSQL(updateSQLStr);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    //查询的方法 返回结果集
    public static List<String[]> query(String querySQLStr,int columns)
    {
        List<String[]> list = new ArrayList<>();
        Cursor cursor=sqLiteDatabase.rawQuery(querySQLStr,null);
        try
        {
            while (cursor.moveToNext())
            {
                String[] content = new String[columns];
                for(int i=0;i<columns;i++)
                {
                    content[i]=cursor.getString(i);
                }
                list.add(content);
            }
            cursor.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return list;
    }
}
