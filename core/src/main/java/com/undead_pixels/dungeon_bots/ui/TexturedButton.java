package com.undead_pixels.dungeon_bots.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javax.swing.JButton;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;

public class TexturedButton extends JButton {

	private Font font = new Font("Arial", Font.BOLD, 52);
	private TextureRegion[][] textures = new TextureRegion[3][3];
	private int zoom;
	
	private TextureRegion original;
	
	public TexturedButton(String text, TextureRegion tex, int zoom) {
		super(text);
		// TextureRegion buttonTexture = new TextureRegion(AssetManager.getTexture("DawnLike/GUI/GUI1.png"), 128, 160, 16, 16);
		
		int w = tex.getW();
		int h = tex.getH();
		int[] xs = new int[] {0, (w-1)/2, (w+1)/2, w};
		int[] ys = new int[] {0, (h-1)/2, (h+1)/2, h};
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				textures[i][j] = tex.subregion(xs[j], ys[i], xs[j+1]-xs[j], ys[i+1]-ys[i]);
			}
		}
		
		original = tex;
		this.zoom = zoom;
		this.setPreferredSize(new Dimension(400, 80));
	}
	
	@Override
	public void paint(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		
		g.setColor(Color.red);
		g.fillRect(0, 0, w, h);
		
		int[] xs = new int[] {0, textures[0][0].getW()*zoom, w - textures[0][2].getW()*zoom, w};
		int[] ys = new int[] {0, textures[0][0].getH()*zoom, h - textures[2][0].getH()*zoom, h};
		
		g.drawImage(original.getTex().getImg(), 0, 0, 16, 16, original.getX(), original.getY(), original.getX2(), original.getY2(), null);
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				TextureRegion tr = textures[i][j];
				
				System.out.println(tr.getTex().getImg()+Arrays.toString(new int[] {xs[j], ys[i], xs[j+1], ys[i+1],
						999999999,
						tr.getX(), tr.getY(), tr.getX2(), tr.getY2()}));
				
				g.drawImage(tr.getTex().getImg(), xs[j], ys[i], xs[j+1], ys[i+1],
						tr.getX(), tr.getY(), tr.getX2(), tr.getY2(),
						null);
			}
		}

		FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
		Rectangle2D bounds = font.getStringBounds(this.getText(), frc);
		
		g.setColor(Color.white);
		g.setFont(font);
		g.drawString(this.getText(), (int)(-bounds.getX() - bounds.getWidth()/2 + w/2), (int)(-bounds.getY() - bounds.getHeight()/2 + h/2));
		System.out.println(this.getText() + " - "+ (int)(-bounds.getX() + bounds.getWidth()/2) + " - "+ (int)(-bounds.getY() + bounds.getHeight()/2));
	}
	
}
