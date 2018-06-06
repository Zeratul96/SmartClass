package com.bn.person

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bn.parameter.Constant
import com.bn.parameter.HandlerConstant
import com.bn.parameter.Preference
import com.bn.parameter.SavedDataConstant
import com.bn.smartclass_android.R
import com.bn.tool_package.ImageTools
import com.bn.util.NetConnectionUtil
import com.bn.util.SelectPicturePopupWindow

import java.io.File

class PersonalSculptureActivity : Activity(), SelectPicturePopupWindow.OnSelectedListener {
    internal var sculpture: ImageView?=null
    private var mSelectPicturePopupWindow: SelectPicturePopupWindow? = null
    internal var sp: SharedPreferences?=null
    private var imageUri: Uri? = null//相机拍照图片保存地址
    private var outputUri: Uri? = null//裁剪万照片保存地址
    private val mImagePath = Environment.getExternalStorageDirectory().toString() + "/meta/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.personal_sculpture_layout)
        personalSculptureActivity = this

        sculpture = findViewById(R.id.self_sculpture) as ImageView
        val bitmap = ImageTools.getLocalBitmap(Preference.sculptureFile)
        if (bitmap != null)
            sculpture!!.setImageBitmap(bitmap)
        else
            sculpture!!.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.login_sculpture))
        findViewById(R.id.backView).setOnClickListener { this@PersonalSculptureActivity.finish() }

        initPopupWindow()

        findViewById(R.id.selection).setOnClickListener { mSelectPicturePopupWindow!!.showPopupWindow(this@PersonalSculptureActivity) }
    }
    override fun OnSelected(v: View, position: Int) {
        when (position) {
            Constant.TAKE_PHOTO -> takePhoto()
            Constant.SELECT_PICTURE -> pickFromGallery()
            Constant.CANCEL -> mSelectPicturePopupWindow!!.dismissPopupWindow()
        }
    }
    private fun initPopupWindow() {
        mSelectPicturePopupWindow = SelectPicturePopupWindow(this@PersonalSculptureActivity)
        mSelectPicturePopupWindow!!.setOnSelectedListener(this)
    }
    private fun takePhoto() {
        mSelectPicturePopupWindow!!.dismissPopupWindow()

        val outputImage = File(mImagePath, "output_image.jpg")
        if (!outputImage.parentFile.exists()) {
            outputImage.parentFile.mkdirs()
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(outputImage)
        } else {
            //Android 7.0系统开始 使用本地真实的Uri路径不安全,使用FileProvider封装共享Uri
            //参数二:fileprovider绝对路径 com.bn.person.provider：项目包名
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            imageUri = FileProvider.getUriForFile(personalSculptureActivity, Preference.filePrvider, outputImage)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        // 启动相机程序
        startActivityForResult(intent, Constant.TAKE_PHOTO)
    }

    private fun pickFromGallery() {
        mSelectPicturePopupWindow!!.dismissPopupWindow()
        val pickIntent = Intent(Intent.ACTION_GET_CONTENT)
        pickIntent.type = "image/*"
        startActivityForResult(pickIntent, Constant.SELECT_PICTURE)
    }

    fun startPhotoZoom(uri: Uri) {
        val file = File(mImagePath, "crop_image.jpg")
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        outputUri = Uri.fromFile(file)
        val intent = Intent("com.android.camera.action.CROP")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        intent.setDataAndType(uri, "image/*")//设置裁剪格式
        intent.putExtra("crop", "true")//调用裁剪过程 如果不设置就会跳过裁剪过程
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("scale", true)
        // outputX outputY 是裁剪图片宽高。单位：像素
        intent.putExtra("outputX", 150)
        intent.putExtra("outputY", 150)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())//输出图片格式
        intent.putExtra("noFaceDetection", true)//取消人脸识别
        intent.putExtra("return-data", false)
        startActivityForResult(intent, Constant.CROP_SMALL_PICTURE)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        // 如果返回码是可以用的
        if (resultCode == Activity.RESULT_OK)
        {
            when (requestCode) {
                Constant.TAKE_PHOTO -> startPhotoZoom(imageUri!!)//裁剪图片
                Constant.SELECT_PICTURE -> startPhotoZoom(data!!.data)
                Constant.CROP_SMALL_PICTURE -> //剪裁后得到的图片
                {
                    if (data != null)
                    {
                        setImageToView(data!!)
                    }
                }
            }
        }
    }

    private fun setImageToView(intent: Intent) {
        val imageData = intent.extras
        var bitmap: Bitmap ?= null
        try {
            bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(outputUri!!))
        }catch (e: Exception) {
            e.printStackTrace()
        }
        sculpture!!.setImageBitmap((bitmap!!))
        if (imageData != null)
            savePicture(bitmap)
    }
    private fun savePicture(bitmap: Bitmap) {
        val myHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == HandlerConstant.CONNECTION_FAILURE)
                    Toast.makeText(this@PersonalSculptureActivity, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show()
            }
        }

        Thread(Runnable {
            val pictureData = ImageTools.BitmapToBytes(bitmap)
            val sculptureName = "5#" + "ImageData/student/" + Preference.dataInfoList[0] + ".jpg" + "|" + Preference.dataInfoList[0]

            val result = NetConnectionUtil.updateStudentPicture(pictureData, sculptureName)

            val msg = Message()
            if (result == Constant.OPERATION_SUCCEED) {
                //服务器上路径保存至内存
                Preference.dataInfoList[9] = "ImageData/student/" + Preference.dataInfoList[0] + ".jpg"
                //图片保存至手机中
                ImageTools.setLocalBitmap(pictureData, Preference.sculptureFile)
                Preference.downloadBitmapFailure = false
                //服务器路径保存至磁盘
                sp = getSharedPreferences(SavedDataConstant.USER, Context.MODE_PRIVATE)
                val editor = sp!!.edit()
                editor.putString(SavedDataConstant.PICTURE_ADDRESS, "ImageData/student/" + Preference.dataInfoList[0] + ".jpg")
                editor.apply()

                msg.what = HandlerConstant.OPERATION_SUCCEED
            } else
                msg.what = HandlerConstant.CONNECTION_FAILURE

            myHandler.sendMessage(msg)
        }).start()
    }

    companion object {
        internal var personalSculptureActivity: PersonalSculptureActivity?=null
    }
}

