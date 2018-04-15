package com.undead_pixels.dungeon_bots.utils.managers;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.undead_pixels.dungeon_bots.nogdx.Texture;
import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;

public class AssetManager {
	private static final int DIM = 16;

	private static Map<String, Texture> textureMap = new HashMap<>();

	public static void reset() {
		textureMap.clear();
	}

	public static TextureRegion getTextureRegion(String filepath, int offset_x, int offset_y) {
		Texture t = getTexture(filepath);
		return new TextureRegion(t, DIM * offset_x, DIM * offset_y, DIM, DIM);

	}

	public static Texture getTexture(String filepath) {
		try {
			Texture ret = textureMap.get(filepath);
			if(ret == null) {
				ret = new Texture(filepath);
				textureMap.put(filepath, ret);
			}
			return ret;
		} catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	public static Dimension biggestWithinMaintainingAspect(Dimension original, Dimension newDim) {
		Dimension dimx = new Dimension(newDim.width, newDim.width/original.width * original.height);
		Dimension dimy = new Dimension(newDim.height/original.height * original.width, newDim.height);
		
		if(dimx.width < dimy.height) {
			return dimx;
		} else {
			return dimy;
		}
	}

}
