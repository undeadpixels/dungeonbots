package com.undead_pixels.dungeon_bots.file;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

public class FileControl {

	private static final JFileChooser _fc = new JFileChooser();

	
	public static boolean isLua(File file){
		return false;
	}
	
	/**
	 * Open and run a save-as file dialog. If the user cancels the process,
	 * returns null. Otherwise, returns the file selected.
	 */
	public static File saveAsDialog(Component parent) {
		// The file chooser should start at the current working directory. 2
		// lines pulled from
		// https://stackoverflow.com/questions/21534515/jfilechooser-open-in-current-directory
		File workingDirectory = new File(System.getProperty("user.dir"));
		_fc.setCurrentDirectory(workingDirectory);
		
		int result = _fc.showSaveDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = _fc.getSelectedFile();
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
		_fc.setCurrentDirectory(workingDirectory);

		int result = _fc.showOpenDialog(parent);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = _fc.getSelectedFile();
			System.out.println("Opening: " + file.getName());
			return file;
		}
		return null;
	}

}
