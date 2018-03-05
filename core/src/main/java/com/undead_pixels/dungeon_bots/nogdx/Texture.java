package com.undead_pixels.dungeon_bots.nogdx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

/**
 * A pretty raw wrapper around BufferedImage.
 * 
 * May be unwrapped at some later point
 */
public class Texture implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Internal backing image
	 */
	private transient BufferedImage img;
	
	/**
	 * @return	The image this texture represents
	 */
	public BufferedImage getImg() {
		return img;
	}

	/**
	 * Constructor
	 * 
	 * @param string			A file path
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

	/**
	 * Called upon deserialization.
	 * Used to load this texture from that data stream
	 * 
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		img = ImageIO.read(inputStream);
	}

	/**
	 * Called upon serialization.
	 * Used to save this texture to that data stream
	 * 
	 * @param outputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void writeObject(ObjectOutputStream outputStream) throws IOException, ClassNotFoundException {
		outputStream.defaultWriteObject();
		ImageIO.write(img, "png", outputStream); // TODO - handle typical images with file path?
	}
}
