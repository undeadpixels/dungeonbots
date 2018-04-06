package com.undead_pixels.dungeon_bots.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.HorizontalLayout;

import com.undead_pixels.dungeon_bots.math.Cartesian;
import com.undead_pixels.dungeon_bots.nogdx.RenderingContext;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.ui.screens.Tool;

public final class JWorldSizer extends JPanel {

	public static final int MAX_WIDTH = 200;
	public static final int MAX_HEIGHT = 200;
	public static final int MIN_WIDTH = 1;
	public static final int MIN_HEIGHT = 1;

	private JDialog dialog = null;
	private boolean changed = false;
	private Rectangle newArea;

	private final World world;
	private final JSpinner widthSpinner, heightSpinner;
	private final JSlider widthSlider, heightSlider;


	public JWorldSizer(World world) {
		this.world = world;
		newArea = new Rectangle(0, 0, (int) world.getSize().x, (int) world.getSize().y);

		ChangeListener controller = new ChangeListener() {

			private boolean propogating = true;


			@Override
			public void stateChanged(ChangeEvent e) {
				if (!propogating)
					return;
				propogating = false;
				if (e.getSource() == widthSpinner) {
					widthSlider.setValue((Integer) widthSpinner.getValue());
				} else if (e.getSource() == heightSpinner) {
					heightSlider.setValue((Integer) heightSpinner.getValue());
				} else if (e.getSource() == widthSlider) {
					widthSpinner.setValue((Integer) widthSlider.getValue());
				} else if (e.getSource() == heightSlider) {
					heightSpinner.setValue((Integer) heightSlider.getValue());
				}
				newArea.setSize((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue());
				propogating = true;
			}

		};

		SpinnerModel widthModel = new SpinnerNumberModel((int) world.getSize().x, MIN_WIDTH, MAX_WIDTH, 1);
		widthSpinner = new JSpinner(widthModel);
		widthSpinner.addChangeListener(controller);

		SpinnerModel heightModel = new SpinnerNumberModel((int) world.getSize().y, MIN_HEIGHT, MAX_HEIGHT, 1);
		heightSpinner = new JSpinner(heightModel);
		heightSpinner.addChangeListener(controller);

		widthSlider = new JSlider(JSlider.HORIZONTAL, MIN_WIDTH, MAX_WIDTH, (int) world.getSize().x);
		widthSlider.addChangeListener(controller);

		heightSlider = new JSlider(JSlider.HORIZONTAL, MIN_HEIGHT, MAX_HEIGHT, (int) world.getSize().y);
		heightSlider.addChangeListener(controller);


		JPanel widthPanel = new JPanel();
		widthPanel.setLayout(new BoxLayout(widthPanel, BoxLayout.LINE_AXIS));
		widthPanel.add(new JLabel("Width:"));
		widthPanel.add(widthSlider);
		widthPanel.add(widthSpinner);

		JPanel heightPanel = new JPanel();
		heightPanel.setLayout(new BoxLayout(heightPanel, BoxLayout.LINE_AXIS));
		heightPanel.add(new JLabel("Height:"));
		heightPanel.add(heightSlider);
		heightPanel.add(heightSpinner);

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.add(widthPanel);
		this.add(heightPanel);
	}

	public JDialog getDialog() { return dialog;}

	void commit() {
		world.setSize(newArea.width, newArea.height,newArea.x, newArea.y);
		Tool.clearUndo(world);
	}


	// ============================================
	// ==== JWorldSizer DIALOG STUFF ==============
	// ============================================

	private static JWorldSizer oneDialogAllowed = null;


	public static JWorldSizer showDialog(Window owner, World world, WorldView view) {
		if (oneDialogAllowed != null) {
			oneDialogAllowed.requestFocus();
			return oneDialogAllowed;
		}

		if (world != view.getWorld())
			throw new RuntimeException("Sanity check.");
		JDialog dialog = new JDialog(owner, "Size your world...", Dialog.ModalityType.MODELESS);
		dialog.setLayout(new BorderLayout());


		JWorldSizer jws = new JWorldSizer(world);
		jws.dialog = dialog;
		Rectangle oldArea = new Rectangle(0, 0, jws.newArea.width, jws.newArea.height);
		dialog.add(jws, BorderLayout.LINE_START);

		ActionListener dialogController = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				switch (arg0.getActionCommand()) {
				case "COMMIT":
					if (!jws.newArea.contains(oldArea)) {
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
					jws.commit();
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
		view.setAdornmentTool(new Tool("Size highlighter", null) {

			@Override
			public void render(Graphics2D g, RenderingContext batch) {
				for (int x = 0; x < jws.newArea.width; x++) {
					for (int y = 0; y < jws.newArea.height; y++) {
						Point cornerA = new Point(jws.newArea.x + x, jws.newArea.y + y+1),
								cornerB = new Point(cornerA.x + 1, cornerA.y + 1); 
						g.setStroke(new BasicStroke(2));
						g.setColor(Color.cyan);
						Rectangle rect = Cartesian.makeRectangle(cornerA, cornerB);
						batch.drawRect(rect.x, rect.y, rect.width, rect.height);
					}
				}
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
		view.setShowGrid(true);
		view.setSelectedEntities(null);
		view.setSelectedTiles(null);
		dialog.setVisible(true);

		return jws;

	}


}
