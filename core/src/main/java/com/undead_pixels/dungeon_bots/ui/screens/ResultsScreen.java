package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;

import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;

/**
 * The screen that shows users how well they did on the challenge they were
 * given
 */
@SuppressWarnings("serial")
public class ResultsScreen extends Screen {

	private World _World;

	public ResultsScreen(World world) {
		super(world);
		this.addWindowListener(getController());

	}

	@Override
	protected ScreenController makeController() {

		return new ScreenController() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getActionCommand()) {
				case "OK":
					DungeonBotsMain.instance.setCurrentScreen(DungeonBotsMain.ScreenType.MAIN_MENU);
					break;
				case "Publish":
				default:
					System.out.println("Have not implemented command: " + e.getActionCommand());
					break;
				}

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosed(WindowEvent e) {
				System.out.println("Closed");

			}

			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing");
				// DungeonBotsMain.instance.setCurrentScreen(new
				// MainMenuScreen());

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub

			}

		};
	}

	/** Handles the rendering of stats entries. */
	private class EntryRenderer extends JLabel implements ListCellRenderer<Entry<String, Object>> {

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

		Map<String, Object> endingState = _World.getState();
		Vector<Entry<String, Object>> entries = new Vector<Entry<String, Object>>(endingState.entrySet());
		JList<Entry<String, Object>> statsList = new JList<Entry<String, Object>>(entries);
		statsList.setLayoutOrientation(JList.VERTICAL);
		statsList.setCellRenderer(new EntryRenderer());

		pane.setLayout(new BorderLayout());
		pane.add(statsList, BorderLayout.CENTER);
		pane.add(bttnPanel, BorderLayout.PAGE_END);
		_World.reset();
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
