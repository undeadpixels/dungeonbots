package com.undead_pixels.dungeon_bots.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class IntervalSet<T extends Comparable<T>> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean _NegativeInfinite = false;
	private boolean _PositiveInfinite = false;
	protected List<Interval> intervals;

	/** Creates a finite inclusion set. */
	public IntervalSet() {
		intervals = new ArrayList<Interval>();
	}

	public IntervalSet(boolean negativeInfinite, boolean positiveInfinite) {
		this();
		this._NegativeInfinite = negativeInfinite;
		this._PositiveInfinite = positiveInfinite;
	}

	// ===================================================
	// ===================================================
	// ======== IntervalSet CONTENT CHANGES ==============
	// ===================================================
	// ===================================================

	/**
	 * Assures that this set contains the given item. Returns whether the set
	 * was changed.
	 */
	public boolean add(T item) {
		if (intervals.size() == 0) {
			if (_PositiveInfinite && _NegativeInfinite)
				return false;
			else {
				intervals.add(new Interval(item, item, true, true));
				return true;
			}
		}

		int idx = getBracketingIntervalIndex(item);
		Interval temp;
		if (idx < 0) {
			if (_NegativeInfinite)
				return false;
			if ((temp = intervals.get(0).combine(item)) == null)
				intervals.add(0, new Interval(item, item, true, true));
			else
				intervals.set(0, temp);
			return true;
		} else {
			if ((temp = intervals.get(idx)).includes(item))
				return false;
			if ((temp = temp.combine(item)) == null) {
				if (idx < intervals.size() - 1) {
					Interval t = intervals.get(idx + 1).combine(temp);
					if (t != null) {
						intervals.set(idx + 1, t);
						return true;
					}
				} else if (_PositiveInfinite)
					return false;
				intervals.add(idx + 1, new Interval(item, item, true, true));
			} else
				intervals.set(idx, temp);

			return true;
		}

	}

	/**
	 * Assures that this set contains the given interval. Returns whether the
	 * set was changed.
	 */
	public boolean add(IntervalSet<T>.Interval newInterval) {

		// Simple infinity situation?
		if (intervals.size() == 0) {
			if (_PositiveInfinite || _NegativeInfinite)
				return false;
			else {
				intervals.add(newInterval.copy());
				return true;
			}
		}

		int replaceEnd = getBracketingIntervalIndex(newInterval.end);
		Interval combinedEnd;

		// New interval entirely precedes what exists?
		if (replaceEnd < 0) {
			if (_NegativeInfinite)
				return false;
			if ((combinedEnd = newInterval.combine(intervals.get(0))) != null) {
				intervals.set(0, combinedEnd);
				return true;
			} else {
				intervals.add(0, newInterval.copy());
				return true;
			}
		}

		// Prior interval is not the last one, but new interval combines with
		// the next one?
		else if (replaceEnd < intervals.size() - 1
				&& (combinedEnd = newInterval.combine(intervals.get(replaceEnd + 1))) != null)
			replaceEnd++;

		// Prior interval is the last one, but new interval won't combine with
		// it?
		else if ((combinedEnd = newInterval.combine(intervals.get(replaceEnd))) == null) {
			if (++replaceEnd == intervals.size() && _PositiveInfinite)
				return false;
			else {
				intervals.add(replaceEnd, newInterval);
				return true;
			}
		}

		// From here, 'combinedEnd' is non-null.
		int replaceStart = getBracketingIntervalIndex(newInterval.start, 0, Math.min(replaceEnd, intervals.size() - 1));
		if (replaceStart < 0)
			replaceStart = 0;
		Interval replacement;

		// Replacement doesn't combine with the interval at the replace start?
		if ((replacement = combinedEnd.combine(intervals.get(replaceStart))) == null) {
			if (++replaceStart < replaceEnd)
				replacement = combinedEnd.combine(intervals.get(++replaceStart));
			else
				replacement = combinedEnd;
		}

		// From here, 'replacement' represents the entire extent of the
		// replacement interval.
		intervals.subList(replaceStart, replaceEnd).clear();
		intervals.set(replaceStart, replacement);

		// Now, check for infinity correctness.
		if (_NegativeInfinite) {
			if (_PositiveInfinite && intervals.size() == 1)
				intervals.clear();
			else {
				Interval temp = intervals.get(0);
				if (temp.start.compareTo(temp.end) != 0)
					intervals.set(0, new Interval(temp.end, temp.end, temp.includesEnd, temp.includesEnd));
			}
		} else if (_PositiveInfinite) {
			Interval temp = intervals.get(intervals.size() - 1);
			if (temp.start.compareTo(temp.end) != 0)
				intervals.set(intervals.size() - 1,
						new Interval(temp.start, temp.start, temp.includesStart, temp.includesStart));
		}

		// Finally, a change MUST have occurred.
		return true;

	}

	/**
	 * Assures that this set contains all items from the given 'from' to the
	 * given 'to'. Returns whether the set was changed.
	 */
	public boolean add(T from, T to) {

		int c = from.compareTo(to);
		if (c > 0)
			return add(to, from);
		else if (c == 0)
			return add(from);

		else
			return add(new Interval(from, to, true, true));
	}

	/**
	 * Removes all contents from this set. If this set is infinite in either the
	 * positive or negative direction, it will be doubly finite after this call.
	 */
	public void clear() {
		intervals.clear();
		_PositiveInfinite = false;
		_NegativeInfinite = false;
	}

	/**
	 * Makes this into a negative-finite set, with the given lower bound the
	 * lowest included member of this set.
	 */
	public void clearNegativeInfinity(T lowerBound) {
		throw new IllegalStateException("Haven't fully implemented infinite sets yet.");
	}

	/**
	 * Makes this into a positive-finite set, with the given upper bound the
	 * highest included member of this set.
	 */
	public void clearPositiveInfinity(T upperBound) {
		throw new IllegalStateException("Haven't fully implemented infinite sets yet.");
	}

	/** Makes this into a negative-infinite set. */
	public void makeNegativeInfinite() {
		throw new IllegalStateException("Haven't fully implemented infinite sets yet.");
	}

	/** Makes this into a positive-infinite set. */
	public void makePositiveInfinite() {
		throw new IllegalStateException("Haven't fully implemented infinite sets yet.");
	}

	/**
	 * Makes this into a universal set, containing everything from positive
	 * infinity to negative infinity.
	 */
	public void makeUniversal() {
		intervals.clear();
		_PositiveInfinite = true;
		_NegativeInfinite = true;
	}

	/**
	 * Ensures that the given item does not exist on this set. Returns whether
	 * the set was changed.
	 */
	public boolean remove(T item) {

		if (intervals.size() == 0) {
			if (_PositiveInfinite && _NegativeInfinite) {
				intervals.add(new Interval(item, item, false, false));
				return true;
			} else
				return false;
		}

		int idx = getBracketingIntervalIndex(item);
		IntervalPair pair;

		// Does the item precede all intervals?
		if (idx < 0) {
			if (!_NegativeInfinite)
				return false;
			throw new IllegalStateException("Haven't fully implemented infinite sets yet.");
		}

		// If the item falls in a gap anyway, no change is made.
		else if ((pair = intervals.get(idx).cut(item)) == null)
			return false;

		else if (pair.length() == 1)
			intervals.set(idx, pair.a);

		else {
			intervals.set(idx, pair.a);
			intervals.add(idx + 1, pair.b);
		}

		if (_PositiveInfinite)
			throw new IllegalStateException("Haven't fully implemented infinite sets yet.");
		if (_NegativeInfinite)
			throw new IllegalStateException("Haven't fully implemented infinite sets yet.");

		return true;
	}

	/**
	 * Ensures that the items from the given 'from' to the given 'to'
	 * (inclusive) do not exist on this set. Returns whether the set was
	 * changed.
	 */
	public boolean remove(T from, T to) {
		int c = from.compareTo(to);
		if (c > 0)
			return remove(to, from);
		else if (c == 0)
			return remove(from);
		else
			return remove(new Interval(from, to, true, true));
	}

	/**
	 * Ensures that the items from the interval do not exist on this set.
	 * Returns whether the set was changed.
	 */
	public boolean remove(Interval other) {

		if (intervals.size() == 0) {
			if (_PositiveInfinite || _NegativeInfinite) {
				intervals.add(new Interval(other.start, other.start, false, false));
				intervals.add(new Interval(other.end, other.end, false, false));
				return true;
			} else
				return false;
		}

		int idxEnd = getBracketingIntervalIndex(other.end);

		// Is the removal entirely below the listed inclusions?
		if (idxEnd < 0) {
			if (!_NegativeInfinite)
				return false;
			else
				throw new IllegalStateException("Haven't fully implemented infinite sets yet.");
		}

		int idxStart = getBracketingIntervalIndex(other.start, 0, idxEnd);

		// Does the removal bracket from below the listed inclusions to a point
		// within the inclusions?
		if (idxStart < 0) {
			IntervalPair endPair;
			if ((endPair = intervals.get(idxEnd).cut(other)) == null)
				intervals.subList(0, idxEnd + 1).clear();
			else if (endPair.length() == 1) {
				intervals.subList(0, idxEnd).clear();
				intervals.set(0, endPair.a);
			}
		}

		// Is the removal entirely within a single inclusion?
		else if (idxStart == idxEnd) {
			IntervalPair endPair;
			if ((endPair = intervals.get(idxEnd).cut(other)) == null)
				return false;
			else {
				intervals.set(idxEnd, endPair.a);
				if (endPair.length() > 1)
					intervals.add(idxEnd + 1, endPair.b);
			}
		}

		// Otherwise, the removal brackets one or more inclusions.
		else {
			IntervalPair endPair, startPair = intervals.get(idxStart).cut(other);
			if (startPair != null && !startPair.a.isEmpty())
				intervals.set(idxStart++, startPair.a);
			endPair = intervals.get(idxEnd).cut(other);
			if (endPair.a.isEmpty())
				idxEnd++;
			else
				intervals.set(idxEnd, endPair.a);
			intervals.subList(idxStart, idxEnd).clear();
		}

		if (_PositiveInfinite)
			throw new IllegalStateException("Haven't fully implemented infinite sets yet.");
		if (_NegativeInfinite)
			throw new IllegalStateException("Haven't fully implemented infinite sets yet.");

		return true;
	}

	// ===================================================
	// ===================================================
	// ======== IntervalSet CONTENT QUERIES ==============
	// ===================================================
	// ===================================================

	public final boolean any(T from, T to) {

		int fromIdx = getBracketingIntervalIndex(from);
		int toIdx = getBracketingIntervalIndex(to, Math.max(fromIdx, 0), intervals.size() - 1);
		int c;

		if (toIdx < 0)
			// Here, the interval entirely precedes all intervals in the set.
			// Return true if and only if negative infinity is included.
			return _NegativeInfinite;
		if ((c = toIdx - fromIdx) > 1)
			// An interval is entirely spanned - of course there's at least one
			// inclusion.
			return true;
		else if (c == 1) {

			// The 'from' and 'to' come from adjacent intervals. Only return
			// true if the 'from' snags the lower interval, or the 'to' snags
			// the upper.
			if ((c = from.compareTo(intervals.get(fromIdx).end)) < 0)
				return true;
			else if (c == 0 && intervals.get(fromIdx).includesEnd)
				return true;
			else if ((c = to.compareTo(intervals.get(toIdx).start)) > 0)
				return true;
			else if (c == 0 && intervals.get(toIdx).includesStart)
				return true;
			else
				return false;
		} else if (c == 0) {

			// The 'from' and 'to' are in the same interval. Only return true if
			// the 'from' snags the lower interval, or if we're at the last
			// interval and positive infinity is included.
			if ((c = from.compareTo(intervals.get(fromIdx).end)) < 0)
				return true;
			else if (c == 0 && intervals.get(fromIdx).includesEnd)
				return true;
			else if (fromIdx == intervals.size() - 1 && _PositiveInfinite)
				return true;
			else
				return false;
		}

		throw new IllegalStateException("Sanity check.");
	}

	/**
	 * Returns true if the given items are consecutive. Default behavior is to
	 * always return false. Override this function in a derived class to add
	 * functionality for discrete types of 'T'. For example, a class overriding
	 * this class that maintains a set of 'int's should return true if the given
	 * first is one less than the given second.
	 */
	protected boolean areConsecutive(T first, T second) {
		return false;
	}

	/**
	 * Returns the index of the interval whose start would precede the given
	 * item. Uses binary search. If no such index exists, returns -1.
	 */
	protected final int getBracketingIntervalIndex(T item) {
		return getBracketingIntervalIndex(item, 0, intervals.size() - 1);
	}

	/**
	 * Returns the index of the interval whose start would precede the given
	 * item. Uses binary search. If no such index exists, returns -1.
	 */
	protected final int getBracketingIntervalIndex(T item, int startingIndex, int endingIndex) {
		if (startingIndex < 0)
			throw new IllegalStateException("Starting index must be equal to or greater than 0.");
		if (intervals.size() == 0)
			return -1;
		if (item.compareTo(intervals.get(0).start) < 0)
			return -1;

		while (startingIndex < endingIndex) {
			// 0..1 -> 1 ========= 1..2 -> 2
			// 0..2 -> 2 ========= 1..3 -> 3
			// 0..3 -> 2 ========= 1..4 -> 3
			// 0..4 -> 3
			int middleIndex = ((startingIndex + endingIndex) >> 1) + 1;
			Interval middleInterval = intervals.get(middleIndex);
			int c = item.compareTo(middleInterval.start);
			if (c < 0)
				endingIndex = middleIndex - 1;
			else if (c > 0)
				startingIndex = middleIndex;
			else
				return middleIndex;
		}
		return startingIndex;
	}

	/** Returns the count of contiguous intervals in this set. */
	public int getIntervalsCount() {
		return intervals.size();
	}

	/** Returns whether the given item is included in this set. */
	public boolean includes(T item) {
		int idx = getBracketingIntervalIndex(item);
		if (idx < 0)
			return _NegativeInfinite;
		if (intervals.get(idx).includes(item))
			return true;
		if (idx == intervals.size() - 1)
			return _PositiveInfinite;
		return false;
	}

	/**
	 * Returns whether ALL the items from the given 'from' to the given 'to' are
	 * included in this set.
	 */
	public boolean includes(T from, T to) {
		int c = from.compareTo(to);
		if (c > 0)
			return includes(to, from);
		else if (c == 0)
			return includes(from);

		if (intervals.size() == 0)
			return (_PositiveInfinite && _NegativeInfinite);

		int toIdx = getBracketingIntervalIndex(to);
		if (toIdx < 0)
			return _NegativeInfinite;
		int fromIdx = getBracketingIntervalIndex(from, 0, toIdx);
		if (fromIdx < 0) {
			if (!_NegativeInfinite)
				return false;
			Interval val = intervals.get(0);
			if (toIdx > 0)
				return false;
			if (!val.includesStart)
				return false;
			if (intervals.size() == 1 && _PositiveInfinite && val.includesEnd)
				return true;
			return val.includes(to);
		}
		if (fromIdx == toIdx) {
			Interval val = intervals.get(fromIdx);
			return val.includes(to) && val.includes(from);
		}
		return false;
	}

	/** Returns whether this set contains no contents. */
	public boolean isEmpty() {
		return intervals.size() == 0 && !(_PositiveInfinite || _NegativeInfinite);
	}

	/**
	 * Returns whether this set is infinite in both the positive and negative
	 * directions.
	 */
	public boolean isInfinite() {
		return _PositiveInfinite && _NegativeInfinite;
	}

	/** Returns whether this set is infinite in the negative direction. */
	public boolean isNegativeInfinite() {
		return _NegativeInfinite;
	}

	/** Returns whether this set is infinite in the positive direction. */
	public boolean isPositiveInfinite() {
		return _PositiveInfinite;
	}

	/**
	 * Returns whether this set contains all items in the universal set (for
	 * most purposes, all items from negative infinity to positive infinity).
	 */
	public boolean isUniversal() {
		return intervals.size() == 0 && _PositiveInfinite && _NegativeInfinite;
	}

	@Override
	public String toString() {
		return ((_PositiveInfinite) ? "<--" : "") + intervals.toString() + ((_NegativeInfinite) ? "-->" : "");
	}

	private final class IntervalPair implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public Interval a;
		public Interval b;

		public IntervalPair(Interval a, Interval b) {
			this.a = a;
			this.b = b;
		}

		public int length() {
			return (b == null) ? 1 : 2;
		}
	}

	/** Data structure which internally maintains a single inclusion set. */
	protected final class Interval implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public boolean includesStart;
		public T start;
		public T end;
		public boolean includesEnd;

		public Interval(T start, T end, boolean includesStart, boolean includesEnd) {
			this();
			this.start = start;
			this.end = end;
			this.includesStart = includesStart;
			this.includesEnd = includesEnd;
		}

		private Interval() {
		}

		/** Returns a shallow copy of this interval. */
		public Interval copy() {
			return new Interval(start, end, includesStart, includesEnd);
		}

		/**
		 * Returns an interval which combines all the contents of this and the
		 * given 'other' interval. If the two could not be combined (either
		 * because they do not overlap at all or because they are
		 * non-consecutive), returns null.
		 */
		public final Interval combine(Interval other) {
			int c = other.end.compareTo(start);

			// The other's end lands on the current start?
			if (c == 0)
				return (other.includesEnd || includesStart)
						? new Interval(other.start, end, other.includesStart, includesEnd) : null;
			// The other's end precedes the current start?
			else if (c < 0)
				return (areConsecutive(other.end, start) && other.includesEnd && includesStart)
						? new Interval(other.start, end, other.includesStart, includesEnd) : null;
			// The end lands on the other's start?
			else if ((c = end.compareTo(other.start)) == 0)
				return (includesEnd || other.includesStart)
						? new Interval(start, other.end, includesStart, other.includesEnd) : null;
			// The end precede's the other's start?
			else if (c < 0)
				return (areConsecutive(end, other.start) && includesEnd && other.includesStart)
						? new Interval(start, other.end, includesStart, other.includesEnd) : null;
			// The two definitely overlap to some degree.
			Interval combined = new Interval();
			if ((c = start.compareTo(other.start)) < 0) {
				combined.start = start;
				combined.includesStart = includesStart;
			} else if (c == 0) {
				combined.start = start;
				combined.includesStart = includesStart || other.includesStart;
			} else {
				combined.start = other.start;
				combined.includesStart = other.includesStart;
			}
			if ((c = end.compareTo(other.end)) > 0) {
				combined.end = end;
				combined.includesEnd = includesEnd;
			} else if (c == 0) {
				combined.end = end;
				combined.includesEnd = includesEnd || other.includesEnd;
			} else {
				combined.end = other.end;
				combined.includesEnd = other.includesEnd;
			}
			return combined;
		}

		/**
		 * Returns an interval which combines all the contents of this, plus the
		 * given item. If the two could not be combined (either because they do
		 * not overlap at all or because they are non-consecutive), returns
		 * null.
		 */
		public Interval combine(T item) {
			int c = item.compareTo(start);
			if (c < 0) {
				if (!areConsecutive(item, start) || !includesStart)
					return null;
				return new Interval(item, end, true, includesEnd);
			} else if (c == 0)
				return new Interval(start, end, true, includesEnd);
			else if ((c = item.compareTo(end)) > 0) {
				if (!areConsecutive(end, start) || !includesEnd)
					return null;
				return new Interval(start, item, includesStart, true);
			} else if (c == 0)
				return new Interval(start, end, includesStart, true);
			else
				return new Interval(start, end, includesStart, includesEnd);
		}

		/**
		 * Returns an interval which combines all the contents of from the given
		 * 'from' to the given 'to' (inclusive). If the two intervals could not
		 * be combined (either because they do not overlap at all or because
		 * they are non-consecutive), returns null.
		 */
		public final Interval combine(T from, T to) {

			int c = from.compareTo(to);
			// 'to' and 'from' are in the wrong order?
			if (c > 0)
				return combine(to, from);
			return combine(new Interval(from, to, true, true));
		}

		/**
		 * Returns an interval pair structure representing this interval after
		 * the given item has been removed. If the given item already does not
		 * exist in this interval, returns null. Otherwise, returns a
		 * single-length structure if the removal would leave only a single
		 * interval, or a double-length structure if the removal would split
		 * this interval in two.
		 */
		public final IntervalPair cut(T item) {

			int c;

			// Item precedes this interval?
			if ((c = item.compareTo(start)) < 0)
				return null;

			// Item matches this interval's start?
			else if (c == 0) {
				if (!includesStart)
					return null;
				if (item.compareTo(end) == 0)
					return new IntervalPair(new Interval(item, item, false, false), null);
				return new IntervalPair(new Interval(start, end, false, includesEnd), null);
			}

			// Item follows this interval's end?
			else if ((c = item.compareTo(end)) > 0)
				return null;

			// Item matches this interval's end?
			else if (c == 0) {
				if (!includesEnd)
					return null;
				return new IntervalPair(new Interval(start, end, includesStart, false), null);
			}

			// Item consecutive with non-included start?
			else if (areConsecutive(start, item) && !includesStart) {
				if (areConsecutive(item, end) && !includesEnd)
					// A three-part singleton?
					return new IntervalPair(new Interval(item, item, false, false), null);
				return new IntervalPair(new Interval(item, end, false, includesEnd), null);
			}

			// Item consecutive with non-included end?
			else if (areConsecutive(item, end) && !includesEnd)
				return new IntervalPair(new Interval(start, item, includesStart, false), null);

			// In all other cases, the interval is split into two intervals.
			else {
				Interval a = new Interval(start, item, includesStart, false);
				Interval b = new Interval(item, end, false, includesEnd);
				return new IntervalPair(a, b);
			}
		}

		/**
		 * Returns an interval pair structure representing this interval after
		 * the given range has been removed. If no part of the given range
		 * already exists in this interval, returns null. Otherwise, returns a
		 * single-length structure if the removal would leave only a single
		 * interval, or a double-length structure if the removal would split
		 * this interval in two.
		 */
		public final IntervalPair cut(T from, T to) {
			return cut(new Interval(from, to, true, true));
		}

		/**
		 * Returns an interval pair structure representing this interval after
		 * the given interval has been removed. If no part of the given interval
		 * already exists in this interval, returns null. Otherwise, returns a
		 * single-length structure if the removal would leave only a single
		 * interval, or a double-length structure if the removal would split
		 * this interval in two.
		 */
		public final IntervalPair cut(Interval other) {

			int c;
			if ((c = other.end.compareTo(start)) < 0)
				return null;
			else if (c == 0) {
				if (includesStart && other.includesEnd)
					return new IntervalPair(new Interval(start, end, false, includesEnd), null);
				else
					return null;
			} else if ((c = other.start.compareTo(end)) < 0)
				return null;
			else if (c == 0) {
				if (includesEnd && other.includesStart)
					return new IntervalPair(new Interval(start, end, includesStart, false), null);
				else
					return null;
			} else if (other.start.compareTo(start) < 0)
				return new IntervalPair(new Interval(other.end, end, !other.includesEnd, includesEnd), null);
			else if (other.end.compareTo(end) > 0)
				return new IntervalPair(new Interval(start, other.start, includesStart, !other.includesStart), null);
			else if (areConsecutive(start, other.start) && !includesStart && other.includesStart)
				return new IntervalPair(new Interval(other.start, end, false, includesEnd), null);
			else if (areConsecutive(other.end, end) && !includesEnd && other.includesEnd)
				return new IntervalPair(new Interval(start, other.end, includesStart, false), null);
			else {
				Interval a = new Interval(start, other.start, includesStart, !other.includesStart);
				Interval b = new Interval(other.end, end, !other.includesEnd, includesEnd);
				return new IntervalPair(a, b);
			}
		}

		/**
		 * Returns whether this interval contains the given item. If the item
		 * matches the 'start' or 'end' of this interval, checks that the
		 * boolean flag includes it.
		 */
		public final boolean includes(T item) {
			int c = item.compareTo(start);
			if (c < 0)
				return false;
			if (c == 0)
				return includesStart;
			c = item.compareTo(end);
			if (c > 0)
				return false;
			if (c == 0)
				return includesEnd;
			return true;
		}

		/**
		 * Returns whether this interval is empty. It can only be empty if it is
		 * either a singleton, or an interval where start and end are
		 * consecutive and both not included.
		 */
		public boolean isEmpty() {
			if (includesStart || includesEnd)
				return false;
			if (start.compareTo(end) == 0)
				return true;
			if (areConsecutive(start, end))
				return true;
			return false;
		}

		/** Returns true if this interval set contains a single member. */
		public final boolean isSingleton() {
			if ((start.compareTo(end)) == 0 && (includesStart || includesEnd))
				return true;
			return false;
		}

		/**
		 * Returns if the given item is the only member of this interval. The
		 * function captures a wider set of circumstances than isSingleton(),
		 * because the case of X_X would be a singleton too where the X's are
		 * the excluded start and end, and the _ indicates the item.
		 */
		public final boolean isSingleton(T item) {
			if (isSingleton() && start.compareTo(item) == 0)
				return true;
			return !(includesStart || includesEnd) && areConsecutive(start, item) && areConsecutive(item, end);
		}

		@Override
		public String toString() {
			if (start.compareTo(end) == 0)
				return (includesStart || includesEnd) ? " [ " + start.toString() + " ] "
						: " ( " + start.toString() + " ) ";
			return ((includesStart) ? " [ " + start.toString() + " ]--" : " ( " + start.toString() + " )--")
					+ ((includesEnd) ? "[" + end.toString() + "] " : "(" + end.toString() + ") ");
		}
	}

}
