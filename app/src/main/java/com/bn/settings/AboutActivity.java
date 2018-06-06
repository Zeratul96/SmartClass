package com.bn.settings;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

import com.bn.smartclass_android.R;

public class AboutActivity extends Activity implements OnClickListener{

    public static AboutActivity aboutActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        if(aboutActivity!=null) aboutActivity.finish();
        aboutActivity = this;

        findViewById(R.id.contact).setOnClickListener(this);
        findViewById(R.id.suggestion).setOnClickListener(this);

        findViewById(R.id.backView).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.contact:startActivity(new Intent(this, ContactActivity.class));break;

            case R.id.suggestion:startActivity(new Intent(this, SuggestionActivity.class));break;

            case R.id.backView:
                this.finish();
                break;
        }
    }
}
