package com.undead_pixels.dungeon_bots.scene;


import java.util.function.BiFunction;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;

/**For templating newly-placed Entities as they are added into the world.*/
public class EntityType {

	public final String name;
	public final TextureRegion previewTexture;
	public final BiFunction<Float, Float, Entity> entitySupplier;


	public EntityType(String name, TextureRegion texture, BiFunction<Float, Float, Entity> entitySupplier) {
		this.name = name;
		this.previewTexture = texture;
		this.entitySupplier = entitySupplier;
	}

	public Entity get(float x, float y) {
		return entitySupplier.apply(x, y);
	}

}
