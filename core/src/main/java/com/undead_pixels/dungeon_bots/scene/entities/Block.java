package com.undead_pixels.dungeon_bots.scene.entities;

import com.undead_pixels.dungeon_bots.nogdx.TextureRegion;
import com.undead_pixels.dungeon_bots.scene.LoggingLevel;
import com.undead_pixels.dungeon_bots.scene.TeamFlavor;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.UserScript;
import com.undead_pixels.dungeon_bots.scene.entities.actions.Action;
import com.undead_pixels.dungeon_bots.scene.entities.actions.OnlyOneOfActions;
import com.undead_pixels.dungeon_bots.scene.entities.actions.SequentialActions;
import com.undead_pixels.dungeon_bots.scene.entities.actions.SpriteAnimatedAction;
import com.undead_pixels.dungeon_bots.script.SandboxManager;
import com.undead_pixels.dungeon_bots.script.UserScriptCollection;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.BindTo;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaValue;

import java.awt.*;

public class Block extends Actor implements Pushable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final TextureRegion DEFAULT_TEXTURE =
			AssetManager.getTextureRegion("DawnLike/Objects/Tile.png", 3, 0);

	private Boolean isMoveable = true;

	public Block(World world, float x, float y) {
		super(world, "block", DEFAULT_TEXTURE, new UserScriptCollection(), x, y);
		if(isMoveable) {
			this.getScripts().add(new UserScript("init",
					"registerBumpedListener(function(e, dir)\n"
					+ "  this:move(dir)\n"
					+ "end)\n"));
		}
	}

	/**
	 *
	 * @param world
	 * @param x
	 * @param y
	 * @return
	 */
	@BindTo("new")
	@Bind(value = SecurityLevel.TEAM, doc = "Create a new Block")
	public static Block create(
			@Doc("The World that contains the Block") LuaValue world,
			@Doc("The X position of the Block") LuaValue x,
			@Doc("The Y position of the Block") LuaValue y) {
		return new Block(userDataOf(World.class, world), x.tofloat(), y.tofloat());
	}

	/**
	 *
	 * @param movable
	 * @return
	 */
	@Bind(value = SecurityLevel.ENTITY, doc = "Set the entity to a movable state with a boolean flag")
	public Block setMovable(
			@Doc("A Boolean: True if Block should be moveable, false otherwise") LuaValue movable) {
		this.isMoveable = movable.checkboolean();
		return this;
	}

	@Override
	public LuaSandbox createSandbox() {
		LuaSandbox sandbox = super.createSandbox();
		sandbox.registerEventType("PUSH", "Called when this block is pushed", "direction");
		sandbox.registerEventType("BUMPED", "Called when this block is bumped into", "direction");
		sandbox.registerEventType("MOVED",
				"Called after the block has moved",
				"The X position of the block",
				"The Y position of the block");
		world.listenTo(World.EntityEventType.ENTITY_MOVED, this, e -> {
			if(e == this) {
				getSandbox().fireEvent("MOVED",
						LuaValue.valueOf(getPosition().x + 1),
						LuaValue.valueOf(getPosition().y + 1));
			}
		});
		return sandbox;
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public float getZ() {
		return 10f;
	}

	@Override
	public TeamFlavor getTeam() {
		return TeamFlavor.AUTHOR;
	}

	@Override
	public void push(final Actor.Direction direction) {
		if(isMoveable) {
			queueMoveSlowly(direction, true);
		}
		getSandbox().fireEvent("PUSH", LuaValue.valueOf(direction.name()));
	}

	public void bumpedInto(final Entity e, final Actor.Direction direction) {
		getSandbox().fireEvent("BUMPED", e.getLuaValue(), LuaValue.valueOf(direction.name()));
		world.message(this, String.format("Bumped %s", direction.name().toLowerCase()), LoggingLevel.GENERAL);
	}

	@Override
	public Image getImage() {
		return DEFAULT_TEXTURE.toImage();
	}

	@Override
	public String inspect() {
		return "A Heavy Block of Ice. Perhaps you can push it?";
	}

	/**
	 * Enqueues an action to the action queue that directs the Actor to
	 * move in the provided direction
	 * @param dir The direction to move
	 * @param blocking
	 */
	public void queueMoveSlowly(Direction dir, boolean blocking) {
		int dx = 0, dy = 0;

		switch (dir) {
			case UP:
				dy = 1;
				break;
			case DOWN:
				dy = -1;
				break;
			case LEFT:
				dx = -1;
				break;
			case RIGHT:
				dx = 1;
				break;
		}

		Entity e = this;
		final int _dx = dx, _dy = dy;
		final int[] initialPos = {0, 0};
		SpriteAnimatedAction tryMoveAction = new SpriteAnimatedAction(sprite, getMoveDuration()) {

			int _x = 0;
			int _y = 0;
			public boolean preAct() {
				initialPos[0] = Math.round(e.getPosition().x);
				initialPos[1] = Math.round(e.getPosition().y);
				_x = initialPos[0] + _dx;
				_y = initialPos[1] + _dy;
				boolean canMove = world.requestMoveToNewTile(e, _x, _y);
				if(canMove) steps++; else bumps++;
				this.setFinalPosition(_x, _y);
				return canMove;
			}

			public void postAct() {
				world.didLeaveTile(e, initialPos[0], initialPos[1]);
				if(world.fillIfPit(_x, _y)) {
					world.queueRemove(e);
				}
			}
		};

		Action fail1 = new SpriteAnimatedAction(sprite, .2f) {
			public boolean preAct() {
				this.setFinalPosition(_dx*.2f + initialPos[0], _dy*.2f + initialPos[1]);
				return true;
			}
		};
		Action fail2 = new SpriteAnimatedAction(sprite, .1f) {
			public boolean preAct() {
				this.setFinalPosition(initialPos[0], initialPos[1]);
				return true;
			}
		};
		Action moveFailAction = new SequentialActions(fail1, fail2);

		if(blocking) {
			SandboxManager.getCurrentSandbox().safeWaitUntil(() -> actionQueue.isEmpty());
		}

		actionQueue.enqueue(new OnlyOneOfActions(tryMoveAction, moveFailAction));

		if(blocking) {
			SandboxManager.getCurrentSandbox().safeWaitUntil(() -> actionQueue.isEmpty());
		}
	}
}
