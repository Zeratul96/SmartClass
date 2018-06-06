package com.bn.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import com.bn.parameter.Preference;
import com.bn.parameter.SavedDataConstant;
import com.bn.smartclass_android.R;
import com.bn.widget.MyToggleButton;

public class WrongSetSettingActivity extends Activity {

    MyToggleButton autoAddBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrong_set_setting_layout);

        findViewById(R.id.backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WrongSetSettingActivity.this.finish();
            }
        });

        autoAddBtn = (MyToggleButton) findViewById(R.id.auto_add_btn);
        autoAddBtn.setOnToggleStateListener(new MyToggleButton.OnToggleListener() {
            @Override
            public void onToggleSate(boolean state) {
                Preference.isAutoAddWrongSet = state;
                SharedPreferences sp = getSharedPreferences(SavedDataConstant.SETTINGS_PREFERENCE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SavedDataConstant.WRONG_SET, Preference.isAutoAddWrongSet);
                editor.commit();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoAddBtn.setToggleState(Preference.isAutoAddWrongSet);
    }
}
