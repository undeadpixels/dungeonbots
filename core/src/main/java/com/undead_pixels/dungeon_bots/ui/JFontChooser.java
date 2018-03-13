package com.undead_pixels.dungeon_bots.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionListener;

public class JFontChooser {

	private JComboBox<Font> _FontChooser;
	private JComboBox<Integer> _FontSizeChooser;


	public void addFont(Font font) {
		HashMap<String, Font> existing = new HashMap<String, Font>();
		for (int i = 0; i < _FontChooser.getItemCount(); i++) {
			Font f = _FontChooser.getItemAt(i);
			existing.put(f.getName(), f);
		}
		existing.put(font.getName(), font);

		String[] sortedNames = existing.keySet().toArray(new String[existing.size()]);
		Arrays.sort(sortedNames);
		_FontChooser.removeAllItems();
		for (String name : sortedNames)
			_FontChooser.addItem(existing.get(name));
	}


	public void removeFont(Font font) {
		HashMap<String, Font> existing = new HashMap<String, Font>();
		for (int i = 0; i < _FontChooser.getItemCount(); i++) {
			Font f = _FontChooser.getItemAt(i);
			existing.put(f.getName(), f);
		}
		existing.remove(font.getName());

		String[] sortedNames = existing.keySet().toArray(new String[existing.size()]);
		Arrays.sort(sortedNames);
		_FontChooser.removeAllItems();
		for (String name : sortedNames)
			_FontChooser.addItem(existing.get(name));
	}


	public void addAllKnownFonts() {
		HashMap<String, Font> existing = new HashMap<String, Font>();
		for (int i = 0; i < _FontChooser.getItemCount(); i++) {
			Font f = _FontChooser.getItemAt(i);
			existing.put(f.getName(), f);
		}
		Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font f : allFonts)
			existing.put(f.getName(), f);

		String[] sortedNames = existing.keySet().toArray(new String[existing.size()]);
		Arrays.sort(sortedNames);
		_FontChooser.removeAllItems();
		for (String name : sortedNames)
			_FontChooser.addItem(existing.get(name));
	}


	public JFontChooser create() {

		_FontChooser = new JComboBox<Font>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
		_FontChooser.setRenderer(_FontNameRenderer);		
		_FontChooser.addActionListener(_Controller);		
		
		_FontSizeChooser = new JComboBox<Integer>(new Integer[] { 8, 10, 12, 14, 16, 18, 20, 24, 28, 36, 72 });
		_FontSizeChooser.addActionListener(_Controller);

		throw new RuntimeException("Not implemented yet.");
	}


	public void addListSelectionListener(ListSelectionListener l) {
		throw new RuntimeException("Not implemented yet.");
	}


	private static final ListCellRenderer<Font> _FontNameRenderer = new ListCellRenderer<Font>() {

		@Override
		public Component getListCellRendererComponent(JList<? extends Font> fontList, Font font, int index,
				boolean isSelected, boolean hasFocus) {
			return new JLabel(font.getName());
		}

	};

	private final ActionListener _Controller = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO throw a ListSelection event?
			throw new RuntimeException("Not implemented yet.");

		}

	};
}
