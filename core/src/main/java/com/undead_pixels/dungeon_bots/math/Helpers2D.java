package com.undead_pixels.dungeon_bots.math;

import java.awt.geom.Rectangle2D;

public class Helpers2D {

	public static boolean intersect(Rectangle2D.Float a, Rectangle2D.Float b) {

		// Step #1 - check for horizontal overlap.
		float aLeft = a.x, aRight = a.x + a.width;
		float bLeft = b.x, bRight = b.x + b.width;
		if (!(contained(aLeft, bLeft, aRight) || contained(aLeft, bRight, aRight) || contained(bLeft, aLeft, bRight)
				|| contained(bLeft, aRight, bRight)))
			return false;

		// Step #2 - check for vertical overlap.
		float aTop = a.y, aBottom = a.y + a.height;
		float bTop = b.y, bBottom = b.y + b.height;
		if (!(contained(aTop, bTop, aBottom) || contained(aTop, bBottom, aBottom) || contained(bTop, aTop, bBottom)
				|| contained(bTop, aBottom, bBottom)))
			return false;

		// They overlap both horizontally and vertically, so they must
		// intersect.
		return true;
	}

	private static boolean contained(float a, float item, float b) {
		return (a <= item && item <= b) || (b <= item && item <= a);
	}

}
