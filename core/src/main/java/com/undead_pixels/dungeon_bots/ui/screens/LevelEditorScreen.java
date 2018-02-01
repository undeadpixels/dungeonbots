/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.undead_pixels.dungeon_bots.file.editor.GameEditorState;
import com.undead_pixels.dungeon_bots.file.editor.TileRegionSection;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.ui.JPlayerEditor;
import com.undead_pixels.dungeon_bots.ui.WorldView;

/**
 * The screen for the level editor
 * @author Wesley
 *
 */
public class LevelEditorScreen extends GDXandSwingScreen implements InputProcessor {

	/**
	 * The view
	 */
	private WorldView view;
	
	/**
	 * Current state. Used to update the world and write to file.
	 */
	private GameEditorState state;

	/**
	 * Constructor
	 */
	public LevelEditorScreen() {
		state = new GameEditorState();
		view = new WorldView(state.world);
		
		// TODO - these are 0-based, but some lua things are 1-based. Needs to be figured out.
		state.tileRegionSection.add(new TileRegionSection.TileRegion(0, 15, 0, 15, "floor"));

		Gdx.input.setInputProcessor(this);
		
		

		// super-simple swing gui on side
		Box b = new Box(BoxLayout.Y_AXIS);
		JButton printButton = new JButton("Print to lua");
		printButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(state.toLua());
			}
			
		});
		b.add(printButton);
		b.add(new JLabel("click/drag with the mouse to place walls"));
		this.addPane(b, BorderLayout.WEST);
	}


	@Override
	public void render(float dt) {
		//view.update(dt);  - don't update the world since it isn't technically running (it's just visualizing)
		view.render();
	}
	

	@Override
	public boolean keyDown(int keycode) {
		// TODO pass these to the world to handle
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO pass these to the world to handle
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO pass these to the world to handle
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector2 gameSpace = view.getScreenToGameCoords(screenX, screenY);
		
		int x = (int)gameSpace.x;
		int y = (int)gameSpace.y;
		
		state.tileRegionSection.add(new TileRegionSection.TileRegion(x, x, y, y, "wall"));
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		Vector2 gameSpace = view.getScreenToGameCoords(screenX, screenY);
		
		int x = (int)gameSpace.x;
		int y = (int)gameSpace.y;
		
		state.tileRegionSection.add(new TileRegionSection.TileRegion(x, x, y, y, "wall"));
		
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
