package com.qslll.expandingpager;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.qslll.expandingpager.adapter.TravelViewPagerAdapter;
import com.qslll.expandingpager.model.SysApplication;
import com.qslll.expandingpager.model.Travel;
import com.qslll.expandingpager.model.users.UserData;
import com.qslll.library.ExpandingPagerFactory;
import com.qslll.library.fragments.ExpandingFragment;

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



    public static final String ACTION_REBOOT =
            "android.intent.action.REBOOT";
    public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    private IMyAidlInterface iMyAidlInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupWindowAnimations();

        SysApplication.getInstance().addActivity(this);


        //获取系统时间
        SimpleDateFormat sDateFormat = new    SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String  sysDate = sDateFormat.format(new java.util.Date());
        String [] arr = sysDate.split("\\s+");
        final String sdate=arr[0];
        final String stime=arr[1];
        clock.setText(sdate+"   "+stime);

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

        TravelViewPagerAdapter adapter = new TravelViewPagerAdapter(getSupportFragmentManager());
        adapter.addAll(generateTravelList());
        viewPager.setAdapter(adapter);


        ExpandingPagerFactory.setupViewPager(viewPager);
        viewPager.setCurrentItem(2);//设置当前viewpage是第几页

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // ExpandingFragment expandingFragment = ExpandingPagerFactory.getCurrentFragment(viewPager);

            }

            @Override
            public void onPageSelected(int position) {
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
            }
        });
        /**
         * 切换用户按钮
         */
        user.setOnClickListener(this);

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

    private List<Travel> generateTravelList() {
        List<Travel> travels = new ArrayList<>();
        for (int i = 0; i < 1; ++i) {
            travels.add(new Travel("主从模式", R.drawable.masterslave, null));
            travels.add(new Travel("游戏模式", R.drawable.game, null));
            travels.add(new Travel("手套操", R.drawable.exercise, null));
            travels.add(new Travel("评估", R.drawable.evaluate, null));
            travels.add(new Travel("历史记录", R.drawable.history, null));
        }
        return travels;
    }



    @Override
    public void onExpandingClick(View v) {
        //v is expandingfragment layout
       /* ViewGroup vp =(ViewGroup)v.getParent();
        ViewGroup back = (ViewGroup)vp.getParent();
        int index=back.indexOfChild(vp);
        if (viewPager.getCurrentItem() != index) {
            viewPager.setCurrentItem(index);

        }*/

        Travel travel = generateTravelList().get(viewPager.getCurrentItem());
        Bundle mbundle = new Bundle();//存menu点击值
        if (travel.getName() == "游戏模式") {
            Intent i = new Intent(MainActivity.this, GameItemActivity.class);
            mbundle.putInt("Mode", 3);
            i.putExtras(mbundle);

            sendTrainMode(0);
            startActivity(i);
        }
        if (travel.getName() == "主从模式") {
            Intent i = new Intent(MainActivity.this, MasterSlaveActivity.class);
            mbundle.putInt("Mode", 3);
            i.putExtras(mbundle);

            sendTrainMode(1);
            startActivity(i);
        }
        if (travel.getName() == "手套操") {
            Intent i = new Intent(MainActivity.this, ExerciseActivity.class);
            mbundle.putInt("Mode", 3);
            i.putExtras(mbundle);

            sendTrainMode(2);
            startActivity(i);
        }
        if (travel.getName() == "评估") {
            Intent i = new Intent(MainActivity.this, EvaluateActivity.class);
            mbundle.putInt("Mode", 3);
            i.putExtras(mbundle);

            sendTrainMode(3);
            startActivity(i);
        }
        if (travel.getName() == "历史记录") {
            Intent i = new Intent(MainActivity.this, HistoryActivity.class);
            mbundle.putInt("Mode", 3);
            i.putExtras(mbundle);
            startActivity(i);
        }

    }

    //向下位机发送训练模式
    public void sendTrainMode(int mode) {

        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendTrainMode(mode);
            } catch (RemoteException e) {
                e.printStackTrace();
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


}



