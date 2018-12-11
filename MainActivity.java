package com.wdn.qqloginweb;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int  GET_SERVER_RESOPENSE = 1;
    private static final int GET_ERROR             = 2;
    /**
     * 1.定义一个共享参数(存放数据方便的api)
     */
    private SharedPreferences sp;
    private EditText et_qqnumber;
    private EditText et_passwd;
    private CheckBox cb_remember;
    private Button bt_login;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SERVER_RESOPENSE:
                    Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case GET_ERROR:
                    Toast.makeText(MainActivity.this, "登录失败，服务器或者网络出错", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 2.通过上下文得到一个共享参数的实例对象
        sp = this.getSharedPreferences("config", this.MODE_PRIVATE);
        et_qqnumber = (EditText) findViewById(R.id.et_qqnumber);
        et_passwd = (EditText) findViewById(R.id.et_passwd);
        cb_remember = (CheckBox) findViewById(R.id.cb_remember);
        bt_login = (Button) findViewById(R.id.bt_login);
        restoreInfo();
    }
    /**
     * 从sp文件当中读取信息
     */
    private void restoreInfo() {
        String qq=sp.getString("qq","");
        String password=sp.getString("password","");
        et_qqnumber.setText(qq);
        et_passwd.setText(password);
    }
    /**
     * 登录按钮的点击事件
     *
     * @param view
     */
    public void login(View view) {
        final String qq = et_qqnumber.getText().toString().trim();
        final String password = et_passwd.getText().toString().trim();

        if (TextUtils.isEmpty(qq) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // 判断是否需要记录用户名和密码
            if (cb_remember.isChecked()) {
                // 被选中状态，需要记录用户名和密码
                // 3.将数据保存到sp文件中
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("qq", qq);
                editor.putString("password", password);
                editor.commit();// 提交数据，类似关闭流，事务
            }

            bt_login.setEnabled(false);
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                        String path = "http://192.168.1.6:8080/servlet/LoginServlet?username=" + qq + "&password=" + password;
                        URL url = new URL(path);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5000);
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            InputStream is = conn.getInputStream();
                            String result = StreamUtils.readStream(is);
                            Message msg = Message.obtain();
                            msg.what = GET_SERVER_RESOPENSE;
                            msg.obj = result;
                            handler.sendMessage(msg);
                        } else {
                            Message msg = Message.obtain();
                            msg.what = GET_ERROR;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message msg = Message.obtain();
                        msg.what = GET_ERROR;
                        handler.sendMessage(msg);
                    }
                }
            }.start();
        }

    }
}
