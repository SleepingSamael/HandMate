package com.chej.HandMate;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.chej.HandMate.Model.MyCustomDialog;
import com.chej.HandMate.Model.SysApplication;
import com.chej.HandMate.Transmission.Wifi.WifiService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class AdminActivity extends AppCompatActivity {

    private EditText thumbFlat_et;//拇指
    private EditText thumbMiddle_et;
    private EditText thumbFist_et;
    private EditText thumbStretch_et;
    private EditText thumbMove_et;
    private EditText forefingerFlat_et;//食指
    private EditText forefingerMiddle_et;
    private EditText forefingerFist_et;
    private EditText forefingerStretch_et;
    private EditText forefingerMove_et;
    private EditText middleFingerFlat_et;//中指
    private EditText middleFingerMiddle_et;
    private EditText middleFingerFist_et;
    private EditText middleFingerStretch_et;
    private EditText middleFingerMove_et;
    private EditText ringFingerFlat_et;//无名指
    private EditText ringFingerMiddle_et;
    private EditText ringFingerFist_et;
    private EditText ringFingerStretch_et;
    private EditText ringFingerMove_et;
    private EditText littleFingerFlat_et;//小指
    private EditText littleFingerMiddle_et;
    private EditText littleFingerFist_et;
    private EditText littleFingerStretch_et;
    private EditText littleFingerMove_et;
    private EditText thumbRatio_et;//增益系数
    private EditText forefingerRatio_et;
    private EditText middleFingerRatio_et;
    private EditText ringFingerRatio_et;
    private EditText littleFingerRatio_et;
    private Button quit;
    private Button save;
    private ToggleButton glove;
    private LineChart mChart;

    private IMyAidlInterface iMyAidlInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        SysApplication.getInstance().addActivity(this);

        Intent myServiceIntent = new Intent(AdminActivity.this, WifiService.class);
        bindService(myServiceIntent, serviceConnection,
                Context.BIND_AUTO_CREATE);

        sendcSVCMode(4,1);
        TabHost th=(TabHost)findViewById(R.id.tabhost);
        th.setup();            //初始化TabHost容器

        //在TabHost创建标签，然后设置：标题／图标／标签页布局
        th.addTab(th.newTabSpec("tab1").setIndicator("手套配置",getResources().getDrawable(R.drawable.logo)).setContent(R.id.tab_glove));
        th.addTab(th.newTabSpec("tab2").setIndicator("标签2",null).setContent(R.id.tab2));
        th.addTab(th.newTabSpec("tab3").setIndicator("标签3",null).setContent(R.id.tab3));
        //上面的null可以为getResources().getDrawable(R.drawable.图片名)设置图标

        glove=(ToggleButton)findViewById(R.id.glove_toggleButton) ;
        quit=(Button)findViewById(R.id.btn_quit) ;
        save=(Button)findViewById(R.id.btn_save) ;
        thumbFlat_et=(EditText)findViewById(R.id.thumbFlat);
        thumbMiddle_et=(EditText)findViewById(R.id.thumbMiddle);
        thumbFist_et=(EditText)findViewById(R.id.thumbFist);
        thumbStretch_et=(EditText)findViewById(R.id.thumbStretch);
        thumbMove_et=(EditText)findViewById(R.id.thumbMove);
        forefingerFlat_et=(EditText)findViewById(R.id.forefingerFlat);
        forefingerMiddle_et=(EditText)findViewById(R.id.forefingerMiddle);
        forefingerFist_et=(EditText)findViewById(R.id.forefingerFist);
        forefingerStretch_et=(EditText)findViewById(R.id.forefingerStretch);
        forefingerMove_et=(EditText)findViewById(R.id.forefingerMove);
        middleFingerFlat_et=(EditText)findViewById(R.id.middleFingerFlat);
        middleFingerMiddle_et=(EditText)findViewById(R.id.middleFingerMiddle);
        middleFingerFist_et=(EditText)findViewById(R.id.middleFingerFist);
        middleFingerStretch_et=(EditText)findViewById(R.id.middleFingerStretch);
        middleFingerMove_et=(EditText)findViewById(R.id.middleFingerMove);
        ringFingerFlat_et=(EditText)findViewById(R.id.ringFingerFlat);
        ringFingerMiddle_et=(EditText)findViewById(R.id.ringFingerMiddle);
        ringFingerFist_et=(EditText)findViewById(R.id.ringFingerFist);
        ringFingerStretch_et=(EditText)findViewById(R.id.ringFingerStretch);
        ringFingerMove_et=(EditText)findViewById(R.id.ringFingerMove);
        littleFingerFlat_et=(EditText)findViewById(R.id.littleFingerFlat);
        littleFingerMiddle_et=(EditText)findViewById(R.id.littleFingerMiddle);
        littleFingerFist_et=(EditText)findViewById(R.id.littleFingerFist);
        littleFingerStretch_et=(EditText)findViewById(R.id.littleFingerStretch);
        littleFingerMove_et=(EditText)findViewById(R.id.littleFingerMove);
        thumbRatio_et=(EditText)findViewById(R.id.thumbRatio);
        forefingerRatio_et=(EditText)findViewById(R.id.forefingerRatio);
        middleFingerRatio_et=(EditText)findViewById(R.id.middleFingerRatio);
        ringFingerRatio_et=(EditText)findViewById(R.id.ringFingerRatio);
        littleFingerRatio_et=(EditText)findViewById(R.id.littleFingerRatio);

        //获取Preferences
        final SharedPreferences userSettings = getSharedPreferences("setting", 0);
        thumbFlat_et.setText(userSettings.getString("thumbFlat","10"));
        forefingerFlat_et.setText(userSettings.getString("foreFlat","10"));
        middleFingerFlat_et.setText(userSettings.getString("middleFlat","10"));
        ringFingerFlat_et.setText(userSettings.getString("ringFlat","10"));
        littleFingerFlat_et.setText(userSettings.getString("littleFlat","10"));
        thumbMiddle_et.setText(userSettings.getString("thumbMiddle","110"));
        forefingerMiddle_et.setText(userSettings.getString("foreMiddle","110"));
        middleFingerMiddle_et.setText(userSettings.getString("middleMiddle","110"));
        ringFingerMiddle_et.setText(userSettings.getString("ringMiddle","110"));
        littleFingerMiddle_et.setText(userSettings.getString("littleMiddle","110"));
        thumbFist_et.setText(userSettings.getString("thumbFist","120"));
        forefingerFist_et.setText(userSettings.getString("foreFist","140"));
        middleFingerFist_et.setText(userSettings.getString("middleFist","140"));
        ringFingerFist_et.setText(userSettings.getString("ringFist","140"));
        littleFingerFist_et.setText(userSettings.getString("littleFist","120"));
        thumbStretch_et.setText(userSettings.getString("thumbStretch","50"));
        forefingerStretch_et.setText(userSettings.getString("foreStretch","50"));
        middleFingerStretch_et.setText(userSettings.getString("middleStretch","50"));
        ringFingerStretch_et.setText(userSettings.getString("ringStretch","50"));
        littleFingerStretch_et.setText(userSettings.getString("littleStretch","50"));
        thumbMove_et.setText(userSettings.getString("thumbMove","113"));
        forefingerMove_et.setText(userSettings.getString("foreMove","113"));
        middleFingerMove_et.setText(userSettings.getString("middleMove","113"));
        ringFingerMove_et.setText(userSettings.getString("ringMove","113"));
        littleFingerMove_et.setText(userSettings.getString("littleMove","113"));
        thumbRatio_et.setText(userSettings.getString("thumbRatio","10"));
        forefingerRatio_et.setText(userSettings.getString("foreRatio","10"));
        middleFingerRatio_et.setText(userSettings.getString("middleRatio","10"));
        ringFingerRatio_et.setText(userSettings.getString("ringRatio","10"));
        littleFingerRatio_et.setText(userSettings.getString("littleRatio","10"));
        if(userSettings.getString("glove","右").equals("右")){
            glove.setChecked(false);
        }else{
            glove.setChecked(true);
        }

        quit.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                sendcSVCMode(4,0);
                Intent i;
                i = new Intent(AdminActivity.this, Hardwarectivity.class);
                startActivity(i);
                finish();
            }
        });
        save.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                if(thumbFlat_et.getText().toString().trim().equals("") || thumbMiddle_et.getText().toString().trim().equals("") ||
                        thumbFist_et.getText().toString().trim().equals("") || thumbStretch_et.getText().toString().trim().equals("") ||
                        thumbMove_et.getText().toString().trim().equals("") || forefingerFlat_et.getText().toString().trim().equals("") ||
                        forefingerMiddle_et.getText().toString().trim().equals("") || forefingerFist_et.getText().toString().trim().equals("") ||
                        forefingerStretch_et.getText().toString().trim().equals("") || forefingerMove_et.getText().toString().trim().equals("") ||
                        middleFingerFlat_et.getText().toString().trim().equals("") || middleFingerMiddle_et.getText().toString().trim().equals("") ||
                        middleFingerFist_et.getText().toString().trim().equals("") || middleFingerStretch_et.getText().toString().trim().equals("") ||
                        middleFingerMove_et.getText().toString().trim().equals("") || ringFingerFlat_et.getText().toString().trim().equals("") ||
                        ringFingerMiddle_et.getText().toString().trim().equals("") || ringFingerFist_et.getText().toString().trim().equals("") ||
                        ringFingerStretch_et.getText().toString().trim().equals("") || ringFingerMove_et.getText().toString().trim().equals("") ||
                        littleFingerFlat_et.getText().toString().trim().equals("") || littleFingerMiddle_et.getText().toString().trim().equals("") ||
                        littleFingerFist_et.getText().toString().trim().equals("") || littleFingerStretch_et.getText().toString().trim().equals("") ||
                        littleFingerMove_et.getText().toString().trim().equals("") || thumbRatio_et.getText().toString().trim().equals("")||
                        forefingerRatio_et.getText().toString().trim().equals("") ||middleFingerRatio_et.getText().toString().trim().equals("")||
                        ringFingerRatio_et.getText().toString().trim().equals("") ||littleFingerRatio_et.getText().toString().trim().equals(""))
                {
                    new MyCustomDialog.Builder(AdminActivity.this)
                            .setTitle("警告").setMessage("不能有空项目，请重新输入！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                    return;

                }
                else if(Integer.parseInt(thumbFlat_et.getText().toString())>180 || Integer.parseInt(thumbMiddle_et.getText().toString())>180 ||
                        Integer.parseInt(thumbFist_et.getText().toString())>180 || Integer.parseInt(thumbStretch_et.getText().toString())>180 ||
                        Integer.parseInt(forefingerFlat_et.getText().toString())>180 || Integer.parseInt(forefingerMiddle_et.getText().toString())>180 ||
                        Integer.parseInt(forefingerFist_et.getText().toString())>180 || Integer.parseInt(forefingerStretch_et.getText().toString())>180 ||
                        Integer.parseInt(middleFingerFlat_et.getText().toString())>180 || Integer.parseInt(middleFingerMiddle_et.getText().toString())>180 ||
                        Integer.parseInt(middleFingerFist_et.getText().toString())>180 || Integer.parseInt(middleFingerStretch_et.getText().toString())>180 ||
                        Integer.parseInt(ringFingerFlat_et.getText().toString())>180 || Integer.parseInt(ringFingerMiddle_et.getText().toString())>180 ||
                        Integer.parseInt(ringFingerFist_et.getText().toString())>180 || Integer.parseInt(ringFingerStretch_et.getText().toString())>180 ||
                        Integer.parseInt(littleFingerFlat_et.getText().toString())>180 || Integer.parseInt(littleFingerMiddle_et.getText().toString())>180 ||
                        Integer.parseInt(littleFingerFist_et.getText().toString())>180 || Integer.parseInt(littleFingerStretch_et.getText().toString())>180 ||
                        Integer.parseInt(thumbMove_et.getText().toString())>113 || Integer.parseInt(thumbMove_et.getText().toString())<10 ||
                        Integer.parseInt(forefingerMove_et.getText().toString())>113 || Integer.parseInt(forefingerMove_et.getText().toString())<10 ||
                        Integer.parseInt(middleFingerMove_et.getText().toString())>113 || Integer.parseInt(middleFingerMove_et.getText().toString())<10 ||
                        Integer.parseInt(ringFingerMove_et.getText().toString())>113 || Integer.parseInt(ringFingerMove_et.getText().toString())<10 ||
                        Integer.parseInt(littleFingerMove_et.getText().toString())>113 || Integer.parseInt(littleFingerMove_et.getText().toString())<10)
                {
                    new MyCustomDialog.Builder(AdminActivity.this)
                            .setTitle("警告").setMessage("数值超过有效范围，请重新输入！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                    return;
                }
                else {
                    //让setting处于编辑状态
                    SharedPreferences.Editor editor = userSettings.edit();
                    //存放数据
                    editor.putString("thumbFlat",thumbFlat_et.getText().toString());
                    editor.putString("foreFlat",forefingerFlat_et.getText().toString());
                    editor.putString("middleFlat",middleFingerFlat_et.getText().toString());
                    editor.putString("ringFlat",ringFingerFlat_et.getText().toString());
                    editor.putString("littleFlat",littleFingerFlat_et.getText().toString());
                    editor.putString("thumbMiddle",thumbMiddle_et.getText().toString());
                    editor.putString("foreMiddle",forefingerMiddle_et.getText().toString());
                    editor.putString("middleMiddle",middleFingerMiddle_et.getText().toString());
                    editor.putString("ringMiddle",ringFingerMiddle_et.getText().toString());
                    editor.putString("littleMiddle",littleFingerMiddle_et.getText().toString());
                    editor.putString("thumbFist",thumbFist_et.getText().toString());
                    editor.putString("foreFist",forefingerFist_et.getText().toString());
                    editor.putString("middleFist",middleFingerFist_et.getText().toString());
                    editor.putString("ringFist",ringFingerFist_et.getText().toString());
                    editor.putString("littleFist",littleFingerFist_et.getText().toString());
                    editor.putString("thumbStretch",thumbStretch_et.getText().toString());
                    editor.putString("foreStretch",forefingerStretch_et.getText().toString());
                    editor.putString("middleStretch",middleFingerStretch_et.getText().toString());
                    editor.putString("ringStretch",ringFingerStretch_et.getText().toString());
                    editor.putString("littleStretch",littleFingerStretch_et.getText().toString());
                    editor.putString("thumbMove",thumbMove_et.getText().toString());
                    editor.putString("foreMove",forefingerMove_et.getText().toString());
                    editor.putString("middleMove",middleFingerMove_et.getText().toString());
                    editor.putString("ringMove",ringFingerMove_et.getText().toString());
                    editor.putString("littleMove",littleFingerMove_et.getText().toString());
                    editor.putString("thumbRatio",thumbRatio_et.getText().toString());
                    editor.putString("foreRatio",forefingerRatio_et.getText().toString());
                    editor.putString("middleRatio",middleFingerRatio_et.getText().toString());
                    editor.putString("ringRatio",ringFingerRatio_et.getText().toString());
                    editor.putString("littleRatio",littleFingerRatio_et.getText().toString());
                    //d、完成提交
                    editor.commit();
                    Toast.makeText(getApplicationContext(), "信息已保存", Toast.LENGTH_SHORT).show();
                    String data = userSettings.getString("thumbFlat","10")+" "+userSettings.getString("foreFlat","10")+" "
                            +userSettings.getString("middleFlat","10")+" "+userSettings.getString("ringFlat","10")+
                            " "+userSettings.getString("littleFlat","10")+" "+userSettings.getString("thumbMiddle","110")
                            +" "+userSettings.getString("foreMiddle","110")+" "+userSettings.getString("middleMiddle","110")
                            +" "+userSettings.getString("ringMiddle","110")+" "+userSettings.getString("littleMiddle","110")
                            +" "+userSettings.getString("thumbFist","120")+" "+userSettings.getString("foreFist","140")
                            +" "+userSettings.getString("middleFist","140")+" "+userSettings.getString("ringFist","140")
                            +" "+userSettings.getString("littleFist","120")+" "+userSettings.getString("thumbStretch","50")
                            +" "+userSettings.getString("foreStretch","50")+" "+userSettings.getString("middleStretch","50")
                            +" "+userSettings.getString("ringStretch","50")+" "+userSettings.getString("littleStretch","50")
                            +" "+userSettings.getString("thumbMove","113")+" "+userSettings.getString("foreMove","113")
                            +" "+userSettings.getString("middleMove","113")+" "+userSettings.getString("ringMove","113")
                            +" "+userSettings.getString("littleMove","113")+" "+userSettings.getString("thumbRatio","10")
                            +" "+userSettings.getString("foreRatio","10")+" "+userSettings.getString("middleRatio","10")
                            +" "+userSettings.getString("ringRatio","10")+" "+userSettings.getString("littleRatio","10");
                    Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
                    sendConfigData(data);
                }
            }
        });

        glove.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //让setting处于编辑状态
                SharedPreferences.Editor editor = userSettings.edit();
                if(isChecked)
                {//存放数据
                    editor.putString("glove","左");
                }else {
                    editor.putString("glove","右");
                }
                //完成提交
                editor.commit();
            }
        });


        //图表
        mChart = (LineChart) findViewById(R.id.chart);

        Description description =new Description();
        description.setText("舵机报文折线图");
        mChart.setDescription(description);

        mChart.setNoDataText("暂时尚无数据");

        mChart.setTouchEnabled(true);

        // 可拖曳
        mChart.setDragEnabled(true);

        // 可缩放
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        mChart.setPinchZoom(true);

        // 设置图表的背景颜色
        mChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();

        // 数据显示的颜色
        data.setValueTextColor(Color.WHITE);

        // 先增加一个空的数据，随后往里面动态添加
        mChart.setData(data);

        // 图表的注解(只有当数据集存在时候才生效)
        Legend l = mChart.getLegend();

        // 可以修改图表注解部分的位置
        // l.setPosition(LegendPosition.LEFT_OF_CHART);

        // 线性，也可是圆
        l.setForm(LegendForm.LINE);

        // 颜色
        l.setTextColor(Color.WHITE);

        // x坐标轴
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);

        // 几个x坐标轴之间才绘制？
    //    xl.setSpaceBetweenLabels(5);

        // 如果false，那么x坐标轴将不可见
        xl.setEnabled(true);

        // 将X坐标轴放置在底部，默认是在顶部。
        xl.setPosition(XAxisPosition.BOTTOM);

        // 图表左边的y坐标轴线
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);

        // 最大值
        leftAxis.setAxisMaxValue(90f);

        // 最小值
        leftAxis.setAxisMinValue(40f);

        // 不一定要从0开始
        leftAxis.setStartAtZero(false);

        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        // 不显示图表的右边y坐标轴线
        rightAxis.setEnabled(false);



        // 每点击一次按钮，增加一个点
        Button addButton = (Button) findViewById(R.id.button);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addEntry();
            }
        });

    }

    // 添加进去一个坐标点
    private void addEntry() {

        LineData data = mChart.getData();

        // 每一个LineDataSet代表一条线，每张统计图表可以同时存在若干个统计折线，这些折线像数组一样从0开始下标。
        // 本例只有一个，那么就是第0条折线
        ILineDataSet set = data.getDataSetByIndex(0);

        // 如果该统计折线图还没有数据集，则创建一条出来，如果有则跳过此处代码。
        if (set == null) {
            set = createLineDataSet();
            data.addDataSet(set);
        }
        // 先添加一个x坐标轴的值
        // 因为是从0开始，data.getXValCount()每次返回的总是全部x坐标轴上总数量，所以不必多此一举的加1
    //    data.addXValue((data.getXValCount()) + "");

        // 生成随机测试数
        float f = (float) ((Math.random()) * 20 + 50);

        // set.getEntryCount()获得的是所有统计图表上的数据点总量，
        // 如从0开始一样的数组下标，那么不必多次一举的加1
        Entry entry = new Entry(f, set.getEntryCount());

        // 往linedata里面添加点。注意：addentry的第二个参数即代表折线的下标索引。
        // 因为本例只有一个统计折线，那么就是第一个，其下标为0.
        // 如果同一张统计图表中存在若干条统计折线，那么必须分清是针对哪一条（依据下标索引）统计折线添加。
        data.addEntry(entry, 0);

        // 像ListView那样的通知数据更新
        mChart.notifyDataSetChanged();

        // 当前统计图表中最多在x轴坐标线上显示的总量
        mChart.setVisibleXRangeMaximum(5);

        // y坐标轴线最大值
        // mChart.setVisibleYRange(30, AxisDependency.LEFT);

        // 将坐标移动到最新
        // 此代码将刷新图表的绘图
     //   mChart.moveViewToX(data.getXValCount() - 5);

        // mChart.moveViewTo(data.getXValCount()-7, 55f,
        // AxisDependency.LEFT);
    }

    // 初始化数据集，添加一条统计折线，可以简单的理解是初始化y坐标轴线上点的表征
    private LineDataSet createLineDataSet() {

        LineDataSet set = new LineDataSet(null, "动态添加的数据");
        set.setAxisDependency(AxisDependency.LEFT);

        // 折线的颜色
        set.setColor(ColorTemplate.getHoloBlue());

        set.setCircleColor(Color.WHITE);
        set.setLineWidth(10f);
        set.setCircleSize(5f);
        set.setFillAlpha(128);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.GREEN);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);
        set.setDrawValues(true);
        return set;
    }

    //向下位机发送配置信息
    public void sendConfigData(String Data) {
        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendConfigData(Data);
            } catch (RemoteException e) {
                Log.e("sendConfigData",e.toString());
            }
        }
    }

    /*
    当进入服务模式时，通知选择的服务模式和状态，此时下位机进入服务模式后停止运动。
    当退出服务模式后恢复运动，并使能最后发送的配置项。
    SVCMode 0：NULL1：版本升级2：网络状态3：部件状态4：运动配置
    ModeStatus 0：退出 1：进入
     */
    public void sendcSVCMode(int SVCMode,int ModeStatus) {
        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendcSVCMode(SVCMode, ModeStatus);
            } catch (RemoteException e) {
                Log.e("sendcSVCMode",e.toString());
            }
        }
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