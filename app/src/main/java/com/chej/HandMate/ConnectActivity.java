package com.chej.HandMate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.chej.HandMate.Adapter.BluetoothGloveAdapter;
import com.chej.HandMate.Model.BluetoothGloveItem;

import java.util.ArrayList;
import java.util.List;

public class ConnectActivity extends AppCompatActivity{
    private static final String TAG = "BluetoothActivity";
    private List<BluetoothGloveItem> L_gloveList = new ArrayList<BluetoothGloveItem>();//蓝牙手套列表L
    private List<BluetoothGloveItem> R_gloveList = new ArrayList<BluetoothGloveItem>();//蓝牙手套列表R

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //初始化手套数据
        initGloveL();
        initGloveR();
        BluetoothGloveAdapter adapterL = new BluetoothGloveAdapter(ConnectActivity.this, R.layout.glove_item, L_gloveList);
        ListView dGlove_lv= (ListView) findViewById(R.id.diseased_glove_item_list);
        dGlove_lv.setAdapter(adapterL);

        BluetoothGloveAdapter adapterR = new BluetoothGloveAdapter(ConnectActivity.this, R.layout.glove_item, R_gloveList);
        ListView hGlove_lv= (ListView) findViewById(R.id.healthy_glove_item_list);
        hGlove_lv.setAdapter(adapterR);
    }

    private void initGloveL() {
        BluetoothGloveItem glove1 = new BluetoothGloveItem ("L-glove1", R.drawable.connected);
        L_gloveList.add(glove1);
        BluetoothGloveItem  glove2 = new BluetoothGloveItem ("L-glove2", R.drawable.connecting_animation);
        L_gloveList.add(glove2);
        BluetoothGloveItem  glove3 = new BluetoothGloveItem ("L-glove3", R.drawable.unconnected);
        L_gloveList.add(glove3);
        BluetoothGloveItem  glove4 = new BluetoothGloveItem ("L-glove4", R.drawable.unconnected);
        L_gloveList.add(glove4);
        BluetoothGloveItem  glove5 = new BluetoothGloveItem ("L-glove12", R.drawable.unconnected);
        L_gloveList.add(glove5);
    }
    private void initGloveR() {
        BluetoothGloveItem glove1 = new BluetoothGloveItem ("R-glove1", R.drawable.connected);
        R_gloveList.add(glove1);
        BluetoothGloveItem  glove2 = new BluetoothGloveItem ("R-glove2", R.drawable.connecting_animation);
        R_gloveList.add(glove2);
        BluetoothGloveItem  glove3 = new BluetoothGloveItem ("R-glove3", R.drawable.unconnected);
        R_gloveList.add(glove3);
        BluetoothGloveItem  glove4 = new BluetoothGloveItem ("R-glove4", R.drawable.unconnected);
        R_gloveList.add(glove4);
        BluetoothGloveItem  glove5 = new BluetoothGloveItem ("R-glove12", R.drawable.unconnected);
        R_gloveList.add(glove5);
    }
}

