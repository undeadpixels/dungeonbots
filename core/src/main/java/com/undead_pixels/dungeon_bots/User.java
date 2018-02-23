package com.undead_pixels.dungeon_bots;

import java.util.HashMap;

import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;

import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

public class User {

	// The following should be serialized:
	private String _UserName;

	
	private final int _ID;

	public User(String userName) {
		_UserName = userName;
		_ID = -123456789;
	}

	/**
	 * Constructs a User from JSON. This is the method by which a user is
	 * imported from the website database and constructed locally.
	 */
	public static User fromJSON(String json) {
		throw new RuntimeException("Not implemented yet.");
	}

	/** Returns a useless User. For testing purposes. */
	public static User dummy() {
		return new User("dummy");
	}

	

	/** Returns the username for this user. */
	public String getUserName() {
		return _UserName;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other instanceof User) {
			User u = (User) other;
			return _ID == u._ID;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return _UserName.hashCode();
	}

	public int getID() {
		return _ID;
	}

}
