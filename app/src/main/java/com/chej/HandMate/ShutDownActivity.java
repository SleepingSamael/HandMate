package com.chej.HandMate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.chej.HandMate.TTS.SpeechUtil;
import com.chej.HandMate.Transmission.USB.UsbService;
import com.chej.HandMate.Transmission.Wifi.WifiService;

import java.util.Timer;
import java.util.TimerTask;

public class ShutDownActivity extends AppCompatActivity {

    private IMyAidlInterface iMyAidlInterface;
    private int sdid;
    private SpeechUtil speechUtil;

    //下位机关机调用
    public static void shutDownStart(Context context) {
        Intent intent = new Intent(context, ShutDownActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle sdbundle = new Bundle();//存重启、关机信息
        sdbundle.putInt("Mode", 0);
        intent.putExtras(sdbundle);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shut_down);

        Bundle bundle = this.getIntent().getExtras();
        sdid = bundle.getInt("Mode") ;

        Intent myServiceIntent = new Intent(ShutDownActivity.this, UsbService.class);
        bindService(myServiceIntent, serviceConnection,
                Context.BIND_AUTO_CREATE);

        speechUtil = new SpeechUtil(this);
        speechUtil.speak("正在关机，请稍候");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Looper.prepare();
                if (sdid==0)
                {
                    shutdown();
                }
                else if(sdid==1)
                {
                    reboot();
                }
                Looper.loop();
            }
        },3000);
    }

    private void shutdown()
    {
        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendShutdown();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        try{
            //关机
            Process proc =Runtime.getRuntime().exec(new

                    String[]{"su","-c","sync;sleep 1;reboot -p"});
            proc.waitFor();
        }catch(Exception
                e){
            e.printStackTrace();
        }
    }
    private  void reboot()
    {
        if (iMyAidlInterface!=null){
            try {
                iMyAidlInterface.sendShutdown();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        try
        {
            Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","sync;sleep 1;reboot "});//重启
            proc.waitFor();
        }catch (Exception ex){
            ex.printStackTrace();
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
