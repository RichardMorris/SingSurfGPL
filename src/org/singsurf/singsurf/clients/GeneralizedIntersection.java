/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.SortedSet;

import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.calculators.ChainedCalculator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import org.singsurf.singsurf.operators.SimpleCalcIntersection;
import org.singsurf.singsurf.operators.SimpleCalcMap;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import jv.project.PgGeometryIf;

public class GeneralizedIntersection extends Intersection implements GeneralisedOperator {

    /**
     * 
     */
    private static final long serialVersionUID = 8862422439891056104L;

    /** A choice of avaliable inputs */
    protected Choice ch_ingredient = new Choice();

    /** Whether to project onto the given surface. */
    protected Checkbox cbProject = new Checkbox("Project onto surface", false);

	Checkbox cbParamsFromTexture = new Checkbox("Parameters from texture",false);

 

    public GeneralizedIntersection(GeomStore store, Definition def) {
	super(store, def);
	if (getClass() == GeneralizedIntersection.class) {
	    setDisplayEquation(def.getEquation());
	    init(def);
	}
    }

    @Override
    public void init(Definition def) {
	super.init(def);
	ch_ingredient.addItemListener(this);
	cbProject.addItemListener(this);
    }

    @Override
    public void loadDefinition(Definition newdef) {
	Definition def = newdef.duplicate();
	checkDef(def);
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

	Option ittopt = def.getOption("numItts");
	if (ittopt != null)
	    this.chItts.select(ittopt.getStringVal());

	Option projopt = def.getOption("project");
	if (projopt != null)
	    this.cbProject.setState(projopt.getBoolVal());
	
	setCheckboxStateFromOption(cbParamsFromTexture,def,"parametersFromTextures");		

	// calcSurf();
    }

    @Override
    public void setDefinitionOptions(Definition def) {

	def.setOption("showFace", this.cbShowFace.getState());
	def.setOption("showEdge", this.cbShowEdge.getState());

	def.setOption("showVert", this.cbShowVert.getState());
	def.setOption("showCurve", this.cbShowCurves.getState());
	def.setOption("colour", this.chCurveColours.getSelectedItem());
	def.setOption("numItts", Integer.parseInt(this.chItts.getSelectedItem()));
	def.setOption("project", this.cbProject.getState());

	def.setOption("parametersFromTextures", cbParamsFromTexture.getState());		

    }

    AbstractClient ingredient;

	private Calculator inCalc;

    @Override
    public AbstractClient getIngredient() {
	return ingredient;
    }

    @Override
    public void setIngredient(AbstractClient ingr) {
	ingredient = ingr;

	inCalc = ingr.getCalculator();
	((ChainedCalculator) calc).setIngredient(inCalc);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
	ItemSelectable itSel = e.getItemSelectable();

	if (itSel == ch_ingredient) {
	    String ingrName = ch_ingredient.getSelectedItem();
	    if (ingrName.equals(NONE))
		return;
	    setIngredient(store.getGenerator(ingrName));
	} else if (itSel == this.cbProject) {
		this.calcGeoms();
	} else
	    super.itemStateChanged(e);
    }

	public PgGeometryIf calcGeom(PgGeometryIf input) throws UnSuportedGeometryException, EvaluationException {
		SimpleCalcIntersection intersectAlgorithm = null;
		intersectAlgorithm = new SimpleCalcIntersection(
				calc.createEvaluator(),
				Integer.parseInt(chItts.getSelectedItem()));
		intersectAlgorithm.setParamsFromTexture(cbParamsFromTexture.getState());

		PgGeometryIf result = intersectAlgorithm.operate(input);

	
	if(this.cbProject.getState()) {
		   /** The operator which performs the mapping */
	    SimpleCalcMap projectionMap   = new SimpleCalcMap(inCalc.createEvaluator(),inCalc.getDerivDepth()>0);

	    PgGeometryIf res = projectionMap.operate(result);
	    result = res;
	}
	return result;
    }

    @Override
    public void geometryHasChanged(String geomName) {
	super.geometryHasChanged(geomName);
	if (!calc.isGood())
	    return;
	if (goodIngredient() && ((ChainedCalculator) calc).getIngredient().getDefinition().getName().equals(geomName)) {
	    this.calcGeoms();
	}
    }

    @Override
    public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
	if (goodIngredient() && ((ChainedCalculator) calc).getIngredient() == inCalc)
	    this.setIngredient(client);
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
	return getName() + "(" + this.getIngridientName() + "," + name
		+ ")";
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

    @Override
    public String getIngridientName() {
	if(calc!=null) {
	    ChainedCalculator cc = (ChainedCalculator) calc;
	    if(cc!=null) { 
		Calculator ing = cc.getIngredient();
		if(ing!=null)
		    return ing.getDefinition().getName();
	    }
	}
	return "null";
    }
}
