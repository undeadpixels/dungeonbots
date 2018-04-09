package com.undead_pixels.dungeon_bots.scene;

import java.awt.Color;

public enum LoggingLevel {
	STDOUT(1, Color.lightGray),
	GENERAL(2, Color.white),
	QUEST(4, Color.green),
	ERROR(8, Color.red);
	
	public final Color color;
	
	private LoggingLevel(int val, Color c) {
		color = c;
	}
}
