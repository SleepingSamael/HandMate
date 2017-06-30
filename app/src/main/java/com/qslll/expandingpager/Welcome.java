package com.qslll.expandingpager;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qslll.expandingpager.Model.SysApplication;
import com.qslll.expandingpager.Transmission.ComService;


public class Welcome extends Activity {
    private Button enterGame;
    private TextView size;


    private String tag = "Welcome";
    private int buttonCounter =0;

    private IMyAidlInterface iMyAidlInterface;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        size =(TextView) findViewById(R.id.sizeTV) ;

        SysApplication.getInstance().addActivity(this);//统一关闭用


        //权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            }
        }
        try {
            Intent startIntent = new Intent(Welcome.this, ComService.class);
            startService(startIntent); // 启动服务

            Intent intent = new Intent(Welcome.this, ComService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }catch(Exception e){
            e.printStackTrace();
        }


        enterGame = (Button) findViewById(R.id.enter_game);
        enterGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mintent = new Intent(Welcome.this, UsersActivity.class);
                startActivity(mintent);
                finish();

            }
        });

        size.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                size.setText("");
             /*   getScreenSizeOfDevice();
                getDensity();
                getDisplayInfomation();*/

            }
        });
    }

//系统向service发消息

    /*    button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iMyAidlInterface!=null){
                    try {
                        iMyAidlInterface.send2Service(new Entity("I am from Welcome",0));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                }
            }
        }
    }



    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(iBinder);
            try {
                iMyAidlInterface.registerCallback(iCallBack);
                Intent mintent = new Intent(Welcome.this, UsersActivity.class);
                startActivity(mintent);
                finish();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private ICallBack.Stub iCallBack = new ICallBack.Stub/*回调函数注册*/() {
        @Override
        public void callBack(final Entity entity) throws RemoteException {//注册需要service发送信息的activity
            Log.e("Welcome","Welcome receive the entity "+entity.getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(entity.getName().equals("CHECK_DEVICE_CONNECTION")){

                    }else if (entity.getName().equals("CHECK_WIFI_STATUS")){

                    }
                }
            });

        }
    };



    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

/*


    private void getDisplayInfomation() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        size.append("the screen size is "+point.toString()+"\n");
        getWindowManager().getDefaultDisplay().getRealSize(point);
        size.append("the screen real size is "+point.toString()+"\n");
    }
    private void getDensity() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        size.append("Density is "+displayMetrics.density+" densityDpi is "+displayMetrics.densityDpi+" height: "+displayMetrics.heightPixels+
                " width: "+displayMetrics.widthPixels+"\n");
    }
    private void getScreenSizeOfDevice() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        double x = Math.pow(width,2);
        double y = Math.pow(height,2);
        double diagonal = Math.sqrt(x+y);

        int dens=dm.densityDpi;
        double screenInches = diagonal/(double)dens;
        size.append("The screenInches "+screenInches+"\n");
    }
*/

}
