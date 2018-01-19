package com.undead_pixels.dungeon_bots;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

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
				JButton btn = new JButton("Button "+(++numInternalFrames));
				addWindowFor(btn, "Window # "+(numInternalFrames));
			}
		});

		b.add(new JLabel("Hi, I'm Swing"));
		b.add(btn);
		b.add(btn2);
		b.add(new java.awt.Button("Heavy"));
		
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
		
		makeGuiRight();
		

		
		JMenuBar mb = new JMenuBar();
		JMenu file = new JMenu("File");
		file.add(new JMenuItem("Foo"));
		file.add(new JMenuItem("Bar"));
		file.add(new JMenuItem("Baz"));
		mb.add(file);
		
		

		setJMenuBar(mb);
		
		
		
		
		Gdx.input.setInputProcessor(stage);

		// Relying on tutorials at
		// https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java#L37

		Pixmap whitePM = new Pixmap(1, 1, Format.RGBA8888);
		whitePM.setColor(Color.WHITE);
		whitePM.fill();
		skin.add("white", new Texture(whitePM));

		// Not sure what a table does vis-a-vis a stage...
		table.setFillParent(true);
		stage.addActor(table);
		table.setDebug(true);

		skin.add("defaultFont", new BitmapFont()); // Use the default font
													// for now.

		// Set up the standard textbutton style.
		TextButtonStyle style = new TextButtonStyle();
		style.up = skin.newDrawable("white", Color.DARK_GRAY);
		style.down = skin.newDrawable("white", Color.DARK_GRAY);
		style.checked = skin.newDrawable("white", Color.GREEN);
		style.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		style.font = skin.getFont("defaultFont");
		skin.add("default", style);

		TextButton button1 = new TextButton("Here!", style);
		table.add(button1);

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