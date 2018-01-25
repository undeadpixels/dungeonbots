package com.undead_pixels.dungeon_bots.scene;

import com.undead_pixels.dungeon_bots.scene.entities.ActionQueue;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;

public abstract interface ActionGroupings {
	
	public boolean allowsDequeueAction(ActionQueue q);
	public void update();
	public default void dequeueIfAllowed(ActionQueue aq) {
		if(allowsDequeueAction(aq)) {
			aq.dequeueIfIdle();
		}
	}
	
	
	public static class RTSGrouping implements ActionGroupings {

		@Override
		public boolean allowsDequeueAction(ActionQueue q) {
			return true;
		}

		@Override
		public void update() {
		}
		
	}
	public static class TeamTurnsGrouping implements ActionGroupings {
		ActionGroupLock currentTeamLock = new ActionGroupLock();
		TeamFlavor currentTeam = TeamFlavor.NONE;
		boolean allowingDequeuesThisUpdate = true;

		@Override
		public boolean allowsDequeueAction(ActionQueue q) {
			if(allowingDequeuesThisUpdate) {
				return q.getEntity().getTeam() == currentTeam;
			}
			return false;
		}

		@Override
		public void update() {
			if(currentTeamLock.isFinished()) {
				allowingDequeuesThisUpdate = true;
				
				switch(currentTeam) {
				case NONE:
					currentTeam = TeamFlavor.PLAYER;
					break;
				case PLAYER:
					currentTeam = TeamFlavor.ENEMY;
					break;
				case ENEMY:
					currentTeam = TeamFlavor.NONE;
					break;
				}
			} else {
				allowingDequeuesThisUpdate = false;
			}
		}
		
	}
	public static class EntityTurnsGrouping implements ActionGroupings {
		ActionGroupLock everything = new ActionGroupLock();
		public boolean allowsDequeueAction(ActionQueue q) {
			if(everything.isFinished()) {
				everything.add(q);
				
				return true;
			}
			
			return false;
		}
		@Override
		public void update() {
		}
	}
}
