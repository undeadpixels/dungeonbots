package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;
import com.undead_pixels.dungeon_bots.utils.generic.Pair;

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
	@Bind(SecurityLevel.DEFAULT) @Override
	public Boolean use() {
		if(form == null) {
			form = getForm(this.questions);
			form.pack();
		}
		form.setVisible(true);
		return true;
	}

	/**
	 * Get's the ResponseQuestion's underlying response.
	 * @return An optional containing a response if present
	 */
	public Map<String,String> getResonseQuestions() {
		return results;
	}

	@Override
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
