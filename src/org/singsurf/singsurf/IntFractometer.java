package org.singsurf.singsurf;

import java.text.NumberFormat;

public class IntFractometer extends Fractometer {
	private static final long serialVersionUID = 1L;
	int scale = 1;
	
	public IntFractometer(int value) {
		super(value);
		nf = NumberFormat.getIntegerInstance();	
		this.decimalPlaces=0;
	}

	public IntFractometer(int value,int min) {
		super(value);
		nf = NumberFormat.getIntegerInstance();	
		this.setMinVal(min);
		this.decimalPlaces=0;
	}

	@Override
	public void setDecimalPlaces(int decimalPlaces) {
	}

	protected void adjustValue(int amount) {
		setValue(this.value+scale * amount);
		if(jvParent!=null) jvParent.update(this);
	}

	public int getIntValue() {
		return (int) getValue();
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public void setIntValue(int val) {
		setValue(val);
	}
	
	

}
