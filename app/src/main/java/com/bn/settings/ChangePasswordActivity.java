package com.bn.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.parameter.SavedDataConstant;
import com.bn.smartclass_android.R;
import com.bn.util.NetConnectionUtil;

public class ChangePasswordActivity extends Activity implements TextWatcher, OnClickListener{

    EditText oldPassword;
    EditText newPassword;
    EditText confirmPassword;

    TextView finishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_layout);

        oldPassword = (EditText) findViewById(R.id.old_password);
        newPassword = (EditText) findViewById(R.id.new_password);
        confirmPassword = (EditText) findViewById(R.id.confirm_password);

        oldPassword.addTextChangedListener(this);
        newPassword.addTextChangedListener(this);
        confirmPassword.addTextChangedListener(this);
        findViewById(R.id.backView).setOnClickListener(this);

        finishButton = (TextView) findViewById(R.id.confirm_change);
        finishButton.setEnabled(false);
        finishButton.setOnClickListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        if(oldPassword.getText().toString().equals("")||newPassword.getText().toString().equals("")||
           confirmPassword.getText().toString().equals("")||newPassword.getText().toString().equals(Preference.dataInfoList.get(1)))
        {
            finishButton.setEnabled(false);
        }
        else finishButton.setEnabled(true);
    }

    @Override
    public void afterTextChanged(Editable editable) {}


    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.backView:this.finish();break;

            case R.id.confirm_change:confirmChange();break;

        }

    }

    private void confirmChange()
    {
        if(!oldPassword.getText().toString().equals(Preference.dataInfoList.get(1))){
            Toast.makeText(this, "输入的旧密码不正确。", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!newPassword.getText().toString().equals(confirmPassword.getText().toString())){
            Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        final Handler mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                switch (msg.what)
                {
                    case HandlerConstant.CONNECTION_FAILURE:
                        Toast.makeText(ChangePasswordActivity.this, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show();break;

                    case HandlerConstant.OPERATION_SUCCEED:
                        startActivity(new Intent(ChangePasswordActivity.this, SettingsActivity.class));
                        ChangePasswordActivity.this.finish();
                        break;

                    default:break;

                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                String result = NetConnectionUtil.updateStudent("1#"+newPassword.getText().toString()+"|"+Preference.dataInfoList.get(0));

                Message msg = new Message();
                if(result.equals(Constant.OPERATION_SUCCEED)) msg.what = HandlerConstant.OPERATION_SUCCEED;
                else if(result.equals(Constant.SERVER_CONNECTION_ERROR)) msg.what = HandlerConstant.CONNECTION_FAILURE;
                else msg.what = -1;

                //后续操作 账号信息写入内存写入SD卡
                if(msg.what == HandlerConstant.OPERATION_SUCCEED){
                    Preference.dataInfoList.set(1, newPassword.getText().toString());
                    SharedPreferences sp = ChangePasswordActivity.this.getSharedPreferences(SavedDataConstant.LOGIN, MODE_PRIVATE);

                    Editor editor = sp.edit();
                    editor.putString(SavedDataConstant.LOGIN_PASSWORD, newPassword.getText().toString());
                    editor.apply();
                }

                mHandler.sendMessage(msg);

            }
        }).start();

    }


}
