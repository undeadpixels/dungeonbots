package com.undead_pixels.dungeon_bots.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.undead_pixels.dungeon_bots.DungeonBotsMain;
import com.undead_pixels.dungeon_bots.User;
import com.undead_pixels.dungeon_bots.ui.Login;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeEditor;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.SyntaxStyle;
import jsyntaxpane.TokenType;
import jsyntaxpane.syntaxkits.LuaSyntaxKit;
import jsyntaxpane.util.Configuration;

/**
 * The Launcher for this game.
 * Runs for a couple moments to initialize contexts and
 * start other things and then is unused once the application is running.
 * 
 * Unless we need something really specific (or we want to change more colors),
 * we shouldn't need to touch this class any more.
 */
public class DesktopLauncher {

	/**
	 * A boolean to force the theme to be a dark, platform-independent flavor,
	 * instead of the native OS-specific theme that is default.
	 */
	private static final boolean forceNimbus = true;

	public static void main(String[] arg) {

		DefaultSyntaxKit.initKit();

		// UI theming
		if (forceNimbus) {
			setDarkNimbus();
		}

		// Tell macOS to handle the main menu bar like most macOS apps do
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		DungeonBotsMain game = DungeonBotsMain.instance;
		game.setUser(user);
		
		// create the GL canvas
		JFrame frame = new JFrame("DungeonBots");
		LwjglAWTCanvas canvas = new LwjglAWTCanvas(DungeonBotsMain.instance);
		frame.setLayout(new BorderLayout(0, 0));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO:re-examine

		
		// Add everything to a window and show it
		DungeonBotsMain.instance.setFrame(frame);
		frame.add(canvas.getCanvas(), BorderLayout.CENTER);
		frame.setSize(1024, 768);
		frame.revalidate();
		frame.setVisible(true);

	}

	/**
	 * Sets the swing "look-and-feel" (theme) to nimbus and then makes it dark.
	 * 
	 * Also changes the default config for the JSyntaxPane stuff. 
	 */
	private static void setDarkNimbus() {

		try {
			// https://stackoverflow.com/questions/36128291/how-to-make-a-swing-application-have-dark-nimbus-theme-netbeans/39482204#39482204
			UIManager.put("control", new Color(128, 128, 128));
			UIManager.put("info", new Color(128, 128, 128));
			UIManager.put("nimbusBase", new Color(18, 30, 49));
			UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
			UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
			UIManager.put("nimbusFocus", new Color(115, 164, 209));
			UIManager.put("nimbusGreen", new Color(176, 179, 50));
			UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
			UIManager.put("nimbusLightBackground", new Color(18, 30, 49));
			UIManager.put("nimbusOrange", new Color(191, 98, 4));
			UIManager.put("nimbusRed", new Color(169, 46, 34));
			UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
			UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
			UIManager.put("text", new Color(230, 230, 230));
			UIManager.setLookAndFeel(new javax.swing.plaf.nimbus.NimbusLookAndFeel());

			// The following sets the syntax appearance configuration.
			// Style is one of: 0 = plain, 1=bold, 2=italic, 3=bold/italic
			Configuration syntaxConfig = DefaultSyntaxKit.getConfig(LuaSyntaxKit.class);
			syntaxConfig.put("LineNumbers.Background", "0x203040");
			syntaxConfig.put("LineNumbers.Foreground", "0xccccee");
			syntaxConfig.put("LineNumbers.CurrentBack", "0x304050");

			syntaxConfig.put("CaretColor", "0xffffff");
			syntaxConfig.put("TokenMarker.Color", "0x403020");
			syntaxConfig.put("PairMarker.Color", "0x665544");

			syntaxConfig.put("Editable.Color", "0x333333");

			// Comments come from the SyntaxStyle lib:
			// Language operators
			syntaxConfig.put("Style.OPERATOR", "0xe6e6e6, 2");

			// Delimiters. Constructs that are not necessarily operators for a
			// language
			syntaxConfig.put("Style.DELIMITER", "0xffffff, 2");

			// language reserved keywords
			syntaxConfig.put("Style.KEYWORD", "0x9999ff, 1");

			// Other language reserved keywords, like C #defines
			syntaxConfig.put("Style.KEYWORD2", "0xffffff, 1");

			// identifiers, variable names, class names
			syntaxConfig.put("Style.IDENTIFIER", "0xffffff, 0");

			// numbers in various formats
			syntaxConfig.put("Style.NUMBER", "0xefefef, 1");

			// String
			syntaxConfig.put("Style.STRING", "0xff7777, 1");

			// For highlighting meta chars within a String
			syntaxConfig.put("Style.STRING2", "0xffffff, 1");

			// comments
			syntaxConfig.put("Style.COMMENT", "0x00ffaa, 0");

			// special stuff within comments
			syntaxConfig.put("Style.COMMENT2", "0xffffff, 0");

			// regular expressions
			syntaxConfig.put("Style.REGEX", "0xffffff, 0");

			// special chars within regular expressions
			syntaxConfig.put("Style.REGEX2", "0xffffff, 0");

			// Types, usually not keywords, but supported by the language
			syntaxConfig.put("Style.TYPE", "0xffffff, 0");

			// Types from standard libraries
			syntaxConfig.put("Style.TYPE2", "0xffffff, 0");

			// Types for users
			syntaxConfig.put("Style.TYPE3", "0xffffff, 0");

			// any other text
			syntaxConfig.put("Style.DEFAULT", "0xffffff, 0");

			// Text that should be highlighted as a warning
			syntaxConfig.put("Style.WARNING", "0xffffff, 0");

			// Text that signals an error
			syntaxConfig.put("Style.ERROR", "0xffffff, 0");

			// _SyntaxCfg.put("Style.NOT_EDITABLE", "0xffffff, 1");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
