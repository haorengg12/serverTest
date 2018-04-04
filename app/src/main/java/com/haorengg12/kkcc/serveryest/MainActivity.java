package com.haorengg12.kkcc.serveryest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Android Tcp即时通讯客户端
 */
public class MainActivity extends Activity implements Runnable {
    private static final String TAG = "MainActivity";
    private TextView tv_msg = null;
    private EditText ed_msg = null;
    private Button btn_send = null;
    //想办法设置服务器地址
    private static final String HOST = "10.128.5.200";//服务器地址
    private static final int PORT = 8888;//连接端口号
    private Socket socket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String msg;

    //接收线程发送过来信息，并用TextView追加显示
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tv_msg.append((CharSequence) msg.obj);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_msg = (TextView) findViewById(R.id.txt_1);
        ed_msg = (EditText) findViewById(R.id.et_talk);
        btn_send = (Button) findViewById(R.id.btn_send);

        btn_send.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                msg = ed_msg.getText().toString();
                if (socket.isConnected()) {//如果服务器连接
                    Log.d(TAG, "onClick: 服务器连接");
                    if (!socket.isOutputShutdown()) {//如果输出流没有断开
                        Log.d(TAG, "onClick: 输出流没断开");
                        Log.d(TAG, "onClick: " + msg);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                out.println(msg);//点击按钮发送消息(一个APP如果在主线程中请求网络操作，将会
                                // 抛出此异常。Android这个设计是为了防止网络请求时间过长而导致界面假死的情况发生,
                                // 所以我把它放在子线程)
                            }
                        }).start();
                        ed_msg.setText("");//清空编辑框
                    }
                }
            }
        });
        //启动线程，连接服务器，并用死循环守候，接收服务器发送过来的数据
        new Thread(this).start();
    }

    /**
     * 连接服务器
     */
    private void connection() {
        try {
            socket = new Socket(HOST, PORT);//连接服务器
            in = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));//接收消息的流对象
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);//发送消息的流对象
        } catch (IOException ex) {
            ex.printStackTrace();
//            ShowDialog("连接服务器失败：" + ex.getMessage());
            Toast.makeText(this, "无连接", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 如果连接出现异常，弹出AlertDialog！
     */
//    public void ShowDialog(String msg) {
//        new AlertDialog.Builder(this).setTitle("通知").setMessage(msg)
//                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                }).show();
//    }

    /**
     * 读取服务器发来的信息，并通过Handler发给UI线程
     */
    public void run() {
        connection();// 连接到服务器
        try {
            while (true) {//死循环守护，监控服务器发来的消息
                if (!socket.isClosed()) {//如果服务器没有关闭
                    if (socket.isConnected()) {//连接正常
                        if (!socket.isInputShutdown()) {//如果输入流没有断开
                            String getLine;
                            if ((getLine = in.readLine()) != null) {//读取接收的信息
                                getLine += "\n";
                                Message message = new Message();
                                message.obj = getLine;
                                mHandler.sendMessage(message);//通知UI更新
                            } else {
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}