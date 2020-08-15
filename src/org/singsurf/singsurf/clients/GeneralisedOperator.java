/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

public interface GeneralisedOperator {
	public void setIngredient(AbstractClient inCalc);
	public boolean goodIngredient();
	AbstractClient getIngredient();
	String getIngridientName();
}
