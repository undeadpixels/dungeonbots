package com.undead_pixels.dungeon_bots.scene;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.undead_pixels.dungeon_bots.script.interfaces.GetBindable;
import com.undead_pixels.dungeon_bots.script.interfaces.Scriptable;

public class TileType implements GetBindable {
	private final TextureRegion[] textureRegions;
	private final boolean random;
	private final String name;

	public TileType(TextureRegion[] textureRegions, String name, boolean random) {
		super();
		if(textureRegions.length < 16) {
			this.random = true;
		} else {
			this.random = random;
		}
		this.textureRegions = textureRegions;
		this.name = name;
		// TODO - make the textureRegion dependent on surrounding tiles (so walls will flow nicely together and such)
	}

	public TextureRegion getTexture(TileType left, TileType right, TileType up, TileType down) {
		if(random) {
			if(textureRegions.length == 1) {
				return textureRegions[0];
			} else {
				// TODO
				return textureRegions[0];
			}
		} else {
			int idx = (this == left  ? 1: 0)
					| (this == right ? 2: 0)
					| (this == up    ? 4: 0)
					| (this == down  ? 8: 0);

			return textureRegions[idx];
		}
	}

	@Override
	public int getId() {
		return 0;
	}

	public String getName() {
		return name;
	}
}
