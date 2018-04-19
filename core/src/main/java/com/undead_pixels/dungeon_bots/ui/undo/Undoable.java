package com.undead_pixels.dungeon_bots.ui.undo;

import java.util.EventListener;


public abstract class Undoable<T> {

	protected final T before;
	protected final T after;
	protected final Object context;


	/**Stores the given 'before' and 'after' value within the Undoable object.*/
	public Undoable(T before, T after) {
		this.before = before;
		this.after = after;
		context = null;
	}


	public Undoable(T before, T after, Object... context) {
		this.before = before;
		this.after = after;
		this.context = context;
	}


	public Undoable(T before, T after, Object context) {
		this.before = before;
		this.after = after;
		this.context = context;
	}


	/**Throws an exception indicating possible undo stack corruption.*/
	protected final void error() {
		throw new RuntimeException("Possible undo stack corruption.");
	}


	public final void undo() {
		if (!okayToUndo())
			error();
		undoValidated();
	}


	protected abstract void undoValidated();


	public final void redo() {
		if (!okayToRedo())
			error();
		redoValidated();
	}


	protected abstract void redoValidated();


	/**Used for detecting corruption of the undo stack.  If the current after-value is not equal to 
	 * this Undoable instance's 'after', then the undo stack must have been corrupted somehow.  Base 
	 * class behavior simply returns true.*/
	protected boolean okayToUndo() {
		return true;
	}


	/**Used for detecting corruption of the undo stack.  If the current before-value is not equal to 
	 * this Undoable instance's 'before', then the undo stack must have been corrupted somehow.  Base 
	 * class behavior is simply returns true.*/
	protected boolean okayToRedo() {
		return true;
	}


	/**Following the observer pattern, listens for Undoable changes so they can be passed on to 
	 * push onto an UndoStack object.*/
	public static abstract class Listener implements EventListener {

		public abstract void pushUndoable(Undoable<?> u);

	}


}
