/*
Created 12-Jun-2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingUtilities;

import org.singsurf.singsurf.LmsPointSetMaterial;
import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.geometries.SSGeomListener;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;

public abstract class AbstractOperatorClient extends AbstractClient implements SSGeomListener {
	private static final long serialVersionUID = 1L;
	/** A choice of available inputs */
	protected Choice ch_inputSurf;
	/** String to represent no input */
	public static final String NONE = "-- None --";

	/** Pairs of input and output geometries indexed by the input name */
	protected Map<String, GeomPair> activePairs = new HashMap<String, GeomPair>();
	/** A list of activeInputs **/
	protected java.awt.List activeInputNames = new java.awt.List(10, false);

	protected Map<String, LmsPointSetMaterial> materials = new HashMap<String, LmsPointSetMaterial>();

	protected Button removeInputButton = new Button("Detach");
	protected Button removeInputGeomButton = new Button("Remove input and geom");
	protected Button removeInputDepButton = new Button("Remove input and geom and dependant");

	public AbstractOperatorClient(GeomStore store, String projName) {
		super(store, projName);
	}

	@Override
	public final void init() {
		super.init();
		ch_inputSurf = new Choice();
		ch_inputSurf.addItemListener(this);
		ch_inputSurf.setEnabled(true);
		store.addGeomListner(this);
		
		removeInputButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String name = activeInputNames.getSelectedItem();
				ch_inputSurf.select(NONE);
				GeomPair pair = activePairs.remove(name);
				activeInputNames.remove(name);
				store.releaseGeometry(pair.getOutput());
			}});

		removeInputGeomButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String name = activeInputNames.getSelectedItem();
				ch_inputSurf.select(NONE);
				removeGeometry(name,false);
			}});

		removeInputDepButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String name = activeInputNames.getSelectedItem();
				ch_inputSurf.select(NONE);
				removeGeometry(name,true);
			}});
		
		activeInputNames.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				String name = activeInputNames.getSelectedItem();
				GeomPair p = activePairs.get(name);
				if(p!=null)
					setCheckboxesFromGeomety(p.getOutput());
			}});

	}

	@Override
	public void geometryNameHasChanged(String oldName, String newName) {
		if (activePairs.containsKey(oldName)) {
			GeomPair p = activePairs.get(oldName);
			activePairs.remove(oldName);
			activePairs.put(newName, p);
			activeInputNames.remove(oldName);
			activeInputNames.add(newName);
		}
	}

	@Override
	public void geometryHasChanged(String geomName) {
		if (activePairs.containsKey(geomName))
			calcGeom(activePairs.get(geomName));
	}

	@Override
	public void removeGeometry(String geomName, boolean rmDependants) {
		if (activePairs.containsKey(geomName)) {
			GeomPair p = activePairs.get(geomName);
			activePairs.remove(geomName);
			activeInputNames.remove(geomName);
			store.removeGeometry(p.getOutput(), rmDependants);
		}
	}

	public void removeOutpuGeometry(PgGeometryIf outGeom, boolean rmDependants) {
		for (Entry<String, GeomPair> p : activePairs.entrySet()) {
			if (p.getValue().getOutput().equals(outGeom)) {
				activeInputNames.remove(p.getValue().getInput().getName());
				activePairs.remove(p.getKey());
			}
			store.removeGeometry(outGeom, rmDependants);
		}
	}

	/**
	 * Calculate all the needed geoms.
	 */
	@Override
	public void calcGeoms() {
		for (GeomPair p : activePairs.values())
			calcGeom(p);
	}

	public void calcGeom(GeomPair p) {

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

		Thread t = new Thread(new CalcGeomRunnable(p));
		t.start();

	}

	Lock lock = new ReentrantLock();
	 
	class CalcGeomRunnable implements Runnable {
		GeomPair pair;
		
		public CalcGeomRunnable(GeomPair pair) {
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
	
	class DisplayGeomRunnable implements Runnable {
		GeomPair pair;
		PgGeometryIf resultGeom;

		public DisplayGeomRunnable(GeomPair pair, PgGeometryIf resultGeom) {
			super();
			this.pair = pair;
			this.resultGeom = resultGeom;
		}

		@Override
		public void run() {
			displayGeom(pair,resultGeom);
			showStatus("Calculated "+getName() +" for "+pair.getInput().getName());
		}
	}
	
	public abstract PgGeometryIf calcGeomThread(GeomPair p);
	
	public abstract void displayGeom(GeomPair p,PgGeometryIf result);

	public abstract void newActiveInput(String name);

	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();

		if (itSel == ch_inputSurf) {
			if (ch_inputSurf.getSelectedItem().equals(NONE))
				return;
			newActiveInput(ch_inputSurf.getSelectedItem());
		} else
			super.itemStateChanged(e);
	}

	@Override
	public void refreshList(SortedSet<String> list) {
		String item = this.ch_inputSurf.getSelectedItem();
		this.ch_inputSurf.removeAll();
		ch_inputSurf.add(NONE);
		for (String name : list)
			this.ch_inputSurf.add(name);
		this.ch_inputSurf.select(item);
	}

	/**
	 * Returns the preferred name for an output geom, given the input name. The
	 * eventual name of the output geom may be changed to avoid name clashes. Names
	 * are of the form <tt>project(name)</tt>.
	 * 
	 * @param name the name of the input geom
	 * @return the name of the output geom
	 */
	public String getPreferredOutputName(String name) {
		return getName() + "(" + name + ")";
	}

	@Override
	public List<PgGeometryIf> getOutputGeoms() {
		List<PgGeometryIf> outputs = new ArrayList<PgGeometryIf>();
		for (GeomPair p : activePairs.values())
			outputs.add(p.getOutput());
		return outputs;
	}

	public List<PgGeometryIf> getInputGeoms() {
		List<PgGeometryIf> inputs = new ArrayList<PgGeometryIf>();
		for (GeomPair p : activePairs.values())
			inputs.add(p.getInput());
		return inputs;
	}

	public Collection<GeomPair> getInputOutputPairs() {
		return activePairs.values();
	}

	@Override
	public void setName(String arg0) {
		super.setName(arg0);
		if (activePairs == null)
			return;
		for (GeomPair p : activePairs.values()) {
			store.setName(p.getOutput(), getPreferredOutputName(p.getInput().getName()));
		}
	}

	@Override
	public void setDisplayProperties() {
		for (String input : activeInputNames.getSelectedItems()) {
			GeomPair p = activePairs.get(input);
			setDisplayProperties(p.getOutput());
		}

	}

	
	
	@Override
	public ProjectComponents getProjectComponents() {
		ProjectComponents pc = new ProjectComponents(this.getName());
		for (String key : this.activePairs.keySet()) {
			pc.addInput(key);
			addInputOptionsFromGeometry(pc,key);
		}
		return pc;
	}

	@Override
	public void loadProjectComponents(ProjectComponents comp, PaSingSurf ss) {
		for (String s : comp.getInputs()) {
			setCheckboxes(comp,s);
			this.newActiveInput(s);
			
		}
	}

	@Override
	protected String getDetails(PgGeometryIf input) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("\"definition\": ");
		sb.append(this.getDefinition().getJSON());
		sb.append(",\n");

		if(this instanceof GeneralisedOperator) {
			GeneralisedOperator go = (GeneralisedOperator) this;
			sb.append("\"ingridient\": ");
			sb.append(go.getIngredient().getDefinition().getJSON());
			sb.append(",\n");
		}
		if(this instanceof GeneralisedBiOperator) {
			GeneralisedBiOperator go = (GeneralisedBiOperator) this;
			sb.append("\"ingridient1\": ");
			sb.append(go.getIngredient1().getDefinition().getJSON());
			sb.append(",\n");
			sb.append("\"ingridient2\": ");
			sb.append(go.getIngredient2().getDefinition().getJSON());
			sb.append(",\n");
		}
		sb.append("\"input\": ");
		if(input.getGeometryInfo() == null || input.getGeometryInfo().getDetail() == null) {
			sb.append("No details");
		} else 
			sb.append(input.getGeometryInfo().getDetail());
		sb.append("\n");
		sb.append("}");
		return sb.toString();
	}


	public void selectOutputGeometry(PgGeometryIf geom) {
		for(Entry<String, GeomPair> ent:activePairs.entrySet()) {
			if(ent.getValue().getOutput().equals(geom)) {
				selectGeometry(ent.getValue());
				break;
			}
		}
	}
	

	protected void setCheckboxesFromGeomety(PgGeometryIf geom) {
		if(geom instanceof PgElementSet) {
			PgElementSet surf = (PgElementSet) geom;
			cbShowFace.setState(surf.isShowingElements());
			cbShowEdge.setState(surf.isShowingEdges());
			cbShowVert.setState(surf.isShowingVertices());
			this.cbShowFace.setEnabled(true);
			this.cbShowEdge.setEnabled(true);
			this.cbShowVert.setEnabled(true);
			this.cbShowPoints.setEnabled(false);
			this.cbShowCurves.setEnabled(false);
			
			if(surf.hasElementColors()) {
				chSurfColours.select("Unchanged");								
			} else if(surf.isShowingElementColorFromVertices()) {
				chSurfColours.select("Unchanged");
			} else {
				Color c = surf.getGlobalElementColor();
				if(Color.red.equals(c)) {
					this.chSurfColours.select("Red");
				} else if(Color.green.equals(c)) {
					this.chSurfColours.select("Green");
				} else if(Color.blue.equals(c)) {
					this.chSurfColours.select("Blue");
				} else if(Color.cyan.equals(c)) {
					this.chSurfColours.select("Cyan");
				} else if(Color.magenta.equals(c)) {
					this.chSurfColours.select("Magenta");
				} else if(Color.yellow.equals(c)) {
					this.chSurfColours.select("Yellow");
				} else if(Color.black.equals(c)) {
					this.chSurfColours.select("Black");
				} else if(Color.gray.equals(c)) {
					this.chSurfColours.select("Grey");
				} else if(Color.white.equals(c)) {
					this.chSurfColours.select("White");
				} else if(Color.orange.equals(c)) {
					this.chSurfColours.select("Orange");
				}
			}
			
		} else if(geom instanceof PgPolygonSet) {
			PgPolygonSet surf = (PgPolygonSet) geom;
			cbShowCurves.setState(surf.isShowingPolygons());
			cbShowPoints.setState(surf.isShowingVertices());
			this.cbShowFace.setEnabled(false);
			this.cbShowEdge.setEnabled(false);
			this.cbShowVert.setEnabled(true);
			this.cbShowPoints.setEnabled(false);
			this.cbShowCurves.setEnabled(true);
			
			if(surf.hasPolygonColors()) {
				chCurveColours.select("Unchanged");								
			} else if(surf.isShowingEdgeColorFromVertices()) {
				chCurveColours.select("Unchanged");
			} else {
				Color c = surf.getGlobalPolygonColor();
				if(Color.red.equals(c)) {
					this.chCurveColours.select("Red");
				} else if(Color.green.equals(c)) {
					this.chCurveColours.select("Green");
				} else if(Color.blue.equals(c)) {
					this.chCurveColours.select("Blue");
				} else if(Color.cyan.equals(c)) {
					this.chCurveColours.select("Cyan");
				} else if(Color.magenta.equals(c)) {
					this.chCurveColours.select("Magenta");
				} else if(Color.yellow.equals(c)) {
					this.chCurveColours.select("Yellow");
				} else if(Color.black.equals(c)) {
					this.chCurveColours.select("Black");
				} else if(Color.gray.equals(c)) {
					this.chCurveColours.select("Grey");
				} else if(Color.white.equals(c)) {
					this.chCurveColours.select("White");
				} else if(Color.orange.equals(c)) {
					this.chCurveColours.select("Orange");
				}
			}
			
		} else if(geom instanceof PgPointSet) {
			PgPointSet surf = (PgPointSet) geom;
			cbShowPoints.setState(surf.isShowingVertices());
			this.cbShowFace.setEnabled(false);
			this.cbShowEdge.setEnabled(false);
			this.cbShowVert.setEnabled(false);
			this.cbShowCurves.setEnabled(false);
			this.cbShowPoints.setEnabled(true);
		} else {
			this.cbShowFace.setEnabled(false);
			this.cbShowEdge.setEnabled(false);
			this.cbShowVert.setEnabled(false);
			this.cbShowCurves.setEnabled(false);
			this.cbShowPoints.setEnabled(false);			
		}
	}
	
	private void setCheckboxes(ProjectComponents comp, String name) {
		Map<String, String> map = comp.getInputOptions(name);
		for(Entry<String, String> ent:map.entrySet()) {
			if("showFace".equals(ent.getKey())) {
				setCheckbox(this.cbShowFace,ent.getValue());
			}
			if("showEdge".equals(ent.getKey())) {
				setCheckbox(this.cbShowEdge,ent.getValue());
			}
			if("showVert".equals(ent.getKey())) {
				setCheckbox(this.cbShowVert,ent.getValue());
			}
			if("showCurves".equals(ent.getKey())) {
				setCheckbox(this.cbShowCurves,ent.getValue());
			}
			if("showPoints".equals(ent.getKey())) {
				setCheckbox(this.cbShowPoints,ent.getValue());
			}
			if("surfColours".equals(ent.getKey())) {
				this.chSurfColours.select(ent.getValue());
			}
			if("curveColours".equals(ent.getKey())) {
				this.chCurveColours.select(ent.getValue());
			}
		}
	}

	protected void setCheckbox(Checkbox cb, String string) {
		cb.setState("true".equals(string));
	}

	private void addInputOptionsFromGeometry(ProjectComponents pc, String key) {
		PgGeometryIf geom = this.activePairs.get(key).getOutput();
		if(geom instanceof PgElementSet) {
			PgElementSet surf = (PgElementSet) geom;
			pc.addInputOption(key, "showFace", surf.isShowingElements() ? "true" : "false");
			pc.addInputOption(key, "showEdge", surf.isShowingEdges() ? "true" : "false");
			pc.addInputOption(key, "showVert", surf.isShowingVertices() ? "true" : "false");
			
			if(surf.hasElementColors()) {
				pc.addInputOption(key, "surfColours","Unchanged");								
			} else if(surf.isShowingElementColorFromVertices()) {
				pc.addInputOption(key, "surfColours","Unchanged");								
			} else {
				Color c = surf.getGlobalElementColor();
				if(Color.red.equals(c)) {
					pc.addInputOption(key, "surfColours","Red");
				} else if(Color.green.equals(c)) {
					pc.addInputOption(key, "surfColours","Green");
				} else if(Color.blue.equals(c)) {
					pc.addInputOption(key, "surfColours","Blue");
				} else if(Color.cyan.equals(c)) {
					pc.addInputOption(key, "surfColours","Cyan");
				} else if(Color.magenta.equals(c)) {
					pc.addInputOption(key, "surfColours","Magenta");
				} else if(Color.yellow.equals(c)) {
					pc.addInputOption(key, "surfColours","Yellow");
				} else if(Color.black.equals(c)) {
					pc.addInputOption(key, "surfColours","Black");
				} else if(Color.gray.equals(c)) {
					pc.addInputOption(key, "surfColours","Grey");
				} else if(Color.white.equals(c)) {
					pc.addInputOption(key, "surfColours","White");
				} else if(Color.orange.equals(c)) {
					pc.addInputOption(key, "surfColours","Orange");
				}
			}
			
		} else if(geom instanceof PgPolygonSet) {
			PgPolygonSet polygon = (PgPolygonSet) geom;
			pc.addInputOption(key, "showCurves",polygon.isShowingPolygons() ? "true" : "false");
			pc.addInputOption(key, "showPoints",polygon.isShowingVertices() ? "true" : "false");
			if(polygon.hasPolygonColors()) {
				pc.addInputOption(key, "curveColours","Unchanged");								
			} else if(polygon.isShowingEdgeColorFromVertices()) {
				pc.addInputOption(key, "curveColours","Unchanged");
			} else {
				Color c = polygon.getGlobalPolygonColor();
				if(Color.red.equals(c)) {
				pc.addInputOption(key, "curveColours","Red");
			} else if(Color.green.equals(c)) {
				pc.addInputOption(key, "curveColours","Green");
			} else if(Color.blue.equals(c)) {
				pc.addInputOption(key, "curveColours","Blue");
			} else if(Color.cyan.equals(c)) {
				pc.addInputOption(key, "curveColours","Cyan");
			} else if(Color.magenta.equals(c)) {
				pc.addInputOption(key, "curveColours","Magenta");
			} else if(Color.yellow.equals(c)) {
				pc.addInputOption(key, "curveColours","Yellow");
			} else if(Color.black.equals(c)) {
				pc.addInputOption(key, "curveColours","Black");
			} else if(Color.gray.equals(c)) {
				pc.addInputOption(key, "curveColours","Grey");
			} else if(Color.white.equals(c)) {
				pc.addInputOption(key, "curveColours","White");
			} else if(Color.orange.equals(c)) {
				pc.addInputOption(key, "curveColours","Orange");
			}
			}
		} else if(geom instanceof PgPointSet) {
			PgPointSet surf = (PgPointSet) geom;
			pc.addInputOption(key, "showPoints",surf.isShowingVertices() ? "true" : "false");
		} else {
		}
		
	}

	
	public void selectGeometry(GeomPair geomPair) {
		String name = geomPair.getInput().getName();
		String[] items = activeInputNames.getItems();
		PgGeometryIf output = geomPair.getOutput();
		setCheckboxesFromGeomety(output);
		
		int index=-1;	
		for(int i=0;i<items.length;++i) {
			if(name.equals(items[i]))
				index = i;
		}
		if(index==-1) return;	

		activeInputNames.select(index);
	}

}
