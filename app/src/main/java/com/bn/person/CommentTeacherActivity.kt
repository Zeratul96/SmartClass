package com.bn.person


import android.os.Bundle
import android.app.Activity
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.view.View.OnClickListener
import android.widget.Toast

import com.bn.parameter.BundleConstant
import com.bn.parameter.Constant
import com.bn.parameter.HandlerConstant
import com.bn.parameter.Preference
import com.bn.smartclass_android.R
import com.bn.tool_package.TimeTools
import com.bn.util.NetConnectionUtil

class CommentTeacherActivity : Activity(), OnClickListener, TextWatcher {


    internal var teacherFeedback: EditText?=null
    internal var finishButton: TextView?=null
    internal var notShowID: CheckBox?=null
    internal var subjectName: String?=null
    internal var teacherID: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comment_teacher_layout)

        findViewById(R.id.backView).setOnClickListener(this)
        finishButton = findViewById(R.id.confirm_change) as TextView
        finishButton!!.isEnabled = false
        finishButton!!.setOnClickListener(this)

        teacherFeedback = findViewById(R.id.detail_suggestion) as EditText
        teacherFeedback!!.addTextChangedListener(this)
        notShowID = findViewById(R.id.check_box) as CheckBox

        val bundle = this.intent.extras
        //学科名|老师ID
        val ss = bundle.getString(BundleConstant.INFO_STR)
        val piece = ss!!.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        subjectName = piece[0]
        teacherID = piece[1]

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.backView -> this.finish()

            R.id.confirm_change -> confirmChange()
        }
    }


    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        finishButton!!.isEnabled = teacherFeedback!!.text.toString() != ""
    }

    override fun afterTextChanged(editable: Editable) {}


    private fun confirmChange() {
        val myHandler = object : Handler() {

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                when (msg.what) {
                    HandlerConstant.OPERATION_SUCCEED -> {
                        Toast.makeText(this@CommentTeacherActivity, "评价教师成功，感谢您的反馈。", Toast.LENGTH_SHORT).show()
                        this@CommentTeacherActivity.finish()
                    }

                    HandlerConstant.CONNECTION_FAILURE -> Toast.makeText(this@CommentTeacherActivity, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show()


                }
            }
        }


        Thread(Runnable {
            val sb = StringBuilder()
            sb.append(TimeTools.generateNumberByTime() + "|")
            sb.append(teacherID + "|")
            sb.append(if (notShowID!!.isChecked) "" else Preference.dataInfoList[0])
            sb.append("|$subjectName|")
            sb.append(teacherFeedback!!.text.toString() + "|")

            val result = NetConnectionUtil.insertTeacherFeedback(sb.toString())
            val message = Message()
            if (result == Constant.OPERATION_SUCCEED)
                message.what = HandlerConstant.OPERATION_SUCCEED
            else if (result == Constant.SERVER_CONNECTION_ERROR)
                message.what = HandlerConstant.CONNECTION_FAILURE
            else
                message.what = -1

            myHandler.sendMessage(message)
        }).start()
    }
}
