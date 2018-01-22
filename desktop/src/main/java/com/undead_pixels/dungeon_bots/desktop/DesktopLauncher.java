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

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.syntaxkits.LuaSyntaxKit;

public class DesktopLauncher {
	
	private static final boolean forceNimbus = true;
	
	public static void main (String[] arg) {
		DefaultSyntaxKit.initKit();
		
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
		
		
		JFrame frame = new JFrame("DungeonBots");
		LwjglAWTCanvas canvas = new LwjglAWTCanvas(DungeonBotsMain.instance);
		frame.setLayout(new BorderLayout(0, 0));
		
		DungeonBotsMain.instance.setFrame(frame);
		
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO - deleteme and replace with some kind of listener for saving
		
		frame.add(canvas.getCanvas(), BorderLayout.CENTER);
		
		frame.setSize(1024, 768);
		frame.revalidate();
		frame.setVisible(true);
	}
}
 