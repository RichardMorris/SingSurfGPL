/**
 * 
 */
package org.singsurf.singsurf;

import java.awt.Component;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;

import org.singsurf.singsurf.definitions.DefVariable;

import jv.object.PsObject;

public class PuVariable  extends PsObject {
    private static final long serialVersionUID = 1L;

	Label minLabel,maxLabel,stepsLabel;
	Fractometer minControl,maxControl;
	IntFractometer stepsFract;
	PsObject parent;
	Panel minPanel,maxPanel;
	public PuVariable(PsObject obj,DefVariable var)
	{
		this(obj,var.getName(),var.getMin(),var.getMax(),var.getSteps());
	}
	public PuVariable(PsObject obj,String name,double min,double max,int steps) {
		parent = obj;
		minLabel = new Label(name+" min");
		minControl = new Fractometer(min);
		minControl.setParent(this);
		maxControl = new Fractometer(max);
		maxControl.setParent(this);
		maxLabel = new Label(name+" max");
		stepsLabel = new Label(name+" steps");
		stepsFract = new IntFractometer(steps,2);
		stepsFract.setParent(this);
		Font baseFont = Font.decode(System.getProperty("font"));
		minLabel.setFont(baseFont);
		maxLabel.setFont(baseFont);
		stepsLabel.setFont(baseFont);
	}

	public double getMin() {
		return minControl.getValue();
	}
	public double getMax() {
		return maxControl.getValue();
	}
	public int getSteps() {
		return stepsFract.getIntValue();
	}
	public void setMin(double m) {
		minControl.setValue(m);
	}
	public void setMax(double m) {
		maxControl.setValue(m);
	}
	public void setSteps(int s) {
		stepsFract.setValue(s);
	}
	public void setBounds(double mi,double ma,int s) {
		minControl.setValue(mi);
		maxControl.setValue(ma);
		stepsFract.setValue(s);
	}
	public void set(DefVariable var) {
		String name = var.getName();
        minLabel.setText(name + " min");
		minControl.setValue(var.getMin());
		maxLabel.setText(name + " max");
		maxControl.setValue(var.getMax());
		stepsLabel.setName(name + " steps");
		//stepsControl.setDefValue(var.getSteps());
		stepsFract.setValue(var.getSteps());
	}
	public Component getMinLabel() {
		return minLabel;
	}
	public Component getMaxLabel() {
		return maxLabel;
	}

	public Component getStepsLabel() {
		return stepsLabel;
	}
public Panel getMinPanel() {
		return minControl;
	}
	public Panel getMaxPanel() {
		return maxControl;
	}
	public Panel getStepsPanel() {
		return stepsFract; 
	}

	@Override
    public boolean update(Object arg0) {
//		System.out.println("Lparam update"+arg0.toString());
		return parent.update(this);
	}
	public Fractometer getMaxControl() {
		return maxControl;
	}
	public Fractometer getMinControl() {
		return minControl;
	}
	public IntFractometer getStepsControl() {
		return stepsFract;
	}

	public void setDP(int dp) {
		this.maxControl.setDecimalPlaces(dp);
		this.minControl.setDecimalPlaces(dp);
	}
	public void setScale(int scale) {
		this.stepsFract.setScale(scale);
	}
}