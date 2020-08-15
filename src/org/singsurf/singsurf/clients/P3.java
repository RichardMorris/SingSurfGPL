package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.singsurf.singsurf.Fractometer;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.PuIntChoice;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.asurf.BoxClevA;
import org.singsurf.singsurf.asurf.BoxClevJavaView;
import org.singsurf.singsurf.asurf.PlotAbstract.PlotMode;
import org.singsurf.singsurf.asurf.Region_info;
import org.singsurf.singsurf.calculators.PolynomialCalculator;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.Parameter;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jep.EquationPolynomialConverter;
import org.singsurf.singsurf.operators.SimpleClip;
import org.singsurf.singsurf.operators.SphereIntersectionClip;
import org.singsurf.singsurf.operators.Split4D;

import org.singsurf.singsurf.jepwrapper.EvaluationException;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.parser.ASTVarNode;
import com.singularsys.jep.parser.Node;

import jv.geom.PgElementSet;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;
import jv.vecmath.PdMatrix;
import jv.vecmath.PdVector;

public class P3 extends AbstractClient {
	private static final long serialVersionUID = 1L;
	
	JButton Bxwp,Bxwm,Bywp,Bywm,Bzwp,Bzwm,Breset;
	protected PgElementSet inXYZ,inXYW,inXZW,inYZW;
	protected PgElementSet outXYZ,outXYW,outXZW,outYZW;
	
	Definition def;
	Option singPower,facePower,edgePower;

	private DefVariable localX;
	private DefVariable localY;
	private DefVariable localZ;
	private DefVariable localW;
	
	
	protected CheckboxGroup cbg_coarse; // Coarse checkbox group
//	protected CheckboxGroup cbg_timeout; // Timeout checkbox group

	protected Checkbox cb_c_4, cb_c_8, cb_c_16, cb_c_32, 
		cb_c_64, cb_c_128, cb_c_256, cb_c_512;
	
	protected Checkbox cb_autoUpdate;
	protected Checkbox cb_dgen;
	protected Checkbox cb_skeleton;
	protected Checkbox cb_adaptiveMesh;
	protected Checkbox cb_refineCurvature;
	protected Checkbox cb_triangulate;

	protected Fractometer m_Clipping;

	protected Checkbox cb_stereographic;

	protected PuIntChoice singPowerFrac;
	protected PuIntChoice facePowerFrac;
	protected PuIntChoice edgePowerFrac;

	Fractometer colourMin;
	Fractometer colourMax;

	
	PdMatrix RR,Rxwp,Rxwm,Rywp,Rywm,Rzwp,Rzwm;
	double angInc = Math.PI / 20; 
	
	private double x0;
	private double y0;
	private double z0;
	private double w0;

	Split4D splitter = new Split4D();
	
	public P3(GeomStore store, Definition def) {
		super(store, def.getName());
		init(def);
	}
	
	public void init(Definition def1) {
		super.init();
		initMatricies();
		localX = def1.getVar(0);
		localY = def1.getVar(1);
		localZ = def1.getVar(2);
		localW = def1.getVar(3);
		displayVars = new PuVariable[] { 
				new PuVariable(this, localX), 
				new PuVariable(this, localY),
				new PuVariable(this, localZ) };
		newParams = new LParamList(this);
		
		singPowerFrac = new PuIntChoice(this,"Sing Power",2, new int[] {1,2,4,8,16,32,64});
		facePowerFrac = new PuIntChoice(this,"Face Power",8, new int[] {1,2,4,8,16,32,64});
		edgePowerFrac = new PuIntChoice(this,"Edge Power",8, new int[] {1,2,4,8,16,32,64});
		
//		this.cbColour.setLabel("Colour by position");

		colourMin = new Fractometer(-1.0);
		colourMax = new Fractometer(1.0);
		this.chSurfColours.select("Colours from XYZ"); 
		
		this.cbShowFace.setState(true);
		this.cbShowEdge.setState(false);
		this.cbShowCurves.setState(false);
		this.cbShowVert.setState(false);
		this.cbShowPoints.setState(false);
		this.cbShowBoundary.setState(true);

		cbg_coarse = new CheckboxGroup();
		cb_c_4 = new Checkbox("4", cbg_coarse, false);
		cb_c_8 = new Checkbox("8", cbg_coarse, false);
		cb_c_16 = new Checkbox("16", cbg_coarse, false);
		cb_c_32 = new Checkbox("32", cbg_coarse, true);
		cb_c_64 = new Checkbox("64", cbg_coarse, false);
		cb_c_128 = new Checkbox("128", cbg_coarse, false);
		cb_c_256 = new Checkbox("256", cbg_coarse, false);
		cb_c_512 = new Checkbox("512", cbg_coarse, false);
		ItemListener cit = new CoarseItemListener();
		cb_c_4.addItemListener(cit);
		cb_c_8.addItemListener(cit);
		cb_c_16.addItemListener(cit);
		cb_c_32.addItemListener(cit);
		cb_c_64.addItemListener(cit);
		cb_c_128.addItemListener(cit);
		cb_c_256.addItemListener(cit);
		cb_c_512.addItemListener(cit);

		cb_adaptiveMesh = new Checkbox("Adaptive mesh",true);
		cb_refineCurvature = new Checkbox("Refine by curvature",false);
		cb_triangulate = new Checkbox("Triangulate",true);

		cb_dgen = new Checkbox("Calc non manifold", false);
		cb_skeleton = new Checkbox("Calc skeleton", false);
		cb_dgen.addItemListener(this);
		cb_skeleton.addItemListener(this);

		cb_stereographic = new Checkbox("Stereographic",true);
		cb_stereographic.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				rotateSliceProject();
			}
			
		});
		m_Clipping = new Fractometer(10.0);
		m_Clipping.setMinVal(0.0);

		this.cb_autoUpdate = new Checkbox("Automatically re-calculate", true);
		this.cb_autoUpdate.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				autoUpdate = cb_autoUpdate.getState();
			}
		});

		cbShowVert.setState(false);

		
		Bxwp = new JButton("Rxw+");
		Bxwp.addActionListener(this);
		Bxwm = new JButton("Rxw-");
		Bxwm.addActionListener(this);
		
		Bywp = new JButton("Ryw+");
		Bywp.addActionListener(this);
		Bywm = new JButton("Ryw-");
		Bywm.addActionListener(this);

		Bzwp = new JButton("Rzw+");
		Bzwp.addActionListener(this);
		Bzwm = new JButton("Rzw-");
		Bzwm.addActionListener(this);

		Breset = new JButton("Reset rotation");
		Breset.addActionListener(this);

		loadDefinition(def1);

	}

	/**
	 * Main method for change in rotation.
	 */
	void rotateSliceProject() {
	
		if (outXYZ == null)
			outXYZ = store.aquireSurface(getModelName()+" XYZ", null);
		if (outXYW == null)
			outXYW = store.aquireSurface(getModelName()+" XYW", null);
		if (outXZW == null)
			outXZW = store.aquireSurface(getModelName()+" XZW", null);
		if (outYZW == null)
			outYZW = store.aquireSurface(getModelName()+" YZW", null);

		System.out.println("\n4D rotation matrix\n");
		System.out.printf("[[%6.3f %6.3f %6.3f %6.3f],%n",RR.getEntry(0, 0),RR.getEntry(0, 1),RR.getEntry(0, 2),RR.getEntry(0, 3) );
		System.out.printf(" [%6.3f %6.3f %6.3f %6.3f],%n",RR.getEntry(1, 0),RR.getEntry(1, 1),RR.getEntry(1, 2),RR.getEntry(1, 3) );
		System.out.printf(" [%6.3f %6.3f %6.3f %6.3f],%n",RR.getEntry(2, 0),RR.getEntry(2, 1),RR.getEntry(2, 2),RR.getEntry(2, 3) );
		System.out.printf(" [%6.3f %6.3f %6.3f %6.3f]]%n",RR.getEntry(3, 0),RR.getEntry(3, 1),RR.getEntry(3, 2),RR.getEntry(3, 3) );
		try { 
		rotateSliceProject(inXYZ,0,outXYZ);
		rotateSliceProject(inXYW,1,outXYW);
		rotateSliceProject(inXZW,2,outXZW);
		rotateSliceProject(inYZW,3,outYZW);	
		} catch(Exception ex) {
			System.out.println(ex);
		}
	}
	
	
	private void rotateSliceProject(PgElementSet in, int which,PgElementSet out) {
		PgElementSet rotated = new PgElementSet(4);
		final int nVert = in.getNumVertices();
		System.out.printf("Input #%d v %d f %d%n", which, in.getNumVertices(),in.getNumElements());
		rotated.setNumVertices(nVert);
		rotated.setNumElements(in.getNumElements());
		for(int i=0;i<nVert;++i) {
			PdVector vec = in.getVertex(i);
			PdVector vec4=null;
			switch(which) {
			case 0:
				vec4 = new PdVector(vec.getEntry(0),vec.getEntry(1),vec.getEntry(2),w0);
				break;
			case 1:
				vec4 = new PdVector(vec.getEntry(0),vec.getEntry(1),z0,vec.getEntry(2));
				break;
			case 2:
				vec4 = new PdVector(vec.getEntry(0),y0,vec.getEntry(1),vec.getEntry(2));
				break;
			case 3:
				vec4 = new PdVector(x0,vec.getEntry(0),vec.getEntry(1),vec.getEntry(2));
				break;
			}
			vec4.leftMultMatrix(RR);
			rotated.setVertex(i, vec4);
		}
		rotated.setElements(in.getElements());
//		rotated.setModelMatrix(RR);
//		rotated.applyModelingMatrix();
		
		System.out.printf("Rotated %d %d%n", rotated.getNumVertices(),rotated.getNumElements());
		try {
			splitter.operateSurface(rotated);
		} catch (EvaluationException e) {
			e.printStackTrace();
		}
		rotated.removeMarkedVertices();
		rotated.removeMarkedElements();
		System.out.printf("Sliced %d %d%n", rotated.getNumVertices(),rotated.getNumElements());
		
		final int rNvert = rotated.getNumVertices();
		out.setNumVertices(rNvert);
		out.setNumElements(rotated.getNumElements());
		
		for(int i=0;i<rNvert;++i) {
			PdVector projected = project(rotated.getVertex(i));
			out.setVertex(i, projected);
		}
		out.setElements(rotated.getElements());
		System.out.printf("Projected %d %d%n", out.getNumVertices(),out.getNumElements());
		
		if(!cb_stereographic.getState()) {
			SphereIntersectionClip clip = new SphereIntersectionClip(3,this.m_Clipping.getValue());
			try {
				clip.operate(out);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}

		out.makeNeighbour();
		this.setDisplayProperties(out,which);
		store.geomChanged(out);
	}

	private void setDisplayProperties(PgElementSet out, int which) {
		switch(which) {
		case 0:
			out.setGlobalElementColor(Color.getHSBColor(0f, 0.666f, 0.6f));
			break;
		case 1:
			out.setGlobalElementColor(Color.getHSBColor(0.25f, 0.666f, 0.6f));
			break;
		case 2:
			out.setGlobalElementColor(Color.getHSBColor(0.5f, 0.666f, 0.6f));
			break;
		case 3:
			out.setGlobalElementColor(Color.getHSBColor(0.75f, 0.666f, 0.6f));
			break;
		}
		setDisplayProperties(out);
	}
	
	private PdVector project(PdVector vertex) {
		double w = vertex.getEntry(3);
		PdVector res = new PdVector(vertex.getEntry(0),vertex.getEntry(1),vertex.getEntry(2));
		double l = res.length();
		if( cb_stereographic.getState() ) {
			if(w>0) {
				res.multScalar(1.0/( l + w));
			} else {
				res.multScalar(-1.0/( l - w));		
			}
		} else {
			res.multScalar(1.0/w);
		}

		return res;
	}


	private void initMatricies() {
		RR = new PdMatrix(4); 
		RR.setIdentity();
		double c = Math.cos(angInc);
		double s = Math.sin(angInc);
		Rxwp = new PdMatrix( new double[][] {{c,0,0, s},{0,1,0,0}, {0,0,1,0},{-s,0,0,c}}); 
		Rxwm = new PdMatrix( new double[][] {{c,0,0,-s},{0,1,0,0}, {0,0,1,0},{s,0,0,c}}); 
		Rywp = new PdMatrix( new double[][] {{1,0,0,0},{0,c,0, s}, {0,0,1,0},{0,-s,0,c}}); 
		Rywm = new PdMatrix( new double[][] {{1,0,0,0},{0,c,0,-s}, {0,0,1,0},{0,s,0,c}}); 
		Rzwp = new PdMatrix( new double[][] {{1,0,0,0},{0,1,0,0}, {0,0,c, s},{0,0,-s,c}}); 
		Rzwm = new PdMatrix( new double[][] {{1,0,0,0},{0,1,0,0}, {0,0,c,-s},{0,0,s,c}}); 
	}

	private void applyIncrementMatrix(PdMatrix mat) {
		RR.leftMult(mat);
	}
	
	class CoarseItemListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			def.setOption("coarse", getCoarse());
		}
		
	}
	 
	public int getCoarse() {
		return Integer.parseInt(cbg_coarse.getSelectedCheckbox().getLabel());
	}

	@Override
	public void setDisplayProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<PgGeometryIf> getOutputGeoms() {
		return Arrays.asList(outXYZ,outXYW,outXZW,outYZW);
	}

	@Override
	public ProjectComponents getProjectComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadProjectComponents(ProjectComponents comp, PaSingSurf ss) {
		// TODO Auto-generated method stub

	}

	@Override
	public void calcGeoms() {
		if (!calc.isGood()) {
			showStatus(calc.getMsg());
			return;
		}

		Thread t = new Thread(new CalcGeomRunnable(),"P3");
		t.start();
	}

	@Override
	public void setDefinitionOptions(Definition def) {
		def.setOption("coarse", this.getCoarse());
		def.removeOption("fine");
		def.removeOption("face");
		def.removeOption("edge");
		def.setOption("singPower", this.singPowerFrac.getVal());
		def.setOption("facePower", this.facePowerFrac.getVal());
		def.setOption("edgePower", this.edgePowerFrac.getVal());
		def.setOption("showFace", this.cbShowFace.getState());
		def.setOption("showEdge", this.cbShowEdge.getState());
		def.setOption("showVert", this.cbShowVert.getState());
		def.setOption("showCurve", this.cbShowCurves.getState());
		def.setOption("showPoint", this.cbShowPoints.getState());
		def.setOption("showBoundary", this.cbShowBoundary.getState());
		def.setOption("calcSkeleton", this.cb_skeleton.getState());
		def.setOption("calcDgen", this.cb_dgen.getState());
		def.setOption("surfColour",chSurfColours.getSelectedItem());
		def.setOption("colourMinVal",colourMin.getValue());
		def.setOption("colourMaxVal",colourMax.getValue());
		def.setOption("edgeColour",chCurveColours.getSelectedItem());
		def.setOption("adaptiveMesh",cb_adaptiveMesh.getState());
		def.setOption("refineByCurvature",cb_refineCurvature.getState());
		def.setOption("triangulate",cb_triangulate.getState());
	}

	@Override
	public void loadDefinition(Definition newdef) {
		// System.out.println(Thread.currentThread());
		System.out.println("Load def " + newdef.getName() + " this" + this.getName());
		def = newdef.duplicate();
		// def.setName(this.getName());
//		this.getInfoPanel().setTitle(def.getName());
		calc = new PolynomialCalculator(def, 0);
		calc.build();
		if (!calc.isGood())
			showStatus(calc.getMsg());
		localX = calc.getDefVariable(0);
		localY = calc.getDefVariable(1);
		localZ = calc.getDefVariable(2);
		localW = calc.getDefVariable(3);
		setDisplayEquation(def.getEquation());
//		displayVars[0].set(localX);
//		displayVars[1].set(localY);
//		displayVars[2].set(localZ);
//		displayVars[3].set(localW);
		refreshParams();
		
		{
			Option copt = def.getOption("coarse");
			if (copt != null)
				setCoarse(copt.getIntegerVal());
		}
		loadFromNumber(calc.getSimpleAssignment("singresmul"),singPowerFrac);
		loadFromNumber(calc.getSimpleAssignment("faceresmul"),edgePowerFrac);
		loadFromNumber(calc.getSimpleAssignment("edgeresmul"),facePowerFrac);
		loadFromNumber(calc.getSimpleAssignment("triangulate"),cb_triangulate);
		loadFromNumber(calc.getSimpleAssignment("knitfacets"),cb_adaptiveMesh);
//		delDefLines(def,new String[] {"singresmul","faceresmul","edgeresmul",
//				"triangulate","knitfacets"});
		
		loadFromDefOption(def,"singPower",singPowerFrac);
		loadFromDefOption(def,"facePower",facePowerFrac);
		loadFromDefOption(def,"edgePower",edgePowerFrac);
		
		setStandardControlsFromOptions();

		setCheckboxStateFromOption(cb_adaptiveMesh,def,"adaptiveMesh");
		setCheckboxStateFromOption(cb_refineCurvature,def,"refineByCurvature");
		setCheckboxStateFromOption(cb_triangulate,def,"triangulate");

		setCheckboxStateFromOption(cb_skeleton,def,"calcSkeleton");
		setCheckboxStateFromOption(cb_dgen,def,"calcDgen");

		loadFromDefOption(def,"surfColour",chSurfColours);
		loadFromDefOption(def,"colourMinVal",colourMin);
		loadFromDefOption(def,"colourMaxVal",colourMax);
		loadFromDefOption(def,"edgeColour",chCurveColours);
		
		this.m_name = newdef.getName();
		store.showStatus("Loaded " + m_name);
		this.getInfoPanel().repaint();
//		outSurf = store.aquireSurface(newdef.getName(), this);
		// outCurve=store.aquireCurve(newdef.getName()+" lines",this);
		// outPoints=store.aquirePoints(newdef.getName()+" points",this);
		calcGeoms();
	}

	
	private void setCoarse(int integerVal) {
		
	}

	@Override
	public Definition createDefaultDef() {
		// TODO Auto-generated method stub
		return null;
	}

	Lock lock = new ReentrantLock();

	class CalcGeomRunnable implements Runnable {

		private int coarse;
		private int fine;
		private int face;
		private int edge;

		@Override
		public void run() {
//			lock.lock();
			store.showStatus("Calculating geometry \"" + getModelName() + "\"");
			inXYZ = new PgElementSet(3);
			inXYW = new PgElementSet(3);
			inXZW = new PgElementSet(3);
			inYZW = new PgElementSet(3);
			
			x0 = 1.10;
			y0 = 1.05;
			z0 = 0.95;
			w0 = 0.90;
			
			EquationPolynomialConverter ec = new EquationPolynomialConverter(calc.getJep(), calc.getField());
			try {
				final List<Parameter> paramsXYZ = new ArrayList<>(calc.getParams());
				paramsXYZ.add(new Parameter(localW.getName(),w0));
				double[][][] coeffsXYZ = ec.convert3D(calc.getPreprocessedEqns(),
						new String[] { localX.getName(), localY.getName(), localZ.getName() }, paramsXYZ);
				
				final List<Parameter> paramsXYW = new ArrayList<>(calc.getParams());
				paramsXYW.add(new Parameter(localZ.getName(),z0));
				double[][][] coeffsXYW = ec.convert3D(calc.getPreprocessedEqns(),
						new String[] { localX.getName(), localY.getName(), localW.getName() }, paramsXYW);
				
				final List<Parameter> paramsXZW = new ArrayList<>(calc.getParams());
				paramsXZW.add(new Parameter(localY.getName(),y0));
				double[][][] coeffsXZW = ec.convert3D(calc.getPreprocessedEqns(),
						new String[] { localX.getName(), localZ.getName(), localW.getName() }, paramsXZW);

				final List<Parameter> paramsYZW = new ArrayList<>(calc.getParams());
				paramsYZW.add(new Parameter(localX.getName(),x0));
				double[][][] coeffsYZW = ec.convert3D(calc.getPreprocessedEqns(),
						new String[] { localY.getName(), localZ.getName(), localW.getName() }, paramsYZW);
				
				PlotMode plotMode;
				if (cb_skeleton.getState()) {
					plotMode = PlotMode.Skeleton;
				} else if (cb_dgen.getState()) {
					plotMode = PlotMode.Degenerate;
				} else {
					plotMode = PlotMode.JustSurface;
				}

				BoxClevA boxclevXYZ = new BoxClevJavaView(inXYZ, null, null, plotMode, getDefinition().toString());
				setBoxclevOptions(boxclevXYZ);
				BoxClevA boxclevXYW = new BoxClevJavaView(inXYW, null, null, plotMode, getDefinition().toString());
				setBoxclevOptions(boxclevXYW);
				BoxClevA boxclevXZW = new BoxClevJavaView(inXZW, null, null, plotMode, getDefinition().toString());
				setBoxclevOptions(boxclevXZW);
				BoxClevA boxclevYZW = new BoxClevJavaView(inYZW, null, null, plotMode, getDefinition().toString());
				setBoxclevOptions(boxclevYZW);
				
//				Region_info regionXYZ = new Region_info(-x0-.001, x0, -y0-0.001, y0, -z0-0.001, z0);
//				Region_info regionXYW = new Region_info(-x0-.001, x0, -y0-0.001, y0, -w0-0.001, w0);
//				Region_info regionXZW = new Region_info(-x0-.001, x0, -z0-0.001, z0, -w0-0.001, w0);
//				Region_info regionYZW = new Region_info(-y0-.001, y0, -z0-0.001, z0, -w0-0.001, w0);

				// lambda = (m-l)/(h-l)
				// lambda*(h-l) = m-l)
				// lambda*h - lambda*l = m - l
				// lambda h = m - l + lambda * l
				// h = (m - (1-lambda) l)/lambda
				// m = x0, l = -x0, lambda = 7/8 = 0.75 + 0.125 = 0.895
				// h = (x0 - 1/8 * -x0) / (7/8)
				// h = (8 x0 + x0) / 7
				// h = 9/7 x0
				// h = 9/7, l = -1
				
				
				Region_info regionXYZ = new Region_info(-x0, 9/7 * x0, -y0, 9/7*y0, -z0, 9/7*z0);
				Region_info regionXYW = new Region_info(-x0, 9/7 * x0, -y0, 9/7*y0, -w0, 9/7*w0);
				Region_info regionXZW = new Region_info(-x0, 9/7 * x0, -z0, 9/7*z0, -w0, 9/7*w0);
				Region_info regionYZW = new Region_info(-y0, 9/7 * y0, -z0, 9/7*z0, -w0, 9/7*w0);

				
				boxclevXYZ.marmain(coeffsXYZ, regionXYZ, coarse, fine, face, edge);
				store.showStatus("Geometry \"" + getModelName() + "\" sucessfully calculated. χ = "+boxclevXYZ.getEuler()+", "+boxclevXYZ.getNumSings()+" singularities");
				Pruner p1 = new Pruner(x0,y0,z0);
				p1.operate(inXYZ);

				boxclevXYW.marmain(coeffsXYW, regionXYW, coarse, fine, face, edge);
				store.showStatus("Geometry \"" + getModelName() + "\" sucessfully calculated. χ = "+boxclevXYW.getEuler()+", "+boxclevXYW.getNumSings()+" singularities");
				Pruner p2 = new Pruner(x0,y0,w0);
				p2.operate(inXYW);

				boxclevXZW.marmain(coeffsXZW, regionXZW, coarse, fine, face, edge);
				store.showStatus("Geometry \"" + getModelName() + "\" sucessfully calculated. χ = "+boxclevXZW.getEuler()+", "+boxclevXZW.getNumSings()+" singularities");
				Pruner p3 = new Pruner(x0,z0,w0);
				p3.operate(inXZW);

				boxclevYZW.marmain(coeffsYZW, regionYZW, coarse, fine, face, edge);
				store.showStatus("Geometry \"" + getModelName() + "\" sucessfully calculated. χ = "+boxclevYZW.getEuler()+", "+boxclevYZW.getNumSings()+" singularities");
				Pruner p4 = new Pruner(y0,z0,w0);
				p4.operate(inYZW);				
				
			} catch (AsurfException e) {
				store.showStatus("Error calculating geometry");
				PsDebug.error(e.getMessage());
				e.printStackTrace();
			} catch (ParseException e) {
				store.showStatus("Error parsing expression");
				PsDebug.error(e.getMessage());
			} catch (Exception e) {
				store.showStatus(e.toString());
				PsDebug.error(e.toString());
				e.printStackTrace();
			}

			DisplayGeomRunnable runnable = new DisplayGeomRunnable();
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		/**
		 * @param boxclev
		 */
		public void setBoxclevOptions(BoxClevA boxclev) {
			boxclev.global_selx = -1;
			boxclev.global_sely = -1;
			boxclev.global_selz = -1;

			int singmul = singPowerFrac.getVal();
			int facemul = facePowerFrac.getVal();
			int edgemul = edgePowerFrac.getVal();
			
			boxclev.setTriangulate(cb_triangulate.getState()?1:0);

			for (Node n : calc.getRawEqns()) {
				System.out.println(";");
				
				if (n.jjtGetNumChildren() == 0)
					continue;
				if (!(n.jjtGetChild(0) instanceof ASTVarNode))
					continue;
				
				useIntSetting(n,"selx", i -> boxclev.setGlobal_selx(i));
				useIntSetting(n,"sely", i -> boxclev.setGlobal_sely(i));
				useIntSetting(n,"selz", i -> boxclev.setGlobal_selz(i));
				useIntSetting(n,"seld", i -> boxclev.setGlobal_denom(i));

				useIntSetting(n, "triangulate", i-> boxclev.setTriangulate(i));
				useIntSetting(n, "littlefacet", i-> boxclev.setLittleFacets(i!=0));
				useIntSetting(n, "cleanmesh", i-> boxclev.setCleanmesh(i));
				useIntSetting(n, "tagbad", i-> boxclev.setTagbad(i));
				useIntSetting(n, "tagsing", i-> boxclev.setTagSing(i));
				useIntSetting(n, "blowup", i-> boxclev.setBlowup(i));

				useDoubleSetting(n,"convtol",d -> boxclev.setConvtol(d));
				useDoubleSetting(n,"normlenlevel1",d -> boxclev.setNormlenlevel1(d));
				useDoubleSetting(n,"normlenlevel2",d -> boxclev.setNormlenlevel2(d));
				useDoubleSetting(n,"normlenlevel3",d -> boxclev.setNormlenlevel3(d));
				useDoubleSetting(n,"normlenlevel4",d -> boxclev.setNormlenlevel4(d));

				singmul = getIntSetting(n, "singresmul",singmul);
				facemul = getIntSetting(n, "faceresmul",facemul);
				edgemul = getIntSetting(n, "edgeresmul",edgemul);
			}
			
			String ctype = chSurfColours.getSelectedItem();
			switch(ctype) {
			case "Gaussian curvature":
				boxclev.setColortype(BoxClevA.COLOUR_GAUSSIAN_CURVATURE);
				boxclev.setColourMin((float) colourMin.getValue());
				boxclev.setColourMax((float) colourMax.getValue());
				break;
			case "Mean curvature":
				boxclev.setColortype(BoxClevA.COLOUR_MEAN_CURVATURE);
				boxclev.setColourMin((float) colourMin.getValue());
				boxclev.setColourMax((float) colourMax.getValue());
				break;
			default:
				boxclev.setColortype(0);
				break;					
			}
			
			boxclev.setKnitFacets(cb_adaptiveMesh.getState());
			boxclev.setRefineCurvature(cb_refineCurvature.getState());
			boxclev.setCurvatureLevel1(colourMax.getValue()/4);
			boxclev.setCurvatureLevel2(colourMax.getValue()/2);
			boxclev.setCurvatureLevel3(colourMax.getValue());
			boxclev.setCurvatureLevel4(colourMax.getValue()*2);

			
			coarse = getCoarse();
			fine = coarse * singmul;
			face = fine * facemul;
			edge = face * edgemul;
		}

	}


	static class Pruner extends SimpleClip {
		double x,y,z;
		
		
		public Pruner(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}


		@Override
		public boolean testClip(PdVector vec) throws EvaluationException {
			
			return (vec.getEntry(0)<=x) && (vec.getEntry(1)<=y) && (vec.getEntry(2)<=z);
		}
		
	}

	class DisplayGeomRunnable implements Runnable {
		public DisplayGeomRunnable() {
		}

		@Override
		public void run() {
			rotateSliceProject();
		}
	}

	public String getModelName() {
		return this.m_name;
	}


	@Override
	public void actionPerformed(ActionEvent event) {
		Object src = event.getSource();
		if(Bxwp.equals(src)) {
			this.applyIncrementMatrix(Rxwp);
			this.rotateSliceProject();
		} else if(Bxwm.equals(src)) {
			this.applyIncrementMatrix(Rxwm);
			this.rotateSliceProject();
		} else if(Bywp.equals(src)) {
			this.applyIncrementMatrix(Rywp);
			this.rotateSliceProject();
		} else if(Bywm.equals(src)) {
			this.applyIncrementMatrix(Rywm);
			this.rotateSliceProject();
		} else if(Bzwp.equals(src)) {
			this.applyIncrementMatrix(Rzwp);
			this.rotateSliceProject();
		} else if(Bzwm.equals(src)) {
			this.applyIncrementMatrix(Rzwm);
			this.rotateSliceProject();
		} else if(Breset.equals(src)) {
			this.resetIncrementMatrix();
			this.rotateSliceProject();
		} else {
			super.actionPerformed(event);
		}
	}

	private void resetIncrementMatrix() {
		RR.setIdentity();
	}

	@Override
	public boolean update(Object o) {
		if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else if (o instanceof PuIntChoice) {
			this.setDefinitionOptions(getDefinition());
			return true;
		} else
			return super.update(o);
	}

}
