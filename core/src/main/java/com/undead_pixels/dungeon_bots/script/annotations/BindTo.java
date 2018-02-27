package com.undead_pixels.dungeon_bots.script.annotations;

import java.lang.annotation.*;

/** TODO: what does this signify? */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.TYPE })
public @interface BindTo {
	String value();
}
