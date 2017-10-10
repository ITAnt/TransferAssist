package com.ithouse.assist;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FileTools {

	/**
	 * �����ļ����ͻ���
	 * @param out
	 * @param file Ҫ���͵��ļ�
	 */
	public static void sendFileToClient(DataOutputStream out, File file, TextTool textTool) {
		if (file.length() <=0) {
			return;
		}
        try {
            // ���ļ������߿ͻ���
            out.writeUTF(file.getName());
            out.flush();

            
            out.writeLong(file.length());
            out.flush();

            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            byte buf[] = new byte[1024];
            int len = -1;

            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            inputStream.close();
            textTool.showTips("�ļ�������ɡ�");
            // ���ͽ�����
        } catch (IOException e1) {
            e1.printStackTrace();
            textTool.showTips("�ļ�����ʧ�ܡ�");
        }
	}
	
	/**
	 * �ӿͻ��˽����ļ�
	 * @param clientSocket
	 */
	public static void receiveFileFromClient(Socket clientSocket, TextTool textTool) {
		try {
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            // ���ϵؼ��������������Ƿ��ж�������
            while (!Thread.currentThread().isInterrupted()) {
                String fileName = inputStream.readUTF();
                textTool.showTips("��ʼ�����ļ�...");
                if (fileName != null && !fileName.isEmpty()) {
                    long fileSize = inputStream.readLong();
                    File destFile = new File(fileName);
                    if (destFile.exists()) {
                        destFile.delete();
                    }

                    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)));

                    byte[] buffer = new byte[(int)fileSize];
                    int nIdx = 0;
                    int nTotalLen = buffer.length;
                    int nReadLen = 0;

                    while (nIdx < nTotalLen) {
                        // ��nIdx��ʼ�����nTotalLen - nIdx��ô�࣬ʵ������ζ���nReadLen
                        nReadLen = inputStream.read(buffer, nIdx, nTotalLen - nIdx);

                        if (nReadLen > 0) {
                            outputStream.write(buffer, nIdx, nReadLen);
                            nIdx = nIdx + nReadLen;
                        } else {
                            break;
                        }
                    }
                    outputStream.close();
                    // ��������һ��Ӧ�ã�ˢ���б�
                    textTool.showTips("�ɹ����յ���һ���ļ���" + fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            textTool.showTips("�ļ�����ʧ��");
        }
	}
}
