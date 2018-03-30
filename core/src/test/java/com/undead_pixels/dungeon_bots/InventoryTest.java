package com.undead_pixels.dungeon_bots;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Inventory;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Note;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class InventoryTest {

	World w;
	Player p;
	Item i;

	@Before public void setup() {
		w = new World();
		p = new Player(w, "player", 1, 1);
		i = new Note(w, "This is a test");
	}

	@Test public void testCreateInventory() {
		Inventory i = p.getInventory();
		assertFalse("Inventory should not be null", i == null);
	}

	@Test public void testAddItem() {
		p.getInventory().addItem(i);
		assertTrue("Inventory does not contain item", p.getInventory().containsItem(i));
	}

	@Test public void testFindItemIndex() {
		p.getInventory().addItem(i);
		Optional<Integer> index = p.getInventory().findIndex(i);
		assertTrue("Item should be indexed", index.isPresent());
	}

	@Test public void testRemoveItem() {
		p.getInventory().addItem(i);
		Optional<Integer> index = p.getInventory().findIndex(i);
		assert index.isPresent();
		Item getItem = p.getInventory().removeItem(index.get());
		assertEquals("Did not return the correct item", i, getItem);
		assertFalse("Inventory should no longer contain the item", p.getInventory().containsItem(i));
	}

	@Test public void testResetInventory() {
		p.getInventory().addItem(i);
		p.getInventory().reset();
		assertEquals("Inventory was not reset", 0, (int)p.getInventory().capacity());
	}
}
