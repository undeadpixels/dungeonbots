package com.undead_pixels.dungeon_bots;

public class TestUtils {

	public static boolean cmp(double a, double b, double epsilon) {
		return Math.abs(a - b) < epsilon;
	}
}
