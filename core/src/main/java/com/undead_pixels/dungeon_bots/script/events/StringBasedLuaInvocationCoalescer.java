/**
 * 
 */
package com.undead_pixels.dungeon_bots.script.events;

import com.undead_pixels.dungeon_bots.queueing.CoalescingGroup;
import com.undead_pixels.dungeon_bots.script.LuaInvocation;


/**
 * @author kevin
 *
 */
public class StringBasedLuaInvocationCoalescer implements CoalescingGroup<LuaInvocation> {
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime * result + extraID;
		result = prime * result + ( (str == null) ? 0 : str.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StringBasedLuaInvocationCoalescer other = (StringBasedLuaInvocationCoalescer) obj;
		if (extraID != other.extraID) {
			return false;
		}
		if (str == null) {
			if (other.str != null) {
				return false;
			}
		} else if (!str.equals(other.str)) {
			return false;
		}
		return true;
	}

	public final String str;
	public final int extraID;

	public StringBasedLuaInvocationCoalescer(String str) {
		super();
		this.str = str;
		this.extraID = 0;
	}

	public StringBasedLuaInvocationCoalescer(String str, int extraID) {
		super();
		this.str = str;
		this.extraID = extraID;
	}

	/* (non-Javadoc)
	 * @see com.undead_pixels.dungeon_bots.queueing.CoalescingGroup#coalesce(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void coalesce (LuaInvocation otherT, LuaInvocation t) {
		// nothing needs to be done
	}
	
}