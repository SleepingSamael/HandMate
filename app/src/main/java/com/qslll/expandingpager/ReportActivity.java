package com.qslll.expandingpager;

import android.database.Cursor;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qslll.expandingpager.Database.HistoryDataManager;
import com.qslll.expandingpager.Model.SysApplication;
import com.qslll.expandingpager.Model.users.UserData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

public class ReportActivity extends AppCompatActivity {
    private Button back;
    private Button save;
    private TextView user_ID;
    private TextView user_Name;
    private TextView user_Age;
    private TextView user_Sex;
    private TextView user_Date;
    private TextView user_Diag;
    private TextView his_Item;
    private TextView his_Score;
    private TextView his_Time;
    private TextView his_Level;
    private TextView his_Dur;
    private HistoryDataManager mHistoryDataManager;
    Document doc=new Document();
    FileOutputStream fos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        SysApplication.getInstance().addActivity(this);

        back=(Button) findViewById(R.id.btn_back);
        save=(Button) findViewById(R.id.btn_save);
        user_ID=(TextView) findViewById(R.id.userID);
        user_Name=(TextView) findViewById(R.id.userName);
        user_Age=(TextView) findViewById(R.id.userAge);
        user_Sex=(TextView) findViewById(R.id.userSex);
        user_Date=(TextView) findViewById(R.id.userDate);
        user_Diag=(TextView) findViewById(R.id.userDiag);
        his_Time=(TextView) findViewById(R.id.historyTime);
        his_Item=(TextView) findViewById(R.id.historyItem);
        his_Score=(TextView) findViewById(R.id.historyScore);
        his_Level=(TextView) findViewById(R.id.historyLevel);
        his_Dur=(TextView) findViewById(R.id.historyDuration);

        //全局变量UserData中调取名字
        final UserData userData=(UserData)getApplication();
        user_ID.setText(userData.getUserId());
        user_Date.setText(userData.getUserDate());
        user_Name.setText(userData.getUserName());
        user_Sex.setText(userData.getUserSex());
        user_Age.setText(String.valueOf(userData.getUserAge()));
        user_Diag.setText(userData.getUserDiag());

        Bundle bundle = this.getIntent().getExtras();
        String hid = bundle.getString("ID");

        if (mHistoryDataManager == null) {
            mHistoryDataManager = new HistoryDataManager(this);
            mHistoryDataManager.openDataBase();            //建立本地数据库
        }
        //1打开数据库输出流
        final Cursor cursor = mHistoryDataManager.fetchHistoryDataByID(hid);
        his_Time.setText(cursor.getString(cursor.getColumnIndex("date"))+" "+cursor.getString(cursor.getColumnIndex("time")));
        his_Item.setText(cursor.getString(cursor.getColumnIndex("item")));
        his_Score.setText(cursor.getString(cursor.getColumnIndex("score")));


        //返回按钮
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /**
                 * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
                 */
                /*if (Build.VERSION.SDK_INT >= 23) {
                    int REQUEST_CODE_CONTACT = 101;
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    //验证是否许可权限
                    for (String str : permissions) {
                        if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                            //申请权限
                            this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                            return;
                        }
                    }
                }*/
                try {
                    fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/report.pdf"));
                    PdfWriter.getInstance(doc, fos);
                    doc.open ();
                    doc.setPageCount(1);
                    doc.add(new Paragraph("训练报告",setChineseFont()));
                    doc.add(new Paragraph(("\n个人信息\n病历号："+user_ID.getText()+"姓名："+user_Name.getText()
                    +"性别："+user_Sex.getText()+"年龄："+user_Age.getText()+"\n入院日期："+user_Date.getText()
                    +"诊断："+user_Diag.getText()), setChineseFont()));
                    doc.add(new Paragraph(("\n训练情况\n训练时间："+his_Time.getText()+"训练项目："+his_Item.getText()
                            +"得分："+his_Score.getText()+"\n成绩等级："+his_Level.getText()
                            +"用时："+his_Dur.getText()), setChineseFont()));
                    doc.add(new Paragraph(("\n图表分析"), setChineseFont()));
                    //一定要记得关闭document对象
                    doc.close();
                    fos.flush();
                    fos.close();
                    Toast.makeText(getApplicationContext(), "已保存",
                            Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (DocumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    // 产生PDF字体
    public static Font setChineseFont() {
        BaseFont bf = null;
        Font fontChinese = null;
        try {
            bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
            fontChinese = new Font(bf, 12, Font.NORMAL);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fontChinese;
    }


}
