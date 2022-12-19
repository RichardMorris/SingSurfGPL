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
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;

public class BiIntersection extends Intersection implements GeneralisedBiOperator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8862422439891056104L;

	/** A choice of available inputs */
	protected Choice ch_ingredient1 = new Choice();
	protected Choice ch_ingredient2 = new Choice();

	public BiIntersection(GeomStore store, Definition def) {
		super(store, def);
		if (getClass() == BiIntersection.class) {
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
		checkDef(def);
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

		Option svopt = def.getOption("showVert");
		if (svopt != null)
			this.cbShowVert.setState(svopt.getBoolVal());
		Option scopt = def.getOption("showCurve");
		if (scopt != null)
			this.cbShowCurves.setState(scopt.getBoolVal());
		Option colopt = def.getOption("colour");
		if (colopt != null)
			this.chCurveColours.select(colopt.getStringVal());

		Option ittopt = def.getOption("numItts");
		if (ittopt != null)
			this.chItts.select(ittopt.getStringVal());

		// calcSurf();
	}

	@Override
	public void setDefinitionOptions(Definition def) {

		def.setOption("showVert", this.cbShowVert.getState());
		def.setOption("showCurve", this.cbShowCurves.getState());
		def.setOption("colour", this.chCurveColours.getSelectedItem());
		def.setOption("numItts", Integer.parseInt(this.chItts.getSelectedItem()));
	}

	AbstractProject ingredient1;
	AbstractProject ingredient2;

	@Override
	public void setFirstIngredient(AbstractProject client) {
		this.ingredient1 = client;
		((BiChainedCalculator) calc).setFirstIngredient(client.getCalculator());
	}

	@Override
	public void setSecondIngredient(AbstractProject client) {
		this.ingredient2 = client;
		((BiChainedCalculator) calc).setSecondIngredient(client.getCalculator());
		boolean goodIngredients = goodIngredients();
		System.out.println("setIgr2 " + goodIngredients);
	}

	@Override
	public AbstractProject getFirstIngredient() {
		return ingredient1;
	}

	@Override
	public AbstractProject getSecondIngredient() {
		return ingredient2;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();

		if (itSel == ch_ingredient1) {
			String ingrName = ch_ingredient1.getSelectedItem();
			if (ingrName.equals(NONE))
				return;
			setFirstIngredient(store.getGenerator(ingrName));
		} else if (itSel == ch_ingredient2) {
			String ingrName = ch_ingredient2.getSelectedItem();
			if (ingrName.equals(NONE))
				return;
			setSecondIngredient(store.getGenerator(ingrName));
		} else
			super.itemStateChanged(e);
	}

	@Override
	public void geometryHasChanged(String geomName) {
		super.geometryHasChanged(geomName);
		if (!calc.isGood())
			return;
		if (goodIngredients()
				&& (((BiChainedCalculator) calc).getFirstIngredient().getDefinition().getName().equals(geomName)
						|| ((BiChainedCalculator) calc).getSecondIngredient().getDefinition().getName().equals(geomName))) {
			this.calcGeoms();
		}
	}

	@Override
	public void geometryDefHasChanged(AbstractProject client, Calculator inCalc) {
		if (goodIngredients() && ((BiChainedCalculator) calc).getFirstIngredient() == inCalc)
			this.setFirstIngredient(client);

		if (goodIngredients() && ((BiChainedCalculator) calc).getSecondIngredient() == inCalc)
			this.setSecondIngredient(client);
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
		String s = getName() + "(" + this.getIngridient1Name() + "," + this.getIngridient2Name() + "," + name + ")";
		return s;
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
	public void loadProjectComponents(ProjectComponents comp) {
		if (comp.getIngredients().size() >= 1) {
			String name = comp.getIngredients().get(0);
			this.setFirstIngredient(store.getProject(name));
			this.ch_ingredient1.select(name);

		}
		if (comp.getIngredients().size() >= 2) {
			String name = comp.getIngredients().get(1);
			this.setSecondIngredient(store.getProject(name));
			this.ch_ingredient2.select(name);
		}
		super.loadProjectComponents(comp);
	}

	public String getIngridient1Name() {
		if (this.goodIngredients()) {
			return this.getFirstIngredient().getName();
		}
		return "null";
	}

	public String getIngridient2Name() {
		if (this.goodIngredients()) {
			return this.getSecondIngredient().getName();
		}
		return "null";
	}

}
