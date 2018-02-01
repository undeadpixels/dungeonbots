package com.undead_pixels.dungeon_bots.script;


/**
 * An "event" that is executed by invoking a block of code.
 */
public class CodeBlockScriptEvent extends ScriptEvent {

	
	public final String codeBlock;
	
	
	public CodeBlockScriptEvent(LuaSandbox sandbox, String codeBlock) {
		super(sandbox);
		this.codeBlock = codeBlock;
	}


	@Override
	public boolean startScript() {
		this.script = sandbox.script(codeBlock);
		this.script.start();
		return true;
	}
}
