package com.qslll.expandingpager;

import android.annotation.TargetApi;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.qslll.expandingpager.model.SysApplication;
import com.qslll.expandingpager.model.users.UserData;

import java.text.SimpleDateFormat;

public class SystemSetActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener{
    private TextView tv_user;
    private Button user;
    private Button set;
    private ImageView home;
    private SeekBar light;
    private SeekBar volum;
    private Switch voice;
    private ImageView senior;
    private TextView clock;
    private AudioManager mAudioManager;
    // 最大音量
    private int maxVolume;
    // 当前音量
    private int currentVolume;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_set);
        SysApplication.getInstance().addActivity(this);
        tv_user=(TextView)findViewById(R.id.tv_user);
        user=(Button)findViewById(R.id.user);
        light = (SeekBar) findViewById(R.id.light_seekBar);
        volum = (SeekBar) findViewById(R.id.volum_seekBar);
        voice = (Switch) findViewById(R.id.voice_switch);
        senior = (ImageView)findViewById(R.id.senior_set);
        set = (Button)findViewById(R.id.set);


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

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        initViews();
        initVolume();
        initLight();
        //语音开关switch
        voice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {

                } else {

                }
            }
        });
        //高级设置
        senior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i;
                i = new Intent(SystemSetActivity.this, SeniorSetActivity.class);
                startActivity(i);
                finish();
            }
        });

        //home键
        home=(ImageView)findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mintent = new Intent(SystemSetActivity.this, MainActivity.class);
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
                i = new Intent(SystemSetActivity.this, SystemSetActivity.class);
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
        switch (item.getItemId()) {
            case R.id.change:
                Toast.makeText(this, "切换用户", Toast.LENGTH_SHORT).show();
                Intent i;
                i = new Intent(SystemSetActivity.this, UsersActivity.class);
                startActivity(i);
                finish();
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

    /**
     * 获取当前屏幕亮度
     */
    private void initLight() {
        float currentBright = 0.0f;
        try {
            // 系统亮度值范围：0～255，应用窗口亮度范围：0.0f～1.0f。
            currentBright = android.provider.Settings.System.getInt(
                    getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS) * 100;
        } catch (Exception e) {
            e.printStackTrace();
        }
        light.setProgress((int) currentBright);
        // 转换成百分比
        //light_tv.setText("当前亮度：" + (int) currentBright + "%");
    }

    private void initViews() {
        this.volum
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        // 设置音量
                        mAudioManager.setStreamVolume(
                                AudioManager.STREAM_MUSIC, progress, 0);
                       // voice_tv.setText("当前音量百分比：" + progress * 100
                        //        / maxVolume + " %");
                    }
                });
        // 调节亮度
        this.light
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        /**
                         *在android 6.0及以后，WRITE_SETTINGS权限的保护等级已经由原来的dangerous升级为signature
                         * 这意味着我们的APP需要用系统签名或者成为系统预装软件才能够申请此权限
                         * 并且还需要提示用户跳转到修改系统的设置界面去授予此权限
                         * 也就是说，要想申请该权限，apk必须要打包，签名打包。要签名打包，debug模式是不能申请该权限
                         */
                        /*
                        // 设置系统亮度模式
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        // 设置系统亮度
                        Settings.System.putInt(getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS, progress);*/
                        //设置当前窗口亮度
                        WindowManager.LayoutParams lp = getWindow()
                                .getAttributes();
                        lp.screenBrightness = Float.valueOf(progress)
                                * (1f / 100f);
                        // 调节亮度
                        getWindow().setAttributes(lp);
                       // light_tv.setText("当前亮度：" + progress + "%");
                    }
                });
    }

    /**
     * 初始化音量数据
     *
     * @description：
     * @author ldm
     * @date 2016-12-2 下午3:20:05
     */
    private void initVolume() {
        // 获取系统最大音量
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 设置voice_seekbar的最大值
        volum.setMax(maxVolume);
        // 获取到当前 设备的音量
        currentVolume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        // 显示音量
       // voice_tv.setText("当前音量百分比：" + currentVolume * 100 / maxVolume + " %");
    }

    //申请权限
    private void requestWriteSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Settings.System.canWrite(this)) {
                Log.i("ERRRRRRRRRRRRR", "onActivityResult write settings granted");
            }
        }
    }
}
