package com.undead_pixels.dungeon_bots.scene.entities.inventory.items;

import com.undead_pixels.dungeon_bots.scene.World;
import com.undead_pixels.dungeon_bots.scene.entities.Entity;
import com.undead_pixels.dungeon_bots.scene.entities.Player;
import com.undead_pixels.dungeon_bots.script.annotations.*;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Item that, when used, displays a question prompt with a list of questions.
 * When the player selects a response and submits the question, saves the selected
 * answer in the Question Item which can be submitted or returned to a requester.
 */
@Doc("A MultipleChoiceQuestion is a question that presents the invoker with a dialog window\n" +
		"With multiple options, where only one option can be selected.\n" +
		"By clicking Submit, the player sets a field in the Question that can be queried\n" +
		"from a grading script.")
public class MultipleChoiceQuestion extends Question {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A List of answers that are supplied to the Question form.
	 */
	private String[] answers;

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

	@BindTo("new")
	@Bind(value = SecurityLevel.DEFAULT, doc= "Create a new MultipleChoice question item")
	public static MultipleChoiceQuestion create(
			@Doc("The World the question belongs to") LuaValue world,
			@Doc("The underlying question.") LuaValue descr,
			@Doc("A List/Table of answers to the question") LuaValue answers) {
		final Varargs v = answers.checktable().unpack();
		final String[] ans = new String[v.narg()];
		for(int i = 0; i < v.narg(); i++) {
			ans[i] = v.isstring(i) ? v.tojstring(i) : "";
		}
		return new MultipleChoiceQuestion(
				(World)world.checktable().get("this").checkuserdata(World.class),
				descr.checkjstring(),
				ans);
	}

	/**
	 * Method that creates and displays a Question form that displays
	 * a list of answers.
	 * @return
	 */
	@Override
	public Boolean applyTo(Entity e) {
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
				getSubmittedAnswer());
	}

	@Doc("Get the Answer to submitted for the question.")
	@Bind(SecurityLevel.AUTHOR)
	public String getSubmittedAnswer() {
		return Optional.ofNullable(this.submittedAnswer).orElse("None");
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
	@Bind(value = SecurityLevel.AUTHOR, doc = "Set the Answers for the question")
	public void setAnswers(Varargs answers) {
		this.answers = Question.varargsToStringArr(answers.arg1().isstring() ? answers : answers.subargs(2));
	}

	public Varargs getAnswers() {
		return LuaValue.varargsOf(Stream.of(answers).map(v -> LuaValue.valueOf(v)).collect(Collectors.toList()).toArray(new LuaValue[]{}));
	}
}
