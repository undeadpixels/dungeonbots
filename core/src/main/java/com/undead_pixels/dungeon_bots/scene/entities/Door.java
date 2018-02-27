package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Item;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.Key;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;

import java.util.Optional;

public class Door extends SpriteEntity {

	private boolean solid = false;
	private Key key;

	public Door(World world, TextureRegion tex) {
		super(world, "door", tex);
	}

	@Override
	public boolean isSolid() {
		return this.solid;
	}

	@Override
	public float getZ() {
		return 10;
	}

	@Bind Key genKey() {
		this.key = new Key(
				getName() + "key",
				String.format("A key that unlocks the door for %s", getName()));
		return this.key;
	}

	@Override
	public Boolean useItem(ItemReference itemRef) {
		Optional<Item> item = itemRef.getItem();
		return item.map(i -> {
			if(i == key) {
				itemRef.derefItem();
				return true;
			}
			return false;
		}).orElse(false);
	}

}
