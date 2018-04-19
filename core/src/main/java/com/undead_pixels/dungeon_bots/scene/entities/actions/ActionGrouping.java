package com.undead_pixels.dungeon_bots.scene.entities.actions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import com.undead_pixels.dungeon_bots.scene.TeamFlavor;

/**
 * A scheme for grouping collections of actions together.
 * 
 * This can be used to implement an RTS-like or turn-based game.
 */
public abstract interface ActionGrouping extends Serializable {
	
	/**
	 * Checks if this queue is allowed to start new tasks
	 * 
	 * @param q		The queue in question
	 * @return		True if the queue is allowed to start new tasks
	 */
	public boolean allowsDequeueAction(ActionQueue q);
	
	/**
	 * Updates this grouping (to be called every frame to allow it to determine if it should start doing something new)
	 */
	public void update();
	
	/**
	 * Tells the given queue to dequeueIfIdle, if this ActionGrouping determines it's allowed to.
	 * 
	 * For example, in a turn-based game, an enemy won't be able to deque (start) actions
	 * while it's still the player's turn.
	 * 
	 * @param aq		The queue to try updating
	 */
	public default boolean dequeueIfAllowed(ActionQueue aq) {
		if(allowsDequeueAction(aq)) {
			aq.dequeueIfIdle();
			return true;
		}
		return false;
	}
	
	
	/**
	 * A grouping that defines an RTS.
	 * Actions are allowed to begin at any time, regardless of what else is performing actions.
	 */
	public static class RTSGrouping implements ActionGrouping {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean allowsDequeueAction(ActionQueue q) {
			return true;
		}

		@Override
		public void update() {
		}
		
	}
	
	/**
	 * A grouping that allows each entity on a each team to make one move, one team at a time.
	 * 
	 * For example, a Player and all of their Bots will begin their move at the exact same time;
	 * then once those moves all finish, Enemies will all begin their moves.
	 */
	public static class TeamTurnsGrouping implements ActionGrouping {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * A lock that waits for all actions of a team to finish before allowing the next team to start.
		 */
		private transient ActionGroupLock currentTeamLock = new ActionGroupLock();
		
		/**
		 * The current team
		 */
		private TeamFlavor currentTeam = TeamFlavor.NONE;
		
		/**
		 * Even if it's the currentTeam's turn, they still can't start more than one move per entity per turn.
		 * So we just say that entities are only allowed to begin moves at the beginning of the turn.
		 */
		private boolean allowingDequeuesThisUpdate = true;

		@Override
		public boolean allowsDequeueAction(ActionQueue q) {
			if(allowingDequeuesThisUpdate) {
				// Only current team can move, only at the beginning of the turn
				currentTeamLock.add(q);
				return q.getOwner().getTeam() == currentTeam;
			}
			return false;
		}

		@Override
		public void update() {
			if(currentTeamLock.isFinished()) {
				// Once an entire team worth of entities has finished their turn, start the next team's turn
				allowingDequeuesThisUpdate = true;
				
				switch(currentTeam) {
				case NONE:
					currentTeam = TeamFlavor.PLAYER;
					break;
				case PLAYER:
					currentTeam = TeamFlavor.AUTHOR;
					break;
				case AUTHOR:
					currentTeam = TeamFlavor.NONE;
					break;
				}
			} else {
				allowingDequeuesThisUpdate = false;
			}
		}
		

		private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
			inputStream.defaultReadObject();
			currentTeamLock = new ActionGroupLock();
		}
		
	}
	
	/**
	 * A mode where only a single entity can move at a time.
	 * Instead of team-based turns, it's just based on the order
	 * the given entity was added to the world.
	 */
	public static class EntityTurnsGrouping implements ActionGrouping {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * A lock for all of the entities in the World
		 */
		private transient ActionGroupLock everything = new ActionGroupLock();
		
		
		public boolean allowsDequeueAction(ActionQueue q) {
			if(everything.isFinished()) {
				// only allow other moves to start when the given entity has finished moving
				everything.add(q);
				return true;
			}
			
			return false;
		}
		@Override
		public void update() {
		}

		private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
			inputStream.defaultReadObject();
			everything = new ActionGroupLock();
		}
	}
}
