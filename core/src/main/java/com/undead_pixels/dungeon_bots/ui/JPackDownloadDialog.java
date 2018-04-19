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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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
import javax.swing.SwingUtilities;
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


	private LevelPack resultPack;
	private String resultJson;
	private Page currentPage;
	private JTextPane errorPane;
	private JPanel contentPnl;
	private JLabel lblPage;


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
				Image img = pack.image;
				if (img == null)
					img = UIBuilder.getImage(BAD_DOWNLOAD_IMAGE);
				pnl.add(new JLabel(
						new ImageIcon(img.getScaledInstance(ICON_SIZE.width, ICON_SIZE.height, Image.SCALE_FAST))));

				JPanel textPnl = new JPanel(new VerticalLayout());
				textPnl.add(new JLabel(pack.title));
				textPnl.add(new JLabel(pack.desc));
				pnl.add(textPnl);
				if (isSelected)
					pnl.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.red));
				else
					pnl.setBorder(new EmptyBorder(2, 2, 2, 2));
				pnl.setPreferredSize(new Dimension(250, 50));
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
		contentPnl.setBorder(new EmptyBorder(10, 10, 10, 10));
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
		bttnPnl.add(UIBuilder.buildButton().image("icons/refresh.png").toolTip("Refresh this page.")
				.action("REFRESH", controller).create());
		bttnPnl.add(bttnNextPage = UIBuilder.buildButton().image("icons/resultset_next.png")
				.toolTip("Go forward to next page.").action("NEXT_PAGE", controller).create());
		bttnPnl.add(bttnLastPage = UIBuilder.buildButton().image("icons/resultset_last.png")
				.toolTip("Go forward to last page.").action("LAST_PAGE", controller).create());


		this.add(lblPage = new JLabel("PAGE LABEL"), BorderLayout.PAGE_START);
		this.add(contentPnl, BorderLayout.CENTER);
		this.add(bttnPnl, BorderLayout.PAGE_END);
		this.pack();

		setPage(1);

	}


	/**Returns the result of the download.  If nothing was downloaded, this value is null.*/
	public LevelPack getResultPack() {
		return this.resultPack;
	}
	
	public String getResultJson(){
		return this.resultJson;
	}


	private static final Gson gson = new Gson();


	private final void downloadPack(Page.Pack stub) {

		try {
			String url = WEBSITE + stub.file_link;
			String json = downloadResource(url);
			LevelPack p = LevelPack.fromJsonPartial(json);
			resultJson = json;	
			resultPack = p;			
			return;
		} catch (Exception e) {
			System.err.println("Could not parse JSON LevelPack. " + e.getMessage());
		}
	}


	private static final String downloadPage(int number) {
		// https://dungeonbots.herokuapp.com/api/v1/packs?page=2
		return downloadResource("https://dungeonbots.herokuapp.com/api/v1/packs?page=" + number);
	}


	private static final String downloadResource(String address) {
		URL url = null;
		BufferedReader reader = null;
		try {
			url = new URL(address);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0"); 
			int responseCode = conn.getResponseCode();
			//System.out.println("Response code:" + responseCode);
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null)
				sb.append(line + "\n");
			String ret = sb.toString();
			return ret;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				// do nothing
			}
		}

	}


	private static final String downloadResourceB(String address) {
		URL url = null;
		BufferedReader reader = null;
		try {
			url = new URL(address);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			String ret = sb.toString();
			return ret;
		} catch (IOException e) {
			if (url != null)
				System.err.println("Error fetching resource: " + url.toString());
			// e.printStackTrace();
			return null;
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				// Do nothing.
			}
		}
	}


	public void setPage(Integer pageNum) {
		if (fetching)
			return;
		else if (pageNum < 1)
			return;
		else if (currentPage != null && pageNum > currentPage.pages)
			return;
		else if (currentPage != null && pageNum == currentPage.page)
			return;

		currentPage = null;
		errorPane.setText("Looking up available level packs...");
		updateGUI();
		fetching = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				String str = downloadPage(pageNum);

				// Get back to the EDT thread.
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						onDownloadDone(str, pageNum);
					}
				});

			}
		}).start();
	}


	private transient boolean fetching = false;


	private void onDownloadDone(String str, Integer pageNum) {
		fetching = false;
		DefaultListModel<Page.Pack> model = (DefaultListModel<Page.Pack>) list.getModel();
		currentPage = null;
		list.clearSelection();


		// Check that a string was actually returned.
		if (str == null) {
			currentPage = null;
			model.removeAllElements();
			errorPane.setText(
					"Could not download this page from the website.  This may be a problem with internet connectivity.  Please try again later.");
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
				String resource = downloadResource(WEBSITE + pack.picture_link);
				URL url = new URL(WEBSITE + pack.picture_link);
				pack.image = UIBuilder.getImage(url);
			} catch (Exception ex) {
			}
			if (pack.image == null)
				pack.image = UIBuilder.getImage(BAD_DOWNLOAD_IMAGE);
		}

		updateGUI();
	}


	private void updateGUI() {

		String startPage = (currentPage == null) ? "__" : ("" + currentPage.page);
		String ofPage = (currentPage == null) ? "__" : ("" + currentPage.pages);
		this.lblPage.setText("Page " + startPage + " of " + ofPage);
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
				resultPack = null;
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
			case "REFRESH":
				if (currentPage == null)
					return;
				int pg = currentPage.page;
				currentPage = null;
				setPage(pg);
				return;
			case "DOWNLOAD":
				if (currentPage == null)
					return;
				int idx = list.getSelectedIndex();
				if (idx < 0)
					return;
				if (idx >= currentPage.packs.length)
					return;
				Page.Pack stub = currentPage.packs[idx];
				downloadPack(stub);
				JPackDownloadDialog.this.dispose();
				return;
			default:
				System.err.println(this.getClass().getName() + " has not implemented command " + e.getActionCommand());
			}

		}

	};


}

