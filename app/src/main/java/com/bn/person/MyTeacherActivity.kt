package com.bn.person

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.app.Activity
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.bn.parameter.BundleConstant
import com.bn.parameter.Constant
import com.bn.parameter.HandlerConstant
import com.bn.parameter.Preference
import com.bn.smartclass_android.R
import com.bn.tool_package.ImageTools
import com.bn.tool_package.ListViewHeight
import com.bn.util.MyListViewAdapter
import com.bn.util.NetConnectionUtil



class MyTeacherActivity : Activity(), OnClickListener {
    internal var bitmaps: Array<Bitmap> ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_teacher_layout)

        //防止Activity在栈中残存
        if (myTeacherActivity != null) myTeacherActivity!!.finish()
        myTeacherActivity = this

        findViewById(R.id.backView).setOnClickListener(this)
        initTeacher()

    }

    override fun onClick(view: View) {
        this.finish()

    }

    private fun initTeacher() {
        val listView = findViewById(R.id.list_for_teacher) as ListView
        val myHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                val bundle = msg.data
                val info = bundle.getStringArray(BundleConstant.INFO_STR)

                when(msg!!.what)
                {
                    HandlerConstant.OPERATION_SUCCEED->
                    {
                        if (info!![0]!=Constant.NO_RECORD_FOUND)
                        {
                            initListView(listView, info, true)
                        }
                    }
                    HandlerConstant.CONNECTION_FAILURE->
                    {
                        Toast.makeText(this@MyTeacherActivity, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        Thread(Runnable {
            //course_name,teacher_name,teacher_tel,teacher_email,teacher_picture, teacher_id
            val info = NetConnectionUtil.queryCourseAndTeacher(Preference.dataInfoList[0])


            val message = Message()
            if (info != Constant.SERVER_CONNECTION_ERROR && info.length > 0) {
                message.what = HandlerConstant.OPERATION_SUCCEED
            } else
                message.what = HandlerConstant.CONNECTION_FAILURE

            val bundle = Bundle()
            val per = info.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            bundle.putStringArray(BundleConstant.INFO_STR, per)
            message.data = bundle

            //如果网络通畅则继续加载图片
            if (message.what == HandlerConstant.OPERATION_SUCCEED) {
                val length:Int = per.size
                bitmaps  = arrayOfNulls<Bitmap>(length) as Array<Bitmap>

                val url = arrayOfNulls<String>(length)
                for (i in 0..length - 1) {
                    val piece = per[i].split("<#>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    url[i] = piece[4]
                }

                for (i in 0..length - 1) {
                    val picData = NetConnectionUtil.downLoadPicture(url[i])
                    if (picData != null)
                        bitmaps!![i] = ImageTools.getBitmapFromByteArray(picData)
                }

            }

            myHandler.sendMessage(message)
        }).start()
    }

    private fun initListView(listView: ListView, info: Array<String>, hasConnection: Boolean) {

        val baseAdapter = object : MyListViewAdapter(info.size) {
            internal var inflater = LayoutInflater.from(this@MyTeacherActivity)
            //0#course_name, 1#teacher_name, 2#teacher_tel, 3#teacher_email, 4#teacher_picture 5#teacher_id

            override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
                var content: LinearLayout
                if (view == null) {
                    content = inflater.inflate(R.layout.teacher_list, null).findViewById(R.id.teacher_layout) as LinearLayout
                }else
                {
                    content = view as LinearLayout
                }

                val textContents = content!!.getChildAt(0) as LinearLayout
                val teacherImg = textContents.getChildAt(0) as ImageView

                //去除最后一列的线
                if (i == info.size - 1) {
                    val line = content!!.getChildAt(2) as LinearLayout
                    line.visibility = View.INVISIBLE
                }

                //拿到文字区
                val textsArea = textContents.getChildAt(1) as LinearLayout

                val subjectLayout = textsArea.getChildAt(1) as LinearLayout
                val teacherLayout = textsArea.getChildAt(2) as LinearLayout
                val emailLayout = textsArea.getChildAt(3) as LinearLayout
                val telLayout = textsArea.getChildAt(4) as LinearLayout

                val subject = subjectLayout.getChildAt(1) as TextView
                val teacher = teacherLayout.getChildAt(1) as TextView
                val email = emailLayout.getChildAt(1) as TextView
                val tel = telLayout.getChildAt(1) as TextView

                if (hasConnection) {
                    val perInfo = info[i].split("<#>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    subject.text = perInfo[0]
                    teacher.text = perInfo[1]
                    email.text = perInfo[3]
                    tel.text = perInfo[2]

                    if (bitmaps!![i] != null) {
                        teacherImg.setImageBitmap(bitmaps!![i])
                        teacherImg.scaleType = ImageView.ScaleType.FIT_XY
                    }
                }
                return content
            }
        }

        listView.adapter = baseAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val bundle = Bundle()
            val strings = info[i].split("<#>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val ss = strings[0] + "|" + strings[5]
            bundle.putString(BundleConstant.INFO_STR, ss)

            startActivity(Intent(this@MyTeacherActivity, CommentTeacherActivity::class.java).putExtras(bundle))
        }
        ListViewHeight.setListViewHeight(listView)
    }

    companion object { internal var myTeacherActivity: MyTeacherActivity? = null }
}
