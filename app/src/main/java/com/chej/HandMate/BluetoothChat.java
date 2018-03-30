package com.chej.HandMate;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chej.HandMate.Transmission.Bluetooth.BluetoothService;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothChat extends Activity {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private TextView mTitle;
    private EditText text_chat;
    private EditText text_input;
    private Button but_On_Off;
    private Button but_search; // ------> 在菜单中可以搜索
    private Button but_create; // ------> 在菜单中设置"可被发现"
    private Button mSendButton;
    // 连接到的蓝牙设备的名称
    private String mConnectedDeviceName;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mChatService = null;

    private ArrayList<String> mPairedDevicesList = new ArrayList<String>();
    private ArrayList<String> mNewDevicesList = new ArrayList<String>();
    private String[] strName;
    private String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_bluetooth_chat);

        mTitle = (TextView) this.findViewById(R.id.text_title);
        text_chat = (EditText) this.findViewById(R.id.text_chat);
        text_input = (EditText) this.findViewById(R.id.text_input);
        but_On_Off = (Button) this.findViewById(R.id.but_off_on);
        but_search = (Button) this.findViewById(R.id.but_search_div);
        but_create = (Button) this.findViewById(R.id.but_cjlj);
        mSendButton = (Button) this.findViewById(R.id.but_fsxx);

        // 获得本地的蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 如果为null,说明没有蓝牙设备
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "没有蓝牙设备", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (mBluetoothAdapter.isEnabled()) {
            but_On_Off.setText("关闭蓝牙");
        } else {
            but_On_Off.setText("开启蓝牙");
        }

        but_On_Off.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                    Toast.makeText(BluetoothChat.this, "蓝牙已开启",
                            Toast.LENGTH_SHORT).show();
                    but_On_Off.setText("关闭蓝牙");
                } else {
                    mBluetoothAdapter.disable();
                    Toast.makeText(BluetoothChat.this, "蓝牙已关闭",
                            Toast.LENGTH_SHORT).show();
                    but_On_Off.setText("开启蓝牙");
                }
            }
        });

        but_search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                searchDevice();
            }
        });

        but_create.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                final EditText et = new EditText(BluetoothChat.this);
                et.setSingleLine();
                et.setText(mBluetoothAdapter.getName());

                new AlertDialog.Builder(BluetoothChat.this)
                        .setTitle("请输入房间名：")
                        .setView(et)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        String name = et.getText().toString()
                                                .trim();
                                        if (name.equals("")) {
                                            Toast.makeText(BluetoothChat.this,
                                                    "请输入房间名",
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        // 设置房间名
                                        mBluetoothAdapter.setName(name);
                                    }

                                })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                    }
                                }).create().show();

                // 创建连接，也就是设备本地蓝牙设备可被其他用户的蓝牙搜到
                ensureDiscoverable();
            }
        });

        // 获得一个已经配对的蓝牙设备的set集合
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesList.add("已配对：" + device.getName() + "\n"
                        + device.getAddress());
            }
        } else {
            Toast.makeText(this, "没有已配对的设备", Toast.LENGTH_SHORT).show();
        }

        // 当发现一个新的蓝牙设备时注册广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // 当搜索完毕后注册广播
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null)
                setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity
        // returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {

        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = text_input.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null)
            mChatService.stop();

        // Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /** 使本地的蓝牙设备可被发现 */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * 发送消息
     *
     * @param message
     *            A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, "没有连接上", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            text_input.setText(mOutStringBuffer);
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            mTitle.setText("已经连接");
                            mTitle.append(mConnectedDeviceName);
                            // mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            mTitle.setText("正在连接中...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            mTitle.setText("未连接上");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    // mConversationArrayAdapter.add("Me:  " + writeMessage);
                    text_chat.append("我：" + writeMessage + "\n");
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    // mConversationArrayAdapter.add(mConnectedDeviceName+":  " +
                    // readMessage);
                    text_chat.append(mConnectedDeviceName + "：" + readMessage
                            + "\n");
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "连接到 " + mConnectedDeviceName, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };

    // 连接蓝牙设备
    private void linkDevice() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        int cou = mPairedDevicesList.size() + mNewDevicesList.size();
        if (cou == 0) {
            Toast.makeText(BluetoothChat.this, "没有搜索到可用的蓝牙设备",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 把已经配对的蓝牙设备和新发现的蓝牙设备的名称都放入数组中，以便在对话框列表中显示
        strName = new String[cou];
        for (int i = 0; i < mPairedDevicesList.size(); i++) {
            strName[i] = mPairedDevicesList.get(i);
        }
        for (int i = mPairedDevicesList.size(); i < strName.length; i++) {
            strName[i] = mNewDevicesList.get(i - mPairedDevicesList.size());
        }
        address = strName[0].substring(strName[0].length() - 17);
        new AlertDialog.Builder(BluetoothChat.this)
                .setTitle("搜索到的蓝牙设备：")
                .setSingleChoiceItems(strName, 0,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // 当用户点击选中的蓝牙设备时，取出选中的蓝牙设备的MAC地址
                                address = strName[which].split("\\n")[1].trim();
                            }
                        })
                .setPositiveButton("连接", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (address == null) {
                            Toast.makeText(BluetoothChat.this, "请先连接外部蓝牙设备",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.i("sxd", "address:" + address);
                        // Get the BLuetoothDevice object
                        BluetoothDevice device = mBluetoothAdapter
                                .getRemoteDevice(address);
                        // Attempt to connect to the device
                        mChatService.connect(device);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }

    // 搜索蓝牙设备蓝牙设备
    private void searchDevice() {
        mTitle.setText("正在努力搜索中...");
        setProgressBarIndeterminateVisibility(true);
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mNewDevicesList.clear();
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "搜索设备");
        menu.add(0, 2, 0, "可被发现");
        return true;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 当发现一个新的蓝牙设备时
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed
                // already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String s = "未配对： " + device.getName() + "\n"
                            + device.getAddress();
                    if (!mNewDevicesList.contains(s))
                        mNewDevicesList.add(s);
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                if (mNewDevicesList.size() == 0) {
                    Toast.makeText(BluetoothChat.this, "没有发现新设备",
                            Toast.LENGTH_SHORT).show();
                }
                mTitle.setText("未连接");
                linkDevice();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                searchDevice();
                return true;
            case 2:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }

}