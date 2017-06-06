package com.qslll.expandingpager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.qslll.expandingpager.Database.UserDataManager;
import com.qslll.expandingpager.model.SysApplication;
import com.qslll.expandingpager.model.users.UserData;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UserEditActivity extends AppCompatActivity {
    private EditText name;
    private EditText age;
    private RadioButton male;
    private RadioButton female;
    private EditText ID;
    private EditText date;
    private EditText tel;
    private EditText linkman;
    private EditText diag;
    private Button save;
    private Button quit;
    private TextView clock;
    private UserDataManager mUserDataManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);
        name = (EditText) findViewById(R.id.editText_name);
        age = (EditText) findViewById(R.id.editText_age);
        male = (RadioButton) findViewById(R.id.radioButton_male);
        female = (RadioButton) findViewById(R.id.radioButton_female);
        ID = (EditText) findViewById(R.id.editText_id);
        date = (EditText) findViewById(R.id.editText_date);
        tel = (EditText) findViewById(R.id.editText_tel);
        linkman = (EditText) findViewById(R.id.editText_linkman);
        diag = (EditText) findViewById(R.id.editText_diag);
        save = (Button) findViewById(R.id.btn_save);
        quit= (Button) findViewById(R.id.btn_quit);
        final UserData userData = (UserData) getApplication();

        SysApplication.getInstance().addActivity(this);

        //获取系统时间
        SimpleDateFormat sDateFormat = new    SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String  sysDate = sDateFormat.format(new java.util.Date());
        String [] arr = sysDate.split("\\s+");
        final String sdate=arr[0];
        final String stime=arr[1];
        clock=(TextView)findViewById(R.id.clock);
        clock.setText(sdate+"   "+stime);


        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();            //建立本地数据库
        }

        Bundle bundle = this.getIntent().getExtras();
        final int mode=bundle.getInt("Mode");
        if (mode == 1) //修改信息，数据库中信息显示在界面上
        {
            ID.setKeyListener(null);
            name.setText(userData.getUserName());
            ID.setText(userData.getUserId());
            age.setText(String.valueOf(userData.getUserAge()));
            date.setText(userData.getUserDate());
            tel.setText(userData.getUserTel());
            linkman.setText(userData.getUserLinkman());
            diag.setText(userData.getUserDiag());
            if (userData.getUserSex() == "男")
                male.setChecked(true);
            else
                female.setChecked(true);
        } else {
            name.setText("");
            ID.setText("");
            age.setText("");
            date.setText("");
            tel.setText("");
            linkman.setText("");
            diag.setText("");
            male.setChecked(false);
            female.setChecked(false);
        }
        /**
         * data点击弹出日期选择框
         */
        date.setKeyListener(null);
        date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Calendar c = Calendar.getInstance();
                // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
                new DatePickerDialog(UserEditActivity.this,
                        // 绑定监听器
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                int month = monthOfYear+1;
                                date.setText(year+"-"+month+"-"+dayOfMonth);
                            }
                        }
                        // 设置初始日期
                        ,c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                        .get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        quit.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(UserEditActivity.this,UsersActivity.class);
                startActivity(i);
            }
        });

        /**
         * 保存按钮
         */
        save.setOnClickListener(new Button.OnClickListener() {//创建监听
            public void onClick(View v) {
                if (mode == 1)//修改数据
                {
                    if (ID.getText().toString().trim().equals("") || name.getText().toString().trim().equals("")
                            || age.getText().toString().trim().equals("")
                            || (male.isChecked() == false && female.isChecked() == false)) {

                        new AlertDialog.Builder(UserEditActivity.this)
                                .setTitle("警告").setMessage("带*项目不能为空，请重新输入！")
                                .setPositiveButton("确定", null).show();
                        return;
                    }
                    else
                    {
                        userData.setUserId(ID.getText().toString());
                        userData.setUserName(name.getText().toString());
                        userData.setUserAge(Integer.parseInt(age.getText().toString()));
                        userData.setUserDate(date.getText().toString());
                        userData.setUserTel(tel.getText().toString());
                        userData.setUserLinkman(linkman.getText().toString());
                        userData.setUserDiag(diag.getText().toString());
                        if (male.isChecked()) {
                            userData.setUserSex("男");
                        } else {
                            userData.setUserSex("女");
                        }
                        mUserDataManager.updateUserData(userData);
                        Toast.makeText(getApplicationContext(), "信息已保存",
                                Toast.LENGTH_SHORT).show();
                        Intent i;
                        i = new Intent(UserEditActivity.this, UsersActivity.class);
                        startActivity(i);

                    }

                }
                else {//新增数据
                    if (ID.getText().toString().trim().equals("") || name.getText().toString().trim().equals("")
                            || age.getText().toString().trim().equals("")
                            || (male.isChecked() == false && female.isChecked() == false)) {

                        new AlertDialog.Builder(UserEditActivity.this)
                                .setTitle("警告").setMessage("带*项目不能为空，请重新输入！")
                                .setPositiveButton("确定", null).show();
                        return;
                    }
                    else
                        {
                        userData.setUserId(ID.getText().toString());
                        userData.setUserName(name.getText().toString());
                        userData.setUserAge(Integer.parseInt(age.getText().toString()));
                        userData.setUserDate(date.getText().toString());
                        userData.setUserTel(tel.getText().toString());
                        userData.setUserLinkman(linkman.getText().toString());
                        userData.setUserDiag(diag.getText().toString());
                        if (male.isChecked()) {
                            userData.setUserSex("男");
                        } else {
                            userData.setUserSex("女");
                        }
                        mUserDataManager.insertUserData(userData);
                        Toast.makeText(getApplicationContext(), "信息已保存",
                                Toast.LENGTH_SHORT).show();
                        Intent i;
                        i = new Intent(UserEditActivity.this, UsersActivity.class);
                        startActivity(i);
                    }
                }
            }
        });


    }



}
