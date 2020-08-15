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
import org.singsurf.singsurf.calculators.ChainedCalculator;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;

public class GeneralizedMapping extends Mapping implements GeneralisedOperator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8862422439891056104L;

	/** A choice of avaliable inputs */
	protected Choice ch_ingredient = new Choice();;

//	Checkbox cbParamsFromTexture = new Checkbox("Parameters from texture",false);

	public GeneralizedMapping(GeomStore store, String projName) {

		super(store, projName);
		if (getClass() == GeneralizedMapping.class) {
			init(this.createDefaultDef());
		}
	}

	public GeneralizedMapping(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == GeneralizedMapping.class) {
			init(def);
		}
	}

	@Override
	public Definition createDefaultDef() {
		Definition def;
		def = new Definition("Gen Map", DefType.genMap, "");
		def.add(new DefVariable("x", "none"));
		def.add(new DefVariable("y", "none"));
		def.add(new DefVariable("z", "none"));
		def.add(new DefVariable("S", "psurf"));
		def.setOpType(DefType.psurf);
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
		calc = new ChainedCalculator(def, 0);
		calc.build();
		if (!calc.isGood())
			showStatus(calc.getMsg());
		// ingredientVar = calc.getDefVariable(0);
		setDisplayEquation(def.getEquation());
		refreshParams();
		// calcSurf();

		Option sfopt = def.getOption("showFace");
		if (sfopt != null)
			this.cbShowFace.setState(sfopt.getBoolVal());
		Option seopt = def.getOption("showEdge");
		if (seopt != null)
			this.cbShowEdge.setState(seopt.getBoolVal());

		Option svopt = def.getOption("showVert");
		if (svopt != null)
			this.cbShowVert.setState(svopt.getBoolVal());
		Option scopt = def.getOption("showCurve");
		if (scopt != null)
			this.cbShowCurves.setState(scopt.getBoolVal());
		Option colopt = def.getOption("colour");
		if (colopt != null)
			this.chCurveColours.select(colopt.getStringVal());

		Option clipopt = def.getOption("clipping");
		if (clipopt != null)
			this.m_Clipping.setValue(clipopt.getDoubleVal());

		Option contopt = def.getOption("continuity");
		if (contopt != null)
			this.m_ContDist.setValue(contopt.getDoubleVal());
	}

	@Override
	public void setDefinitionOptions(Definition def) {

		def.setOption("showFace", this.cbShowFace.getState());
		def.setOption("showEdge", this.cbShowEdge.getState());

		def.setOption("showVert", this.cbShowVert.getState());
		def.setOption("showCurve", this.cbShowCurves.getState());
		def.setOption("colour", this.chCurveColours.getSelectedItem());

		def.setOption("clipping", this.m_Clipping.getValue());
		def.setOption("continuity", this.m_ContDist.getValue());

	}

	AbstractClient ingredient;

	@Override
	public AbstractClient getIngredient() {
		return ingredient;
	}

	@Override
	public void setIngredient(AbstractClient ingr) {
		ingredient = ingr;

		((ChainedCalculator) calc).setIngredient(ingr.getCalculator());
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
		if (goodIngredient() && ((ChainedCalculator) calc).getIngredient() == inCalc)
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
			ChainedCalculator cc = (ChainedCalculator) calc;
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
		return calc != null && ((ChainedCalculator) calc).goodIngredient();
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
