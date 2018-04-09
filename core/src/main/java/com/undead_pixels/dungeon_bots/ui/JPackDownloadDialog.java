package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeModel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.undead_pixels.dungeon_bots.file.Serializer;
import com.undead_pixels.dungeon_bots.scene.level.LevelPack;

public class JPackDownloadDialog extends JDialog {

	private static final String CARD_LIST = "LIST";
	private static final String CARD_ERROR = "ERROR";
	private static final String WEBSITE = " https://dungeonbots.herokuapp.com";
	private static final String BAD_DOWNLOAD_IMAGE = "images/sprite.jpg";
	private static final Dimension ICON_SIZE = new Dimension(40, 40);
	private JList<Page.Pack> list;
	private JButton bttnDownload, bttnCancel, bttnPrevPage, bttnNextPage, bttnFirstPage, bttnLastPage;


	private LevelPack result;
	private Page currentPage;
	private JTextPane errorPane;
	private JPanel contentPnl;


	public JPackDownloadDialog(Window owner) {
		super(owner, "Import from the community...", Dialog.ModalityType.DOCUMENT_MODAL);
		this.setLayout(new BorderLayout());
		list = new JList<Page.Pack>(new DefaultListModel<Page.Pack>());
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateGUI();
			}
		});
		list.setCellRenderer(new ListCellRenderer<Page.Pack>() {

			@Override
			public Component getListCellRendererComponent(JList<? extends Page.Pack> list, Page.Pack pack, int index,
					boolean isSelected, boolean cellHasFocus) {
				JPanel pnl = new JPanel(new HorizontalLayout());
				pnl.add(new JLabel(new ImageIcon(
						pack.image.getScaledInstance(ICON_SIZE.width, ICON_SIZE.height, Image.SCALE_FAST))));

				JPanel textPnl = new JPanel(new VerticalLayout());
				textPnl.add(new JLabel(pack.title));
				textPnl.add(new JLabel(WEBSITE + pack.picture_link));
				pnl.add(textPnl);
				if (isSelected)
					pnl.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.red));
				else
					pnl.setBorder(new EmptyBorder(2, 2, 2, 2));
				return pnl;
			}
		});
		JScrollPane scroller = new JScrollPane(list);
		errorPane = new JTextPane();
		errorPane.setText("ERROR PANE");
		errorPane.setFocusable(true);
		errorPane.setOpaque(true);
		errorPane.setForeground(Color.RED);
		errorPane.setEditable(false);

		contentPnl = new JPanel(new CardLayout());
		contentPnl.add(errorPane, CARD_ERROR);
		contentPnl.add(scroller, CARD_LIST);


		JPanel bttnPnl = new JPanel();
		bttnPnl.add(bttnFirstPage = UIBuilder.buildButton().image("icons/resultset_first.png")
				.toolTip("Go back to first page.").action("FIRST_PAGE", controller).create());
		bttnPnl.add(bttnPrevPage = UIBuilder.buildButton().image("icons/resultset_previous.png")
				.toolTip("Go back to previous page.").action("PREV_PAGE", controller).create());
		bttnPnl.add(bttnDownload = UIBuilder.buildButton().image("icons/ok.png").toolTip("Download this level pack.")
				.action("DOWNLOAD", controller).create());
		bttnPnl.add(bttnCancel = UIBuilder.buildButton().image("icons/close.png").toolTip("Cancel.")
				.action("CANCEL", controller).create());
		bttnPnl.add(bttnNextPage = UIBuilder.buildButton().image("icons/resultset_next.png")
				.toolTip("Go forward to next page.").action("NEXT_PAGE", controller).create());
		bttnPnl.add(bttnLastPage = UIBuilder.buildButton().image("icons/resultset_last.png")
				.toolTip("Go forward to last page.").action("LAST_PAGE", controller).create());

		this.add(contentPnl, BorderLayout.CENTER);
		this.add(bttnPnl, BorderLayout.PAGE_END);
		this.pack();

		setPage(1);

	}


	/**Returns the result of the download.  If nothing was downloaded, this value is null.*/
	public LevelPack getResult() {
		return this.result;
	}


	private static final Gson gson = new Gson();


	private static final String downloadPage(int number) {
		return Serializer.readStringFromFile("website_api.json");
		// https://dungeonbots.herokuapp.com/api/v1/packs?page=2
		/*
		try (Socket socket = new Socket("https://dungeonbots.herokuapp.com/api/v1/packs?page=" + number, 80);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null)
				sb.append(line + "\n");
			String ret = sb.toString();
			return ret;
		} catch (Exception ex) {
			return null;
		}
		*/
	}


	public void setPage(int pageNum) {
		int oldNum = (currentPage == null) ? -1 : currentPage.page;
		if (pageNum == oldNum)
			return;
		DefaultListModel<Page.Pack> model = (DefaultListModel<Page.Pack>) list.getModel();

		// Check that a string was actually returned.
		String str = downloadPage(pageNum);
		if (str == null) {
			currentPage = null;
			model.removeAllElements();
			errorPane.setText(
					"Could not download from the website.  \nThis may be a problem with internet connectivity.  Please try again later.");
			updateGUI();
			return;
		}

		// Check that the string can be parsed correctly.
		try {
			currentPage = parsePage(str);
		} catch (Exception ex) {
			currentPage = null;
			model.removeAllElements();
			errorPane.setText("Could not interpret downloaded page. " + ex.getMessage());
			updateGUI();
			return;
		}

		// Check that the right page was returned.
		if (currentPage.page != pageNum) {
			currentPage = null;
			model.removeAllElements();
			errorPane.setText("Downloaded wrong page number.");
			updateGUI();
			return;
		}

		// Set the list's contents to the new packs.
		model.removeAllElements();
		for (Page.Pack pack : currentPage.packs) {
			model.addElement(pack);
			try {
				URL url = new URL(WEBSITE + pack.picture_link);
				pack.image = UIBuilder.getImage(url);
			} catch (Exception ex) {
			}
			if (pack.image == null)
				pack.image = UIBuilder.getImage(BAD_DOWNLOAD_IMAGE);
		}

		// Set the selection to an appropriate pack.
		if (currentPage.packs.length > 0) {
			if (oldNum < currentPage.page)
				list.setSelectedIndex(0);
			else
				list.setSelectedIndex(currentPage.packs.length - 1);
		}

		updateGUI();

	}


	private void updateGUI() {

		CardLayout cl = (CardLayout) contentPnl.getLayout();
		if (currentPage == null)
			cl.show(contentPnl, CARD_ERROR);
		else
			cl.show(contentPnl, CARD_LIST);

		bttnDownload.setEnabled(!list.isSelectionEmpty());
		this.bttnNextPage.setEnabled(currentPage != null && currentPage.page < currentPage.pages);
		this.bttnPrevPage.setEnabled(currentPage != null && currentPage.page > 1);
		this.bttnFirstPage.setEnabled(currentPage != null && currentPage.page > 1);
		this.bttnLastPage.setEnabled(currentPage != null && currentPage.page < currentPage.pages);
	}


	private static final Page parsePage(String string) {
		JsonObject obj = new JsonParser().parse(string).getAsJsonObject();
		Page result = gson.fromJson(obj, Page.class);
		return result;
	}


	private static final class Page {

		int page;
		int pages;
		int packs_total;
		int packs_here;
		Pack[] packs;


		private static class Pack {

			int pack_id;
			int user_id;
			String title;
			String desc;
			String picture_link;
			String file_link;

			transient Image image = null;
		}
	}


	private final ActionListener controller = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case "FIRST_PAGE":
				if (currentPage == null)
					return;
				if (currentPage.page <= 1)
					return;
				setPage(1);
				return;
			case "PREV_PAGE":
				if (currentPage == null)
					return;
				if (currentPage.page <= 1)
					return;
				setPage(currentPage.page - 1);
				return;
			case "CANCEL":
				result = null;
				JPackDownloadDialog.this.dispose();
				return;
			case "NEXT_PAGE":
				if (currentPage == null)
					return;
				if (currentPage.page >= currentPage.pages)
					return;
				setPage(currentPage.page + 1);
				return;
			case "LAST_PAGE":
				if (currentPage == null)
					return;
				if (currentPage.page >= currentPage.pages)
					return;
				setPage(currentPage.pages);
				return;
			case "DOWNLOAD":
				if (currentPage == null)
					return;
				int idx = list.getSelectedIndex();
				if (idx < 0)
					return;
				if (idx >= currentPage.packs.length)
					return;
				String link = currentPage.packs[idx].file_link;
				System.out.println("Preparing to download: " + link);
			default:
				System.err.println(this.getClass().getName() + " has not implemented command " + e.getActionCommand());
			}

		}

	};


}

