package com.undead_pixels.dungeon_bots.math;

public class IntegerIntervalSet extends IntervalSet<Integer> {
	@Override
	protected boolean areConsecutive(Integer a, Integer b) {
		return a == (b - 1);
	}
}
