package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

import java.awt.Image;
import java.util.Optional;

/**Ensures that implements can return an image.*/
public interface HasImage {

	/**
	 * @return	An image associated with this object.
	 */
	default Image getImage() {
		return AssetManager.getTextureRegion("DawnLike/Objects/Decor0.png", 0, 12).toImage();
	}
}
