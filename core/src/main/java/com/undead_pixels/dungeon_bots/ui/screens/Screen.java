package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;
import com.undead_pixels.dungeon_bots.ui.UIBuilder;

/**
 * Defines GUI and control interface. A protected GUI state can be stored.
 * NOTE:  inheritance is from JFrame so that items contained can be focusable.
 */
public abstract class Screen extends JFrame {

	// NOTE - these really should be in only the screens where it's actually relevant
	// for example, the main menu does not need them, and they are duplicated other places.
	protected LevelPack levelPack;
	protected World world;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** TODO:  Will be used for saving GUI state, later. */
	protected final HashMap<String, Object> guiState = new HashMap<String, Object>();


	// =========================================================================
	// -------------------Screen CONSTRUCTORS-----------------------------------
	// =========================================================================

	protected Screen() {
		super();
		this.world = null;
		this.levelPack = null;
	}
	
	protected Screen(LevelPack levelPack){
		super();
		this.world = (this.levelPack = levelPack).getCurrentWorld();		
	}
	
	/**NOTE:  this constructur should not be used, as every level should have an associated World*/
	@Deprecated
	protected Screen (World world){
		this.world = world;
		this.levelPack = null;
	}


	public void setup() {
		this.setIconImage(UIBuilder.getImage("images/sprite.jpg"));
		this._Controller = this.makeController();
		this.setDefaultLayout();
		this.addComponents(this.getContentPane());
	}


	/**
	 * This method is called at construction time to set the controller. It
	 * forces classes inheriting from Screen to override it, and in turn, to
	 * implement a ScreenController class.
	 */
	protected abstract ScreenController makeController();


	/**
	 * This method is called at construction time. Override the method to layout
	 * components for the game screen.
	 */
	protected abstract void addComponents(Container pane);


	/**
	 * Called at construction time to layout this screen. Override to specify
	 * the layout appearance. Can be called after construction time.
	 */
	protected abstract void setDefaultLayout();


	// =========================================================================
	// -------------------Screen CONTROL-----------------------------------
	// =========================================================================

	private ScreenController _Controller;


	/**
	 * Returns the current ScreenController. The default controller will simply
	 * call overrideable functions from the Screen class.
	 */
	protected ScreenController getController() {
		return _Controller;
	}


	/**
	 * The default controller simply calls overrideable functions from the
	 * Screen class. If a different controller is desired, it can be replaced.
	 */
	protected void setController(ScreenController controller) {
		_Controller = controller;

	}


	/**
	 * This controller is implemented separately so that classes inheriting from
	 * Screen will not be chock-full of irrelevant event handlers. This class
	 * must be extended in any class that inherits from Screen.
	 */
	protected abstract class ScreenController implements ActionListener {

		/**A convenience function that registers this controller as a listener for all applicable listening runtime types.  
		 * TODO:  cut out the ones that aren't actually used.*/
		public void registerSignalsFrom(Component signaller) {
			if (this instanceof MouseInputListener)
				signaller.addMouseListener((MouseInputListener) this);
			if (this instanceof MouseWheelListener)
				signaller.addMouseWheelListener((MouseWheelListener) this);
			if (this instanceof KeyListener)
				signaller.addKeyListener((KeyListener) this);
			if (this instanceof ComponentListener)
				signaller.addComponentListener((ComponentListener) this);
			if (this instanceof PropertyChangeListener)
				signaller.addPropertyChangeListener((PropertyChangeListener) this);
			if (this instanceof MouseMotionListener)
				signaller.addMouseMotionListener((MouseMotionListener) this);

			if (signaller instanceof Container) {
				Container c = (Container) signaller;
				if (this instanceof ContainerListener)
					c.addContainerListener((ContainerListener) this);
			}

			if (signaller instanceof JComponent) {
				JComponent jc = (JComponent) signaller;
				if (this instanceof AncestorListener)
					jc.addAncestorListener((AncestorListener) this);
				if (this instanceof VetoableChangeListener)
					jc.addVetoableChangeListener((VetoableChangeListener) this);
			}

			if (signaller instanceof AbstractButton) {
				AbstractButton bttn = (AbstractButton) signaller;
				if (this instanceof ActionListener)
					bttn.addActionListener((ActionListener) this);
			}

			if (signaller instanceof JList) {
				JList<?> list = (JList<?>) signaller;
				if (this instanceof ListSelectionListener)
					list.addListSelectionListener((ListSelectionListener) this);
			}


			// Action, ActionListener, AdjustmentListener, AncestorListener,
			// AWTEventListener, BeanContextMembershipListener,
			// BeanContextServiceRevokedListener, BeanContextServices,
			// BeanContextServicesListener, CaretListener, CellEditorListener,
			// ChangeListener, ComponentListener, ConnectionEventListener,
			// ContainerListener, ControllerEventListener, DocumentListener,
			// DragGestureListener, DragSourceListener,
			// DragSourceMotionListener, DropTargetListener, FlavorListener,
			// FocusListener, HandshakeCompletedListener,
			// HierarchyBoundsListener, HierarchyListener, HyperlinkListener,
			// IIOReadProgressListener, IIOReadUpdateListener,
			// IIOReadWarningListener, IIOWriteProgressListener,
			// IIOWriteWarningListener, InputMethodListener,
			// InternalFrameListener, ItemListener, KeyListener, LineListener,
			// ListDataListener, ListSelectionListener, MenuDragMouseListener,
			// MenuKeyListener, MenuListener, MetaEventListener,
			// MouseInputListener, MouseListener, MouseMotionListener,
			// MouseWheelListener, NamespaceChangeListener, NamingListener,
			// NodeChangeListener, NotificationListener, ObjectChangeListener,
			// PopupMenuListener, PreferenceChangeListener,
			// PropertyChangeListener, RowSetListener, RowSorterListener,
			// SSLSessionBindingListener, StatementEventListener,
			// TableColumnModelListener, TableModelListener, TextListener,
			// TreeExpansionListener, TreeModelListener, TreeSelectionListener,
			// TreeWillExpandListener, UndoableEditListener,
			// UnsolicitedNotificationListener, VetoableChangeListener,
			// WindowFocusListener, WindowListener, WindowStateListener
		}


		public void unRegisterSignals(Component signaller) {
			throw new RuntimeException("Not implemented yet.");
		}
	}

}
