package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

public class User {

	// The following should be serialized:
	private String _UserName;

	// The following would not be serialized:
	private SecurityLevel _SecurityLevel = SecurityLevel.AUTHOR;
	private DungeonBotsMain _CurrentGame = null;

	public User(String userName) {
		_UserName  = userName;
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
		if (game == _CurrentGame)
			return _SecurityLevel;
		_CurrentGame = game;
		if (game == null)
			return _SecurityLevel = SecurityLevel.DEFAULT;
		game.setUser(this);
		if (game.isAuthor(this))
			return _SecurityLevel = SecurityLevel.AUTHOR;
		return _SecurityLevel = _SecurityLevel.DEFAULT;
	}

	/** Returns the username for this user. */
	public Object getUserName() {
		return _UserName;
	}

}
