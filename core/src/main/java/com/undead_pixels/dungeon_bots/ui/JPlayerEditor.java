package com.undead_pixels.dungeon_bots.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.LuaSandbox;
import com.undead_pixels.dungeon_bots.ui.code_edit.JCodeREPL;

public class JPlayerEditor extends JPanel {

	public JPlayerEditor(Player player) {
		super(new BorderLayout());
		
		this.setPreferredSize(new Dimension(400,600));

		JCodeREPL repl = new JCodeREPL(player.getSandbox());		
		repl.message("This message is sent from some old object");
		repl.message("This message will be in the form of an internal echo from the editor itself", repl);
		repl.message("Turmoil has engulfed the Galactic Republic. The taxation of trade routes to outlying "
				+ "star systems is in dispute.\n\nHoping to resolve the matter with a blockade of deadly "
				+ "battleships, the greedy Trade Federation has stopped all shipping to the small planet of "
				+ "Naboo.\n\nWhile the congress of the Republic endlessly debates this alarming chain of "
				+ "events, the Supreme Chancellor has secretly dispatched two Jedi Knights, the guardians "
				+ "of peace and justice in the galaxy, to settle the conflict....");
		repl.message("Egads!  Not trade routes in dispute!", repl);
		repl.message("There is unrest in the Galactic Senate. Several thousand solar systems have declared "
				+ "their intentions to leave the Republic. This separatist movement, under the leadership "
				+ "of the mysterious Count Dooku, has made it difficult for the limited number of Jedi "
				+ "Knights to maintain peace and order in the galaxy. Senator Amidala, the former Queen of "
				+ "Naboo, is returning to the Galactic Senate to vote on the critical issue of creating an "
				+ "ARMY OF THE REPUBLIC to assist the overwhelmed Jedi....");
		repl.message("In retrospect, perhaps relying on a small group of religious zealots for galaxy-wide "
				+ "security may have been a mistake.", repl);
		repl.message("War! The Republic is crumbling under attacks by the ruthless Sith Lord, Count Dooku. "
				+ "There are heroes on both sides. Evil is everywhere. In a stunning move, the fiendish "
				+ "droid leader, General Grievous, has swept into the Republic capital and kidnapped "
				+ "Chancellor Palpatine, leader of the Galactic Senate. As the Separatist Droid Army "
				+ "attempts to flee the besieged capital with their valuable hostage, two Jedi Knights "
				+ "lead a desperate mission to rescue the captive Chancellor....");
		repl.message("Jeez.  It took you how many movies to get to the good stuff?  You should have just "
				+ "called your self 'Star Ways and Means Committee from the beginning'.", repl);
		repl.setCode("x=5+4");
		repl.execute(100);
		repl.setCode("return x");
		repl.setPreferredSize(new Dimension(400,500));
		
		
		this.add(repl, BorderLayout.LINE_START);
	}

	

	

}
