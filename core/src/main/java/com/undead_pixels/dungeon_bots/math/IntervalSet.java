package com.undead_pixels.dungeon_bots.math;

import java.util.ArrayList;

/**
 * A data structure that represents set inclusion for comparable objects. Use
 * this data structure for tracking intervals of things in sets, and performing
 * logical operations on those sets. This class should be overridden if the
 * object type is one of consecutive objects (like an Integer), with the
 * areConsecutive(T,T) method returning whether two T values are
 * consecutive. @author Wesley Oates
 */
public class IntervalSet<T extends Comparable<T>> {

	private final ArrayList<Inflection> _Inflections = new ArrayList<Inflection>();

	// ==============================================
	// ====== IntervalSet CONTENT QUERIES ===========
	// ==============================================

	@Override
	public final boolean equals(Object other) {
		if (other instanceof IntervalSet<?>) {
			return _Inflections.equals(((IntervalSet<?>) other)._Inflections);
		}
		return false;
	}

	/** Returns whether this collection represents the universal set. */
	public final boolean isUniversal() {
		if (_Inflections.size() != 1)
			return false;
		Inflection inf = _Inflections.get(0);
		return inf.includesBefore && inf.includesValue && inf.includesAfter;
	}

	/** Returns whether this collection extends to positive infinity. */
	public final boolean includesPositiveInfinity() {
		return _Inflections.size() > 0 && _Inflections.get(_Inflections.size() - 1).includesAfter;
	}

	/** Returns whether this collection extends to negative infinity. */
	public final boolean includesNegativeInfinity() {
		return _Inflections.size() > 0 && _Inflections.get(0).includesBefore;
	}

	/** Returns whether this collection contains only a singleton. */
	public final boolean isSingleton() {
		return _Inflections.size() == 1 && _Inflections.get(0).isSingleton();
	}

	/** Returns whether this collection is an empty set. */
	public final boolean isEmpty() {
		return _Inflections.size() == 0;
	}

	/** Returns whether this collection includes the given item. */
	public final boolean includes(T item) {
		int idx = getPrecedingInflectionIndex(item, 0);
		if (idx < 0)
			return _Inflections.size() > 0 && _Inflections.get(0).includesBefore;
		Inflection inf = _Inflections.get(idx);
		if (item.equals(inf.value))
			return inf.includesValue;
		return inf.includesAfter;
	}

	/**
	 * Returns whether this collection includes every item in the given range.
	 */
	public final boolean includes(T from, T to) {

		// An empty set obviously won't contain the range.
		if (_Inflections.size() == 0)
			return false;

		// In the wrong order? Just reverse the order.
		int c = from.compareTo(to);
		if (c > 0)
			return includes(to, from);

		// 'to' and 'from' are the same? Return the other overload of this
		// method.
		else if (c == 0)
			return includes(from);

		// Find the index preceding the 'from', and following the 'to'.
		int fromIdx = getPrecedingInflectionIndex(from, 0);
		if (fromIdx < 0)
			return false;
		int toIdx = getFollowingInflectionIndex(to, fromIdx);
		if (toIdx < 0)
			return false;

		// If the 'from' index and 'to' index are not sequential, then an
		// inflection in between is by definition missing something.
		if (toIdx != fromIdx + 1)
			return false;

		// Check that the from end includes everything
		Inflection fromInf = _Inflections.get(fromIdx);
		if (!fromInf.includesAfter)
			return false;
		if (from.compareTo(fromInf.value) == 0 && !fromInf.includesValue)
			return false;

		// If the 'to' is equal to the 'to' end's value and that value isn't
		// included, then that value at least is missing.
		Inflection toInf = _Inflections.get(toIdx);
		if (to.compareTo(toInf.value) == 0 && !toInf.includesValue)
			return false;

		// All possible disqualifications exhausted, the entire range must exist
		// here.
		return true;

	}

	public final boolean includes(IntervalSet<T> other) {
		if (_Inflections.size() == 0)
			return false;
		if (other._Inflections.size() == 0)
			return true;
		throw new IllegalStateException("Not implemented yet.");
	}

	/**
	 * Returns the index of the inflection immediately equal to or preceding the
	 * given item, starting the search from the given index to the end of the
	 * existing inflection list. If the given item precedes all inflections,
	 * returns -1. If the given item follows all inflections, returns the max
	 * index.
	 */
	protected final int getPrecedingInflectionIndex(T item, int searchStart) {
		// Edge cases.
		if (_Inflections.size() == 0)
			return -1;
		int max = _Inflections.size() - 1;
		if (item.compareTo(_Inflections.get(0).value) < 0)
			return -1;

		// Use binary search.
		while (searchStart != max) {
			int divider = ((searchStart + max) >> 1) + 1;
			int c = item.compareTo(_Inflections.get(divider).value);
			if (c < 0)
				max = divider - 1;
			else if (c > 0)
				searchStart = divider;
			else
				break;
		}

		// Now that min == max, just return the min.
		return searchStart;
	}

	/**
	 * Returns the index of the inflection equal to or immediately following the
	 * given item, starting the search from the given index to the end of the
	 * existing inflection list. If the given item follows all inflections,
	 * returns -1. If the given item precedes all inflections, returns 0.
	 */
	protected final int getFollowingInflectionIndex(T item, int searchStart) {
		if (_Inflections.size() == 0)
			return -1;
		int max = _Inflections.size() - 1;
		if (searchStart >= max)
			return -1;
		if (item.compareTo(_Inflections.get(max).value) > 0)
			return -1;

		// Use binary search
		while (searchStart != max) {
			int divider = (searchStart + max) >> 1;
			int c = item.compareTo(_Inflections.get(divider).value);
			if (c < 0)
				max = divider;
			else if (c > 0)
				searchStart = divider + 1;
			else
				break;
		}

		return searchStart;

	}

	/**
	 * Override this method in a derived class to indicate consecutiveness
	 * between two items. Not all types will allow for consecutives. For
	 * example, two Integer objects such as 2 and 3 would be consecutive. But
	 * two floating-point numbers such as 2.0f and 3.0f would not (and such a
	 * type would be presumed to never have consecutives)
	 */
	protected boolean areConsecutive(T a, T b) {
		return false;
	}

	/**
	 * Returns true if the item is consecutive to the inflection value (in that
	 * order) AND the inflection point includes its value.
	 */
	protected final boolean areConsecutive(T item, Inflection inflection) {
		return areConsecutive(item, inflection.value) && inflection.includesValue;
	}

	/**
	 * Returns true if the inflection value is consecutive to the item (in that
	 * order) AND the inflection point includes its value.
	 */
	protected final boolean areConsecutive(Inflection inflection, T item) {
		return areConsecutive(inflection.value, item) && inflection.includesValue;
	}

	// ==============================================
	// ====== IntervalSet CONTENT MANAGEMENT ========
	// ==============================================

	/**
	 * Add a singleton item. Returns true if the set was changed; otherwise,
	 * returns false.
	 */
	public final boolean add(T item) {

		// Case #0: an empty set.
		if (_Inflections.size() == 0) {
			_Inflections.add(new Inflection(item, false, true, false));
			return true;
		}

		int prevIdx = getPrecedingInflectionIndex(item, 0);

		// Cases #A: the item precedes all other inflections.
		if (prevIdx < 0) {
			Inflection start = _Inflections.get(0);
			if (start.includesBefore)
				return false; // If falls in early infinity, no change is made.
			else if (start.includesValue && areConsecutive(item, start.value)) {
				_Inflections.set(0, new Inflection(item, false, true, true));
				if (!start.includesAfter)
					_Inflections.add(1, new Inflection(start.value, true, true, false));
			} else
				_Inflections.add(0, new Inflection(item, false, true, false));
			return true;
		}

		// Case #B.1: the item lands in a region already included.
		Inflection prevInf = _Inflections.get(prevIdx);
		if (prevInf.includesAfter) {
			if (prevInf.includesValue)
				return false;
			if (item.compareTo(prevInf.value) != 0)
				return false;
			_Inflections.set(prevIdx, new Inflection(prevInf.value, prevInf.includesBefore, true, true));
			return true;
		}

		// Case #B.2: the item lands immediately after the previous region,
		// consecutively.
		else if (prevInf.includesValue && areConsecutive(prevInf.value, item))
			_Inflections.set(prevIdx, new Inflection(item, prevInf.includesBefore, true, false));

		// Case #B.3: the item is a singleton, separate from the previous
		// region.
		else
			_Inflections.add(++prevIdx, prevInf = new Inflection(item, false, true, false));

		// Case #C.1: There is no following inflection.
		if (prevIdx == _Inflections.size() - 1)
			return true;

		// CAse #C.2: the next inflection immediately follows consecutively.
		Inflection nextInf = _Inflections.get(prevIdx + 1);
		if (nextInf.includesValue && areConsecutive(prevInf.value, nextInf.value)) {
			_Inflections.set(prevIdx, new Inflection(prevInf.value, prevInf.includesBefore, true, true));
			if (prevInf.includesAfter)
				_Inflections.remove(prevIdx + 1);
			else
				_Inflections.set(prevIdx + 1, new Inflection(nextInf.value, true, true, false));
		}

		return true;
	}

	/**
	 * Add an interval range. Returns true if the set was changed; otherwise,
	 * returns false.
	 */
	public final boolean add(T from, T to) {

		// Find the index preceding the 'from', and following the 'to'.
		int fromIdx = getPrecedingInflectionIndex(from, 0);
		int toIdx = getFollowingInflectionIndex(to, fromIdx);

		// 'from' precedes the inflections?
		if (fromIdx < 0) {

			// 'to' also follows the inflections?
			if (toIdx < 0) {
				_Inflections.clear();
				_Inflections.add(new Inflection(from, false, true, true));
				_Inflections.add(new Inflection(to, true, true, false));
			}
			// 'from' is the only end that brackets the existing inflections
			else {
				_Inflections.subList(0, toIdx).clear();
				Inflection newFirst = _Inflections.get(0);
				if (!newFirst.includesBefore) {
					if (areConsecutive(to, newFirst))
						_Inflections.set(0, new Inflection(from, false, true, newFirst.includesAfter));
					else {
						_Inflections.add(0, new Inflection(from, false, true, true));
						_Inflections.add(1, new Inflection(to, true, true, false));
					}
				}
			}
			return true;
		}
		// 'to' occurs beyond the last inflection point.
		else if (toIdx < 0) {
			int lastIdx = _Inflections.size() - 1;
			_Inflections.subList(fromIdx + 1, lastIdx).clear();
			Inflection newLast = _Inflections.get(lastIdx = (_Inflections.size() - 1));
			if (!newLast.includesAfter) {
				if (areConsecutive(newLast, from))
					_Inflections.set(lastIdx, new Inflection(to, newLast.includesBefore, true, false));
				else {
					_Inflections.add(new Inflection(from, false, true, true));
					_Inflections.add(new Inflection(to, true, true, false));
				}
			}
			return true;
		}

		// From here, I know there is an inflection point preceding the 'from',
		// and an inflection point following the 'to'. Also, the 'fromIdx' <
		// 'toIdx'.
		Inflection prior = _Inflections.get(fromIdx), next = _Inflections.get(toIdx);
		_Inflections.subList(fromIdx + 1, toIdx).clear();
		toIdx = fromIdx + 1;
		boolean changed = false;

		// Make sure the 'from' end works.
		if (prior.value.compareTo(from) == 0 && !(prior.includesValue && prior.includesAfter)) {
			if (!prior.includesBefore)
				_Inflections.set(fromIdx, new Inflection(from, false, true, true));
			else
				// the new version of the prior would be meaningless.
				_Inflections.remove(fromIdx);
			changed = true;
		} else if (!prior.includesAfter) {
			if (!areConsecutive(prior, from))
				_Inflections.add(fromIdx + 1, new Inflection(from, false, true, true));
			else if (prior.includesBefore)
				// Consecutive with the prior, but the prior would meaningless.
				_Inflections.remove(fromIdx);
			else
				// Otherwise, the prior is a consecutive singleton.
				_Inflections.set(fromIdx, new Inflection(prior.value, false, true, true));
			changed = true;
		}

		// Make sure the 'to' end works.
		if (next.value.compareTo(to) == 0 && !(next.includesBefore && next.includesValue)) {
			if (!next.includesAfter)
				_Inflections.set(toIdx, new Inflection(to, changed = true, true, next.includesAfter));
			else
				// The new version of the next would be meaningless.
				_Inflections.remove(toIdx);
			changed = true;
		} else if (!next.includesBefore) {
			if (!areConsecutive(to, next))
				_Inflections.add(toIdx, new Inflection(to, true, true, false));
			else if (next.includesAfter)
				_Inflections.remove(toIdx);
			else
				_Inflections.set(toIdx, new Inflection(next.value, true, true, false));
			changed = true;
		}

		return changed;
	}

	/** Empties this set of all contents. */
	public final void makeEmpty() {
		_Inflections.clear();
	}

	/** Makes this set a universal set. */
	public final void makeUniversal() {
		_Inflections.clear();
		_Inflections.add(new Inflection(null, true, true, true));
	}

	/** Removes the given singleton item from this set. */
	public final boolean remove(T item) {
		int priorIdx = getPrecedingInflectionIndex(item, 0);
		if (priorIdx < 0) {
			if (_Inflections.size() == 0)
				return false;
			Inflection inf = _Inflections.get(0);
			if (!_Inflections.get(0).includesBefore)
				return false;
			if (areConsecutive(item, inf)) {
				_Inflections.set(0, new Inflection(inf.value, false, true, inf.includesAfter));
				_Inflections.add(0, new Inflection(item, true, false, false));
			} else
				_Inflections.add(0, new Inflection(item, true, false, true));
			return true;
		}
		Inflection prior = _Inflections.get(priorIdx);
		if (prior.value.compareTo(item) == 0) {
			if (prior.includesValue) {
				_Inflections.set(priorIdx, new Inflection(item, prior.includesBefore, false, prior.includesAfter));
				return true;
			}
			return false;
		} else if (prior.includesAfter) {
			if (areConsecutive(prior, item)) {
				_Inflections.set(priorIdx, new Inflection(prior.value, prior.includesBefore, false, false));
				_Inflections.add(priorIdx + 1, new Inflection(item, false, false, true));
			} else
				_Inflections.add(priorIdx + 1, new Inflection(item, true, false, true));
			return true;
		}
		return false;
	}

	public final boolean remove(T from, T to) {
		throw new IllegalStateException("Not implemented yet.");
	}

	// ==============================================
	// ====== IntervalSet SET MATH ==================
	// ==============================================

	public final IntervalSet<T> union(IntervalSet<T> other) {
		throw new IllegalStateException("Have not implemented yet.");
	}

	public final IntervalSet<T> intersection(IntervalSet<T> other) {
		throw new IllegalStateException("Have not implemented yet.");
	}

	public final IntervalSet<T> subtract(IntervalSet<T> other) {
		throw new IllegalStateException("Have not implemented yet.");
	}

	public final IntervalSet<T> xor(IntervalSet<T> other) {
		throw new IllegalStateException("Have not implemented yet.");
	}

	public final IntervalSet<T> copy() {
		IntervalSet<T> result = new IntervalSet<T>();
		for (Inflection inf : _Inflections) {
			result._Inflections
					.add(new Inflection(inf.value, inf.includesBefore, inf.includesValue, inf.includesAfter));
		}
		return result;
	}

	/** Returns an empty set. */
	public static <T extends Comparable<T>> IntervalSet<T> empty() {
		return new IntervalSet<T>();
	}

	/** Returns a universal set. */
	public static <T extends Comparable<T>> IntervalSet<T> universal() {
		IntervalSet<T> ret = new IntervalSet<>();
		ret.makeUniversal();
		return ret;
	}

	private final class Inflection {
		public final T value;
		public final boolean includesBefore;
		public final boolean includesValue;
		public final boolean includesAfter;

		public Inflection(T value, boolean includeBefore, boolean includeValue, boolean includeAfter) {
			this.value = value;
			this.includesBefore = includeBefore;
			this.includesValue = includeValue;
			this.includesAfter = includeAfter;
		}

		public boolean isSingleton() {
			return includesValue && !includesBefore && !includesAfter;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object other) {
			if (other instanceof IntervalSet<?>.Inflection) {
				IntervalSet<?>.Inflection inf = (IntervalSet<?>.Inflection) other;
				return (this.value.equals(inf.value) || this.value.compareTo((T) inf.value) == 0)
						&& this.includesBefore == inf.includesBefore && this.includesValue == inf.includesValue
						&& this.includesAfter == inf.includesAfter;
			}
			return false;
		}
	}

}
