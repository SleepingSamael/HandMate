package com.chej.HandMate;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView;

import com.chej.HandMate.Database.UserDataManager;
import com.chej.HandMate.Model.MyCustomDialog;
import com.chej.HandMate.Model.SysApplication;
import com.chej.HandMate.Model.users.UserData;
import com.chej.HandMate.Model.users.UsersConstant;
import com.chej.HandMate.Search.CharacterParser;
import com.chej.HandMate.Search.PinyinComparator;
import com.chej.HandMate.Search.SortAdapter;
import com.chej.HandMate.Search.SortModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements View.OnClickListener,PopupMenu.OnMenuItemClickListener {
    private GridView users_gv;
    private  Button user;
    private UserDataManager mUserDataManager;
    private android.widget.SearchView searchView;
    private SortAdapter adapter;
    private Button sortByID;
    private Button sortBySex;
    private Button sortByDate;
    private Button sortByName;
    private Button add;
    private TextView clock;
    Bundle userbundle = new Bundle();//区分修改信息和新增用户界面




    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        users_gv=(GridView) findViewById(R.id.users_lv);
        user=(Button)findViewById(R.id.user);
        searchView=(android.widget.SearchView) findViewById(R.id.userSearchview);
        sortBySex=(Button) findViewById(R.id.sortBySex);
        sortByID=(Button) findViewById(R.id.sortByID);
        sortByDate=(Button) findViewById(R.id.sortByTime);
        sortByName=(Button) findViewById(R.id.sortByName);
        add =(Button)findViewById(R.id.add);
        final UserData userData=(UserData) getApplication();

        SysApplication.getInstance().addActivity(this);


        //获取系统时间
        SimpleDateFormat sDateFormat = new    SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String  sysDate = sDateFormat.format(new java.util.Date());
        String [] arr = sysDate.split("\\s+");
        final String sdate=arr[0];
        final String stime=arr[1];
        clock=(TextView)findViewById(R.id.clock);
        clock.setText(sdate+"   "+stime);

        //移除焦点
        searchView.setFocusable(false);
        //默认缩成放大镜button
        //searchView.setIconifiedByDefault(true);

        //修改信息
        /*Intent i;
        i = new Intent(UsersActivity.this,UserEditActivity.class);
        userbundle.putInt("Mode", 0);
        i.putExtras(userbundle);
        startActivity(i);*/
        /**
         * 切换用户按钮
         */
        user.setOnClickListener(this);


        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();            //建立本地数据库
        }
        //1打开数据库输出流
        final Cursor cursor=mUserDataManager.fetchAllUserDatas();
        //2.将数据源中数据加载到适配器中
        /*
        SimpleCursorAdapter(Context context,int layout, Cursor c,String[] from,
              int[] to, int flags)
              Context context 上下文对象
              int layout 表示适配器控件中每项item的布局id
              Cursor c 表示Cursor数据源
              String[] from 表示Cursor中数据表字段的数组
              int[] to 表示展示字段对应值的控件资源id
              int flags 设置适配器的标记
         */
        SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,R.layout.user_list_item,cursor,
                new String[]{UsersConstant._ID, UsersConstant.NAME,UsersConstant.SEX,UsersConstant.DATE,UsersConstant.AGE},
                new int[]{R.id.tv_id,R.id.tv_name,R.id.tv_sex,R.id.tv_indata,R.id.tv_age},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        //3.将适配器的数据加载到控件
        users_gv.setAdapter(adapter);

        users_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取点击item的ID
                TextView c = (TextView) view.findViewById(R.id.tv_id);
                String CID = c.getText().toString();

                Cursor cursor=mUserDataManager.fetchUserData(CID);

                userData.setUserName(cursor.getString(cursor.getColumnIndex("name")));
                userData.setUserId(cursor.getString(cursor.getColumnIndex("_id")));
                userData.setUserAge(cursor.getInt(cursor.getColumnIndex("age")));
                userData.setUserSex(cursor.getString(cursor.getColumnIndex("sex")));
                userData.setUserDate(cursor.getString(cursor.getColumnIndex("date")));
                userData.setUserAddress(cursor.getString(cursor.getColumnIndex("address")));
                userData.setUserTel(cursor.getString(cursor.getColumnIndex("tel")));
                userData.setUserLinkman(cursor.getString(cursor.getColumnIndex("linkman")));
                userData.setUserDiag(cursor.getString(cursor.getColumnIndex("diagnosis")));

                Intent i = new Intent(UsersActivity.this, MainActivity.class);
                startActivity(i);
                finish();

            }
        });
        // 添加长按点击弹出选择菜单
        users_gv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.clearHeader();
                //menu.setHeaderTitle("选择操作");
                menu.add(0, 0, 0, "删除用户");
                menu.add(0, 1, 0, "修改信息");
                menu.add(0, 1, 0, "查看信息");
            }

        });



        //按姓名排序，奇数次单击升序排列，偶数次降序
        sortByName.setOnClickListener(new Button.OnClickListener(){
            boolean flag = false;

            public void onClick(View v) {
                Drawable upDrawable= getResources().getDrawable(R.drawable.up);
                Drawable downDrawable= getResources().getDrawable(R.drawable.down);//升降序图标
                upDrawable.setBounds(0, 0, upDrawable.getMinimumWidth(), upDrawable.getMinimumHeight());
                downDrawable.setBounds(0, 0, downDrawable.getMinimumWidth(), downDrawable.getMinimumHeight());//升降序图标大小
                if(!flag)
                {
                    //升序
                    PinYinViews();
                    sortByName.setCompoundDrawables(null,null,upDrawable,null);

                }
                else
                {
                    //降序
                    showlist(mUserDataManager.orderByName());
                    sortByName.setCompoundDrawables(null,null,downDrawable,null);
                }
                flag=!flag;
                sortByName.setBackgroundResource(R.drawable.panon_clicked);
                sortByID.setBackgroundResource(R.drawable.panon);
                sortByID.setCompoundDrawables(null,null,null,null);
                sortBySex.setBackgroundResource(R.drawable.panon);
                sortBySex.setCompoundDrawables(null,null,null,null);
                sortByDate.setBackgroundResource(R.drawable.panon);
                sortByDate.setCompoundDrawables(null,null,null,null);
            }

        });
        //按ID排序，奇数次单击升序排列，偶数次降序
        sortByID.setOnClickListener(new Button.OnClickListener(){
            boolean flag = false;
            public void onClick(View v) {
                Drawable upDrawable= getResources().getDrawable(R.drawable.up);
                Drawable downDrawable= getResources().getDrawable(R.drawable.down);//升降序图标
                upDrawable.setBounds(0, 0, upDrawable.getMinimumWidth(), upDrawable.getMinimumHeight());
                downDrawable.setBounds(0, 0, downDrawable.getMinimumWidth(), downDrawable.getMinimumHeight());//升降序图标大小
                if(!flag)
                {
                    //升序
                    showlist(mUserDataManager.orderByID("asc"));
                    sortByID.setCompoundDrawables(null,null,upDrawable,null);
                }
                else
                {
                    //降序
                    showlist(mUserDataManager.orderByID("desc"));
                    sortByID.setCompoundDrawables(null,null,downDrawable,null);
                }
                flag=!flag;
                sortByName.setBackgroundResource(R.drawable.panon);
                sortByName.setCompoundDrawables(null,null,null,null);
                sortByID.setBackgroundResource(R.drawable.panon_clicked);
                sortBySex.setBackgroundResource(R.drawable.panon);
                sortBySex.setCompoundDrawables(null,null,null,null);
                sortByDate.setBackgroundResource(R.drawable.panon);
                sortByDate.setCompoundDrawables(null,null,null,null);
            }

        });
        //按性别排序，奇数次单击升序排列，偶数次降序
        sortBySex.setOnClickListener(new Button.OnClickListener(){
            boolean flag = false;
            public void onClick(View v) {
                Drawable upDrawable= getResources().getDrawable(R.drawable.up);
                Drawable downDrawable= getResources().getDrawable(R.drawable.down);//升降序图标
                upDrawable.setBounds(0, 0, upDrawable.getMinimumWidth(), upDrawable.getMinimumHeight());
                downDrawable.setBounds(0, 0, downDrawable.getMinimumWidth(), downDrawable.getMinimumHeight());//升降序图标大小
                if(!flag)
                {
                    //升序
                    showlist(mUserDataManager.orderBySex("asc"));
                    sortBySex.setCompoundDrawables(null,null,upDrawable,null);
                }
                else
                {
                    //降序
                    showlist(mUserDataManager.orderBySex("desc"));
                    sortBySex.setCompoundDrawables(null,null,downDrawable,null);
                }
                flag=!flag;
                sortByName.setBackgroundResource(R.drawable.panon);
                sortByName.setCompoundDrawables(null,null,null,null);
                sortByID.setBackgroundResource(R.drawable.panon);
                sortByID.setCompoundDrawables(null,null,null,null);
                sortBySex.setBackgroundResource(R.drawable.panon_clicked);
                sortByDate.setBackgroundResource(R.drawable.panon);
                sortByDate.setCompoundDrawables(null,null,null,null);
            }

        });
        //按日期排序，奇数次单击升序排列，偶数次降序
        sortByDate.setOnClickListener(new Button.OnClickListener(){
            boolean flag = false;
            public void onClick(View v) {
                Drawable upDrawable= getResources().getDrawable(R.drawable.up);
                Drawable downDrawable= getResources().getDrawable(R.drawable.down);//升降序图标
                upDrawable.setBounds(0, 0, upDrawable.getMinimumWidth(), upDrawable.getMinimumHeight());
                downDrawable.setBounds(0, 0, downDrawable.getMinimumWidth(), downDrawable.getMinimumHeight());//升降序图标大小
                if(!flag)
                {
                    //升序
                    showlist(mUserDataManager.orderByDate("asc"));
                    sortByDate.setCompoundDrawables(null,null,upDrawable,null);
                }
                else
                {
                    //降序
                    showlist(mUserDataManager.orderByDate("desc"));
                    sortByDate.setCompoundDrawables(null,null,downDrawable,null);
                }
                flag=!flag;
                sortByName.setBackgroundResource(R.drawable.panon);
                sortByName.setCompoundDrawables(null,null,null,null);
                sortByID.setBackgroundResource(R.drawable.panon);
                sortByID.setCompoundDrawables(null,null,null,null);
                sortBySex.setBackgroundResource(R.drawable.panon);
                sortBySex.setCompoundDrawables(null,null,null,null);
                sortByDate.setBackgroundResource(R.drawable.panon_clicked);
            }

        });

        //根据输入框输入值的改变来过滤搜索

        // 设置搜索文本监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    users_gv.setFilterText(newText);
                    //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                   filterData(newText);
                }else{
                   // users_gv.clearTextFilter();
                    showlist(mUserDataManager.fetchAllUserDatas());//刷新列表
                }
                return false;
            }
        });

        //添加用户
        add.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Intent i;
                i = new Intent(UsersActivity.this, UserEditActivity.class);
                userbundle.putInt("Mode", 0);
                i.putExtras(userbundle);
                startActivity(i);
                finish();
            }

        });

    }


    //给菜单项添加事件
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //info.position得到listview中选择的条目绑定的view
        TextView c = (TextView) info.targetView.findViewById(R.id.tv_id);
        String UID = c.getText().toString();

        final  UserData userData=(UserData) getApplication();

        Cursor cursor=mUserDataManager.fetchUserData(UID);

        userData.setUserName(cursor.getString(cursor.getColumnIndex("name")));
        userData.setUserId(cursor.getString(cursor.getColumnIndex("_id")));
        userData.setUserAge(cursor.getInt(cursor.getColumnIndex("age")));
        userData.setUserSex(cursor.getString(cursor.getColumnIndex("sex")));
        userData.setUserDate(cursor.getString(cursor.getColumnIndex("date")));
        userData.setUserAddress(cursor.getString(cursor.getColumnIndex("address")));
        userData.setUserTel(cursor.getString(cursor.getColumnIndex("tel")));
        userData.setUserLinkman(cursor.getString(cursor.getColumnIndex("linkman")));
        userData.setUserDiag(cursor.getString(cursor.getColumnIndex("diagnosis")));

        switch (item.getItemId()) {
            case 0:
                deleteDialog();
                return true;
            case 1:
                Intent i;
                i = new Intent(UsersActivity.this,UserEditActivity.class);
                userbundle.putInt("Mode", 1);
                i.putExtras(userbundle);
                startActivity(i);
                finish();
                return true;
            case 2:
                Intent j;
                j = new Intent(UsersActivity.this,UserEditActivity.class);
                userbundle.putInt("Mode", 1);
                j.putExtras(userbundle);
                startActivity(j);
                finish();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    //刷新显示列表项
    private void showlist(Cursor cursor){
        SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,R.layout.user_list_item,cursor,
                new String[]{UsersConstant._ID, UsersConstant.NAME,UsersConstant.SEX,UsersConstant.DATE,UsersConstant.AGE},
                new int[]{R.id.tv_id,R.id.tv_name,R.id.tv_sex,R.id.tv_indata,R.id.tv_age},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        users_gv.setAdapter(adapter);
    }

    protected void deleteDialog() {
        MyCustomDialog.Builder builder=new MyCustomDialog.Builder(this);
        builder.setMessage("确认删除吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final  UserData userData=(UserData) getApplication();
                mUserDataManager.deleteUserData(userData.getUserId());//根据id删除数据
                showlist(mUserDataManager.fetchAllUserDatas());//刷新列表
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

    private void shutDownDialog() {
        MyCustomDialog.Builder builder=new MyCustomDialog.Builder(this);
        builder.setMessage("确定要关机吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle sdbundle = new Bundle();//存重启、关机信息
                Intent i;
                i = new Intent(UsersActivity.this, ShutDownActivity.class);
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

    public void restartDialog(){//弹出第一个对话框
        MyCustomDialog.Builder dialog=new MyCustomDialog.Builder(this);
        dialog.setTitle("提示")
                .setMessage("确定要重启吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle sdbundle = new Bundle();//存重启、关机信息
                        Intent j;
                        j = new Intent(UsersActivity.this, ShutDownActivity.class);
                        sdbundle.putInt("Mode", 1);
                        j.putExtras(sdbundle);
                        startActivity(j);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
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


    //按拼音排序
    private void PinYinViews() {

        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        pinyinComparator = new PinyinComparator();

        //数据库内容存入数组
        Cursor cursor=mUserDataManager.fetchAllUserDatas();
        String[] allName = new String[cursor.getCount()];
        String[] allID = new String[cursor.getCount()];
        String[] allDate = new String[cursor.getCount()];
        String[] allSex = new String[cursor.getCount()];
        String[] allAge = new String[cursor.getCount()];
        int position = 0;
        //cursor.moveToPosition(position);
        cursor.moveToFirst();
        int index1 = cursor.getColumnIndex(UsersConstant.NAME);
        int index2 = cursor.getColumnIndex(UsersConstant._ID);
        int index3 = cursor.getColumnIndex(UsersConstant.DATE);
        int index4 = cursor.getColumnIndex(UsersConstant.SEX);
        int index5 = cursor.getColumnIndex(UsersConstant.AGE);
        String str1 = cursor.getString(index1);
        String str2 = cursor.getString(index2);
        String str3 = cursor.getString(index3);
        String str4 = cursor.getString(index4);
        String str5 = cursor.getString(index5);
        allName[position] = str1;
        allID[position] = str2;
        allDate[position] = str3;
        allSex[position] = str4;
        allAge[position] = str5;
        while (cursor.moveToNext()) {
            str1 = cursor.getString(index1);
            str2 = cursor.getString(index2);
            str3 = cursor.getString(index3);
            str4 = cursor.getString(index4);
            str5 = cursor.getString(index5);
            position++;
            allName[position] = str1;
            allID[position] = str2;
            allDate[position] = str3;
            allSex[position] = str4;
            allAge[position] = str5;

        }

        SourceDateList = filledData(allName,allID,allDate,allSex,allAge);

        // 根据a-z进行排序源数据
        Collections.sort(SourceDateList, pinyinComparator);
        adapter = new SortAdapter(this, SourceDateList);
        users_gv.setAdapter(adapter);

    }
    /**
     * 为ListView填充数据
     * @param name
     * @return
     */
    private List<SortModel> filledData(String [] name,String [] id,String [] date,String [] sex,String[] age){
        List<SortModel> mSortList = new ArrayList<SortModel>();

        for(int i=0; i<name.length; i++){
            SortModel sortModel = new SortModel();
            sortModel.setName(name[i]);
            sortModel.setId(id[i]);
            sortModel.setDate(date[i]);
            sortModel.setSex(sex[i]);
            sortModel.setAge(age[i]);
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(name[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if(sortString.matches("[A-Z]")){
                sortModel.setSortLetters(sortString.toUpperCase());
            }else{
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private void filterData(String filterStr){
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        pinyinComparator = new PinyinComparator();
        //数据库内容存入数组
        Cursor cursor=mUserDataManager.fetchAllUserDatas();
        String[] allName = new String[cursor.getCount()];
        String[] allID = new String[cursor.getCount()];
        String[] allDate = new String[cursor.getCount()];
        String[] allSex = new String[cursor.getCount()];
        String[] allAge = new String[cursor.getCount()];
        int position = 0;
        //cursor.moveToPosition(position);
        cursor.moveToFirst();
        int index1 = cursor.getColumnIndex(UsersConstant.NAME);
        int index2 = cursor.getColumnIndex(UsersConstant._ID);
        int index3 = cursor.getColumnIndex(UsersConstant.DATE);
        int index4 = cursor.getColumnIndex(UsersConstant.SEX);
        int index5 = cursor.getColumnIndex(UsersConstant.AGE);
        String str1 = cursor.getString(index1);
        String str2 = cursor.getString(index2);
        String str3 = cursor.getString(index3);
        String str4 = cursor.getString(index4);
        String str5 = cursor.getString(index5);
        allName[position] = str1;
        allID[position] = str2;
        allDate[position] = str3;
        allSex[position] = str4;
        allAge[position] = str5;
        while (cursor.moveToNext()) {
            str1 = cursor.getString(index1);
            str2 = cursor.getString(index2);
            str3 = cursor.getString(index3);
            str4 = cursor.getString(index4);
            str5 = cursor.getString(index5);
            position++;
            allName[position] = str1;
            allID[position] = str2;
            allDate[position] = str3;
            allSex[position] = str4;
            allAge[position] = str5;
        }

        SourceDateList = filledData(allName,allID,allDate,allSex,allAge);

        if(TextUtils.isEmpty(filterStr)){
            filterDateList = SourceDateList;
        }else{
            filterDateList.clear();
            for(SortModel sortModel : SourceDateList){
                String name = sortModel.getName();
                if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter = new SortAdapter(this, filterDateList);
        users_gv.setAdapter(adapter);
    }


}
