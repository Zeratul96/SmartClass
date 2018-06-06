package com.bn.tool_package;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;

import com.bn.parameter.Preference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Created by 13273 on 2017/7/28.
 *
 */

public class ImageTools {

    //判断文件是否存在
    public static boolean filesExists(String filePath)
    {
        try{
            File f = new File(filePath);
            if(!f.exists()) return false;
        }
        catch (Exception e){
            return false;
        }
        return true;
    }


    //从数组解码到BitMap
    public static Bitmap getBitmapFromByteArray(byte[] array) {
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }

    /**
     * 得到本地的图片
     * @param picName:图片名
     * @return: bitmap格式文件
     */
    public static Bitmap getLocalBitmap(String picName)
    {
        //pic的路径
        String picFilePath = Preference.appDataPath + picName;
        BitmapFactory.Options options = new BitmapFactory.Options();
        /* 不进行图片抖动处理 */
        options.inDither = false;
        /* 设置让解码器以最佳方式解码 */
        options.inPreferredConfig = null;
         /* 图片长宽方向缩小倍数 */
        options.inSampleSize = 1;
        return BitmapFactory.decodeFile(picFilePath, options);
    }


    /**
     * 将图片保存到本机中
     * @param bb:图片数组
     * @param picName:图片名称
     */
    public static void setLocalBitmap(byte[] bb, String picName)
    {
        //得到路径
        String filePath = Preference.appDataPath;
        InputStream input;
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        input = new ByteArrayInputStream(bb);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));
        bitmap = (Bitmap) softRef.get();
        try {
                input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
        FileOutputStream fos;
        file = new File(filePath + "/" + picName);
        try {
            fos = new FileOutputStream(file); // 读到SD卡中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            bitmap.recycle();
            System.gc();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 将图片保存到SD卡中（额外的存储路径）
     * @param bb:图片数组
     * @param picName:图片名称
     */
    public static void setBitmapInSD(byte[] bb, String picName)
    {
        //得到路径
        String filePath = Environment.getExternalStorageDirectory()+"/smart_class/";
        InputStream input;
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        input = new ByteArrayInputStream(bb);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));
        bitmap = (Bitmap) softRef.get();
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
        FileOutputStream fos;
        file = new File(filePath + "/" + picName);
        try {
            fos = new FileOutputStream(file); // 读到SD卡中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            bitmap.recycle();
            System.gc();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把Bitmap转化为Byte
     */
    public static byte[] BitmapToBytes(Bitmap bitmap)
    {
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        return fos.toByteArray();
    }
    /**
     * 圆形头像
     */
    public static Bitmap createCircleImage(Bitmap source)
    {
        int length = source.getWidth()<source.getHeight()?source.getWidth():source.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        Bitmap target = Bitmap.createBitmap(length,length,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.drawCircle(length/2, length/2, length/2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0 ,0, paint);
        return target;
    }

}
