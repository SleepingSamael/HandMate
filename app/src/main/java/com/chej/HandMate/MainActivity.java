package com.chej.HandMate;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chej.HandMate.Adapter.GalleryViewPagerAdapter;
import com.chej.HandMate.Model.GalleryItems;
import com.chej.HandMate.Model.MyCustomDialog;
import com.chej.HandMate.Model.SysApplication;
import com.chej.HandMate.Model.users.UserData;
import com.chej.HandMate.TTS.SpeechUtil;
import com.chej.HandMate.Transmission.ComService;
import com.chej.library.ExpandingPagerFactory;
import com.chej.library.fragments.ExpandingFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ExpandingFragment.OnExpandingClickListener,OnClickListener,OnMenuItemClickListener {
    @Bind(R.id.viewPager)
    ViewPager viewPager;
    @Bind(R.id.back)
    ViewGroup back;
    @Bind(R.id.set)
    Button set;
    @Bind(R.id.user)
    Button user;
    @Bind(R.id.tv_user)
    TextView tv_user;
    @Bind(R.id.clock)
    TextView clock;
    @Bind(R.id.power)ImageView power;
    @Bind(R.id.volume)ImageView volume;
    @Bind(R.id.wifi)ImageView wifi;
    @Bind(R.id.bluetooth)ImageView bluetooth;

    private IMyAidlInterface iMyAidlInterface;

    private SpeechUtil speechUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupWindowAnimations();

        SysApplication.getInstance().addActivity(this);


        speechUtil = new SpeechUtil(this);

        //获取系统时间
        SimpleDateFormat sDateFormat = new    SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String  sysDate = sDateFormat.format(new java.util.Date());
        String [] arr = sysDate.split("\\s+");
        final String sdate=arr[0];
        final String stime=arr[1];
        clock.setText(sdate+"   "+stime);

        sendrConfigData();

        Intent myServiceIntent = new Intent(MainActivity.this, ComService.class);
        bindService(myServiceIntent, serviceConnection,
                Context.BIND_AUTO_CREATE);


        //全局变量UserData中调取名字
        final UserData userData = (UserData) getApplication();
        tv_user.setText(userData.getUserName());

        back.setClipChildren(false);
        viewPager.setClipChildren(false);

        /**
         * 需要将整个页面的事件分发给ViewPager，不然的话只有ViewPager中间的view能滑动，其他的都不能滑动，
         * 这是肯定的，因为ViewPager总体布局就是中间那一块大小，其他的子布局都跑到ViewPager外面来了
         */
        //将容器的触摸事件反馈给ViewPager
        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //只把滑动反馈给ViewPager
                /*float my = 0;
                float startX = 0;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        my = event.getX();
                        break;
                }
                if(my==startX)
                {
                    return false;
                }
               else return viewPager.dispatchTouchEvent(event);*/

                return viewPager.dispatchTouchEvent(event);
            }
        });

        GalleryViewPagerAdapter adapter = new GalleryViewPagerAdapter(getSupportFragmentManager());
        adapter.addAll(generateTravelList());
        viewPager.setAdapter(adapter);


        ExpandingPagerFactory.setupViewPager(viewPager);
        viewPager.setCurrentItem(1);//设置当前viewpage是第几页

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // ExpandingFragment expandingFragment = ExpandingPagerFactory.getCurrentFragment(viewPager);
            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0: speechUtil.speak("主从模式");
                        break;
                    case 1: speechUtil.speak("手套操");
                        break;
                    case 2: speechUtil.speak("评估");
                        break;
                    case 3: speechUtil.speak("历史记录");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        /**
         * 设置按钮
         */
        set.setOnClickListener(new Button.OnClickListener() {//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(MainActivity.this, SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        power.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(MainActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        volume.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(MainActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        wifi.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(MainActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        bluetooth.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(MainActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        /**
         * 切换用户按钮
         */
        user.setOnClickListener(this);
        tv_user.setOnClickListener(this);
    }



    @Override
    public void onBackPressed() {
        if (!ExpandingPagerFactory.onBackPressed(viewPager)) {
            super.onBackPressed();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Explode slideTransition = new Explode();
        getWindow().setReenterTransition(slideTransition);
        getWindow().setExitTransition(slideTransition);
    }

    private List<GalleryItems> generateTravelList() {
        List<GalleryItems> galleryItemses = new ArrayList<>();
        for (int i = 0; i < 1; ++i) {
            galleryItemses.add(new GalleryItems("主从模式", R.drawable.masterslave, null));
            galleryItemses.add(new GalleryItems("手套操", R.drawable.exercise, null));
            galleryItemses.add(new GalleryItems("评估", R.drawable.evaluate, null));
            galleryItemses.add(new GalleryItems("历史记录", R.drawable.history, null));
        }
        return galleryItemses;
    }


    @Override
    public void onExpandingClick(View v) {
        sendrConfigData();
        GalleryItems galleryItems = generateTravelList().get(viewPager.getCurrentItem());
        Bundle mbundle = new Bundle();//存menu点击值
        if (galleryItems.getName() == "主从模式") {
            Intent i = new Intent(MainActivity.this, MasterSlaveActivity.class);
            mbundle.putInt("Mode", 3);
            i.putExtras(mbundle);

            sendTrainMode(1);
            startActivity(i);
            finish();
        }
        if (galleryItems.getName() == "手套操") {
            Intent i = new Intent(MainActivity.this, ExerciseActivity.class);
            mbundle.putInt("Mode", 3);
            i.putExtras(mbundle);

            sendTrainMode(2);
            startActivity(i);
            finish();
        }
        if (galleryItems.getName() == "评估") {
            Intent i = new Intent(MainActivity.this, EvaluateActivity.class);
            mbundle.putInt("Mode", 3);
            i.putExtras(mbundle);
            sendTrainMode(3);
            startActivity(i);
            finish();
        }
        if (galleryItems.getName() == "历史记录") {
            Intent i = new Intent(MainActivity.this, HistoryActivity.class);
            mbundle.putInt("Mode", 3);
            i.putExtras(mbundle);
            startActivity(i);
            finish();
        }

    }

    //向下位机发送训练模式
    public void sendTrainMode(int mode) {

        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendTrainMode(mode);
            } catch (RemoteException e) {
                Log.e("sendTrainMode",e.toString());
            }
        }
    }
    //请求配置信息
    public void sendrConfigData(){
        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendrConfigData();
            } catch (RemoteException e) {
                Log.e("sendrConfigData",e.toString());
            }
        }
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
                i = new Intent(MainActivity.this, UsersActivity.class);
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
        MyCustomDialog.Builder builder = new MyCustomDialog.Builder(MainActivity.this);
        builder.setMessage("确定要关机吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle sdbundle = new Bundle();//存重启、关机信息
                Intent i;
                i = new Intent(MainActivity.this, ShutDownActivity.class);
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
        MyCustomDialog.Builder builder = new MyCustomDialog.Builder(MainActivity.this);
        builder.setMessage("确定要重启吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle sdbundle = new Bundle();//存重启、关机信息
                Intent i;
                i = new Intent(MainActivity.this, ShutDownActivity.class);
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




    //绑定ComService
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(iBinder);
            try {
                iMyAidlInterface.registerCallback(iCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            Log.e("ERROR", "--->>连接失败.");
        }
    };


    private ICallBack.Stub iCallBack = new ICallBack.Stub() {
        @Override
        public void callBack(final Entity entity) throws RemoteException {

            Log.e("MainActivity","MainActivity receive the entity"+entity.getName());
            Log.e("MainActivity","MainActivity receive the entity"+entity.getName());
            Log.e("MainActivity","MainActivity receive the entity"+entity.getName());
            Log.e("MainActivity","MainActivity receive the entity"+entity.getName());
            Log.e("MainActivity","MainActivity receive the entity"+entity.getName());
            Log.e("MainActivity","MainActivity receive the entity"+entity.getName());
            Log.e("MainActivity","MainActivity receive the entity"+entity.getName());
        }
    };
    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(serviceConnection);
    }

}



