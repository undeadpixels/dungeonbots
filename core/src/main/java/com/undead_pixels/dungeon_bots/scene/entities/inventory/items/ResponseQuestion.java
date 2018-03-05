package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.inventory.items.Question;
import com.undead_pixels.dungeon_bots.script.annotations.Bind;
import com.undead_pixels.dungeon_bots.script.annotations.SecurityLevel;

import java.util.Optional;

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
	 * The question that is presented.
	 */
	private final String question;

	/**
	 * The response that is submitted to the question.
	 */
	private String questionResponse;

	/**
	 *
	 * @param name
	 * @param descr
	 * @param value
	 * @param weight
	 */
	public ResponseQuestion(World w, String name, String descr, int value, int weight, String question) {
		super(w, name, descr, value, weight);
		this.question = question;
	}

	/**
	 * Creates a UI Window that features a question, and a textarea input
	 * region for a response. The submitted response is submitted to the
	 * ResponseQuestion item.
	 * @return
	 */
	@Bind(SecurityLevel.DEFAULT) @Override
	public Boolean use() {
		// TODO: Create UI form with Textarea input for response
		return true;
	}

	/**
	 * Get's the ResponseQuestion's underlying question.
	 * @return A String corresponding to the ResponseQuestion's underlying question.
	 */
	public String getQuestion() {
		return this.question;
	}

	/**
	 * Get's the ResponseQuestion's underlying response.
	 * @return An optional containing a response if present
	 */
	public Optional<String> getQuestionResponse() {
		return Optional.ofNullable(this.questionResponse);
	}

	/**
	 * Set's the ResponseQuestions response value
	 * @param response The desired response to the ResponseQuestion
	 */
	public void setQuestionResponse(String response) {
		this.questionResponse = response;
	}
}
