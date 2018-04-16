/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * @author kevin
 *
 */
public class TreeIcons {
	

	public static final Icon collapsedIcon = new CollapsedIcon();
	public static final Icon expandedIcon = new ExpandedIcon();
	
	
	private static class CollapsedIcon implements Icon {

		/* (non-Javadoc)
		 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
		 */
		@Override
		public void paintIcon (Component c, Graphics g, int x, int y) {
			int o = 0;
			int ox = -4;
			
			int lx = o + x + ox;
			int rx = getIconWidth() - o + x + ox;
			int ty = o + y;
			int by = getIconHeight() - o + y;
			int my = (by+ty)/2;
			
			g.setColor(Color.lightGray);
			g.fillPolygon(new int[] {lx, rx, lx}, new int[] {ty, my, by}, 3);
		}

		/* (non-Javadoc)
		 * @see javax.swing.Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth () {
			return 8;
		}

		/* (non-Javadoc)
		 * @see javax.swing.Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight () {
			return 8;
		}
	}
	
	private static class ExpandedIcon extends CollapsedIcon {
		@Override
		public void paintIcon (Component c, Graphics g, int x, int y) {
			int o = 0;
			int ox = -4;
			
			int lx = o + x + ox;
			int rx = getIconWidth() - o + x + ox;
			int ty = o + y;
			int by = getIconHeight() - o + y;
			int mx = (lx+rx)/2;

			g.setColor(Color.lightGray);
			g.fillPolygon(new int[] {lx, rx, mx}, new int[] {ty, ty, by}, 3);
		}
	}
}
