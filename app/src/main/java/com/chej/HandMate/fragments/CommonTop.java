package com.chej.HandMate.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chej.HandMate.ConnectActivity;
import com.chej.HandMate.Database.users.UserData;
import com.chej.HandMate.R;
import com.chej.HandMate.SystemSetActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by samael on 2018/3/13.
 */

public class CommonTop implements View.OnClickListener {
    Context mContext;
    OnCommonBottomClick listener;
    TextView clock;
    ImageView volume;
    ImageView m_hand;
    ImageView s_hand;
    private static final int msgKey1 = 1;
    //音量状态
    AudioManager mAudioManager;
    // 当前音量
    int currentVolume;

    public CommonTop(Context context) {
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        if (listener == null) return;
        switch (v.getId()) {
            case R.id.volume:
               // listener.onVolumeClick(v);
                Intent i;
                i = new Intent(UserData.getContext(),SystemSetActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);//打开新acticity时关闭所有其他acticity
                UserData.getContext().startActivity(i);
                break;
            case R.id.s_hand:
                Intent i2;
                i2 = new Intent(UserData.getContext(), ConnectActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);//打开新acticity时关闭所有其他acticity
                UserData.getContext().startActivity(i2);
                break;
        }
    }


    public interface OnCommonBottomClick {
        //public void onVolumeClick(View v);

        //public void onBluetoothClick();

    }

    public void setListener(OnCommonBottomClick listener) {
        this.listener = listener;
    }

    public CommonTop init() {
        volume = (ImageView) ((Activity) mContext).findViewById(R.id.volume);
        s_hand = (ImageView) ((Activity) mContext).findViewById(R.id.s_hand);
        m_hand = (ImageView) ((Activity) mContext).findViewById(R.id.m_hand);
        clock = (TextView) ((Activity) mContext).findViewById(R.id.clock);
        volume.setOnClickListener(this);
        s_hand.setOnClickListener(this);

        //获取系统时间
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");
        String sysDate = sDateFormat.format(new java.util.Date());
        String[] arr = sysDate.split("\\s+");
        final String sdate = arr[0];
        final String stime = arr[1];
        clock.setText(sdate + "   " + stime);
        new TimeThread().start();

        // 获取到当前 设备的音量
        mAudioManager=(AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 显示音量
        if(currentVolume==0)
        {
            volume.setImageResource(R.drawable.volume0);
        }else if(currentVolume > 0 && currentVolume <= 7)
        {
            volume.setImageResource(R.drawable.volume2);
        }else if(currentVolume >7 && currentVolume <15)
        {
            volume.setImageResource(R.drawable.volume1);
        }
        else if(currentVolume == 15)
        {
            volume.setImageResource(R.drawable.volume);
        }

        return this;
    }

    //动态更新时间的线程
    public class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = msgKey1;
                    mHandler.sendMessage(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgKey1:
                    long time = System.currentTimeMillis();
                    Date date = new Date(time);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");
                    clock.setText(format.format(date));
                    break;
                default:
                    break;
            }
        }
    };


}
