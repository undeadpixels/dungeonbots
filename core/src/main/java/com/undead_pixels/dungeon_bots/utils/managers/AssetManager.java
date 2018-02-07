package com.undead_pixels.dungeon_bots.utils.managers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.undead_pixels.dungeon_bots.nogdx.Texture;
import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;

/**TODO:  I can't find any references to this - is it being used?*/
public class AssetManager {
	private static final int DIM = 16;

	public enum AssetSrc {
		Player("DawnLike/Characters/Player0.png"),
		Floor("DawnLike/Characters/Floor0.png"),
		Wall("DawnLike/Characters/Wall0.png");
		final String src;
		AssetSrc(String s){ src = s; }
	}

	private static Map<String, TextureRegion> textureRegionMap = new HashMap<>();

	public static void reset() {
		textureRegionMap.clear();
	}

	public static void loadAsset(AssetSrc src, Class<?> clz) {
		// TODO assetManager.load(src.src, clz);
	}

	public static void finishLoading() {
		// TODO assetManager.finishLoading();
	}

	public static Optional<TextureRegion> getAsset(String name, AssetSrc src, int offset_x, int offset_y) {
		try {
			if(!textureRegionMap.containsKey(name)) {
				// TODO - actually cache these
				Texture t = new Texture(src.src);
				textureRegionMap.put(name, new TextureRegion(t, DIM * offset_x, DIM * offset_y, DIM, DIM));
			}
			return Optional.ofNullable(textureRegionMap.get(name));
		}
		catch (Exception e) { return Optional.empty(); }

	}

	public static Texture getTexture(String string) {
		try {
			return new Texture(string); // TODO - caching
		} catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
