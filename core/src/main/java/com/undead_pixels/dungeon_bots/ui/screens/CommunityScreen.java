package com.undead_pixels.dungeon_bots.ui.screens;

import java.awt.Container;
import java.net.URI;
import java.net.URISyntaxException;

public class CommunityScreen extends Screen {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final URI homeURI;
	
	static {
		URI homeURItmp;
		try {
			homeURItmp = new URI("https://dungeonbots.herokuapp.com");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			homeURItmp = null;
		}
		homeURI = homeURItmp;
	}
	
	protected CommunityScreen() {
		super();
		// TODO Auto-generated constructor stub
	}

	

	@Override
	protected ScreenController makeController() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void addComponents(Container pane) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setDefaultLayout() {
		// TODO Auto-generated method stub
		
	}

}
