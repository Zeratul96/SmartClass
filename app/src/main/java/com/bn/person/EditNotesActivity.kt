package com.bn.person

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.bn.database.DataBaseUtil
import com.bn.parameter.BundleConstant
import com.bn.parameter.Constant
import com.bn.smartclass_android.R
import com.bn.tool_package.TimeTools



class EditNotesActivity : Activity(), TextWatcher, View.OnClickListener {

    internal var editText: EditText?=null
    internal var delete: ImageView?=null
    internal var finishBtn: TextView?=null

    internal var info: Array<String>? = null
    internal var alertDialog: AlertDialog?=null


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_note_layout)

        editText = findViewById(R.id.editText) as EditText
        delete = findViewById(R.id.delete_btn) as ImageView
        finishBtn = findViewById(R.id.create_btn) as TextView
        finishBtn!!.setOnClickListener(this)

        val bundle = this.intent.extras
        info = bundle.getStringArray(BundleConstant.INFO_STR)
        if (info != null) {
            (editText as EditText) .setText(info!![0])
        }

        editText!!.setSelection(editText!!.length())
        editText!!.addTextChangedListener(this)

        findViewById(R.id.backView).setOnClickListener { this@EditNotesActivity.finish() }

        delete!!.setOnClickListener { showDeleteDialog() }
    }

    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        finishBtn!!.isEnabled = editText!!.text.toString() != ""

    }

    override fun afterTextChanged(editable: Editable) {}

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    override fun onClick(view: View) {
        DataBaseUtil.createOrOpenDataBase(Constant.NOTES)

        val updateStrArrays =
                arrayOf(
                        "'"+ TimeTools.generateContentFormatTime() + "'",
                        "'" + editText!!.text.toString() + "'",
                        "'" + info!![0] + "'"
                )
        DataBaseUtil.update("update notes set content = " + updateStrArrays[1] + ",create_date = " + updateStrArrays[0] +
                " where primary_key = " + updateStrArrays[2])
        DataBaseUtil.closeDatabase()

        this@EditNotesActivity.finish()
    }

    private fun showDeleteDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)

        builder.setMessage("确定删除该条笔记？")

        builder.setPositiveButton("确定") { _, _ ->
            DataBaseUtil.createOrOpenDataBase(Constant.NOTES)
            DataBaseUtil.delete("delete from notes where primary_key = " + "'" + info!![1] + "'")
            DataBaseUtil.closeDatabase()

            this@EditNotesActivity.finish()
        }

        builder.setNegativeButton("取消") { _, _ -> alertDialog!!.dismiss() }

        builder.setCancelable(false)

        alertDialog = builder.create()
        alertDialog !!.show()
    }
}
