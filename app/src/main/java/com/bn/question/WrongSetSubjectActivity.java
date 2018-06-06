package com.bn.question;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.database.DataBaseUtil;
import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.smartclass_android.R;
import com.bn.tool_package.ListViewHeight;
import com.bn.util.MyListViewAdapter;
import com.bn.util.NetConnectionUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WrongSetSubjectActivity extends Activity {

    static WrongSetSubjectActivity wrongSetSubjectActivity;

    ListView listView;
    List<String[]> questionList;
    Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_layout);

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("错题集");


        if (wrongSetSubjectActivity != null) wrongSetSubjectActivity.finish();
        wrongSetSubjectActivity = this;

        listView = (ListView) findViewById(R.id.list_for_question);
        findViewById(R.id.backView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                WrongSetSubjectActivity.this.finish();
            }
        });

        //创建Handler线程
        createHandlerThread();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化学科数据
        initSubjectData();
        //初始化ListView
        initListView(listView);
    }

    /**
     * 加载学科数据
     */
    private void initSubjectData() {
        DataBaseUtil.createOrOpenDataBase(Constant.WRONG_SET);
        questionList = DataBaseUtil.query("select question_id,subject from wrong_set", 2);
        DataBaseUtil.closeDatabase();
    }

    private void createHandlerThread() {
        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what==HandlerConstant.OPERATION_SUCCEED)
                {
                    Bundle bundle = msg.getData();
                    startActivity(new Intent(WrongSetSubjectActivity.this,IncorrectActivity.class).putExtras(bundle));
                }
                else
                    Toast.makeText(WrongSetSubjectActivity.this, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show();

            }
        };

    }


    private void initListView(ListView listView)
    {
        /**
         * 以下代码为了去除重复的学科
         */
        Set<String> subjectSet = new HashSet<>();
        for(String[] ss:questionList)
            subjectSet.add(ss[1]);

        final String[] subjectArray = new String[subjectSet.size()];
        Iterator<String> iterator = subjectSet.iterator();

        int count = 0;
        while (iterator.hasNext())
        {
            subjectArray[count++] = iterator.next();
        }


        MyListViewAdapter baseAdapter = new MyListViewAdapter(subjectArray.length)
        {
            LayoutInflater inflater = LayoutInflater.from(WrongSetSubjectActivity.this);

            @Override
            public View getView(int i, View view, ViewGroup viewGroup)
            {
                LinearLayout content = (LinearLayout)view;
                if(content==null){
                    content = (LinearLayout)
                            (inflater.inflate(R.layout.subject_list, null)).findViewById(R.id.subject_layout);
                }

                LinearLayout textContent = (LinearLayout) content.getChildAt(0);
                TextView subjectName = (TextView) textContent.getChildAt(1);

                subjectName.setText(subjectArray[i]);

                return content;
            }
        };

        listView.setAdapter(baseAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
            {
                final StringBuilder sb = new StringBuilder();
                for(String[] ss:questionList)
                {
                    if(ss[1].equals(subjectArray[i]))
                        sb.append(ss[0]+"|");
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String result = NetConnectionUtil.queryWrongSet(sb.toString());

                        Message message = new Message();
                        if(result!=null&&!result.equals(Constant.SERVER_CONNECTION_ERROR))
                        {
                            String[] info = result.split("\\|");
                            String[] questionID = sb.toString().split("\\|");

                            Bundle bundle = new Bundle();
                            bundle.putStringArray(BundleConstant.INFO_STR, info);
                            bundle.putStringArray(BundleConstant.SHORT_STR, questionID);
                            bundle.putString(BundleConstant.SUBJECT_NAME,questionList.get(i)[1]);

                            message.what = HandlerConstant.OPERATION_SUCCEED;
                            message.setData(bundle);

                        }
                        else
                            message.what = HandlerConstant.CONNECTION_FAILURE;

                        myHandler.sendMessage(message);
                    }
                }).start();
            }
        });

        ListViewHeight.setListViewHeight(listView);
    }
}
