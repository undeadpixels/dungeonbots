/**
 * 
 */
package com.undead_pixels.dungeon_bots.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.VerticalLayout;

import com.undead_pixels.dungeon_bots.ui.code_edit.JScriptEditor;

import jsyntaxpane.DefaultSyntaxKit;


/**
 * @author kevin
 *
 */
public class CodeInsertions {




	public static void main(String[] args) {
		DefaultSyntaxKit.initKit();
		
		JFrame f = new JFrame("");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		f.add(new CodeInsertions().makeTree((s) -> System.out.println(s)));
		f.pack();
		f.setVisible(true);
	}

	public static class InsertionEntry {
		public final String name;
		public final String templateText;
		public final InsertionEntryGroup group;
		
		public String humanReadableText() {
			return templateText; // TODO - make readable
		}

		public InsertionEntry(InsertionEntryGroup group, String name, String templateText) {
			super();
			this.group = group;
			this.name = name;
			this.templateText = templateText;
		}
		
		public String toString() {
			return this.name;
		}
	}
	
	private static class InsertionReplacementState {
		private static class Field {
			public final int originalBegin;
			public final int originalEnd;
			public final String originalString;
			public final String templateName;
			public final String templateType;
			public String currentString;
			
			public Field(int originalBegin, int originalEnd, String originalString) {
				super();
				this.originalBegin = originalBegin;
				this.originalEnd = originalEnd;
				this.originalString = originalString;

				if(originalEnd - originalBegin < 2) {
					currentString = originalString;
					templateName = originalString;
					templateType = "";
				} else {
					// remove the type info
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
			
			public String getInlineRepresentation() {
				if(currentString.equals("")) {
					return "<"+templateName+">";
				} else {
					return currentString;
				}
			}
		}
		
		public final InsertionEntry entry;
		public final ArrayList<Field> fields = new ArrayList<>();
		public InsertionReplacementState(InsertionEntry entry) {
			super();
			this.entry = entry;
			
			Pattern templatePattern = Pattern.compile("<[^>]*>");
			Matcher matches = templatePattern.matcher(entry.templateText);
			
			while(matches.find()) {
				int start = matches.start();
				int end = matches.end();
				String str = matches.group();
				
				fields.add(new Field(start, end, str));
			}
		}
		
		public String toString() {
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
			final int prime = 31;
			int result = 1;
			result = prime * result + ( (name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals (Object obj) {
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

	public void add(InsertionEntryGroup group, String name, String templateText) {
		add(new InsertionEntry(group, name, templateText));
	}

	public void add(String group, String name, String templateText) {
		add(new InsertionEntry(getGroupByName(group), name, templateText));
	}
	
	
	/**
	 * @param group
	 * @return
	 */
	public InsertionEntryGroup getGroupByName (String group) {
		InsertionEntryGroup ret = new InsertionEntryGroup(group);
		return ret;
	}
	
	private void fireTemplateReplacer(InsertionEntry entry, Consumer<String> acceptAction) {
		JPanel replacerView = new JPanel(new VerticalLayout());
		
		InsertionReplacementState state = new InsertionReplacementState(entry);
		
		JEditorPane codeArea = new JEditorPane();
		JScrollPane codeScroll = new JScrollPane(codeArea);
		codeArea.setEditable(false);
		codeArea.setFocusable(true);
		codeArea.setContentType("text/lua");
		codeArea.setText(state.toString());
		
		ArrayList<JTextField> textFields = new ArrayList<>();
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
					state.fields.get(i).currentString = textFields.get(i).getText();
				}
				
				codeArea.setText(state.toString());
			}
			
		};

		replacerView.add(codeScroll);
		replacerView.add(new JSeparator());
		
		
		for(InsertionReplacementState.Field field : state.fields) {
			JTextField textField = new JTextField(field.currentString);
			textField.getDocument().addDocumentListener(docListener);
			textFields.add(textField);
			
			Box box = new Box(BoxLayout.X_AXIS);
			box.add(new JLabel(field.templateName));
			box.add(textField);
			
			replacerView.add(box);
		}
		
		int result = JOptionPane.showConfirmDialog(null, replacerView, entry.name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
		
		if(result == JOptionPane.OK_OPTION) {
			acceptAction.accept(codeArea.getText());
		}
	}

	public CodeInsertions() {
		
		// comments
		this.add("Comments", "One-line Comment",
				  "-- This is a comment");
		this.add("Comments", "Multi-line Comment",
				  "--[[\n"
				+ "  This is a mult-line comment\n"
				+ "]]");
		
		// loops
		this.add("Flow Control", "While loop",
				  "while <Condition:boolean> do\n"
				+ "  -- Your Code Here\n"
				+ "end");
		this.add("Flow Control", "Repeat loop (\"do\")",
				  "repeat\n"
				+ "  -- Your Code Here\n"
				+ "until <condition:boolean>");
		this.add("Flow Control", "For loop",
				  "for <VariableName> = <Begin:int> , <End:int> do\n"
				+ "  -- Your Code Here\n"
				+ "end");
		this.add("Flow Control", "For loop by",
				  "for <VariableName> = <Begin:float>, <End:float>, <IncrementBy:float> do\n"
				+ "  -- Your Code Here\n"
				+ "end");
		this.add("Flow Control", "For-each loop",
				  "for key, value in pairs(<TableName>) do\n"
				+ "  -- Your Code Here\n"
				+ "end");
		this.add("Flow Control", "If statement",
				  "if <Condition:boolean> then\n"
				+ "  -- This runs if true\n"
				+ "end");
		this.add("Flow Control", "If/Else statement",
				  "if <Condition:boolean> then\n"
				+ "  -- This runs if true\n"
				+ "else\n"
				+ "  -- This runs if false\n"
				+ "end");
		this.add("Flow Control", "If/Else-if statement",
				  "if <Condition:boolean> then\n"
				+ "  -- This runs if true\n"
				+ "elseif <Condition:boolean> then\n"
				+ "  -- This runs if the next condition is true\n"
				+ "else\n"
				+ "  -- This runs if none of the above were true\n"
				+ "end");

		// functions
		this.add("Functions", "Function Declaration",
				  "function f ()\n"
				+ "  -- Your code here\n"
				+ "end");
		this.add("Functions", "Lambda-style Declaration",
				  "f = function ()\n"
				+ "  -- Your code here\n"
				+ "end");
		
		
		
		/*
		 * nil | false | true
		 * ‘{’ [fieldlist] ‘}’
		 * ""
		 * [[]]
		 * 
		 * +: addition
		 * -: subtraction
		 * : multiplication
		 * /: float division
		 * //: floor division
		 * %: modulo
		 * ^: exponentiation
		 * -: unary minus
		 * 
		 * &: bitwise AND
		 * |: bitwise OR
		 * ~: bitwise exclusive OR
		 * >>: right shift
		 * <<: left shift
		 * ~: unary bitwise NOT
		 * 
		 * ==: equality
		 * ~=: inequality
		 * <: less than
		 * >: greater than
		 * <=: less or equal
		 * >=: greater or equal
		 * 
		 * 10 or 20 --> 10
		 * 10 or error() --> 10
		 * nil or "a" --> "a"
		 * nil and 10 --> nil
		 * false and error() --> false
		 * false and nil --> false
		 * false or nil --> nil
		 * 10 and 20 --> 20
		 * 
		 * #
		 * 
		 */
		
		
	}
	
	

	
	/**
	 * @return
	 */
	public JTree makeTree (Consumer<String> action) {
		JTree ret = new JTree(treeModel);
		
		ret.setRootVisible(false);
		ret.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		for(int i = 0; i < ret.getRowCount(); i++) {
			ret.expandRow(i);
		}

		ret.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged (TreeSelectionEvent e) {
				Object nodeAsObj = e.getPath().getLastPathComponent();
				if(nodeAsObj instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeAsObj;
					Object o = node.getUserObject();
					if(o instanceof InsertionEntry) {
						InsertionEntry ie = (InsertionEntry)o;
						fireTemplateReplacer(ie, action);
					}
				}
			}
			
		});
		
		return ret;
	}

	/**
	 * @param editor
	 * @return
	 */
	public JScrollPane makeScroller (JEditorPane editor) {
		JTree codeInsertionTree = makeTree((s) -> {
			System.out.println("got string: "+s);
			Document d = editor.getDocument();
			int pos = editor.getCaretPosition();
			try {
				d.insertString(pos, s, null);
				System.out.println("Attempted inserting string: "+s);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		});
		JScrollPane insertionScroller = new JScrollPane(codeInsertionTree);
		insertionScroller.setBorder(BorderFactory.createTitledBorder("Insert:"));
		
		return insertionScroller;
	}
	
}
