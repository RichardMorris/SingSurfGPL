package org.singsurf.singsurf;

import java.awt.*;
import java.awt.event.*;
import jv.object.PsDialog;
import jv.object.PsDebug;
import jv.number.PuDouble;

/** A Dialog to handle global changes in a range **/

public class RangeConfig extends PsDialog {
    private static final long serialVersionUID = 1L;

    double min, max, smallStep, bigStep, accuracy;

    TextField tf_min;
    TextField tf_max;
    TextField tf_smallStep;
    TextField tf_bigStep;
    Button b_OK, b_cancle;

    public RangeConfig(Frame frm, boolean modal) {
	super(frm, "Config", modal);

	tf_min = new TextField(10);
	tf_max = new TextField(10);
	tf_smallStep = new TextField(10);
	tf_bigStep = new TextField(10);
	b_OK = new Button("OK");
	b_cancle = new Button("Cancel");

	if (getClass() == RangeConfig.class)
	    init();
    }

    @Override
    public void init() {
	super.init();

	setLayout(new GridLayout(5, 2));
	add(new Label("Min"));
	add(tf_min);
	add(new Label("Max"));
	add(tf_max);
	add(new Label("Small Step"));
	add(tf_smallStep);
	add(new Label("Big Step"));
	add(tf_bigStep);
	add(b_OK);
	add(b_cancle);
	pack();
	b_OK.addActionListener(this);
	b_cancle.addActionListener(this);
    }

    /** Set the apply these values to a PuDouble. **/

    public void apply(PuDouble dub) {
	dub.setBounds(min, max, smallStep, bigStep);
	dub.update(dub);
    }

    /** set up the values from a PuDouble **/

    public void setUp(PuDouble dub) {
	smallStep = dub.getLineIncr();
	max = dub.getMax();
	min = dub.getMin();
	bigStep = dub.getPageIncr();

	tf_min.setText(Double.toString(min));
	tf_max.setText(Double.toString(max));
	tf_smallStep.setText(Double.toString(smallStep));
	tf_bigStep.setText(Double.toString(bigStep));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
	Object source = event.getSource();
	if (source == b_OK) {
	    try {
		Double d;
		d = new Double(tf_min.getText());
		min = d.doubleValue();
		d = new Double(tf_max.getText());
		max = d.doubleValue();
		d = new Double(tf_smallStep.getText());
		smallStep = d.doubleValue();
		d = new Double(tf_bigStep.getText());
		bigStep = d.doubleValue();
	    } catch (NumberFormatException e) {
		PsDebug.error("Number Format Error", this);
	    }
	}
	setVisible(false);
    }
}
