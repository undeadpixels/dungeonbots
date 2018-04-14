/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.script.LuaSandbox;

import jsyntaxpane.DefaultSyntaxKit;


/**
 * Utilities (and views) for automagically inserting code into an editor
 * 
 * @author kevin
 *
 */
public class CodeInsertions {

	/**
	 * An entry in the list of possible insertions
	 */
	public static class InsertionEntry {
		public final String name;
		public final String description;
		public final String templateText;
		public final InsertionEntryGroup group;
		
		public String humanReadableText() {
			return templateText; // TODO - make readable
		}

		public InsertionEntry(InsertionEntryGroup group, String name, String description, String templateText) {
			super();
			this.group = group;
			this.name = name;
			this.templateText = templateText;
			this.description = description;
		}
		
		public String toString() {
			return this.name;
		}
	}
	
	/**
	 * A state holding progress through replacing variable names in an insertion
	 */
	private static class InsertionReplacementState {
		/**
		 * A single template name in an insertion with some info about it
		 */
		private static class Field {
			public final int originalBegin;
			public final int originalEnd;
			public final String templateName;
			public final String templateType;
			private String currentString;

			public Field(int originalBegin, int originalEnd, String originalString) {
				super();
				this.originalBegin = originalBegin;
				this.originalEnd = originalEnd;

				// get more info if possible
				if(originalEnd - originalBegin < 2) {
					currentString = originalString;
					templateName = originalString;
					templateType = "";
				} else {
					// split the type info off
					String inner = originalString.substring(1, originalString.length()-1);
					String[] innerSplit = inner.split(":");
					templateName = innerSplit[0];
					
					if(innerSplit.length > 1) {
						templateType = innerSplit[1];
					} else {
						templateType = "";
					}
					
					currentString = "<"+templateName+">";
				}
			}
			
			/* (non-Javadoc)
			 * @see java.lang.Object#hashCode()
			 */
			@Override
			public int hashCode () {
				final int prime = 31;
				int result = 1;
				result = prime * result + originalBegin;
				result = prime * result + originalEnd;
				result = prime * result + ( (templateName == null) ? 0 : templateName.hashCode());
				result = prime * result + ( (templateType == null) ? 0 : templateType.hashCode());
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
				Field other = (Field) obj;
				if (originalBegin != other.originalBegin) {
					return false;
				}
				if (originalEnd != other.originalEnd) {
					return false;
				}
				if (templateName == null) {
					if (other.templateName != null) {
						return false;
					}
				} else if (!templateName.equals(other.templateName)) {
					return false;
				}
				if (templateType == null) {
					if (other.templateType != null) {
						return false;
					}
				} else if (!templateType.equals(other.templateType)) {
					return false;
				}
				return true;
			}

			/**
			 * @return	A good way to represent this field inline with the rest of the code
			 */
			public String getInlineRepresentation() {
				String currentString = getCurrentString ();
				if(currentString.equals("")) {
					return "<"+templateName+">";
				} else {
					return currentString;
				}
			}
			
			public String getCurrentString () {
				return currentString;
			}

			
			public void setCurrentString (String currentString) {
				this.currentString = currentString;
			}
			
			public static class SharedField extends Field {

				public final Field parent;
				
				/**
				 * @param originalBegin
				 * @param originalEnd
				 * @param originalString
				 */
				public SharedField(int originalBegin, int originalEnd, String originalString, Field parent) {
					super(originalBegin, originalEnd, originalString);
					this.parent = parent;
				}

				public String getCurrentString () {
					return parent.currentString;
				}

				
				public void setCurrentString (String currentString) {
					parent.currentString = currentString;
				}
			}
		}
		
		public final InsertionEntry entry;
		public final ArrayList<Field> fields = new ArrayList<>();
		public InsertionReplacementState(InsertionEntry entry) {
			super();
			this.entry = entry;
			
			Pattern templatePattern = Pattern.compile("<[^<>]*>");
			Matcher matches = templatePattern.matcher(entry.templateText);
			
			while(matches.find()) {
				int start = matches.start();
				int end = matches.end();
				String str = matches.group();
				
				if(str.endsWith(":shared>")) {
					Field parent = new Field(0, 0, "<error>");
					String searchFor = str.substring(1).split(":")[0];
					for(Field f : fields) {
						if(f.templateName.equals(searchFor)) {
							parent = f;
							break;
						}
					}
					
					if(parent.templateName.equals("error")) {
						System.err.println("Could not find parent for: "+searchFor);
					}
					
					fields.add(new Field.SharedField(start, end, str, parent));
				} else {
					fields.add(new Field(start, end, str));
				}
			}
		}
		
		public String toString() {
			// Condense all fields and non-field text down, then return
			StringBuilder ret = new StringBuilder();
			
			Field prevField = new Field(0, 0, "");
			
			for(Field field : fields) {
				String normalText = entry.templateText.substring(prevField.originalEnd, field.originalBegin);
				ret.append(normalText);
				ret.append(field.getInlineRepresentation());
				
				prevField = field;
			}

			String endText = entry.templateText.substring(prevField.originalEnd, entry.templateText.length());
			ret.append(endText);
			
			return ret.toString();
		}
	}
	
	public static class InsertionEntryGroup {
		public final String name;

		public InsertionEntryGroup(String name) {
			super();
			this.name = name;
		}

		@Override
		public int hashCode () {
			// autogen'd
			final int prime = 31;
			int result = 1;
			result = prime * result + ( (name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals (Object obj) {
			// autogen'd
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InsertionEntryGroup other = (InsertionEntryGroup) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
	
	
	
	private DefaultMutableTreeNode treeModel = new DefaultMutableTreeNode();
	private HashMap<InsertionEntryGroup, Integer> groups = new HashMap<>();
	
	public void add(InsertionEntry e) {
		// append an InsertionEntry to the table model
		DefaultMutableTreeNode groupNode;
		
		if(groups.containsKey(e.group)) {
			int idx = groups.get(e.group);
			groupNode = (DefaultMutableTreeNode) treeModel.getChildAt(idx);
		} else {
			int idx = treeModel.getChildCount();
			groupNode = new DefaultMutableTreeNode(e.group.name);
			groups.put(e.group, idx);
			treeModel.add(groupNode);
		}
		
		groupNode.add(new DefaultMutableTreeNode(e));
	}

	public void add(InsertionEntryGroup group, String name, String description, String templateText) {
		add(new InsertionEntry(group, name, description, templateText));
	}

	public void add(String group, String name, String description, String templateText) {
		add(new InsertionEntry(getGroupByName(group), name, description, templateText));
	}
	
	
	/**
	 * @param group
	 * @return
	 */
	public InsertionEntryGroup getGroupByName (String group) {
		InsertionEntryGroup ret = new InsertionEntryGroup(group);
		return ret;
	}
	
	/**
	 * A stupid function to do line wrapping for JLabels, since JLabels don't support it and JTextAreas are uncooperative
	 */
	private String wrap(String text, int cols) {
		StringBuilder sb = new StringBuilder(text);

		// https://stackoverflow.com/questions/4212675/wrap-the-string-after-a-number-of-characters-word-wise-in-java
		int i = 0;
		while (i + cols < sb.length()) {
			int newlinePlace = sb.lastIndexOf("\n", i + cols);
			if(newlinePlace > i) {
				i = newlinePlace;
			} else {
				int nextI = sb.lastIndexOf(" ", i + cols);
				if(i == nextI) {
					i += cols;
				} else {
					i = nextI;
				}
			}
			if(i == -1) break;
			sb.replace(i, i + 1, "\n");
		}
		
		String retStr = sb.toString();
		retStr = retStr.replace("<", "&lt;");
		retStr = retStr.replace(">", "&gt;");
		retStr = retStr.replace("\n", "<br>");
		return "<html>"+retStr+"</html>";
	}
	
	/**
	 * Creates a template replacer window, allowing variables/fields/etc to be chosen
	 * 
	 * @param entry
	 * @param acceptAction
	 */
	private void fireTemplateReplacer(InsertionEntry entry, Consumer<String> acceptAction) {
		JPanel replacerView = new JPanel(new VerticalLayout());
		
		InsertionReplacementState state = new InsertionReplacementState(entry);
		
		JEditorPane codeArea = new JEditorPane();
		JScrollPane codeScroll = new JScrollPane(codeArea);
		codeArea.setEditable(false);
		codeArea.setFocusable(true);
		codeArea.setContentType("text/lua");
		codeArea.setText(state.toString());
		
		JLabel helpTextLabel = new JLabel(wrap(entry.description, 70));
		helpTextLabel.setHorizontalAlignment(JLabel.CENTER);
		
		HashMap<InsertionReplacementState.Field, JTextField> textFields = new HashMap<>();
		DocumentListener docListener = new DocumentListener() {

			@Override
			public void insertUpdate (DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate (DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate (DocumentEvent e) {
				for(int i = 0; i < state.fields.size(); i++) {
					// update model
					InsertionReplacementState.Field f = state.fields.get(i);
					JTextField textField = textFields.get(f);
					if(textField != null) {
						f.setCurrentString(textField.getText());
					}
				}
				
				codeArea.setText(state.toString());
			}
			
		};

		replacerView.add(helpTextLabel);
		replacerView.add(Box.createVerticalStrut(10));
		replacerView.add(codeScroll);
		replacerView.add(Box.createVerticalStrut(5));
		replacerView.add(new JSeparator());
		
		replacerView.add(Box.createVerticalStrut(10));
		
		
		// make the labels and text boxes line up nicely
		JPanel bottomBox = new JPanel();
		GroupLayout groupLayout = new GroupLayout(bottomBox);
		bottomBox.setLayout(groupLayout);
		
		groupLayout.setAutoCreateGaps(true);
		groupLayout.setAutoCreateContainerGaps(true);
		
		GroupLayout.SequentialGroup hGroup = groupLayout.createSequentialGroup();
		GroupLayout.Group leftGroup = groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING);
		hGroup.addGroup(leftGroup);
		GroupLayout.Group rightGroup = groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
		hGroup.addGroup(rightGroup);
		groupLayout.setHorizontalGroup(hGroup);
		
		GroupLayout.SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
		groupLayout.setVerticalGroup(verticalGroup);
		
		for(InsertionReplacementState.Field field : state.fields) {
			if(field instanceof InsertionReplacementState.Field.SharedField) {
				continue;
			}
			JTextField textField = new JTextField(field.getCurrentString(), 20);
			textField.getDocument().addDocumentListener(docListener);
			textFields.put(field, textField);
			
			JLabel label = new JLabel(field.templateName);
			label.setLabelFor(textField);
			
			leftGroup.addComponent(label);
			rightGroup.addComponent(textField);
			
			verticalGroup.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(label).addComponent(textField));
		}
		
		replacerView.add(bottomBox);
		
		
		
		
		// show the user the dialog
		int result = JOptionPane.showConfirmDialog(null, replacerView, "Insert " + entry.name + "?",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
		
		if(result == JOptionPane.OK_OPTION) {
			String text = codeArea.getText();
			
			if(text.contains("\n")) {
				text = text + "\n";
			}
			
			acceptAction.accept(text);
		}
	}

	/**
	 * Creates a default list of insertions, largely based on lua spec, but with a few handy dungeonbots-specific things
	 */
	public CodeInsertions() {
		
		// help
		this.add("Help", "help()", "The help function returns everything accessible to this lua script environment. "
				+ "If you're looking for help about the world, try help(world) or for information about what this entity can do, "
				+ "try help(this).", "help()");
		this.add("Help", "help(this)", "The help function help information about objects passed to it. "
				+ "This command will show everything that this object can do.", "help(this)");
		
		// loops
		this.add("Flow Control", "While loop", "While loops repeat the code inside of them, but only while their given condition is met.",
				  "while <Condition:boolean> do\n"
				+ "    -- Your Code Here\n"
				+ "end");
		this.add("Flow Control", "Repeat loop (\"do\")",
				"Repeat loops (or \"do loops\" in other programming languages) always execute once, "
				+ "and then continue executing until a condition triggers them to end.",
				  "repeat\n"
				+ "    -- Your Code Here\n"
				+ "until <Condition:boolean>");
		this.add("Flow Control", "For loop", 
				"For loops execute the code inside of them a given number of times.\n"
				+ "Think of them as \"Execute this code for N repetitions\".",
				  "for <Variable Name> = <Begin:int>, <End:int> do\n"
				+ "    -- Your Code Here\n"
				+ "end");
		this.add("Flow Control", "For loop with Increment",
				"For loops execute the code inside of them a given number of times.\n"
				+ "This is like a normal \"for\" loop, except instead of producing numbers such as \"1, 2, 3, ...\", it "
				+ "produces numbers separated by the given <Increment By> parameter.",
				  "for <Variable Name> = <Begin:float>, <End:float>, <Increment By:float> do\n"
				+ "    -- Your Code Here\n"
				+ "end");
		this.add("Flow Control", "For-each loop",
				"A for-each loop will perform a given operation on each element of an array or table "
				+ "(also known as a dictionary in other programming languages).\n"
				+ "The key and value pair indicates that myTable[key] = value.",
				  "for key, value in pairs(<Table Name>) do\n"
				+ "    -- Your Code Here\n"
				+ "end");
		this.add("Flow Control", "If statement",
				"If statements only execute code if a certain condition is met.",
				  "if <Condition:boolean> then\n"
				+ "    -- This runs if true\n"
				+ "end");
		this.add("Flow Control", "If/Else statement",
				"If statements only execute code if a certain condition is met.\n"
				+ "An else clause allows alternate code to run if the condition is not met.",
				  "if <Condition:boolean> then\n"
				+ "    -- This runs if true\n"
				+ "else\n"
				+ "    -- This runs if false\n"
				+ "end");
		this.add("Flow Control", "If/Else-if statement",
				"If statements only execute code if a certain condition is met.\n"
				+ "An else-if clause only runs if the \"if\" clause didn't run and the next given condition is met.\n"
				+ "An else clause allows alternate code to run if none of conditions are met.",
				  "if <Condition 1:boolean> then\n"
				+ "    -- This runs if true\n"
				+ "elseif <Condition 2:boolean> then\n"
				+ "    -- This runs if the next condition is true\n"
				+ "else\n"
				+ "    -- This runs if none of the above were true\n"
				+ "end");

		// functions
		this.add("Functions", "Function Declaration",
				"Functions are a nice way to organize your code.\n"
				+ "It's just like baking: your cookie recipe probably has you assemble your dough in a few pieces. "
				+ "First it asks you to assemble the dry ingredients, then store them somewhere. "
				+ "Next you assemble the liquids, and then you put it all together. "
				+ "Likewise in programming, you can explain how to do something once (in a function), and then use that function anywhere.",
				  "function f ()\n"
				+ "    -- Your code here\n"
				+ "end");
		this.add("Functions", "Lambda-style function",
				"Functions are a nice way to organize your code.\n"
				+ "Lambda expressions (and these lambda-like functions) are a more compact way to express functions.",
				  "f = function ()\n"
				+ "    -- Your code here\n"
				+ "end");
		
		// comments
		this.add("Comments", "One-line Comment",
				"Comments are things that the computer ignores when running your script.\n"
				+ "They are helpful for humans trying to read and understand your code.",
				  "-- This is a comment");
		this.add("Comments", "Multi-line Comment",
				"Comments are things that the computer ignores when running the script.\n"
				+ "They are helpful for humans trying to read and understand your code.\n"
				+ "Multi-line comments are helpful when you have a lot to say.",
				  "--[[\n"
				+ "  This is a mult-line comment.\n"
				+ "  Write as much as you want in here.\n"
				+ "]]");
		

		// keyword values
		this.add("Keyword values", "nil (null)", "nil (or null in other languages) is an indication that there is no value for a given variable.",
				"nil");
		this.add("Keyword values", "false", "Something that is never true.",
				"false");
		this.add("Keyword values", "true", "Something that is always true.",
				"true");
		

		// operators
		final String OPERATOR_HELP = "Operators in programming are just like the ones you learned about in math class.";
		this.add("Operators", "addition (+)", OPERATOR_HELP, "<A> + <B>");
		this.add("Operators", "subtraction (-)", OPERATOR_HELP, "<A> - <B>");
		this.add("Operators", "multiplication (*)", OPERATOR_HELP, "<A> * <B>");
		this.add("Operators", "float division (/)", OPERATOR_HELP+"\n"
				+ "The reason this is specified as \"float division\" is because something like 1/2 = .5. "
				+ "In other programming languages, division of integers always results in another integer (so 1/2 = 0, and 3/2 = 1), "
				+ "but in lua, 1/2 = .5 and 3/2 = 1.5.",
				"<A> / <B>");
		//this.add("Operators", "floor division (//)", OPERATOR_HELP+"\n"
		//		+ "Floor division only handles integers, so 1/2 = 0.\n"
		//		+ "For division where 1/2 = .5, see float division.", "<A> // <B>");
		this.add("Operators", "modulo (%)", "The modulo operator is similar to the remainder from long devision.", "<A> % <B>");
		this.add("Operators", "exponentiation (^)", OPERATOR_HELP, "<A> ^ <B>");
		
		
		this.add("Operators", "logical OR",
				"If <A> or <B> is true, or if both are true, then the result of the \"or\" statement is true.",
				"<A> or <B>");
		this.add("Operators", "logical AND",
				"If <A> and <B> is true, then the result of the \"and\" statement is true.\n"
				+ "Otherwise, the result is false.", "<A> and <B>");
		this.add("Operators", "logical NOT",
				"If <A> is false, then \"not <A>\" is true.\n"
				+ "If <A> is true, then \"not <A>\" is false.", "not <A>");

		this.add("Operators", "equality", "True if <A> and <B> are equal.\n"
				+ "Note that the == operator does not apply to unique tables that have the same contents.\n\n"
				+ "3 == 3 is true.\n"
				+ "If we say \"myTable = {a=1}\", then\n"
				+ "myTable == myTable is true (as myTable is the exact same table as itself), but:\n"
				+ "{a=1} == {a=1} is false, since two unique tables holding the same values are created in this last case.", "<A> == <B>");
		this.add("Operators", "inequality", "True if <A> and <B> are not equal.\n"
				+ "For example, 1 ~= 2 is true, but\n"
				+ "5 ~= 5 is false.\n\n"
				+ "Tables are handled slightly different from numbers. "
				+ "For more information, please click on the \"equality\" operator from this list.", "<A> ~= <B>");
		this.add("Operators", "less than", "True if <A> is less than <B>", "<A> < <B>");
		this.add("Operators", "greater than", "True if <A> is greater than <B>", "<A> > <B>");
		this.add("Operators", "less or equal", "True if <A> is less than or equal to <B>", "<A> <= <B>");
		this.add("Operators", "greater or equal", "True if <A> is greater than or equal to <B>", "<A> >= <B>");

		this.add("Arrays/Tables", "Set array/table value",
				"Arrays are long collections of values, and tables are a collection of values, mapped from their name.\n"
				+ "For example, if Fido is 3 years old, then fido[\"age\"] = 3.\n"
				+ "This updates or creates a value in an array or table.",
				"<Table Name>[<Index Name>] = <New Value>");
		this.add("Arrays/Tables", "Set table value (\".\" style)",
				"Arrays are long collections of values, and tables are a collection of values, mapped from their name.\n"
				+ "For example, if Fido is 3 years old, then fido.age = 3.\n"
				+ "This updates or creates a value in an array or table.",
				"<Table Name>.<Key Name> = <New Value>");
		this.add("Arrays/Tables", "Append value to array",
				"We can combine the length operator and setting a particular index to append to an array.\n"
				+ "For more information on those operators, try clicking on them.",
				"<Table Name>[#<Table Name::shared> + 1] = <New Value>");
		this.add("Arrays/Tables", "Array length",
				"Arrays are long collections of values.\n"
				+ "The length of an array is the number of values that it holds.",
				"#<Array>");

		//this.add("Operators", "bitwise AND", "<A> & <B>");
		//this.add("Operators", "bitwise OR", "<A> | <B>");
		//this.add("Operators", "bitwise exclusive OR", "<A> ~ <B>");
		//this.add("Operators", "right shift", "<A> >> <B>");
		//this.add("Operators", "left shift", "<A> << <B>");
		//this.add("Operators", "unary bitwise NOT", "~ <A>");
		
		/*
		 * ‘{’ [fieldlist] ‘}’
		 * ""
		 * [[]]
		 * 
		 */
		
		
	}
	

	/**
	 * Adds default insertions and headers for events
	 */
	public CodeInsertions(LuaSandbox sandbox) {
		this();
		
		for(LuaSandbox.EventInfo i : sandbox.getEvents()) {
			this.add("Events", i.niceName, i.description, i.generateTemplateListener());
		}
	}
	
	

	
	/**
	 * @return	A JTree representing the insertions available
	 */
	public JTree makeTree (Consumer<String> action) {
		JTree ret = new JTree(treeModel);
		ret.setToolTipText("Click on one of these insertions to see what it does");
		
		ret.setForeground(Color.white);
		
		TreeUI uncastUI = ret.getUI();
		
		if(uncastUI instanceof BasicTreeUI) {
			BasicTreeUI ui = (BasicTreeUI)uncastUI;
			ui.setCollapsedIcon(TreeIcons.collapsedIcon);
			ui.setExpandedIcon(TreeIcons.expandedIcon);
		}
		
		
		
		
		ret.setRootVisible(false);
		ret.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		for(int i = 0; i < ret.getRowCount(); i++) {
			ret.expandRow(i);
		}

		ret.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged (TreeSelectionEvent e) {
				
				Object nodeAsObj = e.getPath().getLastPathComponent();
				
				if(ret.getSelectionCount() == 1 && nodeAsObj instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeAsObj;
					Object o = node.getUserObject();
					if(o instanceof InsertionEntry) {
						InsertionEntry ie = (InsertionEntry)o;
						fireTemplateReplacer(ie, action);
					}
				}
				
				ret.clearSelection();
			}
			
		});
		
		return ret;
	}

	/**
	 * Creates a JTree for the list of insertions, wraps it in a scroll pane, and builds listeners
	 * 
	 * @param editor
	 * @return
	 */
	public JScrollPane makeScrollerAndSetup (JEditorPane editor) {
		JTree codeInsertionTree = makeTree((s) -> {
			Document d = editor.getDocument();

			int pos = editor.getCaretPosition();
			
			if(s.contains("\n")) {
				// find out where in the document we are so we can handle indentation
				Element root = d.getDefaultRootElement();
				int row = 0;
				for(; row < root.getElementCount(); row++) {
					if(root.getElement(row).getEndOffset() > pos) {
						break;
					}
				}
				
				// Get the current row so we can look at the whitespace/indentation it has...
				int rowBegin = root.getElement(row).getStartOffset();
				int rowEnd = root.getElement(row).getEndOffset();
				String rowStr = "";
				try {
					rowStr = d.getText(rowBegin, rowEnd - rowBegin - 1);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				
				// collect the whitespace at the beginning
				String whitespace = "";
				int col = 0;
				for(; col < rowStr.length() && Character.isWhitespace(rowStr.charAt(col)); col++) {
					whitespace += rowStr.charAt(col);
				}
				
				if(rowStr.equals(whitespace)) {
					// blank row; that's easy
				} else {
					// make a new row, I guess
					s = "\n" + s;
				}
				
				pos = rowEnd-1;
				s = s.replace("\n", "\n"+whitespace);
				editor.setCaretPosition(pos);
			}
			
			try {
				d.insertString(pos, s, null);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
			
			editor.requestFocusInWindow();
		});
		
		JScrollPane insertionScroller = new JScrollPane(codeInsertionTree);
		insertionScroller.setBorder(BorderFactory.createTitledBorder("Insert:"));
		
		return insertionScroller;
	}
	
}
