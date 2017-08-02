/**
 * U3D界面
 */
package com.qslll.expandingpager.U3D;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.qslll.expandingpager.Transmission.ComService;
import com.qslll.expandingpager.Database.HistoryDataManager;
import com.qslll.expandingpager.Entity;
import com.qslll.expandingpager.ICallBack;
import com.qslll.expandingpager.IMyAidlInterface;
import com.qslll.expandingpager.R;
import com.qslll.expandingpager.Model.history.HistoryData;
import com.unity3d.player.UnityPlayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.LinearLayout;


import java.util.Timer;
import java.util.TimerTask;

//建立U3D连接
public class u3dPlayer extends UnityPlayerActivity {
    private String tag = "";
    private LinearLayout u3dLayout;
    private float angle = 0;
    private int scenenum = 1;//场景变量
    private int hid = 0;//历史id
    private int score = 0;
    private String[] upangles;//手套操发送的角度
    //private ServiceToConnection serviceConnection;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private IMyAidlInterface iMyAidlInterface;

    private float angleFromUnity = 0;
    private int fingerNumber=0;

    public float[] fingerArrayFloat= new float[5];

    private HistoryDataManager mhistoryDataManager;
    HistoryData historyData=new HistoryData();
/*
    private ComService backgroundService;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            backgroundService = ((ComService.DownloadBinder) service).getService();
            Log.e("MainActivity","I'm connected to Service");
        }
    };
*/
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_u3d_player);

        if (mhistoryDataManager == null) {
            mhistoryDataManager = new HistoryDataManager(this);
            mhistoryDataManager.openDataBase();            //建立本地数据库
        }

        //根据menu点击值改变场景值
        Bundle bundle = this.getIntent().getExtras();
        hid = bundle.getInt("ID");
        scenenum = bundle.getInt("Mode") ;


        try {
            Intent bindIntent = new Intent(u3dPlayer.this, ComService.class);
            bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
        }catch(Exception e){

        }

        IntentFilter filter = new IntentFilter("com.example.updateUI");
        this.registerReceiver(new u3dPlayer.MyBroadCaseReceiver(), filter);

        u3dLayout = (LinearLayout) findViewById(R.id.u3d_layout);
        u3dLayout.addView(mUnityPlayer);
        mUnityPlayer.requestFocus();


        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }



    class MyBroadCaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String i = intent.getStringExtra("currentEvent");
            if(i == "CloseAcitivity"){
                Log.d("MainActivity", "I'm closing the activity");
                makePauseUnity();
            }
        }
    }


    public String getName() {

        return "This is the message that Unity call Android";
    }
    public int getScore(){
        return score;
    }


    //u3d调取舵机状态
    public String[] getComponentStatus(){
        if (iMyAidlInterface!=null){
            try {
                return iMyAidlInterface.getComponentStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    //u3d调取手指角度
    public String[]  getFingerArray(){

        if (iMyAidlInterface!=null){
            try {
                return iMyAidlInterface.getFingerArray();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
    //u3d调取配置数据
    public String[]  getConfigArray(){

        Log.e("getConfigArray","in");
        if (iMyAidlInterface!=null){
            try {
                return iMyAidlInterface.getConfigArray();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    //向下位机传递(U3D调用)
   public void sendToLowerComputer(String angles)//U3d传来角度数据,转换成报文传给下位机
   {
       Log.e("u3dPlayer","I'm receving from Unity "+"   "+ angles);
       if (iMyAidlInterface!=null){
           try {
             iMyAidlInterface.setCurrentAngle(angles);
           } catch (RemoteException e) {
               e.printStackTrace();
           }
       }
   }

   //触觉反馈（U3D调用）
   public void ToLowerTouchFeedBack(float[] touchFeedBack){

       for (int j=0;j<touchFeedBack.length;j++)
       {
           Log.e("u3dPlayer", "I'm receving from Unity " + "   " + touchFeedBack[j]);
       }
   }

    public void sendDataToAndroid(String input) {
        Log.d(tag,input);
    }


    //返回游戏分数
    public void sendToAndroidGameResult(int result){

        Log.e("u3dPlayer","result: "+"   "+ result);
        mhistoryDataManager.updateUserData(hid,result);//游戏分数存入数据库
        score=result;

    }
    public void sendToAndroidEvaluateResult( float[] result) {

        for (float s : result)
        {
            Log.e("u3dPlayer", "I'm receving from Unity " + "   " + s);
        }

    }

    public void getmsg() {

        Log.d(tag,"I got the trigger!");


    }
    public int getSceneNum() {
        return scenenum;
    }

    //向下位机发送结束信号
    public void sendTrainAckoff(int mode) {

        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendTrainAck(mode,0);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e("sendTranAck", String.valueOf(e));
            }
        }

    }
    //暂停
    public  static void  pauseUnity(){
        try {
            UnityPlayer.UnitySendMessage("ALL", "PressStop", "");
        }catch(Exception e) {
            Log.e("pauseUnity", String.valueOf(e));
        }
    }
    //退出unity界面(外部调用)
    public static void stopUnity(){
        u3dPlayer u3dPlayerNow=new u3dPlayer();
        u3dPlayerNow.makePauseUnity();
    }
    //退出unity界面
    public void makePauseUnity() {
        switch (scenenum/1000){
            case 1:sendTrainAckoff(1);//主从
                break;
            case 2:sendTrainAckoff(2);//手套操
                break;
            case 3:sendTrainAckoff(3);//评估
                break;
            case 4:sendTrainAckoff(4);//游戏
                break;
        }
        unbindService(serviceConnection);
        finish();
       /* runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mUnityPlayer != null) {

                    try {
                        unbindService(serviceConnection);
                        //serviceConnection.backgroundService.connection.angleFromDownStream=0;

                        mUnityPlayer.quit();
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.e("u3dPlayer","close msocket error");
                    }
                    Log.e(tag, "--->>取消server.");

                }

                //MainActivity.this.finish();
                Intent intent = new Intent(u3dPlayer.this, Welcome.class);
                startActivity(intent);
            }
        });*/

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onDestroy();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnityPlayer.quit();
        finish();
    }


    // Pause Unity
    @Override
    protected void onPause() {
        super.onPause();
        mUnityPlayer.pause();

    }

    // Resume Unity
    @Override
    protected void onResume() {
        super.onResume();
        mUnityPlayer.resume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // mUnityPlayer.quit();
        // this.finish();

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }



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
