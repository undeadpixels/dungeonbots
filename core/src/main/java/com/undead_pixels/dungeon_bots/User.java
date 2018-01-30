package com.undead_pixels.dungeon_bots;

public class User {

	public User() {
		// TODO Auto-generated constructor stub
	}

	public static User dummy() {
		return new User();
	}
	
	public static User fromJSON (String json){
		
		if (json == "") return dummy();
		
		throw new UnsupportedOperationException("Not implemented yet.");
	}

}
