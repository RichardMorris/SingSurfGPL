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
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.operators.TangentSpaceCalcMap;
import org.singsurf.singsurf.operators.vectorfields.AbstractVectorField;
import org.singsurf.singsurf.operators.vectorfields.CubicVectorField;
import org.singsurf.singsurf.operators.vectorfields.UmbilicField;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgVectorField;
import jv.project.PgGeometryIf;

public class GenVecField extends VecField implements GeneralisedOperator {

	private static final String CUBIC = "Cubic";
	private static final String RIDGE = "Ridge";

	/**
	 * 
	 */
	private static final long serialVersionUID = 8862422439891056104L;
	/**
	 * The default equation (x^2 y - y^3 - z^2 = 0.0;).
	 */

//	{
//		programName = "GeneralizedVecField";
//	}

	/** The variable corresponding to the ingredient */
	// DefVariable ingredientVar;

	/** A choice of available inputs */
	protected Choice ch_ingredient = new Choice();

	/** Whether to project onto the given surface. */
	protected Checkbox cbProject = new Checkbox("Project onto surface", false);

//	TangentSpaceCalcMap projection;

	AbstractClient ingredient;

	public GenVecField(GeomStore store, String projName) {

		super(store, projName);
		if (getClass() == GenVecField.class) {
			init(this.createDefaultDef());
		}
	}

	public GenVecField(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == GenVecField.class) {
			init(def);
		}
	}

	@Override
	public Definition createDefaultDef() {
		Definition def;
		def = new Definition("Gen VField", DefType.genVfield, "");
		def.add(new DefVariable("x", "none"));
		def.add(new DefVariable("y", "none"));
		def.add(new DefVariable("S", "psurf"));
		def.setOpType(DefType.psurf);
		return def;
	}

	@Override
	public void init(Definition def) {

		ch_ingredient.addItemListener(this);
		this.cbProject.addItemListener(this);
		chOrientation.addItemListener(this);
		chOrientation.add(CUBIC);
		chOrientation.add(RIDGE);
		super.init(def);
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
	public void rebuildClient() {
		this.buildFieldOperator();
	}

	@Override
	public void setDefinitionOptions(Definition def) {
		super.setDefinitionOptions(def);
		def.setOption("project", this.cbProject.getState());
		def.setOption("orientation", this.chOrientation.getSelectedItem());
	}

	public void extractDefOptions(Definition def) {
		super.extractDefOptions(def);
		Option projopt = def.getOption("project");
		if (projopt != null)
			this.cbProject.setState(projopt.getBoolVal());
		Option orient = def.getOption("orientation");
		if (orient != null)
			this.chOrientation.select(orient.getStringVal());

	}
	
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
		} else if (itSel == this.cbProject) {
			this.calcGeoms();
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
		if (((ChainedCalculator) calc).getIngredient() == inCalc) {
			this.setIngredient(client);
		}
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

	
	@Override
	public PgVectorField[] calcVFGeomThread(GeomPair p) {
		// TODO Auto-generated method stub
		PgVectorField[] fields =  super.calcVFGeomThread(p);
		if(fields==null) return null;
		if(this.cbProject.getState()) {
			TangentSpaceCalcMap projection = new TangentSpaceCalcMap(
				ingredient.getCalculator().createEvaluator(),
				this.doNormilization,
				calc.getNumInputVariables(),this.lengthControl.getVal());
			
			for(int i=0;i<fields.length;++i) {
				try {
					projection.operateVectorField(fields[i]);
				} catch (EvaluationException e) {
					e.printStackTrace();
				}
			}
/*			
			for(int i=0;i<10;++i) {
				PdVector v1 = fields[0].getVector(i);
				PdVector v2 = fields[1].getVector(i);
				PdVector v3 = fields[2].getVector(i);
				PdVector v4 = fields[3].getVector(i);
				System.out.printf("%d %6.3e %6.3e %6.3e %6.3e %6.3e%n", i,
						v1.dot(v1),v1.dot(v2),v1.dot(v3),v1.dot(v4),v3.dot(v3));
			}
*/
		}
		return fields;
		
	}

	@Override
	public AbstractVectorField buildFieldOperator() {
		if(this.chOrientation.getSelectedItem().equals(CUBIC)) {
			return new CubicVectorField(calc.createEvaluator());
		} else if(this.chOrientation.getSelectedItem().equals(RIDGE)) {
				return new UmbilicField(calc.createEvaluator());
			} else {
		return super.buildFieldOperator();
		}
	}

	public void displayVFGeom(GeomPair p, PgVectorField[] vecs) {
		
		System.out.println("mapped outer");
		// debugCols((PgElementSet) mappedGeom);
		PgGeometryIf out = p.getOutput();
//		PgGeometryIf input = p.getInput();

		
		if (out instanceof PgElementSet) {
			((PgElementSet) out).showElements(false);
			((PgElementSet) out).showEdges(false);
		}

		System.out.println("mapped outer");

		System.out.println("out " + ((PgPointSet) out).getNumVertices());

		String type = this.chOrientation.getSelectedItem();
		
		if (type.equals(CUBIC) || type.equals(RIDGE)) {
			for(PgVectorField field:vecs) {
				setColour(field, this.chCurveColours.getSelectedItem());
				field.showVectorColors(false);
				field.showVectorArrows(false);
			}
		} else if (type.equals("Eigenvectors") || type.equals("Implicit")) {
			setColour(vecs[0], this.chCurveColours.getSelectedItem());
			vecs[0].showVectorColors(false);
			setColour(vecs[1], this.chCurveColours.getSelectedItem());
			vecs[1].showVectorColors(false);
			setColour(vecs[2], this.chColours2.getSelectedItem());
			vecs[2].showVectorColors(false);
			setColour(vecs[3], this.chColours2.getSelectedItem());
			vecs[3].showVectorColors(false);
			vecs[0].showVectorArrows(false);
			vecs[1].showVectorArrows(false);
			vecs[2].showVectorArrows(false);
			vecs[3].showVectorArrows(false);
		} else {
			setColour(vecs[0], this.chCurveColours.getSelectedItem());
			vecs[0].showVectorColors(false);
			vecs[0].showVectorArrows(true);
		}

		setGeometryInfo(out,p.getInput());
		store.geomChanged(out);
	}

}
