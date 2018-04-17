package com.undead_pixels.dungeon_bots.math;

import java.io.Serializable;
import java.util.Iterator;


/**
 * An IntervalSet which represents a collection of integers, but which only
 * maintains a simple list of inflection points as to whether a range of integer
 * is includes or not.
 */
public class IntegerSet extends IntervalSet<Integer> implements Iterable<IntegerSet.Interval> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean areConsecutive(Integer a, Integer b) {
		return (a == b - 1);
	}

	public boolean add(Interval interval) {
		return add(interval.start, interval.end);
	}

	public void spread(int atIndex, int size){
		int leftIdx = this.getBracketingIntervalIndex(atIndex);
		if (leftIdx <0) {
			for (int i = 0; i < this.intervals.size(); i++){
				this.intervals.get(i).start += size;
				this.intervals.get(i).end += size;
			}			
			return;
		} else if (intervals.get(leftIdx).end >= atIndex) {
			intervals.get(leftIdx).end += size;
		}
		for (int i = leftIdx + 1; i < this.intervals.size(); i++) {
			this.intervals.get(i).start += size;
			this.intervals.get(i).end += size;
		}
	}
	
	@Override
	public Iterator<IntegerSet.Interval> iterator() {
		return new Iterator<IntegerSet.Interval>() {

			int idx = 0;

			@Override
			public boolean hasNext() {
				return idx < intervals.size();
			}

			@Override
			public IntegerSet.Interval next() {
				IntervalSet<Integer>.Interval rawInterval = intervals.get(idx++);
				int start = rawInterval.start + ((rawInterval.includesStart) ? 0 : 1);
				int end = rawInterval.end + ((rawInterval.includesEnd) ? 0 : -1);
				return new Interval(start, end);
			}

		};
	}

	/**
	 * A simple data structure representing an interval from one int to another,
	 * inclusive at both ends.
	 */
	public static final class Interval implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final int start;
		public final int end;

		public Interval(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return "[ " + start + " ] .. [ " + end + " ]";
		}

		/**Returns a copy of this interval.*/
		public Interval copy() {
			return new Interval(start, end);
		}

		@Override
		public int hashCode () {
			final int prime = 31;
			int result = 1;
			result = prime * result + end;
			result = prime * result + start;
			return result;
		}

		@Override
		public boolean equals (Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Interval other = (Interval) obj;
			if (end != other.end)
				return false;
			if (start != other.start)
				return false;
			return true;
		}
	}

}
