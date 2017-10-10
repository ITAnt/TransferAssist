package com.ithouse.assist;

import javax.swing.JTextArea;

public class TextTool {
	private JTextArea mArea;
	private StringBuilder mBuilder;
	public TextTool(JTextArea area, StringBuilder builder) {
		mArea = area;
		mBuilder = builder;
	}
	
	public void showTips(String text) {
		if (mArea == null || mBuilder == null) {
			return;
		}
		mBuilder.append(text).append("\r\n\r\n");
		mArea.setText(mBuilder.toString());
	}
}
