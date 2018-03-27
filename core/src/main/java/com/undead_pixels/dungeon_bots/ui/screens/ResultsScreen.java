package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;

/**
 * The screen that shows users how well they did on the challenge they were
 * given
 */
public class ResultsScreen extends Screen {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final World world;

	public ResultsScreen(World world) {
		super();
		this.world = world;
	}

	@Override
	protected ScreenController makeController() {

		return new ScreenController() {

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getActionCommand()) {
				case "OK":
					DungeonBotsMain.instance.setCurrentScreen(new MainMenuScreen());
					break;
				case "Publish":
				default:
					System.out.println("Have not implemented command: " + e.getActionCommand());
					break;
				}

			}



		};
	}

	/** Handles the rendering of stats entries. */
	private class EntryRenderer extends JLabel implements ListCellRenderer<Entry<String, Object>> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<? extends Entry<String, Object>> list,
				Entry<String, Object> item, int index, boolean isSelected, boolean cellHasFocus) {
			setText(item.getKey() + " : " + item.getValue().toString());
			return this;
		}

	}

	@Override
	protected void addComponents(Container pane) {

		// JPanel dispPanel = new JPanel();

		JPanel bttnPanel = new JPanel();
		bttnPanel.setLayout(new BoxLayout(bttnPanel, BoxLayout.LINE_AXIS));
		bttnPanel.add(UIBuilder.buildButton().text("OK").toolTip("Quit to Main Menu.").action("OK", getController())
				.create());
		bttnPanel.add(UIBuilder.buildButton().text("Publish").action("PUBLISH", getController()).create());

		Map<String, Object> endingState = this.world.getState();
		Vector<Entry<String, Object>> entries = new Vector<Entry<String, Object>>(endingState.entrySet());
		JList<Entry<String, Object>> statsList = new JList<Entry<String, Object>>(entries);
		statsList.setLayoutOrientation(JList.VERTICAL);
		statsList.setCellRenderer(new EntryRenderer());

		pane.setLayout(new BorderLayout());
		pane.add(statsList, BorderLayout.CENTER);
		pane.add(bttnPanel, BorderLayout.PAGE_END);
		this.world.reset();
	}

	@Override
	protected void setDefaultLayout() {
		this.setSize(640, 480);
		this.setLocationRelativeTo(null);
		// Image img = DungeonBotsMain.getImage("results_screen.jpg");
		Image img = UIBuilder.getImage("dungeon_room.jpg");

		if (img != null) {
			img = img.getScaledInstance(this.getSize().width, this.getSize().height, Image.SCALE_SMOOTH);

			this.setContentPane(new JLabel(new ImageIcon(img)));
		}
	}

}
