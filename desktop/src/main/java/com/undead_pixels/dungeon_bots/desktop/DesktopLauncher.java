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

public class DesktopLauncher {

	private static final boolean forceNimbus = true;

	private static jsyntaxpane.util.Configuration _SyntaxCfg;

	public static void main(String[] arg) {

		System.out.println("Starting login...");
		User user = Login.challenge("Welcome to DungeonBots.");
		if (user == null) {
			System.out.println("Invalid user login.  Closing program.");
			return;
		}
		System.out.println("Login valid.");

		DefaultSyntaxKit.initKit();

		// UI theming
		if (forceNimbus)
			setDarkNimbus();

		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// create the GL canvas
		JFrame frame = new JFrame("DungeonBots");
		LwjglAWTCanvas canvas = new LwjglAWTCanvas(DungeonBotsMain.instance);
		frame.setLayout(new BorderLayout(0, 0));

		DungeonBotsMain game = DungeonBotsMain.instance;
		game.setUser(user);
		game.setFrame(frame);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO - deleteme
																// and replace
																// with some
																// kind of
																// listener for
																// saving

		frame.add(canvas.getCanvas(), BorderLayout.CENTER);

		// =====================================================
		// THE FOLLOWING DEMONSTRATES HOW TO USE THE NEW JCodeREPL and
		// JCodeEditor!!!!
		// =====================================================

		JPanel demoPanel = new JPanel(new BorderLayout());
		frame.add(demoPanel, BorderLayout.LINE_START);

		JCodeREPL repl = new JCodeREPL();
		repl.setPreferredSize(new Dimension(300, 400));
		repl.message("This is the REPL message window.", null);
		repl.setCode("x=5+4");
		repl.execute(100);
		repl.setCode("return x  --Waiting for execution...");
		demoPanel.add(repl, BorderLayout.PAGE_START);

		JCodeEditor editor = new JCodeEditor();
		editor.setCode("-- this is a test\n\n"
				+ "function f()\n"
				+ "    foo()\n"
				+ "    bar = baz * 16\n"
				+ "    s = \"str\" .. 1\n"
				+ "    if true then\n"
				+ "        print(\"something was true\")\n"
				+ "    end\n"
				+ "end\n");
		
		demoPanel.add(editor, BorderLayout.AFTER_LINE_ENDS);

		frame.setSize(1024, 768);
		frame.revalidate();
		frame.setVisible(true);
	}

	private static void setDarkNimbus() {
		// Sure...

		try {
			// if(true) throw new Exception();
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
			_SyntaxCfg = DefaultSyntaxKit.getConfig(LuaSyntaxKit.class);
			_SyntaxCfg.put("LineNumbers.Background", "0x203040");
			_SyntaxCfg.put("LineNumbers.Foreground", "0xccccee");
			_SyntaxCfg.put("LineNumbers.CurrentBack", "0x304050");

			_SyntaxCfg.put("CaretColor", "0xffffff");
			_SyntaxCfg.put("TokenMarker.Color", "0x403020");
			_SyntaxCfg.put("PairMarker.Color", "0x665544");
			
			_SyntaxCfg.put("Editable.Color",  "0x333333");

			//Comments come from the SyntaxStyle lib:			
			// Language operators
			_SyntaxCfg.put("Style.OPERATOR", "0xe6e6e6, 2");

			// Delimiters. Constructs that are not necessarily operators for a
			// language
			_SyntaxCfg.put("Style.DELIMITER", "0xffffff, 2");
			
			//language reserved keywords
			_SyntaxCfg.put("Style.KEYWORD", "0x9999ff, 1");
			
			//Other language reserved keywords, like C #defines
			_SyntaxCfg.put("Style.KEYWORD2", "0xffffff, 1");
			
			//identifiers, variable names, class names
			_SyntaxCfg.put("Style.IDENTIFIER", "0xffffff, 0");

			// numbers in various formats
			_SyntaxCfg.put("Style.NUMBER", "0xefefef, 1");

			// String
			_SyntaxCfg.put("Style.STRING", "0xff7777, 1");
			
			//For highlighting meta chars within a String
			_SyntaxCfg.put("Style.STRING2", "0xffffff, 1");
			
			//comments
			_SyntaxCfg.put("Style.COMMENT", "0x00ffaa, 0");
			
			// special stuff within comments
			_SyntaxCfg.put("Style.COMMENT2", "0xffffff, 0");
			
			// regular expressions
			_SyntaxCfg.put("Style.REGEX", "0xffffff, 0");
			
			// special chars within regular expressions
			_SyntaxCfg.put("Style.REGEX2", "0xffffff, 0");
			
			// Types, usually not keywords, but supported by the language
			_SyntaxCfg.put("Style.TYPE", "0xffffff, 0");
			
			// Types from standard libraries
			_SyntaxCfg.put("Style.TYPE2", "0xffffff, 0");
			
			// Types for users
			_SyntaxCfg.put("Style.TYPE3", "0xffffff, 0");
			
			// any other text
			_SyntaxCfg.put("Style.DEFAULT", "0xffffff, 0");
			
			// Text that should be highlighted as a warning
			_SyntaxCfg.put("Style.WARNING", "0xffffff, 0");
			
			// Text that signals an error
			_SyntaxCfg.put("Style.ERROR", "0xffffff, 0");

			//_SyntaxCfg.put("Style.NOT_EDITABLE", "0xffffff, 1");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
