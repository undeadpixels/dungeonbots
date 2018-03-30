package com.undead_pixels.dungeon_bots.script.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Bind {
	SecurityLevel value() default SecurityLevel.AUTHOR;
	String doc() default "";
}
