/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * @author kevin
 *
 */
public class JSemitransparentPanel extends JPanel {
	
	private boolean draggable;
	private float blur = 0.0f;
	private Color color = new Color(0, 0, 0, 127);
	private int roundingRadius = 32;
	
	public JSemitransparentPanel() {
		this.setBorder(BorderFactory.createEmptyBorder(roundingRadius, roundingRadius, roundingRadius, roundingRadius));
	}
	

	public void paint(Graphics g) {
		g.setColor(color);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), roundingRadius, roundingRadius);
		
	}
}
