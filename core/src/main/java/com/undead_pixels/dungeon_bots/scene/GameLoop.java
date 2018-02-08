package com.undead_pixels.dungeon_bots.scene;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.luaj.vm2.LuaError;

import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.script.LuaScript;
import com.undead_pixels.dungeon_bots.script.ScriptStatus;
import com.undead_pixels.dungeon_bots.script.annotations.UserScript;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameLoop implements IGameLoop {

	private IWorld _World;
	private boolean _PauseRequested = false;
	private boolean _IsPaused = true;
	private GameLoopWorker _Worker;
	private Exception _Exception;
	private GameLoopListener _Listener;
	private TurnController _TurnController;

	private ReadWriteLock loopLock = new ReentrantReadWriteLock();

	public GameLoop(IWorld world) {
		this._World = world;

		// The turn controller allows all entities simultaneous turns.
		_TurnController = new TurnController() {
			@Override
			public boolean timeForTurn(Entity entity) {
				return true;
			}
		};
	}

	public GameLoop(IWorld world, TurnController controller) {
		this._World = world;
		this._TurnController = controller;
	}

	@Override
	public void start() {

		try {
			// Step #1 - get the scripts associated with the World startup.
			UserScript[] worldScripts;
			Lock l = _World.getScriptsLock().readLock();
			l.lock();
			try {
				worldScripts = _World.getScripts().toArray(new UserScript[_World.getScripts().size()]);
			} finally {
				l.unlock();
			}

			// Step #2 - execute all the world startup scripts.
			LuaSandbox initSandbox = new LuaSandbox();
			for (UserScript script : worldScripts) {
				if (!script.canExecute(_World, 0L))
					continue;
				LuaScript ls = initSandbox.script(script.code);
				ls.start();
				ls.join(300);

				if (ls.getError() != null)
					throw ls.getError();
				else if (ls.getStatus() != ScriptStatus.COMPLETE)
					throw new IllegalStateException("Unhandled ScriptStatus: " + ls.getStatus().toString());				
			}

			// Step #3 - start the actual game loop worker.
			resume();

		} catch (Exception ex) {
			this._Exception = ex;
		}

	}

	private class GameLoopWorker extends SwingWorker {

		@Override
		protected Object doInBackground() throws Exception {

			Lock l = _World.getEntitiesLock().readLock();
			l.lock();
			Entity[] entities;
			try {
				entities = _World.getEntities().toArray(new Entity[_World.getEntities().size()]);
			} finally {
				l.unlock();
			}
			for (Entity e : entities) {

			}

			// Step #1 - get the entities' threads running.

			return null;
		}

		@Override
		protected void done() {
			try {
				get();
			} catch (Exception ee) {
				GameLoop.this._Exception = ee;
			}
		}

	}

	@Override
	public boolean isPaused() {

		return _IsPaused;
	}

	@Override
	public void pause() {
		_PauseRequested = true;
	}

	@Override
	public void kill(Entity entity) {
		// TODO Auto-generated method stub

	}

	/**
	 * Starts the game loop up without performing initialization. Can be used to
	 * re-start the loop after it has been paused. If the loop is running
	 * already, does nothing.
	 */
	@Override
	public void resume() {

		// Changing properties of the loop. Must lock for write.
		Lock l = loopLock.writeLock();
		try {
			l.lock();
			if (!_IsPaused)
				return;
			_Worker = new GameLoopWorker();
			_Worker.execute();
		} catch (Exception ex) {
			this._Exception = ex;
		} finally {
			l.unlock();
		}
	}

	public void setListener(GameLoopListener listener) {
		this._Listener = listener;
	}

	public abstract class TurnController {
		public abstract boolean timeForTurn(Entity entity);
	}
	
	protected void notifyError(Exception ex){
		this._Exception = ex;
		if (_Listener != null) SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				_Listener.onError(GameLoop.this);
			}
			
		});
	}
}
