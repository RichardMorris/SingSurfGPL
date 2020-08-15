package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.SortedSet;

import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.calculators.ChainedCalculator;
import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.operators.SimpleCalcMap;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;
import org.singsurf.singsurf.operators.vectorfields.AbstractIntergralCurve;
import org.singsurf.singsurf.operators.vectorfields.AbstractVectorField;
import org.singsurf.singsurf.operators.vectorfields.ConstantVectorField;
import org.singsurf.singsurf.operators.vectorfields.EigenVectorField;
import org.singsurf.singsurf.operators.vectorfields.ImplicitVectorField;
import org.singsurf.singsurf.operators.vectorfields.RC4MethodUnorientedIC;
import org.singsurf.singsurf.operators.vectorfields.SelectVectorField;
import org.singsurf.singsurf.operators.vectorfields.SimpleCalcField;
import org.singsurf.singsurf.operators.vectorfields.UnorientedVectorField;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

public class GenICurve extends ICurve implements GeneralisedOperator {
	private static final String UNORIENTED = "Unoriented";

	private static final String ORIENTED = "Oriented";

	private static final String IMPLICIT = "Implicit";

	private static final long serialVersionUID = 1L;

	private static final String EIGENVECTOR1 = "Eigenvectors 1";
	private static final String EIGENVECTOR2 = "Eigenvectors 2";

	private static final String POSX = "Pos X";
	private static final String NEGX = "Neg X";
	private static final String POSY = "Pos Y";
	private static final String NEGY = "Neg Y";
	private static final String POSZ = "Pos Z";
	private static final String NEGZ = "Neg Z";
	private static final String INDIR = "Inwards";
	private static final String OUTDIR = "Outwards";

	/** A choice of available inputs */
	protected Choice ch_ingredient = new Choice();

	/** Whether to project onto the given surface. */
	protected Checkbox cbProject = new Checkbox("Project onto surface", false);

	AbstractClient ingredient;

	protected Choice ch_start = new Choice();

	public GenICurve(GeomStore store, String projName) {

		super(store, projName);
		if (getClass() == GenICurve.class) {
			init(this.createDefaultDef());
		}
	}

	public GenICurve(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == GenICurve.class) {
			init(def);
		}
	}

	public void init(Definition def) {

		chOrientation.add(EIGENVECTOR1);
		chOrientation.add(EIGENVECTOR2);

		ch_ingredient.addItemListener(this);
		cbProject.addItemListener(this);
		
		ch_start.add(POSX);
		ch_start.add(NEGX);
		ch_start.add(POSY);
		ch_start.add(NEGY);
		ch_start.add(POSZ);
		ch_start.add(NEGZ);
		ch_start.add(INDIR);
		ch_start.add(OUTDIR);

		super.init(def);
	}


	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();

		if (itSel == ch_ingredient) {
			String ingrName = ch_ingredient.getSelectedItem();
			if (ingrName.equals(NONE))
				return;
			setIngredient(store.getGenerator(ingrName));
		} else if (itSel == this.cbProject || itSel == ch_start) {
			this.calcGeoms();
		} else
			super.itemStateChanged(e);
	}

	@Override
	public void loadDefinition(Definition newdef) {
		Definition def = newdef.duplicate();
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		calc = new ChainedCalculator(def, 1);
		calc.build();
		if (!calc.isGood())
			showStatus(calc.getMsg());
		this.extractDefOptions(def);
		setDisplayEquation(def.getEquation());
		refreshParams();
	}

	
	@Override
	public void setIngredient(AbstractClient ingr) {
		ingredient = ingr;

		((ChainedCalculator) calc).setIngredient(ingr.getCalculator());
	}

	@Override
	public boolean goodIngredient() {
		return calc != null && ((ChainedCalculator) calc).goodIngredient();
	}

	@Override
	public AbstractClient getIngredient() {
		return ingredient;
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
	public void setDefinitionOptions(Definition def) {
		super.setDefinitionOptions(def);
		def.setOption("project", this.cbProject.getState());
		def.setOption("StartDir", this.ch_start.getSelectedItem());
	}
	
	public void extractDefOptions(Definition def) {
		super.extractDefOptions(def);
		Option projopt = def.getOption("project");
		if (projopt != null)
			this.cbProject.setState(projopt.getBoolVal());
		Option startdir = def.getOption("StartDir");
		if(startdir!=null)
			this.ch_start.select(def.getOption("StartDir").getStringVal());
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
		if (((ChainedCalculator) calc).getIngredient() == inCalc) {
			this.setIngredient(client);
		}
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
	public AbstractVectorField buildFieldOperator() {
		/** The operator which performs the mapping */
		AbstractVectorField field = null;

		String type = this.chOrientation.getSelectedItem();
		if (type.equals(ORIENTED)) {
			field = new SimpleCalcField(calc.createEvaluator());
			this.doNormilization = false;
		} else if (type.equals(UNORIENTED)) {
			field = new UnorientedVectorField(calc.createEvaluator());
			this.doNormilization = true;
		} else if (type.equals(EIGENVECTOR1)) {
			field = new SelectVectorField(new EigenVectorField(calc.createEvaluator()),0);
			this.doNormilization = true;
		} else if (type.equals(EIGENVECTOR2)) {
			field = new SelectVectorField(new EigenVectorField(calc.createEvaluator()),1);
			this.doNormilization = true;
		} else if (type.equals(IMPLICIT)) {
			field = new ImplicitVectorField(calc.createEvaluator());
			this.doNormilization = true;
		}
		return field;
	}

	@Override
	public AbstractIntergralCurve buildICurveOperator(AbstractVectorField field) {
		AbstractVectorField start=null;
		PdVector dir=null;
		switch(ch_start.getSelectedItem()) {
		case POSX:
			dir = new PdVector(1,0,0);
			start = new ConstantVectorField(dir);	
			break;
		case NEGX:
			dir = new PdVector(-1,0,0);
			start = new ConstantVectorField(dir);	
			break;
		case POSY:
			dir = new PdVector(0,1,0);
			start = new ConstantVectorField(dir);	
			break;
		case NEGY:
			dir = new PdVector(0,-1,0);
			start = new ConstantVectorField(dir);	
			break;
		case POSZ:
			dir = new PdVector(0,0,1);
			start = new ConstantVectorField(dir);	
			break;
		case NEGZ:
			dir = new PdVector(0,0,-1);
			start = new ConstantVectorField(dir);	
			break;

		case INDIR:
			start = new AbstractVectorField() {

				@Override
				public PdVector calcVector(PdVector vec) throws EvaluationException {
					PdVector v = PdVector.copyNew(vec);
					v.multScalar(-1);
					return v;
				}};
			break;
			
		case OUTDIR:
			start = new AbstractVectorField() {

				@Override
				public PdVector calcVector(PdVector vec) throws EvaluationException {
					PdVector v = PdVector.copyNew(vec);
					return v;
				}};
			break;
			
		}

		return new RC4MethodUnorientedIC(field,numSteps.getIntValue(),lengthControl.getVal(),start);
	}

	@Override
	public PgGeometryIf calcGeomThread(GeomPair p) {
		PgGeometryIf geom = super.calcGeomThread(p);
		if(this.cbProject.getState()) {
			final Calculator inCalc = this.getIngredient().getCalculator();
			Evaluator eval = inCalc.createEvaluator();
		    SimpleCalcMap projectionMap   = new SimpleCalcMap(eval,inCalc.derivDepth>0);

		    PgGeometryIf res;
			try {
				res = projectionMap.operate(geom);
			} catch (UnSuportedGeometryException e) {
				showStatus("Unsupported geometry type ");
				return null;
			} catch (EvaluationException e) {
				System.out.println(e.toString());
				calc.setGood(false);
				return null;
			}
		    geom = res;
		    
		}
		return geom;
	}

	
	
}
