/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

public interface GeneralisedOperator {
	public void setIngredient(AbstractProject inCalc);
	public boolean goodIngredient();
	AbstractProject getIngredient();
	String getIngredientName();
}
