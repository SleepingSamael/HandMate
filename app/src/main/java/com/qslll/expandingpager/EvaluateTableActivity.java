package com.qslll.expandingpager;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.qslll.expandingpager.Model.SysApplication;

public class EvaluateTableActivity extends AppCompatActivity {

    private Button back;
    private Button save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate_table);
        SysApplication.getInstance().addActivity(this);

        back=(Button) findViewById(R.id.btn_back);
        save=(Button) findViewById(R.id.btn_save);
        //back键
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
