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



class UpdateUserTelActivity : Activity() {

    internal var updateTelText: EditText ? = null
    internal var saveBtn: TextView ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_tel_layout)


        findViewById(R.id.backView).setOnClickListener { this@UpdateUserTelActivity.finish() }

        saveBtn = findViewById(R.id.confirm_change) as TextView
        saveBtn!!.isEnabled = false
        saveBtn!!.setOnClickListener { confirmChange() }

        updateTelText = findViewById(R.id.update_tel) as EditText
        updateTelText!!.setText(Preference.dataInfoList[8])
        updateTelText!!.setSelection(updateTelText!!.length())
        updateTelText!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                saveBtn!!.isEnabled = !(updateTelText!!.text.toString() == "" || updateTelText!!.text.toString() == Preference.dataInfoList[8])
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
                    HandlerConstant.CONNECTION_FAILURE -> Toast.makeText(this@UpdateUserTelActivity, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show()

                    HandlerConstant.OPERATION_SUCCEED -> this@UpdateUserTelActivity.finish()
                }
            }
        }

        Thread(Runnable {
            val message = "3#" + updateTelText!!.text.toString() + "|" + Preference.dataInfoList[0]
            val result = NetConnectionUtil.updateStudent(message)
            val msg = Message()

            if (result != null && result == Constant.OPERATION_SUCCEED) {
                Preference.dataInfoList[8] = updateTelText!!.text.toString()

                val sp = getSharedPreferences(SavedDataConstant.USER, Context.MODE_PRIVATE)
                val editor = sp.edit()
                editor.putString(SavedDataConstant.TEL, updateTelText!!.text.toString())
                editor.apply()

                msg.what = HandlerConstant.OPERATION_SUCCEED
            } else
                msg.what = HandlerConstant.CONNECTION_FAILURE


            mHandler.sendMessage(msg)
        }).start()
    }
    companion object{
        internal val updateUserTelActivity:UpdateUserTelActivity ? = null
    }
}
