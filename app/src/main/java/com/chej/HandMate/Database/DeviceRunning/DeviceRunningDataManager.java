package com.chej.HandMate.Database.DeviceRunning;

/**
 * Created by samael on 2017/11/30.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.chej.HandMate.Database.SQLdm;
import org.joda.time.DateTime;

public class DeviceRunningDataManager{
    private String TAG ="DeviceRunningDataManager";
    private Context mContext = null;
    private SQLiteDatabase mSQLiteDatabase = null;
    private DataBaseManagementHelper mDatabaseHelper = null;
    //DataBaseManagementHelper继承自SQLiteOpenHelper
    private class DataBaseManagementHelper extends SQLiteOpenHelper {
        DataBaseManagementHelper(Context context) {
            super(context, DeviceRunningConstant.DATABASE_NAME, null, DeviceRunningConstant.DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {

        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }
    public DeviceRunningDataManager(Context context) {
        mContext = context;
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
    public long inserDeviceRunningData(DeviceRunningData data) {
        ContentValues values = new ContentValues();
        values.put(DeviceRunningConstant.UID, data.getUID());
        values.put(DeviceRunningConstant.CreateDate, String.valueOf(DateTime.now()));
        values.put(DeviceRunningConstant.DeviceId, data.getDeviceId());
        values.put(DeviceRunningConstant.DeviceStatus, data.getDeviceStatus());
        values.put(DeviceRunningConstant.DeviceVersion, data.getDeviceVersion());

        return mSQLiteDatabase.insert(DeviceRunningConstant.TABLE_NAME,null, values);
    }
    //获取数据总数
    public int countData() {
        int Count = 0;
        Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " + DeviceRunningConstant.TABLE_NAME, null);
        Count=cursor.getCount();
        cursor.close();
        return Count;
    }
    //根据uid获取历史记录
    public Cursor fetchDeviceRunningDataByID(String uid) {
        Cursor mCursor = mSQLiteDatabase.query(false, DeviceRunningConstant.TABLE_NAME, null, DeviceRunningConstant.UID
                + "=" + uid, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    //
    public Cursor fetchAllDeviceRunningDatas() {
        return mSQLiteDatabase.rawQuery("select * from "+ DeviceRunningConstant.TABLE_NAME,null);
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