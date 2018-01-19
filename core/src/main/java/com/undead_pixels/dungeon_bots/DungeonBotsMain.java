package com.undead_pixels.dungeon_bots;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.*;

import javax.swing.*;
import javax.swing.text.rtf.RTFEditorKit;

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
import com.undead_pixels.dungeon_bots.ui.DropDownMenu;
import com.undead_pixels.dungeon_bots.ui.DropDownMenuStyle;
import com.undead_pixels.dungeon_bots.ui.code_edit.JLuaEditor;

import jsyntaxpane.syntaxkits.LuaSyntaxKit;

import com.undead_pixels.dungeon_bots.libraries.jsyntaxpane.*;
import com.undead_pixels.dungeon_bots.libraries.jsyntaxpane.syntaxkits.*;



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
		private Skin skin ;

		public NullScreen() {
			create();
		}

		public void create() {
			Gdx.input.setInputProcessor(stage);

			//Set up the skin.
			skin = new Skin(Gdx.files.internal("uiskin.json"));
			Pixmap whitePM = new Pixmap(1, 1, Format.RGBA8888);
			whitePM.setColor(Color.WHITE);
			whitePM.fill();
			Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
			skin.add("white", new Texture(whitePM));
			skin.add("defaultFont", new BitmapFont()); // Use the default font
			

			// Not sure what a table does vis-a-vis a stage...
			table.setFillParent(true);
			stage.addActor(table);
			table.setDebug(true);

			
			JMenuBar menuBar;
			JMenu menu, submenu;
			JMenuItem menuItem;
			JRadioButtonMenuItem rbMenuItem;
			JCheckBoxMenuItem cbMenuItem;
			
			menuBar = new JMenuBar();
			menu = new JMenu("AMenu");
			menu.setMnemonic(KeyEvent.VK_A);
			menu.getAccessibleContext().setAccessibleDescription("Accessible description.");
			menuBar.add(menu);
			
			JFrame frame = new JFrame();
			frame.setJMenuBar(menuBar);
			frame.setSize(800, 640);
			frame.setVisible(true);
			
			
			String initialURL = "http://www.java.com/";
			final JEditorPane ed;
			

			JLabel lblURL = new JLabel("URL");
			final JTextField txtURL = new JTextField(initialURL, 30);
			JButton btnBrowse = new JButton("Browse");

			JPanel panel = new JPanel();			
			panel.setLayout(new FlowLayout());
			panel.add(lblURL);
			panel.add(txtURL);
			panel.add(btnBrowse);

			
			JEditorPane jep = new JEditorPane();
			//LuaSyntaxKit lsk = new LuaSyntaxKit();
			//jep.setEditorKit(lsk);
			
			panel.add(jep);

			//com.undead_pixels.dungeon_bots.libraries.jsyntaxpane.syntaxkits.JavaSyntaxKit kit;
			
			//jep.setEditorKit(new com.undead_pixels.dungeon_bots.libraries.jsyntaxpane.syntaxkits.JavaSyntaxKit());
		
			
			
			

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
