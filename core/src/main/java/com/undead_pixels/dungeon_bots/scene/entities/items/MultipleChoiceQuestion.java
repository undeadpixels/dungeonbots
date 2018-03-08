package com.undead_pixels.dungeon_bots.scene.entities.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.script.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.stream.Stream;

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
	private final String[] answers;

	/**
	 * The Context form that appears when the Question is used.
	 */
	private transient JFrame form;

	/**
	 * The Answer that was selected for the Question.
	 */
	private volatile String selectedAnswer;

	/**
	 * The Solution that is considered 'submitted' by the user.
	 */
	private volatile String submittedAnswer;

	public MultipleChoiceQuestion(World w, String descr,  String... answers) {
		super(w, "Multiple Choice Question", descr, 0, 0);
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
		if(form == null) {
			form = questionForm( answers);
			form.pack();
		}
		form.setVisible(true);
		return true;
	}

	@Override
	public String getDescription() {
		return String.format("Problem: %s\nMy Solution: %s",
				description,
				getSubmittedAnswer().orElse("None"));
	}

	public Optional<String> getSubmittedAnswer() {
		return Optional.ofNullable(this.submittedAnswer);
	}

	private JFrame questionForm(final String[] questions) {
		final JFrame localForm 		  = new JFrame();
		final JPanel titlePanel 	  = new JPanel(new FlowLayout());
		final JPanel questionPanel 	  = new JPanel(new FlowLayout());
		final ButtonGroup buttonGroup = new ButtonGroup();

		localForm.setLayout(new BorderLayout());
		titlePanel.add(new JLabel(this.description));
		localForm.add(titlePanel, BorderLayout.NORTH);

		// Add each question to the form
		Stream.of(questions).map(JRadioButton::new).forEach(radio -> {
			questionPanel.add(radio);
			buttonGroup.add(radio);
			radio.addActionListener(e -> selectedAnswer = e.getActionCommand()); });

		// Listen for the Submit button event
		final Button submit = new Button("Submit");
		submit.addActionListener(e -> {
			submittedAnswer = selectedAnswer;
			localForm.setVisible(false); });

		// Components to the form
		localForm.add(submit, BorderLayout.SOUTH);
		localForm.add(questionPanel, BorderLayout.CENTER);
		return localForm;
	}
}
