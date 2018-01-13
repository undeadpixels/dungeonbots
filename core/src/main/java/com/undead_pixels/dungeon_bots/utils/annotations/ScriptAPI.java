package com.undead_pixels.dungeon_bots.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ScriptAPI {
    String value();
    SecurityLevel security() default SecurityLevel.DEBUG;
}
