package com.undead_pixels.dungeon_bots.script.events;

import com.undead_pixels.dungeon_bots.queueing.CoalescingGroup;
import com.undead_pixels.dungeon_bots.script.FunctionCallScriptEvent;

public class UpdateCoalescer extends CoalescingGroup<FunctionCallScriptEvent> {
	
	public static final UpdateCoalescer instance = new UpdateCoalescer();
	
	@Override
	public int hashCode() {
		return 0x1234567; // arbitrary number
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	private UpdateCoalescer() {
		
	}

	@Override
	public void coalesce(FunctionCallScriptEvent otherT, FunctionCallScriptEvent t) {
		t.args[0].add(otherT.args[0]);
	}
 
}
