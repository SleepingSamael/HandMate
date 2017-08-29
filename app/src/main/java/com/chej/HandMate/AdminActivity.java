package com.chej.HandMate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.chej.HandMate.Model.SysApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminActivity extends AppCompatActivity {
    private EditText thumbFlat_et;//拇指
    private EditText thumbMiddle_et;
    private EditText thumbFist_et;
    private EditText thumbStretch_et;
    private EditText thumbMove_et;
    private EditText forefingerFlat_et;//食指
    private EditText forefingerMiddle_et;
    private EditText forefingerFist_et;
    private EditText forefingerStretch_et;
    private EditText forefingerMove_et;
    private EditText middleFingerFlat_et;//中指
    private EditText middleFingerMiddle_et;
    private EditText middleFingerFist_et;
    private EditText middleFingerStretch_et;
    private EditText middleFingerMove_et;
    private EditText ringFingerFlat_et;//无名指
    private EditText ringFingerMiddle_et;
    private EditText ringFingerFist_et;
    private EditText ringFingerStretch_et;
    private EditText ringFingerMove_et;
    private EditText littleFingerFlat_et;//小指
    private EditText littleFingerMiddle_et;
    private EditText littleFingerFist_et;
    private EditText littleFingerStretch_et;
    private EditText littleFingerMove_et;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        SysApplication.getInstance().addActivity(this);

        TabHost th=(TabHost)findViewById(R.id.tabhost);
        th.setup();            //初始化TabHost容器

        //在TabHost创建标签，然后设置：标题／图标／标签页布局
        th.addTab(th.newTabSpec("tab1").setIndicator("舵机",getResources().getDrawable(R.drawable.logo)).setContent(R.id.tab_motor));
        th.addTab(th.newTabSpec("tab2").setIndicator("标签2",null).setContent(R.id.tab2));
        th.addTab(th.newTabSpec("tab3").setIndicator("标签3",null).setContent(R.id.tab3));
        //上面的null可以为getResources().getDrawable(R.drawable.图片名)设置图标

        thumbFlat_et=(EditText)findViewById(R.id.thumbFlat);
        thumbMiddle_et=(EditText)findViewById(R.id.thumbMiddle);
        thumbFist_et=(EditText)findViewById(R.id.thumbFist);
        thumbStretch_et=(EditText)findViewById(R.id.thumbStretch);
        thumbMove_et=(EditText)findViewById(R.id.thumbMove);
        forefingerFlat_et=(EditText)findViewById(R.id.forefingerFlat);
        forefingerMiddle_et=(EditText)findViewById(R.id.forefingerMiddle);
        forefingerFist_et=(EditText)findViewById(R.id.forefingerFist);
        forefingerStretch_et=(EditText)findViewById(R.id.forefingerStretch);
        forefingerMove_et=(EditText)findViewById(R.id.forefingerMove);
        middleFingerFlat_et=(EditText)findViewById(R.id.middleFingerFlat);
        middleFingerMiddle_et=(EditText)findViewById(R.id.middleFingerMiddle);
        middleFingerFist_et=(EditText)findViewById(R.id.middleFingerFist);
        middleFingerStretch_et=(EditText)findViewById(R.id.middleFingerStretch);
        middleFingerMove_et=(EditText)findViewById(R.id.middleFingerMove);
        ringFingerFlat_et=(EditText)findViewById(R.id.ringFingerFlat);
        ringFingerMiddle_et=(EditText)findViewById(R.id.ringFingerMiddle);
        ringFingerFist_et=(EditText)findViewById(R.id.ringFingerFist);
        ringFingerStretch_et=(EditText)findViewById(R.id.ringFingerStretch);
        ringFingerMove_et=(EditText)findViewById(R.id.ringFingerMove);
        littleFingerFlat_et=(EditText)findViewById(R.id.littleFingerFlat);
        littleFingerMiddle_et=(EditText)findViewById(R.id.littleFingerMiddle);
        littleFingerFist_et=(EditText)findViewById(R.id.littleFingerFist);
        littleFingerStretch_et=(EditText)findViewById(R.id.littleFingerStretch);
        littleFingerMove_et=(EditText)findViewById(R.id.littleFingerMove);

    }
}
