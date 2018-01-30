/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JPanel;

/**
 * @author Wesley
 *
 */
public class JCodeEditor extends JPanel {

	public JCodeEditor() {

	}

	public static class Range<T extends Comparable<T>> {
		private List<Inflection> _Inflections;

		public T start() {
			if (_Inflections.size() < 2)
				return null;
			return _Inflections.get(0).point;
		}

		public T end() {
			if (_Inflections.size() < 2)
				return null;
			return _Inflections.get(_Inflections.size() - 1).point;
		}

		private Range() {
			_Inflections = new ArrayList<Inflection>();
		}

		public Range(T from, T to) {
			this();
			if (from.compareTo(to) <= 0) {
				_Inflections.add(new Inflection(from, false, true, true));
				_Inflections.add(new Inflection(from, true, true, false));
			}
		}

		private Range(List<Inflection> inflections) {
			_Inflections = inflections;
		}

		public boolean isEmpty() {
			return _Inflections.size() < 2;
		}

		public Range<T> empty() {
			return new Range<T>();
		}

		public Range<T> copy() {
			return new Range<T>(new ArrayList<Inflection>(_Inflections));
		}

		public final Range<T> subtract(Range<T> other) {

			if (isEmpty())
				return empty();
			if (other.isEmpty())
				return new Range<T>(new ArrayList<Inflection>(_Inflections));

			int idxThis = 0, idxOther = 0;
			List<Inflection> newInflections = new ArrayList<Inflection>();

			// The inclusion status starts out as whether the first inflections
			// include before.
			boolean including = _Inflections.get(0).includeBefore && !_Inflections.get(0).includeBefore;
			while (true) {
				Inflection newInflection = new Inflection();

				// What's the comparison?
				int c = setSubtraction(_Inflections.get(idxThis), _Inflections.get(idxOther), newInflection);

				// I'm not sure if it's possible for the new inflection to have
				// a different "before" status than the
				// inflection status.
				if (newInflection.includeBefore != including)
					throw new IllegalStateException("This shouldn't ever happen.");

				// Add the new inflection point, but only if it's not redundant
				// for the current inflection status.
				if (including && !newInflection.isUniversal())
					newInflections.add(newInflection);
				else if (!including && !newInflection.isEmpty())
					newInflections.add(newInflection);
				including = newInflection.includeAfter;

				// Advance forward the index of the smaller compared inflection.
				if (c < 0) {
					if (idxThis < _Inflections.size())
						idxThis++;
					else if (idxOther < other._Inflections.size())
						idxOther++;
					else
						break;
				} else if (c > 0) {
					if (idxOther < other._Inflections.size())
						idxOther++;
					else if (idxThis < _Inflections.size())
						idxThis++;
					else
						break;
				}
			}

			return new Range<T>(newInflections);

		}

		public final Range<T> subtract(T item) {

			if (isEmpty())
				return empty();

			int precedingIdx = getPrecedingIndex(item);
			if (precedingIdx < 0) {
				if (!_Inflections.get(0).includeBefore)
					return copy();
				List<Inflection> newInfs = new ArrayList<Inflection>();
				newInfs.add(new Inflection(item, true, false, true));
				for (Inflection inf : _Inflections)
					newInfs.add(inf);
				return new Range<T>(newInfs);
			}

			Inflection p = _Inflections.get(precedingIdx);
			if (p.point.compareTo(item) == 0) {
				if (!p.includeThis)
					return copy();
				List<Inflection> newInfs = new ArrayList<Inflection>();
				for (int i = 0; i < precedingIdx; i++)
					newInfs.add(_Inflections.get(i));
				Inflection inf = new Inflection(item, p.includeBefore, false, p.includeAfter);
				if (!inf.isEmpty())
					newInfs.add(inf);
				for (int i = precedingIdx + 1; i < _Inflections.size(); i++)
					newInfs.add(_Inflections.get(i));
				return new Range<T>(newInfs);
			}

			if (p.includeAfter) {
				List<Inflection> newInfs = new ArrayList<Inflection>();
				for (int i = 0; i <= precedingIdx; i++)
					newInfs.add(_Inflections.get(i));
				newInfs.add(new Inflection(item, true, false, true));
				for (int i = precedingIdx + 1; i < _Inflections.size(); i++)
					newInfs.add(_Inflections.get(i));
				return new Range<T>(newInfs);
			}

			return copy();
		}

		private int setSubtraction(Inflection infA, Inflection infB, Inflection result) {
			
		}

		public final Range<T> or(Range<T> other) {

			if (isEmpty())
				return other.copy();
			if (other.isEmpty())
				return copy();
			
			

		}

		/**
		 * Finds the index of the inflection equal to or preceding the given
		 * item.
		 */
		private int getPrecedingIndex(T item) {

			int idx = 0;
			while (idx < _Inflections.size()) {
				Inflection inf = _Inflections.get(idx);
				if (inf.point.compareTo(item) > 0) {
					idx--;
					break;
				}
				idx++;
			}
			return idx;
		}

		/**
		 * An object representing a transition point from inclusion to
		 * exclusion, or vice versa. The object should not be mutable outside
		 * the Range class.
		 */
		private class Inflection {
			T point;
			boolean includeBefore;
			boolean includeThis;
			boolean includeAfter;

			/***/
			public Inflection() {
			}

			public Inflection(T point, boolean includeBefore, boolean includeThis, boolean includeAfter) {
				this.point = point;
				this.includeBefore = includeBefore;
				this.includeThis = includeThis;
				this.includeAfter = includeAfter;
			}

			/**
			 * If this inflection represents exclusion of everything before and
			 * after, as well as the point itself, it's not a real inflection
			 * point.
			 */
			public boolean isEmpty() {
				return !includeBefore && !includeThis && !includeAfter;
			}

			/** Represents the inflection point of a single item. */
			public boolean isSoliton() {
				return !includeBefore && includeThis && !includeAfter;
			}

			/**
			 * If this inflection represents inclusion of everything before and
			 * after, as well as the point itself, it's not a real inflection
			 * point.
			 */
			public boolean isUniversal() {
				return includeBefore && includeThis && includeAfter;
			}
			
			public Inflection setSubtraction(Inflection ai, Inflection b){
				int c = infA.point.compareTo(infB.point);
				if (c < 0) {
					result.point = infA.point;
					result.includeBefore = infA.includeBefore && !infB.includeBefore;
					result.includeThis = infA.includeThis && !infB.includeBefore;
					result.includeAfter = infA.includeAfter && !infB.includeBefore;
				} else if (c > 0) {
					result.point = infB.point;
					result.includeBefore = infA.includeAfter && !infB.includeBefore;
					result.includeThis = infA.includeAfter && !infB.includeThis;
					result.includeAfter = infA.includeAfter && !infB.includeAfter;
				} else {
					result.point = infA.point;
					result.includeBefore = infA.includeBefore && !infB.includeBefore;
					result.includeThis = infA.includeThis && !infB.includeThis;
					result.includeAfter = infA.includeAfter && !infB.includeAfter;
				}
				return c;
			}
		}

	}

}
