package com.undead_pixels.dungeon_bots.file;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.undead_pixels.dungeon_bots.scene.level.LevelPack;

public class FileControl {


	private static final String[] IMAGE_EXTENSIONS = new String[] { "jpg", "png", "jpeg", "gif" };


	public static boolean isLua(File file) {
		return false;
	}


	/**
	 * Open and run a save-as file dialog. If the user cancels the process,
	 * returns null. Otherwise, returns the file selected.
	 */
	public static File saveAsDialog(Component parent) {
		return saveAsDialog(parent, System.getProperty("user.dir"));
	}


	public static File saveAsDialog(Component parent, String directory) {
		if (directory == null)
			directory = System.getProperty("user.dir");
		File workingDirectory = new File(directory);
		if (!workingDirectory.exists())
			workingDirectory = new File(System.getProperty("user.dir"));
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(workingDirectory);
		fc.setFileFilter(new DBKFileFilter());
		fc.setDialogTitle("Save your level pack file...");
		fc.setApproveButtonText("Save");

		int result = fc.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			String filename = fc.getSelectedFile().getPath();
			if (!filename.toLowerCase().endsWith("." + LevelPack.EXTENSION.toLowerCase()))
				filename += ("." + LevelPack.EXTENSION);
			File file = new File(filename);
			System.out.println("Saving: " + file.getName());
			return file;
		}
		return null;
	}


	/**
	 * Open and run a file opening dialog. If the user cancels the process,
	 * returns null. Otherwise, returns the file selected.
	 */
	public static File openPackDialog(Component parent) {

		File workingDirectory = new File(System.getProperty("user.dir"));
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(workingDirectory);
		fc.setFileFilter(new DBKFileFilter());
		fc.setDialogTitle("Choose a level pack file to open...");
		fc.setApproveButtonText("Open");

		int result = fc.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			System.out.println("Opening: " + file.getName());
			return file;
		}
		return null;
	}

	/**
	 * Open and run a file opening dialog. If the user cancels the process,
	 * returns null. Otherwise, returns the file selected.
	 */
	public static File openImageDialog(Component parent) {
		File workingDirectory = new File(System.getProperty("user.dir"));
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(workingDirectory);
		fc.setFileFilter(new ImageFileFilter());
		fc.setDialogTitle("Open an image...");
		fc.setApproveButtonText("Open");
		
		int result = fc.showOpenDialog(parent);
		if (result==JFileChooser.APPROVE_OPTION){
			File file = fc.getSelectedFile();
			System.out.println("Opening: " + file.getName());
			return file;
		}
		return null;
	}


	static class ImageFileFilter extends FileFilter {

		@Override
		public boolean accept(File arg0) {
			for (String extension : IMAGE_EXTENSIONS) {
				if (arg0.getName().toLowerCase().endsWith(extension))
					return true;
			}
			return false;
		}


		@Override
		public String getDescription() {
			return "Image files";
		}

	}


	static class DBKFileFilter extends FileFilter {


		@Override
		public boolean accept(File f) {
			// Called when a user attempts to open or save a file.
			if (f.isDirectory())
				return true;
			return (f.getName().toLowerCase().endsWith(LevelPack.EXTENSION.toLowerCase()));
		}


		@Override
		public String getDescription() {
			return "." + LevelPack.EXTENSION + " - DungeonBots level pack";
		}
	}

}
