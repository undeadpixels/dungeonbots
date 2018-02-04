package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Texture {
	
	private final BufferedImage img;
	
	public BufferedImage getImg() {
		return img;
	}

	public Texture(String string) throws IOException {
		img = ImageIO.read(new File(string));
	}
	
	public Texture(BufferedImage img) {
		this.img = img;
	}

}
