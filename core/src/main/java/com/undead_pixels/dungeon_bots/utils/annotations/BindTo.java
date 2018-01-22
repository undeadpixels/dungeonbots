package com.undead_pixels.dungeon_bots.utils.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BindTo {
	String value();
}
