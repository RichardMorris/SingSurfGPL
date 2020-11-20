package org.singsurf.singsurf.clients;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

import javax.swing.JTextArea;

import org.nfunk.jep.ASTConstant;
import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.Node;
import org.singsurf.singsurf.Fractometer;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.LmsElementSetMaterial;
import org.singsurf.singsurf.LmsPointSetMaterial;
import org.singsurf.singsurf.LmsPolygonSetMaterial;
import org.singsurf.singsurf.PaSingSurf;
import org.singsurf.singsurf.PuIntChoice;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.SingSurfMessages;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.DefinitionReader;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.definitions.Parameter;
import org.singsurf.singsurf.definitions.ProjectComponents;
import org.singsurf.singsurf.geometries.GeomStore;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.geom.PgVectorField;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;
import jv.project.PjProject;
import jv.project.PjProject_IP;
import jv.rsrc.PsAuthorInfo;
import jv.rsrc.PsGeometryInfo;

/**
 * JavaView project which is sub classed by all the different LSMP project.
 * <p>
 * This class contains
 * 
 * @author Richard Morris
 */

public abstract class AbstractClient extends PjProject implements ItemListener, ActionListener {
	private static final long serialVersionUID = 1L;
	static final boolean PRINT_TIME = false;
	static final boolean PRINT_DEBUG = false;
	static final boolean JAVA_1_2 = false;
	static final boolean DEBUG_ECHO_INPUT = false;

	Frame m_frame;

	/** Material properties for faces. */
	LmsElementSetMaterial face_mat = null;
	/** Material properties for lines. */
	LmsPolygonSetMaterial line_mat = null;
	/** Material properties for points. */
	LmsPointSetMaterial point_mat = null;

	/**
	 * The name of the base geometry. For project where the full geometry consists
	 * of surfaces, lines and points this the names of the secondary geoms will be
	 * constructed from the baseName.
	 **/
	private String baseName;

	// ******************** Definitions of geometries and defaults

	/** Array of definitions */
	protected Definition lsmpDefs[] = null;

	/** The definition. */
	String s_def;
	// *********** Info Panel elements *************/

	/** The Calculate button. When pressed the surface will be calculated. */
	protected Button m_go;

	/** The main text area for the equation. **/
	protected JTextArea taDef;

	/** Checkbox for selecting whether colour info should be calculated. **/
//	protected Checkbox cbColour;

	/** Checkbox for selecting whether new objects should be created each time. **/
	protected Checkbox cbCreateNew;

	/**
	 * Checkbox for selecting whether the existing material properties should be
	 * retained
	 */
	protected Checkbox cbKeepMat;

	/** Checkbox for whether to draw faces */
	protected Checkbox cbShowFace;
	/** Checkbox for whether to draw edges */
	protected Checkbox cbShowEdge;
	/** Checkbox for whether to draw vertices */
	protected Checkbox cbShowVert;
	/** Checkbox for whether to draw curves */
	protected Checkbox cbShowCurves;
	/** Checkbox for whether to draw points */
	protected Checkbox cbShowPoints;
	/** Checkbox for whether to draw boundary */
	protected Checkbox cbShowBoundary;

	/** This contains the list of pre-defined definitions. */
	protected Choice chDefs;
	/** Choice for surface colours */
	protected Choice chSurfColours;
	/** Choice for curve colours */
	protected Choice chCurveColours;
	
	/** Choice for number of DP to show */
	protected Choice chDP;
	/** Choice for Scale of interger values */
	protected Choice chScale;

	protected Button bLoad;
	protected Button bSave;

	// ******************* Various other fields

	/** The project panel **/
	protected PjProject_IP m_IP;

	/** The main GeomStore object */
	protected GeomStore store;
	/** A reference to the containing viewer. */
	// protected PvViewerIf myViewer = null;
	/** A reference to the containing applet. */
	// protected Applet myApplet;

	protected Choice ch_auxSurf;
	protected Calculator calc;
	public LParamList newParams;
	PuVariable displayVars[];

	/** Whether changing variable or parameters force a redrawing of surface */
	protected boolean autoUpdate = true;
	public Font basicFont;

	/** Whether to do a fit display after constructing geoms. **/
	// public static boolean doFitDisplay = false;

	private AbstractClient() {
		super("LSMP surface Client");
	}

	/**
	 * Constructor which just passes project name to super class. *
	 * 
	 * @param store Store to create new geometries
	 * @param projName name of project
	 */

	public AbstractClient(GeomStore store, String projName) {
		super(projName);
		if (PRINT_DEBUG)
			System.out.println("PjLC constructor");
		this.store = store;
		// tf_tmpFile = new TextField(s_tmpFile);
		// tf_asurfURL = new TextField(s_asurfURL);

//		cbColour = new Checkbox("Draw in Colour", true);
		cbCreateNew = new Checkbox("Create new geoms", false);
		cbKeepMat = new Checkbox("Keep materials props", false);
		cbShowFace = new Checkbox("Show faces", true);
		cbShowEdge = new Checkbox("Show edges", false);
		cbShowVert = new Checkbox("Show vertices", false);
		cbShowCurves = new Checkbox("Show curves", true);
		cbShowPoints = new Checkbox("Show points", false);
		cbShowBoundary = new Checkbox("Show boundary", false);
		cbShowFace.addItemListener(this);
		cbShowEdge.addItemListener(this);
		cbShowVert.addItemListener(this);
		cbShowCurves.addItemListener(this);
		cbShowPoints.addItemListener(this);
		cbShowBoundary.addItemListener(this);
//		cbColour.addItemListener(this);

		chSurfColours = new Choice();
		for(String col:getPossibleSurfaceColours()) {
			chSurfColours.addItem(col);
		}
		chSurfColours.addItemListener(this);

		chCurveColours = new Choice();
		for(String col:getPossibleCurveColours()) {
			chCurveColours.addItem(col);
		}
		chCurveColours.addItemListener(this);

		m_go = new Button("Calculate");
		m_go.addActionListener(this);

		chDP = new Choice();
		for (int i = 0; i <= 9; ++i) {
			chDP.addItem("" + i);
		}
		chDP.select(1);
		chDP.addItemListener(this);

		chScale = new Choice();
		String num ="1";
		for (int i = 0; i <= 3; ++i) {
			chScale.addItem(num);
			num += "0";
		}
		chScale.select(0);
		chScale.addItemListener(this);

		// dRngConfig = new Dialog(PsConfig.getFrame(),true);
		// m_geom = new PgElementSet(3);
		// m_line_geom = new PgPolygonSet(3);
		// m_point_geom = new PgPointSet(3);

		if (getClass() == AbstractClient.class)
			init();
		if (PRINT_DEBUG)
			System.out.println("PjLC constructor done");

		basicFont = Font.decode(store.getParameter("Font"));
	}

	protected List<String> getPossibleSurfaceColours() {
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
		"Orange",
		"White");
	}

	String getFullPath(String fileName) {
		return store.getFullPath(fileName);
	}

	/**
	 * Initialise the project.
	 * 
	 * @param flag whether to include a choice for pre-selected defs.
	 */

	public void init(String defFileName) {
		if (PRINT_DEBUG)
			System.out.println("PjLC init");

		taDef = new JTextArea("", 30, 10);
		Font font = Font.decode(this.getParameter("Font"));
		Font tafont = Font.decode(store.getParameter("TextFont"));
		taDef.setFont(tafont);

		if (defFileName != null) {
			chDefs = new Choice();
			loadDefs(defFileName);
			chDefs.addItemListener(this);
//			chDefs.select(getDefaultDefName());
			chDefs.setFont(font);
		} else
			chDefs = null;
		bLoad = new Button("Load");
		bSave = new Button("Save");
		bLoad.addActionListener(this);
		bSave.addActionListener(this);
		super.init();
		if (PRINT_DEBUG)
			System.out.println("PjLC init done");
	}

	@Override
	public void init() {
		this.init(null);
	}

	/**
	 * Second stage initialisation, called after project registered with viewer.
	 **/

	public void init2() {
	}

	/**
	 * Start method is invoke when project is selected in the viewer.
	 */

	@Override
	public void start() {
		if (PRINT_DEBUG)
			System.out.println("PjLC start");
		super.start();
		if (PRINT_DEBUG)
			System.out.println("PjLC start done");
	}

	/**
	 * Stop method is invoke when project is de-selected in the viewer.
	 */

	@Override
	public void stop() {
		if (PRINT_DEBUG)
			System.out.println("PjLC stop");
		super.stop();
		if (PRINT_DEBUG)
			System.out.println("PjLC stop done");
	}

	/** Print a message with a timestamp. **/

	public static void timemessage(String str) {
		long t = System.currentTimeMillis() / 10;
		long tsec = (t / 100) % 100;
		long thund = t % 1000;
		Runtime r = Runtime.getRuntime();
		System.out.println(tsec + "." + thund + "\t" + str + "\tmem " + r.totalMemory() / 1024 + "K\tfree "
				+ r.freeMemory() / 1024 + "K");
	}

	public void loadDefs(String filename) {
		try {
			DefinitionReader ldr = new DefinitionReader(filename);
			ldr.read();
			for (Definition def : ldr.getDefs()) {
				chDefs.addItem(def.getName());
			}
		} catch (Exception e) {
			PsDebug.warning("Error reading definition file: " + e.getMessage());
		}
	}

	public Definition getDef(String name) {
		for (int i = 0; i < lsmpDefs.length; ++i)
			if (lsmpDefs[i].getName().equals(name))
				return lsmpDefs[i];
		return null;
	}

	public void calculate() {
		equationChanged(taDef.getText());
	}

	/**
	 * Update method of project. Responds to mouse events.
	 */

	@Override
	public boolean update(Object event) {
		if (PRINT_DEBUG)
			System.out.println("PjLC update");
		if (event != null && event == m_go) {
			calculate();
			return true;
		}
		return super.update(event);
	}

	/**
	 * Overwrite method of superclass to be able to react when new geometry is
	 * loaded from file by menu import. This method is invoked when the import menu
	 * is pressed.
	 */

	@Override
	public boolean addGeometry(PgGeometryIf geom) {
		PsDebug.message("AbstractClient.addGeometry(): new geometry added.");
		return super.addGeometry(geom);
	}

	/**
	 * Overwrite method of superclass to be able to react when new geometry is
	 * loaded from file by menu import. This method is invoked when the imported
	 * geometry is accepted.
	 */

	@Override
	public void selectGeometry(PgGeometryIf geom) {
		// PsDebug.message("PjAsurfClient.selectGeometry(): new geometry selected.");
		super.selectGeometry(geom);
	}

	/**
	 * Overwrite method of superclass to be able to react when new geometry is
	 * loaded from file by menu import. This method is invoked when the imported
	 * geometry is cancelled.
	 */

	@Override
	public void removeGeometry(PgGeometryIf geom) {
		// PsDebug.message("PjAsurfClient.removeGeometry(): geometry removed.");
		super.removeGeometry(geom);
	}

	// public abstract void removeOutpuGeometry(PgGeometryIf outGeom, boolean
	// rmDependants);

	/** Get the base name for the geometries **/

	protected final String getBaseName() {
		return baseName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jv.object.PsObject#setName(java.lang.String)
	 */
	@Override
	public void setName(String arg0) {
		if (calc != null)
			calc.setName(arg0);
		super.setName(arg0);
	}

	public void rename(String name) {
		if (calc != null)
			calc.setName(name);
		super.setName(name);
		this.getInfoPanel().setTitle(name);
	}

	/** Sets the defining equation **/

	public void setDisplayEquation(String def) {
		if (taDef != null)
			taDef.setText(def);
		else
			s_def = def;

	}

	/** Gets the defining equation **/

	public String getDisplayEquation() {
		if (taDef != null)
			return taDef.getText();
		else
			return s_def;
	}

	/**
	 * Loads a definition. Typically this should be overwritten to load a def of the
	 * specified type.
	 **/

	public boolean loadDefByName(String s) {
		PsDebug.warning("Loading definition by name not supported for current project");
		showStatus("Loading definition by name not supported for current project");
		return false;
	}

	/** Shows a Status message */

	public void showStatus(String str) {
		store.showStatus(str);
	}

	/** Gets an applet parameter */

	@Override
	public String getParameter(String str) {
		return store.getParameter(str);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();
		if (itSel == cbShowFace || itSel == cbShowEdge || itSel == cbShowVert 
				|| itSel == cbShowCurves || itSel == cbShowPoints 
				|| itSel == cbShowBoundary || itSel == chCurveColours) {
			setDisplayProperties();
		} else if (itSel == chDP) {
			int dp = chDP.getSelectedIndex();
			if (this.displayVars != null) {
				for (PuVariable var : this.displayVars) {
					var.setDP(dp);
				}
			}
			this.newParams.setDP(dp);
		}
		else if (itSel == chScale) {
			String  str = chScale.getSelectedItem();
			if (this.displayVars != null) {
				for (PuVariable var : this.displayVars) {
					var.setScale(Integer.parseInt(str));
				}
			}
		}

		setDefinitionOptions(getDefinition());
	}

	public void setDP(int dp) {
		if (this.displayVars != null) {
			for (PuVariable var : this.displayVars) {
				var.setDP(dp);
			}
		}
		chDP.select(dp);
	}

	public abstract void setDisplayProperties();

	/**
	 * Sets the display properties of the output geom according to state of
	 * different checkboxes. Gracefully ignores case when geom is null.
	 */
	public void setDisplayProperties(PgGeometryIf geom) {
		if (geom == null)
			return;
		if (geom instanceof PgElementSet) {
			((PgElementSet) geom).showElements(cbShowFace.getState());
			((PgElementSet) geom).showEdges(cbShowEdge.getState());
			((PgElementSet) geom).showVertices(cbShowVert.getState());
			((PgElementSet) geom).showBoundaries(cbShowBoundary.getState());

		} else if (geom instanceof PgPolygonSet) {
			((PgPolygonSet) geom).showPolygons(cbShowCurves.getState());
			((PgPolygonSet) geom).showVertices(cbShowVert.getState());
			setColour((PgPolygonSet) geom, chCurveColours.getSelectedItem());

		} else if (geom instanceof PgPointSet) {
			((PgPointSet) geom).showVertices(cbShowPoints.getState());
		}
		store.geomApperenceChanged(geom);
	}

//	public final String getProgramName() {
//		return
//	}

	/*
	 * public PgGeometryIf getGeometry(String name) { return null; }
	 */
	protected void refreshParams() {
		newParams.reset();
		int size = calc.getNParam();
		for (int i = 0; i < size; ++i) {
			
			Parameter param = calc.getParam(i);
			if(param.getName().startsWith("global_")) {
				store.addGlobalParameter(param,this);
				calc.setParamValue(param.getName(), param.getVal());
			} else {
				newParams.addParameter(param);
			}
		}
		newParams.rebuild();
		store.rebuildGlobalsParameters();
	}

	protected void setColour(PgPolygonSet curve, String s) {
		if (s.equals("None")) {
			curve.showPolygonColors(false);
			return;
		}
		curve.showPolygonColors(false);
		// for(int i=0;i<curve.getNumPolygons();++i)
		// {
		curve.setGlobalPolygonColor(getColor(s));
		// }
	}

	/**
	 * @param curve
	 * @param s
	 */
	protected Color getColor(String s) {
		if (s.equals("Red"))
			 return Color.red;
		if (s.equals("Green"))
			return Color.green;
		if (s.equals("Blue"))
			return Color.blue;
		if (s.equals("Cyan"))
			return Color.cyan;
		if (s.equals("Magenta"))
			return Color.magenta;
		if (s.equals("Yellow"))
			return Color.yellow;
		if (s.equals("Black"))
			return Color.black;
		if (s.equals("White"))
			return Color.white;
		if (s.equals("Grey"))
			return Color.gray;
		if (s.equals("Grey"))
			return Color.gray;
		if (s.equals("Orange"))
			return Color.ORANGE;
		return Color.black;
	}

	protected void setColour(PgVectorField curve, String s) {
		curve.showIndividualMaterial(true);
		curve.setGlobalVectorColor(getColor(s));
	}

	public Definition getDefinition() {
		return calc.getDefinition();
	}

	public abstract List<PgGeometryIf> getOutputGeoms();

	/** Save this projects definition to a file */
	public void save(String filename, boolean append) {
		Definition def = getDefinition();

		try {
			FileWriter fw = new FileWriter(filename, append);
			fw.append(def.toString());
			fw.close();
		} catch (IOException e) {
			showStatus("Failed to write to " + filename);
		}
	}

	public abstract ProjectComponents getProjectComponents();

	public abstract void loadProjectComponents(ProjectComponents comp, PaSingSurf ss);

	/** Called when the displayed equation is changed */
	public void equationChanged(String text) {
		System.out.println("equationChanged");
		calc.setEquation(text);
		refreshParams();
		rebuildClient();
		store.geomDefinitionChanged(this, calc);
		calcGeoms();
	}

	/**
	 * Client specific action to be performed after the equation has changed and the
	 * calculator has been rebuilt.
	 */
	public void rebuildClient() {
	}

	/** Called when a displayed parameter is changed */
	public boolean parameterChanged(PuParameter p) {
//		System.out.println("parmChanged " + p.getName() + ":" + p.getVal());
		calc.setParamValue(p.getName(), p.getVal());
		if (this.autoUpdate)
			calcGeoms();
		return true;
	}

	public abstract void calcGeoms();

	public Calculator getCalculator() {
		return calc;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == bLoad)
			loadDef();
		else if (source == bSave)
			saveDef();
		else if (source == m_go) {
			calculate();
		}

	}

	public abstract void setDefinitionOptions(Definition def);

	public final void saveDef() {
		saveAppendDef(false);
	}

	public void saveAppendDef(boolean append) {
		this.setDefinitionOptions(getDefinition());
		Definition def = calc.getDefinition();
		FileDialog fd = new FileDialog(m_frame, (append? "Append project " : "Save project ")+getName(), (append ? FileDialog.LOAD : FileDialog.SAVE));
		fd.setVisible(true);
		if(fd.getDirectory() == null || fd.getFile() == null) {
			System.out.println("File dialog canceled");
			return;
		}
		File f = new File(fd.getDirectory(), fd.getFile());

		setDefinitionOptions(def);
		String txt = def.toString();
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f.getPath(),append), "utf-8"));
			writer.write(txt);
		} catch (IOException ex) {
			System.out.println(ex);
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception ex) {
				/* ignore */}
		}
	}

	abstract public void loadDefinition(Definition newdef);

	protected void loadDef() {
		FileDialog fd = new FileDialog(m_frame, "Load", FileDialog.LOAD);
		fd.setVisible(true);
		if(fd.getDirectory() == null || fd.getFile() == null) {
			System.out.println("File dialog canceled");
			return;
		}

		String filename = fd.getDirectory() + fd.getFile();

		try {
			DefinitionReader reader = new DefinitionReader(filename);
			reader.read();
			Definition def = reader.getDefs().get(0);
			System.out.println(def);
			loadDefinition(def);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void setFrame(Frame frame) {
		this.m_frame = frame;
	}

	protected void loadFromDefOption(Definition def, String name, Fractometer fract) {
		Option option = def.getOption(name);
		if(option==null) return;
		Double val = option.getDoubleVal();
		fract.setValue(val);
	}

	protected void loadFromDefOption(Definition def, String name, Choice choice) {
		Option option = def.getOption(name);
		if(option==null) return;
		choice.select(option.getStringVal());
	}

	protected void loadFromDefOption(Definition def, String name, PuIntChoice frac) {
		Option option = def.getOption(name);
		if(option!=null)
			frac.setVal(option.getIntegerVal());
	}

	protected void loadFromNumber(Number num, PuIntChoice fract) {
		if(num!=null)
			fract.setVal(num.intValue());
	}
	protected void loadFromNumber(Number num, Checkbox cb) {
		if(num!=null)
			cb.setState(num.intValue() >0);
	}

	protected void setCheckboxStateFromOption(Checkbox cb, Definition def, String optName) {
		Option sfopt = def.getOption(optName);
		if (sfopt != null)
			cb.setState(sfopt.getBoolVal());
	}

	protected void setGeometryInfo(PgGeometryIf geom) {
		setGeometryInfo(geom,null);
	}
	
	protected void setGeometryInfo(PgGeometryIf geom,PgGeometryIf input) {
		PsGeometryInfo info = new PsGeometryInfo();
		info.setDetail(getDetails(input));
		info.setAbstract(getAbstract());
		info.setSoftware("SingSurf https://singsurf.org/ ");
		geom.setGeometryInfo(info);
		PsAuthorInfo author = new PsAuthorInfo();
		author.setNumAuthors(1);
		author.setInfo(0, PsAuthorInfo.FIRST_NAME, "Richard");
		author.setInfo(0, PsAuthorInfo.LAST_NAME, "Morris");
		author.setInfo(0, PsAuthorInfo.EMAIL, "rich@singsurf.org");
		author.setInfo(0, PsAuthorInfo.URL, "https://singsurf.org/");
		author.setInfo(0, PsAuthorInfo.ADDRESS, "1 Lerryn View\n"
				+ "Lerryn\n"
				+ "Lostwithiel\n"
				+ "Cornwall\n"
				+ "PL22 0QJ\n"
				+ "England");
		geom.setAuthorInfo(author);
	}

	/**
	 * 
	 * @return
	 */
	protected  String getAbstract() {
		String name = this.getDefinition().getType().toString();
		return 	SingSurfMessages.getString(name+".longName");
	}

	/**
	 * Details added to the JavaView Geometry info
	 * @param input used by subclasses
	 * @return
	 */
	protected String getDetails(PgGeometryIf input) {
		return getDefinition().getJSON();
	}

	/**
	 * 
	 */
	protected void setStandardControlsFromOptions() {
		Definition def = getDefinition();
		setCheckboxStateFromOption(cbShowFace,def,"showFace");
		setCheckboxStateFromOption(cbShowEdge,def,"showEdge");
		setCheckboxStateFromOption(cbShowVert,def,"showVert");
		setCheckboxStateFromOption(cbShowCurves,def,"showCurve");
		setCheckboxStateFromOption(cbShowPoints,def,"showPoint");
		setCheckboxStateFromOption(cbShowVert,def,"showVert");
		setCheckboxStateFromOption(cbShowBoundary,def,"showBoundary");
	}

	protected void useIntSetting(Node n, String tag, IntConsumer setter) {
		if (tag.equals(((ASTVarNode) n.jjtGetChild(0)).getName()) )
			setter.accept( ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).intValue());
	}

	protected void useDoubleSetting(Node n, String tag, DoubleConsumer setter) {
		if (tag.equals(((ASTVarNode) n.jjtGetChild(0)).getName()) )
			setter.accept( ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).doubleValue());
	}

	protected int getIntSetting(Node n, String tag, int defaultVal) {
		if (tag.equals(((ASTVarNode) n.jjtGetChild(0)).getName()) )
			return  ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).intValue();
		return defaultVal;
	}

} // end of class
