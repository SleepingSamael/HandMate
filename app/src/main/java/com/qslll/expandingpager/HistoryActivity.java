package com.qslll.expandingpager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.qslll.expandingpager.Database.HistoryDataManager;
import com.qslll.expandingpager.Model.SysApplication;
import com.qslll.expandingpager.Model.history.HistoryConstant;
import com.qslll.expandingpager.Model.users.UserData;
import com.qslll.expandingpager.Timeline.HorizontalListView;
import com.qslll.expandingpager.Timeline.ItemBean;
import com.qslll.expandingpager.Timeline.TimeLineAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * 演示查询sdcard中数据库表中的数据适配到listview中
 */
public class HistoryActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener {
    private TextView tv_user;
    private Button user;
    private Button set;
    private HistoryDataManager mHistoryDataManager;
    private Context mContext;
    private TextView tv_name;
    private TextView tv_id;
    private TextView tv_sex;
    private TextView tv_age;
    private TextView tv_date;
    private TextView info;
    private TextView clock;
    private ImageView home;
    private ImageView power;
    private ImageView volume;
    private ImageView wifi;
    private ImageView bluetooth;
    //一个横向的ListView
    private HorizontalListView lv_timeline;
    private TimeLineAdapter adapter;
    List<ItemBean> datas = new ArrayList<ItemBean>();
    String[] allItem =null;
    String[] allDate =null;
    String[] allTime =null;
    String[] allScore =null;
    String[] allID = null;

    private static final String LOG_TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_history);

        SysApplication.getInstance().addActivity(this);

        //获取系统时间
        SimpleDateFormat sDateFormat = new    SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String  sysDate = sDateFormat.format(new java.util.Date());
        String [] arr = sysDate.split("\\s+");
        final String sdate=arr[0];
        final String stime=arr[1];
        clock=(TextView)findViewById(R.id.clock);
        clock.setText(sdate+"   "+stime);

        tv_user = (TextView) findViewById(R.id.tv_user);
        user = (Button) findViewById(R.id.user);
        set =(Button)findViewById(R.id.set);
        volume=(ImageView)findViewById(R.id.volume);
        power=(ImageView)findViewById(R.id.power);
        wifi=(ImageView)findViewById(R.id.wifi);
        bluetooth=(ImageView)findViewById(R.id.bluetooth);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_id = (TextView) findViewById(R.id.tv_id);
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        tv_age = (TextView) findViewById(R.id.tv_age);
        tv_date = (TextView) findViewById(R.id.tv_date);
        info = (TextView) findViewById(R.id.info);
        //全局变量UserData中调取名字
         UserData userData = (UserData) getApplication();
        tv_user.setText(userData.getUserName());
        //病历卡信息
        tv_name.setText(userData.getUserName());
        tv_age.setText(String.valueOf(userData.getUserAge()));
        tv_sex.setText(userData.getUserSex());
        tv_id.setText(userData.getUserId());
        tv_date.setText(userData.getUserDate());
        info.setText(userData.getUserName()+"，"+userData.getUserSex()+"，现年"
                +String.valueOf(userData.getUserAge()) +"岁，" + "于"+userData.getUserDate()
                +"日入住我院治疗,已完成一个康复疗程，剩余三个康复疗疗程，康复效果佳，见效快。"
                +userData.getUserDiag());


        if (mHistoryDataManager == null) {
            mHistoryDataManager = new HistoryDataManager(this);
            mHistoryDataManager.openDataBase();            //建立本地数据库
        }
        //1打开数据库输出流
        final Cursor cursor = mHistoryDataManager.fetchHistoryData(userData.getUserId());
        //数据库内容存入数组
        allItem = new String[cursor.getCount()];
        allDate = new String[cursor.getCount()];
        allTime = new String[cursor.getCount()];
        allScore = new String[cursor.getCount()];
        allID = new String[cursor.getCount()];
        int position = 0;
        //cursor.moveToPosition(position);
        if (cursor.getCount()!=0) {
            cursor.moveToFirst();
            int index1 = cursor.getColumnIndex(HistoryConstant.ITEM);
            int index2 = cursor.getColumnIndex(HistoryConstant.DATE);
            int index3 = cursor.getColumnIndex(HistoryConstant.TIME);
            int index4 = cursor.getColumnIndex(HistoryConstant.SCORE);
            int index5 = cursor.getColumnIndex(HistoryConstant._ID);
            String str1 = cursor.getString(index1);
            String str2 = cursor.getString(index2);
            String str3 = cursor.getString(index3);
            String str4 = cursor.getString(index4);
            String str5 = cursor.getString(index5);
            allItem[position] = str1;
            allDate[position] = str2;
            allTime[position] = str3;
            allScore[position] = str4;
            allID[position] = str5;
            while (cursor.moveToNext()) {
                str1 = cursor.getString(index1);
                str2 = cursor.getString(index2);
                str3 = cursor.getString(index3);
                str4 = cursor.getString(index4);
                str5 = cursor.getString(index5);
                position++;
                allItem[position] = str1;
                allDate[position] = str2;
                allTime[position] = str3;
                allScore[position] = str4;
                allID[position] = str5;

            }
        }
        //2.将数据源中数据加载到适配器中

/*        SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,R.layout.history_list_item,cursor,
                new String[]{HistoryConstant._ID,HistoryConstant.DATE,HistoryConstant.TIME,HistoryConstant.ITEM,HistoryConstant.SCORE},
                new int[]{0,R.id.tv_date,R.id.tv_time,R.id.tv_item,R.id.tv_score},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        //3.将适配器的数据加载到控件
        history_lv.setAdapter(adapter);


        history_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });*/

        //home键
        home=(ImageView)findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mintent = new Intent(HistoryActivity.this, MainActivity.class);
                startActivity(mintent);
                finish();

            }
        });
        user.setOnClickListener(this);
        tv_user.setOnClickListener(this);

        //时间线实现
        InitDatas();
        lv_timeline = (HorizontalListView) findViewById(R.id.list);
        adapter = new TimeLineAdapter(mContext, datas);
        lv_timeline.setAdapter(adapter);
        lv_timeline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取点击item的ID
                TextView c = (TextView) view.findViewById(R.id.hide_id);
                String CID = c.getText().toString();

                Bundle mbundle = new Bundle();//存history的id值

                Intent i = new Intent(HistoryActivity.this, ReportActivity.class);
                mbundle.putString("ID", CID);
                i.putExtras(mbundle);
                startActivity(i);
            }
        });


        /**
         * 设置按钮
         */
        set.setOnClickListener(new Button.OnClickListener() {//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(HistoryActivity.this, SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        power.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(HistoryActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        volume.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(HistoryActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        wifi.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(HistoryActivity.this,SystemSetActivity.class);
                startActivity(i);
                finish();
            }
        });
        bluetooth.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent i;
                i = new Intent(HistoryActivity.this,SystemSetActivity.class);
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
                i = new Intent(HistoryActivity.this, UsersActivity.class);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
        builder.setMessage("确定要关机吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle sdbundle = new Bundle();//存重启、关机信息
                Intent i;
                i = new Intent(HistoryActivity.this, ShutDownActivity.class);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
        builder.setMessage("确定要重启吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle sdbundle = new Bundle();//存重启、关机信息
                Intent i;
                i = new Intent(HistoryActivity.this, ShutDownActivity.class);
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
     * timeline插入数据
     */
    private void InitDatas() {

        for(int i = 0;i<allItem.length;i++)
        {
            ItemBean item = new ItemBean();
            item.setTitle(allItem[i]);
            item.setSubTitle(allScore[i]+" 分");//subtitle待修改
            item.setTime(allDate[i]+"  "+allTime[i]);
            item.setID(allID[i]);
            item.setStatu(1);
            datas.add(item);
        }
        // TODO Auto-generated method stub
    }



}
