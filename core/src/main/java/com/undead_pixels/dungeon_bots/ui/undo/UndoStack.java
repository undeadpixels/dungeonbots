package com.undead_pixels.dungeon_bots.ui.undo;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**A stack which manages Undoable arguments for both an undo and redo stack.  To get the next 
 * argument for undo, call pop().  To get the next argument for redo, call unpop().  This class 
 * is designed to be thread-safe, and Undoable arguments will be stored in the order they are 
 * received.
 * @author Wesley Oates
 */
public class UndoStack {

	private static int DEFAULT_CAPACITY = 100;
	private int _Capacity = DEFAULT_CAPACITY;
	private final LinkedList<Undoable<?>> _Undoables = new LinkedList<Undoable<?>>();
	private final LinkedList<Undoable<?>> _Redoables = new LinkedList<Undoable<?>>();
	private final ReentrantLock _Lock = new ReentrantLock();


	/**Returns the size of the stack, including all Undoable items stacked for undo and all stacked 
	 * for redo.*/
	public int size() {
		_Lock.lock();
		try {
			return _Undoables.size() + _Redoables.size();
		} finally {
			_Lock.unlock();
		}

	}


	/**Returns the capacity of the undo stack.*/
	public int getCapacity() {
		_Lock.lock();
		try {
			return _Capacity;
		} finally {
			_Lock.unlock();
		}
	}


	/**Sets the capacity of the undo stack as indicated.  If a reduced capacity size would exclude items 
	 * in the stack, Undoable items on the undo stack are cut off before items on the redo stack.*/
	public void setCapacity(int c) {
		_Lock.lock();
		try {
			_Capacity = c;
			while (_Undoables.size() > 0 && (_Undoables.size() + _Redoables.size()) > c) {
				_Undoables.removeFirst();
			}
			while (_Redoables.size() > c)
				_Redoables.removeLast();
		} finally {
			_Lock.unlock();
		}

	}


	/**Add an Undoable to the undo stack.  Doing so will clear the redo stack.*/
	public void push(Undoable<?> u) {
		if (u == null)
			throw new RuntimeException("A null undoable cannot be pushed on the stack.");
		_Lock.lock();
		try {
			_Undoables.addLast(u);
			_Redoables.clear();
			while (_Undoables.size() > _Capacity)
				_Undoables.removeFirst();
		} finally {
			_Lock.unlock();
		}

	}


	/**Returns the next Undoable on the stack (which will be added to the internal redo stack for later redo() ).
	 * Use this function to find the next action to undo.  If no such item for undo is present, returns null.*/
	public Undoable<?> nextUndo() {

		_Lock.lock();
		try {
			if (_Undoables.size() == 0)
				return null;
			Undoable<?> u = _Undoables.removeLast();
			_Redoables.addFirst(u);
			return u;
		} finally {
			_Lock.unlock();
		}

	}


	/** Returns the next Undoable on the internal redo stack (which will be added to the undo stack for later undo() ).
	 * Use this function to find the next action to redo.  If no such item for redo is present, returns null.*/
	public Undoable<?> nextRedo() {
		if (_Redoables.size() == 0)
			return null;
		_Lock.lock();
		try {
			Undoable<?> u = _Redoables.removeFirst();
			_Undoables.addLast(u);
			return u;
		} finally {
			_Lock.unlock();
		}

	}


	/**Returns the next Undoable item for undo, if one exists.  If not, returns null.*/
	public Undoable<?> peekUndo() {
		_Lock.lock();
		try {
			return _Undoables.peekLast();
		} finally {
			_Lock.unlock();
		}

	}


	/**Returns the next Undoable item for redo, if one exists.  If not, returns null.*/
	public Undoable<?> peekRedo() {
		_Lock.lock();
		try {
			return _Redoables.peekFirst();
		} finally {
			_Lock.unlock();
		}

	}
}
