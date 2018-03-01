package com.undead_pixels.dungeon_bots.scene.entities.inventory;

import com.undead_pixels.dungeon_bots.script.annotations.*;

import java.util.List;

/**
 * An Item that, when used, displays a question prompt with a list of questions.
 * When the player selects a response and submits the question, saves the selected
 * answer in the Question Item which can be submitted or returned to a requester.
 */
public class MultipleChoiceQuestion extends Question {

	/**
	 *
	 */
	private final List<String> answers;

	/**
	 *
	 */
	private String selectedAnswer;

	public MultipleChoiceQuestion(String name, String descr, int value, int weight, List<String> answers) {
		super(name, descr, value, weight);
		this.answers = answers;
	}

	/**
	 *
	 * @return
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	public boolean use() {
		/**  Do something that displays the multiple choice question. **/
		answers.forEach(System.out::println);
		/* Register event handler for players selected answer that sets the selectedAnswer
		 * in the Question item.  */
		selectedAnswer = "WRONG";
		return true;
	}

	public String getSelectedAnswer() {
		return selectedAnswer;
	}
}
