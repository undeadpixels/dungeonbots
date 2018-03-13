package com.undead_pixels.dungeon_bots.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.text.BadLocationException;

import com.undead_pixels.dungeon_bots.math.IntegerSet;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

/**
 * A user script associates a Lua script with the information pertaining to how
 * it will be used. For example, it will include info including the underlying
 * String representation of the script, information about the sections which are
 * or are not player-editable, and a function which can be overridden to
 * determine whether or not the script will be executed on a particular pass
 * through the game loop.
 * 
 * When we implement the onStart script, the player's onBotNear script, etc., we
 * will inherit from this and add to the entity's collection of scripts.
 */
public class UserScript implements Serializable, Comparable<UserScript> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String code;

	/** The security level at which the script can be edited. */
	public SecurityLevel level;

	/** The name for this script, like "onStartup" or "canActivate". */
	public String name;

	/** The collection of code-locked intervals. */
	public IntegerSet.Interval[] locks;
	// public ArrayList<IntegerSet.Interval> locks;

	/** If the name can be edited. */
	public boolean editableName = true;

	private UserScript() {
		this.name = "";
		this.level = SecurityLevel.DEFAULT;
		this.code = "";
		this.locks = new IntegerSet.Interval[0];
	}

	public UserScript(String name) {
		this(name, "");
	}

	public UserScript(String name, String code) {
		this(name, code, SecurityLevel.DEFAULT);
	}

	public UserScript(String name, String code, SecurityLevel level) {
		this.name = name;
		this.code = code;
		this.level = level;
		this.locks = new IntegerSet.Interval[0];
	}

	public UserScript(String name, File file) {
		this(name, file, SecurityLevel.DEFAULT);
	}
	
	public UserScript(String name, File file, SecurityLevel level) {
		this.name = name;
		this.level = level;
		this.locks = new IntegerSet.Interval[0];
		try {
			// May need to append newline to left string argument in accumulator function.
			BufferedReader reader = new BufferedReader(new FileReader(file));
			this.code = reader.lines().reduce("", (a, b) -> a + "\n" + b);
			reader.close();
		}
		catch (IOException exception) {
			// TODO: Consider changing contract of method to return an Optional<UserScript> or have it throw an exception
			this.code = "";
		}
		
	}

	/** Returns a copy of this UserScript. */
	public final UserScript copy() {
		UserScript ret = new UserScript();
		ret.name = new String(this.name);
		ret.code = new String(this.code);
		ret.level = this.level;
		ret.locks = new IntegerSet.Interval[this.locks.length];
		for (int i = 0; i < this.locks.length; i++)
			ret.locks[i] = new IntegerSet.Interval(this.locks[i].start, this.locks[i].end);
		return ret;
	}
	
	@Override
	public int compareTo(UserScript other){
		return name.compareTo(other.name);
	}

	@Deprecated
	/**
	 * Determines whether or not this user script will execute on this pass
	 * through the game loop. Can be overridden in a derived class.
	 * 
	 * TODO: in a derived class that implements an event, should check if the
	 * event's conditions have been triggered such that "canExecute" would be
	 * true.
	 */
	public boolean canExecute(World world, long time) {
		return true;
	}

	@Override
	public final String toString() {
		return "Script: " + name;
	}

	/**
	 * Sets the locked sections of this UserScript's code as indicated.
	 * 
	 * @throws BadLocationException
	 */
	public void setLocks(Iterable<IntegerSet.Interval> highlightIntervals) throws BadLocationException {
		ArrayList<IntegerSet.Interval> list = new ArrayList<IntegerSet.Interval>();
		for (IntegerSet.Interval i : highlightIntervals) {
			if (i.start < 0 || i.start > i.end || i.end >= this.code.length())
				throw new BadLocationException(
						"Lock " + i.toString() + " could not be placed in document "
								+ (new IntegerSet.Interval(0, this.code.length() - 1)).toString() + ".",
						this.code.length());
			list.add(i.copy());
		}
		this.locks = list.toArray(new IntegerSet.Interval[list.size()]);
	}
	
	@Override
	public boolean equals(Object other){
		if (other==null) return false;
		if (!(other instanceof UserScript)) return false;
		UserScript u = (UserScript)other;
		if (!u.name.equals(name)) return false;
		if (!u.code.equals(code)) return false;
		return true;
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}

}
