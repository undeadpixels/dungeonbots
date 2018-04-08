package com.undead_pixels.dungeon_bots.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class JDotIcon implements Icon {

	private Color color = Color.black;
	private int width = 10;
	private int height = 10;
	private int arc = 3;


	public JDotIcon(Color color, int width, int height, int arc) {
		this.color = color;
		this.width = width;
		this.height = height;
		this.arc = arc;
	}


	public int getArc() {
		return arc;
	}


	public void setArc(int arc) {
		this.arc = arc;
	}


	public Color getColor() {
		return this.color;
	}


	public void setColor(Color color) {
		if (this.color.equals(color))
			return;
		this.color = color;
	}


	@Override
	public int getIconHeight() {
		return height;
	}


	public void setIconHeight(int h) {
		this.height = h;
	}


	@Override
	public int getIconWidth() {
		return width;
	}


	public void setIconWidth(int w) {
		this.width = w;
	}


	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// int cHeight = c.getHeight();
		int cWidth = c.getWidth();
		int xOffset = (cWidth - width) / 2;
		// int yOffset = (cHeight - height) / 2;
		g.setColor(color);
		g.fillOval(x + xOffset, y + 0, width, height);
	}
}
