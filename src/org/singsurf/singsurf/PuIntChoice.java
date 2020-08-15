/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf;

import java.awt.Choice;
import java.awt.Component;
import java.awt.Label;

import org.singsurf.singsurf.clients.AbstractClient;

import jv.object.PsObject;

/**
 * A control for a single int value. Based around PuDouble, but adds a ref
 * field. This field is useful when combined with MRpeEval as it can store a
 * reference to the corresponding variable.
 * 
 * @author Rich Morris Created on 31-Mar-2005
 */
public class PuIntChoice extends PsObject {
    private static final long serialVersionUID = 1L;
//    IntFractometer control;
    Choice choice;
    Label label;
    PsObject parent;
    int ref = -1;

	public PuIntChoice(AbstractClient client, String str, int val,int[] values) {
		parent = client;
		label = new Label(str);
		choice = new Choice();
		for(int item:values) {
			choice.add(Integer.toString(item));
		}
		choice.select(Integer.toString(val));
		choice.addItemListener(client);
	}

//	public PuIntOption(AbstractClient client, String str, int val, int min) {
//		this(client,str,val);
////		control.setMinVal(min);
//	}

	@Override
    public String getName() {
	return label.getText();
    }

    @Override
    public boolean update(Object arg0) {
	// System.out.println("Lparam update"+arg0.toString());
	return parent.update(this);
    }

    public Component getLabel() {
	return label;
    }

    public Component getControlPanel() {
	return choice;
    }

    public int getVal() {
	return Integer.valueOf(choice.getSelectedItem());
    }

    public void setVal(int val) {
		choice.select(Integer.toString(val));
    }

    void setRef(int r) {
	ref = r;
    }

    int getRef() {
	return ref;
    }

	@Override
	public String toString() {
		return "PuInt "+getName()+" val "+getVal()+" ref "+ ref;
	}
	
	

}