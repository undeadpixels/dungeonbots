package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.undead_pixels.dungeon_bots.DungeonBotsMain;

/**
 * The menu where users select Play, Create, or Community
 */
public class MainMenuScreen extends GDXandSwingScreen {
	
	public MainMenuScreen() {
		Box b = new Box(BoxLayout.Y_AXIS);

		JButton play = new JButton("Play");
		JButton create = new JButton("Create");
		JButton community = new JButton("Community");
		
		play.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DungeonBotsMain.instance.setScreen(new GameView());
			}
			
		});

		b.add(play);
		b.add(create);
		b.add(community);
		
		this.addPane(b, BorderLayout.EAST);
	}

}
