/*
Created 5 Feb 2007 - Richard Morris
*/
package org.singsurf.singsurf;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;

import jv.object.PsObject;

public class Fractometer extends Panel {
	private static final long serialVersionUID = 1L;

	/** The value of the control */
	double value;
	/** The number of decimal places to display */
	int		decimalPlaces=1;

	double minVal = -Double.MAX_VALUE;
	double maxVal = Double.MAX_VALUE;
	
	JTextField tf = new JTextField("0.0");
	NumberFormat nf = null;
	PsObject jvParent=null;

	public Fractometer(double value) {
		Font baseFont = Font.decode(System.getProperty("font"));
		tf.setFont(baseFont);
		this.setFont(baseFont);
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(this.decimalPlaces);
		nf.setMinimumFractionDigits(this.decimalPlaces);

			GridBagLayout gridbag = new GridBagLayout();
	        GridBagConstraints c = new GridBagConstraints();
	        c.anchor = GridBagConstraints.NORTHWEST;
	        c.fill = GridBagConstraints.BOTH;
	        c.gridheight = 1;
	        c.gridwidth = 1;
	        c.weightx = 0.1;
	        c.weighty = 1;
	        c.gridx = 0;
	        c.gridy = 0;
	        c.ipadx = 0;
	        c.ipady = 1;
	        c.insets = new Insets(0,0,0,0);
	        this.setLayout(gridbag);
			Button bmm = new RepeatButton("<<");
			bmm.setPreferredSize(new Dimension(25,25));
			bmm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					adjustValue(-10);
				}

			});
			Button bm = new RepeatButton("<");
            bm.setPreferredSize(new Dimension(25,25));
			bm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					adjustValue(-1);
				}});
			Button bp = new RepeatButton(">");
            bp.setPreferredSize(new Dimension(25,25));
			bp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					adjustValue(1);
				}});
			Button bpp = new RepeatButton(">>");
            bpp.setPreferredSize(new Dimension(25,25));
			bpp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					adjustValue(10);
				}});
			tf.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setValue(tf.getText());
					displayValue();
				}});
			tf.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
					String oldText = getFromattedValue();
					String newText = tf.getText();
					if(!oldText.equals(newText)) {
						setValue(tf.getText());
						displayValue();
					}				
				}});
			bmm.setFocusable(false);
			bm.setFocusable(false);
			bp.setFocusable(false);
			bpp.setFocusable(false);
			tf.setFocusable(true);
			
			this.add(bmm,c);
			c.gridx++;
			this.add(bm,c);
			c.gridx++; c.weightx = 0.9;
			this.add(tf,c);
			c.gridx++; c.weightx = 0.1;
			this.add(bp,c);
			c.gridx++;
			this.add(bpp,c);
			c.gridx++;
			
			setValue(value);
		}


	protected String getFromattedValue() {
		return nf.format(value);
	}


	protected void adjustValue(int amount) {
		double scale = Math.pow(10,-decimalPlaces);
		setValue(this.value+scale * amount);
		if(jvParent!=null) jvParent.update(this);
	}
	
	private double setValue(String text) {
		try {
			Number n = nf.parse(text);
			setValue(n.doubleValue());
			if(jvParent!=null) jvParent.update(this);
		} catch (ParseException e) {
			displayValue();
		}
		return value;
	}

	public double setValue(double val) {
		if(val < minVal) val = minVal;
		if(val > maxVal) val = maxVal;
		this.value = val;
		displayValue();
		return value;
	}

	private void displayValue() {
		tf.setText(nf.format(this.value));
	}
	
	
	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
		nf.setMaximumFractionDigits(this.decimalPlaces);
		nf.setMinimumFractionDigits(this.decimalPlaces);
		displayValue();
	}

	public double getMaxVal() {
		return maxVal;
	}

	public void setMaxVal(double maxVal) {
		this.maxVal = maxVal;
		setValue(this.value);
	}

	public double getMinVal() {
		return minVal;
	}

	public void setMinVal(double minVal) {
		this.minVal = minVal;
		setValue(this.value);
	}

	public double getValue() {
		return value;
	}
	
	public void setParent(PsObject parent) {
		this.jvParent = parent;
//		setFont(parent.baseFont);
	}
	
	@Override
	public String toString() {
		return "value "+value;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Fract main");
		Frame frame	= new Frame("parameters");
		frame.setBounds(new Rectangle(395, 5, 630, 600));
//			frame.setBounds(new Rectangle(100, 5, 830, 550));
		frame.setLayout(new GridLayout(4,1));
		final Fractometer f1 = new Fractometer(1.0);
		frame.add(f1);
		final Fractometer f2 = new Fractometer(1.0);
		frame.add(f2);
		final Fractometer f3 = new Fractometer(1.0);
		frame.add(f3);
		final Fractometer f4 = new Fractometer(1.0);
		frame.add(f4);
		Button b = new Button("Press me");
		frame.add(b);
		final Choice ch = new Choice();
		ch.add("1");
		ch.add("2");
		ch.add("3");
		ch.add("4");
		ch.add("5");
		ch.add("6");
		ch.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				int dp = Integer.parseInt(ch.getSelectedItem());
				f1.setDecimalPlaces(dp);
				f2.setDecimalPlaces(dp);
				f3.setDecimalPlaces(dp);
				f4.setDecimalPlaces(dp);
			}});
		
		frame.add(ch);
		
		PsObject panel = new PsObject() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean update(Object event) {
				System.out.println(event.getClass().getSimpleName() + " "+event);
				return true;
			}
			
		};		
		f1.setParent(panel);
		f2.setParent(panel);
		f3.setParent(panel);
		f4.setParent(panel);
		frame.setVisible(true);
	}	
}
