/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.RoundRectangle2D;
import java.util.function.BiConsumer;

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
		 * Keeps a constant distance but not direction
		 */
		SPRINGY,
		
		/**
		 * Maintains a constant distance and direction
		 */
		FOLLOW
	}
	
	private boolean draggable;
	private float blur = 0.0f;
	private Color color = new Color(50, 45, 40, 200);
	private int roundingRadius = 8;
	private Shape shape;
	private boolean hasAnchorTail = true;
	
	/**
	 * @return the hasAnchorTail
	 */
	public boolean hasAnchorTail () {
		return hasAnchorTail;
	}


	
	/**
	 * @param hasAnchorTail the hasAnchorTail to set
	 */
	public void setHasAnchorTail (boolean hasAnchorTail) {
		this.hasAnchorTail = hasAnchorTail;
	}

	private FloatingFlavor floatingFlavor = FloatingFlavor.FOLLOW;
	
	/**
	 * @return the floatingFlavor
	 */
	public FloatingFlavor getFloatingFlavor () {
		return floatingFlavor;
	}

	
	/**
	 * @param floatingFlavor the floatingFlavor to set
	 */
	public void setFloatingFlavor (FloatingFlavor floatingFlavor) {
		this.floatingFlavor = floatingFlavor;
	}

	private float anchorX, anchorY;
	private float tailWidth = 32.0f;
	private JPanel contentPane = new JPanel();

	private float centerOffsetX, centerOffsetY;
	private float targetDistance = 200f, sloshiness = .01f;
	
	public JSemitransparentPanel() {
		contentPane.setBorder(BorderFactory.createEmptyBorder(roundingRadius, roundingRadius, roundingRadius, roundingRadius));
		
		setSize(100, 100);
		setAnchor(150, 150);
		
		this.setOpaque(false);
		
		this.add(contentPane);
		contentPane.setOpaque(false);
		//contentPane.setLocation(100, 100);
		//contentPane.setSize(100, 100);
		

		//javax.swing.Timer t = new Timer(15, new ActionListener() {
		//	@Override
		//	public void actionPerformed (ActionEvent e) {
		//		setAnchor(250, (float) (300 + Math.sin(System.currentTimeMillis() / 10000.0) * 0));
		//		update(.015f);
		//	}
		//});
		
		//t.setRepeats(true);
		//t.start();
	}
	
	public JComponent getContentPane() {
		return contentPane;
	}
	
	private float accumulatedDx = 0.0f, accumulatedDy = 0.0f;
	public void update(float dt) {
		Point2D.Float center = this.getCenter();
		Point2D.Float target = center;
		switch(floatingFlavor) {
			case ANCHORED:
				// do nothing
				break;
			case FOLLOW:
				target = new Point2D.Float(anchorX + centerOffsetX, anchorY + centerOffsetX);
				break;
			case SPRINGY:
				Point2D.Float delta = new Point2D.Float(center.x - anchorX, center.y - anchorY);
				float deltaLen = (float) delta.distance(new Point2D.Float());
				if(deltaLen > .0001) {
					target = new Point2D.Float(
							center.x + delta.x / deltaLen * targetDistance,
							center.y + delta.y / deltaLen * targetDistance);
				} else {
					target = new Point2D.Float(center.x + targetDistance , center.y);
				}
				break;
			default:
				break;
		}
		
		float dxTarget = target.x - center.x;
		float dyTarget = target.y - center.y;

		float dx = dxTarget * (1 - sloshiness) * dt;
		float dy = dyTarget * (1 - sloshiness) * dt;


		accumulatedDx += dx;
		accumulatedDy += dy;
		int dxInt = (int) (accumulatedDx);
		int dyInt = (int) (accumulatedDy);
		
		if(dxInt != 0 || dyInt != 0) {
			Point before = contentPane.getLocation();
			this.contentPane.setLocation(before.x + dxInt, before.y + dyInt);
			
			// compute bounding box and fit
			
			this.repaint();
			accumulatedDx -= dxInt;
			accumulatedDy -= dyInt;
		} else {
		}
	}
	
	public void setAnchor(float x, float y) {
		//this.setLocation(x, y);
		this.anchorX = x;
		this.anchorY = y;
		
		reformGeometry();
	}
	
	private void reformGeometry() {
		Point topLeft = this.contentPane.getLocation();
		Dimension size = this.contentPane.getSize();
		Shape rect = new RoundRectangle2D.Float(topLeft.x, topLeft.y, size.width, size.height, roundingRadius*2, roundingRadius*2);
		
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
		Point topLeft = this.contentPane.getLocation();
		Dimension size = this.contentPane.getSize();
		return new Point2D.Float(topLeft.x + size.width/2.0f, topLeft.y + size.height/2.0f);
	}

	@Override
	public void paintComponent(Graphics g) {
		reformGeometry();
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(color);
		g2.fill(shape);
		
	}

	/**
	 * 
	 */
	public void recursiveTransparentify () {
		recursiveTransparentify(this.contentPane);
	}
	
	private static void recursiveTransparentify(Component comp) {
		if(comp instanceof JComponent) {
			JComponent j = (JComponent) comp;
			j.setOpaque(false);
		}
		if(comp instanceof Container) {
			Container cont = (Container) comp;

			for(Component c : cont.getComponents()) {
				recursiveTransparentify(c);
			}
		}
	}
}
