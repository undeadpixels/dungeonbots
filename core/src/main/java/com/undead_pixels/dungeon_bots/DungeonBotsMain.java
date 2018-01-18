package com.undead_pixels.dungeon_bots;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.*;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pool;

/**
 * The main class. Basically, all it does is point to the screen that we are
 * actually trying to render.
 *
 */
public class DungeonBotsMain extends Game {

	/**
	 * Singleton instance
	 */
	public static final DungeonBotsMain instance = new DungeonBotsMain();

	/**
	 * private constructor for singleton
	 */
	private DungeonBotsMain() {

	}

	@Override
	public void create() {
		setScreen(new NullScreen());
	}

	
	/**
	 * This will be deleted eventually, but it at least allows us to have a fake
	 * screen
	 */
	public static class NullScreen extends ScreenAdapter {

		SpriteBatch batch = new SpriteBatch();
		Texture img = new Texture("badlogic.jpg");

		private Stage stage = new Stage();
		private Table table = new Table();
		private Skin skin = new Skin();

		public NullScreen() {
			create();
		}

		public void create() {
			Gdx.input.setInputProcessor(stage);

			
			

			// Not sure what a table does vis-a-vis a stage...
			table.setFillParent(true);
			stage.addActor(table);
			table.setDebug(true);


			//Create and add a menu.			
			MenuStyle menuStyle = new MenuStyle();
			menuStyle.columnWidth=20;
			Menu menu = new Menu(menuStyle);
			table.add(menu);
			
			menu.addItem("", "File", null, null);
			menu.addItem("File",  "Open", null, null);
			menu.addItem("Path",  "Save",  null, null);

		}

		@Override
		public void render(float delta) {
			Gdx.gl.glClearColor(1, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			batch.draw(img, 0, 0);
			batch.end();

			stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
			stage.draw();
		}

		@Override
		public void dispose() {
			batch.dispose();
			img.dispose();
		}

	}
}
