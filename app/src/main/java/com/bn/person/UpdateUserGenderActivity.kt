package com.bn.person

import android.app.Activity
import android.content.Context

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.Toast

import com.bn.parameter.Constant
import com.bn.parameter.HandlerConstant
import com.bn.parameter.Preference
import com.bn.parameter.SavedDataConstant
import com.bn.smartclass_android.R
import com.bn.util.NetConnectionUtil



class UpdateUserGenderActivity : Activity(), OnClickListener {

    internal var originItem: String ? = null

    internal var maleView: ImageView  ? = null
    internal var femaleView: ImageView ? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gender_layout)

        maleView = findViewById(R.id.male) as ImageView
        femaleView = findViewById(R.id.female) as ImageView

        findViewById(R.id.backView).setOnClickListener { this@UpdateUserGenderActivity.finish() }

        originItem = Preference.dataInfoList[7]

        if (originItem == "男")
            maleView!!.setBackgroundResource(R.drawable.blue_circle)
        else if (originItem == "女")
            femaleView!!.setBackgroundResource(R.drawable.blue_circle)


        findViewById(R.id.male_layout).setOnClickListener(this)
        findViewById(R.id.female_layout).setOnClickListener(this)

    }

    override fun onClick(view: View) {
        if (view.id == R.id.male_layout)
            changeSelectedItem("男")
        else
            changeSelectedItem("女")
    }

    private fun changeSelectedItem(selectedGender: String) {
        if (selectedGender == originItem)
            return

        val myHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                when (msg.what) {
                    HandlerConstant.OPERATION_SUCCEED -> {
                        if (originItem == "男") {
                            maleView!!.setBackgroundResource(R.drawable.blue_circle)
                            femaleView!!.setBackgroundResource(R.drawable.circle)
                        } else {
                            femaleView!!.setBackgroundResource(R.drawable.blue_circle)
                            maleView!!.setBackgroundResource(R.drawable.circle)
                        }
                        postDelayed({ this@UpdateUserGenderActivity.finish() }, 500)
                    }

                    HandlerConstant.CONNECTION_FAILURE ->
                        Toast.makeText(this@UpdateUserGenderActivity, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Thread(Runnable {
            val ss: String
            if (selectedGender == "男") {
                ss = "4#" + "男" + "|" + Preference.dataInfoList[0]
                Preference.dataInfoList[7] = "男"
            } else {
                ss = "4#" + "女" + "|" + Preference.dataInfoList[0]
                Preference.dataInfoList[7] = "女"
            }

            val result = NetConnectionUtil.updateStudent(ss)

            val message = Message()
            if (result == Constant.OPERATION_SUCCEED) {
                originItem = selectedGender
                val sp = getSharedPreferences(SavedDataConstant.USER, Context.MODE_PRIVATE)
                val editor = sp.edit()
                editor.putString(SavedDataConstant.NAME, selectedGender)
                editor.apply()

                message.what = HandlerConstant.OPERATION_SUCCEED
            } else
                message.what = HandlerConstant.CONNECTION_FAILURE

            myHandler.sendMessage(message)
        }).start()
    }

}
