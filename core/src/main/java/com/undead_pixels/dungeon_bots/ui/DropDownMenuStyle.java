package com.undead_pixels.dungeon_bots.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;

public class DropDownMenuStyle extends SelectBoxStyle {

	private DropDownMenuStyle() {}
	public static DropDownMenuStyle fromDefault(){
		DropDownMenuStyle result = new DropDownMenuStyle();
		result.listStyle = new ListStyle();
		result.scrollStyle = new ScrollPaneStyle();
		result.font = new BitmapFont();
		
		return result;
	}
}
