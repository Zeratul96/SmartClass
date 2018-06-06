package com.bn.question;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.smartclass_android.R;
import com.bn.tool_package.TimeTools;
import com.bn.util.NetConnectionUtil;

public class CommentQuestionActivity extends Activity implements TextWatcher{

    TextView finishBtn;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_question_layout);

        finishBtn = (TextView) findViewById(R.id.confirm_change);
        finishBtn.setEnabled(false);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handOnComment();
            }
        });
        editText = (EditText) findViewById(R.id.detail_suggestion);
        editText.addTextChangedListener(this);

        findViewById(R.id.backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentQuestionActivity.this.finish();
            }
        });
    }

    @Override
    public void afterTextChanged(Editable editable) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(editText.getText().toString().equals(""))
            finishBtn.setEnabled(false);
        else
            finishBtn.setEnabled(true);
    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    private void handOnComment()
    {
        final Handler myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(msg.what==HandlerConstant.OPERATION_SUCCEED){
                    Toast.makeText(CommentQuestionActivity.this, "评论题目成功", Toast.LENGTH_SHORT).show();
                    CommentQuestionActivity.this.finish();
                }
                else
                    Toast.makeText(CommentQuestionActivity.this, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                StringBuilder sb = new StringBuilder();
                sb.append(CommentQuestionActivity.this.getIntent().getExtras().getString(BundleConstant.INFO_STR)+"|");
                sb.append(Preference.dataInfoList.get(0)+"|");
                sb.append(editText.getText().toString()+"|");
                sb.append(TimeTools.generateContentFormatTime());
                String result = NetConnectionUtil.insertQuestionComment(sb.toString());

                Message message = new Message();
                if(result!=null&&result.equals(Constant.OPERATION_SUCCEED))
                    message.what = HandlerConstant.OPERATION_SUCCEED;
                else
                    message.what = HandlerConstant.CONNECTION_FAILURE;

                myHandler.sendMessage(message);
            }
        }).start();
    }
}
