/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingUtilities;

import org.lsmp.djep.xjep.PrintVisitor;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.Fractometer;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.LmsElementSetMaterial;
import org.singsurf.singsurf.LmsPointSetMaterial;
import org.singsurf.singsurf.LmsPolygonSetMaterial;
import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.PuIntChoice;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.asurf.BoxClevA;
import org.singsurf.singsurf.asurf.BoxClevJavaView;
import org.singsurf.singsurf.asurf.PlotAbstract.PlotMode;
import org.singsurf.singsurf.calculators.PolynomialCalculator;
import org.singsurf.singsurf.asurf.Region_info;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jep.EquationPolynomialConverter;
import org.singsurf.singsurf.operators.SimpleClip;

import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class ASurf extends AbstractClient {
	private static final long serialVersionUID = 1L;

	Definition def;
	DefVariable localX, localY, localZ;
	Option singPower,facePower,edgePower;
	
	protected PgElementSet outSurf;
	protected PgPolygonSet outCurve;
	protected PgPointSet outPoints;	
	
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
		
	protected PuIntChoice singPowerFrac;
	protected PuIntChoice facePowerFrac;
	protected PuIntChoice edgePowerFrac;

	Fractometer colourMin;
	Fractometer colourMax;
	
	public ASurf(GeomStore store, String name, String defFile) {
		super(store, name);
		try {
			java.util.List<Definition> defs = store.loadDefs(defFile).getDefs();
			this.lsmpDefs = new Definition[defs.size()];
			this.lsmpDefs = defs.toArray(this.lsmpDefs);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (getClass() == ASurf.class) {
			init(this.createDefaultDef());
		}
	}

	public ASurf(GeomStore store, String name) {
		super(store, name);
		if (getClass() == ASurf.class) {
			Definition def = this.createDefaultDef();
			def.setName(name);
			init(def);
		}
	}

	public ASurf(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == ASurf.class) {
			init(def);
		}
	}

	public ASurf(GeomStore store, String name, String defFile, Definition initialDef) {
		super(store, name);
		try {
			java.util.List<Definition> defs = store.loadDefs(defFile).getDefs();
			this.lsmpDefs = new Definition[defs.size()];
			this.lsmpDefs = defs.toArray(this.lsmpDefs);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (getClass() == ASurf.class) {
			init(initialDef);
		}
	}

	public ASurf(GeomStore store, String name, String defFile, String model) {
		super(store, model);
		try {
			java.util.List<Definition> defs = store.loadDefs(defFile).getDefs();
			this.lsmpDefs = new Definition[defs.size()];
			this.lsmpDefs = defs.toArray(this.lsmpDefs);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (getClass() == ASurf.class) {
			Definition initialDef = getDef(model);
			init(initialDef);
		}
	}

	protected List<String> getPossibleSurfaceColours() {
		return Arrays.asList("None",
		"Colours from XYZ",
		"Gaussian curvature",
		"Mean curvature",
		"Red",
		"Green",
		"Blue",
		"Cyan",
		"Magenta",
		"Yellow",
		"Black",
		"Grey",
		"White");
	}

	protected List<String> getPossibleCurveColours() {
		return Arrays.asList("None",
		"Red",
		"Green",
		"Blue",
		"Cyan",
		"Magenta",
		"Yellow",
		"Black",
		"Grey",
		"White");
	}

	@Override
	public Definition createDefaultDef() {
		Definition def1;
		def1 = new Definition("ASurf", DefType.asurf, "");
		def1.add(new DefVariable("x", -1.03, 1.02));
		def1.add(new DefVariable("y", -1.04, 1.01));
		def1.add(new DefVariable("z", -1.05, 1));
		return def1;
	}

	public void init(Definition def1) {
		super.init();
		localX = def1.getVar(0);
		localY = def1.getVar(1);
		localZ = def1.getVar(2);
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
		
		this.cb_autoUpdate = new Checkbox("Automatically re-calculate", true);
		this.cb_autoUpdate.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				autoUpdate = cb_autoUpdate.getState();
			}
		});

		cbShowVert.setState(false);

		loadDefinition(def1);
	}

	public void setCoarse(int c) {
		if (c == 4)
			cbg_coarse.setSelectedCheckbox(cb_c_4);
		if (c == 8)
			cbg_coarse.setSelectedCheckbox(cb_c_8);
		if (c == 16)
			cbg_coarse.setSelectedCheckbox(cb_c_16);
		if (c == 32)
			cbg_coarse.setSelectedCheckbox(cb_c_32);
		if (c == 64)
			cbg_coarse.setSelectedCheckbox(cb_c_64);
		if (c == 128)
			cbg_coarse.setSelectedCheckbox(cb_c_128);
		if (c == 256)
			cbg_coarse.setSelectedCheckbox(cb_c_256);
		if (c == 512)
			cbg_coarse.setSelectedCheckbox(cb_c_512);
		def.setOption("coarse", c);
	}

	public int getCoarse() {
		return Integer.parseInt(cbg_coarse.getSelectedCheckbox().getLabel());
	}

	void checkDef(Definition def1) {
	}

	@Override
	public void loadDefinition(Definition newdef) {
		// System.out.println(Thread.currentThread());
		System.out.println("Load def " + newdef.getName() + " this" + this.getName());
		def = newdef.duplicate();
		checkDef(def);
		// def.setName(this.getName());
		this.getInfoPanel().setTitle(def.getName());
		calc = new PolynomialCalculator(def, 0);
		calc.build();
		if (!calc.isGood())
			showStatus(calc.getMsg());
		localX = calc.getDefVariable(0);
		localY = calc.getDefVariable(1);
		localZ = calc.getDefVariable(2);
		setDisplayEquation(def.getEquation());
		displayVars[0].set(localX);
		displayVars[1].set(localY);
		displayVars[2].set(localZ);
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
		delDefLines(def,new String[] {"singresmul","faceresmul","edgeresmul",
				"triangulate","knitfacets"});
		
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
		
	
		if (outSurf != null)
			store.removeGeometry(outSurf, false);
		if (outCurve != null)
			store.removeGeometry(outCurve, false);
		if (outPoints != null)
			store.removeGeometry(outPoints, false);

		this.m_name = newdef.getName();
		store.showStatus("Loaded " + m_name);
		this.getInfoPanel().repaint();
		outSurf = store.aquireSurface(newdef.getName(), this);
		// outCurve=store.aquireCurve(newdef.getName()+" lines",this);
		// outPoints=store.aquirePoints(newdef.getName()+" points",this);
		calcGeoms();
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

	private void delDefLines(Definition def2, String[] names) {
		String whole = def2.getEquation();
		String lines[] = whole.split("\n");
		List<String> newLines = new ArrayList<>();
		for(String line:lines) {
			boolean reject=false;
			for(String name:names) {
				if(line.contains(name))
					reject = true;
			}
			if(!reject)
				newLines.add(line);
		}
		String joined = String.join("\n", newLines);
		def.setEquation(joined);
		this.taDef.setText(joined);
	}

	public void variableRangeChanged(int n, PuVariable v) {
		calc.setVarBounds(n, v.getMin(), v.getMax(), v.getSteps());
		if (n == 0)
			localX.setBounds(v.getMin(), v.getMax(), v.getSteps());
		if (n == 1)
			localY.setBounds(v.getMin(), v.getMax(), v.getSteps());
		if (n == 2)
			localZ.setBounds(v.getMin(), v.getMax(), v.getSteps());
		if (autoUpdate) {
			calcGeoms();
		}
	}

	@Override
	public void calcGeoms() {
		if (!calc.isGood()) {
			showStatus(calc.getMsg());
			return;
		}
		if (outSurf != null)
			face_mat = new LmsElementSetMaterial(outSurf);
		if (outCurve != null)
			line_mat = new LmsPolygonSetMaterial(outCurve);
		if (outPoints != null)
			point_mat = new LmsPointSetMaterial(outPoints);

		Thread t = new Thread(new CalcGeomRunnable(),"ASurf");
		t.start();
	}

	String getModelName() {
		return m_name;
	}

	boolean trimmed_domain=false;
	Lock lock = new ReentrantLock();

	class CalcGeomRunnable implements Runnable {

		@Override
		public void run() {
//			lock.lock();
			store.showStatus("Calculating geometry \"" + getModelName() + "\"");
			PgElementSet surfRes = new PgElementSet(3);
			PgPolygonSet curveRes = new PgPolygonSet(3);
			PgPointSet pointsRes = new PgPointSet(3);
			
			EquationPolynomialConverter ec = new EquationPolynomialConverter(calc.getJep(), calc.getField());
			try {
				double[][][] coeffs = ec.convert3D(calc.getPreprocessedEqns(),
						new String[] { localX.getName(), localY.getName(), localZ.getName() }, calc.getParams());
				if (coeffs.length == 1 && coeffs[0].length == 1 && coeffs[0][0].length == 1)
					throw new AsurfException("Equation is a constant");
				
				PlotMode plotMode;
				if (cb_skeleton.getState()) {
					plotMode = PlotMode.Skeleton;
				} else if (cb_dgen.getState()) {
					plotMode = PlotMode.Degenerate;
				} else {
					plotMode = PlotMode.JustSurface;
				}

				BoxClevA boxclev = new BoxClevJavaView(surfRes, curveRes, pointsRes, plotMode,
						getDefinition().toString());
				boxclev.global_selx = -1;
				boxclev.global_sely = -1;
				boxclev.global_selz = -1;

				int singmul = singPowerFrac.getVal();
				int facemul = facePowerFrac.getVal();
				int edgemul = edgePowerFrac.getVal();
				
				PrintVisitor pv = calc.getJep().getPrintVisitor();
				calc.getJep().getOperatorTable().getMultiply().setPrintSymbol(null);
				DecimalFormat format = new DecimalFormat();
				format.setMaximumFractionDigits(12);
				format.setMinimumFractionDigits(0);
				format.setGroupingUsed(false);
				pv.setNumberFormat(format);
				pv.setMaxLen(80);

				boxclev.setTriangulate(cb_triangulate.getState()?1:0);

				trimmed_domain=false;
				for (Node n : calc.getRawEqns()) {
					pv.print(n);
					System.out.println(";");
					
					if (n.jjtGetNumChildren() == 0)
						continue;
					if (!(n.jjtGetChild(0) instanceof ASTVarNode))
						continue;
					
					useIntSetting(n,"selx", i -> boxclev.setGlobal_selx(i));
					useIntSetting(n,"sely", i -> boxclev.setGlobal_sely(i));
					useIntSetting(n,"selz", i -> boxclev.setGlobal_selz(i));
					useIntSetting(n,"seld", i -> boxclev.setGlobal_denom(i));

					useIntSetting(n,"trimmed_domain", i -> trimmed_domain = (i!=0));

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

//				Node subst = ec.subAll(calc.getPreprocessedEqns());
//				System.out.print("Substituted form ");
//				pv.println(subst);
//				Node expanded = ec.subAllExpand(calc.getPreprocessedEqns());
//				System.out.print("Expanded form ");
//				pv.println(expanded);
//				Node reparsed = calc.getJep().parse(pv.toString(expanded));
//				Node cleaned = calc.getJep().clean(reparsed);
//				System.out.print("Rounded form  ");
//				pv.println(cleaned);
				
				int coarse = getCoarse();
				int fine = coarse * singmul;
				int face = fine * facemul;
				int edge = face * edgemul;

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
//				useIntSetting(n, "knitfacets", i-> boxclev.setKnitFacets(i!=0));
//				useIntSetting(n, "refinecurvature", i-> boxclev.setRefineCurvature(i!=0));
//				useDoubleSetting(n,"curvature1", d -> boxclev.setCurvatureLevel1(d));
//				useDoubleSetting(n,"curvature2", d -> boxclev.setCurvatureLevel2(d));
//				useDoubleSetting(n,"curvature3", d -> boxclev.setCurvatureLevel3(d));
//				useDoubleSetting(n,"curvature4", d -> boxclev.setCurvatureLevel4(d));

				double xl= localX.getMin();
				double xh= localX.getMax();
				double yl= localY.getMin();
				double yh= localY.getMax();
				double zl= localZ.getMin();
				double zh= localZ.getMax();
				
				Region_info region;
				if(!trimmed_domain) {
					region = new Region_info(xl,xh,yl,yh,zl,zh);
				} else {
					                            // x0 + 8(x0+x0)/7
					region = new Region_info(xl, (8*xh-xl)/7, yl, (8*yh-yl)/7, zl, (8*zh-zl)/7 );		
				}
				
//				lock.unlock();
				boxclev.marmain(coeffs, region, coarse, fine, face, edge);
				
				if(trimmed_domain) {
					Pruner pruner = new Pruner(xh,yh,zh);
					pruner.operate(surfRes);
				}
				store.showStatus("Geometry \"" + getModelName() + "\" sucessfully calculated. χ = "+boxclev.getEuler()+", "+boxclev.getNumSings()+" singularities");

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

			DisplayGeomRunnable runnable = new DisplayGeomRunnable(surfRes, curveRes, pointsRes);
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
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

	@Override
	public void setDisplayProperties() {
		if (outSurf != null) {
			setDisplayProperties(outSurf);
			store.geomApperenceChanged(outSurf);
		}
		if (outCurve != null) {
			setCurveDisplayProperties(outCurve);
			store.geomApperenceChanged(outCurve);
		}
		if (outPoints != null) {
			setDisplayProperties(outPoints);
			store.geomApperenceChanged(outPoints);
		}
	}

	public void setCurveDisplayProperties(PgGeometryIf geom) {
		if (geom == null)
			return;
		if (geom instanceof PgPolygonSet) {
			((PgPolygonSet) geom).showPolygons(cbShowCurves.getState());
			((PgPolygonSet) geom).showVertices(cbShowVert.getState());
		}
		store.geomApperenceChanged(geom);
	}

	@Override
	public boolean update(Object o) {
		if (o == displayVars[0]) {
			variableRangeChanged(0, (PuVariable) o);
			return true;
		}
		if (o == displayVars[1]) {
			variableRangeChanged(1, (PuVariable) o);
			return true;
		}
		if (o == displayVars[2]) {
			variableRangeChanged(2, (PuVariable) o);
			return true;
		} else if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else if (o instanceof PuIntChoice) {
			this.setDefinitionOptions(getDefinition());
			return true;
		} else
			return super.update(o);
	}

	class DisplayGeomRunnable implements Runnable {
		PgElementSet surfRes;
		PgPolygonSet curveRes;
		PgPointSet pointsRes;

		/**
		 * @param surfRes
		 * @param curveRes
		 * @param pointsRes
		 */
		public DisplayGeomRunnable(PgElementSet surfRes, PgPolygonSet curveRes, PgPointSet pointsRes) {
			this.surfRes = surfRes;
			this.curveRes = curveRes;
			this.pointsRes = pointsRes;
		}

		@Override
		public void run() {

			if (outSurf == null)
				outSurf = store.aquireSurface(getModelName(), null);
			GeomStore.copySrcTgt(surfRes, outSurf);

			try {
//				int coltype = chSurfColours.getSelectedIndex();
				String colName = chSurfColours.getSelectedItem();
				
				switch(colName) {
				case "Colours from XYZ": {
					outSurf.makeElementColorsFromXYZ();
					outSurf.showElementColors(true);					
					break;
				}
				case "Gaussian curvature":
				case "Mean curvature":
					outSurf.showVertexColors(true);
					outSurf.showElementColorFromVertices(true);					
					outSurf.showElementColors(true);
					break;
				default: {
					Color c = getColor(colName);
					outSurf.setGlobalElementColor(c);
					outSurf.showElementColors(false);
					outSurf.showVertexColors(false);
					break;
				}
				}
				setEdgeColour(outSurf, chCurveColours.getSelectedItem());

			} catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
			}
			setDisplayProperties(outSurf);
			outSurf.showBoundaries(cbShowBoundary.getState());
//			outSurf.setCreaseAngle(0);
//			outSurf.setState(PvGeometryIf.SMOOTH_ELEMENT_COLORS , false);
			setGeometryInfo(outSurf);
			store.geomChanged(outSurf);

			boolean hasCurves = curveRes.getNumVertices() > 0;
			if (hasCurves) {
				if (outCurve == null)
					outCurve = store.aquireCurve(getModelName() + " lines", null);
				GeomStore.copySrcTgt(curveRes, outCurve);
				outCurve.showVertices(cbShowVert.getState());
				outCurve.showPolygons(cbShowCurves.getState());
				setDisplayProperties(outCurve);
				outCurve.showPolygonColors(true);
				setGeometryInfo(outCurve);
				store.geomChanged(outCurve);
			} else {
				if (outCurve != null) {
					store.removeGeometry(outCurve);
					outCurve = null;
				}
			}

			boolean hasPoints = pointsRes.getNumVertices() > 0;
			if (hasPoints) {
				if (outPoints == null)
					outPoints = store.aquirePoints(getModelName() + " points", null);
				GeomStore.copySrcTgt(pointsRes, outPoints);
				outPoints.showVertices(cbShowPoints.getState());
				setGeometryInfo(outPoints);
				store.geomChanged(outPoints);
			} else {
				if (outPoints != null) {
					store.removeGeometry(outPoints);
					outPoints = null;
				}
			}
		}
	}

	class CoarseItemListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			def.setOption("coarse", getCoarse());
		}
		
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

	public void setEdgeColour(PgElementSet surf, String colName) {
		surf.setGlobalEdgeColor(getColor(colName));
	}

	@Override
	public List<PgGeometryIf> getOutputGeoms() {
		List<PgGeometryIf> list = new ArrayList<>();
		list.add(outSurf);
		if (outCurve != null)
			list.add(outCurve);
		if (outPoints != null)
			list.add(outPoints);
		return list;
	}

	@Override
	public boolean loadDefByName(String s) {
		Definition ldef = getDef(s);
		if (ldef == null)
			return false;
		loadDefinition(ldef);
		return true;
	}

	@Override
	public ProjectComponents getProjectComponents() {
		return new ProjectComponents(this.getName());
	}

	@Override
	public void loadProjectComponents(ProjectComponents comp, PaSingSurf ss) {

	}

}
