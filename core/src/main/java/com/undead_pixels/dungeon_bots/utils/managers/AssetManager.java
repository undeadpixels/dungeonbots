package com.undead_pixels.dungeon_bots.utils.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AssetManager {
	private static final int DIM = 16;
	private static com.badlogic.gdx.assets.AssetManager assetManager = new com.badlogic.gdx.assets.AssetManager();

	public enum AssetSrc {
		Player("DawnLike/Characters/Player0.png"),
		Floor("DawnLike/Characters/Floor0.png"),
		Wall("DawnLike/Characters/Wall0.png");
		final String src;
		AssetSrc(String s){ src = s; }
	}

	private static Map<String, TextureRegion> textureRegionMap = new HashMap<>();

	public static void reset() {
		assetManager.clear();
		textureRegionMap.clear();
	}

	public static void loadAsset(AssetSrc src, Class<?> clz) {
		assetManager.load(src.src, clz);
	}

	public static void finishLoading() {
		assetManager.finishLoading();
	}

	public static Optional<TextureRegion> getAsset(String name, AssetSrc src, int offset_x, int offset_y) {
		try {
			if(!textureRegionMap.containsKey(name)) {
				Texture t = assetManager.get(src.src, Texture.class);
				textureRegionMap.put(name, new TextureRegion(t, DIM * offset_x, DIM * offset_y, DIM, DIM));
			}
			return Optional.ofNullable(textureRegionMap.get(name));
		}
		catch (Exception e) { return Optional.empty(); }

	}

}
