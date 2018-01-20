package com.undead_pixels.dungeon_bots;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.undead_pixels.dungeon_bots.ui.GDXandSwingScreen;

import jsyntaxpane.DefaultSyntaxKit;

/**
 * This will be deleted eventually, but it at least allows us to have a fake
 * screen
 */
public class NullScreen extends GDXandSwingScreen {

	SpriteBatch batch = new SpriteBatch();
	Texture img = new Texture("badlogic.jpg");

	private Stage stage = new Stage();
	private Table table = new Table();
	private Skin skin = new Skin();

	public NullScreen() {
		create();
	}
	
	public void makeEditorPane() {
		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;
		
		menuBar = new JMenuBar();
		menu = new JMenu("AMenu");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Accessible description.");
		menuBar.add(menu);

		String initialURL = "http://www.java.com/";


		JLabel lblURL = new JLabel("URL");
		final JTextField txtURL = new JTextField(initialURL, 30);
		JButton btnBrowse = new JButton("Browse");


		Box panel = new Box(BoxLayout.Y_AXIS);
		Box topPanel = new Box(BoxLayout.X_AXIS);
		topPanel.add(lblURL);
		topPanel.add(txtURL);
		topPanel.add(btnBrowse);
		topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)(txtURL.getPreferredSize().getHeight()) + 10));

		//topPanel.setPreferredSize(new Dimension(400, (int) (txtURL.getPreferredSize().getHeight())));

		panel.add(topPanel);

		DefaultSyntaxKit.initKit();
		JEditorPane jep = new JEditorPane();
		JScrollPane scl = new JScrollPane(jep);
		jep.setContentType("text/lua");
		jep.setText("-- this is a test\n\nfunction f()\n    foo()\nend\n");
		
		//jep.setBackground(java.awt.Color.WHITE);

		scl.setPreferredSize(new Dimension(400, 400));

		//LuaSyntaxKit lsk = new LuaSyntaxKit();
		//jep.setEditorKit(lsk);

		panel.add(scl);

		addWindowFor(panel, "An Editor", menuBar);
	}

	private int numInternalFrames = 0;
	public void makeGuiRight() {
		Box b = new Box(BoxLayout.Y_AXIS);
		JButton btn = new JButton("Bye!");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removePane(b);
				makeGuiBottom();
			}
		});

		JButton btn2 = new JButton("Window!");
		b.add(btn);
		btn2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				makeEditorPane();
			}
		});

		b.add(new JLabel("Hi, I'm Swing"));
		b.add(btn);
		b.add(btn2);
		
		addPane(b, BorderLayout.EAST);
	}
	
	public void makeGuiBottom() {
		Box b = new Box(BoxLayout.X_AXIS);

		JButton btn = new JButton("Come Back!!");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removePane(b);
				makeGuiRight();
			}
		});
		b.add(btn);

		addPane(b, BorderLayout.SOUTH);
	}
	
	public void create() {
		System.out.println("Creating NullScreen...");
		Gdx.input.setInputProcessor(stage);

		//Set up the skin.
		skin = new Skin(Gdx.files.internal("uiskin.json"));
		Pixmap whitePM = new Pixmap(1, 1, Format.RGBA8888);
		whitePM.setColor(Color.WHITE);
		whitePM.fill();
		Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
		skin.add("white", new Texture(whitePM));
		skin.add("defaultFont", new BitmapFont()); // Use the default font
		

		// Not sure what a table does vis-a-vis a stage...
		table.setFillParent(true);
		stage.addActor(table);
		table.setDebug(true);

		
		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;
		
		menuBar = new JMenuBar();
		menu = new JMenu("AMenu");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Accessible description.");
		menuBar.add(menu);
		
		
		
		makeGuiRight();
		

		
		JMenuBar mb = new JMenuBar();
		JMenu file = new JMenu("File");
		file.add(new JMenuItem("Foo"));
		file.add(new JMenuItem("Bar"));
		file.add(new JMenuItem("Baz"));
		mb.add(file);
		
		

		setMainJMenuBar(mb);

		System.out.println("Created NullScreen");
	}
	
	public void resize(int w, int h) {
		// TODO - we need this soemhow
		stage.getViewport().update(w, h, true);
		stage.getCamera().update();
		batch.setProjectionMatrix(stage.getCamera().projection);
		System.out.println("Resize: "+w+", "+h);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();

		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void dispose() {
		batch.dispose();
		img.dispose();
	}

}