package com.ithouse.assist;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MainActivity implements ActionListener {
	private AssistServer mServer;

	private JButton btn_start_transfer;
	private JLabel label_file_name;
	private JButton btn_choose_file;
	private JButton btn_start_service;
	private JTextField tf_port;
	//private JTextField tf_ip;
	private JTextArea ta_tips;
	private StringBuilder mStringBuilder;
	private TextTool mTextTool;
	
	// 选中的要发往客户端的文件
	private File mSelectedFile;
	// 端口号
	private int mPort;

	public MainActivity() {
		mStringBuilder = new StringBuilder();
		
		// 创建 JFrame 实例
        JFrame frame = new JFrame("传输助手-PC");
        // Setting the width and height of frame
        frame.setSize(640, 320);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int x = (int)(toolkit.getScreenSize().getWidth()-frame.getWidth())/2;
        int y = (int)(toolkit.getScreenSize().getHeight()-frame.getHeight())/2;
        frame.setLocation(x, y);

        /* 创建面板，这个类似于 HTML 的 div 标签
         * 我们可以创建多个面板并在 JFrame 中指定位置
         * 面板中我们可以添加文本字段，按钮及其他组件。
         */
        JPanel panel = new JPanel();
        // 调用用户定义的方法并添加组件到面板
        initViews(panel);

        // 添加面板
        frame.add(panel);
        // 设置界面可见
        frame.setVisible(true);
        
        mTextTool = new TextTool(ta_tips, mStringBuilder);
	}

	private void initViews(JPanel panel) {
        /* 布局部分我们这边不多做介绍
         * 这边设置布局为 null
         */
        panel.setLayout(null);

        // IP地址
        //JLabel label_ip = new JLabel("IP地址:");
        /* 这个方法定义了组件的位置。
         * setBounds(x, y, width, height)
         * x 和 y 指定左上角的新位置，由 width 和 height 指定新的大小。
         */
        //label_ip.setBounds(10,20,80,25);
        //panel.add(label_ip);

        //tf_ip = new JTextField(20);
        //tf_ip.setBounds(100,20,170,25);
        //panel.add(tf_ip);

        // 端口
        JLabel label_port = new JLabel("端口:");
        label_port.setBounds(10,50,40,25);
        panel.add(label_port);

        tf_port = new JTextField(5);
        tf_port.setBounds(60,50,70,25);
        panel.add(tf_port);

        btn_start_service = new JButton("开启服务");
        btn_start_service.setBounds(10, 90, 120, 25);
        btn_start_service.addActionListener(this);
        panel.add(btn_start_service);
        
        
        btn_choose_file = new JButton("选择文件");
        btn_choose_file.setBounds(10, 130, 120, 25);
        btn_choose_file.addActionListener(this);
        panel.add(btn_choose_file);
        
        label_file_name = new JLabel("未选择");
        label_file_name.setBounds(150,130,130,25);
        panel.add(label_file_name);
        
        btn_start_transfer = new JButton("开始发送");
        btn_start_transfer.setBounds(10, 170, 120, 25);
        btn_start_transfer.addActionListener(this);
        panel.add(btn_start_transfer);
        
        // 文字提示区
        ta_tips = new JTextArea();
        ta_tips.setLineWrap(true);
        // 滚动条
        JScrollPane scrollPane = new JScrollPane(ta_tips);
        scrollPane.setBounds(330, 10, 250, 250);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
        panel.add(scrollPane);
    }

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == btn_start_service) {
			// 开启服务
			if (mServer == null) {
				String portString = tf_port.getText();
				try {
					mPort = Integer.parseInt(portString);
				} catch (Exception e) {
					// TODO: handle exception
					mPort = 0;
				}
				
				if (mPort <= 0 || mPort > 65535) {
					mTextTool.showTips("请输入正确的端口号。");
					return;
				}
			
				mServer = new AssistServer(mTextTool, mPort);
				mServer.start();
				mTextTool.showTips("服务已开启！等待客户端连接。");
			} else {
				mTextTool.showTips("您已开启服务，无需再次开启。");
			}
			
		} else if (source == btn_choose_file) {
			// 选择文件
			JFileChooser dlg = new JFileChooser();
			dlg.setDialogTitle("选择文件");
			// 打开"打开文件"对话框
			int result = dlg.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				mSelectedFile = dlg.getSelectedFile();
			}
			if (mSelectedFile != null) {
				label_file_name.setText(mSelectedFile.getName());
			}
		} else if (source == btn_start_transfer) {
			if (mServer == null || mServer.mOutputStream == null) {
				mTextTool.showTips("请先开启服务并等待客户端连接。");
				return;
			}
			
			if (mPort <= 0 || mPort > 65535) {
				mTextTool.showTips("请输入正确的端口号。");
				return;
			}
			
			if (mSelectedFile == null || mSelectedFile.length() <=0) {
				mTextTool.showTips("请选择一个文件。");
				return;
			}
			
			mTextTool.showTips("开始发送文件...");
			// 发送文件给客户端：
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					FileTools.sendFileToClient(mServer.mOutputStream, mSelectedFile, mTextTool);
				}
			}).start();
		}
	}
}
