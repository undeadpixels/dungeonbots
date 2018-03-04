package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Item;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Note;
import org.junit.Before;
import org.junit.Test;
import org.luaj.vm2.LuaValue;

import java.util.Optional;

import static org.junit.Assert.*;

public class TestItem {

	private World world;
	private Player player;
	private Item note;
	private final String NAME = "Test Item";
	private final String DESC = "This is a test";

	@Before
	public void setup() {
		world = new World();
		player = new Player(world, "player");
		note = new Note(world, NAME, DESC);
		player.getInventory().reset();
	}

	@Test
	public void testItem() {
		assertTrue(note != null);
		assertEquals(note.getName(), NAME);
		assertEquals(note.getDescription(), DESC);
	}

	@Test
	public void testItemReference() {
		player.getInventory().addItem(note);
		Optional<Integer> index = player.getInventory().findIndex(note);
		assert index.isPresent();
		ItemReference itemReference = player.getInventory().peek(LuaValue.valueOf(index.get() + 1));
		assertEquals(NAME, itemReference.getName());
		assertEquals(DESC, itemReference.getDescription());
	}
}
