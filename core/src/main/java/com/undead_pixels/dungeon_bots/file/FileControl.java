package com.undead_pixels.dungeon_bots.file;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.undead_pixels.dungeon_bots.scene.level.LevelPack;

public class FileControl {

	// private static final JFileChooser _fc = new JFileChooser();


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
			File file = fc.getSelectedFile();
			System.out.println("Saving: " + file.getName());
			return file;
		}
		return null;
	}


	/**
	 * Open and run a file opening dialog. If the user cancels the process,
	 * returns null. Otherwise, returns the file selected.
	 */
	public static File openDialog(Component parent) {

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


	public static class DBKFileFilter extends FileFilter {


		@Override
		public boolean accept(File f) {
			// Called when a user attempts to open or save a file.
			if (f.isDirectory())
				return true;
			return (f.getName().toLowerCase().endsWith(LevelPack.EXTENSION));
		}


		@Override
		public String getDescription() {
			return "DungeonBots level pack";
		}
	}

}
