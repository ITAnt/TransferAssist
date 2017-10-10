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
	 * 发送文件到客户端
	 * @param out
	 * @param file 要发送的文件
	 */
	public static void sendFileToClient(DataOutputStream out, File file, TextTool textTool) {
		if (file.length() <=0) {
			return;
		}
        try {
            // 把文件名告诉客户端
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
            textTool.showTips("文件发送完成。");
            // 发送结束了
        } catch (IOException e1) {
            e1.printStackTrace();
            textTool.showTips("文件发送失败。");
        }
	}
	
	/**
	 * 从客户端接收文件
	 * @param clientSocket
	 */
	public static void receiveFileFromClient(Socket clientSocket, TextTool textTool) {
		try {
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            // 不断地监听，看服务器是否有东西过来
            while (!Thread.currentThread().isInterrupted()) {
                String fileName = inputStream.readUTF();
                textTool.showTips("开始接收文件...");
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
                    textTool.showTips("成功接收到了一个文件：" + fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            textTool.showTips("文件接收失败");
        }
	}
}
