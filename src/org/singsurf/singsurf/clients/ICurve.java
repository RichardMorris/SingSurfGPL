/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;

import org.singsurf.singsurf.IntFractometer;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.Parameter;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;
import org.singsurf.singsurf.operators.vectorfields.AbstractIntergralCurve;
import org.singsurf.singsurf.operators.vectorfields.AbstractVectorField;
import org.singsurf.singsurf.operators.vectorfields.EigenVectorField;
import org.singsurf.singsurf.operators.vectorfields.EulerMethodIC;
import org.singsurf.singsurf.operators.vectorfields.HeunMethodIC;
import org.singsurf.singsurf.operators.vectorfields.ImplicitVectorField;
import org.singsurf.singsurf.operators.vectorfields.MidpointMethodIC;
import org.singsurf.singsurf.operators.vectorfields.RC4MethodIC;
import org.singsurf.singsurf.operators.vectorfields.SimpleCalcField;
import org.singsurf.singsurf.operators.vectorfields.UnorientedVectorField;

import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.number.PuDouble;
import jv.project.PgGeometryIf;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class ICurve extends AbstractOperatorClient {
	private static final String MIDPOINT = "Midpoint";
	private static final String EULER = "Euler";
	private static final String HEUN = "Heun";
	private static final String RC4 = "Runge-Kutta 4";

	/** 
	 * 
	 */
	private static final long serialVersionUID = 350L;
//	protected String programName = "Vector field";

	/**
	 * The model loaded by default (defaultsurf.jvx.gz).
	 */
	protected String my_defModelName = "defaultvfield.jvx.gz";

	/** file name for definitions */
	protected String my_defFileName = "defs/vfield.defs";

//	/** The operator which performs the mapping */
//	SimpleField field = null;

	/** clipping control */
	protected PuDouble m_Clipping;

	/** A choice of available inputs */
	protected Choice chOrientation = new Choice();

	protected PuParameter lengthControl;
	
	private Parameter lengthParameter;

	protected boolean doNormilization = false;
	
	protected Choice chMethod = new Choice();
	
//	protected JButton bCalcIC = new JButton("Calc Integral Curve");

	IntFractometer numSteps;

	/********** Constructor *********/

	public ICurve(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == ICurve.class) {
			init(def);
		}
	}

	public void init(Definition def) {
		super.init();
		newParams = new LParamList(this);
		m_Clipping = new PuDouble("Clipping", this);
		m_Clipping.setValue(100.0);
		m_Clipping.setBounds(0.0, 1000.0);

		chOrientation.add("Oriented");
		chOrientation.add("Unoriented");
		chOrientation.add("Eigenvectors");
	  	chOrientation.add("Implicit");		
		chOrientation.addItemListener(this);


		lengthParameter = new Parameter("length", 1.0);
		lengthControl = new PuParameter(this, lengthParameter);

		chMethod.add(EULER);
		chMethod.add(MIDPOINT);
		chMethod.add(HEUN);
		chMethod.add(RC4);
		chMethod.addItemListener(this);
		numSteps = new IntFractometer(10,0);
		numSteps.setParent(this);
				
		this.chCurveColours.select("Red");
		loadDefinition(def);
	}

	@Override
	public void loadDefinition(Definition newdef) {
		Definition def = newdef.duplicate();
		extractDefOptions(def);
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		calc = new Calculator(def, 1);
		calc.build();

		if (!calc.isGood())
			showStatus(calc.getMsg());
		buildFieldOperator();
		setDisplayEquation(def.getEquation());
		refreshParams();

	}

	
	public AbstractVectorField buildFieldOperator() {
		/** The operator which performs the mapping */
		AbstractVectorField field = null;

		String type = this.chOrientation.getSelectedItem();
		if (type.equals("Oriented")) {
			field = new SimpleCalcField(calc.createEvaluator());
			this.doNormilization = false;
		} else if (type.equals("Unoriented")) {
			field = new UnorientedVectorField(calc.createEvaluator());
			this.doNormilization = true;
		} else if (type.equals("Eigenvectors")) {
			field = new EigenVectorField(calc.createEvaluator());
			this.doNormilization = true;
		} else if (type.equals("Implicit")) {
			field = new ImplicitVectorField(calc.createEvaluator());
			this.doNormilization = true;
		}
		return field;
	}

	public void extractDefOptions(Definition def) {
		Option svopt = def.getOption("showVect");
		if (svopt != null)
			this.cbShowVert.setState(svopt.getBoolVal());

		Option colopt = def.getOption("colour");
		if (colopt != null)
			this.chCurveColours.select(colopt.getStringVal());

		Option clipopt = def.getOption("clipping");
		if (clipopt != null)
			this.m_Clipping.setValue(clipopt.getDoubleVal());

		Option orient = def.getOption("orientation");
		if (orient != null)
			this.chOrientation.select(orient.getStringVal());

		Option method = def.getOption("method");
		if (method != null)
			this.chMethod.select(method.getStringVal());

		Option length = def.getOption("length");
		if (length != null)
			this.lengthControl.setVal(length.getDoubleVal());
		
		Option steps = def.getOption("steps");
		if (steps != null)
			this.numSteps.setIntValue(steps.getIntegerVal());
	}

	@Override
	public void setDefinitionOptions(Definition def) {
		def.setOption("colour", this.chCurveColours.getSelectedItem());
		def.setOption("clipping", this.m_Clipping.getValue());
		def.setOption("orientation", this.chOrientation.getSelectedItem());
		def.setOption("length", lengthControl.getVal());
		def.setOption("steps", this.numSteps.getIntValue());
		def.setOption("method", this.chMethod.getSelectedItem());
	}

	
	public PgGeometryIf calcGeomThread(GeomPair p) {
		if (calc == null) {
			showStatus("Null calculator");
			return null;
		}
		if (!calc.isGood()) {
			showStatus(calc.getMsg());
			return null;
		}
		PgGeometryIf input = p.getInput();
		if (input == null) {
			showStatus("Null input");
			return null;
		}
		AbstractVectorField field = buildFieldOperator();
		if (field == null) {
			showStatus("Null field");
			return null;
		}
		AbstractIntergralCurve icurve = buildICurveOperator(field);
		
		PgGeometryIf resultGeom = null;

		System.out.println("icurve before " + ((PgPointSet) input).getNumVertices() + " pts");
		// debugCols((PgElementSet) mappedGeom);
		try {
			field.setLength(this.lengthControl.getVal());

			resultGeom = icurve.operate(input);

//			System.out.println("icurve after map " + resultGeom.get + " pts");
		} catch (UnSuportedGeometryException e) {
			showStatus("Unsupported geometry type ");
			return null;
		} catch (EvaluationException e) {
			System.out.println(e.toString());
			calc.setGood(false);
			return null;
		}
		
		return resultGeom;
	}
	
	public AbstractIntergralCurve buildICurveOperator(AbstractVectorField field) {
		String type = chMethod.getSelectedItem();
		if(type.equals(EULER)) {
			return new EulerMethodIC(field,this.numSteps.getIntValue());
		}
		else if(type.equals(MIDPOINT)) {
			return new MidpointMethodIC(field,this.numSteps.getIntValue());
		}
		else if(type.equals(HEUN)) {
			return new HeunMethodIC(field,this.numSteps.getIntValue());
		}
		else if(type.equals(RC4)) {
			return new RC4MethodIC(field,this.numSteps.getIntValue());
		}
		
		return null;
	}

	@Override
	public void displayGeom(GeomPair p, PgGeometryIf result) {
	
		System.out.println("display icurve");
		// debugCols((PgElementSet) mappedGeom);
		PgGeometryIf out = p.getOutput();
//		PgGeometryIf input = p.getInput();

		setCheckboxesFromGeomety(out);
		GeomStore.copySrcTgt(result, out);

		System.out.println("mapped outer");

		System.out.println("out " + ((PgPointSet) out).getNumVertices());

		setDisplayProperties(out);
		((PgPolygonSet) out).setGlobalPolygonSize(1);
		setGeometryInfo(out,p.getInput());
		store.geomChanged(out);
	}

	
	@Override
	public void newActiveInput(String name) {
		if (activePairs.containsKey(name)) {
			showStatus(name + " is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		PgGeometryIf output = store.aquireCurve(getPreferredOutputName(name), this);
		GeomPair p = new GeomPair(input, output);
		activePairs.put(name, p);
		activeInputNames.add(name);
		activeInputNames.select(activeInputNames.getItemCount()-1);
		setDisplayProperties(p.getOutput());
		setCheckboxesFromGeomety(p.getOutput());
		calcGeom(p);
		store.newPair(this, p);
	}

	@Override
	public boolean update(Object o) {
		if (o == lengthControl || o == numSteps) {
			calcGeoms();
			return true;
		} else if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else if (o == m_Clipping) {
			calcGeoms();
			return true;
		} else
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
		} else if (itSel == this.chOrientation) {
			buildFieldOperator();
		} else
			super.itemStateChanged(e);
	}

	public String makeCGIstring() {
		return null;
	}

	@Override
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
	}

	@Override
	public void setDP(int dp) {
		super.setDP(dp);

		this.lengthControl.setDP(dp);
	}

	@Override
	public void rebuildClient() {
		this.buildFieldOperator();
	}


}
