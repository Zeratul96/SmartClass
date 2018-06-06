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

import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.smartclass_android.R;
import com.bn.tool_package.ListViewHeight;
import com.bn.util.MyListViewAdapter;
import com.bn.util.NetConnectionUtil;

public class QuestionSetSubjectActivity extends Activity{

    static QuestionSetSubjectActivity questionSetSubjectActivity;

    Handler myHandler;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_layout);

        if(questionSetSubjectActivity!=null) questionSetSubjectActivity.finish();
        questionSetSubjectActivity = this;

        listView = (ListView) findViewById(R.id.list_for_question);
        findViewById(R.id.backView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionSetSubjectActivity.this.finish();
            }
        });

        createHandler();
        querySubject();


    }

    private void querySubject()
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                String result = NetConnectionUtil.queryCourseAndTeacher(Preference.dataInfoList.get(0));

                Message message = new Message();

                if(!result.equals(Constant.SERVER_CONNECTION_ERROR)&&result.length()>0)
                    message.what = HandlerConstant.OPERATION_SUCCEED;
                else
                    message.what = HandlerConstant.CONNECTION_FAILURE;

                String[] ss = result.split("\\|");
                String[] subjectArray = new String[ss.length];

                //传输成功才做进一步数据处理
                if(message.what==HandlerConstant.OPERATION_SUCCEED)
                {
                    for(int i=0,len = ss.length;i<len;i++)
                    {
                        String[] piece = ss[i].split("<#>");
                        subjectArray[i] = piece[0];
                    }
                }

                Bundle bundle = new Bundle();
                bundle.putStringArray(BundleConstant.SHORT_STR, subjectArray);
                message.setData(bundle);

                myHandler.sendMessage(message);

            }
        }).start();
    }


    private void createHandler()
    {
        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                switch (msg.what)
                {
                    case HandlerConstant.SUBJECT_INIT_SUCCEED:
                        Bundle bundleSubject = msg.getData();
                        String[] info = bundleSubject.getStringArray(BundleConstant.SHORT_STR);
                        initListView(listView, info, true);
                        break;


                    case HandlerConstant.QUESTION_INIT_SUCCEED:
                        Bundle bundle = msg.getData();
                        startActivity(new Intent(QuestionSetSubjectActivity.this, QuestionActivity.class).putExtras(bundle));
                        break;


                    case HandlerConstant.CONNECTION_FAILURE:
                        Toast.makeText(QuestionSetSubjectActivity.this, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show();
                        break;


                    case HandlerConstant.NO_RECORD_FOUND:
                        Toast.makeText(QuestionSetSubjectActivity.this, "抱歉，题库里还没有该门学科的题目呢，请耐心等待。", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    private void initListView(ListView listView, final String[] info, final boolean hasConnection)
    {
        MyListViewAdapter baseAdapter = new MyListViewAdapter(info.length)
        {
            LayoutInflater inflater = LayoutInflater.from(QuestionSetSubjectActivity.this);

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

                if(hasConnection)
                    subjectName.setText(info[i]);

                return content;

            }
        };

        listView.setAdapter(baseAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
            {
                //查询题库数据
                new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        String result = NetConnectionUtil.queryQuestion("0#"+info[i]);

                        Message message = new Message();
                        if(!result.equals(Constant.SERVER_CONNECTION_ERROR)&&!result.equals(Constant.NO_RECORD_FOUND)&&result.length()>0)
                            message.what = HandlerConstant.QUESTION_INIT_SUCCEED;

                        else if(result.equals(Constant.NO_RECORD_FOUND))
                            message.what = HandlerConstant.NO_RECORD_FOUND;

                        else
                            message.what = HandlerConstant.CONNECTION_FAILURE;


                        String[] infoArray = result.split("\\|");
                        Bundle bundle = new Bundle();
                        bundle.putStringArray(BundleConstant.INFO_STR, infoArray);
                        message.setData(bundle);

                        myHandler.sendMessage(message);

                    }
                }).start();

            }
        });

        ListViewHeight.setListViewHeight(listView);
    }
}
