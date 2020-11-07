package org.singsurf.singsurf.clients;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.Parameter;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;

import jv.project.PgGeometryIf;
import jv.project.PgJvxSrc;

public class Globals extends AbstractClient {
	private static final long serialVersionUID = 1L;
	Map<String,PgGeometryIf> orphans = new HashMap<>();
	
	protected Button removeGeomButton = new Button("Delete");

	/** A list of activeInputs **/
	protected java.awt.List activeGeomNames = new java.awt.List(10, false);

	public Globals(GeomStore store, Definition def) {
		super(store, def == null ? "Globals" : def.getName());
		init(def);
	}

//	public Globals(GeomStore store, String projName) {
//		super(store, projName);
//		init();
//	}

	/**
	 * 
	 */
	public void init(Definition def) {
		super.init();
		newParams = new LParamList(this);
//		Definition def = createDefaultDef();
		this.calc = new Calculator(def,0);
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
	public void loadProjectComponents(ProjectComponents comp, PaSingSurf ss) {
	}

	@Override
	public void calcGeoms() {
	}

	@Override
	public void setDefinitionOptions(Definition def) {

	}

	@Override
	public void loadDefinition(Definition newdef) {

	}

	public Definition createDefaultDef() {
		return  new Definition("Globals", DefType.globals, "");
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
		PgGeometryIf geom = store.aquireFromJvx(jvx, name, this);
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

	
}
