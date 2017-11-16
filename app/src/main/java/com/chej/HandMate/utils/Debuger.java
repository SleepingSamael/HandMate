package com.chej.HandMate.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.chej.HandMate.MainActivity;
import com.chej.HandMate.Model.MyCustomDialog;
import com.chej.HandMate.Model.users.UserData;
import com.chej.HandMate.Welcome;

/**
 * Created by chenx on 2017/11/2.
 */

public class Debuger {
    static String _errorTitle;
    static String _errorMessage;
    public static void dialogError(String errorTitle,String errorMessage) {

        try{
            MyCustomDialog.Builder builder = new MyCustomDialog.Builder(UserData.getContext());
            builder.setTitle(errorTitle);
            builder.setMessage(errorMessage);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            Dialog dialog=builder.create();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            //builder.create().show();
            dialog.show();
        }catch (Exception ex0){
            try{
                _errorTitle= errorTitle;
                _errorMessage=errorMessage;
                Welcome.activity.runOnUiThread( new Runnable(){

                    @Override
                    public void run() {
                        Toast.makeText(Welcome.activity, _errorTitle+" "+_errorMessage,Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (Exception ex2){
                Log.e("Debuger","Can not show");
            }

        }

    }
}
