/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.singsurf.singsurf.Fractometer;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.LmsElementSetMaterial;
import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import org.singsurf.singsurf.operators.SphereClip;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import jv.geom.PgElementSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class Psurf extends AbstractClient {
	private static final long serialVersionUID = 1L;

	protected Fractometer m_Clipping;
	int globalSteps = 40;
	DefVariable localX, localY;
	protected PgElementSet outSurf;

	Fractometer colourMin;
	Fractometer colourMax;

	/**
	 * Constructor when a definition is specified.
	 * @param store
	 * @param def
	 */
	public Psurf(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == Psurf.class) {
			setDisplayEquation(def.getEquation());
			init(def);
		}
	}

	public void init(Definition def) {
		super.init();
		localX = new DefVariable("x", "Normal");
		localY = new DefVariable("y", "Normal");
		displayVars = new PuVariable[] { new PuVariable(this, localX), new PuVariable(this, localY) };

		colourMin = new Fractometer(-1.0);
		colourMax = new Fractometer(1.0);

		newParams = new LParamList(this);
		m_Clipping = new Fractometer(100.0);
		m_Clipping.setMinVal(0.0);
		this.cbShowFace.setState(true);
		this.cbShowEdge.setState(true);
		this.cbShowVert.setState(false);
		this.chSurfColours.select("Colours from XYZ"); 
		
		this.chSurfColours.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				calc.setDerivDepth(getDerivDepthFromColour());
			}});
		loadDefinition(def);
	}

	void checkDef(Definition def) {
		DefVariable var = def.getVar(0);
		if (var.getSteps() == -1)
			var.setBounds(var.getMin(), var.getMax(), globalSteps);
		var = def.getVar(1);
		if (var.getSteps() == -1)
			var.setBounds(var.getMin(), var.getMax(), globalSteps);
	}

	@Override
	public void loadDefinition(Definition newdef) {
		Definition def = newdef.duplicate();
		checkDef(def);

		String lname = this.getName();
		def.setName(lname);
		this.getInfoPanel().setTitle(lname);

		loadFromDefOption(def,"surfColour",chSurfColours);
		calc = new Calculator(def, getDerivDepthFromColour());
		calc.build();
		if (!calc.isGood())
			showStatus(calc.getMsg());
		localX = calc.getDefVariable(0);
		localY = calc.getDefVariable(1);
		setDisplayEquation(def.getEquation());
		displayVars[0].set(localX);
		displayVars[1].set(localY);
		refreshParams();
		outSurf = store.aquireSurface(newdef.getName(), this);

		Option sfopt = def.getOption("showFace");
		if (sfopt != null)
			this.cbShowFace.setState(sfopt.getBoolVal());
		Option seopt = def.getOption("showEdge");
		if (seopt != null)
			this.cbShowEdge.setState(seopt.getBoolVal());
		Option svopt = def.getOption("showVert");
		if (svopt != null)
			this.cbShowVert.setState(svopt.getBoolVal());

		loadFromDefOption(def,"surfColour",chSurfColours);
		loadFromDefOption(def,"colourMinVal",colourMin);
		loadFromDefOption(def,"colourMaxVal",colourMax);
		loadFromDefOption(def,"edgeColour",chCurveColours);
		loadFromDefOption(def,"clipping", m_Clipping);
		calcGeoms();
	}

	private int getDerivDepthFromColour() {
		switch(chSurfColours.getSelectedItem()) {
		case "Gaussian curvature":
		case "Mean curvature":
			return 2;
		default:
			return 1;
		}
	}

	
	protected List<String> getPossibleSurfaceColours() {
		return Arrays.asList("None",
		"Colours from XYZ",
		"Colours from Parameters",
		"Colours from Z",
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
		return Collections.emptyList();
	}

	@Override
	public void setDefinitionOptions(Definition def) {
		def.setOption("showFace", this.cbShowFace.getState());
		def.setOption("showEdge", this.cbShowEdge.getState());
		def.setOption("showVert", this.cbShowVert.getState());
		def.setOption("surfColour",chSurfColours.getSelectedItem());
		def.setOption("colourMinVal",colourMin.getValue());
		def.setOption("colourMaxVal",colourMax.getValue());
		def.setOption("edgeColour",chCurveColours.getSelectedItem());

		def.setOption("clipping", this.m_Clipping.getValue());
	}

	public void variableRangeChanged(int n, PuVariable v) {
		calc.setVarBounds(n, v.getMin(), v.getMax(), v.getSteps());
		if (n == 0)
			localX.setBounds(v.getMin(), v.getMax(), v.getSteps());
		if (n == 1)
			localY.setBounds(v.getMin(), v.getMax(), v.getSteps());
		rebuildResultArray();
		calcGeoms();
	}

	public void rebuildResultArray() {
		outSurf.setNumVertices((localX.getSteps()) * localY.getSteps() );
		outSurf.makeQuadrConn((localX.getSteps()), (localY.getSteps()));
		outSurf.setDimOfElements(-1);
		outSurf.removeElementColors();
		outSurf.removeVertexColors();
		outSurf.showElementColors(false);
		outSurf.showVertexColors(false);
		outSurf.showBoundaries(false);
		outSurf.assureVertexNormals();
		outSurf.assureVertexTextures();
		switch(chSurfColours.getSelectedItem()) {
		case "Colours from Parameters":
		case "Gaussian curvature": 
		case "Mean curvature": 
			outSurf.assureVertexColors();
			break;
		default:
			break;
		}

	}

	@Override
	public void calcGeoms() {
		if (!calc.isGood()) {
			showStatus(calc.getMsg());
			return;
		}
		Evaluator ce = calc.createEvaluator();
		
		// if(m_geom.getNumVertices()!= (localX.getSteps()+1)*(localY.getSteps()+1))
		rebuildResultArray();
		if (outSurf != null)
			face_mat = new LmsElementSetMaterial(outSurf);

		try {
			// System.out.println("Num vertices: "+m_geom.getNumVertices());
			// System.out.println("x steps "+psVars[0].steps);
			int index = 0;
			for (int i = 0; i < localX.getSteps(); ++i) {
				double x = localX.getSteps() > 1 
						? localX.getMin() + ((localX.getMax() - localX.getMin()) * i) / (localX.getSteps()-1)
						: (localX.getMax() - localX.getMin())/2;
				calc.setVarValue(0, x);
				
				double xlambda = localX.getSteps() > 1 
						? ((double) i)/(localX.getSteps() -1)
						: 0.5;
						
				for (int j = 0; j < localY.getSteps(); ++j) {

					double y = localY.getSteps() > 1
						? localY.getMin() + ((localY.getMax() - localY.getMin()) * j) / (localY.getSteps()-1)
						: (localY.getMax() - localY.getMin())/2;
					calc.setVarValue(1, y);

					double ylambda = localY.getSteps() > 1 
							? ((double) j)/(localY.getSteps() - 1)
							: 0.5;

					double topRes[] = ce.evalTop(new double[] { x, y });
					double[] dx = ce.evalDerivative(0);
					PdVector dxVec = new PdVector(dx);
					double[] dy = ce.evalDerivative(1);
					PdVector dyVec = new PdVector(dy);
					PdVector norm = PdVector.crossNew(dxVec, dyVec);
					norm.normalize();
					// if(Math.abs(topRes[0]-5.225) < 0.001 )
//		    	System.out.println("i "+i+" j "+j+" index "+index+ " ["+topRes[0]+" "+topRes[1]+" "+topRes[2]);
					outSurf.setVertex(index, topRes[0], topRes[1], topRes[2]);
					outSurf.setVertexNormal(index, norm);
					outSurf.setVertexTexture(index, new PdVector(xlambda,ylambda));
					switch(chSurfColours.getSelectedItem()) {
					case "Colours from Parameters":
						outSurf.setVertexColor(index,getParameterColour(xlambda,ylambda));
						break;
					case "Gaussian curvature": {
						double[] dxx = ce.evalDerivative(2);
						double[] dxy = ce.evalDerivative(3);
						double[] dyy = ce.evalDerivative(4);
						outSurf.setVertexColor(index,getGaussianColour(dx,dy,dxx,dxy,dyy,norm));
						break;
					}
					case "Mean curvature": {
						double[] dxx = ce.evalDerivative(2);
						double[] dxy = ce.evalDerivative(3);
						double[] dyy = ce.evalDerivative(4);
						outSurf.setVertexColor(index,getMeanColour(dx,dy,dxx,dxy,dyy,norm));
					}
					default:
						break;
					}
					
					++index;
				}
			}
			getDefinition().setOption("textureXmin",localX.getMin());
			getDefinition().setOption("textureXmax",localX.getMax());
			getDefinition().setOption("textureYmin",localY.getMin());
			getDefinition().setOption("textureYmax",localY.getMax());

			showSurf();

		} catch (EvaluationException e) {
			System.out.println(e.toString());
		}

	}

	private Color getMeanColour(double[] dx, double[] dy, double[] dxx, double[] dxy, double[] dyy, PdVector norm) {
		double E = dot(dx,dx);
		double F = dot(dx,dy);
		double G = dot(dy,dy);
		double l = dot(dxx,norm.m_data);
		double m = dot(dxy,norm.m_data);
		double n = dot(dyy,norm.m_data);
//		System.out.printf("E %6.3f F %6.3f G %6.3f l %6.3f m %6.3f  n %6.3f%n",
//				E,F,G,l,m,n);

//		double K = ( l *n - m*m ) / ( E *G - F*F );
		double H = ( G* l + E* n - 2 *F* m ) / ( 2 *E* G - 2 *F*F);

		if(Math.abs(H) <  colourMax.getValue() / 100 ) {
			return Color.green;
		}
		double gval = H > 0 ? 1 - H / colourMax.getValue()
						    : 1 - H / colourMin.getValue();
		float rval = H > 0 ? 1 : 0;
		float bval = H > 0 ? 0 : 1;
		float gclip = gval > 1 ? 1 : gval < 0 ? 0 : (float) gval;

//		System.out.printf("H %6.3g r %6.3f g %6.3f b %6.3f%n", H,rval,gval,bval);
		return new Color(rval,gclip,bval);
	}

	private Color getGaussianColour(double[] dx, double[] dy, double[] dxx, double[] dxy, double[] dyy, PdVector norm) {

		double E = dot(dx,dx);
		double F = dot(dx,dy);
		double G = dot(dy,dy);
		double l = dot(dxx,norm.m_data);
		double m = dot(dxy,norm.m_data);
		double n = dot(dyy,norm.m_data);

		double K = ( l *n - m*m ) / ( E *G - F*F );
//		double H = ( G* l + E* n - 2 *F* m ) / ( 2 *E* G - 2 *F*F);

		if(Math.abs(K) <  colourMax.getValue() / 100 ) {
			return Color.green;
		}

		double gval = K > 0 ? 1 - K / colourMax.getValue()
				: 1 - K / colourMin.getValue();
		float rval = K > 0 ? 1 : 0;
		float bval = K > 0 ? 0 : 1;
		float gclip = gval > 1 ? 1 : gval < 0 ? 0 : (float) gval;

		return new Color(rval,gclip,bval);
	}

	private Color getParameterColour(double xlambda, double ylambda) {
		double x = xlambda - 0.5;
		double y = ylambda - 0.5;
		double ang = Math.atan2(y, x);
		float h = (float)( (ang + Math.PI) / (2 * Math.PI)); 
		float s = 1f;
		float b = 1f;
		return Color.getHSBColor(h, s, b);
	}

	private double dot(double[] u,double[] v) {
		return u[0]*v[0]+u[1]*v[1]+u[2]*v[2];
	}

	public void showSurf() {
		SphereClip clip = new SphereClip(this.m_Clipping.getValue());
		try {
			clip.operate((outSurf));
			
			switch(chSurfColours.getSelectedItem()) {
			case "None":
				break;
			case "Colours from XYZ":
				outSurf.makeElementColorsFromXYZ();
				outSurf.showElementColors(true);
				break;
			case "Colours from Z":
				outSurf.makeElementColorsFromZ();
				outSurf.showElementColors(true);
				break;

			case "Colours from Parameters":
			case "Gaussian curvature":
			case "Mean curvature":
				outSurf.showVertexColors(true);
				outSurf.showElementColorFromVertices(true);					
				outSurf.showElementColors(true);
				break;
			default: {
				Color c = getColor(chSurfColours.getSelectedItem());
				outSurf.setGlobalElementColor(c);
				outSurf.showElementColors(false);
				outSurf.showVertexColors(false);
				break;
			}
			}
			setDisplayProperties(outSurf);
			setGeometryInfo(outSurf);
			store.geomChanged(outSurf);
		} catch (UnSuportedGeometryException e) {
			showStatus("Unknown geometry type");
			return;
		} catch (EvaluationException e) {
			System.out.println(e.toString());
			calc.setGood(false);
			return;
		}
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
		} else if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else if (o == m_Clipping) {
			calcGeoms();
			return true;
		} else
			return super.update(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.singsurf.singsurf.clients.AbstractClient#setDisplayProperties()
	 */
	@Override
	public void setDisplayProperties() {
		setDisplayProperties(outSurf);
		store.geomApperenceChanged(outSurf);
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

//    @Override
//    public String getDefaultDefName() {
//	return this.my_defaultDefName;
//    }

//    @Override
//    public String getDefinitionFileName() {
//	return this.my_defFileName;
//    }

//    @Override
//    public String getProgramName() {
//	return programName;
//    }

	@Override
	public List<PgGeometryIf> getOutputGeoms() {
		return Collections.singletonList((PgGeometryIf) outSurf);
	}

	@Override
	public ProjectComponents getProjectComponents() {
		return new ProjectComponents(this.getName());
	}

	@Override
	public void loadProjectComponents(ProjectComponents comp, PaSingSurf ss) {

	}

}
