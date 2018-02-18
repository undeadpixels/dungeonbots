package com.undead_pixels.dungeon_bots.math;

import java.util.Iterator;

public class IntegerIntervalSet extends IntervalSet<Integer> implements Iterable<IntervalSet<Integer>.Interval> {

	@Override
	public boolean areConsecutive(Integer a, Integer b) {
		return (a == b - 1);
	}

	@Override
	public Iterator<IntervalSet<Integer>.Interval> iterator() {
		return new Iterator<IntervalSet<Integer>.Interval>() {

			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < intervals.size();
			}

			@Override
			public IntervalSet<Integer>.Interval next() {
				return intervals.get(idx++);
			}

		};
	}

	

}
