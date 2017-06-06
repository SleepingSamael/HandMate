package com.qslll.expandingpager;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
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

import com.qslll.expandingpager.Database.HistoryDataManager;
import com.qslll.expandingpager.adapter.TravelViewPagerAdapter;
import com.qslll.expandingpager.model.SysApplication;
import com.qslll.expandingpager.model.Travel;
import com.qslll.expandingpager.model.history.HistoryData;
import com.qslll.expandingpager.model.users.UserData;
import com.qslll.library.ExpandingPagerFactory;
import com.qslll.library.fragments.ExpandingFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ExerciseActivity extends AppCompatActivity implements ExpandingFragment.OnExpandingClickListener,View.OnClickListener,PopupMenu.OnMenuItemClickListener{
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
    private HistoryDataManager mhistoryDataManager;
    HistoryData historyData=new HistoryData();

    Bundle mbundle = new Bundle();//存储menu点击值
    int mode;//存储menu点击值

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

                Intent mintent = new Intent(ExerciseActivity.this, MainActivity.class);
                startActivity(mintent);
                finish();

            }
        });
        /**
         * 开始按钮
         */
        start.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i = new Intent(ExerciseActivity.this, com.qslll.expandingpager.U3D.u3dPlayer.class);
                switch (title.getText().toString()) {
                    case "手套操":
                        historyData.setPid(userData.getUserId());
                        historyData.setHid(mhistoryDataManager.countData()+1);
                        historyData.setItem("手套操");
                        historyData.setDate(sdate);
                        historyData.setTime(stime);
                        mhistoryDataManager.inserHistorytData(historyData);
                        mbundle.putInt("Mode", 2001);
                        break;
                }
                i.putExtras(mbundle);
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
                Travel travel = generateTravelList().get(viewPager.getCurrentItem());
                DetailPhoto.setImageResource(travel.getImage());
                title.setText(travel.getName());
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
                i = new Intent(ExerciseActivity.this,SystemSetActivity.class);
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
            travels.add(new Travel("手套操", R.drawable.exerciseitem,"手套操是专业康复医生根据患者康复需要所编成的训练操，患者通过手套带动进行动作连续，屏幕上显示当前进行的动作给患者直观视觉体验。"));

        }
        return travels;
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
                i = new Intent(ExerciseActivity.this, UsersActivity.class);
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
   /* @Override
    public void onExpandingClick(View v) {
        //v is expandingfragment layout
        View view = v.findViewById(R.id.image);
        Travel travel = generateTravelList().get(viewPager.getCurrentItem());
        startInfoActivity(view,travel);
    }*/
}


