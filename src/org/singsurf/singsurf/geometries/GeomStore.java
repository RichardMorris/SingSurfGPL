/*
Created 24-May-2006 - Richard Morris
*/
package org.singsurf.singsurf.geometries;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.clients.AbstractOperatorClient;
import org.singsurf.singsurf.clients.Globals;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.DefinitionReader;
import org.singsurf.singsurf.definitions.Parameter;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsConfig;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;
import jv.project.PgJvxSrc;
import jv.project.PvDisplayIf;
import jv.project.PvGeometryListenerIf;
import jv.project.PvViewerIf;

/**
 * Main interface between SingSurf programs and JavaView. Holds all the current
 * list of all geometries.
 * 
 * 
 * @author Richard Morris
 *
 */
public class GeomStore implements PvGeometryListenerIf {
    /** Reference to the main viewer */
    PvViewerIf viewer;
    /** Reference to main applet */
    /** A list of geometries indexed by name */
    Map<String, PgGeometryIf> allGeoms = new HashMap<String, PgGeometryIf>();
    /** All projects which wish to know the complete list of geometries */
    List<SSGeomListener> listeners = new ArrayList<SSGeomListener>();
    /** Whether to fit the display */
    public boolean doFitDisplay = false;

    boolean fitNeeded = false;

    /** The programs which generated the inputs */
    Map<PgGeometryIf, AbstractClient> generators = new HashMap<PgGeometryIf, AbstractClient>();
    
    /** Global project */
    public Globals globals;

    public GeomStore(PvViewerIf viewer) {
	this.viewer = viewer;
	PvDisplayIf[] disps = viewer.getDisplays();
	for (int i = 0; i < disps.length; ++i) {
	    if (!disps[i].hasGeometryListener(this))
		disps[i].addGeometryListener(this);
	}
    };

    /**
     * Registers a project which wants to be kept informed of the current list of
     * geometries.
     * 
     * @param listner
     */
    public void addGeomListner(SSGeomListener listner) {
	listeners.add(listner);
	SortedSet<String> s = new TreeSet<String>(allGeoms.keySet());
	listner.refreshList(s);
    }

    Pattern endsWithNumber = Pattern.compile("^(.*)#(\\d+)$");

    /**
     * Finds a unique name for a geometry.
     * 
     * @param name
     *                 preferred name
     * @return a unique name which possible ends with #1, #2 etc.
     */
    public String getUniqueName(String name) {
	if (!allGeoms.containsKey(name))
	    return name;
	Matcher m = endsWithNumber.matcher(name);
	int n = 0;
	if (m.matches()) {
	    MatchResult mr = m.toMatchResult();
	    name = mr.group(1);
	    String num = mr.group(2);
	    n = Integer.parseInt(num);
	}
	++n;
	while (allGeoms.containsKey(name + "#" + n))
	    ++n;
	return name + "#" + n;
    }

    /**
     * Returns a new surface geometry
     * 
     * @param preferredName
     *                          the preferred name for the geometry
     * @return a new geometry object
     */
    public PgElementSet aquireSurface(String preferredName, AbstractClient client) {
    	PgElementSet res = new PgElementSet(3);
    	res.setName(getUniqueName(preferredName));
    	viewer.getDisplay().addGeometry(res);
    	generators.put(res, client);
    	return res;
    }

    public PgGeometryIf aquireFromJvx(PgJvxSrc jvx,String name, AbstractClient client) {

    	if(jvx.getNumElements()>0) {
    		PgElementSet geom = aquireSurface(name, client);
    		geom.setJvx(jvx);
    		geom.setName(name);
    		return geom;
    	} else if(jvx.getNumPolygons()>0) {
    		PgPolygonSet geom = aquireCurve(name, client);
    		geom.setJvx(jvx);
    		geom.setName(name);
    		return geom;			
    	} else {
    		PgPointSet geom = aquirePoints(name,client);
    		geom.setJvx(jvx);
    		geom.setName(name);
    		return geom;			
    	}
    }
    /**
     * Returns a new curve geometry
     * 
     * @param preferredName
     *                          the preferred name for the geometry
     * @return a new geometry object
     */
    public PgPolygonSet aquireCurve(String preferredName, AbstractClient client) {
	PgPolygonSet res = new PgPolygonSet(3);
	res.setName(getUniqueName(preferredName));
	viewer.getDisplay().addGeometry(res);
	generators.put(res, client);
	return res;
    }

    /**
     * Returns a new geometry for a set of points
     * 
     * @param preferredName
     *                          the preferred name for the geometry
     * @return a new geometry object
     */

    public PgPointSet aquirePoints(String preferredName, AbstractClient client) {
	PgPointSet res = new PgPointSet(3);
	res.setName(getUniqueName(preferredName));
	viewer.getDisplay().addGeometry(res);
	generators.put(res, client);
	return res;
    }

    /** Aquire a geometry who's type matches the input */
    public PgGeometryIf aquireGeometry(String name, PgGeometryIf input, AbstractClient client) {
	PgGeometryIf output = null;
	if (input instanceof PgElementSet)
	    output = this.aquireSurface(name, client);
	else if (input instanceof PgPolygonSet)
	    output = this.aquireCurve(name, client);
	else if (input instanceof PgPointSet)
	    output = this.aquirePoints(name, client);
	return output;
    }

    /**
     * Registers than the given geometry has changed. Will select the geometry in
     * the viewer and fit the display if needed.
     * 
     * @param geom
     *                 the geometry to change
     */
    public void geomChanged(PgGeometryIf geom) {
	boolean oldFitNeeded = fitNeeded;
	fitNeeded = false;

	geom.update(null);
	viewer.setGeometry(geom);
	for (SSGeomListener listner : listeners)
	    listner.geometryHasChanged(geom.getName());

	if (oldFitNeeded && this.doFitDisplay) {
		System.out.println("Fitting "+oldFitNeeded+" "+doFitDisplay);
	    PvDisplayIf[] disps = viewer.getDisplays();
	    for (int i = 0; i < disps.length; ++i) {
		disps[i].fit();
	    }
	}
	fitNeeded = oldFitNeeded;
    }

    public void geomApperenceChanged(PgGeometryIf geom) {
	geom.update(null);
	viewer.setGeometry(geom);
    }

    /**
     * Registers than the given geometry has changed. Will select the geometry in
     * the viewer and fit the display if needed.
     */
    public void geomDefinitionChanged(AbstractClient client, Calculator inCalc) {
	for (SSGeomListener listner : listeners)
	    listner.geometryDefHasChanged(client, inCalc);
    }

    /**
     * Displays a status message.
     * 
     * @param str
     */
    public void showStatus(String str) {
	System.out.println(str);
	// if(applet!=null)
	// applet.showStatus(str);
	// else
	PsDebug.pushStatus(str);

	// ((PaSingSurf) applet).messageField;

    }

    /** Gets an applet parameter */
    public String getParameter(String str) {
	if (viewer != null)
	    return viewer.getParameter(str);
	else
	    return null;
    }

    /**
     * Utility method to copy the geometry data from its source to target.
     * 
     * @param src
     * @param tgt
     */
    public static void copySrcTgt(PgGeometryIf src, PgGeometryIf tgt) {
    	try {
    		if (tgt instanceof PgElementSet) {
    			ElementSetMaterial mat = new ElementSetMaterial((PgElementSet) tgt);
    			((PgElementSet) tgt).copy((PgElementSet) src);
    			mat.apply((PgElementSet) tgt);
    		} else if (tgt instanceof PgPolygonSet) {
    			PolygonSetMaterial mat = new PolygonSetMaterial((PgPolygonSet) tgt);
    			((PgPolygonSet) tgt).copy((PgPolygonSet) src);
    			mat.apply((PgPolygonSet) tgt);
    		} else if (tgt instanceof PgPointSet) {
    			PointSetMaterial mat = new PointSetMaterial((PgPointSet) tgt);
    			((PgPointSet) tgt).copy((PgPointSet) src);
    			mat.apply((PgPointSet) tgt);
    		} 
    	} catch(NoSuchElementException|ArrayIndexOutOfBoundsException ex) {
    			System.out.println("Error copying geom "+src.getName());
    	}

    }

    /** Returns a geometry with a given name */
    public PgGeometryIf getGeom(String name) {
	return allGeoms.get(name);
    }

    /**** PvViewerIf methods ********/

    @Override
    public void addGeometry(PgGeometryIf geom) {
	allGeoms.put(geom.getName(), geom);
	tellListners();
	System.out.println("ADD '" + geom.getName() + "'allGeoms.size " + allGeoms.size());
    }

    @Override
    public String getName() {
	return "SingSurf GeometryStore";
    }

    @Override
    public void removeGeometry(PgGeometryIf geom) {
	System.out.println("remove geom '" + geom.getName() + "' allGeoms.size " + allGeoms.size());
	this.removeGeometry(geom, false);
    }

    public void removeGeometry(PgGeometryIf geom, boolean rmDependants) {
    	if(geom==null)
    		return;
    	allGeoms.remove(geom.getName());
    	for (SSGeomListener l : listeners)
    		l.removeGeometry(geom.getName(), rmDependants);
    	tellListners();
    	for (PvDisplayIf disp : viewer.getDisplays()) {
    		if (disp.containsGeometry(geom))
    			disp.removeGeometry(geom);
    	}
    }

    @Override
    public void selectGeometry(PgGeometryIf arg0) {
    }

    public void removeAll() {
	for (PvDisplayIf disp : viewer.getDisplays()) {
	    for (PgGeometryIf geom : disp.getGeometries())
		disp.removeGeometry(geom);
	}
    }

    public void setName(PgGeometryIf geom, String name) {
	String oldName = geom.getName();
	allGeoms.remove(oldName);
	geom.setName(name);
	allGeoms.put(name, geom);
	for (SSGeomListener listner : listeners)
	    listner.geometryNameHasChanged(oldName, name);
    }

    /** Tells all the listeners when a geometry is added or removed. */
    public void tellListners() {
	SortedSet<String> s = new TreeSet<String>(allGeoms.keySet());
	for (SSGeomListener listner : listeners)
	    listner.refreshList(s);
    }

    /**
     * @return Returns the doFitDisplay.
     */
    public boolean isDoFitDisplay() {
	return doFitDisplay;
    }

    /**
     * @param doFitDisplay
     *                         The doFitDisplay to set.
     */
    public void setDoFitDisplay(boolean doFitDisplay) {
    	System.out.println("setDoFitDisplay("+doFitDisplay+")");
	this.doFitDisplay = doFitDisplay;
    }

    public String getFullPath(String fileName) {
	String head = null;

	String codeBase = PsConfig.getUserBase();
//	if (codeBase == null)
//	    codeBase = applet.getParameter("DefUrl");
	if (codeBase.endsWith(".")) // Hack to get it to work with jview which has /. at the end of codeBase
	{
	    head = codeBase + "/";
	} else {
	    head = codeBase;
	}
	// if(PRINT_DEBUG) System.out.println("getFullPath ("+head+")+("+fileName+")");
	return head + fileName;
    }

    public DefinitionReader loadDefs(String fileName) throws IOException {
	DefinitionReader ldr = new DefinitionReader(getFullPath(fileName));
	ldr.read();
	return ldr;
    }

    public List<Definition> loadDefs(URL url) throws IOException {
	DefinitionReader ldr = new DefinitionReader(url);
	ldr.read();
	return ldr.getDefs();
    }

    public AbstractClient getGenerator(PgGeometryIf geom) {
	return generators.get(geom);
    }

    public AbstractClient getGenerator(String name) {
	return generators.get(this.getGeom(name));
    }

	public void newPair(AbstractOperatorClient intersection, GeomPair p) {
		tellListners();	
	}

	Map<PuParameter,Set<AbstractClient>> globalDependants = new HashMap<>();
	
	public void addGlobalParameter(Parameter param, AbstractClient abstractClient) {
		PuParameter pup = globals.newParams.getParameter(param);
		if(pup==null)
			pup = globals.addParameter(param);
		else {
			param.setVal(pup.getVal());
		}
		
		Set<AbstractClient> list = globalDependants.get(pup);
		if(list == null) {
			list = new HashSet<>();
			globalDependants.put(pup, list);
		}
		if(!list.contains(abstractClient))
			list.add(abstractClient);
	}

	public void rebuildGlobalsParameters() {
		globals.newParams.rebuild();	
	}

	public void updateGlobal(PuParameter p) {
		Set<AbstractClient> list = globalDependants.get(p);
		for(AbstractClient client:list) {
			client.parameterChanged(p);
		}

		
	}

	public void removeListerner(SSGeomListener proj) {
		listeners.remove(proj);
	}

	public void releaseGeometry(PgGeometryIf output) { 
		globals.addGeometry(output);
	}

}
