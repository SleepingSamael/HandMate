package com.chej.HandMate.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.chej.HandMate.Model.history.HistoryData;
import com.chej.HandMate.Model.history.HistoryConstant;

import static android.content.ContentValues.TAG;

/**
 * Created by samael on 2017/3/6.
 */

public class HistoryDataManager {
    private Context mContext = null;


    private SQLiteDatabase mSQLiteDatabase = null;
    private DataBaseManagementHelper mDatabaseHelper = null;

    //DataBaseManagementHelper继承自SQLiteOpenHelper
    private class DataBaseManagementHelper extends SQLiteOpenHelper {

        DataBaseManagementHelper(Context context) {
            super(context, HistoryConstant.DATABASE_NAME, null, HistoryConstant.DATABASE_VERSION);
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

    public HistoryDataManager(Context context) {
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

    //添加新记录
    public long inserHistorytData(HistoryData historyData) {
        int hid=historyData.getHid();
        String pid=historyData.getPid();
        String date=historyData.getDate();
        String time=historyData.getTime();
        String item=historyData.getItem();
        int score=historyData.getScore();
        ContentValues values = new ContentValues();
        values.put(HistoryConstant._ID, hid);
        values.put(HistoryConstant.PID, pid);
        values.put(HistoryConstant.DATE, date);
        values.put(HistoryConstant.TIME, time);
        values.put(HistoryConstant.ITEM, item);
        values.put(HistoryConstant.SCORE, score);
        return mSQLiteDatabase.insert(HistoryConstant.TABLE_NAME,null, values);
    }
    //更新信息
    public boolean updateUserData(int id,int score) {
        ContentValues values = new ContentValues();
        values.put(HistoryConstant.SCORE, score);
        //return mSQLiteDatabase.update(UsersConstant.TABLE_NAME, values,null, null) > 0;
        return mSQLiteDatabase.update(HistoryConstant.TABLE_NAME, values, HistoryConstant._ID + "=" + id, null) > 0;
    }

    //获取数据总数
    public int countData() {
        int Count = 0;
        Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " + HistoryConstant.TABLE_NAME, null);
        Count=cursor.getCount();
        cursor.close();
        return Count;

    }

    //根据id删除
    public boolean deleteHistoryData(String id) {
        return mSQLiteDatabase.delete(HistoryConstant.TABLE_NAME, HistoryConstant._ID + "=" + id, null) > 0;
    }
    //获取某用户的所有历史记录
    public Cursor fetchHistoryData(String  pid) throws SQLException {
        Cursor mCursor = mSQLiteDatabase.query(false, HistoryConstant.TABLE_NAME, null, HistoryConstant.PID
                + "=" + pid, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    //根据id获取历史记录
    public Cursor fetchHistoryDataByID(String hid) {
        Cursor mCursor = mSQLiteDatabase.query(false, HistoryConstant.TABLE_NAME, null, HistoryConstant._ID
                + "=" + Integer.valueOf(hid).intValue(), null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    //
    public Cursor fetchAllHistoryDatas() {
        return mSQLiteDatabase.rawQuery("select * from "+ HistoryConstant.TABLE_NAME,null);
    }

    //
    public String getStringByColumnName(String columnName, String id) {
        Cursor mCursor = fetchHistoryData(id);
        int columnIndex = mCursor.getColumnIndex(columnName);
        String columnValue = mCursor.getString(columnIndex);
        mCursor.close();
        return columnValue;
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
