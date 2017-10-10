package com.ithouse.assist;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AssistServer {

	private ServerSocket mServerSocket;
	// ���ͻ��˷����ļ������
    public DataOutputStream mOutputStream;
    private TextTool mTextTool;
    private int mPort;
	
    public AssistServer(TextTool textTool, int port) {
    	this.mTextTool = textTool;
    	this.mPort = port;
    }
    
    /**
     * ��������
     */
    public void start() {
    	new ServerThread().start();
    }
	
	/**
	 * Socket��������
	 * @author ղ�Ӵ�
	 */
	 class ServerThread extends Thread {

	    @Override
	    public void run() {
	        try {
	            mServerSocket = new ServerSocket(mPort);
	            System.out.println("�������ѿ�����");
	        } catch (IOException e) {
	            e.printStackTrace();
	            mTextTool.showTips("�˿� " + mPort + " ��ռ�ã���ѡ�������˿ڣ�");
	            return;
	        }

	        while (!Thread.currentThread().isInterrupted()) {
	            try {
	                // һ���ͻ�����������
	                final Socket client = mServerSocket.accept();
	                mOutputStream = new DataOutputStream(client.getOutputStream());
	                mTextTool.showTips("һ���ͻ���������");
	                
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