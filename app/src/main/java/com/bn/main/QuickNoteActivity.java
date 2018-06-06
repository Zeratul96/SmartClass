package com.bn.main;

import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bn.database.DataBaseUtil;
import com.bn.parameter.Constant;
import com.bn.smartclass_android.R;
import com.bn.tool_package.TimeTools;

public class QuickNoteActivity extends Activity implements View.OnClickListener{

    TextView createBtn;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_note_layout);

        createBtn = (TextView) findViewById(R.id.create_btn);
        createBtn.setEnabled(false);
        createBtn.setOnClickListener(this);
        findViewById(R.id.delete_btn).setVisibility(View.GONE);
        editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editText.getText().toString().equals(""))
                    createBtn.setEnabled(false);
                else
                    createBtn.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        createBtn.setOnClickListener(this);
        findViewById(R.id.backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuickNoteActivity.this.finish();
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        DataBaseUtil.createOrOpenDataBase(Constant.NOTES);
        String[] insertValues = new String[]
                {
                    "'"+ TimeTools.generateNumberByTime()+"'",
                    "'"+ TimeTools.generateContentFormatTime()+"'",
                    "'"+ editText.getText().toString()+"'"
                };
        DataBaseUtil.insert("insert into notes values("+insertValues[0]+","+insertValues[1]+","+insertValues[2]+")");
        DataBaseUtil.closeDatabase();

        QuickNoteActivity.this.finish();

    }
}
