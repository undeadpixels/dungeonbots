package com.undead_pixels.dungeon_bots;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * A Menu participates in layout.
 * 
 * @author Wesley Oates
 */
public class Menu extends Widget {

	// Note most of this widget drawn from gdx selectBox so as to learn gdx
	// Relying on tutorials at
	// https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java#L37

	private float _ColumnWidth;
	private float _PrefWidth=20, _PrefHeight=20;
	private ClickListener _ClickListener;
	private MenuStyle _Style;

	/** Menu tree structure. */
	private MenuNode _RootNode;

	/** A Menu has its own stage so it can prioritize input handling. */
	// private Stage _Stage = new Stage();
	private Skin _Skin;
	private Table _Table;

	public Menu(Skin skin) {
		this(skin.get(MenuStyle.class));
	}

	public Menu(Skin skin, String styleName) {
		this(skin.get(styleName, MenuStyle.class));

	}

	public Menu(MenuStyle style) {
		// setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());
		_RootNode = new MenuNode(null);
		_RootNode.open();
		_Table = new Table();
		_Table.setFillParent(true);

		Pixmap whitePM = new Pixmap(1, 1, Format.RGBA8888);
		whitePM.setColor(Color.WHITE);
		whitePM.fill();
		Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
		skin.add("white", new Texture(whitePM));
		skin.add("defaultFont", new BitmapFont()); // Use the default font

		// Set up the standard textbutton style.
		TextButtonStyle tbStyle = new TextButtonStyle();
		tbStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		tbStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
		tbStyle.checked = skin.newDrawable("white", Color.GREEN);
		tbStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		tbStyle.font = skin.getFont("defaultFont");
		skin.add("default", style);

		TextButton button1 = new TextButton("I am a button!", tbStyle);
		_Table.setBounds(250, 0, 100, 300);
		//_Table.setHeight(50);
		//_Table.setWidth(50);
		_Table.add(button1);
		
		Stage spStage = new Stage();
		Table spTable = new Table(skin);
		spTable.setBounds(0, 0, 300, 300);
		spTable.setFillParent(true);
		Label temp = new Label("A label right here now", skin);
		temp.setAlignment(Align.left, Align.center);
		spTable.addActor(temp);
		ScrollPane pane = new ScrollPane(spTable, skin);
		pane.setFillParent(true);
		//spStage.addActor(spTable);
		_Table.add(pane);
		

	}

	/// MENU CONTENT CONTROLS
	protected class MenuNode {

		/** True if the MenuNode is open, false if not. */
		private boolean _IsOpen = false;
		private Object _Contents;
		private Hashtable<Object, MenuNode> _SubNodes = null;
		private Function<Object, Boolean> _EnabledTester;
		private Consumer<Object> _Executor;

		public ScrollPane pane;

		public MenuNode(Object contents) {
			this(contents, obj -> true, mn -> System.out.println(contents.toString()));
		}

		public MenuNode(Object contents, Function<Object, Boolean> enabledTester, Consumer<Object> executor) {
			_Contents = contents;
			_EnabledTester = enabledTester;
			_Executor = executor;
		}

		/**
		 * Two nodes compared are equal if their contents are equal. For other
		 * types, a node is equal to another object if its contents are equal to
		 * that object.
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

		@Override
		public String toString() {
			return _Contents.toString();
		}

		/**
		 * Returns the MenuNode at the end of the specified route. If the route
		 * does not lead to a valid node, returns null.
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
		 * Returns the MenuNode at the end of the specified string path, whose
		 * levels are delimited by '/' characters. If the route does not lead to
		 * a valid node, returns null.
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
		 * Ensures that the specified node tree route exists, and returns the
		 * very last node in the route.
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
		 * Ensures that nodes along the given '/' - delimited route exist, and
		 * ensures a node containing the given contents exists at the end. The
		 * node will follow the given anonymous functions for determining
		 * whether the node is enabled or not, and for execution when the node
		 * is clicked.
		 */
		public MenuNode addNode(java.util.List<Object> route, Object newContents,
				Function<Object, Boolean> enabledTester, Consumer<Object> executor) {
			MenuNode node = addNode(route);

			MenuNode child;
			if (node._SubNodes == null)
				node._SubNodes = new Hashtable<Object, MenuNode>();
			else if ((child = node._SubNodes.get(newContents)) != null)
				return child;

			child = new MenuNode(newContents, enabledTester, executor);
			node._SubNodes.put(child._Contents, child);
			return child;
		}

		/**
		 * Ensures that the specified node tree route exists, and returns the
		 * very last node in the route.
		 */
		public MenuNode addNode(String route, Object newContents, Function<Object, Boolean> enabledTester,
				Consumer<Object> executor) {
			ArrayList<Object> listRoute = new ArrayList<Object>();
			for (String str : route.split("/"))
				if (!str.equals(""))
					listRoute.add(str);
			return addNode(listRoute, newContents, enabledTester, executor);
		}

		/**
		 * Activates the defined executor established when this node was
		 * created.
		 */
		public void execute() {
			if (_EnabledTester.apply(_Contents))
				_Executor.accept(_Contents);
		}

		/** Marks as closed this node and all children of this node. */
		public void close() {
			this._IsOpen = false;
			this.closeChildren();
		}

		/** Marks as closed all children of this node. */
		public void closeChildren() {
			for (MenuNode child : _SubNodes.values()) {
				child.close();
			}
		}

		/** Returns whether the given node is a child of this node. */
		public boolean contains(MenuNode node) {
			return _SubNodes.containsValue(node);
		}

		/**
		 * Returns whether the given object is contained as the contents of a
		 * child of this node.
		 */
		public boolean contains(Object obj) {
			return (obj.equals(this._Contents)) || _SubNodes.containsKey(obj);
		}

		public void open() {
			_IsOpen = true;
		}
	}

	public boolean addItem(String path, Object newContents, Function<Object, Boolean> enabledTester,
			Consumer<Object> executor) {
		MenuNode node = _RootNode.addNode(path, newContents, enabledTester, executor);

		return node != null;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		validate();

		_Table.draw(batch, parentAlpha);
		// Draw the root menu.

		System.out.println("Draw");
	}

}

/** The style for a menu */
class MenuStyle extends SelectBoxStyle {
	public int columnWidth;

	public MenuStyle() {
		super();
	}

	public MenuStyle(int columnWidth, BitmapFont font, Color fontColor, Drawable background,
			ScrollPaneStyle scrollStyle, ListStyle listStyle) {
		super(font, fontColor, background, scrollStyle, listStyle);
		this.columnWidth = columnWidth;
	}

	public MenuStyle(MenuStyle originalStyle) {
		super(originalStyle);
		this.columnWidth = originalStyle.columnWidth;
	}
}
