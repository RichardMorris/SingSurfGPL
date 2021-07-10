/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;

import org.singsurf.singsurf.Fractometer;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.geometries.ElementSetMaterial;
import org.singsurf.singsurf.geometries.PointSetMaterial;
import org.singsurf.singsurf.geometries.PolygonSetMaterial;
import org.singsurf.singsurf.jepwrapper.EvaluationException;
import org.singsurf.singsurf.operators.ContinuityClip;
import org.singsurf.singsurf.operators.SimpleCalcMap;
import org.singsurf.singsurf.operators.SimpleMap;
import org.singsurf.singsurf.operators.SphereClip;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.project.PgGeometryIf;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class Mapping extends AbstractOperatorClient {
	/** 
	 * 
	 */
	private static final long serialVersionUID = -4659133739866394363L;

	protected Fractometer m_Clipping;
	protected Fractometer m_ContDist;

	Checkbox cbParamsFromTexture = new Checkbox("Parameters from texture",false);

	/********** Constructor *********/

	public Mapping(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == Mapping.class) {
			init(def);
		}
	}

	public void init(Definition def) {
		super.init();
		newParams = new LParamList(this);
		m_Clipping = new Fractometer(100.0);
		m_Clipping.setMinVal(0.0);
		m_ContDist = new Fractometer(100.0);
		m_ContDist.setMinVal(0.0);
		this.cbShowFace.setState(true);
		this.cbShowEdge.setState(false);
		this.cbShowVert.setState(false);
		this.chCurveColours.addItemListener(this);
		loadDefinition(def);
	}

	@Override
	public void loadDefinition(Definition newdef) {
		Definition def = newdef.duplicate();
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		calc = new Calculator(def, 1);
		calc.build();

		if (!calc.isGood())
			showStatus(calc.getMsg());
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
		loadFromDefOption(def,"surfColour",chSurfColours);
		loadFromDefOption(def,"curveColour",chCurveColours);

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
		def.setOption("surfColour", this.chSurfColours.getSelectedItem());
		def.setOption("curveColour", this.chCurveColours.getSelectedItem());

		def.setOption("clipping", this.m_Clipping.getValue());
		def.setOption("continuity", this.m_ContDist.getValue());

	}

	
	
	protected List<String> getPossibleSurfaceColours() {
		return Arrays.asList("Unchanged",
		"Colours from XYZ",
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
		return Arrays.asList("Unchanged",
		"Colours from XYZ",
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
	public PgGeometryIf calcGeomThread(GeomPair p) {
		if (calc != null && !calc.isGood()) {
			showStatus(calc.getMsg());
			return null;
		}
		PgGeometryIf input = p.getInput();

		SimpleMap map = new SimpleCalcMap(calc.createEvaluator(),calc.getDerivDepth()>0);
		map.setParamsFromTexture(cbParamsFromTexture.getState());

		if (input == null) {
			showStatus("Null input");
			return null;
		}
		// debugCols((PgElementSet) input);
		PgPointSet mappedGeom = (PgPointSet) input.clone();
		System.out.println("mapped before " + mappedGeom.getNumVertices() + " pts");
		if(mappedGeom instanceof PgElementSet) {
			System.out.println("\t " + ((PgElementSet)mappedGeom).getNumElements() + " faces");
		}
		try {
			map.operate(mappedGeom);
			System.out.println("mapped after map " + mappedGeom.getNumVertices() + " pts");
			if(mappedGeom instanceof PgElementSet) {
				System.out.println("\t " + ((PgElementSet)mappedGeom).getNumElements() + " faces");
			}
//		for(int i=0;i<mappedGeom.getNumVertices();++i) {
//			PdVector vec = mappedGeom.getVertex(i);
//			System.out.println(Arrays.toString(vec.getEntries()));
//		}

			ContinuityClip cclip = new ContinuityClip(this.m_ContDist.getValue());
			cclip.operate(mappedGeom);
			System.out.println("mapped after cont clip " + mappedGeom.getNumVertices() + " pts");
			if(mappedGeom instanceof PgElementSet) {
				System.out.println("\t " + ((PgElementSet)mappedGeom).getNumElements() + " faces");
			}

			SphereClip clip = new SphereClip(this.m_Clipping.getValue());
			clip.operate(mappedGeom);
			System.out.println("mapped after sphere clip " + mappedGeom.getNumVertices() + " pts");
			if(mappedGeom instanceof PgElementSet) {
				System.out.println("\t " + ((PgElementSet)mappedGeom).getNumElements() + " faces");
			}
		} catch (UnSuportedGeometryException e) {
			showStatus("Unsupported geometry type");
			return null;
		} catch (EvaluationException e) {
			System.out.println(e.toString());
			calc.setGood(false);
			return null;
		}

		return mappedGeom;
	}
	
	public void displayGeom(GeomPair p, PgGeometryIf mappedGeom) {
	
		System.out.println("mapped outer");
		// debugCols((PgElementSet) mappedGeom);
		PgGeometryIf out = p.getOutput();
		PgGeometryIf input = p.getInput();

		// debugCols((PgElementSet) out);
		PointSetMaterial mat = PointSetMaterial.getMaterial(out);
		GeomStore.copySrcTgt(mappedGeom, out);
		mat.apply(out);

		System.out.println("out " + ((PgPointSet) out).getNumVertices());
		// debugCols((PgElementSet) out);
/*
		if (input instanceof PgElementSet) {
			PgElementSet outSurf = (PgElementSet) out;
			PgElementSet in2 = (PgElementSet) input; 
			if (in2.isShowingElementColors())
				outSurf.showElementColors(true);
			if (in2.hasVertexColors()) {
				// out2.makeElementFromVertexColors();
			} else if (in2.hasElementColors()) {
				outSurf.showElementColors(true);
			}
			switch(chSurfColours.getSelectedItem()) {
			case "Unchanged":
				outSurf.setGlobalElementColor(in2.getGlobalElementColor());
				break;
			case "Colours from XYZ":
				outSurf.makeElementColorsFromXYZ();
				outSurf.showElementColors(true);
				break;
			default:
				Color c = getColor(chSurfColours.getSelectedItem());
				outSurf.setGlobalElementColor(c);
				outSurf.showElementColors(false);
				outSurf.showVertexColors(false);
				break;
			}
		} else if (input instanceof PgPolygonSet) {
			final PgPolygonSet outCurve = (PgPolygonSet) out;
			final PgPolygonSet inCurve = (PgPolygonSet) input;
			outCurve.setGlobalPolygonColor(inCurve.getGlobalPolygonColor());
			if (inCurve.isShowingPolygonColors())
				outCurve.showPolygonColors(true);
			
			switch(this.chCurveColours.getSelectedItem()) {
			case "Unchanged":
				outCurve.setGlobalPolygonColor(inCurve.getGlobalPolygonColor());
				break;
			default:
				break;
			}
			

		}
		
		*/
//		setDisplayProperties(input.getName(),out);
		setGeometryInfo(out,input);
		store.geomChanged(out);
	}

	@Override
	public void newActiveInput(String name) {
		if (activePairs.containsKey(name)) {
			showStatus(name + " is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		PgGeometryIf output = store.aquireGeometry(getPreferredOutputName(name), input, this);
		GeomPair p = new GeomPair(input, output);
		PointSetMaterial mat = PointSetMaterial.getMaterial(input);
		mat.apply(output);
		activePairs.put(name, p);
		activeInputNames.add(name);
		materials.put(name,mat);
//		setDisplayProperties(p.getOutput());
		this.setCheckboxesFromGeomety(input);
		calcGeom(p);
		store.newPair(this, p);
	}

	@Override
	public boolean update(Object o) {
		if (o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		} else if (o == m_Clipping || o == this.m_ContDist) {
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
		} else if(itSel == chSurfColours) {
			setActiveMaterialsFromCheckboxes();
			setDisplayProperties();
		} else if(itSel == chCurveColours) {
			setActiveMaterialsFromCheckboxes();
			setDisplayProperties();
		} else if (itSel == cbShowFace || itSel == cbShowEdge || itSel == cbShowVert 
					|| itSel == cbShowCurves || itSel == cbShowPoints 
	 				|| itSel == cbShowBoundary || itSel == chCurveColours) {
			setActiveMaterialsFromCheckboxes();
			setDisplayProperties();
		} else {
			super.itemStateChanged(e);
		}
	}

	private void setActiveMaterialsFromCheckboxes() {
		for (String inputName : activeInputNames.getSelectedItems()) {
			PointSetMaterial mat = this.materials.get(inputName);
			setMaterialFromCheckboxes(mat);
		}
	}

	private void setMaterialFromCheckboxes(PointSetMaterial mat) {
		if(mat instanceof ElementSetMaterial) {
			ElementSetMaterial emat = (ElementSetMaterial) mat;
			emat.showEles = cbShowFace.getState();
			emat.showEdge = cbShowEdge.getState();
			emat.showVerts = cbShowVert.getState();
			emat.showBnd = cbShowBoundary.getState();
			
			switch(chSurfColours.getSelectedItem()) {
			case "Unchanged":
				emat.showEleCols = true;
				break;
			case "Red":
				emat.gEleCol = Color.red;
				break;
			case "Green":
				emat.gEleCol = Color.green;
				break;
			case "Blue":
				emat.gEleCol = Color.blue;
				break;
			case "Cyan":
				emat.gEleCol = Color.cyan;
				break;
			case "Magenta":
				emat.gEleCol = Color.magenta;
				break;
			case "Yellow":
				emat.gEleCol = Color.yellow;
				break;
			case "Black":
				emat.gEleCol = Color.black;
				break;
			case "White":
				emat.gEleCol = Color.white;
				break;
			case "Grey":
				emat.gEleCol = Color.gray;
				break;
			}
		} else if(mat instanceof PolygonSetMaterial) {
			PolygonSetMaterial pmat = (PolygonSetMaterial) mat;
			pmat.showPolys = cbShowCurves.getState();
			pmat.showVerts = cbShowVert.getState();
			
			switch(chCurveColours.getSelectedItem()) {
			case "Unchanged":
				pmat.showPolyCols = true;
				break;
			case "Red":
				pmat.gPolyCol = Color.red;
				break;
			case "Green":
				pmat.gPolyCol = Color.green;
				break;
			case "Blue":
				pmat.gPolyCol = Color.blue;
				break;
			case "Cyan":
				pmat.gPolyCol = Color.cyan;
				break;
			case "Magenta":
				pmat.gPolyCol = Color.magenta;
				break;
			case "Yellow":
				pmat.gPolyCol = Color.yellow;
				break;
			case "Black":
				pmat.gPolyCol = Color.black;
				break;
			case "White":
				pmat.gPolyCol = Color.white;
				break;
			case "Grey":
				pmat.gPolyCol = Color.gray;
				break;
			}
		} else {
			mat.showVerts = cbShowPoints.getState();
		}
		
	}


	@Override
	public void setDisplayProperties() {
		for (String inputName : activeInputNames.getSelectedItems()) {
			GeomPair p = activePairs.get(inputName);
			setDisplayProperties(inputName,p.getOutput());
		}

	}

	private void setDisplayProperties(String inputName, PgGeometryIf output) {
		PointSetMaterial mat = this.materials.get(inputName);
		mat.apply(output);
		store.geomApperenceChanged(output);
	}

	@Override
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
	}

	@Override
	public void setDP(int dp) {
		super.setDP(dp);
		this.m_Clipping.setDecimalPlaces(dp);
		this.m_ContDist.setDecimalPlaces(dp);
	}
}
