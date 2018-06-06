package com.bn.settings;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import com.bn.smartclass_android.R;

public class ContactActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_layout);

       findViewById(R.id.backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //叶子Activity  离开该Activity直接销毁
                ContactActivity.this.finish();
            }
        });
    }

}
