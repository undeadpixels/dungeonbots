package com.undead_pixels.dungeon_bots.scene;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;

public interface IGameLoop {

	public void start();
	
	public boolean isPaused();
	
	public void pause();
	
	public void resume();
	
	public void kill(Entity entity);
	
	
	
}
