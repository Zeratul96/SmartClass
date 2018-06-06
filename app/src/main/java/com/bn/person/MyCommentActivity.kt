package com.bn.person

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.app.Activity
import android.os.Handler
import android.os.Message
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
import com.bn.question.CommentDetailActivity
import com.bn.smartclass_android.R
import com.bn.tool_package.ImageTools
import com.bn.tool_package.ListViewHeight
import com.bn.util.MyListViewAdapter
import com.bn.util.NetConnectionUtil

import java.util.ArrayList
import java.util.Arrays

class MyCommentActivity : Activity(), OnClickListener {

    internal var bitmap: Bitmap? = null
    internal var baseAdapter: MyListViewAdapter?=null
    internal var arrayList: MutableList<String>?=null

    internal var myHandler: Handler?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_comment_layout)

        if (myCommentActivity != null) myCommentActivity!!.finish()
        myCommentActivity = this

        findViewById(R.id.backView).setOnClickListener { this@MyCommentActivity.finish() }

        if (ImageTools.filesExists(Preference.appDataPath + Preference.sculptureFile))
            bitmap = ImageTools.getLocalBitmap(Preference.sculptureFile)
        else if (Preference.dataInfoList[9] == "")
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.login_sculpture)

        initMyComment()
    }


    private fun initMyComment() {
        val listView = findViewById(R.id.list_for_self_comment) as ListView
        myHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                when (msg.what) {
                    HandlerConstant.OPERATION_SUCCEED -> {
                        val bundle = msg.data
                        val info = bundle.getStringArray(BundleConstant.INFO_STR)
                        if (info != null)
                            arrayList = ArrayList(Arrays.asList(*info)) as ArrayList<String>
                        if (arrayList!![0] != Constant.NO_RECORD_FOUND)
                            initListView(listView, true)
                    }

                    HandlerConstant.CONNECTION_FAILURE -> Toast.makeText(this@MyCommentActivity, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show()

                    HandlerConstant.DELETE_SUCCEED -> {
                        initListView(listView, true)
                        System.gc()
                    }

                    HandlerConstant.COMMENT_DETAIL_SUCCEED -> startActivity(Intent(this@MyCommentActivity, CommentDetailActivity::class.java).putExtras(msg.data))
                }
            }
        }

        Thread(Runnable {
            val info = NetConnectionUtil.querySelfQuestionComment(Preference.dataInfoList[0])

            val message = Message()
            if (info != Constant.SERVER_CONNECTION_ERROR && info.length > 0) {
                message.what = HandlerConstant.OPERATION_SUCCEED
            } else
                message.what = HandlerConstant.CONNECTION_FAILURE

            val bundle = Bundle()
            val per = info.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            bundle.putStringArray(BundleConstant.INFO_STR, per)
            message.data = bundle


            myHandler!!.sendMessage(message)
        }).start()
    }

    /**
     * @param listView:listView对象
     * *
     * @param hasConnection:是否联网
     */
    private fun initListView(listView: ListView, hasConnection: Boolean) {
        baseAdapter = object : MyListViewAdapter(arrayList!!.size) {
            internal var inflater = LayoutInflater.from(this@MyCommentActivity)

            override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
                var content: LinearLayout
                if (view == null) {
                    content = inflater.inflate(R.layout.self_comment_list, null).findViewById(R.id.my_comment_layout) as LinearLayout
                }else
                {
                    content = view as LinearLayout
                }

                //去除最后一列的分割线
                if (i == arrayList!!.size - 1) {
                    val cutLine = content.getChildAt(2) as LinearLayout
                    cutLine.visibility = View.GONE
                }

                //拿到文字区
                val sculpture = content.findViewById(R.id.sculpture) as ImageView
                if (bitmap != null)
                    sculpture.setImageBitmap(bitmap)

                val userName = content.findViewById(R.id.user_name) as TextView
                val date = content.findViewById(R.id.date) as TextView
                val myComment = content.findViewById(R.id.my_comment) as TextView
                val questionContent = content.findViewById(R.id.question_content) as TextView

                val deleteButton = content.findViewById(R.id.delete) as TextView
                deleteButton.tag = i
                deleteButton.setOnClickListener(this@MyCommentActivity)


                //0#comment_id 1#question_id 2#comment_content 3#date 4#question_content
                val info = arrayList!!.toTypedArray()
                if (hasConnection) {
                    val piece = info[i].split("<#>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    userName.text = Preference.dataInfoList[6]
                    date.text = piece[3]
                    myComment.text = piece[2]
                    questionContent.text = piece[4]
                }
                return content
            }
        }

        listView.adapter = baseAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, l ->
            Thread(Runnable {
                val piece = arrayList!![i].split("<#>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                val result = NetConnectionUtil.queryQuestion("5#" + piece[1])
                val message = Message()
                if (result != null && result != Constant.SERVER_CONNECTION_ERROR && result != Constant.NO_RECORD_FOUND && result.length > 0) {
                    message.what = HandlerConstant.COMMENT_DETAIL_SUCCEED
                    val bundle = Bundle()
                    val questionInfo = result.split("<#>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    bundle.putStringArray(BundleConstant.INFO_STR, questionInfo)
                    message.data = bundle

                } else
                    message.what = HandlerConstant.CONNECTION_FAILURE

                myHandler!!.sendMessage(message)
            }).start()
        }

        ListViewHeight.setListViewHeight(listView)
    }

    override fun onClick(view: View) {
        val deletePosition = view.tag as Int
        val piece = arrayList!![deletePosition].split("<#>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        Thread(Runnable {
            val result = NetConnectionUtil.deleteQuestionComment(piece[0])
            val message = Message()
            if (result != null && result == Constant.OPERATION_SUCCEED)
                message.what = HandlerConstant.DELETE_SUCCEED
            else
                message.what = HandlerConstant.CONNECTION_FAILURE

            arrayList!!.removeAt(deletePosition)
            myHandler!!.sendMessage(message)
        }).start()

    }

    companion object {
        internal var myCommentActivity: MyCommentActivity? = null
    }
}
