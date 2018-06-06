package com.bn.person

import android.content.Intent
import android.os.Bundle
import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.bn.database.DataBaseUtil
import com.bn.main.QuickNoteActivity
import com.bn.parameter.BundleConstant
import com.bn.parameter.Constant
import com.bn.smartclass_android.R
import com.bn.tool_package.ListViewHeight
import com.bn.util.MyListViewAdapter

class NotesListActivity : Activity() {
    internal var listView: ListView?=null
    internal var dataList: List<Array<String>>?=null

    internal var alertDialog: AlertDialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notes_list_layout)

        if (notesListActivity != null) notesListActivity!!.finish()
        notesListActivity = this

        listView = findViewById(R.id.list_for_notes) as ListView
        findViewById(R.id.backView).setOnClickListener { this@NotesListActivity.finish() }

        findViewById(R.id.create_btn).setOnClickListener { startActivity(Intent(this@NotesListActivity, QuickNoteActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        initListView()
    }

    private fun initListView() {
        DataBaseUtil.createOrOpenDataBase(Constant.NOTES)
        dataList = DataBaseUtil.query("select primary_key, create_date, content from notes order by primary_key desc", 3)
        DataBaseUtil.closeDatabase()

        val adapter = object : MyListViewAdapter(dataList!!.size) {
            internal var inflater = LayoutInflater.from(this@NotesListActivity)

            override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
                var content: LinearLayout
                if (view == null)
                {
                    content = inflater.inflate(R.layout.notes_list, null).findViewById(R.id.note_layout) as LinearLayout
                }else
                {
                    content = view as LinearLayout
                }


                //去除最后一列的分割线
                if (i == dataList!!.size - 1) {
                    val cutLine = content.getChildAt(2)
                    cutLine.visibility = View.GONE
                }

                val title = content.getChildAt(0) as TextView
                val date = content.getChildAt(1) as TextView

                val tempStrArray = dataList!![i]
                title.text = if (tempStrArray[2].length > 15) tempStrArray[2].substring(0, 15) + "..." else tempStrArray[2]
                date.text = tempStrArray[1]

                return content
            }
        }

        listView!!.adapter = adapter
        listView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            val bundle = Bundle()
            bundle.putStringArray(BundleConstant.INFO_STR, arrayOf(dataList!![i][2], dataList!![i][0]))
            startActivity(Intent(this@NotesListActivity, EditNotesActivity::class.java).putExtras(bundle))
        }

        listView!!.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, i, _ ->
            showDeleteDialog(i)
            true
        }

        ListViewHeight.setListViewHeight(listView)
        System.gc()
    }

    private fun showDeleteDialog(position: Int) {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)

        builder.setMessage("确定删除该条笔记？")

        builder.setPositiveButton("确定") { _, _ ->
            DataBaseUtil.createOrOpenDataBase(Constant.NOTES)
            DataBaseUtil.delete("delete from notes where primary_key = " + "'" + dataList!![position][0] + "'")
            DataBaseUtil.closeDatabase()

            //刷新笔记列表
            initListView()
        }

        builder.setNegativeButton("取消") { _, _ -> alertDialog!!.dismiss() }

        builder.setCancelable(false)

        alertDialog = builder.create()
        alertDialog!!.show()
    }

    companion object { internal var notesListActivity: NotesListActivity? = null }
}
