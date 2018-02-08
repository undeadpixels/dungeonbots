package com.undead_pixels.dungeon_bots.scene;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import com.undead_pixels.dungeon_bots.script.annotations.UserScript;

public interface IWorld {

	ReadWriteLock getScriptsLock();

	List<UserScript> getScripts();

	
	
	ReadWriteLock getEntitiesLock();

	List<UserScript> getEntities();

}
