package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Item;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.ItemReference;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Key;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.util.Optional;

public class Door extends SpriteEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final TextureRegion DEFAULT_TEXTURE = AssetManager.getTextureRegion("DawnLike/Objects/Door0.png", 0, 0);

	private boolean solid = false;
	private Key key;

	public Door(World world, float x, float y) {
		super(world, "door", DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
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
				this.world,
				getName() + "key",
				String.format("A key that unlocks the door for %s", getName()));
		return this.key;
	}

	@Override
	public Boolean useItem(ItemReference itemRef) {
		Item i = itemRef.getItem();
		if(i == key) {
			assert itemRef.derefItem() != null;
			this.unlock();
			return true;
		}
		return false;
	}

	private void unlock() {
		/** Unlock this door. **/
	}

}
