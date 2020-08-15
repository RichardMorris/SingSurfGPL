/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

public interface GeneralisedBiOperator {
	public void setIngredient1(AbstractClient client);
	public void setIngredient2(AbstractClient client);
	public boolean goodIngredients();
	public AbstractClient getIngredient1();
	public AbstractClient getIngredient2();
	
}
