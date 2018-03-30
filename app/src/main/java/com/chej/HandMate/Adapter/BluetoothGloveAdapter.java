package com.chej.HandMate.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chej.HandMate.Model.BluetoothGloveItem;
import com.chej.HandMate.R;

import java.util.List;

/**
 * Created by samael on 2018/3/19.
 */
public class BluetoothGloveAdapter extends ArrayAdapter {
    private final int resourceId;

    public BluetoothGloveAdapter(Context context, int textViewResourceId, List<BluetoothGloveItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothGloveItem bluetoothGloveItem = (BluetoothGloveItem) getItem(position); // 获取当前项的BluetoothItem实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);//实例化一个对象
        ImageView bluetoothImage = (ImageView) view.findViewById(R.id.glove_status);//获取该布局内的图片视图
        TextView bluetoothName = (TextView) view.findViewById(R.id.glove_name);//获取该布局内的文本视图
        bluetoothImage.setImageResource(bluetoothGloveItem.getImageId());//为图片视图设置图片资源
        //添加连接成功动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        alphaAnimation.setRepeatCount(0);
        alphaAnimation.setFillAfter(true);
        if(bluetoothGloveItem.getImageId()==R.drawable.connected)
        {
            bluetoothImage.startAnimation(alphaAnimation);
        }
        bluetoothName.setText(bluetoothGloveItem.getName());//为文本视图设置文本内容
        return view;
    }
}