package com.undead_pixels.dungeon_bots.ui;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class DropDownMenu  extends SelectBox<Object> {

	public DropDownMenu(Skin skin){
		this(skin.get(DropDownMenuStyle.class));
	}
	public DropDownMenu(Skin skin, String styleName) {
		this(skin.get(styleName, DropDownMenuStyle.class));
	}
	public DropDownMenu(DropDownMenuStyle style){
		super(style);
	}

	

}


