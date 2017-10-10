package com.pax.test.syncclient;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private StringBuilder mBuilder;
    private TextView tv_content;
    private EditText et_ip;
    private EditText et_port;
    private EditText et_file_name;

    private static final int MSG_CONNECT_FAIL = 0;
    private static final int MSG_CONNECT_SUC = 1;
    private static final int MSG_START_SENDING = 2;
    private static final int MSG_SENDING_FINISHED = 3;
    private static final int MSG_RECEIVE_FILE = 4;
    private static final int MSG_RECEIVE_FINISHED = 5;
    private Socket mClientSocket;
    private Handler mTipsHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT_FAIL:
                    mBuilder.append("连接服务器失败，正在重试...\r\n\r\n");
                    tv_content.setText(mBuilder.toString());
                    break;

                case MSG_CONNECT_SUC:
                    mBuilder.append("成功连接服务器\r\n\r\n");
                    tv_content.setText(mBuilder.toString());
                    break;

                case MSG_START_SENDING:
                    mBuilder.append("开始发送文件...\r\n\r\n");
                    tv_content.setText(mBuilder.toString());
                    break;
                case MSG_SENDING_FINISHED:
                    mBuilder.append("文件发送完毕\r\n\r\n");
                    tv_content.setText(mBuilder.toString());
                    break;

                case MSG_RECEIVE_FILE:
                    mBuilder.append("开始接收文件...\r\n\r\n");
                    tv_content.setText(mBuilder.toString());
                    break;
                case MSG_RECEIVE_FINISHED:
                    mBuilder.append("文件接收完毕\r\n\r\n");
                    tv_content.setText(mBuilder.toString());
                    break;
                default:
                    //Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_ip = (EditText) findViewById(R.id.et_ip);
        et_port = (EditText) findViewById(R.id.et_port);
        et_file_name = (EditText) findViewById(R.id.et_file_name);
        tv_content = (TextView) findViewById(R.id.tv_content);
        findViewById(R.id.btn_connect).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);

        mBuilder = new StringBuilder();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                final String ip = et_ip.getText().toString();
                String portString = et_port.getText().toString();
                if (TextUtils.isEmpty(ip)) {
                    Toast.makeText(this, "IP不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(portString)) {
                    Toast.makeText(this, "端口不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                final int port = Integer.parseInt(portString);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        connectTCPServer(ip, port);
                    }
                }).start();
                break;
            case R.id.btn_send:
                if (mClientSocket == null) {
                    Toast.makeText(this, "请先连接服务器", Toast.LENGTH_SHORT).show();
                    return;
                }

                String fileName = et_file_name.getText().toString();
                if (TextUtils.isEmpty(fileName)) {
                    Toast.makeText(this, "请输入要发送的文件名", Toast.LENGTH_SHORT).show();
                    return;
                }

                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
                final File file = new File(path, fileName);
                if (!file.exists()) {
                    Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
                    return;
                }

                mTipsHandler.sendEmptyMessage(MSG_START_SENDING);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendFileToServer(file);
                    }
                }).start();
                break;
        }
    }

    private void sendFileToServer(File file) {
        try {
            DataOutputStream mOutputStream = new DataOutputStream(mClientSocket.getOutputStream());
            // 把文件名告诉客户端
            mOutputStream.writeUTF(file.getName());
            mOutputStream.flush();

            // 把长度诉服务器，否则不知道一个文件结尾在哪。千万不要直接传str.length()！！！因为我们采用的
            // 是UTF-8编码，其长度是有所区别的，如果直接传str.length()很可能会导致传递的数据不完整！
            mOutputStream.writeLong(file.length());
            mOutputStream.flush();

            DataInputStream inputStream = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(file)));
            byte buf[] = new byte[1024];
            int len = -1;

            while ((len = inputStream.read(buf)) != -1) {
                mOutputStream.write(buf, 0, len);
            }
            mOutputStream.flush();
            inputStream.close();
            // 发送结束了
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        mTipsHandler.sendEmptyMessage(MSG_SENDING_FINISHED);
    }

    /**
     * 连接服务器，从服务器接收应用
     *
     * @param serverIP
     */
    private void connectTCPServer(String serverIP, int port) {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket(serverIP, port);
                mClientSocket = socket;
            } catch (IOException e) {
                SystemClock.sleep(1000);
                mTipsHandler.sendEmptyMessage(MSG_CONNECT_FAIL);
                System.out.println("connect tcp server failed, retry...");
            }
        }

        mTipsHandler.sendEmptyMessage(MSG_CONNECT_SUC);

        // 接收服务器端的消息和文件
        String destPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "apks" + File.separator;
        File destFileDir = new File(destPath);
        if (!destFileDir.exists()) {
            destFileDir.mkdirs();
        }

        try {
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            // 不断地监听，看服务器是否有东西过来
            while (!MainActivity.this.isFinishing()) {
                String fileName = inputStream.readUTF();

                mTipsHandler.sendEmptyMessage(MSG_RECEIVE_FILE);

                if (!TextUtils.isEmpty(fileName)) {
                    long fileSize = inputStream.readLong();
                    File destFile = new File(destPath + fileName);
                    if (destFile.exists()) {
                        destFile.delete();
                    }

                    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)));

                    byte[] buffer = new byte[(int)fileSize];
                    int nIdx = 0;
                    int nTotalLen = buffer.length;
                    int nReadLen = 0;

                    while (nIdx < nTotalLen) {
                        // 从nIdx开始，想读nTotalLen - nIdx那么多，实际上这次读了nReadLen
                        nReadLen = inputStream.read(buffer, nIdx, nTotalLen - nIdx);

                        if (nReadLen > 0) {
                            outputStream.write(buffer, nIdx, nReadLen);
                            nIdx = nIdx + nReadLen;
                        } else {
                            break;
                        }
                    }
                    outputStream.close();

                    // 接收完了一个应用，刷新列表
                    mTipsHandler.sendEmptyMessage(MSG_RECEIVE_FINISHED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("quit...");
        //socket.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClientSocket != null) {
            try {
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
