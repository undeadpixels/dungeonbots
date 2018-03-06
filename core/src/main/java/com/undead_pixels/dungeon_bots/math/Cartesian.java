package com.undead_pixels.dungeon_bots.math;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Cartesian {

	public static Rectangle2D.Float makeRectangle(Point2D.Float pt1, Point2D.Float pt2) {
		return new Rectangle2D.Float(Math.min(pt1.x, pt2.x), Math.min(pt1.y, pt2.y), Math.abs(pt1.x - pt2.x),
				Math.abs(pt1.y - pt2.y));
	}

	public static Rectangle makeRectangle(Point pt1, Point pt2) {
		return new Rectangle(Math.min(pt1.x, pt2.x), Math.min(pt1.y, pt2.y), Math.abs(pt1.x - pt2.x),
				Math.abs(pt1.y - pt2.y));
	}
}
