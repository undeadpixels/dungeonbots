package com.undead_pixels.dungeon_bots.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class JSemaphorePane<T> extends JPanel {

	private final T[] _Values;
	private final DefaultListModel<FieldValue> _ListModel;

	private final Controller _Controller;
	private final HashMap<String, FieldValue> _Map = new HashMap<String, FieldValue>();


	public JSemaphorePane(T[] allowedValues) {
		this._Controller = new Controller();
		this._Values = allowedValues.clone();

		this._ListModel = new DefaultListModel<FieldValue>();
		JList<FieldValue> list = new JList<FieldValue>();
		list.setModel(new DefaultListModel<FieldValue>());
		list.addListSelectionListener(_Controller);
		list.setCellRenderer(_ItemRenderer);
		JScrollPane scroller = new JScrollPane(list);
		this.add(scroller);
	}


	public boolean addField(String field, String info, T initialValue) {
		if (_Map.containsKey(field))
			return false;
		FieldValue fv = new FieldValue(field, info, initialValue);
		_ListModel.addElement(fv);
		_Map.put(field, fv);
		return true;
	}


	/**Since tuples are frowned upon in Java.  Sheesh.*/
	private class FieldValue {

		public final String field;
		public final T value;
		public final String info;


		public FieldValue(String field, String info, T value) {
			this.field = field;
			this.value = value;
			this.info = info;
		}
	}


	public T getValue(String field) {
		return _Map.get(field).value;
	}


	private final ListCellRenderer<FieldValue> _ItemRenderer = new ListCellRenderer<FieldValue>() {

		@Override
		public Component getListCellRendererComponent(JList<? extends JSemaphorePane<T>.FieldValue> list,
				JSemaphorePane<T>.FieldValue fv, int index, boolean isSelected, boolean hasFocus) {
			JLabel lbl = new JLabel(fv.toString());
			if (isSelected)
				lbl.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
			return lbl;
		}

	};


	private class Controller implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub

		}

	}
}
