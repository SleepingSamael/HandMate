package com.chej.HandMate;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity
{
    private String TAG = "http";
    private EditText mNameText = null;
    private EditText mAgeText = null;

    private Button getButton = null;
    private Button postButton = null;

    private TextView mResult = null;

    // 基本地址：服务器ip地址：端口号/Web项目逻辑地址+目标页面（Servlet）的url-pattern
    private String baseURL = "https://www.pgyer.com/apiv2/app/install?appKey=263f2786f0a43f79698dce115062d02e&_api_key=70ec4c4428f1415bc4b4890a32a36835";//"http://www.blackiron.club/chejcenter/AppVersion2?m=getItemByUID&appType=Android";
    private String downURL ="";
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mNameText = (EditText) findViewById(R.id.name);
        mAgeText = (EditText) findViewById(R.id.age);
        mResult = (TextView) findViewById(R.id.result);

        getButton = (Button) findViewById(R.id.submit_get);
        getButton.setOnClickListener(mGetClickListener);
        postButton = (Button) findViewById(R.id.submit_post);
        postButton.setOnClickListener(mPostClickListener);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
    }

    private View.OnClickListener mGetClickListener = new View.OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            Log.e(TAG, "GET request");
            // 先获取用户名和年龄
            String name = mNameText.getText().toString();
            String age = mAgeText.getText().toString();

            // 使用GET方法发送请求,需要把参数加在URL后面，用？连接，参数之间用&分隔
            String url = baseURL ;
            Log.e(TAG, url);

            // 生成请求对象
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = new DefaultHttpClient();

            // 发送请求
            try
            {

                HttpResponse response = httpClient.execute(httpGet);

                // 显示响应
               // showResponseResult(response);// 一个私有方法，将响应结果显示出来

            }
            catch (Exception e)
            {
                Log.e(TAG+"err",e.toString());
            }
            down(v);
        }
    };

    private View.OnClickListener mPostClickListener = new View.OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            Log.i(TAG, "POST request");
            // 先获取用户名和年龄
            String name = mNameText.getText().toString();
            String age = mAgeText.getText().toString();

            NameValuePair pair1 = new BasicNameValuePair("username", name);
            NameValuePair pair2 = new BasicNameValuePair("age", age);

            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(pair1);
            pairList.add(pair2);

            try
            {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(
                        pairList);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(baseURL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse response = httpClient.execute(httpPost);
                // 显示响应
                showResponseResult(response);
            }
            catch (Exception e)
            {
                Log.e(TAG+"err",e.toString());
            }

        }
    };

    /**
     * 显示响应结果到命令行和TextView
     * @param response
     */
    private void showResponseResult(HttpResponse response)
    {
        if (null == response)
        {
            Log.e(TAG, "response=null");
            return;
        }

        HttpEntity httpEntity = response.getEntity();
        try
        {
            InputStream inputStream = httpEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputStream));
            String result = "";
            String line = "";
            while (null != (line = reader.readLine()))
            {
                result += line;

            }
            mResult.setText("Response Content from server: " + result);
            Log.e(TAG,"Response Content from server: " + result);
            JSONObject root = new JSONObject(result);
            Log.e("root",root.getString("Success"));//根据键名获取键值信息
            JSONObject Datad = root.getJSONObject("Data");
            Log.e(TAG,"------------------");
            Log.e(TAG,"AppType ="+Datad.getString("AppType"));
            Log.e(TAG,"Version  ="+Datad.getString("Version"));
            Log.e(TAG,"Version2  ="+Datad.getString("Version2"));
            Log.e(TAG,"Url  ="+Datad.getString("Url"));
            Log.e(TAG,"NeedUpdate  ="+Datad.getString("NeedUpdate"));
            downURL=Datad.getString("Url");
        }
        catch (Exception e)
        {
            Log.e(TAG+"err",e.toString());
        }

    }

    /**
     * 绑定button的事件
     * @param view
     */
    public void down(View view){
        Dialog dialog =new AlertDialog.Builder(this)
                .setTitle("版本更新")
                .setMessage("当前版本：1.2\r\n最新版本：1.3")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        download();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        dialog.show();
    }

    /**
     * 下载apk
     */
    public void download() {
        pd = new ProgressDialog(this);
        pd.setMessage("正在更新，请稍后。。。");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();
        //连接网络下载，新线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(baseURL);
                try {
                    HttpResponse response = httpClient.execute(httpGet);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        //判断sd卡是否安装
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            //下载apk存放位置
                            File file = new File(Environment.getExternalStorageDirectory(), "aaa.apk");//文件路径
                            FileOutputStream fos = new FileOutputStream(file);//创建文件的输出流
                            InputStream is = response.getEntity().getContent();//服务器返回的流
                            BufferedInputStream bis = new BufferedInputStream(is);

                            //apk总大小
                            int total =(int)response.getEntity().getContentLength();
                            pd.setMax(total);
                            //写入文件
                            byte[] buffer = new byte[1024];//一次读取1024字节
                            int len;
                            int pro = 0;
                            while ((len = bis.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                                pro += len;
                                pd.setProgress(pro);
                            }
                            fos.close();
                            bis.close();
                            is.close();
                            pd.dismiss();
                            //安装apk
                            installApk(file);

                        }
                    }

                } catch (IOException e) {
                    Log.e("httpdown",e.toString());
                }
            }
        }).start();
        /*Uri uri = Uri.parse("http://www.an12.com/download.php?app_id=1795&id=148532");
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);*/

    }

    private void installApk(File file) {
        Intent intent = new Intent();
        //执行显示的动作
        intent.setAction(Intent.ACTION_VIEW);
        //在新的任务栈中启动activity（添加这句话以后，会提示用户打开或者完成）
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //执行的数据类型MIME类型
        //apk的MIME类型为：application/vnd.android.package-archive
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);


    }
}


