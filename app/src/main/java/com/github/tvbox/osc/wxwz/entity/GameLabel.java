package com.github.tvbox.osc.wxwz.entity;

/**
 * 
 * @author Linhai GU
 * 
 */
public class GameLabel {
	public String name;
	public String textColor;// 字体颜色
	public String backgroudColor;// 标签背景颜色
	public String strokeColor;// 标签外框颜色

	public GameLabel() {
	}

	public GameLabel(String name, String textColor) {
		this.name = name;
		this.textColor = textColor;
	}
}
