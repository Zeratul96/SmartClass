package com.bn.main;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.bn.parameter.BundleConstant;
import com.bn.parameter.Constant;
import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.parameter.SavedDataConstant;
import com.bn.smartclass_android.R;
import com.bn.util.NetConnectionUtil;

import java.util.Arrays;
import java.util.LinkedList;

public class MainActivity extends FragmentActivity implements OnCheckedChangeListener{
    RadioGroup radioGroup;
    RadioButton mainButton;
    RadioButton resourceButton;
    RadioButton personButton;

    SharedPreferences sp;
    Handler myHandler;

    //三个Fragment
    HomeFragment homeFragment;
    ResourceFragment resourceFragment;
    PersonFragment personFragment;
    int selectedFragment = 0;//记录当前选择的Fragment 用于按下返回键之后APP的退出情况（有些情况退出，有些不退出只是从Web返回原生）

    public static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        //清除MainActivity避免其残留多个在栈中
        if(mainActivity!=null) mainActivity.finish();
        mainActivity = this;

        //获取下面菜单栏的按钮组
        radioGroup = (RadioGroup)this.findViewById(R.id.main_bottom_tabs);
        mainButton = (RadioButton)this.findViewById(R.id.main_home);
        resourceButton = (RadioButton)this.findViewById(R.id.main_resource);
        personButton = (RadioButton)this.findViewById(R.id.main_person);

        radioGroup.setOnCheckedChangeListener(this);

        //获取Bundle 只有登录才会传过来Bundle
        Bundle bundle = this.getIntent().getExtras();
        if(bundle !=null)
        {
            int loginMethod = bundle.getInt(BundleConstant.LOGIN_METHOD);
            if(loginMethod==BundleConstant.LOGIN_AUTO)//自动登录模式
            {
                autoLogin();
            }
            else
                mainButton.setChecked(true);
        }
        else
        {
            if(selectedFragment==0)
                mainButton.setChecked(true);
            else if(selectedFragment==1)
                resourceButton.setChecked(true);
            else
                personButton.setChecked(true);
        }
    }


    /**
     * SharedPreference必须在此位置开始加载 否则获取不到boolean类型数据
     * 我也不知道为什么反正不能在onCreate中调用
     *
     */
    @Override
    protected void onStart() {
        super.onStart();
        loadingSettings();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedID)
    {
        switch(checkedID)
        {
            case R.id.main_home:
                selectedFragment = 0;
                changeFragment(0);
                break;

            case R.id.main_resource:
                selectedFragment = 1;
                changeFragment(1);
                break;

            case R.id.main_person:
                selectedFragment = 2;
                changeFragment(2);
                break;
            default:break;
        }
    }

    /**
     * 拿到fragmentManager 开始执行Fragment的添加、移除、替换、隐藏四种操作
     * Fragment显示于主界面的Main_Content中
     * 注意：管理结束后要commit才能使当前Activity的Fragment生效
     */
    private void changeFragment(int num)
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        hideFragment(fragmentTransaction);
        switch(num)
        {
            case 0:
                selectedFragment = 0;
                if (homeFragment == null || HomeFragment.needReload) {
                    homeFragment = new HomeFragment();
                    fragmentTransaction.add(R.id.main_content, homeFragment);
                } else fragmentTransaction.show(homeFragment);
                break;

            case 1:
                selectedFragment = 1;
                if (resourceFragment == null || ResourceFragment.needReload) {
                    resourceFragment = new ResourceFragment();
                    fragmentTransaction.add(R.id.main_content, resourceFragment);
                } else fragmentTransaction.show(resourceFragment);
                break;

            case 2:
                selectedFragment = 2;
                if (personFragment == null || PersonFragment.needReload) {
                    personFragment = new PersonFragment();
                    fragmentTransaction.add(R.id.main_content, personFragment);
                } else fragmentTransaction.show(personFragment);
                break;
        }

            fragmentTransaction.commit();
    }


    //隐藏所有现有的Fragment
    private void hideFragment(FragmentTransaction fragmentTransaction)
    {
        if(homeFragment!=null)
            fragmentTransaction.hide(homeFragment);
        if(resourceFragment!=null)
            fragmentTransaction.hide(resourceFragment);
        if(personFragment!=null)
            fragmentTransaction.hide(personFragment);

    }


    private void autoLogin()
    {
        myHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);

                switch (msg.what)
                {
                    case HandlerConstant.CONNECTION_FAILURE:
                        Toast.makeText(MainActivity.this, "网络连接错误，请检查网络连接。", Toast.LENGTH_SHORT).show();
                        break;

                    case HandlerConstant.ID_NOT_EXIST:
                        showAlertDialog(HandlerConstant.ID_NOT_EXIST);
                        break;
                    case HandlerConstant.PASSWORD_INCORRECT:
                        showAlertDialog(HandlerConstant.PASSWORD_INCORRECT);
                        break;

                    case HandlerConstant.LOGIN_SUCCEED:
                        Toast.makeText(MainActivity.this, "欢迎回来，"+Preference.dataInfoList.get(5)+"同学。", Toast.LENGTH_SHORT).show();
                        break;
                }

                mainButton.setChecked(true);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                sp = getSharedPreferences(SavedDataConstant.LOGIN, MODE_PRIVATE);
                int status = loginCheck
                        (sp.getString(SavedDataConstant.LOGIN_ID, ""), sp.getString(SavedDataConstant.LOGIN_PASSWORD, ""));
                Message message = new Message();
                message.what = status;
                myHandler.sendMessage(message);
            }
        }).start();

    }

    private int loginCheck(String loginID, String loginPassword)
    {
        int loginStatus;

        String result = NetConnectionUtil.stuLoginCheck("6#"+loginID);
        //id,password,grade,college,major,name,nickname,gender,tel,picture
        String info[] = result.split("<#>");

        Editor editor = sp.edit();

        if(result.equals(Constant.NO_RECORD_FOUND))
        {
            loginStatus = HandlerConstant.ID_NOT_EXIST;
            editor.remove(SavedDataConstant.LOGIN_PASSWORD);
            editor.remove(SavedDataConstant.LOGIN_ID);
            editor.apply();

        }
        else if(info.length>1&&!info[1].equals(loginPassword))
        {
            loginStatus = HandlerConstant.PASSWORD_INCORRECT;
            editor.remove(SavedDataConstant.LOGIN_PASSWORD);
            editor.apply();
        }
        else if(info.length>1&&info[1].equals(loginPassword))
        {
            loginStatus = HandlerConstant.LOGIN_SUCCEED;
            savingUserInfo(info);
        }
        else
        {
            loginStatus = HandlerConstant.CONNECTION_FAILURE;
            getSavingInfoInDisconnection();
        }

        return loginStatus;
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

    //断网情况下获取存储的信息
    private void getSavingInfoInDisconnection()
    {
        Preference.dataInfoList = new LinkedList<>();

        sp = getSharedPreferences(SavedDataConstant.LOGIN, MODE_PRIVATE);
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.LOGIN_ID,""));
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.LOGIN_PASSWORD,""));

        sp = getSharedPreferences(SavedDataConstant.USER,MODE_PRIVATE);
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.GRADE,""));
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.COLLEGE,""));
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.MAJOR,""));
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.NAME,""));
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.NICKNAME,""));
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.GENDER,""));
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.TEL,""));
        Preference.dataInfoList.add(sp.getString(SavedDataConstant.PICTURE_ADDRESS,""));

    }


    //从数据库加载设置
    private void loadingSettings()
    {
        sp = getSharedPreferences(SavedDataConstant.SETTINGS_PREFERENCE, MODE_PRIVATE);
        Preference.PPTFrames = sp.getInt(SavedDataConstant.PPT_FRAMES, 0);
        Preference.isAutoPhoneMute = sp.getBoolean(SavedDataConstant.AUTO_PHONE_MUTE, true);
        Preference.isShieldComments = sp.getBoolean(SavedDataConstant.SHIELD_COMMENTS, false);
        Preference.isShieldAds = sp.getBoolean(SavedDataConstant.SHIELD_ADS, false);
        Preference.isAutoAddWrongSet = sp.getBoolean(SavedDataConstant.WRONG_SET, true);
    }


    private void showAlertDialog(int status)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogCustom);builder.setTitle("登录提示");
        //对话框内容
        if(status==HandlerConstant.PASSWORD_INCORRECT)
            builder.setMessage("登录密码已过期，请重新登录。如忘记密码，请在登录页面点击忘记密码找回。");
        else
            builder.setMessage("提示：该账号已经被注销，请选择其他账号登录");
        //确定按钮
        builder.setPositiveButton("重新登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                MainActivity.this.finish();
            }
        });
        //固定对话框使其不可被取消
        builder.setCancelable(false);

        //创建对话框
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

}
