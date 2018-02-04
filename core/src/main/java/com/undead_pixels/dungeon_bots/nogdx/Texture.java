package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * A pretty raw wrapper around BufferedImage.
 * 
 * May be unwrapped at some later point
 */
public class Texture {
	
	/**
	 * Internal backing image
	 */
	private final BufferedImage img;
	
	/**
	 * @return	The image this texture represents
	 */
	public BufferedImage getImg() {
		return img;
	}

	/**
	 * Constructor
	 * 
	 * @param string		A file path
	 * @throws IOException	If the image cannot be loaded
	 */
	public Texture(String string) throws IOException {
		img = ImageIO.read(new File(string));
	}
	
	/**
	 * Constructor
	 * 
	 * @param img	The image to use
	 */
	public Texture(BufferedImage img) {
		this.img = img;
	}

}
