package org.singsurf.singsurf.clients;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTextArea;

import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.calculators.Evaluator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Parameter;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.project.PgGeometryIf;
import jv.project.PgJvxSrc;

public class Globals extends AbstractProject {
	private static final long serialVersionUID = 1L;
	Map<String,PgGeometryIf> orphans = new HashMap<>();
	
	protected Button removeGeomButton = new Button("Delete");

	/** A list of activeInputs **/
	protected java.awt.List activeGeomNames = new java.awt.List(10, false);

	JTextArea results = new JTextArea();
	
	public Globals(GeomStore store, Definition def) {
		super(store, def == null ? "Globals" : def.getName());
		init(def);
	}

	/**
	 * 
	 */
	public void init(Definition def) {
		super.init();
		newParams = new LParamList(this);
		this.calc = new Calculator(def,0);
		results.setRows(5);
		this.removeGeomButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				removeGeom(activeGeomNames.getSelectedItem());
			}} );
	}

	@Override
	public void setDisplayProperties() {
		String name = activeGeomNames.getSelectedItem();
		PgGeometryIf geom = orphans.get(name);
		setDisplayProperties(geom);
	}

	@Override
	public List<PgGeometryIf> getOutputGeoms() {
		return new ArrayList<>(orphans.values());
	}

	@Override
	public ProjectComponents getProjectComponents() {
		return new ProjectComponents(this.getName());
	}

	@Override
	public void loadProjectComponents(ProjectComponents comp) {
	}

	@Override
	public void calcGeoms() {
		Evaluator ce = calc.createEvaluator();
		try {
			double topRes[];
			topRes = ce.evalTop(new double[]{0.0});
			StringBuilder sb = new StringBuilder();
			for(double val:topRes) {
				sb.append(val);
				sb.append('\n');
			}
			results.setText(sb.toString());
		} catch (EvaluationException e) {
			System.out.println(e.toString());
			calc.setGood(false);
			return;
		}
	}

	@Override
	public void setDefinitionOptions(Definition def) {

	}

	@Override
	public void loadDefinition(Definition newdef) {
		Definition def = newdef.duplicate();
		setDisplayEquation(def.getEquation());
		// boolean flag =
		calc = new Calculator(def, 0);
		calc.build();
		if (!calc.isGood())
			showStatus(calc.getMsg());

	}

	@Override
	public boolean update(Object o) {
		if (o instanceof PuParameter) {
			PuParameter param = (PuParameter) o; 
			store.updateGlobal(param);
			this.parameterChanged(param);
			calc.setParamValue(param.getName(), param.getVal());

			return true;
		} else
			return super.update(o);
	}

	public PuParameter addParameter(Parameter param) {
//		calc.setParamValue(param.getName(), param.getVal());	
		Parameter p2 = calc.getDefinition().addParameter(param.getName());
		p2.setVal(param.getVal());
		return this.newParams.addParameter(param);
		
	}

	public boolean addOrphan(PgJvxSrc jvx, String name) {
		PgGeometryIf geom = store.acquireFromJvx(jvx, name, this);
		orphans.put(name,geom);
		this.activeGeomNames.add(name);
		store.geomChanged(geom);
		store.tellListners();
		return true;
	}
	
	

	@Override
	public void removeGeometry(PgGeometryIf geom) {
		orphans.remove(geom.getName());
		activeGeomNames.remove(geom.getName());
		store.removeGeometry(geom, false);
	}
	protected void removeGeom(String name) {
		PgGeometryIf geom = orphans.remove(name);
		activeGeomNames.remove(name);
		store.removeGeometry(geom, false);
	}

	@Override
	public boolean addGeometry(PgGeometryIf geom) {
		orphans.put(geom.getName(),geom);
		this.activeGeomNames.add(geom.getName());
//		store.geomChanged(geom);
//		store.tellListners();
		return true;

	}

	protected void refreshParams() {
		store.clearGlobalParameters(this);
		int size = calc.getNParam();
		for (int i = 0; i < size; ++i) {
			
			Parameter param = calc.getParam(i);
			if(param.getName().startsWith("global_")) {
				store.addGlobalParameter(param,this);
				calc.setParamValue(param.getName(), param.getVal());
			} else {
				store.addGlobalParameter(param,this);
				calc.setParamValue(param.getName(), param.getVal());
			}
		}
		store.rebuildGlobalsParameters();
	}

}
