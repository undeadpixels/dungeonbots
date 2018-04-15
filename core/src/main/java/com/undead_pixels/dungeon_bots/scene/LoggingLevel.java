package com.undead_pixels.dungeon_bots.scene;

import java.awt.Color;

public enum LoggingLevel {

	/**
	 * The most verbose level of output.
	 * Players can use it if they really care.
	 */
	DEBUG(new Color(192, 192, 192)),

	/**
	 * Pretty verbose stuff, especially having to do with what the world thinks.
	 */
	GENERAL(new Color(224, 224, 224)),
	
	/**
	 * Output of bots.
	 */
	STDOUT(Color.white),
	
	/**
	 * Stdout of the world, alerts, etc.
	 */
	QUEST(Color.yellow),
	
	/**
	 * For when anything goes really wrong
	 */
	ERROR(Color.red);
	
	
	/**
	 * The color that indicates this logging level
	 */
	public final Color color;
	
	
	private LoggingLevel(Color c) {
		color = c;
	}
}
