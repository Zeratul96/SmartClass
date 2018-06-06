package com.bn.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by 13273 on 2017/4/20.
 *
 */

public class MyContentProvider extends ContentProvider
{
    private static final UriMatcher matcher; //文本过滤器

    /**
     * addURI中的三个参数
     * contact 是你在清单文件中注册时所填写的属性
        path 是一个字符串 和contact组合成完整的uri , path用来区分你要进行的操作是添加,修改 删除 查询中的某一个
        code 是匹配成功的返回值
     */
    static
    {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);//常量UriMatcher.NO_MATCH表示不匹配任何路径的返回码
        matcher.addURI("smart_class.provider.app_data", "data",1);
    }

    SQLiteDatabase sqLiteDatabase;

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        switch (matcher.match(uri))
        {
            case 1:
                //query(String table,String []Columns, String selection, String[]selectionArgs, String having, string　orderBy);
                Cursor cur=sqLiteDatabase.query
                        (
                            "notes",
                            projection,//要查询的列名，可以是多个，可以为null，表示查询所有列
                            selection,//selection:查询条件，比如id=? and name=? 可以为null
                            selectionArgs,//selectionArgs:对查询条件赋值，一个问号对应一个值，按顺序 可以为null
                            null,//
                            null,//语法have 可为null
                            sortOrder//排序
                        );
            return cur;
        }
        return null;
    }

    @Override
    public boolean onCreate()
    {
        sqLiteDatabase=SQLiteDatabase.openDatabase
                (
                        "/data/data/com.bn.smartclass_android/databases", //数据库所在路径
                        null, 								//CursorFactory 使用默认游标工厂
                        SQLiteDatabase.CREATE_IF_NECESSARY //读写、若不存在则创建
                );

        return false;
    }

    @Override
    public String getType(Uri uri) {return null;}

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {return 0;}

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {return null;}

    @Override
    public int delete(Uri uri, String s, String[] strings) {return 0;}
}
