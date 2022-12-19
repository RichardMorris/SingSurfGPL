package org.singsurf.singsurf.clients;

public interface DualIngredientProject {
	AbstractProject getFirstIngredient();
	AbstractProject getSecondIngredient();
	void setFirstIngredient(AbstractProject client);
	void setSecondIngredient(AbstractProject client);
}
