package com.undead_pixels.dungeon_bots;

import java.util.HashMap;

import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;

import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

public class User {

	// The following should be serialized:
	private String _UserName;

	// The following would not be serialized:
	private SecurityLevel _SecurityLevel = SecurityLevel.AUTHOR;

	private final int _ID;

	public User(String userName) {
		_UserName = userName;
		_ID = -123456789;
	}

	public static User dummy() {
		return new User("dummy");
	}

	public static User fromJSON(String json) {

		if (json == "")
			return dummy();

		throw new UnsupportedOperationException("Not implemented yet.");
	}

	public SecurityLevel getSecurityLevel() {
		return _SecurityLevel;
	}

	/**
	 * Sets the game, and returns the user's security level associated with
	 * that.
	 */
	public SecurityLevel setCurrentGame(DungeonBotsMain game) {
		// TODO - this needs to be changed

		if (game == null)
			return _SecurityLevel = SecurityLevel.DEFAULT;
		game.setUser(this);
		// if (game.isAuthor(this))
		// return _SecurityLevel = SecurityLevel.AUTHOR;
		return _SecurityLevel = SecurityLevel.DEFAULT;
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
	
	public int getID() { return _ID;}
	

}
