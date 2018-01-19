package com.undead_pixels.dungeon_bots;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.function.*;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.undead_pixels.dungeon_bots.ui.GDXandSwingScreen;

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

	/**
	 * private constructor for singleton
	 */
	private DungeonBotsMain() {

	}

	@Override
	public void setScreen(Screen screen) {
		if(this.screen != null && this.screen instanceof GDXandSwingScreen) {
			((GDXandSwingScreen) this.screen).attachToFrame(null); // clear the current screen's frame
		}
		
		super.setScreen(screen);

		if(this.screen != null && this.screen instanceof GDXandSwingScreen) {
			((GDXandSwingScreen) this.screen).attachToFrame(frame); // set the frame for the new screen
		}
	}

	/**
	 * Used to tell this to work with a specific JFrame (which likely contains the GDX canvas)
	 * 
	 * @param frame
	 */
	public void setFrame(JFrame frame) {
		if(frame != null) {
			this.frame = frame;
		}
	}
	

	@Override
	public void create() {
		//Menu menu = new Menu();

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

}
