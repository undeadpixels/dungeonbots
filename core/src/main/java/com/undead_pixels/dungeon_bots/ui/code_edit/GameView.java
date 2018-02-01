package com.undead_pixels.dungeon_bots.ui.code_edit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.undead_pixels.dungeon_bots.scene.TileTypes;
import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Actor;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.managers.AssetManager;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;

import java.io.File;

/**
 * The screen for the regular game
 */
public class GameView extends GDXandSwingScreen implements InputProcessor {
	private Stage stage = new Stage(); // deleting this somehow makes it not work...?

	SpriteBatch batch;
	
	OrthographicCamera cam;
	private boolean didInitCam = false;
	
	private World world;
	
	public GameView() {
		final int TILESIZE = 16;
		AssetManager.loadAsset(AssetManager.AssetSrc.Player, Texture.class);
		AssetManager.finishLoading();
		//Player.PLAYER_TEXTURE = new TextureRegion(new Texture("DawnLike/Characters/Player0.png"), TILESIZE *1, TILESIZE *1, TILESIZE, TILESIZE);
		world = new World();
		LuaSandbox sandbox = world.getMapSandbox();

		TileTypes tt = new TileTypes();

		// TODO - visually test these all at some point
		Vector2[] offsetsWalls = new Vector2[] {
				new Vector2(1, 1), // 0 default
				new Vector2(1, 0), // 1 only left
				new Vector2(1, 0), // 2 only right
				new Vector2(1, 0), // 3 only left+right
				new Vector2(0, 1), // 4 only up
				new Vector2(2, 2), // 5 only up+left
				new Vector2(0, 2), // 6 only up+right
				new Vector2(4, 2), // 7 no down
				new Vector2(0, 1), // 8 only down
				new Vector2(2, 0), // 9 only down+left
				new Vector2(0, 0), // A only down+right
				new Vector2(4, 0), // B no up
				new Vector2(0, 1), // C only down+up
				new Vector2(5, 1), // D no right
				new Vector2(3, 1), // E no left
				new Vector2(4, 1), // F all
		};

		Vector2[] offsetsFloors = new Vector2[] {
				new Vector2(5, 0), // 0 default
				new Vector2(6, 1), // 1 only left
				new Vector2(4, 1), // 2 only right
				new Vector2(5, 1), // 3 only left+right
				new Vector2(3, 2), // 4 only up
				new Vector2(2, 2), // 5 only up+left
				new Vector2(0, 2), // 6 only up+right
				new Vector2(1, 2), // 7 no down
				new Vector2(3, 0), // 8 only down
				new Vector2(2, 0), // 9 only down+left
				new Vector2(0, 0), // A only down+right
				new Vector2(1, 0), // B no up
				new Vector2(3, 1), // C only down+up
				new Vector2(2, 1), // D no right
				new Vector2(0, 1), // E no left
				new Vector2(1, 1), // F all
		};
		
		
		tt.registerTile("floor", new Texture("DawnLike/Objects/Floor.png"), TILESIZE, 0, 3, offsetsFloors, false);
		tt.registerTile("wall", new Texture("DawnLike/Objects/Wall.png"), TILESIZE, 0, 3, offsetsWalls, false);

		sandbox.addBindable(world, tt, world.getWhitelist()).addBindableClass(Player.class);
		LuaScript luaScript = sandbox.script(new File("sample-level-packs/sample-pack-1/levels/level1.lua")).start().join();
		assert luaScript.getStatus() == ScriptStatus.COMPLETE && luaScript.getResults().isPresent();
		LuaTable tbl = luaScript.getResults().get().checktable(1);
		LuaFunction luaFunction = tbl.get("init").checkfunction();
		LuaFunction mapUpdate = tbl.get("update").checkfunction();

		world.addMapUpdate(mapUpdate);
		luaFunction.invoke();
		//Gdx.input.setInputProcessor(stage);
		Gdx.input.setInputProcessor(this);
	}
	
	public void resize(int w, int h) {
		// TODO - we need this somehow
		//stage.getViewport().update(w, h, true);
		//batch.setProjectionMatrix(stage.getCamera().projection);
		//batch.setTransformMatrix(stage.getCamera().view);
		//OrthographicCamera cam = new OrthographicCamera(w, h);
		//cam.translate(w/2, h/2);
		//cam.update();
		//batch.setProjectionMatrix(cam.projection);
		//batch.setTransformMatrix(cam.view);
	}
		
	
	public void render(float dt) {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		if(!didInitCam) {
			batch = new SpriteBatch();
			cam = new OrthographicCamera(w, h);
			//cam = new AFixedOrthographicCameraBecauseGDXsDefaultOrthographicCameraHasABugInItThatTookHoursToFigureOut(w, h);
			
			float ratioW = w / world.getSize().x;
			float ratioH = h / world.getSize().y;
			if(ratioW < ratioH) {
				cam.zoom = 1.0f / ratioW;
			} else {
				cam.zoom = 1.0f / ratioH;
			}
    			cam.position.x = world.getSize().x/2;
    			cam.position.y = world.getSize().y/2;
    			didInitCam = true;
		}

		cam.viewportWidth = w;
		cam.viewportHeight = h;
		
		cam.update();
		batch.setProjectionMatrix(cam.combined);
		//batch.setTransformMatrix(cam.view);
		
		if(world != null) {
			world.update(dt);
			world.render(batch);
		}

		//Gdx.gl.glClearColor(1, 0, 0, 1);
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//batch.begin();
		//batch.draw(img, 7.5f, 7.5f, 1, 1);
		//batch.end();
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		//System.out.println("raw: "+screenX+", "+screenY);
		cam.update();
		Vector3 gameSpace = cam.unproject(new Vector3(screenX, screenY, 0));
		
		Entity e = world.getEntityUnderLocation(gameSpace.x, gameSpace.y);
		
		if(e instanceof Player) {
			LuaSandbox env = e.getSandbox();

			JCodeREPL repl = new JCodeREPL(env);
			Object o = new Object();
			/**
			repl.message("This message is sent from some old object", o);
			repl.message("This message will be in the form of an internal echo from the editor itself", repl);
			repl.message("Turmoil has engulfed the Galactic Republic. The taxation of trade routes to outlying "
					+ "star systems is in dispute.\n\nHoping to resolve the matter with a blockade of deadly "
					+ "battleships, the greedy Trade Federation has stopped all shipping to the small planet of "
					+ "Naboo.\n\nWhile the congress of the Republic endlessly debates this alarming chain of "
					+ "events, the Supreme Chancellor has secretly dispatched two Jedi Knights, the guardians "
					+ "of peace and justice in the galaxy, to settle the conflict....", o);
			repl.message("Egads!  Not trade routes in dispute!", repl);
			repl.message("There is unrest in the Galactic Senate. Several thousand solar systems have declared "
					+ "their intentions to leave the Republic. This separatist movement, under the leadership "
					+ "of the mysterious Count Dooku, has made it difficult for the limited number of Jedi "
					+ "Knights to maintain peace and order in the galaxy. Senator Amidala, the former Queen of "
					+ "Naboo, is returning to the Galactic Senate to vote on the critical issue of creating an "
					+ "ARMY OF THE REPUBLIC to assist the overwhelmed Jedi....", o);
			repl.message("In retrospect, perhaps relying on a small group of religious zealots for galaxy-wide "
					+ "security may have been a mistake.", repl);
			repl.message("War! The Republic is crumbling under attacks by the ruthless Sith Lord, Count Dooku. "
					+ "There are heroes on both sides. Evil is everywhere. In a stunning move, the fiendish "
					+ "droid leader, General Grievous, has swept into the Republic capital and kidnapped "
					+ "Chancellor Palpatine, leader of the Galactic Senate. As the Separatist Droid Army "
					+ "attempts to flee the besieged capital with their valuable hostage, two Jedi Knights "
					+ "lead a desperate mission to rescue the captive Chancellor....", o);
			repl.message("Jeez.  It took you how many movies to get to the good stuff?  You should have just "
					+ "called your self 'Star Ways and Means Committee from the beginning'.", repl);
			repl.setCode("x=5+4");
			repl.execute(100);
			repl.setCode("return x");
			 */
			this.addWindowFor(repl, "Player's REPL");
			
		}

		System.out.println("Clicked entity "+e+" at "+ gameSpace.x+", "+gameSpace.y+" (screen "+screenX+", "+screenY+")");
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
