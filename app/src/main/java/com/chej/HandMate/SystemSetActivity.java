package com.chej.HandMate;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.chej.HandMate.Model.MyCustomDialog;
import com.chej.HandMate.Model.SysApplication;
import com.chej.HandMate.Database.users.UserData;
import com.chej.HandMate.TTS.SpeechUtil;
import com.chej.HandMate.fragments.CommonTop;

import java.text.SimpleDateFormat;

public class SystemSetActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener, CommonTop.OnCommonBottomClick {
    private TextView tv_user;
    private Button user;
    private Button set;
    private ImageView home;
    private SeekBar light;
    private SeekBar volum;
    private Switch voice;
    private TableRow senior;
    private AudioManager mAudioManager;
    private Context mcontext;
    CommonTop commonTop;
    // 最大音量
    private int maxVolume;
    // 当前音量
    private int currentVolume;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;

    private SpeechUtil speechUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_set);

        //复用代码块的实例化
        commonTop=new CommonTop(this);
        commonTop.init().setListener(this);

        SysApplication.getInstance().addActivity(this);
        mcontext=this;
        //获取Preferences
        final SharedPreferences userSettings = UserData.getContext().getSharedPreferences("setting", Context.MODE_APPEND);
        tv_user=(TextView)findViewById(R.id.tv_user);
        user=(Button)findViewById(R.id.user);
        light = (SeekBar) findViewById(R.id.light_seekBar);
        volum = (SeekBar) findViewById(R.id.volum_seekBar);
        voice = (Switch) findViewById(R.id.voice_switch);
        if(userSettings.getString("voiceSwitch","on").equals("on"))
        {
            voice.setChecked(true);
        }
        else {
            voice.setChecked(false);
        }
        senior = (TableRow) findViewById(R.id.senior_set);
        set = (Button)findViewById(R.id.set);

        speechUtil = new SpeechUtil(this);
        speechUtil.speak("系统设置");

        //全局变量UserData中调取名字
        final UserData userData=(UserData)getApplication();
        tv_user.setText(userData.getUserName());
        user.setOnClickListener(this);
        tv_user.setOnClickListener(this);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        initVolume();
        initLight();
        initViews();
        //语音开关switch
        voice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    //让setting处于编辑状态
                    SharedPreferences.Editor editor = userSettings.edit();
                    //存放数据
                    editor.putString("voiceSwitch","on");
                    //d、完成提交
                    editor.commit();
                    speechUtil.release();
                    speechUtil = new SpeechUtil(mcontext);
                    speechUtil.speak("语音功能已开启");

                } else {
                    //让setting处于编辑状态
                    SharedPreferences.Editor editor = userSettings.edit();
                    //存放数据
                    editor.putString("voiceSwitch","off");
                    //d、完成提交
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "语音功能已关闭", Toast.LENGTH_SHORT).show();
                    speechUtil.release();
                    speechUtil = new SpeechUtil(mcontext);
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
                speechUtil.speak("返回主界面");
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
                restartDialog();
                break;
            case R.id.exit:
                shutDownDialog();
                break;
            default:
                break;
        }
        return false;
    }
    private void shutDownDialog() {
        speechUtil.speak("确定要关机吗");
        MyCustomDialog.Builder builder=new MyCustomDialog.Builder(this);
        builder.setMessage("确定要关机吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle sdbundle = new Bundle();//存重启、关机信息
                Intent i;
                i = new Intent(SystemSetActivity.this, ShutDownActivity.class);
                sdbundle.putInt("Mode", 0);
                i.putExtras(sdbundle);
                startActivity(i);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void restartDialog() {
        speechUtil.speak("确定要重启吗");
        MyCustomDialog.Builder builder=new MyCustomDialog.Builder(this);
        builder.setMessage("确定要重启吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle sdbundle = new Bundle();//存重启、关机信息
                Intent i;
                i = new Intent(SystemSetActivity.this, ShutDownActivity.class);
                sdbundle.putInt("Mode", 1);
                i.putExtras(sdbundle);
                startActivity(i);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
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
                        commonTop=new CommonTop(mcontext);
                        commonTop.init().setListener((CommonTop.OnCommonBottomClick) mcontext);
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

                        // 设置系统亮度模式
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        // 设置系统亮度
                        Settings.System.putInt(getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS, progress);
                        //设置当前窗口亮度
                     //   WindowManager.LayoutParams lp = getWindow()
                     //           .getAttributes();
                     //   lp.screenBrightness = Float.valueOf(progress)
                     //           * (1f / 100f);
                        // 调节亮度
                    //    getWindow().setAttributes(lp);
                        // light_tv.setText("当前亮度：" + progress + "%");
                    }
                });
    }

    /**
     * @description：初始化音量数据
     * @date 2016-12-2 下午3:20:05
     */
    private void initVolume() {
        // 获取系统最大音量
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 设置voice_seekbar的最大值
        volum.setMax(maxVolume);
        // 获取到当前 设备的音量
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 显示音量
       Log.e("当前音量百分比：" , currentVolume * 100 / maxVolume + " %");
        volum.setProgress(currentVolume);
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
                Log.e("ERRRRRRRRRRRRR", "onActivityResult write settings granted");
            }
        }
    }

}
