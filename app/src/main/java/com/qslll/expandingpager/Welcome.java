package com.qslll.expandingpager;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qslll.expandingpager.ComService;
import com.qslll.expandingpager.Connection;
import com.qslll.expandingpager.Entity;
import com.qslll.expandingpager.ICallBack;
import com.qslll.expandingpager.IMyAidlInterface;
import com.qslll.expandingpager.MainActivity;
import com.qslll.expandingpager.R;
import com.qslll.expandingpager.model.SysApplication;

import java.text.BreakIterator;


public class Welcome extends Activity {
    private Button enterGame;
    private Button broadtest;
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
                getScreenSizeOfDevice();
                getDensity();
                getDisplayInfomation();

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


    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
        builder.setMessage("请检查本机或下位机网络状态");
        Toast.makeText(this, "请检查本机或下位机网络状态", Toast.LENGTH_SHORT).show();
        builder.setTitle("提示");
        builder.setPositiveButton("重新连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //serviceConnection.backgroundService.ServiceState();
                try {
                    iMyAidlInterface.runServiceState();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("暂不连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.create().show();
    }

    protected void dialogOne() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
        builder.setMessage("请检查本机网络连接");
        builder.setTitle("提示");
        builder.setPositiveButton("重新连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                try {
                    iMyAidlInterface.runServiceState();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("暂不连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.create().show();
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
                        dialog();
                    }else if (entity.getName().equals("CHECK_WIFI_STATUS")){
                        dialogOne();
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

}
