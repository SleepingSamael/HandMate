package com.chej.HandMate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

import com.chej.HandMate.Model.SysApplication;

public class AdminActivity extends AppCompatActivity {
    private TextView motor;//舵机状态报文显示
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


        motor = (TextView)findViewById(R.id.motor_log);
        for (int i=0;i<100;i++) {
            motor.append("\n"+i);
        }
    }
}
