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
	
	// ѡ�е�Ҫ�����ͻ��˵��ļ�
	private File mSelectedFile;
	// �˿ں�
	private int mPort;

	public MainActivity() {
		mStringBuilder = new StringBuilder();
		
		// ���� JFrame ʵ��
        JFrame frame = new JFrame("��������-PC");
        // Setting the width and height of frame
        frame.setSize(640, 320);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int x = (int)(toolkit.getScreenSize().getWidth()-frame.getWidth())/2;
        int y = (int)(toolkit.getScreenSize().getHeight()-frame.getHeight())/2;
        frame.setLocation(x, y);

        /* ������壬��������� HTML �� div ��ǩ
         * ���ǿ��Դ��������岢�� JFrame ��ָ��λ��
         * ��������ǿ�������ı��ֶΣ���ť�����������
         */
        JPanel panel = new JPanel();
        // �����û�����ķ����������������
        initViews(panel);

        // ������
        frame.add(panel);
        // ���ý���ɼ�
        frame.setVisible(true);
        
        mTextTool = new TextTool(ta_tips, mStringBuilder);
	}

	private void initViews(JPanel panel) {
        /* ���ֲ���������߲���������
         * ������ò���Ϊ null
         */
        panel.setLayout(null);

        // IP��ַ
        //JLabel label_ip = new JLabel("IP��ַ:");
        /* ������������������λ�á�
         * setBounds(x, y, width, height)
         * x �� y ָ�����Ͻǵ���λ�ã��� width �� height ָ���µĴ�С��
         */
        //label_ip.setBounds(10,20,80,25);
        //panel.add(label_ip);

        //tf_ip = new JTextField(20);
        //tf_ip.setBounds(100,20,170,25);
        //panel.add(tf_ip);

        // �˿�
        JLabel label_port = new JLabel("�˿�:");
        label_port.setBounds(10,50,40,25);
        panel.add(label_port);

        tf_port = new JTextField(5);
        tf_port.setBounds(60,50,70,25);
        panel.add(tf_port);

        btn_start_service = new JButton("��������");
        btn_start_service.setBounds(10, 90, 120, 25);
        btn_start_service.addActionListener(this);
        panel.add(btn_start_service);
        
        
        btn_choose_file = new JButton("ѡ���ļ�");
        btn_choose_file.setBounds(10, 130, 120, 25);
        btn_choose_file.addActionListener(this);
        panel.add(btn_choose_file);
        
        label_file_name = new JLabel("δѡ��");
        label_file_name.setBounds(150,130,130,25);
        panel.add(label_file_name);
        
        btn_start_transfer = new JButton("��ʼ����");
        btn_start_transfer.setBounds(10, 170, 120, 25);
        btn_start_transfer.addActionListener(this);
        panel.add(btn_start_transfer);
        
        // ������ʾ��
        ta_tips = new JTextArea();
        ta_tips.setLineWrap(true);
        // ������
        JScrollPane scrollPane = new JScrollPane(ta_tips);
        scrollPane.setBounds(330, 10, 250, 250);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
        panel.add(scrollPane);
    }

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == btn_start_service) {
			// ��������
			if (mServer == null) {
				String portString = tf_port.getText();
				try {
					mPort = Integer.parseInt(portString);
				} catch (Exception e) {
					// TODO: handle exception
					mPort = 0;
				}
				
				if (mPort <= 0 || mPort > 65535) {
					mTextTool.showTips("��������ȷ�Ķ˿ںš�");
					return;
				}
			
				mServer = new AssistServer(mTextTool, mPort);
				mServer.start();
				mTextTool.showTips("�����ѿ������ȴ��ͻ������ӡ�");
			} else {
				mTextTool.showTips("���ѿ������������ٴο�����");
			}
			
		} else if (source == btn_choose_file) {
			// ѡ���ļ�
			JFileChooser dlg = new JFileChooser();
			dlg.setDialogTitle("ѡ���ļ�");
			// ��"���ļ�"�Ի���
			int result = dlg.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				mSelectedFile = dlg.getSelectedFile();
			}
			if (mSelectedFile != null) {
				label_file_name.setText(mSelectedFile.getName());
			}
		} else if (source == btn_start_transfer) {
			if (mServer == null || mServer.mOutputStream == null) {
				mTextTool.showTips("���ȿ������񲢵ȴ��ͻ������ӡ�");
				return;
			}
			
			if (mPort <= 0 || mPort > 65535) {
				mTextTool.showTips("��������ȷ�Ķ˿ںš�");
				return;
			}
			
			if (mSelectedFile == null || mSelectedFile.length() <=0) {
				mTextTool.showTips("��ѡ��һ���ļ���");
				return;
			}
			
			mTextTool.showTips("��ʼ�����ļ�...");
			// �����ļ����ͻ��ˣ�
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
