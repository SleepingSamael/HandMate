package com.qslll.expandingpager.Database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.qslll.expandingpager.Model.users.UserData;
import com.qslll.expandingpager.Model.users.UsersConstant;

import static android.content.ContentValues.TAG;

/**
 * Created by samael on 2017/3/6.
 */

public class UserDataManager {
    private Context mContext = null;


    private SQLiteDatabase mSQLiteDatabase = null;
    private DataBaseManagementHelper mDatabaseHelper = null;

    //DataBaseManagementHelper继承自SQLiteOpenHelper
    private class DataBaseManagementHelper extends SQLiteOpenHelper {

        DataBaseManagementHelper(Context context) {
            super(context, UsersConstant.DATABASE_NAME, null, UsersConstant.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG,"db.getVersion()="+db.getVersion());

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "DataBaseManagementHelper onUpgrade");
            onCreate(db);
        }
    }

    public UserDataManager(Context context) {
        mContext = context;
        Log.i(TAG, "UserDataManager construction!");
    }
    //打开数据库
    public void openDataBase() throws SQLException {
        final SQLdm sqLdm=new SQLdm();
        mSQLiteDatabase =sqLdm.openDatabase(mContext);
        //mDatabaseHelper = new DataBaseManagementHelper(mContext);
        //mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
    }
    //关闭数据库
    public void closeDataBase() throws SQLException {
        mDatabaseHelper.close();
    }

    //添加新用户
    public long insertUserData(UserData userData) {
        String userName=userData.getUserName();
        String userID=userData.getUserId();
        String userSex=userData.getUserSex();
        int userAge=userData.getUserAge();
        String userDate =userData.getUserDate();
        String userAddress=userData.getUserAddress();
        String userLinkman = userData.getUserLinkman();
        String userTel = userData.getUserTel();
        String userDiag = userData.getUserDiag();
        ContentValues values = new ContentValues();
        values.put(UsersConstant.NAME, userName);
        values.put(UsersConstant._ID, userID);
        values.put(UsersConstant.SEX,userSex);
        values.put(UsersConstant.AGE,userAge);
        values.put(UsersConstant.DATE,userDate);
        values.put(UsersConstant.ADDRESS,userAddress);
        values.put(UsersConstant.LINKMAN,userLinkman);
        values.put(UsersConstant.TEL,userTel);
        values.put(UsersConstant.DIAGNOSIS,userDiag);
        return mSQLiteDatabase.insert(UsersConstant.TABLE_NAME,null, values);
    }
    //更新用户信息，如修改密码
    public boolean updateUserData(UserData userData) {
        String userName=userData.getUserName();
        String userID=userData.getUserId();
        String userSex=userData.getUserSex();
        int userAge=userData.getUserAge();
        String userDate =userData.getUserDate();
        String userAddress=userData.getUserAddress();
        String userLinkman = userData.getUserLinkman();
        String userTel = userData.getUserTel();
        String userDiag = userData.getUserDiag();
        ContentValues values = new ContentValues();
        values.put(UsersConstant.NAME, userName);
       // values.put(UsersConstant._ID, userID);
        values.put(UsersConstant.SEX,userSex);
        values.put(UsersConstant.AGE,userAge);
        values.put(UsersConstant.DATE,userDate);
        values.put(UsersConstant.ADDRESS,userAddress);
        values.put(UsersConstant.LINKMAN,userLinkman);
        values.put(UsersConstant.TEL,userTel);
        values.put(UsersConstant.DIAGNOSIS,userDiag);
        //return mSQLiteDatabase.update(UsersConstant.TABLE_NAME, values,null, null) > 0;
        return mSQLiteDatabase.update(UsersConstant.TABLE_NAME, values, UsersConstant._ID + "=" + userID, null) > 0;
    }

    //根据id删除用户
    public boolean deleteUserData(String id) {
        return mSQLiteDatabase.delete(UsersConstant.TABLE_NAME, UsersConstant._ID + "=" + id, null) > 0;
    }

    public Cursor fetchUserData(String id) throws SQLException {
        Cursor mCursor = mSQLiteDatabase.query(false, UsersConstant.TABLE_NAME, null, UsersConstant._ID
                + "=" + id, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    //
    public Cursor fetchAllUserDatas() {
        return mSQLiteDatabase.rawQuery("select * from "+ UsersConstant.TABLE_NAME,null);
    }

    //根据姓名排序
    public Cursor orderByName() {
        return mSQLiteDatabase.rawQuery("select * from "+ UsersConstant.TABLE_NAME + " order by name" ,null);
    }
    //根据ID排序
    public Cursor orderByID(String order) {
        return mSQLiteDatabase.rawQuery("select * from "+ UsersConstant.TABLE_NAME + " order by " + UsersConstant._ID +" "+order ,null);
    }
    //根据性别排序
    public Cursor orderBySex(String order) {
        return mSQLiteDatabase.rawQuery("select * from "+ UsersConstant.TABLE_NAME + " order by " + UsersConstant.SEX +" "+order ,null);
    }
    //根据日期排序
    public Cursor orderByDate(String order) {
        return mSQLiteDatabase.rawQuery("select * from "+ UsersConstant.TABLE_NAME + " order by " + UsersConstant.DATE +" "+order ,null);
    }
    //
    public String getStringByColumnName(String columnName, String id) {
        Cursor mCursor = fetchUserData(id);
        int columnIndex = mCursor.getColumnIndex(columnName);
        String columnValue = mCursor.getString(columnIndex);
        mCursor.close();
        return columnValue;
    }
    //
    public boolean updateUserDataById(String columnName, String id,
                                      String columnValue) {
        ContentValues values = new ContentValues();
        values.put(columnName, columnValue);
        return mSQLiteDatabase.update(UsersConstant.TABLE_NAME, values, UsersConstant._ID + "=" + id, null) > 0;
    }
    //根据用户名找用户，可以判断注册时用户名是否已经存在
    public int findUserByName(String userName){
        Log.i(TAG,"findUserByName , userName="+userName);
        int result=0;
        Cursor mCursor=mSQLiteDatabase.query(UsersConstant.TABLE_NAME, null, UsersConstant.NAME+"="+userName, null, null, null, null);
        if(mCursor!=null){
            result=mCursor.getCount();
            mCursor.close();
            Log.i(TAG,"findUserByName , result="+result);
        }
        return result;
    }
    /*
    根据SQL语句查询获得cursor对象
    db 数据库对象
    sql 查询sql语句
    selectionArgs 查询条件占位符
    返回查询结果
     */
    public static Cursor selectDataBySql(SQLiteDatabase db, String sql, String[] selectionArgs){
        Cursor cursor =null;
        if (db!=null){
            cursor = db.rawQuery(sql,selectionArgs);
        }
        return cursor;
    }

}
