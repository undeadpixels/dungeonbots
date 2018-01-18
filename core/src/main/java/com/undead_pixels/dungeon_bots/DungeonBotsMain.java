package com.undead_pixels.dungeon_bots;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.function.*;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
	
	private JFrame frame = null;
	
	private HashMap<String, JComponent> sidePanes = new HashMap<>();

	/**
	 * private constructor for singleton
	 */
	private DungeonBotsMain() {

	}

	@Override
	public void setScreen(Screen screen) {
		for(JComponent c : sidePanes.values()) {
			if(frame != null) {
				frame.remove(c);
			}
		}
		sidePanes.clear();
		super.setScreen(screen);
	}

	public void setFrame(JFrame frame) {
		if(frame != null) {
			this.frame = frame;
		}
	}
	
	/**
	 * @param pane	A JComponent containing the UI for the given side
	 * @param side	A side, as given by BorderLayout.[EAST][WEST][...]
	 */
	public void addPane(JComponent pane, String side) {
		if(frame != null) {
			JComponent old = sidePanes.get(side);
			if(old != null) {
				frame.remove(old);
			}
			frame.add(pane, side);
			
			frame.revalidate();
		}
	}

	public void removePane(JComponent pane) {
		if(frame != null) {
			frame.remove(pane);
			frame.revalidate();
		}
	}
	public void removePane(String side) {
		JComponent pane = sidePanes.get(side);
		if(pane != null && frame != null) {
			frame.remove(pane);
			frame.revalidate();
		}
	}

	@Override
	public void create() {
		Menu menu = new Menu();

		setScreen(new NullScreen());
	}

	/**
	 * A Menu participates in layout. Being a Table, it can contain other
	 * actors.
	 * 
	 * @author Wesley Oates
	 */
	public class Menu extends Table {

		/* Menu tree structure. */
		MenuNode _Root = new MenuNode(null);

		private class MenuNode {

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
			public MenuNode GetNode(java.util.List<Object> route) {
				if (route == null)
					throw new IllegalArgumentException("Route cannot be a null list of objects.");
				if (route.isEmpty())
					return this;
				if (_SubNodes == null)
					return null;
				final MenuNode subMenu = _SubNodes.get(route.get(0));
				return subMenu == null ? null : subMenu.GetNode(route.subList(1, route.size()));
			}

			/**
			 * Returns the MenuNode at the end of the specified string path,
			 * whose levels are delimited by '/' characters. If the route does
			 * not lead to a valid node, returns null.
			 */
			public MenuNode GetNode(String route) {
				ArrayList<Object> listRoute = new ArrayList<Object>();
				for (String str : route.split("/"))
					listRoute.add(str);
				return GetNode(listRoute);
			}

			/**
			 * Ensures that nodes along the '/'-delimited given route exist, and
			 * returns the very last node in the route.
			 */
			public MenuNode AddNode(String route) {

				ArrayList<Object> listRoute = new ArrayList<Object>();
				for (String str : route.split("/"))
					listRoute.add(str);
				return AddNode(listRoute);
			}

			/**
			 * Ensures that the specified node tree route exists, and returns
			 * the very last node in the route.
			 */
			public MenuNode AddNode(java.util.List<Object> route) {

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
				return subMenu.AddNode(route.subList(1, route.size()));
			}

			/**
			 * Ensures that nodes along the given '/' - delimited route exist,
			 * and ensures a node containing the given contents exists at the
			 * end. The node will follow the given anonymous functions for
			 * determining whether the node is enabled or not, and for execution
			 * when the node is clicked.
			 */
			public MenuNode AddNode(java.util.List<Object> route, Object newContents,
					Function<Object, Boolean> enabledTester, Consumer<Object> executor) {
				MenuNode node = AddNode(route);
				MenuNode child = node._SubNodes.get(newContents);
				if (child != null)
					return child;
				child = new MenuNode(newContents, enabledTester, executor);
				node._SubNodes.put(child._Contents, child);
				return child;
			}

			/**
			 * Activates the defined executor established when this node was
			 * created.
			 */
			public void Activate() {
				if (_EnabledTester.apply(_Contents))
					_Executor.accept(_Contents);
			}
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
			Box b = new Box(BoxLayout.Y_AXIS);
			b.add(new JLabel("Hi, I'm Swing"));
			JButton btn = new JButton("HI");
			b.add(btn);
			btn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					DungeonBotsMain.instance.removePane(b);
				}
				
			});
			b.add(new JLabel("Click the button to make this side thing disappear!"));
			
			DungeonBotsMain.instance.addPane(b, BorderLayout.EAST);
			
			
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
		
		public void resize(int w, int h) {
			// TODO - we need this soemhow
			stage.getViewport().update(w, h);
			stage.getCamera().update();
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
