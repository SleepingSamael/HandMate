package com.qslll.expandingpager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.qslll.expandingpager.model.SysApplication;
import com.qslll.expandingpager.model.users.UserData;

import java.text.SimpleDateFormat;

public class EvaluateTableActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener{
    private TextView tv_user;
    private Button user;
    private TextView clock;
    private Button set;
    private ImageView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate_table);
        SysApplication.getInstance().addActivity(this);
        tv_user=(TextView)findViewById(R.id.tv_user);
        user=(Button)findViewById(R.id.user);
        home=(ImageView)findViewById(R.id.home) ;

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

        set = (Button)findViewById(R.id.set);
        /**
         * 设置按钮
         */
        set.setOnClickListener(new Button.OnClickListener() {//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(EvaluateTableActivity.this, SystemSetActivity.class);
                startActivity(i);
            }
        });
        //home键
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mintent = new Intent(EvaluateTableActivity.this, MainActivity.class);
                startActivity(mintent);
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
        switch (item.getItemId()) {
            case R.id.change:
                Toast.makeText(this, "切换用户", Toast.LENGTH_SHORT).show();
                Intent i;
                i = new Intent(EvaluateTableActivity.this, UsersActivity.class);
                startActivity(i);
                break;
            case R.id.restart:
                Toast.makeText(this, "重新启动", Toast.LENGTH_SHORT).show();
                final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.exit:
                Toast.makeText(this, "退出系统", Toast.LENGTH_SHORT).show();
                //关闭整个程序
                SysApplication.getInstance().exit();
                break;
            default:
                break;
        }
        return false;
    }
}
