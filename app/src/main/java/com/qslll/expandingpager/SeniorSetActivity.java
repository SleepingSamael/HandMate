package com.qslll.expandingpager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.qslll.expandingpager.Model.SysApplication;
import com.qslll.expandingpager.Model.users.UserData;

import java.text.SimpleDateFormat;

public class SeniorSetActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener{
    private TextView tv_user;
    private Button user;
    private Button set;
    private CardView printSet;
    private CardView bluetoothSet;
    private CardView hardware;
    private TextView clock;
    private ImageView home;
    private ImageView power;
    private ImageView volume;
    private ImageView wifi;
    private ImageView bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senior_set);

        SysApplication.getInstance().addActivity(this);

        printSet=(CardView)findViewById(R.id.print_set);
        bluetoothSet=(CardView)findViewById(R.id.bluetooth_set);
        hardware =(CardView)findViewById(R.id.hardware);
        tv_user=(TextView)findViewById(R.id.tv_user);
        user=(Button)findViewById(R.id.user);
        set = (Button)findViewById(R.id.set);
        volume=(ImageView)findViewById(R.id.volume);
        power=(ImageView)findViewById(R.id.power);
        wifi=(ImageView)findViewById(R.id.wifi);
        bluetooth=(ImageView)findViewById(R.id.bluetooth);

        //获取系统时间
        SimpleDateFormat sDateFormat = new    SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String  sysDate = sDateFormat.format(new java.util.Date());
        String [] arr = sysDate.split("\\s+");
        final String sdate=arr[0];
        final String stime=arr[1];
        clock=(TextView)findViewById(R.id.clock);
        clock.setText(sdate+"   "+stime);

        //全局变量UserData中调取名字
        final UserData userData=(UserData)getApplication();
        tv_user.setText(userData.getUserName());
        user.setOnClickListener(this);
        tv_user.setOnClickListener(this);

        //硬件信息
        hardware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i;
                i = new Intent(SeniorSetActivity.this, Hardwarectivity.class);
                startActivity(i);
                finish();
            }
        });

        //home键
        home =(ImageView)findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mintent = new Intent(SeniorSetActivity.this, MainActivity.class);
                startActivity(mintent);
                finish();

            }
        });

        /**
         * 设置按钮
         */
        set.setOnClickListener(new Button.OnClickListener() {//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(SeniorSetActivity.this, SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        power.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(SeniorSetActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        volume.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(SeniorSetActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        wifi.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(SeniorSetActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        bluetooth.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(SeniorSetActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    //点击按钮后，加载弹出式菜单
    @Override
    public void onClick(View v) {
        //创建弹出式菜单对象（最低版本11）
        PopupMenu popup = new PopupMenu(this, v);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        //填充菜单
        inflater.inflate(R.menu.menu_switch, popup.getMenu());
        //绑定菜单项的点击事件
        popup.setOnMenuItemClickListener(this);
        //显示(这一行代码不要忘记了)
        popup.show();

    }

    //弹出式菜单的单击事件处理
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // TODO Auto-generated method stub
        Bundle sdbundle = new Bundle();//存重启、关机信息
        switch (item.getItemId()) {
            case R.id.change:
                Toast.makeText(this, "切换用户", Toast.LENGTH_SHORT).show();
                Intent i;
                i = new Intent(SeniorSetActivity.this, UsersActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.restart:
                i = new Intent(SeniorSetActivity.this, ShutDownActivity.class);
                sdbundle.putInt("Mode", 1);
                i.putExtras(sdbundle);
                startActivity(i);
                break;
            case R.id.exit:
                i = new Intent(SeniorSetActivity.this, ShutDownActivity.class);
                sdbundle.putInt("Mode", 0);
                i.putExtras(sdbundle);
                startActivity(i);
                break;
        }
        return false;
    }

}
