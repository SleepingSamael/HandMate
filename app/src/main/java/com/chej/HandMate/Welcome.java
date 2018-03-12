package com.chej.HandMate;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chej.HandMate.Model.SysApplication;
import com.chej.HandMate.Transmission.USB.USBHelper;
import com.chej.HandMate.Transmission.USB.UsbService;
import com.chej.HandMate.utils.Debuger;
import com.chej.HandMate.utils.DeviceID;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class Welcome extends Activity {

    /*
   * Notifications from UsbService will be received here.
   */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_READY:

                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    Intent mintent = new Intent(Welcome.this, UsersActivity.class);
                    startActivity(mintent);
                    finish();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    Timer timer2;
    TimerTask timer2task;
    private Button enterGame;
    private TextView size;
    private UsbService usbService;
    private MyHandler mHandler;
    public  static Welcome activity;
    private String tag = "Welcome";

    private IMyAidlInterface iMyAidlInterface;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        size =(TextView) findViewById(R.id.sizeTV) ;

        mHandler = new MyHandler(this);
        SysApplication.getInstance().addActivity(this);//统一关闭用

        String CPU = DeviceID.getCPUSerial();
        Debuger.dialogError("CPUSerial",CPU);

        //权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            }
        }

        activity = this;
       /* try {
            Intent startIntent = new Intent(Welcome.this, UsbService.class);
            startService(startIntent); // 启动服务

            Intent intent = new Intent(Welcome.this, UsbService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }catch(Exception e){
            e.printStackTrace();
        }*/


        enterGame = (Button) findViewById(R.id.enter_game);
        enterGame.setOnClickListener(new Button.OnClickListener(){//创建监听
            public void onClick(View v) {
                Intent mintent = new Intent(Welcome.this, UsersActivity.class);
                startActivity(mintent);
            }
        });

        setFilters();  // Start listening notifications from UsbService

        startService(UsbService.class, serviceConnection, null);
    }
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }



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
            } catch (RemoteException e) {
                Log.e(tag,e.toString());
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
    public void onResume() {
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }
    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<Welcome> mActivity;

        public MyHandler(Welcome activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:

                    String data = (String) msg.obj;
                    mActivity.get().size.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
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
