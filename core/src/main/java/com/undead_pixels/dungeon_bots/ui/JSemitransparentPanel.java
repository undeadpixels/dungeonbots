/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * @author kevin
 *
 */
public class JSemitransparentPanel extends JPanel {
	
	public enum FloatingFlavor {
		/**
		 * Does not move when anchor point moves
		 * (just the tail)
		 */
		ANCHORED,
		/**
		 * Moves slowly
		 */
		WOBBLY,
		/**
		 * Always moves to follow the anchor point
		 */
		FLOATY
	}
	
	private boolean draggable;
	private float blur = 0.0f;
	private Color color = new Color(0, 0, 0, 127);
	private int roundingRadius = 32;
	private Shape shape;
	private boolean hasAnchorTail = true;
	private FloatingFlavor floatingFlavor = FloatingFlavor.ANCHORED;
	private float anchorX, anchorY;
	private float tailWidth = 32.0f;
	private JPanel contentPane = new JPanel();
	
	public JSemitransparentPanel() {
		this.setBorder(BorderFactory.createEmptyBorder(roundingRadius, roundingRadius, roundingRadius, roundingRadius));
		
		setSize(100, 100);
		setAnchor(150, 150);
		
		this.setOpaque(false);
		
		this.add(contentPane);
		contentPane.setOpaque(false);
		//contentPane.setLocation(100, 100);
		//contentPane.setSize(100, 100);
		

		javax.swing.Timer t = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				repaint();
			}
		});
		
		t.setRepeats(true);
		t.start();
	}
	
	public JComponent getContentPane() {
		return contentPane;
	}
	
	public void setAnchor(int x, int y) {
		//this.setLocation(x, y);
		this.anchorX = x;
		this.anchorY = y;
		
		reformGeometry();
	}
	
	private void reformGeometry() {
		Shape rect = new RoundRectangle2D.Float(200, 200, 100, 100, roundingRadius, roundingRadius);
		
		if(hasAnchorTail) {
			Point2D.Float center = getCenter();
			Point2D.Float normal = new Point2D.Float(anchorX - center.x, anchorY - center.y);
			float normalLength = (float) normal.distance(new Point2D.Float());
			Point2D.Float ortho = new Point2D.Float(normal.y / normalLength / 2, -normal.x / normalLength / 2);

			int ax = (int) (center.x + ortho.x * tailWidth);
			int bx = (int) (center.x - ortho.x * tailWidth);
			int ay = (int) (center.y + ortho.y * tailWidth);
			int by = (int) (center.y - ortho.y * tailWidth);
			
			Polygon tail = new Polygon(new int[] {ax, bx, (int)anchorX}, new int[] {ay, by, (int)anchorY}, 3);
			
			Area totalShape = new Area();
			totalShape.add(new Area(rect));
			totalShape.add(new Area(tail));
			this.shape = totalShape;
			
		} else {
			this.shape = rect;
		}
	}
	

	/**
	 * @return
	 */
	private Float getCenter () {
		// TODO Auto-generated method stub
		//return new Point2D.Float(this.getX() + this.getWidth()/2, this.getY() + this.getHeight()/2);stub
		return new Point2D.Float(250, 250);
	}

	@Override
	public void paintComponent(Graphics g) {
		reformGeometry();
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(color);
		g2.fill(shape);
		
	}
}
