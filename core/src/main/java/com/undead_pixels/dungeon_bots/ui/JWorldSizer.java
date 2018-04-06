package com.undead_pixels.dungeon_bots.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.math.Cartesian;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Tile;
import com.undead_pixels.dungeon_bots.ui.screens.Tool;

public final class JWorldSizer extends JPanel {

	private final boolean originalShowGrid;
	private JDialog dialog = null;
	private boolean changed = false;
	private Rectangle newArea;
	private Rectangle oldArea;
	private final World world;
	private final WorldView view;


	public JWorldSizer(World world, WorldView view) {
		originalShowGrid = view.getShowGrid();
		this.world = world;
		if (world != view.getWorld())
			throw new RuntimeException("Sanity check.");
		view.setShowGrid(true);
		view.setSelectedEntities(null);
		view.setSelectedTiles(null);
		view.setAdornmentTool(highlighterTool);
		oldArea = new Rectangle(0, 0, (int) world.getSize().x, (int) world.getSize().y);
		newArea = new Rectangle(0, 0, oldArea.width, oldArea.height);
		this.view = view;
	}


	// ============================================
	// ==== JWorldSizer DIALOG STUFF ==============
	// ============================================

	private static JWorldSizer oneDialogAllowed = null;


	public static JWorldSizer createDialog(Window owner, World world, WorldView view) {
		if (oneDialogAllowed != null) {
			oneDialogAllowed.requestFocus();
			return oneDialogAllowed;
		}

		if (world != view.getWorld())
			throw new RuntimeException("Sanity check.");
		JDialog dialog = new JDialog(owner, "Size your world...", Dialog.ModalityType.MODELESS);
		dialog.setLayout(new BorderLayout());

		JWorldSizer jws = new JWorldSizer(world, view);
		jws.dialog = dialog;
		dialog.add(jws, BorderLayout.LINE_START);
		ActionListener dialogController = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				switch (arg0.getActionCommand()) {
				case "COMMIT":
					if (!jws.newArea.contains(jws.oldArea)) {
						int confirm = JOptionPane.showConfirmDialog(jws,
								"Tiles will be removed.  This step cannot be undone.  Are you sure?", "Confirm",
								JOptionPane.YES_NO_OPTION);
						if (confirm != JOptionPane.YES_OPTION)
							break;
					} else {
						int confirm = JOptionPane.showConfirmDialog(jws, "This step cannot be undone.  Are you sure?",
								"Confirm", JOptionPane.YES_NO_OPTION);
						if (confirm != JOptionPane.YES_OPTION)
							break;
					}
					Tool.clearUndo(world);
					jws.commitSize();
					dialog.dispose();
					break;
				case "CANCEL":
					if (jws.changed) {
						int confirm = JOptionPane.showConfirmDialog(jws, "Discard all changes?", "Confirm",
								JOptionPane.YES_NO_OPTION);
						if (confirm != JOptionPane.YES_OPTION)
							break;
					}

					dialog.dispose();
					break;
				case "HELP":
				default:
					throw new RuntimeException("Not implemented command " + arg0.getActionCommand());
				}
			}
		};
		dialog.addWindowListener(new WindowListenerAdapter() {

			@Override
			protected void event(WindowEvent e) {
				if (e.getID() != WindowEvent.WINDOW_CLOSING && e.getID() != WindowEvent.WINDOW_CLOSED)
					return;
				oneDialogAllowed = null;
				view.setAdornmentTool(null);
				jws.dialog = null;
			}
		});


		// Create the approval buttons.
		JPanel pnlButtons = new JPanel(new HorizontalLayout());
		pnlButtons.add(UIBuilder.buildButton().image("icons/ok.png").toolTip("Approve changes and close the dialog.")
				.action("COMMIT", dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/close.png").toolTip("Cancel changes and close the dialog.")
				.action("CANCEL", dialogController).create());
		pnlButtons.add(UIBuilder.buildButton().image("icons/question.png").toolTip("Help on the world sizer.")
				.action("HELP", dialogController).create());
		dialog.add(pnlButtons, BorderLayout.PAGE_END);

		dialog.pack();
		return jws;

	}


	void commitSize() {
		world.setSize(newArea.x, newArea.y, newArea.width, newArea.height);
	}


	// Create the highlighter tool
	private final Tool highlighterTool = new Tool("Size highlighter", null) {

		@Override
		public void render(Graphics2D g) {

		}
	};
}
