/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.List;

import jv.geom.PgPolygonSet;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;

import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Plotter2D;
import org.singsurf.singsurf.calculators.PolynomialCalculator;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.geometries.PolygonSetMaterial;
import org.singsurf.singsurf.jep.EquationPolynomialConverter;



/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class ACurve extends AbstractProject {
    private static final long serialVersionUID = 6873398333105602351L;

    Definition def;

    protected CheckboxGroup cbg_coarse; // Coarse checkbox group
    protected CheckboxGroup cbg_fine; // Coarse checkbox group
    protected CheckboxGroup cbg_timeout; // Timeout checkbox group

    protected Checkbox cb_c_4, cb_c_8, cb_c_16, cb_c_32, cb_c_64, cb_c_128, cb_c_256;
    // protected Checkbox cb_fi_8, cb_fi_16, cb_fi_32, cb_fi_64,
    // cb_fi_128, cb_fi_256, cb_fi_512, cb_fi_1024;

    // int globalSteps = 60;
    DefVariable localX, localY;
    protected PgPolygonSet outCurve;

    Plotter2D plotter;

    public ACurve(GeomStore store, Definition def) {
		super(store, def.getName());
		if (getClass() == ACurve.class) {
		    init(def);
		}
    }

    public void init(Definition def1) {
	super.init();
	localX = new DefVariable("x", "Normal");
	localY = new DefVariable("y", "Normal");
	displayVars = new PuVariable[] { new PuVariable(this, localX), new PuVariable(this, localY) };
	newParams = new LParamList(this);
	cbg_coarse = new CheckboxGroup();
	cbg_fine = new CheckboxGroup();

	cb_c_4 = new Checkbox("4", cbg_coarse, false);
	cb_c_8 = new Checkbox("8", cbg_coarse, false);
	cb_c_16 = new Checkbox("16", cbg_coarse, false);
	cb_c_32 = new Checkbox("32", cbg_coarse, false);
	cb_c_64 = new Checkbox("64", cbg_coarse, true);
	cb_c_128 = new Checkbox("128", cbg_coarse, true);
	cb_c_256 = new Checkbox("256", cbg_coarse, true);

	// cb_fi_8 = new Checkbox("8",cbg_fine,false);
	// cb_fi_16 = new Checkbox("16",cbg_fine,false);
	// cb_fi_32 = new Checkbox("32",cbg_fine,false);
	// cb_fi_64 = new Checkbox("64",cbg_fine,false);
	// cb_fi_128 = new Checkbox("128",cbg_fine,false);
	// cb_fi_256 = new Checkbox("256",cbg_fine,true);
	// cb_fi_512 = new Checkbox("512",cbg_fine,false);
	// cb_fi_1024 = new Checkbox("1024",cbg_fine,false);

	cbShowVert.setState(false);
	int coarse = Integer.parseInt(cbg_coarse.getSelectedCheckbox().getLabel());
	plotter = new Plotter2D(coarse, coarse * 4, 4096);
	// Integer.parseInt(cbg_fine.getSelectedCheckbox().getLabel()),4096);
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
	def.setOption("coarse", c);
    }

    public int getCoarse() {
	// return def.getOpt("coarse").getIntegerVal();
	return Integer.parseInt(cbg_coarse.getSelectedCheckbox().getLabel());
    }
    
    @SuppressWarnings("unused")
    void checkDef(Definition def1) {
	// DefVariable var =def.getVar(0);
	// if(var.getSteps()==-1) var.setBounds(var.getMin(),var.getMax(),globalSteps);
    }

    @Override
    public void loadDefinition(Definition newdef) {
	def = newdef.duplicate();
	checkDef(def);
	def.setName(this.getName());
	this.getInfoPanel().setTitle(this.getName());
	calc = new PolynomialCalculator(def, 0);
	calc.build();
	if (!calc.isGood())
	    showStatus(calc.getMsg());
	localX = calc.getDefVariable(0);
	localY = calc.getDefVariable(1);
	setDisplayEquation(def.getEquation());
	displayVars[0].set(localX);
	displayVars[1].set(localY);
	refreshParams();
	Option copt = def.getOption("coarse");
	if (copt != null)
	    setCoarse(copt.getIntegerVal());
	// Option fopt = def.getOption("fine");
	// if(fopt!=null) setFine(fopt.getIntegerVal());
	Option svopt = def.getOption("showVert");
	if (svopt != null)
	    this.cbShowVert.setState(svopt.getBoolVal());
	Option scopt = def.getOption("showCurve");
	if (scopt != null)
	    this.cbShowCurves.setState(scopt.getBoolVal());
	Option colopt = def.getOption("colour");
	if (colopt != null)
	    this.chCurveColours.select(colopt.getStringVal());

	outCurve = store.acquireCurve(newdef.getName(), this);
	calcGeoms();
    }

    @Override
    public void setDefinitionOptions(Definition def) {
	def.setOption("coarse", this.getCoarse());
	// def.setOption("fine",this.getFine());
	def.setOption("showVert", this.cbShowVert.getState());
	def.setOption("showCurve", this.cbShowCurves.getState());
	def.setOption("colour", this.chCurveColours.getSelectedItem());
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
    }

    @Override
    public void calcGeoms() {
	if (!calc.isGood()) {
	    showStatus(calc.getMsg());
	    return;
	}
	int coarse = Integer.parseInt(cbg_coarse.getSelectedCheckbox().getLabel());
	plotter.setDepths(coarse, coarse * 4, 4096);

	rebuildResultArray();
	if (outCurve != null)
	    line_mat = new PolygonSetMaterial(outCurve);

	PgGeometryIf resultGeom = null;

	EquationPolynomialConverter ec = new EquationPolynomialConverter(calc.getJep());
	try {
	    double[][] coeffs = ec.convert2D(calc.getRawEqns(), new String[] { localX.getName(), localY.getName() },
		    calc.getParams());

	    resultGeom = plotter.calculate(coeffs, localX.getMin(), localX.getMax(), localY.getMin(), localY.getMax());
	} catch (AsurfException e) {
	    e.printStackTrace();
	} catch (ParseException e) {
	    PsDebug.error(e.getMessage());
	}
	if (resultGeom == null) {
	    PsDebug.error("Algebraic curve could not be calculated");
	    return;
	}
	GeomStore.copySrcTgt(resultGeom, outCurve);

	outCurve.showVertices(cbShowVert.getState());
	outCurve.showPolygons(cbShowCurves.getState());
	setColour(outCurve, chCurveColours.getSelectedItem());
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
	}
	if (o == displayVars[1]) {
	    variableRangeChanged(1, (PuVariable) o);
	    return true;
	} else if (o instanceof PuParameter) {
	    return parameterChanged((PuParameter) o);
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
    public void loadProjectComponents(ProjectComponents comp) {

    }
}
