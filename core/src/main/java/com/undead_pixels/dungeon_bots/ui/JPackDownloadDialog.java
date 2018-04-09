package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.gson.JsonObject;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;

public class JPackDownloadDialog extends JDialog {

	private JList<PackSummary> list;
	private JButton bttnDownload, bttnCancel, bttnPrevPage, bttnNextPage;

	private LevelPack result = null;


	public JPackDownloadDialog(Window owner) {
		super(owner, "Import from the community...", Dialog.ModalityType.DOCUMENT_MODAL);
		this.setLayout(new BorderLayout());
		list = new JList<PackSummary>();
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				updateGUI();
			}
		});
		list.setCellRenderer(new ListCellRenderer<PackSummary>() {

			@Override
			public Component getListCellRendererComponent(JList<? extends PackSummary> list, PackSummary value,
					int index, boolean isSelected, boolean cellHasFocus) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		JScrollPane scroller = new JScrollPane(list);


		JPanel bttnPnl = new JPanel();
		bttnPnl.add(bttnPrevPage = UIBuilder.buildButton().text("Last page").action("PREV_PAGE", controller).create());
		bttnPnl.add(bttnDownload = UIBuilder.buildButton().text("Download").action("DOWNLOAD", controller).create());
		bttnPnl.add(bttnCancel = UIBuilder.buildButton().text("Cancel").action("CANCEL", controller).create());
		bttnPnl.add(bttnNextPage = UIBuilder.buildButton().text("Next page").action("NEXT_PAGE", controller).create());

		this.add(scroller, BorderLayout.CENTER);
		this.add(bttnPnl, BorderLayout.CENTER);
	}


	/**Returns the result of the download.  If nothing was downloaded, this value is null.*/
	public LevelPack getResult() {
		return this.result;
	}


	private void updateGUI() {
		bttnDownload.setEnabled(!list.isSelectionEmpty());
		PackSummary p = list.getSelectedValue();
		if (p != null) {

		}
	}


	private final ActionListener controller = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "PREV_PAGE":
			case "DOWNLOAD":
			case "CANCEL":
			case "NEXT_PAGE":
			default:
				System.err.println(this.getClass().getName() + " has not implemented command " + e.getActionCommand());
			}

		}

	};


	private static final class PackSummary {

		public final String description = "Description.";
		public final Image image = UIBuilder.getImage("images/sprite.jpg");
	}
}

