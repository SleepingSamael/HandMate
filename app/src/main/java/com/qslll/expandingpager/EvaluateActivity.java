package com.qslll.expandingpager;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import com.qslll.expandingpager.Database.HistoryDataManager;
import com.qslll.expandingpager.Adapter.TravelViewPagerAdapter;
import com.qslll.expandingpager.Model.SysApplication;
import com.qslll.expandingpager.Model.Travel;
import com.qslll.expandingpager.Model.history.HistoryData;
import com.qslll.expandingpager.Model.users.UserData;
import com.qslll.expandingpager.Transmission.ComService;
import com.qslll.library.ExpandingPagerFactory;
import com.qslll.library.fragments.ExpandingFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EvaluateActivity extends AppCompatActivity implements ExpandingFragment.OnExpandingClickListener,OnClickListener,OnMenuItemClickListener {
    @Bind(R.id.viewPager) ViewPager viewPager;
    @Bind(R.id.back1)ViewGroup back;
    @Bind(R.id.DetailPhoto)ImageView DetailPhoto;
    @Bind(R.id.title) TextView title;
    @Bind(R.id.btn_start)Button start;
    @Bind(R.id.set2)Button set;
    @Bind(R.id.user)Button user;
    @Bind(R.id.tv_user) TextView tv_user;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_item);
        SysApplication.getInstance().addActivity(this);
        ButterKnife.bind(this);
        setupWindowAnimations();

        //获取系统时间

        SimpleDateFormat sDateFormat = new    SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String  sysDate = sDateFormat.format(new java.util.Date());
        String [] arr = sysDate.split("\\s+");
        final String sdate=arr[0];
        final String stime=arr[1];
        clock.setText(sdate+"   "+stime);


        Intent myServiceIntent = new Intent(EvaluateActivity.this, ComService.class);
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
        TravelViewPagerAdapter adapter = new TravelViewPagerAdapter(getSupportFragmentManager());
        adapter.addAll(generateTravelList());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);//设置当前viewpage是第几页


        ExpandingPagerFactory.setupViewPager2(viewPager);

        /**
         * 载入时设置详情图片和介绍
         */
        Travel travel = generateTravelList().get(viewPager.getCurrentItem());
        DetailPhoto.setImageResource(travel.getImage());
        title.setText(travel.getName());
        introduce.setText(travel.getIntroduce());
        //home键
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mintent = new Intent(EvaluateActivity.this, MainActivity.class);
                startActivity(mintent);
                finish();

            }
        });
        /**
         * 开始按钮
         */
        start.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                switch (title.getText().toString()) {
                    case "评估模式":
                        historyData.setPid(userData.getUserId());
                        historyData.setHid(mhistoryDataManager.countData()+1);
                        historyData.setItem("评估模式");
                        historyData.setDate(sdate);
                        historyData.setTime(stime);
                        mhistoryDataManager.inserHistorytData(historyData);
                        i = new Intent(EvaluateActivity.this, com.qslll.expandingpager.U3D.u3dPlayer.class);
                        mbundle.putInt("Mode", 3001);
                        i.putExtras(mbundle);
                        sendTrainAck(3);
                        startActivity(i);
                        break;
                    case "评估量表":
                        i = new Intent(EvaluateActivity.this,EvaluateTableActivity.class);
                        startActivity(i);
                        break;
                }

            }

        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ExpandingFragment expandingFragment = ExpandingPagerFactory.getCurrentFragment(viewPager);

            }

            @Override
            public void onPageSelected(int position) {
                Travel travel = generateTravelList().get(viewPager.getCurrentItem());
                DetailPhoto.setImageResource(travel.getImage());
                title.setText(travel.getName());
                introduce.setText(travel.getIntroduce());
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
                i = new Intent(EvaluateActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        power.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(EvaluateActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        volume.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(EvaluateActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        wifi.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(EvaluateActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        bluetooth.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(EvaluateActivity.this,SystemSetActivity.class);
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

    private List<Travel> generateTravelList(){
        List<Travel> travels = new ArrayList<>();
        for(int i=0;i<1;++i){
            travels.add(new Travel("评估模式", R.drawable.game3,"评估模式旨在对患者的手指活动能力作出评估，" +
                    "患者通过屏幕上的提示尽力做出相应动作，" +
                    "程序根据患者动作的时间与到位程度进行打分，协助医生对患者病情评估。"));
            travels.add(new Travel("评估量表", R.drawable.evaluatetable,"评估量表提供手部活动能力的评估量表让医生对患者进行打分，" +
                    "从而协助医生对患者病情评估。"));

        }
        return travels;
    }


    @Override
    public void onExpandingClick(View v) {

    }

   /* @Override
    public void onExpandingClick(View v) {
        //v is expandingfragment layout
        View view = v.findViewById(R.id.image);
        Travel travel = generateTravelList().get(viewPager.getCurrentItem());
        startInfoActivity(view,travel);
    }*/

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
                i = new Intent(EvaluateActivity.this, UsersActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.restart:
                i = new Intent(EvaluateActivity.this, ShutDownActivity.class);
                sdbundle.putInt("Mode", 1);
                i.putExtras(sdbundle);
                startActivity(i);
                break;
            case R.id.exit:
                i = new Intent(EvaluateActivity.this, ShutDownActivity.class);
                sdbundle.putInt("Mode", 0);
                i.putExtras(sdbundle);
                startActivity(i);
                break;
            default:
                break;
        }
        return false;
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


