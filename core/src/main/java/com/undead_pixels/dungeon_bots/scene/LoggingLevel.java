package com.undead_pixels.dungeon_bots.scene;

import java.awt.Color;

public enum LoggingLevel {

	DEBUG(Color.darkGray),
	STDOUT(Color.lightGray),
	GENERAL(Color.white),
	QUEST(Color.yellow),
	ERROR(Color.red);
	
	public final Color color;
	
	private LoggingLevel(Color c) {
		color = c;
	}
}
