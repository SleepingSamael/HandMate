package com.chej.HandMate;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.transition.Explode;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.chej.HandMate.Database.HistoryDataManager;
import com.chej.HandMate.Adapter.GalleryViewPagerAdapter;
import com.chej.HandMate.Model.GalleryItems;
import com.chej.HandMate.Model.MyCustomDialog;
import com.chej.HandMate.Model.SysApplication;
import com.chej.HandMate.Model.history.HistoryData;
import com.chej.HandMate.Model.users.UserData;
import com.chej.HandMate.TTS.SpeechUtil;
import com.chej.HandMate.Transmission.Wifi.WifiService;
import com.chej.HandMate.U3D.u3dPlayer;
import com.chej.library.ExpandingPagerFactory;
import com.chej.library.fragments.ExpandingFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GameItemActivity extends AppCompatActivity implements ExpandingFragment.OnExpandingClickListener,View.OnClickListener,PopupMenu.OnMenuItemClickListener {
    @Bind(R.id.viewPager) ViewPager viewPager;
    @Bind(R.id.back1)ViewGroup back;
    @Bind(R.id.DetailPhoto)ImageView DetailPhoto;
    @Bind(R.id.title) TextView title;
    @Bind(R.id.btn_start)Button start;
    @Bind(R.id.set2)Button set;
    @Bind(R.id.user)Button user;
    @Bind(R.id.tv_user)TextView tv_user;
    @Bind(R.id.introduce)TextView introduce;
    @Bind(R.id.clock)TextView clock;
    @Bind(R.id.home)ImageView home;
    @Bind(R.id.power)ImageView power;
    @Bind(R.id.volume)ImageView volume;
    @Bind(R.id.wifi)ImageView wifi;
    @Bind(R.id.bluetooth)ImageView bluetooth;
    private HistoryDataManager mhistoryDataManager;
    HistoryData historyData=new HistoryData();


    Bundle mbundle = new Bundle();//存储menu点击值
    int mode;//存储menu点击值

    private IMyAidlInterface iMyAidlInterface;
    private SpeechUtil speechUtil;

    //向下位机发送开始信号
    public void sendTrainAck(int mode) {

        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendTrainAck(mode,1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }
    //通知下位机开始发手套数据  0无手套数据 1 左手套数据 2 右手套数据
    public void senddGloveSelect(int gloveNum){
        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.senddGloveSelect(gloveNum);
            } catch (RemoteException e) {
                Log.e("sendTrainMode",e.toString());
            }
        }
    }
    //请求网络状态
    public void sendrNetStatus(){
        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendrNetStatus();
            } catch (RemoteException e) {
                Log.e("sendTrainMode",e.toString());
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_item);
        SysApplication.getInstance().addActivity(this);
        ButterKnife.bind(this);
        setupWindowAnimations();

        speechUtil = new SpeechUtil(this);

        sendrNetStatus();//载入时获取zigbee连接状态

        //获取患侧手信息
        SharedPreferences userSettings = getSharedPreferences("setting", 0);
        final int gloveFlag;
        if(userSettings.getString("glove","右").equals("右")){
            gloveFlag = 1;
        }else{
            gloveFlag = 2;
        }

        //获取系统时间
        SimpleDateFormat sDateFormat = new    SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String  sysDate = sDateFormat.format(new java.util.Date());
        String [] arr = sysDate.split("\\s+");
        final String sdate=arr[0];
        final String stime=arr[1];
        clock.setText(sdate+"   "+stime);

        Intent myServiceIntent = new Intent(GameItemActivity.this, WifiService.class);
        bindService(myServiceIntent, serviceConnection,
                Context.BIND_AUTO_CREATE);

        if (mhistoryDataManager == null) {
            mhistoryDataManager = new HistoryDataManager(this);
            mhistoryDataManager.openDataBase();            //建立本地数据库
        }

        //全局变量UserData中调取名字
        final UserData userData=(UserData)getApplication();
        tv_user.setText(userData.getUserName());

        Bundle bundle = this.getIntent().getExtras();//释放bundle
        mode = bundle.getInt("Mode") ;//释放bundle

        back.setClipChildren(false);
        viewPager.setClipChildren(false);

        //将容器的触摸事件反馈给ViewPager
        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
                return viewPager.dispatchTouchEvent(event);
            }
        });

        GalleryViewPagerAdapter adapter = new GalleryViewPagerAdapter(getSupportFragmentManager());
        adapter.addAll(generateTravelList());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);//设置当前viewpage是第几页


        ExpandingPagerFactory.setupViewPager2(viewPager);

        /**
         * 载入时设置详情图片和介绍
         */
        GalleryItems galleryItems = generateTravelList().get(viewPager.getCurrentItem());
        DetailPhoto.setImageResource(galleryItems.getImage());
        title.setText(galleryItems.getName());
        introduce.setText(galleryItems.getIntroduce());
        introduce.setMovementMethod(ScrollingMovementMethod.getInstance());

        //home键
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechUtil.speak("返回主界面");
                Intent mintent = new Intent(GameItemActivity.this, MainActivity.class);
                startActivity(mintent);
                finish();
            }
        });
        /**
         * 开始按钮
         */
        start.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i = new Intent(GameItemActivity.this, u3dPlayer.class);
                switch (title.getText().toString()) {
                    case "丰收果园":
                        historyData.setPid(userData.getUserId());
                        historyData.setHid(mhistoryDataManager.countData()+1);
                        historyData.setItem("游戏模式 丰收果园");
                        historyData.setDate(sdate);
                        historyData.setTime(stime);
                        mhistoryDataManager.inserHistorytData(historyData);
                        mbundle.putInt("ID",historyData.getHid());
                        mbundle.putInt("Mode", 4001);
                        mbundle.putString("Glove","0");
                        break;
                    case "欢乐大熊猫":
                        historyData.setPid(userData.getUserId());
                        historyData.setHid(mhistoryDataManager.countData()+1);
                        historyData.setItem("游戏模式 欢乐大熊猫");
                        historyData.setDate(sdate);
                        historyData.setTime(stime);
                        mhistoryDataManager.inserHistorytData(historyData);
                        mbundle.putInt("ID",historyData.getHid());
                        mbundle.putInt("Mode", 4002);
                        mbundle.putString("Glove","0");
                        break;
                    case "钢琴大师":
                        historyData.setPid(userData.getUserId());
                        historyData.setHid(mhistoryDataManager.countData()+1);
                        historyData.setItem("游戏模式 钢琴大师");
                        historyData.setDate(sdate);
                        historyData.setTime(stime);
                        mhistoryDataManager.inserHistorytData(historyData);
                        mbundle.putInt("ID",historyData.getHid());
                        mbundle.putInt("Mode", 4003);
                        mbundle.putString("Glove","0");
                        break;
                    case "打地鼠":
                        historyData.setPid(userData.getUserId());
                        historyData.setHid(mhistoryDataManager.countData()+1);
                        historyData.setItem("游戏模式 打地鼠");
                        historyData.setDate(sdate);
                        historyData.setTime(stime);
                        mhistoryDataManager.inserHistorytData(historyData);
                        mbundle.putInt("ID",historyData.getHid());
                        mbundle.putInt("Mode", 4004);
                        mbundle.putString("Glove","0");
                        break;
                }
                i.putExtras(mbundle);
                senddGloveSelect(gloveFlag);
                sendTrainAck(0);
                startActivity(i);
            }

        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ExpandingFragment expandingFragment = ExpandingPagerFactory.getCurrentFragment(viewPager);

            }

            @Override
            public void onPageSelected(int position) {
                GalleryItems galleryItems = generateTravelList().get(viewPager.getCurrentItem());
                DetailPhoto.setImageResource(galleryItems.getImage());
                title.setText(galleryItems.getName());
                introduce.setText(galleryItems.getIntroduce());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /**
         * 设置按钮
         */
        set.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(GameItemActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        power.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(GameItemActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        volume.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(GameItemActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        wifi.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(GameItemActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        bluetooth.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(GameItemActivity.this,SystemSetActivity.class);
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
        if(!ExpandingPagerFactory.onBackPressed(viewPager)){
            super.onBackPressed();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Explode slideTransition = new Explode();
        getWindow().setReenterTransition(slideTransition);
        getWindow().setExitTransition(slideTransition);
    }

    private List<GalleryItems> generateTravelList(){
        List<GalleryItems> galleryItemses = new ArrayList<>();
        for(int i=0;i<1;++i){
           // galleryItemses.add(new GalleryItems("丰收果园", R.drawable.applegame,"在金黄色的秋季里，没有什么比收获果实更让人心情愉悦的事了。作为农场之主的你，在今日决定去采摘下苹果，那么，出发吧！向着丰收，前进！（使用主动手套）"));
            galleryItemses.add(new GalleryItems("欢乐大熊猫", R.drawable.pandagame,"身为一只在树林里快乐生活的大熊猫，今天又到了进食的时间了。饥肠辘辘的你无意之间进入了树林中的一片竹林，看见天上掉的满满的竹子，高兴坏了，口水直流，到底能吃到多少的竹子就看你的表现了。（使用主动手套）"));
           // galleryItemses.add(new GalleryItems("钢琴大师", R.drawable.pianogame,"你是百年一遇的钢琴天才，应广大媒体的要求，在上海进行了一场巡回演出。接下里，就开始你的表演吧！（使用主动手套）"));
            galleryItemses.add(new GalleryItems("打地鼠", R.drawable.whackamole,"可恶的地鼠又来捣乱啦。身为农场主的你亲自要解决这些地鼠！挥起你的锤子，通过握拳，打掉他们吧！"));
        }
        return galleryItemses;
    }


    @Override
    public void onExpandingClick(View v) {

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
                i = new Intent(GameItemActivity.this, UsersActivity.class);
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
        MyCustomDialog.Builder builder=new MyCustomDialog.Builder(this);
        builder.setMessage("确定要关机吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle sdbundle = new Bundle();//存重启、关机信息
                Intent i;
                i = new Intent(GameItemActivity.this, ShutDownActivity.class);
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
        MyCustomDialog.Builder builder=new MyCustomDialog.Builder(this);
        builder.setMessage("确定要重启吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle sdbundle = new Bundle();//存重启、关机信息
                Intent i;
                i = new Intent(GameItemActivity.this, ShutDownActivity.class);
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
    /* @Override
     public void onExpandingClick(View v) {
         //v is expandingfragment layout
         View view = v.findViewById(R.id.image);
         GalleryItems travel = generateTravelList().get(viewPager.getCurrentItem());
         startInfoActivity(view,travel);
     }*/
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

        }
    };
    @Override
    public void onDestroy(){
        super.onDestroy();
        unbindService(serviceConnection);
    }
}