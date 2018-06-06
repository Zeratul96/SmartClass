package com.bn.welcome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.bn.main.LoginActivity;
import com.bn.main.MainActivity;
import com.bn.parameter.BundleConstant;
import com.bn.parameter.SavedDataConstant;
import com.bn.smartclass_android.R;

public class WelcomeActivity extends Activity {

    ImageView welcomeImg;
    SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //设置为全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //切换到布局
        setContentView(R.layout.welcome_layout);
        welcomeImg = (ImageView) this.findViewById(R.id.welcome_img);

        //开启动画
        AlphaAnimation anima = new AlphaAnimation(0.3f, 1.0f);
        anima.setDuration(800);// 设置动画显示时间
        welcomeImg.startAnimation(anima);
        anima.setAnimationListener(new AnimationImpl());
    }


    private class AnimationImpl implements Animation.AnimationListener
    {

        @Override
        public void onAnimationStart(Animation animation) {
            welcomeImg.setBackgroundResource(R.drawable.welcome);
        }

        @Override
        public void onAnimationEnd(Animation animation)
        {

            sp = getSharedPreferences(SavedDataConstant.LOGIN, Context.MODE_PRIVATE);

            String loginID = sp.getString(SavedDataConstant.LOGIN_ID, null);
            String loginPassword = sp.getString(SavedDataConstant.LOGIN_PASSWORD, null);

            if(loginID==null||loginPassword==null)
            {
                skip(false);
            }
            else
            {
                skip(true);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

    }

    //延迟2000毫秒跳转至登录界面
    private void skip(final boolean hasLogged)
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                Intent intent = (hasLogged)?
                        new Intent(WelcomeActivity.this, MainActivity.class):new Intent(WelcomeActivity.this, LoginActivity.class);
                if(hasLogged)
                {
                    Bundle bundle = new Bundle();
                    bundle.putInt(BundleConstant.LOGIN_METHOD, BundleConstant.LOGIN_AUTO);
                    intent.putExtras(bundle);
                }
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        },2000);

    }
}



