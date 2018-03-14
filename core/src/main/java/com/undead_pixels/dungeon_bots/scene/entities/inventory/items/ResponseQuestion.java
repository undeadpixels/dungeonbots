package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.Doc;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.generic.Pair;
import org.luaj.vm2.LuaTable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Question Item type that prompts a UI Window containing a question and a
 * a textarea input for a response that can be submitted.
 * The ResponseQuestion should receive the response.
 * Authors can choose what to do with the response, but generally speaking,
 * this type of Question isn't intended for 'grading' in the games script
 * (Though an author could feasibly choose to do so if they desire)
 * but is mostly intended for use for survey questions that aren't graded.
 */
@Doc("When invoked, a ResponseQuestion presents the user with a Dialog window of\n" +
		"consisting of questions and text input responses that the user can provide\n" +
		"and submit.")
public class ResponseQuestion extends Question {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The response that is submitted to the question.
	 */
	private String questionResponse;

	private transient JFrame form;

	private final String[] questions;
	private final Map<String,String> results = new HashMap<>();
	private volatile boolean submitted = false;

	/**
	 *
	 * @param descr
	 */
	public ResponseQuestion(World w,String descr, String... questions) {
		super(w, "Response Questions", descr, 0, 0);
		this.questions = questions;
	}

	/**
	 * Creates a UI Window that features a question, and a textarea input
	 * region for a response. The submitted response is submitted to the
	 * ResponseQuestion item.
	 * @return
	 */
	@Override
	public Boolean applyTo(Entity e) {
		if(e.getClass().equals(Player.class)) {
			if (form == null) {
				form = getForm(this.questions);
				form.pack();
			}
			form.setVisible(true);
		}
		return false;
	}

	/**
	 * Get's the ResponseQuestion's underlying response.
	 * @return An optional containing a response if present
	 */
	public Map<String,String> getResonseQuestions() {
		return results;
	}

	@Doc("Get the Response questions the User submitted.")
	@Bind(SecurityLevel.AUTHOR)
	public LuaTable getResponseQuestions() {
		final LuaTable lt = new LuaTable();
		results.forEach(lt::set);
		return lt;
	}

	@Override
	@Bind(SecurityLevel.AUTHOR)
	@Doc("Gets a String representation of the Question and currently input solutions.")
	public String getDescription() {
		StringBuilder ans = new StringBuilder(this.description);
		ans.append("\n");
		results.forEach((k,v) ->
				ans.append(String.format("{ '%s' : '%s' }\n", k, v)));
		return ans.toString();
	}

	private JFrame getForm(String[] questions) {
		final JFrame frame = new JFrame();
		final JPanel body = new JPanel();
		final List<Pair<String,JTextField>> pairs = new ArrayList<>();

		frame.setLayout(new BorderLayout());
		body.setLayout(new GridLayout(0,2));

		frame.add(new JLabel(this.description), BorderLayout.NORTH);
		for(String str: questions) {
			final JTextField jTextField = new JTextField();
			body.add(new JLabel(str));
			pairs.add(new Pair<>(str, jTextField));
			body.add(jTextField);
		}
		frame.add(body, BorderLayout.CENTER);

		final Button submit = new Button("Submit");
		submit.addActionListener(e -> {
			submitted = true;
			pairs.forEach(p -> results.put(p.getFirst(), p.getSecond().getText()));
			frame.setVisible(false);
		});
		frame.add(submit, BorderLayout.SOUTH);
		return frame;
	}
}
