package com.bn.person

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bn.parameter.Constant
import com.bn.parameter.HandlerConstant
import com.bn.parameter.Preference
import com.bn.parameter.SavedDataConstant
import com.bn.smartclass_android.R
import com.bn.util.NetConnectionUtil


class UpdateUserNickNameActivity : Activity() {

    internal var nickName: EditText ? = null
    internal var saveBtn: TextView ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nickname_layout)

        findViewById(R.id.backView).setOnClickListener { this@UpdateUserNickNameActivity.finish() }

        saveBtn = findViewById(R.id.confirm_change) as TextView
        saveBtn!!.setOnClickListener { confirmChange() }
        saveBtn!!.isEnabled = false

        nickName = findViewById(R.id.update_nickname) as EditText
        nickName!!.setText(Preference.dataInfoList[6])
        nickName!!.setSelection(nickName!!.length())
        nickName!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                saveBtn!!.isEnabled = !(nickName!!.text.toString() == "" || nickName!!.text.toString() == Preference.dataInfoList[6])
            }

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {}
        })

    }


    private fun confirmChange() {
        val mHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                when (msg.what) {
                    HandlerConstant.CONNECTION_FAILURE -> Toast.makeText(this@UpdateUserNickNameActivity, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show()

                    HandlerConstant.OPERATION_SUCCEED -> this@UpdateUserNickNameActivity.finish()
                }
            }
        }

        Thread(Runnable {
            val message = "2#" + nickName!!.text.toString() + "|" + Preference.dataInfoList[0]
            val result = NetConnectionUtil.updateStudent(message)
            val msg = Message()

            if (result != null && result == Constant.OPERATION_SUCCEED) {
                Preference.dataInfoList[6] = nickName!!.text.toString()

                val sp = getSharedPreferences(SavedDataConstant.USER, Context.MODE_PRIVATE)
                val editor = sp.edit()
                editor.putString(SavedDataConstant.TEL, nickName!!.text.toString())
                editor.apply()

                msg.what = HandlerConstant.OPERATION_SUCCEED
            } else
                msg.what = HandlerConstant.CONNECTION_FAILURE

            mHandler.sendMessage(msg)
        }).start()
    }
}
