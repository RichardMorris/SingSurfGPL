/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.SortedSet;

import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.calculators.BiChainedCalculator;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;

public class BiMap extends Mapping implements GeneralisedBiOperator {

	private static final long serialVersionUID = 1L;

	/** The variable corresponding to the ingredient */
	// DefVariable ingredientVar;

	/** A choice of available inputs */
	protected Choice ch_ingredient1 = new Choice();
	protected Choice ch_ingredient2 = new Choice();

	public BiMap(GeomStore store, Definition def) {
		super(store, def);
		if (getClass() == BiMap.class) {
			init(def);
		}
	}

	@Override
	public void init(Definition def) {
		super.init(def);
		ch_ingredient1.addItemListener(this);
		ch_ingredient2.addItemListener(this);
	}

	@Override
	public void loadDefinition(Definition newdef) {
		Definition def = newdef.duplicate();
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		calc = new BiChainedCalculator(def, 0);
		calc.build();
		if (!calc.isGood())
			showStatus(calc.getMsg());
		// ingredientVar = calc.getDefVariable(0);
		setDisplayEquation(def.getEquation());
		refreshParams();
		// calcSurf();
	}

	AbstractClient ingredient1;
	AbstractClient ingredient2;

	@Override
	public void setIngredient1(AbstractClient client) {
		this.ingredient1 = client;
		((BiChainedCalculator) calc).setIngredient1(client.getCalculator());
		// this.projectionMap = new SimpleCalcMap(inCalc);
		// this.ch_inputSurf.setEnabled(goodIngredients());
	}

	@Override
	public void setIngredient2(AbstractClient client) {
		this.ingredient2 = client;
		((BiChainedCalculator) calc).setIngredient2(client.getCalculator());
		// this.projectionMap = new SimpleCalcMap(inCalc);
		boolean goodIngredients = goodIngredients();
		System.out.println("setIgr2 " + goodIngredients);
		// this.ch_inputSurf.setEnabled(goodIngredients);
	}

	@Override
	public AbstractClient getIngredient1() {
		return ingredient1;
	}

	@Override
	public AbstractClient getIngredient2() {
		return ingredient2;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();

		if (itSel == ch_ingredient1) {
			String ingrName = ch_ingredient1.getSelectedItem();
			if (ingrName.equals(NONE))
				return;
			setIngredient1(store.getGenerator(ingrName));
		} else if (itSel == ch_ingredient2) {
			String ingrName = ch_ingredient2.getSelectedItem();
			if (ingrName.equals(NONE))
				return;
			setIngredient2(store.getGenerator(ingrName));
		} else
			super.itemStateChanged(e);
	}

	@Override
	public void geometryHasChanged(String geomName) {
		super.geometryHasChanged(geomName);
		if (!calc.isGood())
			return;
		if (goodIngredients()
				&& (((BiChainedCalculator) calc).getIngredient1().getDefinition().getName().equals(geomName)
						|| ((BiChainedCalculator) calc).getIngredient2().getDefinition().getName().equals(geomName))) {
			this.calcGeoms();
		}
	}

	@Override
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
		if (goodIngredients() && ((BiChainedCalculator) calc).getIngredient1() == inCalc)
			this.setIngredient1(client);

		if (goodIngredients() && ((BiChainedCalculator) calc).getIngredient2() == inCalc)
			this.setIngredient2(client);
	}

	@Override
	public void geometryNameHasChanged(String oldName, String newName) {
		super.geometryNameHasChanged(oldName, newName);
	}

	@Override
	public void refreshList(SortedSet<String> list) {
		super.refreshList(list);
		String item1 = this.ch_ingredient1.getSelectedItem();
		String item2 = this.ch_ingredient2.getSelectedItem();
		this.ch_ingredient1.removeAll();
		this.ch_ingredient2.removeAll();
		this.ch_ingredient1.add(NONE);
		this.ch_ingredient2.add(NONE);
		for (String name : list) {
			this.ch_ingredient1.add(name);
			this.ch_ingredient2.add(name);
		}
		this.ch_ingredient1.select(item1);
		this.ch_ingredient2.select(item2);
	}

	@Override
	public String getPreferredOutputName(String name) {
		return getName() + "(" + this.getIngridient1Name() + "," + this.getIngridient2Name() + "," + name + ")";
	}

	@Override
	public boolean goodIngredients() {
		return calc != null && ((BiChainedCalculator) calc).goodIngredients();
	}

	@Override
	public ProjectComponents getProjectComponents() {
		ProjectComponents pc = super.getProjectComponents();
		pc.addIngredient(this.ingredient1.getName());
		pc.addIngredient(this.ingredient2.getName());
		return pc;
	}

	@Override
	public void loadProjectComponents(ProjectComponents comp, PaSingSurf ss) {
		if (comp.getIngredients().size() >= 1) {
			String name = comp.getIngredients().get(0);
			this.setIngredient1(ss.getProject(name));
			this.ch_ingredient1.select(name);

		}
		if (comp.getIngredients().size() >= 2) {
			String name = comp.getIngredients().get(1);
			this.setIngredient2(ss.getProject(name));
			this.ch_ingredient2.select(name);
		}
		super.loadProjectComponents(comp, ss);
	}

	public String getIngridient1Name() {
		if (this.goodIngredients()) {
			return this.getIngredient1().getName();
		}
		return "null";
	}

	public String getIngridient2Name() {
		if (this.goodIngredients()) {
			return this.getIngredient2().getName();
		}
		return "null";
	}

}
