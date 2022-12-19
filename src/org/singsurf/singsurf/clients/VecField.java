/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

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
import org.singsurf.singsurf.operators.vectorfields.AbstractVectorField;
import org.singsurf.singsurf.operators.vectorfields.EigenVectorField;
import org.singsurf.singsurf.operators.vectorfields.ImplicitVectorField;
import org.singsurf.singsurf.operators.vectorfields.SimpleCalcField;
import org.singsurf.singsurf.operators.vectorfields.UnorientedVectorField;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgVectorField;
import jv.number.PuDouble;
import jv.project.PgGeometryIf;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class VecField extends AbstractOperatorProject {
	/** 
	 * 
	 */
	private static final long serialVersionUID = 350L;
//	protected String programName = "Vector field";

	/** Checkbox for whether to draw vertices */
	protected Checkbox cbShowVect;

	/** Choice for colours */
	protected Choice chColours2;

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
	
	protected JButton bCalcIC = new JButton("Calc Integral Curve");

	IntFractometer numSteps;

	/********** Constructor *********/

	public VecField(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == VecField.class) {
			init(def);
		}
	}

	public void init(Definition def) {
		super.init();
		newParams = new LParamList(this);
		m_Clipping = new PuDouble("Clipping", this);
		m_Clipping.setValue(100.0);
		m_Clipping.setBounds(0.0, 1000.0);
		cbShowVect = new Checkbox();
		this.cbShowVect.setState(false);

		chOrientation.add("Oriented");
		chOrientation.add("Unoriented");
		chOrientation.add("Eigenvectors");
		chOrientation.add("Implicit");		
		chOrientation.addItemListener(this);

		lengthParameter = new Parameter("length", 1.0);
		this.lengthControl = new PuParameter(this, lengthParameter);

		chMethod.add("Euler");
		chMethod.addItemListener(this);
		numSteps = new IntFractometer(10,0);
		
		chColours2 = new Choice();
		for(String col:getPossibleCurveColours()) {
			chColours2.addItem(col);
		}
		chColours2.addItemListener(this);
		
		this.chCurveColours.select("Red");
		chColours2.select("Blue");
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

		Option colopt2 = def.getOption("colour2");
		if (colopt2 != null)
			this.chColours2.select(colopt2.getStringVal());

		Option clipopt = def.getOption("clipping");
		if (clipopt != null)
			this.m_Clipping.setValue(clipopt.getDoubleVal());

		Option orient = def.getOption("orientation");
		if (orient != null)
			this.chOrientation.select(orient.getStringVal());

		Option length = def.getOption("length");
		if (length != null)
			this.lengthControl.setVal(length.getDoubleVal());
	}

	@Override
	public void setDefinitionOptions(Definition def) {
		def.setOption("showVector", this.cbShowVect.getState());
		def.setOption("colour", this.chCurveColours.getSelectedItem());
		def.setOption("colour2", this.chColours2.getSelectedItem());
		def.setOption("clipping", this.m_Clipping.getValue());
		def.setOption("orientation", this.chOrientation.getSelectedItem());
		def.setOption("length", lengthControl.getVal());
	}

	class CalcVFGeomRunnable implements Runnable {
		GeomPair pair;
		
		public CalcVFGeomRunnable(GeomPair pair) {
			super();
			this.pair = pair;
		}

		@Override
		public void run() {
			PgVectorField[] result = calcVFGeomThread(pair);
						
			DisplayVFGeomRunnable runnable = new DisplayVFGeomRunnable(pair,result);
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		}
	}

	class CalcICGeomRunnable implements Runnable {
		GeomPair pair;
		
		public CalcICGeomRunnable(GeomPair pair) {
			super();
			this.pair = pair;
		}

		@Override
		public void run() {
			calcICGeomThread(pair);
						
			DisplayICGeomRunnable runnable = new DisplayICGeomRunnable(pair);
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		}
	}

	class DisplayVFGeomRunnable implements Runnable {
		GeomPair pair;
		PgVectorField[] resultGeom;

		public DisplayVFGeomRunnable(GeomPair pair, PgVectorField[] resultGeom) {
			super();
			this.pair = pair;
			this.resultGeom = resultGeom;
		}

		@Override
		public void run() {
			displayVFGeom(pair,resultGeom);
		}
	}

	class DisplayICGeomRunnable implements Runnable {
		GeomPair pair;

		public DisplayICGeomRunnable(GeomPair pair) {
			super();
			this.pair = pair;
		}

		@Override
		public void run() {
			displayICGeom(pair);
		}
	}

	
	public PgVectorField[] calcVFGeomThread(GeomPair p) {
		if (calc == null) {
			showStatus("Null calculator");
			return null;
		}
		if (!calc.isGood()) {
			showStatus("calcVFGeomThread calc message "+calc.getMsg());
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
		PgGeometryIf out = p.getOutput();

		GeomStore.copySrcTgt(input, out);

		// debugCols((PgElementSet) input);
		PgVectorField[] vecs;
		System.out.println("vecfield before " + ((PgPointSet) input).getNumVertices() + " pts");
		// debugCols((PgElementSet) mappedGeom);
		try {
			field.setLength(this.lengthControl.getVal());

			vecs = (PgVectorField[]) field.operateAll(out);

			System.out.println("vecfield after map " + vecs[0].getNumVectors() + " pts");
		} catch (UnSuportedGeometryException e) {
			showStatus("Unsupported geometry type ");
			return null;
		} catch (EvaluationException e) {
			System.out.println(e.toString());
			calc.setGood(false);
			return null;
		}
		
		return vecs;
	}
	
	public void displayICGeom(GeomPair pair) {
		// TODO Auto-generated method stub
		
	}

	public void calcICGeomThread(GeomPair pair) {
		// TODO Auto-generated method stub
		
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

		/*

		String type = this.chOrientation.getSelectedItem();
		if (type.equals("Oriented")) {
			field = new SimpleCalcField(calc.createEvaluator());
			this.doNormilization = false;
		} else if (type.equals("Unoriented")) {
			field = new UnorientedVectorField(calc.createEvaluator());
			this.doNormilization = true;
		} else if (type.equals("Major eigenvector")) {
			field = new EigenVectorField(calc.createEvaluator(), true);
			this.doNormilization = true;
		} else if (type.equals("Minor eigenvector")) {
			field = new EigenVectorField(calc.createEvaluator(), false);
			this.doNormilization = true;
		}

 */
		
		if (type.equals("Unoriented")) {
			setColour(vecs[0], this.chCurveColours.getSelectedItem());
			vecs[0].showVectorColors(false);
			setColour(vecs[1], this.chCurveColours.getSelectedItem());
			vecs[1].showVectorColors(false);
			vecs[0].showVectorArrows(false);
			vecs[1].showVectorArrows(false);
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

	/**
	 * Calculate all the needed geoms.
	 */
	@Override
	public void calcGeoms() {
		for (GeomPair p : activePairs.values())
			calcGeom(p);
	}

	public  void calcGeom(GeomPair p) {

		if (!calc.isGood()) {
			showStatus(calc.getMsg());
			return;
		}
		PgGeometryIf input = p.getInput();
		if (input == null) {
			showStatus(getName() +": null input geom");
			return;
		}
		showStatus("Calculating "+getName() +" for "+input.getName());

		Thread t = new Thread(new CalcVFGeomRunnable(p));
		t.start();

	}

	/*
	class CalcVFGeomRunnable implements Runnable {
		GeomPair pair;
		
		public CalcVFGeomRunnable(GeomPair pair) {
			super();
			this.pair = pair;
		}

		@Override
		public void run() {
			lock.lock();
			try {
				System.out.println("CalcGeom run input "+pair.getInput().getName());
				PgGeometryIf result = calcGeomThread(pair);
				System.out.println("CalcGeom run done "+pair.getInput().getName());

				DisplayGeomRunnable runnable = new DisplayGeomRunnable(pair,result);
				try {
					SwingUtilities.invokeAndWait(runnable);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				catch(Exception e) {
					System.out.println(e);
				}
			}
			finally {
				lock.unlock();
			}
		}
	}
*/
	Map<String,GeomPair> activeICurves = new HashMap<>();
	
	@Override
	public void newActiveInput(String name) {
		if (activePairs.containsKey(name)) {
			showStatus(name + " is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		PgGeometryIf output = store.acquireGeometry(getPreferredOutputName(name), input, this);
		GeomPair p = new GeomPair(input, output);
		setCheckboxesFromGeomety(p.getOutput());
		activePairs.put(name, p);
		activeInputNames.add(name);
		setDisplayProperties(p.getOutput());
		calcGeom(p);
		store.newPair(this, p);
	}

	@Override
	public boolean update(Object o) {
		if( o == this.bCalcIC) {
			
			return true;
		} else if (o == this.lengthControl) {
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
	public void geometryDefHasChanged(AbstractProject client, Calculator inCalc) {
	}

	@Override
	public void setDP(int dp) {
		super.setDP(dp);

		this.lengthControl.setDP(dp);
	}

	@Override
	public PgGeometryIf calcGeomThread(GeomPair p) {
		return null;
	}

	@Override
	public void displayGeom(GeomPair p, PgGeometryIf result) {
	}

	@Override
	public void rebuildClient() {
		this.buildFieldOperator();
	}

}
