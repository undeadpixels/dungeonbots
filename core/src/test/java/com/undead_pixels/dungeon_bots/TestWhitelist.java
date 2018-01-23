package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.script.Whitelist;
import com.undead_pixels.dungeon_bots.utils.builders.ActorBuilder;
import org.junit.Assert;
import org.junit.Test;

public class TestWhitelist {

	@Test
	public void testGetWhitelist() {
		Actor a = new ActorBuilder().createActor();
		Whitelist w = a.getWhitelist();
		Assert.assertTrue(w.onWhitelist("up"));
		Assert.assertTrue(w.onWhitelist("down"));
		Assert.assertTrue(w.onWhitelist("left"));
		Assert.assertTrue(w.onWhitelist("right"));
	}

	@Test
	public void testMethodNotOnWhitelist() {
		Actor a = new ActorBuilder().setWhitelist(new Whitelist().addTo("up")).createActor();
		Whitelist w = a.getWhitelist();
		Assert.assertTrue(w.onWhitelist("up"));
		Assert.assertFalse(w.onWhitelist("down"));
		Assert.assertFalse(w.onWhitelist("left"));
		Assert.assertFalse(w.onWhitelist("right"));
	}
}
