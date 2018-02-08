package com.undead_pixels.dungeon_bots.scene;

import java.util.EventListener;

public interface GameLoopListener extends EventListener {

	void onError(IGameLoop sender);
	
	void onPaused(IGameLoop sender);
}
