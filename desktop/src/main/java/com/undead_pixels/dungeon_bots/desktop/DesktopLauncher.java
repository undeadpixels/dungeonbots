package com.undead_pixels.dungeon_bots.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
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
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeEditor;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.syntaxkits.LuaSyntaxKit;

public class DesktopLauncher {
	
	private static final boolean forceNimbus = true;
	
	public static void main (String[] arg) {
		DefaultSyntaxKit.initKit();
		
		// UI theming
		if(forceNimbus) {
			try {
				//if(true) throw new Exception();
				// https://stackoverflow.com/questions/36128291/how-to-make-a-swing-application-have-dark-nimbus-theme-netbeans/39482204#39482204
				UIManager.put( "control", new Color( 128, 128, 128) );
				UIManager.put( "info", new Color(128,128,128) );
				UIManager.put( "nimbusBase", new Color( 18, 30, 49) );
				UIManager.put( "nimbusAlertYellow", new Color( 248, 187, 0) );
				UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128) );
				UIManager.put( "nimbusFocus", new Color(115,164,209) );
				UIManager.put( "nimbusGreen", new Color(176,179,50) );
				UIManager.put( "nimbusInfoBlue", new Color( 66, 139, 221) );
				UIManager.put( "nimbusLightBackground", new Color( 18, 30, 49) );
				UIManager.put( "nimbusOrange", new Color(191,98,4) );
				UIManager.put( "nimbusRed", new Color(169,46,34) );
				UIManager.put( "nimbusSelectedText", new Color( 255, 255, 255) );
				UIManager.put( "nimbusSelectionBackground", new Color( 104, 93, 156) );
				UIManager.put( "text", new Color( 230, 230, 230) );
				UIManager.setLookAndFeel(new javax.swing.plaf.nimbus.NimbusLookAndFeel());
				

				// Style is one of: 0 = plain, 1=bold, 2=italic, 3=bold/italic
				jsyntaxpane.util.Configuration cfg = DefaultSyntaxKit.getConfig(LuaSyntaxKit.class);
				cfg.put("LineNumbers.Background", "0x203040");
				cfg.put("LineNumbers.Foreground", "0xccccee");
				cfg.put("LineNumbers.CurrentBack", "0x304050");

				cfg.put("CaretColor", "0xffffff");
				cfg.put("TokenMarker.Color", "0x403020");
				cfg.put("PairMarker.Color", "0x665544");
				
				cfg.put("Style.COMMENT", "0x00ffaa, 0");
				cfg.put("Style.OPERATOR", "0xe6e6e6, 2");
				cfg.put("Style.KEYWORD", "0x9999ff, 1");
				cfg.put("Style.STRING", "0xff7777, 1");
				cfg.put("Style.NUMBER", "0xefefef, 1");
				cfg.put("Style.IDENTIFIER", "0xffffff, 0");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		
		
		
		// create the GL canvas
		JFrame frame = new JFrame("DungeonBots");
		LwjglAWTCanvas canvas = new LwjglAWTCanvas(DungeonBotsMain.instance);
		frame.setLayout(new BorderLayout(0, 0));
		
		DungeonBotsMain.instance.setFrame(frame);
		
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO - deleteme and replace with some kind of listener for saving
		
		// =====================================================
		// THE FOLLOWING DEMONSTRATES HOW TO USE THE NEW JCodeEditor!!!!
		// =====================================================
		frame.add(canvas.getCanvas(), BorderLayout.CENTER);
		JCodeEditor editor = new JCodeEditor();
		frame.add(editor, BorderLayout.WEST);
		editor.message("This message is sent from some old object", canvas);
		editor.message("This message will be in the form of an internal echo from the editor itself", editor);
		editor.message("Turmoil has engulfed the Galactic Republic. The taxation of trade routes to outlying star systems is in dispute.\n\nHoping to resolve the matter with a blockade of deadly battleships, the greedy Trade Federation has stopped all shipping to the small planet of Naboo.\n\nWhile the congress of the Republic endlessly debates this alarming chain of events, the Supreme Chancellor has secretly dispatched two Jedi Knights, the guardians of peace and justice in the galaxy, to settle the conflict....", canvas);
		editor.message("Egads!  Not trade routes in dispute!", editor);
		editor.message("There is unrest in the Galactic Senate. Several thousand solar systems have declared their intentions to leave the Republic. This separatist movement, under the leadership of the mysterious Count Dooku, has made it difficult for the limited number of Jedi Knights to maintain peace and order in the galaxy. Senator Amidala, the former Queen of Naboo, is returning to the Galactic Senate to vote on the critical issue of creating an ARMY OF THE REPUBLIC to assist the overwhelmed Jedi....", canvas);
		editor.message("In retrospect, perhaps relying on a small group of religious zealots for galaxy-wide security may have been a mistake.", editor);
		editor.message("War! The Republic is crumbling under attacks by the ruthless Sith Lord, Count Dooku. There are heroes on both sides. Evil is everywhere. In a stunning move, the fiendish droid leader, General Grievous, has swept into the Republic capital and kidnapped Chancellor Palpatine, leader of the Galactic Senate. As the Separatist Droid Army attempts to flee the besieged capital with their valuable hostage, two Jedi Knights lead a desperate mission to rescue the captive Chancellor....", canvas);
		editor.message("Jeez.  It took you how many movies to get to the good stuff?  You should have just called your self 'Star Ways and Means Committee from the beginning'.", editor);
		
		frame.setSize(1024, 768);
		frame.revalidate();
		frame.setVisible(true);
	}
}
 