package com.ithouse.assist;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AssistServer {

	private ServerSocket mServerSocket;
	// 往客户端发送文件的入口
    public DataOutputStream mOutputStream;
    private TextTool mTextTool;
    private int mPort;
	
    public AssistServer(TextTool textTool, int port) {
    	this.mTextTool = textTool;
    	this.mPort = port;
    }
    
    /**
     * 开启服务
     */
    public void start() {
    	new ServerThread().start();
    }
	
	/**
	 * Socket服务器端
	 * @author 詹子聪
	 */
	 class ServerThread extends Thread {

	    @Override
	    public void run() {
	        try {
	            mServerSocket = new ServerSocket(mPort);
	            System.out.println("服务器已开启！");
	        } catch (IOException e) {
	            e.printStackTrace();
	            mTextTool.showTips("端口 " + mPort + " 被占用，请选择其他端口！");
	            return;
	        }

	        while (!Thread.currentThread().isInterrupted()) {
	            try {
	                // 一个客户端连接上了
	                final Socket client = mServerSocket.accept();
	                mOutputStream = new DataOutputStream(client.getOutputStream());
	                mTextTool.showTips("一个客户端上线了");
	                
	                CommunicationThread commThread = new CommunicationThread(client, mTextTool);
	                new Thread(commThread).start();

	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}
}


class CommunicationThread implements Runnable {
    private Socket clientSocket;
    private TextTool textTool;
    public CommunicationThread(Socket clientSocket, TextTool textTool) {
        this.clientSocket = clientSocket;
        this.textTool = textTool;
    }

    public void run() {
    	if (clientSocket ==null) {
    		return;
    	}
    	FileTools.receiveFileFromClient(clientSocket, textTool);
    }
}