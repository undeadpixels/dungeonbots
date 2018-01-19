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

public class DesktopLauncher {
	
	private static final boolean forceNimbus = true;
	
	public static void main (String[] arg) {
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*
		java.util.Timer t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				DungeonBotsMain.instance.printMenu();
			}
			
		}, 1000, 1000);
		*/
		

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		
		
		JFrame frame = new JFrame("DungeonBots");
		LwjglAWTCanvas canvas = new LwjglAWTCanvas(DungeonBotsMain.instance);
		JPanel rootPanel = new JPanel();
		rootPanel.setLayout(new BorderLayout(0, 0));
		//frame.setLayout(new BorderLayout(0, 0));
		
		DungeonBotsMain.instance.setFrameAndCanvas(frame, rootPanel);
		
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO - deleteme and replace with some kind of listener for saving
		
		rootPanel.add(canvas.getCanvas(), BorderLayout.CENTER);
		//rootPanel.add(new JButton("hi"), BorderLayout.EAST);
		//frame.getLayeredPane().add(rootPanel, new Integer(100));
		//JButton b;
		//frame.getLayeredPane().add(b=new JButton("asdf"), new Integer(500));
		//frame.add(new JLabel("Foo"));
		
		frame.setSize(1024, 768);
		frame.revalidate();
		//frame.add(canvas.getCanvas(), BorderLayout.CENTER);
		frame.add(rootPanel);
		//p.add(canvas.getCanvas());
		//canvas.getCanvas().setSize(800, 600);
		frame.setVisible(true);

		//b.revalidate();
		//System.out.println(b.getLocation() + ", "+b.getSize());
		
		
		
		
		//LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//new LwjglApplication(DungeonBotsMain.instance, config);
	}
}
 