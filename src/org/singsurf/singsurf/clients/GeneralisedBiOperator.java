/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

public interface GeneralisedBiOperator {
	public void setFirstIngredient(AbstractProject client);
	public void setSecondIngredient(AbstractProject client);
	public boolean goodIngredients();
	public AbstractProject getFirstIngredient();
	public AbstractProject getSecondIngredient();	
}
