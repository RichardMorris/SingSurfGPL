/* @author rich
 * Created on 30-Jun-2003
 *
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package org.singsurf.singsurf;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.NumberFormat;

import javax.imageio.ImageIO;

import jv.object.PsConfig;
import jv.object.PsDebug;
import jv.object.PsPanel;
import jv.project.PvCameraIf;
import jv.project.PvDisplayIf;
import jv.project.PvViewerIf;
import jv.vecmath.PdVector;
import jv.vecmath.PuVectorGeom;

import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.definitions.Parameter;

import com.singularsys.jep.JepException;
import com.singularsys.jep.PrintVisitor;
import com.singularsys.jep.PrintVisitor.PrintRulesI;
import com.singularsys.jep.parser.ASTConstant;
import com.singularsys.jep.parser.Node;

/**
 * A class to calculate raytraced images of algebraic surfaces. Provides an
 * interface to the surf raytrace program.
 * 
 * To use this class do
 * 
 * <pre>
 * class foo implement LsmpDef.LsmpDefCreaterI {
 * RayTrace myRaytracier = new RayTrace();
 * 
 * void setup()
 * {
 * String webserverDirUrl = "http://localhost/cgi-bin/";
 * String executableServerDir = "bin/"; // unix
 * String executableServerDir = "bin\\"; // windows
 * 
 * if executableServerDir is not null it will be used
 * otherwise the webserver will be used.
 *
 * String webserverExtension = ""; // unix
 * String webserverExtension = "exe"; // windows
 * String executableServerExtension = "exe"; // windows
 * String executableServerExtension = ""; // unix
 * 
 * myRaytracier.setCGIServer(webserverDirURL,webserveExtension,
 *			executableServerDir,executableServerExtension);
 *
 * PsViewerIf myViewer; 
 * myRaytracier.setViewer(myViewer);
 * 
 * LsmpDef.LsmpDefCreaterI bar = this;
 * myRaytracier.setLsmpClient(bar);
 * }
 * 
 * // these two methods calculate the surface and show the options 
 * myRaytracier.calculate();
 * myRaytracier.showOptions();
 *
 * // methods needed by interface 
 *	public LsmpDef getLsmpDef() { ... };
 *
 * // a simple implementation of getLsmpDef might be
 * public LsmpDef getLsmpDef()
 * {
 *  String xlmFormatForDef = ...;
 *	return LsmpDef.makeLsmpDef(makeCGIstring());
 * }
 * </pre>
 *
 * @author Rich Morris Created on 30-Jun-2003
 */
public class RayTrace {
    static final boolean PRINT_TIME = true;
    static final boolean PRINT_DEBUG = true;
    static final boolean DEBUG_ECHO_INPUT = false;

    /** The URL of the Surfaces webserver. */
    // String server = "http://localhost/scripts/lsmp/asurf/asurfCV3.exe";
    String server = "/Users/rich/surf-1.0.6/examples/rjm/surfwrap.sh";
    // int serverType = AbstractCGIClient.EXEC_SERVER;
    /** Name of the Server program. */
    private static final String myServer = "surfwrap";

    PvViewerIf myViewer = null;
    /**
     * The client this works with. Should have a function makeCgiString
     */
    AbstractClient myClient = null;
    Applet myApplet;
    /** The definition of the surface */
    // LsmpDef myDef;
    Calculator calc;
    /** The image created. */
    Image myImage;

    Frame rayFrame = null;
    Canvas rayCanvas = null;
    RayDialog rayDialog = null;

    /** The lighting parameters to use. */
    protected String lighting = "illumination = ambient_light + diffuse_light + reflected_light + transmitted_light;\n"
	    + "ambient      = 40;		// ambient light %\n"
	    + "diffuse      = 80;		// defuse light %\n"
	    + "reflected    = 60;		// refleced (specular) light %\n"
	    + "smoothness   = 60;	// roughness of surface %; 0 = shiney\n"
	    + "transmitted  = 20;		// transmitted light % (doesn't seem to have much effect)\n"
	    + "transparence = 10;	// transparancy of surface %; 0 = opaque\n"
	    + "thickness = 20;		// thickness of surface % (affects trsmitted light)\n"
	    + "background_red=200; background_green=200; background_blue=255;\n"
	    + "surface_red=230; surface_green=80; surface_blue=0;\n"
	    + "inside_red=200; inside_green=200; inside_blue=0;\n";

    /** Transformations from user. */
    protected String userTrans;
    /** Contains commands to draw surface. */
    protected String epilogue = "// Do not specify a filename!\n"
	    + "// It is automatically added by the server program.\n" + "draw_surface;\n" + "color_file_format=jpg;\n"
	    + "save_color_image;\n";

    public RayTrace() {
	rayDialog = new RayDialog();
    }

    /**
     * Returns the last created image.
     */
    public Image getImage() {
	return myImage;
    }

    /**
     * Creates the frame and canvas to display the image.
     */
    private void createRayFrame() {
	if (rayFrame != null)
	    return;

	Toolkit tools = Toolkit.getDefaultToolkit();

	rayFrame = new Frame("Raytraced image");
	rayFrame.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		rayFrame.dispose();
		rayFrame = null;
	    }
	});
	if (PsConfig.isApplication())
	    rayFrame.setIconImage(tools.getImage("images/icon.gif"));
	else {
	    try {
		rayFrame.setIconImage(tools.getImage(new URL(PsConfig.getApplet().getCodeBase(), "images/icon.gif")));
	    } catch (MalformedURLException e1) {
		System.out.println(e1.getMessage());
	    }
	}

	rayCanvas = new Canvas() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void paint(Graphics g) {
		if (myImage != null)
		    g.drawImage(myImage, 0, 0, null);
		else
		    System.out.println("null image");
	    }
	};

	Insets insets = rayFrame.getInsets();
	PvDisplayIf disp = myViewer.getDisplay();
	// PvCameraIf cam = disp.getCamera();
	rayFrame.setBounds(0, 0, disp.getSize().width + insets.left + insets.right,
		disp.getSize().height + insets.bottom + insets.top);
	rayFrame.add(rayCanvas);
	rayFrame.pack();
    }

    /**
     * Called every time there is a new image to display. Reshapes the frame to fit
     * the image.
     */
    public void displayImage() {
	if (rayFrame == null)
	    createRayFrame();

	/* Explicitly wait for the image to load */
	if (PRINT_TIME)
	    System.out.println("Waiting for image to load");
	MediaTracker m = new MediaTracker(rayFrame);
	m.addImage(myImage, 1);
	try {
	    m.waitForAll();
	} catch (InterruptedException e) {
	    PsDebug.message("Loading of the image was interrupted");
	}
	if (PRINT_TIME)
	    System.out.println("Image now loaded " + myImage.getWidth(null));

	Insets insets = rayFrame.getInsets();
	// System.out.println("Insets: "+insets);

	int width = myImage.getWidth(null);
	int height = myImage.getHeight(null);
	if (width == -1 || height == -1) {
	    PvDisplayIf disp = myViewer.getDisplay();
	    // PvCameraIf cam = disp.getCamera();
	    rayFrame.setSize(disp.getSize().width + insets.left + insets.right,
		    disp.getSize().height + insets.top + insets.bottom);
	} else
	    rayFrame.setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);

	rayFrame.setVisible(true);
	rayCanvas.repaint();
    }

    /**
     * Show the advanced panel, with many options to tweek.
     */
    public void showOptions() {

	// setLsmpDef(myClient.getDefinition());
	rayDialog.show();
    }

    /**
     * Dialog window to allow fine configuration of Surf.
     */
    private class RayDialog extends Frame implements ActionListener {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	TextArea taLighting = new TextArea(lighting, 9, 30);
	TextArea taTransform = new TextArea(9, 30);
	TextArea taEpilogue = new TextArea(4, 30);
	String oldLighting = null;
	String oldEpilogue = null;

	Choice clippingType = new Choice();
	Button bOk = new Button("Close+Save");
	Button bSaveImage = new Button("Save Image");
	Button bCancle = new Button("Cancle");
	Button bRayTrace = new Button("Raytrace");
	Button bReload = new Button("Reload Transformation");

	RayDialog() {
	    super("Surf RayTrace Options");
	    clippingType.add("none");
	    clippingType.add("sphere (inner)");
	    clippingType.add("sphere (outer)");
	    clippingType.add("sphere (equal volume)");
	    clippingType.add("cube");
	    clippingType.add("cylinder");
	    clippingType.add("tetrahedron");
	    clippingType.add("octahedron");
	    clippingType.add("dodecahedron");
	    clippingType.add("icosahedron");

	    clippingType.select("sphere (equal volume)");

	    PsPanel p1 = new PsPanel();
	    add(p1);
	    p1.addSubTitle("Lighting and Material");
	    p1.add(taLighting);
	    p1.addSubTitle("Transformation/Clipping/Surface equation");
	    Panel p3 = new Panel(new FlowLayout(FlowLayout.LEFT, 1, 0));
	    p3.add(new Label("Clipping:"));
	    p3.add(clippingType);
	    p3.add(bReload);
	    p1.add(p3);
	    p1.add(taTransform);
	    p1.addSubTitle("Epilogue");
	    p1.add(taEpilogue);
	    p1.add(new Label("For more details on Surf commands: see http://surf.sourceforge.net/"));
	    Panel p2 = new Panel(new FlowLayout(FlowLayout.LEFT, 1, 0));
	    p2.add(bRayTrace);
	    p2.add(bOk);
	    p2.add(bSaveImage);
	    p2.add(bCancle);
	    p1.add(p2);
	    add(p1);
	    pack();
	    bOk.addActionListener(this);
	    bSaveImage.addActionListener(this);
	    bCancle.addActionListener(this);
	    bRayTrace.addActionListener(this);
	    bReload.addActionListener(this);

	    addWindowListener(new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
		    lighting = oldLighting;
		    epilogue = oldEpilogue;
		    setVisible(false);
		}
	    });

	    Toolkit tools = Toolkit.getDefaultToolkit();

	    if (PsConfig.isApplication())
		setIconImage(tools.getImage("images/icon.gif"));
	    else {
		try {
		    setIconImage(tools.getImage(new URL(PsConfig.getApplet().getCodeBase(), "images/icon.gif")));
		} catch (MalformedURLException e1) {
		    System.out.println(e1.getMessage());
		}
	    }
	}

	/**
	 * Handels button presses. bOk saves lighting and eppilog and closes window.
	 * bCancle does not save and closes window. bRayTrace recalculates surfaces
	 * using data from dialog. bReload reloads the transformation part to keep in
	 * cinc with JV.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
	    Object source = event.getSource();

	    if (source == bOk) {
		// Save the data
		lighting = taLighting.getText();
		userTrans = taTransform.getText();
		epilogue = taEpilogue.getText();
		setVisible(false);
	    } else if (source == bCancle) {
		lighting = oldLighting;
		epilogue = oldEpilogue;
		setVisible(false);
	    } else if (source == bRayTrace) {
		// Save the data
		lighting = taLighting.getText();
		userTrans = taTransform.getText();
		epilogue = taEpilogue.getText();
		calculate(true);
	    } else if (source == bSaveImage) {
		// Save the data
		FileDialog fd = new FileDialog(this, "Save Raytracied Image", FileDialog.SAVE);
		fd.setVisible(true);
		if(fd.getDirectory() == null || fd.getFile() == null) {
			System.out.println("File dialog canceled");
			return;
		}

		String s = fd.getFile();
		String d = fd.getDirectory();
		if (s == null)
		    return;
		File f = new File(d, s);
		String type = getType(f.getName());
		try {
		    Image img = myImage;
		    BufferedImage bImg = new BufferedImage(img.getWidth(null), img.getHeight(null),
			    BufferedImage.TYPE_INT_RGB);
		    Graphics g = bImg.getGraphics();
		    g.drawImage(img, 0, 0, null);
		    ImageIO.write(bImg, type, f);

		} catch (IOException e) {
		    System.out.println(e);
		}

	    } else if (source == bReload) {
		// Save the data
		// setLsmpDef(myClient.getDefinition());
		taTransform.setText(makeTranformString());
	    }
	}

	private String getType(String name) {
	    String lcname = name.toLowerCase();
	    if (lcname.endsWith(".jpg"))
		return "jpg";
	    if (lcname.endsWith(".png"))
		return "png";
	    if (lcname.endsWith(".bmp"))
		return "bmp";
	    if (lcname.endsWith(".jpeg"))
		return "jpg";
	    return null;
	}

	/**
	 * Displays the dialog. Loads the dialog with relavant info.
	 */
	@Override
	public void show() {
	    oldLighting = lighting;
	    taLighting.setText(lighting);
	    taTransform.setText(makeTranformString());
	    oldEpilogue = epilogue;
	    taEpilogue.setText(epilogue);

	    this.setVisible(true);
	}
    }

    /**
     * Sets the name of the web server to use.
     * 
     * @param serverType
     *                            obsolete number
     * @param serverDir
     *                            base of the URL for the server.
     * @param serverExtension
     *                            extension for the server.
     * @param execDir
     *                            base of the URL for the executable.
     * @param execExtension
     *                            extension for the executable. If execDir is not
     *                            null then there the program will be run by running
     *                            (exec) a local program rather than by using a
     *                            server on the internet.
     * 
     *                            The name of server is serverDir + progname +
     *                            serverExtension. The name of executable is execDir
     *                            + progname + execExtension.
     */

    public void setCGIServer(int serverType, String serverDir, String serverExtension) {
	if (serverDir.endsWith("/") || serverDir.endsWith("\\"))
	    server = serverDir + myServer + serverExtension;
	else
	    server = serverDir + "/" + myServer + serverExtension;
	// PsDebug.message("setCGIServer " + s_asurfURL + " exec " + s_asurfExec );
    }

    /**
     * Registers the viewer.
     */
    public void setViewer(PvViewerIf viewer) {
	myViewer = viewer;
	if(myViewer!=null)	
		myApplet = myViewer.getApplet();
    }

    /**
     * Registers the client to get surface def from.
     */
    public void setLsmpClient(AbstractClient aSurf) {
	myClient = aSurf;
    }

    /**
     * Constructs the Transformation String from JavaView and SingSurf parameters.
     * 
     * <pre>
     * height and width are height and width of camera
     * origin = - camera.getInterest
     * rot_y = -ang[0]; rot_x = ang[1]; rot_z = pi +ang[2];
     * where ang[] = PuVectorGeom.frameToStandardFrame(v1,v2,v3);
     * v1 = v3^v2;
     * v3 = -viewDir;
     * v2 = upDir
     * scale = 1/(10*2) * camera.getDist();
     * specZ = scale/tan(FoV/2); FoV = pi * cam.getFieldOfView() / 180 
     * FoV = 0 -> parallel perspective
     * For Sphere clipping translate mid point to world coords
     *  u1 = origin - center of object
     *  u2 = rotate u1 around Y by ang[0]
     *  u3 = rotate u2 around X by ang[1]
     *  u4 = rotate u3 around Z by ang[2]-pi
     *  center = -u4/scale
     *	clipMult = 1 (inner) or sqrt(3) (outer) or pow(6/pi,1/3) (equal volume)
     *    (this is set in makeCgiString)
     *  radius = clipMult * rad/scale
     *  where rad = max(xh-xl,yh-yl,zh-zl)/2
     * Otherwise
     *  center = center of object
     *  radius = clipMult * max(xh-xl,yh-yl,zh-zl)/2
     * The clipType variable (set in makeCgiString) is used to 
     * select which center/radius to use.
     * Also sets
     * clip_back=-100; clip_front=100;
     * and sets
     * surface = definition of surface
     * </pre>
     */

    public String makeTranformString() {
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(6);
	nf.setGroupingUsed(false);

	PvDisplayIf disp = myViewer.getDisplay();
	PvCameraIf cam = disp.getCamera();
	java.awt.Dimension size = disp.getSize();
	StringBuffer buf = new StringBuffer();
	calc = this.myClient.getCalculator();
	buf.append("width=" + size.width + "; height=" + size.height + ";\n");
	// calc.getDefVariable(0).getMin();
	double xlow = calc.getDefVariable(0).getMin();
	double xhigh = calc.getDefVariable(0).getMax();
	double ylow = calc.getDefVariable(1).getMin();
	double yhigh = calc.getDefVariable(1).getMax();
	double zlow = calc.getDefVariable(2).getMin();
	double zhigh = calc.getDefVariable(2).getMax();
	double xmid = (xhigh + xlow) / 2;
	double ymid = (yhigh + ylow) / 2;
	double zmid = (zhigh + zlow) / 2;
	double xrad = xhigh - xmid;
	double yrad = yhigh - ymid;
	double zrad = zhigh - zmid;
	double rad = xrad < yrad ? xrad : yrad;
	rad = rad < zrad ? rad : zrad;

	PdVector origin = cam.getInterest();
	buf.append("// Origin = - camera.getInterest\n");
	buf.append("origin_x=" + nf.format(-origin.getEntry(0)) + ";\n");
	buf.append("origin_y=" + nf.format(-origin.getEntry(1)) + ";\n");
	buf.append("origin_z=" + nf.format(-origin.getEntry(2)) + ";\n");

	PdVector upVec = PdVector.copyNew(cam.getUpVector());
	upVec.normalize();
	PdVector viewDir = PdVector.copyNew(cam.getViewDir());
	viewDir.multScalar(-1);
	viewDir.normalize();
	PdVector crossVec = PdVector.crossNew(viewDir, upVec);
	double rotAngs[] = PuVectorGeom.frameToStandardFrame(crossVec, upVec, viewDir);

	buf.append("// rot_y = -ang[0]; rot_x = ang[1]; rot_z = pi +ang[2]; \n");
	buf.append("// where ang[] = PuVectorGeom.frameToStandardFrame(v1,v2,v3)\n");
	buf.append("// v1 = v3^v2\n");
	buf.append("// v3 = -viewDir = (" + nf.format(viewDir.getEntry(0)) + "," + nf.format(viewDir.getEntry(1)) + ","
		+ nf.format(viewDir.getEntry(2)) + ")\n");
	buf.append("// v2 = upDir = (" + nf.format(upVec.getEntry(0)) + "," + nf.format(upVec.getEntry(1)) + ","
		+ nf.format(upVec.getEntry(2)) + ")\n");
	buf.append("rot_y=" + nf.format(-rotAngs[0]) + ";\n");
	buf.append("rot_x=" + nf.format(rotAngs[1]) + ";\n");
	buf.append("rot_z=" + nf.format(rotAngs[2] + Math.PI) + ";\n");

	// when dist==2 [-1,1] fits screen so scale factor 1/10

	double scale = 0.05 * Math.abs(cam.getDist());
	buf.append("// Scale = 1/(10*2) * camera.getDist(); getDist= " + nf.format(cam.getDist()) + ";\n");
	buf.append("scale_x=" + nf.format(scale) + ";\n");
	buf.append("scale_y=" + nf.format(scale) + ";\n");
	buf.append("scale_z=" + nf.format(scale) + ";\n");

	double fov = cam.getFieldOfView();
	double specZ = scale / Math.tan(Math.PI * fov / 360.0);
	buf.append("// specZ = scale/tan(FoV/2); FoV=" + nf.format(fov) + "degrees\n");
	if (fov > 0.01) {
	    buf.append("perspective=central;\n");
	    buf.append("spec_z=" + nf.format(specZ) + ";\n");
	} else {
	    buf.append("// FoV = 0 -> parallel perspective\n");
	    buf.append("perspective=parallel;\n");
	    specZ = 0.0;
	}

	// For a sphere the center and radius are in world coordinates
	PdVector mid = new PdVector(-xmid + origin.getEntry(0), -ymid + origin.getEntry(1), -zmid + origin.getEntry(2));
	PdVector xAxis = new PdVector(1.0, 0.0, 0.0);
	PdVector yAxis = new PdVector(0.0, 1.0, 0.0);
	PdVector zAxis = new PdVector(0.0, 0.0, 1.0);
	PdVector afterY = new PdVector(3);
	PdVector afterX = new PdVector(3);
	PdVector afterRot = new PdVector(3);
	PuVectorGeom.rotatePointAroundVector(afterY, mid, yAxis, rotAngs[0]);
	PuVectorGeom.rotatePointAroundVector(afterX, afterY, xAxis, rotAngs[1]);
	PuVectorGeom.rotatePointAroundVector(afterRot, afterX, zAxis, rotAngs[2] - Math.PI);

	buf.append("// for Sphere clipping translate mid point to world coords\n");
	buf.append("// u1 = origin - center of object = (" + nf.format(mid.getEntry(0)) + ","
		+ nf.format(mid.getEntry(1)) + "," + nf.format(mid.getEntry(2)) + ")\n");
	buf.append("// u2 = rotate u1 around Y by ang[0] = (" + nf.format(afterY.getEntry(0)) + ","
		+ nf.format(afterY.getEntry(1)) + "," + nf.format(afterY.getEntry(2)) + ")\n");
	buf.append("// u3 = rotate u2 around X by ang[1] = (" + nf.format(afterX.getEntry(0)) + ","
		+ nf.format(afterX.getEntry(1)) + "," + nf.format(afterX.getEntry(2)) + ")\n");
	buf.append("// u4 = rotate u3 around Z by ang[2]-pi = (" + nf.format(afterRot.getEntry(0)) + ","
		+ nf.format(afterRot.getEntry(1)) + "," + nf.format(afterRot.getEntry(2)) + ")\n");
	buf.append("// center = -u4/scale\n");
	buf.append("double sphCen_x=" + nf.format((-afterRot.getEntry(0)) / scale) + ";\n");
	buf.append("double sphCen_y=" + nf.format((-afterRot.getEntry(1)) / scale) + ";\n");
	buf.append("double sphCen_z=" + nf.format((-afterRot.getEntry(2)) / scale) + ";\n");
	buf.append("// radius = factor rad/scale\n");
	buf.append("// rad = max(xh-xl,yh-yl,zh-zl)/2 = " + nf.format(rad) + "\n");
	buf.append("// clipMult = 1 (inner/cube) or sqrt(3) (outer) or pow(6/pi,1/3) (equal volume/icosohedron etc)\n");
	buf.append("double sphRad=clipMult*" + nf.format(rad / scale) + ";\n");

	buf.append("// Otherwise Clip center = center of object\n");
	buf.append("double cubeCen_x=" + nf.format(xmid) + ";\n");
	buf.append("double cubeCen_y=" + nf.format(ymid) + ";\n");
	buf.append("double cubeCen_z=" + nf.format(zmid) + ";\n");
	buf.append("// radius = max(xh-xl,yh-yl,zh-zl)/2 = " + nf.format(rad) + "\n");
	buf.append("double cubeRad=clipMult*" + nf.format(rad) + ";\n");
	buf.append("// clipType == 1 for sphere, 0 otherwise\n");
	buf.append("center_x=clipType * sphCen_x + (1-clipType) * cubeCen_x;\n");
	buf.append("center_y=clipType * sphCen_y + (1-clipType) * cubeCen_y;\n");
	buf.append("center_z=clipType * sphCen_z + (1-clipType) * cubeCen_z;\n");
	buf.append("radius=clipType * sphRad + (1-clipType) * cubeRad;\n");

	buf.append("clip_back=-100; clip_front=100;\n");
	calc.getJep().getPrintVisitor().addSpecialRule(calc.getJep().getOperatorTable().getPower(), new PrintRulesI() {

	    /**
	    		     * 
	    		     */
	    private static final long serialVersionUID = 1L;

	    // @Override
	    @Override
	    public void append(Node node, PrintVisitor pv) throws JepException {
		Node lhs = node.jjtGetChild(0);
		if (lhs.jjtGetNumChildren() > 0)
		    pv.append("(");
		node.jjtAccept(pv, lhs);
		ASTConstant rhs = (ASTConstant) node.jjtGetChild(1);
		Double d = (Double) rhs.getValue();
		if (lhs.jjtGetNumChildren() > 0)
		    pv.append(")");
		pv.append("^" + d.intValue());
	    }
	});
	for (Parameter p : calc.getParams()) {
	    buf.append("double " + p.getName() + "=" + p.getVal() + ";\n");
	}
	int nEqn = calc.getRawEqns().size();
	for (int i = 0; i < nEqn - 1; ++i) {

	    Node eqn = calc.getRawEqns().get(i);
	    if (eqn.jjtGetChild(1) instanceof ASTConstant)
		buf.append("double " + calc.getJep().toString(eqn) + ";\n");
	    else
		buf.append("poly " + calc.getJep().toString(eqn) + ";\n");
	}
	buf.append("surface = " + calc.getJep().toString(calc.getRawEqns().get(nEqn - 1)) + ";\n");

	return buf.toString();
    }

    /** whether to use the user defined transformation or not. **/
    private boolean useUserTrans = false;

    /**
     * Constructs the String with the commands to send to surf. Concatinates the
     * lighting info, transformation info and epilogue Also sets some variables
     * concerned with clipping
     * 
     * <pre>
     * clip=sphere or cube or tetrahedron etc.
     * clipMult = 1 for cube/sphere(inner)
     * clipMult = sqrt(3) for sphere(outer)
     * clipMult = pow(pow(6/PI,1/3) for sphere(equalVolume) and other types
     * clipType = 1 for sphere types
     * clipType = 0 otherwise
     * </pre>
     */
    public String makeCGIstring() {
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(6);
	nf.setGroupingUsed(false);
	StringBuffer buf = new StringBuffer();

	buf.append(lighting);

	String clipType = rayDialog.clippingType.getSelectedItem();
	if (clipType.startsWith("sphere")) {
	    buf.append("clip=sphere;\n");
	    buf.append("double clipType=1.0;\n");
	} else {
	    buf.append("clip=" + clipType + ";\n");
	    buf.append("double clipType=0.0;\n");
	}

	if (clipType.equals("sphere (inner)"))
	    buf.append("double clipMult=" + nf.format(1.0) + ";\n");
	else if (clipType.equals("sphere (outer)"))
	    buf.append("double clipMult=" + nf.format(Math.sqrt(3)) + ";\n");
	else if (clipType.equals("sphere (equal volume)"))
	    buf.append("double clipMult=" + nf.format(Math.pow(6.0 / Math.PI, 1.0 / 3.0)) + ";\n");
	else if (clipType.equals("cube"))
	    buf.append("double clipMult=" + nf.format(1.0) + ";\n");
	else
	    buf.append("double clipMult=" + nf.format(Math.pow(6.0 / Math.PI, 1.0 / 3.0)) + ";\n");

	if (useUserTrans)
	    buf.append(userTrans);
	else
	    buf.append(makeTranformString());

	buf.append(epilogue);

	return buf.toString();
    }

    /** calculates the surface. */

    public void calculate() {
	calculate(false);
    }

    public void calculate(boolean flag) {
	useUserTrans = flag;
	// setLsmpDef(myClient.getDefinition());
//	if (serverType == AbstractCGIClient.EXEC_SERVER) {
	    if (goExec()) {
		// PsDebug.message("PjPsurfClient.update(): sucessfully loaded geometry from
		// executable");
	    } else {
		// PsDebug.message("PjPsurfClient.update(): failed to get data from
		// executable");
	    }
//	} else {
//	    if (goCGI()) {
//		// PsDebug.message("PjPsurfClient.update(): sucessfully loaded geometry from
//		// server");
//	    } else {
//		// PsDebug.message("PjPsurfClient.update(): failed to get data from server");
//	    }
//	}
    }

    /**
     * Run the server communication in a thread.
     */

    public boolean goCGI() {
	goCgiThread th = new goCgiThread();
	th.start();
	return true;
    }

    class goCgiThread extends Thread {
	String executable;
	String cgiReq;

	boolean successful = false;

	/**
	 * Create the thread. Makes local copies of most mutable variables at creation
	 * time.
	 */

	goCgiThread() {
	    cgiReq = makeCGIstring();
	    if (cgiReq == null)
		return;
	    executable = new String(server);
	}

	/**
	 * run the thread. Creates a new process, writes data to it and reads the data
	 * from it. If sucessful call processJvxSrc to load into JavaView
	 */

	@Override
	public void run() {
	    if (PRINT_TIME)
		AbstractClient.timemessage("cgi_run:");

	    URL myUrl = null;
	    HttpURLConnection connection = null;
	    PrintWriter writer = null;
	    InputStream inStream;
	    // String inputLine;
	    // PrintWriter tmpout = null;

	    // Create the URL of the Asurf server

	    if (cgiReq == null)
		return;
	    try {
		myUrl = new URL(executable);
	    } catch (MalformedURLException e) {
		showStatus("Bad URL for server");
		PsDebug.error("Bad URL for server " + executable);
		successful = false;
		return;
	    }

	    // Create the connection object

	    try {
		connection = (HttpURLConnection) myUrl.openConnection();
	    } catch (IOException e) {
		showStatus("Error opening conection to server");
		PsDebug.error("Error with openConnection");
		successful = false;
		return;
	    }

	    // Ensures that the we can write the server, i.e. do a POST request

	    try {
		connection.setRequestMethod("POST");
	    } catch (ProtocolException e2) {
		PsDebug.error("Protocol exception " + e2.getMessage());
	    }
	    connection.setDoOutput(true);

	    // and establish the stream

	    if (PRINT_TIME)
		AbstractClient.timemessage("cgi_run: sending data");

	    showStatus("Sending data to server");
	    try {
		writer = new PrintWriter(connection.getOutputStream());
	    } catch (IOException e) {
		showStatus("Error writing data to server");
		PsDebug.error("Error opening writer");
		successful = false;
		return;
	    }

	    // write the request to the server

	    writer.print(cgiReq);
	    writer.close();

	    if (PRINT_TIME)
		AbstractClient.timemessage("cgi_run: Calculating surface");

	    showStatus("Calculating image");

	    try {
		int respCode = connection.getResponseCode();
		String contType = connection.getContentType();

		if (PRINT_TIME)
		    AbstractClient.timemessage("cgi_run: loading image");

		/*
		 * System.out.println("Response code "+respCode);
		 * System.out.println("Response message "+connection.getResponseMessage());
		 * System.out.println("Content Type "+contType); int i=1; String key; while((key
		 * = connection.getHeaderFieldKey(i)) != null) {
		 * System.out.println("Key "+key+" val "+ connection.getHeaderField(i)); ++i; }
		 */
		if (respCode == HttpURLConnection.HTTP_OK) {
		    if (contType.equals("image/jpeg")) {
			Object obj = connection.getContent();
			if (obj instanceof ImageProducer) {
			    showStatus("Loading image");
			    ImageProducer imp = (ImageProducer) obj;
			    Toolkit tools = Toolkit.getDefaultToolkit();
			    myImage = tools.createImage(imp);
			    displayImage();
			    showStatus("Image displayed.");
			} else {
			    showStatus("Corrupt image!");
			}
		    } else if (contType.equals("text/plain")) {
			byte buffer[] = new byte[1024];
			inStream = connection.getInputStream();
			int len = inStream.read(buffer);
			String msg = new String(buffer, 0, len);
			showStatus(msg);
			PsDebug.error("Sorry could not create raytraced image.\n" + "Message was " + msg + "\n"
				+ "Possibly caused by a syntax error in the equation.\n"
				+ "For raytace images only a single line equation are supported;\n"
				+ "* must always be used for multiplication and = cannot appear.");
		    }
		}
	    } catch (IOException e1) {
		PsDebug.error("IO Error" + e1.getMessage());
	    }
	    if (PRINT_TIME)
		AbstractClient.timemessage("cgi_run: finished!");
	    return;
	} // end of run
    } // end of goCgiThread

    /**
     * Starts a local executable and get the geometry produced.
     */

    public boolean goExec() {
	goExecThread th = new goExecThread();
	th.start();
	return true;
    }

    /**
     * Run the server communication in a thread.
     */

    class goExecThread extends Thread {
	String executable;
	String envArray[] = new String[1];
	String cgiReq;

	boolean successful = false;

	/**
	 * Create the thread. Makes local copies of most mutable variables at creation
	 * time.
	 */

	goExecThread() {
	    cgiReq = makeCGIstring();
	    if (cgiReq == null)
		return;
	    executable = new String(server);
	    // envArray[0] = "REQUEST_METHOD=POST";
	    envArray[0] = "CONTENT_LENGTH=" + cgiReq.length();
	}

	/**
	 * run the thread. Creates a new process, writes data to it and reads the data
	 * from it.
	 */

	@Override
	public void run() {
	    PrintWriter writer = null;
	    // PrintWriter tmpout = null;
	    InputStream inStream = null;
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    if (cgiReq == null)
		return;
	    if (PRINT_TIME)
		AbstractClient.timemessage("cgi_run:");
	    Runtime myRuntime = Runtime.getRuntime();
	    Process myProcess = null;

	    try {
		System.out.println("raytrace executable " + executable);
		myProcess = myRuntime.exec(executable, envArray, new File("/Users/rich/surf-1.0.6/examples/rjm"));
		// System.out.println("Sucessful process");
	    } catch (Exception e1) {
		PsDebug.error("Error while trying to run C program");
		PsDebug.error(e1.getMessage());
		successful = false;
		return;
	    }

	    if (PRINT_TIME)
		AbstractClient.timemessage("cgi_run: sending data");
	    showStatus("Sending data to server");
	    // PsDebug.message("Sending data to server");

	    writer = new PrintWriter(myProcess.getOutputStream());
	    writer.print(cgiReq);
	    writer.close();

	    if (PRINT_TIME)
		AbstractClient.timemessage("cgi_run: calculating image");
	    showStatus("Calculating image");
	    // PsDebug.message("Calculating surface");

	    // int count = 0;
	    try {
		byte buffer[] = new byte[1024];

		inStream = myProcess.getInputStream();
		if (PRINT_TIME)
		    AbstractClient.timemessage("cgi_run: reading image");

		while (true) {
		    int len = inStream.read(buffer);
		    if (len == -1)
			break;
		    baos.write(buffer, 0, len);
		}

		if (PRINT_TIME)
		    AbstractClient.timemessage("cgi_run: read image");

		inStream.close();
		baos.close();
		myProcess.waitFor();
		int status = myProcess.exitValue();
		if (status != 0) {
		    BufferedReader err = new BufferedReader(new InputStreamReader(myProcess.getErrorStream()));
		    @SuppressWarnings("unused")
		    String line = null;
		    while ((line = err.readLine()) != null) {
			System.out.println();
		    }
		    showStatus("Sorry could not create raytraced image. Check equation.");
		    PsDebug.warning("Sorry could not create raytraced image.\n" + "Exit code was " + status + "\n"
			    + "Possibly caused by a syntax error in the equation.\n"
			    + "For raytace images only a single line equation are supported;\n"
			    + "* must always be used for multiplication and = cannot appear.");
		    successful = false;
		    return;
		}
	    } catch (IOException e) {
		PsDebug.error("Error reading data from server: " + e.getMessage());
		// e.printStackTrace();
		successful = false;
		return;
	    } catch (InterruptedException e2) {
		PsDebug.error("Error reading data from server: " + e2.getMessage());
		// e.printStackTrace();
		successful = false;
		return;
	    }

	    byte ba[] = baos.toByteArray();
	    if (PRINT_DEBUG)
		System.out.println("Byte array length " + ba.length);
	    Toolkit tools = Toolkit.getDefaultToolkit();
	    myImage = tools.createImage(ba);
	    displayImage();
	    showStatus("Image Displayed");
	    if (PRINT_TIME)
		AbstractClient.timemessage("cgi_run: done");
	} // end of run

    } // end of goExecThread

    /** Shows a Status message */

    public void showStatus(String str) {
	if (myApplet != null)
	    myApplet.showStatus(str);
    }

}
