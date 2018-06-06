package com.bn.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bn.parameter.BundleConstant;
import com.bn.parameter.Preference;
import com.bn.parameter.SavedDataConstant;
import com.bn.util.NetConnectionUtil;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.smartclass_android.R;

import java.util.Arrays;
import java.util.LinkedList;

/**
 *该类只进行登录检查并且存储登录信息和加载部分个人信息
 */

public class LoginActivity extends Activity implements OnClickListener,TextWatcher{

    Button loginButton;
    EditText stuID;
    EditText stuPassword;

    SharedPreferences sp;

    Handler msgHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        //初始化控件
        loginButton = (Button)findViewById(R.id.login_button);
        stuID = (EditText)findViewById(R.id.studentID);
        stuID.addTextChangedListener(this);
        stuPassword = (EditText)findViewById(R.id.password);
        stuPassword.addTextChangedListener(this);

        sp = getSharedPreferences(SavedDataConstant.LOGIN, MODE_PRIVATE);
        String ID = sp.getString(SavedDataConstant.LOGIN_ID, null);
        if(ID!=null)
        {
            stuID.setText(ID);
            stuPassword.requestFocus();
        }

        loginButton.setEnabled(false);
        loginButton.setOnClickListener(this);

        final TextView forgetTextView = (TextView) findViewById(R.id.forget_btn);
        forgetTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ForgetPasswordActivity.class));
            }
        });
    }


    /**
     * 点击按钮之后涉及网络通信操作 网络通信操作不能在主线程中执行
     * 同时非主线程不能执行界面更新 需要借助Handler对象通知主线程对UI更新
     * Handler可以直接把任务传给主线程 也可以给主线程发送Message对象
     *Handler必须建立在主线程中 在其他线程中建立Handler对象该线程必须有Loop.prepare
     * 注意：若网络连接通畅但是由于服务器未运行线程会进行较长时间尝试连接
     */
    @Override
    public void onClick(View view)
    {
        msgHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                switch (msg.what)
                {
                    case HandlerConstant.CONNECTION_FAILURE:Toast.makeText(LoginActivity.this, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show();
                        break;

                    case HandlerConstant.ID_NOT_EXIST:Toast.makeText(LoginActivity.this, "用户名不存在，请核对用户名后重新尝试。", Toast.LENGTH_SHORT).show();
                        break;

                    case HandlerConstant.PASSWORD_INCORRECT:Toast.makeText(LoginActivity.this, "密码输入错误，请核对密码后重新尝试。", Toast.LENGTH_SHORT).show();
                        break;

                    case HandlerConstant.LOGIN_SUCCEED:
                    {
                        //存储登录信息
                        sp = getSharedPreferences(SavedDataConstant.LOGIN, MODE_PRIVATE);
                        Editor editor = sp.edit();
                        editor.putString(SavedDataConstant.LOGIN_ID, stuID.getText().toString());
                        editor.putString(SavedDataConstant.LOGIN_PASSWORD, stuPassword.getText().toString());
                        editor.apply();

                        //跳转界面
                        Bundle bundle = new Bundle();
                        bundle.putInt(BundleConstant.LOGIN_METHOD, BundleConstant.LOGIN_MANUAL);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtras(bundle));
                        LoginActivity.this.finish();
                        break;
                    }
                }
            }
        };

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                loginCheck();
            }
        }).start();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}


    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(!stuID.getText().toString().equals("")&&!stuPassword.getText().toString().equals(""))
        {
            loginButton.setEnabled(true);
        }
        else loginButton.setEnabled(false);

    }

    @Override
    public void afterTextChanged(Editable editable) {}


    /**
     * 该方法由自定义线程执行
     */
    private void loginCheck()
    {
        Message message = new Message();
        String result = NetConnectionUtil.stuLoginCheck("6#"+stuID.getText().toString());
        //id,password,grade,college,major,name,nickname,gender,tel,picture
        String[] info = result.split("<#>");


        if(result.equals(Constant.NO_RECORD_FOUND))
        {
            message.what = HandlerConstant.ID_NOT_EXIST;
        }
        else if(info.length>1&&!info[1].equals(stuPassword.getText().toString()))
        {
            message.what = HandlerConstant.PASSWORD_INCORRECT;
        }
        else if(info.length>1&&info[1].equals(stuPassword.getText().toString()))
        {
            message.what = HandlerConstant.LOGIN_SUCCEED;
            savingUserInfo(info);
        }
        else
        {
            message.what = HandlerConstant.CONNECTION_FAILURE;
        }

        msgHandler.sendMessage(message);
    }


    private void savingUserInfo(String[] info)
    {
        //数组存储顺序 id,password,grade,college,major,name,nickname,gender,tel,picture
        //存储到内存
        Preference.dataInfoList = new LinkedList<>(Arrays.asList(info));

        //存储到SD卡
        sp = getSharedPreferences(SavedDataConstant.USER, MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(SavedDataConstant.GRADE, info[2]);
        editor.putString(SavedDataConstant.COLLEGE, info[3]);
        editor.putString(SavedDataConstant.MAJOR, info[4]);
        editor.putString(SavedDataConstant.NAME, info[5]);
        editor.putString(SavedDataConstant.NICKNAME, info[6]);
        editor.putString(SavedDataConstant.GENDER, info[7]);
        editor.putString(SavedDataConstant.TEL, info[8]);
        editor.putString(SavedDataConstant.PICTURE_ADDRESS, info[9]);

        editor.apply();

    }
}
