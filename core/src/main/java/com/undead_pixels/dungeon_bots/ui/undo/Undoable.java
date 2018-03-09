package com.undead_pixels.dungeon_bots.ui.undo;

import java.util.EventListener;

import javax.swing.SwingUtilities;


public abstract class Undoable<T> {

	protected final T before;
	protected final T after;


	/**Stores the given 'before' and 'after' value within the Undoable object.*/
	public Undoable(T before, T after) {
		this.before = before;
		this.after = after;
	}
	
	/**Creates a stateless Undoable, or an Undoable that relies on values supplied by a closure.*/
	public Undoable() {
		this.before = this.after = null;
	}


	

	/**Throws an exception indicating possible undo stack corruption.*/
	protected final void error() {
		throw new RuntimeException("Possible undo stack corruption.");
	}


	public final void undo(){
		if (!validateUndo()) error();
		undoValidated();
	}


	protected abstract void undoValidated();


	public final void redo(){
		if (!validateRedo()) error();
		redoValidated();
	}
	
	protected abstract void redoValidated();


	/**Used for detecting corruption of the undo stack.  If the current after-value is not equal to 
	 * this Undoable instance's 'after', then the undo stack must have been corrupted somehow.  Base 
	 * class behavior simply returns true.*/
	protected boolean validateUndo() {
		return true;
	}


	/**Used for detecting corruption of the undo stack.  If the current after-value is not equal to 
	 * this Undoable instance's 'after', then the undo stack must have been corrupted somehow.  Base 
	 * class behavior is simply returns true.*/
	protected boolean validateRedo() {
		return true;
	}
	
	
	/**Following the observer pattern, listens for Undoable changes so they can be passed on to 
	 * push onto an UndoStack object.*/
	public static abstract class Listener implements EventListener {
		public abstract void pushUndoable(Undoable<?> u);

	}


}
