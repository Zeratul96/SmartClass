package com.bn.settings;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.smartclass_android.R;
import com.bn.tool_package.TimeTools;
import com.bn.util.NetConnectionUtil;

public class SuggestionActivity extends Activity implements OnClickListener{

    ImageView[] imageViews;
    EditText editText;
    int selectedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestion_layout);

        imageViews = new ImageView[]
                {
                        (ImageView) findViewById(R.id.error_button), (ImageView) findViewById(R.id.stack_button),
                        (ImageView) findViewById(R.id.down_button), (ImageView) findViewById(R.id.UI_button)
                };

        findViewById(R.id.error).setOnClickListener(this);
        findViewById(R.id.stack).setOnClickListener(this);
        findViewById(R.id.down).setOnClickListener(this);
        findViewById(R.id.UI_problem).setOnClickListener(this);

        editText = (EditText) findViewById(R.id.detail_suggestion);
        findViewById(R.id.hand_on).setOnClickListener(this);

        findViewById(R.id.backView).setOnClickListener(this);

    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.error:changeButtonStatus(0);break;

            case R.id.stack:changeButtonStatus(1);break;

            case R.id.down:changeButtonStatus(2);break;

            case R.id.UI_problem:changeButtonStatus(3);break;

            case R.id.hand_on:handOnSuggestion();break;

            case R.id.backView:
                this.finish();
                break;
        }

    }

    private void changeButtonStatus(int i) {
        selectedItem = i;
        for (int j = 0; j < 4; j++)
            imageViews[j].setBackgroundResource(R.drawable.circle);

        imageViews[i].setBackgroundResource(R.drawable.blue_circle);
    }

    private void handOnSuggestion()
    {
        if(selectedItem==-1&&editText.getText().toString().equals("")){
            Toast.makeText(this, "请给我们一些评价吧，我们会做得更好。", Toast.LENGTH_SHORT).show();
            return;
        }

        final Handler myHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                switch (msg.what)
                {
                    case HandlerConstant.CONNECTION_FAILURE:
                        Toast.makeText(SuggestionActivity.this, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show();break;

                    case HandlerConstant.OPERATION_SUCCEED:

                        Toast.makeText(SuggestionActivity.this, "提交成功，感谢您的反馈。", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SuggestionActivity.this, AboutActivity.class));
                        SuggestionActivity.this.finish();
                        break;

                    default:break;
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run()
            {

                StringBuilder sb = new StringBuilder();
                sb.append(TimeTools.generateNumberByTime()+"|");
                sb.append("1"+"|");//1代表身份为学生
                sb.append(Preference.dataInfoList.get(0)+"|");

                String ss = "";
                if(selectedItem==0)
                    ss = "闪退。";
                else if(selectedItem==1)
                    ss = "卡顿。";
                else if(selectedItem==2)
                    ss = "死机。";
                else if(selectedItem==3)
                    ss = "界面错位。";

                sb.append(ss+editText.getText().toString());

                String result = NetConnectionUtil.insertSoftWareFeedBack(sb.toString());

                Message msg = new Message();
                if(result.equals(Constant.OPERATION_SUCCEED)) msg.what = HandlerConstant.OPERATION_SUCCEED;
                else if(result.equals(Constant.SERVER_CONNECTION_ERROR)) msg.what = HandlerConstant.CONNECTION_FAILURE;
                else msg.what = -1;

                myHandler.sendMessage(msg);

            }
        }).start();
    }

}
