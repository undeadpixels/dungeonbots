package com.undead_pixels.dungeon_bots.utils.annotations;

import java.lang.annotation.*;

/**
 * Annotation Class intended to be appended to Zero Argument methods on any datatype
 * that returns a LuaBinding to an instance.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ScriptAPI {
    SecurityLevel value() default SecurityLevel.DEFAULT;
}
