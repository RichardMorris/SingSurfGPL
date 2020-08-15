/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.SortedSet;

import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.calculators.ProductCalculator;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;

public class GeneralizedExtrude extends Extrude implements GeneralisedOperator {
	private static final long serialVersionUID = 1L;

	/** A choice of available inputs */
	protected Choice ch_ingredient = new Choice();

	public GeneralizedExtrude(GeomStore store, String projName) {

		super(store, projName);
		if (getClass() == GeneralizedExtrude.class) {
			init(this.createDefaultDef());
		}
	}

	public GeneralizedExtrude(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == GeneralizedExtrude.class) {
			init(def);
		}
	}

	@Override
	public Definition createDefaultDef() {
		Definition def;
		def = new Definition("Gen Extrude", DefType.genClip, "");
		def.add(new DefVariable("t", -1,1,20));
		def.add(new DefVariable("x", "none"));
		def.add(new DefVariable("S", "pcurve"));
		def.setOpType(DefType.pcurve);
		return def;
	}

	@Override
	public void init(Definition def) {
		super.init(def);
		ch_ingredient.addItemListener(this);
	}

	@Override
	public void loadDefinition(Definition newdef) {
		Definition def = newdef.duplicate();
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		localY = def.getVar(0);
		displayVars[0].set(localY);
		calc = new ProductCalculator(def, 0);
		calc.build();
		if (!calc.isGood())
			showStatus(calc.getMsg());
//		map = new ColourCalcMap(calc.beginCalculation());

		setDisplayEquation(def.getEquation());
		refreshParams();

	}


	AbstractClient ingredient;

	@Override
	public AbstractClient getIngredient() {
		return ingredient;
	}

	@Override
	public void setIngredient(AbstractClient ingr) {
		ingredient = ingr;

		((ProductCalculator) calc).setIngredient(ingr.getCalculator());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();

		if (itSel == ch_ingredient) {
			String ingrName = ch_ingredient.getSelectedItem();
			if (ingrName.equals(NONE))
				return;
			setIngredient(store.getGenerator(ingrName));
		} else
			super.itemStateChanged(e);
	}

	@Override
	public void geometryHasChanged(String geomName) {
		super.geometryHasChanged(geomName);
		if (!calc.isGood())
			return;
		if (goodIngredient() && getIngridientName().equals(geomName)) {
			this.calcGeoms();
		}
	}

	@Override
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
		if (goodIngredient() && ((ProductCalculator) calc).getIngredient() == inCalc)
			this.setIngredient(client);
	}

	@Override
	public void geometryNameHasChanged(String oldName, String newName) {
		super.geometryNameHasChanged(oldName, newName);
	}

	@Override
	public void refreshList(SortedSet<String> list) {
		super.refreshList(list);
		String item = this.ch_ingredient.getSelectedItem();
		this.ch_ingredient.removeAll();
		this.ch_ingredient.add(NONE);
		for (String name : list)
			this.ch_ingredient.add(name);
		this.ch_ingredient.select(item);
	}

	@Override
	public String getPreferredOutputName(String name) {

		return getName() + "(" + getIngridientName() + "," + name + ")";
	}

	@Override
	public String getIngridientName() {
		if (calc != null) {
			ProductCalculator cc = (ProductCalculator) calc;
			if (cc != null) {
				Calculator ing = cc.getIngredient();
				if (ing != null)
					return ing.getDefinition().getName();
			}
		}
		return "null";
	}

	@Override
	public boolean goodIngredient() {
		return calc != null && ((ProductCalculator) calc).goodIngredient();
	}

	@Override
	public ProjectComponents getProjectComponents() {
		ProjectComponents pc = super.getProjectComponents();
		pc.addIngredient(this.ingredient.getName());
		return pc;
	}

	@Override
	public void loadProjectComponents(ProjectComponents comp, PaSingSurf ss) {
		if (comp.getIngredients().size() >= 1) {
			String name = comp.getIngredients().get(0);
			this.setIngredient(ss.getProject(name));
			this.ch_ingredient.select(name);
		}
		super.loadProjectComponents(comp, ss);
	}

}
