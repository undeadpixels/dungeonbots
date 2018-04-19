package com.undead_pixels.dungeon_bots.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JAboutDialog extends JDialog {

	// Based on:
	// http://www.java2s.com/Code/Java/Swing-JFC/Createsimpleaboutdialog.htm
	// 4/17/18

	public JAboutDialog(JFrame parent) {
		super(parent, "About", true);

		Box box = Box.createVerticalBox();
		box.add(Box.createGlue());
		box.add(new JLabel("DungeonBots"));
		box.add(new JLabel("Team UndeadPixels"));
		box.add(new JLabel("University of Utah"));
		box.add(new JLabel("Senior Project"));
		box.add(new JLabel("Spring 2018"));
		box.add(new JLabel("dungeonbots.herokuapp.com"));
		box.add(Box.createGlue());
		box.setOpaque(false);
		getContentPane().add(box, "Center");

		JPanel p2 = new JPanel();
		JButton bttnOK = new JButton("Ok");
		p2.add(bttnOK);
		getContentPane().add(p2, "South");

		bttnOK.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				JAboutDialog.this.setVisible(false);
			}
		});

		setSize(250, 200);
	}
}