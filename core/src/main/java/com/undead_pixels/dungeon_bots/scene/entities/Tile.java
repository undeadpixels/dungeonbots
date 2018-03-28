package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.RenderingContext;
import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.BatchRenderable;
import com.undead_pixels.dungeon_bots.scene.TileType;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.interfaces.GetLuaFacade;
import com.undead_pixels.dungeon_bots.script.proxy.LuaProxyFactory;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;

import org.luaj.vm2.LuaValue;

/**
 * A tile in the terrain
 */
public class Tile implements HasImage, BatchRenderable, GetLuaFacade, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Lazily-loaded LuaValue representing this tile
	 */
	private transient LuaValue luaValue;

	/**
	 * The type of this tile
	 */
	private TileType type;

	/**
	 * The entity that is currently occupying this Tile
	 */
	private Entity occupiedBy = null;
	
	/**
	 * The current texture of this tile, based on its neighbors
	 */
	private TextureRegion currentTexture;
	
	/**
	 * Position of this tile
	 */
	private int x, y;


	/**
	 * @param world		The world that contains this tile
	 * @param tileType	The (initial) type of tile
	 * @param x			Location X, in tiles
	 * @param y			Location Y, in tiles
	 */
	public Tile(World world, TileType tileType, int x, int y) {
		this.type = tileType;
		if(tileType != null) {
			this.currentTexture = tileType.getTexture();
		} else {
			this.currentTexture = null;
		}
		
		this.x = x;
		this.y = y;
	}


	public boolean isSolid() {
		return type != null && type.isSolid();
	}


	@Override
	public LuaValue getLuaValue() {
		if (this.luaValue == null)
			this.luaValue = LuaProxyFactory.getLuaValue(this);
		return this.luaValue;
	}


	/**
	 * @param tileType The new type of this Tile
	 */
	public void setType(TileType tileType) {
		this.type = tileType;
	}


	/**
	 * Returns the TileType (which contains image display information and other
	 * default characteristics) of this tile.
	 * 
	 * @return	The current TileType
	 */
	public TileType getType() {
		return type;
	}


	/**
	 * Updates the texture of the tile based on its neighbors.
	 * 
	 * @param l
	 * @param r
	 * @param u
	 * @param d
	 */
	public void updateTexture(Tile l, Tile r, Tile u, Tile d) {
		if (type == null) {
			currentTexture = null;
		} else {
			currentTexture = type.getTexture(l, r, u, d);
		}
	}


	/**
	 * @param e	The entity now occupying this tile
	 */
	public void setOccupiedBy(Entity e) {
		occupiedBy = e;
	}


	/**
	 * @return	The entity currently occupying this tile.  If there is no such entity, returns null.
	 */
	public Entity getOccupiedBy() {
		return occupiedBy;
	}


	/**
	 * @return	True if something is occupying this tile
	 */
	public boolean isOccupied() {
		return occupiedBy != null;
	}


	@Override
	public void update (float dt) {
	}


	/* (non-Javadoc)
	 * @see com.undead_pixels.dungeon_bots.scene.BatchRenderable#render(com.undead_pixels.dungeon_bots.nogdx.RenderingContext)
	 */
	@Override
	public void render (RenderingContext batch) {
		if(currentTexture != null) {
			AffineTransform xform;
			xform = AffineTransform.getTranslateInstance((.5f-.5f) + x, .5f + .5f+y);
			xform.scale(1 / currentTexture.getW(), -1 / currentTexture.getH());
			batch.draw(currentTexture, xform);
		}
	}


	/**
	 * @return	The position of this tile
	 */
	public Point2D.Float getPosition() {
		return new Point2D.Float(x, y);
	}


	@Override
	public float getZ () {
		return 0;
	}

	@Override
	/**Returns a new image associated with this SpriteEntity.*/
	public Image getImage() {
		return currentTexture.toImage();
	}
}
