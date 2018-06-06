package com.bn.settings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;

import com.bn.main.HomeFragment;
import com.bn.main.LoginActivity;
import com.bn.main.MainActivity;
import com.bn.main.ResourceFragment;
import com.bn.parameter.Preference;
import com.bn.parameter.SavedDataConstant;
import com.bn.smartclass_android.R;
import com.bn.widget.MyToggleButton;

import java.io.File;

public class SettingsActivity extends Activity implements OnClickListener{

    public static SettingsActivity settingsActivity;
    AlertDialog alertDialog;

    MyToggleButton shieldCommentBtn;
    MyToggleButton shieldAdsBtn;
    MyToggleButton autoMuteBtn;
    boolean originUIState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        //清除上一次的Activity 避免同一个Activity残留在栈中
        if(settingsActivity!=null) settingsActivity.finish();
        settingsActivity = this;

        //添加监听
        findViewById(R.id.change_password).setOnClickListener(this);
        findViewById(R.id.phone_mute).setOnClickListener(this);
        findViewById(R.id.wrong_set).setOnClickListener(this);
        findViewById(R.id.about).setOnClickListener(this);
        findViewById(R.id.log_out).setOnClickListener(this);

        findViewById(R.id.backView).setOnClickListener(this);

        //屏蔽评论
        shieldCommentBtn = (MyToggleButton) findViewById(R.id.shield_comment_btn);
        shieldCommentBtn.setOnToggleStateListener(new MyToggleButton.OnToggleListener() {
            @Override
            public void onToggleSate(boolean state) {
                Preference.isShieldComments = state;
                SharedPreferences sp = getSharedPreferences(SavedDataConstant.SETTINGS_PREFERENCE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SavedDataConstant.SHIELD_COMMENTS, Preference.isShieldComments);
                editor.commit();
            }
        });
        //界面简化
        shieldAdsBtn = (MyToggleButton) findViewById(R.id.shield_ads_btn);
        shieldAdsBtn.setOnToggleStateListener(new MyToggleButton.OnToggleListener() {
            @Override
            public void onToggleSate(boolean state) {
                Preference.isShieldAds = state;
                SharedPreferences sp = getSharedPreferences(SavedDataConstant.SETTINGS_PREFERENCE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SavedDataConstant.SHIELD_ADS, Preference.isShieldAds);
                editor.commit();
                //两次界面设置不一样那么就需要重新加载界面
                if(originUIState!=Preference.isShieldAds)
                {
                    HomeFragment.needReload = true;
                    ResourceFragment.needReload = true;
                }
            }
        });

        //自动静音
        autoMuteBtn = (MyToggleButton) findViewById(R.id.auto_mute_btn);
        autoMuteBtn.setOnToggleStateListener(new MyToggleButton.OnToggleListener() {
            @Override
            public void onToggleSate(boolean state) {
                Preference.isAutoPhoneMute = state;
                SharedPreferences sp = getSharedPreferences(SavedDataConstant.SETTINGS_PREFERENCE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SavedDataConstant.AUTO_PHONE_MUTE, Preference.isAutoPhoneMute);
                editor.commit();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


        originUIState = Preference.isShieldAds;

        autoMuteBtn.setToggleState(Preference.isAutoPhoneMute);
        shieldCommentBtn.setToggleState(Preference.isShieldComments);
        shieldAdsBtn.setToggleState(Preference.isShieldAds);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.backView:
                this.finish();
                break;

            case R.id.change_password:
                startActivity(new Intent(this, ChangePasswordActivity.class));
                break;

//            case R.id.broadcast_frame:
//                startActivity(new Intent(this, FramesActivity.class));
//                break;

            case R.id.wrong_set:
                startActivity(new Intent(this, WrongSetSettingActivity.class));
                break;

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.log_out:
                showAlertDialog();
                break;

        }

    }

    private void showAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogCustom);builder.setTitle("退出登录");
        //对话框内容
        builder.setMessage("退出后不会删除错题集、记事本中的数据，下次登录依然可以使用本账号。");
        //确定按钮
        builder.setPositiveButton("退出登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                deletePrivateData();
                startActivity(new Intent(SettingsActivity.this, LoginActivity.class));

                SettingsActivity.this.finish();
                MainActivity.mainActivity.finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        //固定对话框使其不可被取消
        builder.setCancelable(false);

        //创建对话框
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePrivateData()
    {
        //删除登录密码
        SharedPreferences sp = getSharedPreferences(SavedDataConstant.LOGIN, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(SavedDataConstant.LOGIN_PASSWORD);
        editor.commit();

        //删除头像
        File file = new File(Preference.appDataPath+Preference.sculptureFile);
        if(file.exists())
            file.delete();
    }

}
