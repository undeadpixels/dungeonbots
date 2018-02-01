package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.badlogic.gdx.Gdx;
import com.undead_pixels.dungeon_bots.DungeonBotsMain;

/**
 * The menu where users select Play, Create, or Community
 */
public class MainMenuScreen extends GDXandSwingScreen {
	
	public MainMenuScreen() {
		// TODO - do all of this in GDX graphics instead of Swing
		
		
		Box b = new Box(BoxLayout.Y_AXIS);

		JButton play = new JButton("Play");
		JButton create = new JButton("Create");
		JButton community = new JButton("Community");

		create.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Gdx.app.postRunnable(new Runnable() {

					@Override
					public void run() {
						DungeonBotsMain.instance.setScreen(new LevelEditorScreen());
						
					}
					
				});
			}
			
		});

		play.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Gdx.app.postRunnable(new Runnable() {

					@Override
					public void run() {
						DungeonBotsMain.instance.setScreen(new GameplayScreen());
						
					}
					
				});
			}
			
		});

		b.add(play);
		b.add(create);
		b.add(community);

		this.addPane(b, BorderLayout.WEST);

		Box infoPane = new Box(BoxLayout.Y_AXIS);
		infoPane.add(new JLabel("The default view has changed!"));
		infoPane.add(new JLabel("Click on 'Play' on the left to get to the main play screen"));
		infoPane.add(new JLabel("Or click on 'Create' to open the editor"));
		infoPane.add(new JLabel("Also, this needs to be prittified. A lot."));
		
		this.addPane(infoPane, BorderLayout.EAST);
	}
}
