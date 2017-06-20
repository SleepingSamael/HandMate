package com.qslll.expandingpager;

/**
 * Created by samael on 2017/6/15.
 * app自启动用广播
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 该类派生自BroadcastReceiver，覆载方法onReceive中
 * 检测接收到的Intent是否符合BOOT_COMPLETED，如果符合，则启动Activity。
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            Intent mainActivityIntent = new Intent(context, Welcome.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }
}
