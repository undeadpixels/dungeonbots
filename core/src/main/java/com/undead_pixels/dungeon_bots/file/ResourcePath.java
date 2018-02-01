package com.undead_pixels.dungeon_bots.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * Some magical class that will handle file IO, regardless of whether it's from the assets/ folder, from a zip file (level pack), or from online (if we do that)
 * 
 * Might be deleted and replaced with something else, such as gdx's AssetManager or something
 */
public class ResourcePath {
	public ResourcePath(String path) {
		
	}
	
	public String readAll() throws IOException {
		// TODO
		return "";
	}
	
	public void writeAll(String str) throws IOException {
		
	}
	
	public InputStream getInputStream() throws IOException {
		throw new IOException("Not yet implemented");
	}
}
