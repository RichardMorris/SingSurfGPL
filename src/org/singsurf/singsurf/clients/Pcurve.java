/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.List;

import jv.geom.PgPolygonSet;
import jv.number.PuDouble;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.LmsPolygonSetMaterial;
import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.operators.SphereClip;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class Pcurve extends AbstractClient {
	private static final long serialVersionUID = 6873398333105602351L;

	Definition def;
	protected PuDouble m_Clipping;
	int globalSteps = 60;
	DefVariable localX;
	protected PgPolygonSet outCurve;

	public Pcurve(GeomStore store, String name) {
		super(store, name);
		if (getClass() == Pcurve.class) {
			init(this.createDefaultDef());
		}
	}

	public Pcurve(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == Pcurve.class) {
			init(def);
		}
	}

	public void init(Definition def1) {
		super.init();
		String vname = "x"; // def.getVar(0).getName();
		localX = new DefVariable(vname, "Normal");
		final PuVariable displayVar = new PuVariable(this, localX);
		displayVar.getStepsControl().setMinVal(1);
		displayVars = new PuVariable[] { displayVar };
		newParams = new LParamList(this);
		m_Clipping = new PuDouble("Clipping", this);
		m_Clipping.setValue(100);
		this.cbShowVert.setState(false);
		loadDefinition(def1);
	}

	@Override
	public Definition createDefaultDef() {
		Definition def1;
		def1 = new Definition("PCurve", DefType.pcurve, "");
		def1.add(new DefVariable("x", -1, 1, 20));
		return def1;
	}

	boolean checkDef(Definition def1) {
		if (def1.getNumVars() != 1)
			return false;
		DefVariable var = def1.getVar(0);
		if (var.getSteps() == -1)
			var.setBounds(var.getMin(), var.getMax(), globalSteps);
		return true;
	}

	@Override
	public void loadDefinition(Definition newdef) {
		def = newdef.duplicate();
		// boolean flag =
		checkDef(def);
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		calc = new Calculator(def, 0);
		calc.build();
		if (!calc.isGood())
			showStatus(calc.getMsg());
		setDisplayEquation(def.getEquation());
		localX = calc.getDefVariable(0);
		displayVars[0].set(localX);
		refreshParams();
		outCurve = store.aquireCurve(newdef.getName(), this);

		Option svopt = def.getOption("showVert");
		if (svopt != null)
			this.cbShowVert.setState(svopt.getBoolVal());
		Option scopt = def.getOption("showCurve");
		if (scopt != null)
			this.cbShowCurves.setState(scopt.getBoolVal());
		Option colopt = def.getOption("colour");
		if (colopt != null)
			this.chCurveColours.select(colopt.getStringVal());

		calcGeoms();
	}

	@Override
	public void setDefinitionOptions(Definition def) {
		def.setOption("showVert", this.cbShowVert.getState());
		def.setOption("showCurve", this.cbShowCurves.getState());
		def.setOption("colour", this.chCurveColours.getSelectedItem());
	}

	public void variableRangeChanged(int n, PuVariable v) {
		calc.setVarBounds(n, v.getMin(), v.getMax(), v.getSteps());
		if (n == 0)
			localX.setBounds(v.getMin(), v.getMax(), v.getSteps());
		rebuildResultArray();
		calcGeoms();
	}

	public void rebuildResultArray() {
		if (outCurve == null)
			return;
		outCurve.setNumVertices(localX.getSteps());
		outCurve.setNumPolygons(1);
		outCurve.setDimOfPolygons(localX.getSteps());
		outCurve.assureVertexTextures();
		int a[] = new int[localX.getSteps()];
		for (int i = 0; i < localX.getSteps(); ++i)
			a[i] = i;
		outCurve.setPolygon(0, new PiVector(a));
		outCurve.removeVertexColors();
		outCurve.showVertexColors(false);
	}

	@Override
	public void calcGeoms() {
		if (!calc.isGood()) {
			showStatus(calc.getMsg());
			return;
		}
		Evaluator ce = calc.createEvaluator();

		// if(outCurve.getNumVertices()!= (localX.getSteps()+1))
		rebuildResultArray();
		if (outCurve != null)
			line_mat = new LmsPolygonSetMaterial(outCurve);

		// System.out.println("Num vertices: "+m_geom.getNumVertices());
		// System.out.println("x steps "+psVars[0].steps);

		try {

			int index = 0;
			for (int i = 0; i < localX.getSteps(); ++i) {
				double x = localX.getSteps() ==1 ? localX.getMin() :
						localX.getMin() + ((localX.getMax() - localX.getMin()) * i) / (localX.getSteps()-1);
				// calc.setVarValue(0,x);

				double topRes[];
				topRes = ce.evalTop(new double[] { x });
				if (topRes.length < 3) {
					double oldRes[] = topRes;
					topRes = new double[3];
					for (int k = 0; k < oldRes.length; ++k)
						topRes[k] = oldRes[k];
				}
				// System.out.println("i "+i+" j "+j+" index "+index);
				outCurve.setVertex(index, topRes[0], topRes[1], topRes[2]);
				outCurve.setVertexTexture(index, new PdVector(((double)i)/localX.getSteps(),0 ));
				++index;
			}
			getDefinition().setOption("textureXmin",localX.getMin());
			getDefinition().setOption("textureXmax",localX.getMax());

			showSurf();

		} catch (EvaluationException e) {
			System.out.println(e.toString());
			calc.setGood(false);
			return;
		}

	}

	public void showSurf() throws EvaluationException {
		SphereClip clip = new SphereClip(m_Clipping.getValue());
		clip.operateCurve(outCurve);
		outCurve.showVertices(cbShowVert.getState());
		outCurve.showPolygons(cbShowCurves.getState());
		setColour(outCurve,chCurveColours.getSelectedItem());
		setDisplayProperties(outCurve);
		setGeometryInfo(outCurve);
		store.geomChanged(outCurve);
	}

	@Override
	public void setDisplayProperties() {
		setDisplayProperties(outCurve);
		store.geomApperenceChanged(outCurve);
	}

	@Override
	public boolean update(Object o) {
		if (o == displayVars[0]) {
			variableRangeChanged(0, (PuVariable) o);
			return true;
		} else if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else if (o == m_Clipping)
			return true;
		else
			return super.update(o);
	}

	/**
	 * Handles the selection of a new surface definition.
	 */

	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();
		if (itSel == chDefs) {
			int i = chDefs.getSelectedIndex();
			loadDefinition(lsmpDefs[i]);
		} else
			super.itemStateChanged(e);
	}

	@Override
	public List<PgGeometryIf> getOutputGeoms() {
		return Collections.singletonList((PgGeometryIf) outCurve);
	}

	@Override
	public ProjectComponents getProjectComponents() {
		return new ProjectComponents(this.getName());
	}

	@Override
	public void loadProjectComponents(ProjectComponents comp, PaSingSurf ss) {

	}

}
