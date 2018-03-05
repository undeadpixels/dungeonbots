package com.undead_pixels.dungeon_bots.ui.code_edit;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A linked list that allows removals and insertions in the middle of the list.
 * Oftentimes, it makes sense to iterate into the middle of a linked list and
 * then make changes there. This list facilitates that functionality by
 * returning references to the nodes containing date in the middle of the list.
 * 
 * ============================================================================
 * ============================================================================
 * ============================================================================
 * ============================================================================
 * Why did you reimplement a LinkedList???
 * 
 * Instead use java.util.LinkedList
 * Then call myLinkedList.listIterator()
 * Using that, you can call .remove() or .add() while iterating
 * 
 * However, for 99% of cases, literally just using an ArrayList and using its
 * add and remove methods (even though they take O(N) time) is probably good enough.
 * 
 * It's not worth spending time over unless it makes a serious performance hit.
 * Because writing all code so that none of it makes a performance hit on execution
 * means a *massive* performance hit on our productivity.
 * 
 * Much easier, and much less code to test.
 * ============================================================================
 * ============================================================================
 * ============================================================================
 * ============================================================================
 * 
 * @author Wesley Oates
 */
@Deprecated
public class DynamicLinkedList<T> implements Serializable, Iterable<T>, Collection<T> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DynamicListNode _Head;
	private DynamicListNode _Tail;
	private int _Count;

	/** The number of items contained in this list. */
	@Override
	public int size() {
		return _Count;
	}

	/**
	 * Returns the node associated with the first instance equal to the given
	 * item.
	 */
	private DynamicListNode getFirstNodeOf(T item) {
		DynamicListNode n = _Head;
		while (n != null) {
			if (n.value.equals(item))
				break;
			n = n._Next;
		}
		return n;
	}

	/** Inserts the given item at the head of the list. */
	public void insertFirst(T item) {
		if (_Count == 0) {
			_Head = _Tail = new DynamicListNode(item, null, null);
			_Count++;
		} else
			_Head.insertBefore(item);
	}

	/** Inserts the given item at the tail of the list. */
	public void insertLast(T item) {
		if (_Count == 0) {
			_Head = _Tail = new DynamicListNode(item, null, null);
			_Count++;
		} else
			_Tail.insertAfter(item);
	}

	/** Removes and returns the first item from the list. */
	public T removeFirst() {
		if (_Head == null)
			throw new NoSuchElementException();
		return _Head.remove();
	}

	/** Removes and returns the tail item from the list. */
	public T removeLast() {
		if (_Tail == null)
			throw new NoSuchElementException();
		return _Tail.remove();
	}

	public DynamicLinkedList() {
		_Head = null;
		_Tail = null;
		_Count = 0;
	}

	public class DynamicListNode {
		public final T value;
		private DynamicListNode _Prev;
		private DynamicListNode _Next;

		/** Returns the previous node. */
		public DynamicListNode getPrevious() {
			return _Prev;
		}

		/** Returns the next node. */
		public DynamicListNode getNext() {
			return _Next;
		}

		private DynamicListNode(T value, DynamicListNode previous, DynamicListNode next) {
			this.value = value;
			this._Prev = previous;
			this._Next = next;
		}

		/**
		 * Removes the current node from the list, and returns the value it
		 * contains.
		 */
		public T remove() {

			// Correct the 'next' reference for the previous node (or the list's
			// head).
			if (this._Prev != null)
				this._Prev._Next = _Next;
			else
				_Head = _Next;

			// Correct the 'previous' reference for the next node (or the list's
			// tail).
			if (this._Next != null)
				this._Next._Prev = _Prev;
			else
				_Tail = _Prev;

			// Update the list count.
			_Count--;

			return value;
		}

		/**
		 * Insert the given item into the list, immediately prior to the current
		 * node. Returns the newly-created node.
		 */
		public DynamicListNode insertBefore(T item) {

			DynamicListNode newNode = new DynamicListNode(item, _Prev, this);
			if (this._Prev != null)
				this._Prev._Next = newNode;
			else
				_Head = newNode;
			this._Prev = newNode;
			_Count++;
			return newNode;
		}

		/**
		 * Inserts the given item into the list, immediately after the current
		 * node. Returns the newly-created node.
		 */
		public DynamicListNode insertAfter(T item) {

			DynamicListNode newNode = new DynamicListNode(item, this, _Next);
			if (this._Next != null)
				this._Next._Prev = newNode;
			else
				_Tail = newNode;
			this._Next = newNode;
			_Count++;
			return newNode;
		}
	}

	
	public ListIterator<T> listIterator(int index) {
		return new ListIterator<T>() {

			private DynamicListNode _Node = _Head;

			@Override
			public void add(T newItem) {
				throw new IllegalStateException("Not implemented yet.");
			}

			@Override
			public boolean hasNext() {
				return _Node._Next != null;
			}

			@Override
			public boolean hasPrevious() {
				return _Node._Prev != null;
			}

			@Override
			public T next() {
				_Node = _Node._Next;
				if (_Node == null)
					throw new NoSuchElementException("List has been shrunk at end of iteration.");
				return _Node.value;
			}

			@Override
			public int nextIndex() {
				throw new IllegalStateException("Not implemented yet.");
			}

			@Override
			public T previous() {
				throw new IllegalStateException("Not implemented yet.");
			}

			@Override
			public int previousIndex() {
				throw new IllegalStateException("Not implemented yet.");
			}

			@Override
			public void remove() {
				throw new IllegalStateException("Not implemented yet.");
			}

			@Override
			public void set(T arg0) {
				throw new IllegalStateException("Not implemented yet.");
			}

		};
	}

	@Override
	public boolean add(T e) {
		insertLast(e);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T item : c)
			insertLast(item);
		return true;
	}

	@Override
	public void clear() {
		_Head = null;
		_Tail = null;
		_Count = 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		return getFirstNodeOf((T) o) != null;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new IllegalStateException("Not implemented yet.");
	}

	@Override
	public boolean isEmpty() {
		return _Count == 0;
	}

	@Override
	public boolean remove(Object o) {
		DynamicListNode n = getFirstNodeOf((T) o);
		if (n == null)
			return false;
		n.remove();
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new IllegalStateException("Not implemented yet.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new IllegalStateException("Not implemented yet.");
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[_Count];
		if (_Count==0) return result;
		
		DynamicListNode n = _Head;
		for (int  i = 0; i < _Count; i++){
			result[i] = n.value;
			n = n._Next;
		}
		
		return result;
	}

	@Override
	public <E> E[] toArray(E[] a) {
		throw new IllegalStateException("Not implemented yet.");
	}

	@Override
	public Iterator<T> iterator() {
		throw new IllegalStateException("Not implemented yet.");
	}

}
