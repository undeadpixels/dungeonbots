package com.undead_pixels.dungeon_bots.scene.level;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**A distinct class to enforce correct serialization of an image.  The reason for this adapter is that 
 * BufferedImage objects have references to themselves, forcing serializers to endlessly recurse and 
 * serialize and object.  I haven't figured out how to stop this behavior with Gson, so I'm using this 
 * little hack.*/
public class ImageList extends ArrayList<BufferedImage>{

}
