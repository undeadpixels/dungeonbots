package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.util.ArrayList;

/**
 * A data set that determines whether an integer is included in a set or not, by
 * only storing the inflection points.
 * @author Wesley
 */
public class InclusionMap {

	// Presumed to be non-inclusive until the first inflection is hit.

	private static class Inclusion {
		public int from;
		public int to;

		public Inclusion(int from, int to) {
			this.from = from;
			this.to = to;
		}

		public boolean brackets(int item) {
			return from <= item && item <= to;
		}

		public boolean brackets(Inclusion other) {
			return from <= other.from && other.to <= to;
		}

		public boolean overlaps(Inclusion other) {
			if (brackets(other))
				return true;
			if (other.brackets(this))
				return true;
			if (brackets(other.from))
				return true;
			if (brackets(other.to))
				return true;
			if (other.brackets(from))
				return true;
			if (other.brackets(to))
				return true;
			return false;
		}

		public boolean consecutive(Inclusion other) {
			return (this.to == other.from - 1) || (other.to + 1 == this.from);
		}
	}

	private ArrayList<Inclusion> _Inclusions;

	/** Factory method for creating a new, empty InclusionMap. */
	public static InclusionMap empty() {
		return new InclusionMap();
	}

	/**
	 * Factory method for creating a new, full InclusionMap from the given
	 * values.
	 */
	public static InclusionMap full() {
		InclusionMap result = new InclusionMap();
		result._Inclusions.add(new Inclusion(Integer.MIN_VALUE, Integer.MAX_VALUE));
		return result;
	}

	private InclusionMap() {
		_Inclusions = new ArrayList<Inclusion>();
	}

	public InclusionMap(int from, int to) {
		this();
		_Inclusions.add(new Inclusion(from, to));
	}

	/**
	 * Ensure that no value from the given 'from' to the given 'to', inclusive,
	 * are included.
	 */
	public void exclude(int from, int to) {

		Inclusion removal = new Inclusion(from, to);
		int i = _Inclusions.size() - 1;

		// Proceed back from the end until changes start.
		while (i >= 0) {
			Inclusion focus = _Inclusions.get(i);
			if (removal.brackets(focus)) {
				_Inclusions.remove(i--);
				break;
			} else if (removal.overlaps(focus)) {
				if (removal.brackets(focus.to)) {
					focus.to = removal.from - 1;
					if (focus.to < focus.from)
						_Inclusions.remove(i);
					return;
				} else {
					focus.from = removal.to + 1;
					if (focus.to < focus.from)
						_Inclusions.remove(i);
					i--;
					break;
				}
			}
			i--;
		}

		// Remove inclusions as long as they fall within the removal range.
		while (i >= 0) {
			Inclusion focus = _Inclusions.get(i);
			if (removal.brackets(focus))
				_Inclusions.remove(i);
			else if (removal.overlaps(focus)) {
				focus.to = removal.from - 1;
				if (focus.to < removal.from)
					return;
				break;
			}
		}
	}

	/**
	 * Returns the beginning point of a set of included integers. The index
	 * given will be the 0-based index of the set.
	 */
	public int getStart(int index) {
		return _Inclusions.get(index).from;
	}

	/**
	 * Returns the ending point of a set of included integers. The index given
	 * will be the 0-based index of the set.
	 */
	public int getEnd(int index) {
		return _Inclusions.get(index).to;
	}

	/**
	 * Ensure that everything from the given 'from' to the given 'to',
	 * inclusive, are included.
	 */
	public void include(int from, int to) {

		// Edge case - no existing inclusions.
		if (_Inclusions.size() == 0) {
			_Inclusions.add(new Inclusion(from, to));
			return;
		}

		// Edge case - precedes the existing inclusions.
		if (to < _Inclusions.get(0).from) {
			if (to == _Inclusions.get(0).from - 1)
				_Inclusions.get(0).from = from;
			else
				_Inclusions.add(0, new Inclusion(from, to));
			return;
		}

		// Find the start matching inclusion among those existing.
		Inclusion compare = new Inclusion(from, to);
		int startIdx = -1;
		if (from < _Inclusions.get(0).from)
			startIdx = 0;
		else {
			for (int i = 0; i < _Inclusions.size(); i++) {
				Inclusion existing = _Inclusions.get(i);
				if (compare.overlaps(existing) || compare.consecutive(existing)) {
					startIdx = i;
					break;
				} else if (existing.to < from) {
					startIdx = i + 1;
					break;
				}
			}
		}

		// Edge case - falls after all the existing.
		if (startIdx == -1) {
			_Inclusions.add(new Inclusion(from, to));
			return;
		}

		// Find the end matching inclusion among those existing.
		int endIdx;
		for (endIdx = startIdx; endIdx < _Inclusions.size() - 1; endIdx++) {
			Inclusion existing = _Inclusions.get(endIdx + 1);
			if (compare.overlaps(existing) || compare.consecutive(existing))
				break;
			if (to < existing.from)
				break;
		}

		// Okay, have a startIdx, and an endIdx. Save the startIdx Inclusion,
		// modify it to include the whole range, and remove everything that's
		// been swallowed up by the new inclusion.
		Inclusion focus = _Inclusions.get(startIdx);
		focus.from = Math.min(from, focus.from);
		focus.to = Math.max(to, _Inclusions.get(endIdx).to);
		while (endIdx > startIdx)
			_Inclusions.remove(endIdx--);
	}

	/**
	 * Returns whether the given integer is included. This is an O(log n)
	 * operation.
	 */
	public boolean isIncluded(int item) {
		// Use binary search to find which inclusion matches the item.
		if (_Inclusions.size() == 0 || item < _Inclusions.get(0).from)
			return false;
		int fromIdx = 0, toIdx = _Inclusions.size();
		do {
			int idx = fromIdx + ((toIdx - fromIdx) / 2);
			Inclusion focus = _Inclusions.get(idx);
			if (focus.brackets(item))
				return true;
			else if (item < focus.from)
				toIdx = idx;
			else
				// item > focus.to
				fromIdx = idx + 1;

		} while (fromIdx != toIdx);
		return false;
	}

}