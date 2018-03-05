package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Question;
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
	private static final long serialVersionUID = 1L;

	/**
	 * A List of answers that are supplied to the Question form.
	 */
	private final List<String> answers;

	/**
	 * The Answer that was selected for the Question.
	 */
	private volatile String selectedAnswer;

	public MultipleChoiceQuestion(World w, String name, String descr, int value, int weight, List<String> answers) {
		super(w, name, descr, value, weight);
		this.answers = answers;
	}

	/**
	 * Method that creates and displays a Question form that displays
	 * a list of answers.
	 * @return
	 */
	@Override @Bind(SecurityLevel.DEFAULT)
	public Boolean use() {
		/**  Do something that displays the multiple choice question. **/
		answers.forEach(System.out::println);
		/* Register event handler for players selected answer that sets the selectedAnswer
		 * in the Question item.  */
		selectedAnswer = "WRONG";
		return true;
	}

	/**
	 *
	 * @return
	 */
	public String getSelectedAnswer() {
		return selectedAnswer;
	}

	public void setSelectedAnswer(String answer) {
		this.selectedAnswer = answer;
	}
}
