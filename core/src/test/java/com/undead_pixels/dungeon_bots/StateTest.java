package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class StateTest {

	@Test
	public void testPlayerState() {
		World w = new World();
		Player p = new Player(w,"player");
		w.setPlayer(p);
		Map<String,Object> worldState = w.getState();
		Object o = worldState.get("steps");
		Assert.assertEquals(o, Integer.toString(0));
	}
}
