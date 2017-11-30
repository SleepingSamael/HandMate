package com.chej.HandMate.Database.HandPosition;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.chej.HandMate.Database.SQLdm;

import org.joda.time.DateTime;

/**
 * Created by samael on 2017/11/30.
 */
public class HandPositionDataManager{
    private String TAG ="HandPositionDataManager";
    private Context mContext = null;
    private SQLiteDatabase mSQLiteDatabase = null;
    private DataBaseManagementHelper mDatabaseHelper = null;
    //DataBaseManagementHelper继承自SQLiteOpenHelper
    private class DataBaseManagementHelper extends SQLiteOpenHelper {
        DataBaseManagementHelper(Context context) {
            super(context, HandPositionConstant.DATABASE_NAME, null, HandPositionConstant.DATABASE_VERSION);
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
    public HandPositionDataManager(Context context) {
        mContext = context;
        Log.i(TAG, "UserDataManager construction!");
    }
    //打开数据库
    public void openDataBase() throws SQLException {
        final SQLdm sqLdm=new SQLdm();
        mSQLiteDatabase =sqLdm.openDatabase(mContext);
    }
    //关闭数据库
    public void closeDataBase() throws SQLException {
        mDatabaseHelper.close();
    }
    //添加新记录
    public long inserHandPositionData(HandPositionData data) {
        ContentValues values = new ContentValues();
        values.put(HandPositionConstant.UID, data.getUID());
        values.put(HandPositionConstant.CreateDate, String.valueOf(DateTime.now()));
        values.put(HandPositionConstant.UserId, data.getUserId());
        values.put(HandPositionConstant.HandType, data.getHandType());
        values.put(HandPositionConstant.Train, data.getTrain());
        values.put(HandPositionConstant.TrainTag, data.getTrainTag());
        values.put(HandPositionConstant.F1, data.getF1());
        values.put(HandPositionConstant.F2, data.getF2());
        values.put(HandPositionConstant.F3, data.getF3());
        values.put(HandPositionConstant.F4, data.getF4());
        values.put(HandPositionConstant.F5, data.getF5());
        values.put(HandPositionConstant.Bak, data.getBak());

        return mSQLiteDatabase.insert(HandPositionConstant.TABLE_NAME,null, values);
    }
    //获取数据总数
    public int countData() {
        int Count = 0;
        Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " + HandPositionConstant.TABLE_NAME, null);
        Count=cursor.getCount();
        cursor.close();
        return Count;
    }
    //根据uid获取历史记录
    public Cursor fetchHandPositionDataByID(String uid) {
        Cursor mCursor = mSQLiteDatabase.query(false, HandPositionConstant.TABLE_NAME, null, HandPositionConstant.UID
                + "=" + uid, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    //
    public Cursor fetchAllHandPositionDatas() {
        return mSQLiteDatabase.rawQuery("select * from "+ HandPositionConstant.TABLE_NAME,null);
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
}// ~