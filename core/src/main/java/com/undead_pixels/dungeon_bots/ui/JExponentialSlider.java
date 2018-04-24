package com.undead_pixels.dungeon_bots.ui;

import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;

public class JExponentialSlider extends JSlider {

	public double min, max, init;
	public double exp;


	public JExponentialSlider(double min, double max, double init) {
		this.min = min;
		this.max = max;
		setExpValue(init);
		this.setPaintTicks(true);
		this.setMajorTickSpacing(10);
		Hashtable<Integer, JComponent> labelTable = new Hashtable<Integer, JComponent>();
		//labelTable.put((int) valueToX(min), new JLabel("" + min));
		labelTable.put((int) valueToX(1), new JLabel("" + 1d));
		labelTable.put((int) valueToX(2), new JLabel("" + 2d));
		labelTable.put((int) valueToX(4), new JLabel("" + 4d));
		labelTable.put((int) valueToX(max), new JLabel("" + max));
		this.setLabelTable(labelTable);
		this.setPaintLabels(true);
	}


	public void setExpMin(double min) {
		double value = this.getExpValue();
		this.min = min;
		if (min >= max)
			throw new RuntimeException("Min must be < max");
		this.setExpValue(value);
	}


	private double valueToX(double value) {
		double numer = (Math.exp(1)*(min-value)) - max + value;
		double denom = (min-max);
		return 100 * Math.log(numer/denom);		
	}


	public void setExpMax(double max) {
		double value = this.getExpValue();
		this.max = max;
		if (min >= max)
			throw new RuntimeException("Min must be < max");
		this.setExpValue(value);
	}


	public void setExpValue(double value) {
		if (value < min || value > max)
			throw new RuntimeException("Value is outside allowed range.");
		this.setValue((int) valueToX(value));
	}


	public double getExpValue() {
		double p = (double) this.getValue() / 100d;
		double eTerm = (Math.exp(p) - 1) / (Math.exp(1) - 1);
		return (eTerm * (max - min)) + min;

	}


	public float getExpMin() {
		throw new RuntimeException("not implemented yet.");
	}


	public float getExpMax() {
		throw new RuntimeException("not implemented yet.");
	}
}
