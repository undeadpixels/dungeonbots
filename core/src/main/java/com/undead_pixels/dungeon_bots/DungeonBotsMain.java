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
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

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
	 * A Menu participates in layout. Being a Table, it can contain other
	 * actors.
	 * 
	 * @author Wesley Oates
	 */
	public class Menu extends Table {

		/*** Menu tree structure. */
		MenuNode _Root = new MenuNode(null);

		/** A Menu has its own stage so it can prioritize input handling. */
		private Stage _Stage = new Stage();
		private Skin _Skin;

		private class MenuNode {

			/** True if the MenuNode is open, false if not. */
			public boolean IsOpen = false;
			private Object _Contents;
			private Hashtable<Object, MenuNode> _SubNodes = null;
			private Function<Object, Boolean> _EnabledTester;
			private Consumer<Object> _Executor;

			public MenuNode(Object contents) {
				this(contents, obj -> true, mn -> System.out.println(contents.toString()));
			}

			public MenuNode(Object contents, Function<Object, Boolean> enabledTester, Consumer<Object> executor) {
				_Contents = contents;
				_EnabledTester = enabledTester;
				_Executor = executor;
			}

			/**
			 * Two nodes compared are equal if their contents are equal. For
			 * other types, a node is equal to another object if its contents
			 * are equal to that object.
			 */
			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (other instanceof MenuNode) {
					final MenuNode that = (MenuNode) other;
					return this._Contents.equals(that._Contents);
				}
				return this._Contents.equals(other);
			}

			/** Nodes hash by their contents. */
			@Override
			public int hashCode() {
				return this._Contents.hashCode();
			}

			/**
			 * Returns the MenuNode at the end of the specified route. If the
			 * route does not lead to a valid node, returns null.
			 */
			public MenuNode getNode(java.util.List<Object> route) {
				if (route == null)
					throw new IllegalArgumentException("Route cannot be a null list of objects.");
				if (route.isEmpty())
					return this;
				if (_SubNodes == null)
					return null;
				final MenuNode subMenu = _SubNodes.get(route.get(0));
				return subMenu == null ? null : subMenu.getNode(route.subList(1, route.size()));
			}

			/**
			 * Returns the MenuNode at the end of the specified string path,
			 * whose levels are delimited by '/' characters. If the route does
			 * not lead to a valid node, returns null.
			 */
			public MenuNode getNode(String route) {
				ArrayList<Object> listRoute = new ArrayList<Object>();
				for (String str : route.split("/"))
					listRoute.add(str);
				return getNode(listRoute);
			}

			/**
			 * Ensures that nodes along the '/'-delimited given route exist, and
			 * returns the very last node in the route.
			 */
			public MenuNode addNode(String route) {

				ArrayList<Object> listRoute = new ArrayList<Object>();
				for (String str : route.split("/"))
					listRoute.add(str);
				return addNode(listRoute);
			}

			/**
			 * Ensures that the specified node tree route exists, and returns
			 * the very last node in the route.
			 */
			public MenuNode addNode(java.util.List<Object> route) {

				// Corner case - bad input.
				if (route == null)
					throw new IllegalArgumentException("Route cannot be a null input.");

				// If we're at the leaf end of the route, return this.
				if (route.isEmpty())
					return this;

				// Are we sitting at an existing leaf in the tree? If so, allow
				// for branches.
				if (_SubNodes == null)
					_SubNodes = new Hashtable<Object, MenuNode>();

				// Is there a route down from here? If not, add it.
				MenuNode subMenu = _SubNodes.get(route.get(0));
				if (subMenu == null) {
					subMenu = new MenuNode(route.get(0));
					_SubNodes.put(subMenu._Contents, subMenu);
				}

				// Return the result of the next item in the tree.
				return subMenu.addNode(route.subList(1, route.size()));
			}

			/**
			 * Ensures that nodes along the given '/' - delimited route exist,
			 * and ensures a node containing the given contents exists at the
			 * end. The node will follow the given anonymous functions for
			 * determining whether the node is enabled or not, and for execution
			 * when the node is clicked.
			 */
			public MenuNode addNode(java.util.List<Object> route, Object newContents,
					Function<Object, Boolean> enabledTester, Consumer<Object> executor) {
				MenuNode node = addNode(route);
				MenuNode child = node._SubNodes.get(newContents);
				if (child != null)
					return child;
				child = new MenuNode(newContents, enabledTester, executor);
				node._SubNodes.put(child._Contents, child);
				return child;
			}

			/**
			 * Ensures that the specified node tree route exists, and returns
			 * the very last node in the route.
			 */
			public MenuNode addNode(String route, Object newContents, Function<Object, Boolean> enabledTester,
					Consumer<Object> executor) {
				ArrayList<Object> listRoute = new ArrayList<Object>();
				for (String str : route.split("/"))
					listRoute.add(str);
				return addNode(listRoute, newContents, enabledTester, executor);
			}

			/**
			 * Activates the defined executor established when this node was
			 * created.
			 */
			public void activate() {
				if (_EnabledTester.apply(_Contents))
					_Executor.accept(_Contents);
			}
		}

		public Menu() {
			this(new Skin(Gdx.files.internal("uiskin.json")));
		}

		public Menu(Skin skin) {

			_Skin = skin;
			setFillParent(true);
			setDebug(true);

			Pixmap whitePixmap = new Pixmap(1, 1, Format.RGBA8888);
			whitePixmap.setColor(Color.WHITE);
			whitePixmap.fill();
			_Skin.add("white", new Texture(whitePixmap));

			_Skin.add("defaultFont", new BitmapFont());
		}

		public boolean addItem(String path, Object newContents, Function<Object, Boolean> enabledTester, Consumer<Object> executor) {
			MenuNode node = _Root.addNode(path, newContents, enabledTester, executor);
			return node != null;
		}

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

			// Relying on tutorials at
			// https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java#L37

			Pixmap whitePM = new Pixmap(1, 1, Format.RGBA8888);
			whitePM.setColor(Color.WHITE);
			whitePM.fill();
			skin.add("white", new Texture(whitePM));

			// Not sure what a table does vis-a-vis a stage...
			table.setFillParent(true);
			stage.addActor(table);
			table.setDebug(true);

			skin.add("defaultFont", new BitmapFont()); // Use the default font
														// for now.

			// Set up the standard textbutton style.
			TextButtonStyle style = new TextButtonStyle();
			style.up = skin.newDrawable("white", Color.DARK_GRAY);
			style.down = skin.newDrawable("white", Color.DARK_GRAY);
			style.checked = skin.newDrawable("white", Color.GREEN);
			style.over = skin.newDrawable("white", Color.LIGHT_GRAY);
			style.font = skin.getFont("defaultFont");
			skin.add("default", style);

			TextButton button1 = new TextButton("Here!", style);
			table.add(button1);

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
