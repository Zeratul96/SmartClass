package com.bn.person

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView

import com.bn.parameter.Preference
import com.bn.smartclass_android.R
import com.bn.tool_package.ImageTools



class PersonCenterActivity : Activity(), OnClickListener {

    internal var accountText: TextView?=null
    internal var nickText: TextView?=null
    internal var genText: TextView?=null
    internal var telText: TextView?=null
    internal var resSculpture: ImageView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.person_center_layout)

        if (personCenterActivity != null) personCenterActivity!!.finish()
        personCenterActivity = this

        accountText = findViewById(R.id.TextView_zhanghao) as TextView
        nickText = findViewById(R.id.TextView_nicheng) as TextView
        genText = findViewById(R.id.TextView_gen) as TextView
        telText = findViewById(R.id.TextView_tel) as TextView
        resSculpture = findViewById(R.id.userPhoto) as ImageView

        accountText!!.text = Preference.dataInfoList[0]

        findViewById(R.id.backView).setOnClickListener { this@PersonCenterActivity.finish() }
        findViewById(R.id.person_head_sculpture).setOnClickListener(this)
        findViewById(R.id.Person_nick_name_Linear).setOnClickListener(this)
        findViewById(R.id.Person_sexual).setOnClickListener(this)
        findViewById(R.id.Person_tel).setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        val bitmap = ImageTools.getLocalBitmap(Preference.sculptureFile)
        if (bitmap != null)
            resSculpture!!.setImageBitmap(ImageTools.createCircleImage(bitmap))
        else
        {
            val res = resources
            val picSclputure = BitmapFactory.decodeResource(res, R.drawable.login_sculpture)
            resSculpture!!.setImageBitmap(ImageTools.createCircleImage(picSclputure))
        }


        nickText!!.text = Preference.dataInfoList[6]
        genText!!.text = Preference.dataInfoList[7]
        telText!!.text = Preference.dataInfoList[8]
        System.gc()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.person_head_sculpture -> startActivity(Intent(this, PersonalSculptureActivity::class.java))
            R.id.Person_nick_name_Linear -> startActivity(Intent(this, UpdateUserNickNameActivity::class.java))
            R.id.Person_tel -> startActivity(Intent(this, UpdateUserTelActivity::class.java))
            R.id.Person_sexual -> startActivity(Intent(this, UpdateUserGenderActivity::class.java))
        }
    }

    companion object {

        internal var personCenterActivity: PersonCenterActivity? = null
    }
}
