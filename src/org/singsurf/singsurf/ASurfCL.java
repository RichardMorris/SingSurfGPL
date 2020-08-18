package org.singsurf.singsurf;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

import org.lsmp.djep.xjep.PrintVisitor;
import org.nfunk.jep.ASTConstant;
import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.asurf.BoxClevA;
import org.singsurf.singsurf.asurf.BoxClevVrml;
import org.singsurf.singsurf.asurf.PlotAbstract.PlotMode;
import org.singsurf.singsurf.asurf.Region_info;
import org.singsurf.singsurf.calculators.PolynomialCalculator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.definitions.DefinitionReader;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.jep.EquationPolynomialConverter;

public class ASurfCL {

	String filename;
	String modelname;
	File file;
	Definition def;
	private PolynomialCalculator calc;

	public ASurfCL(String filename, String modelname) throws Exception {
		super();
		this.filename = filename;
		this.modelname = modelname;
		DefinitionReader ldr = new DefinitionReader(filename);
		ldr.read();
		List<Definition> list = ldr.getDefs();
		for(Definition def:list) {
			if(modelname.equals(def.getName())) {
				this.def = def;
				System.out.println(def.toString());
				break;
			}
		}
		if(this.def==null) {
			System.out.println("Definition "+modelname+" not found in "+filename);
			System.out.println("Available models");
			for(Definition def:list) {
				System.out.println(def.getName());
			}
		}
	}



	int coarse=32;
	int singmul=8;
	int facemul=8;
	int edgemul=8;
	int fine=coarse*8;
	int face=fine*8;
	int edge=face*8;

	PlotMode plotMode = PlotMode.JustSurface;

	private void go(String outfilename) throws ParseException, AsurfException {
		calc = new PolynomialCalculator(def, 0);
//		calc.getJep().getOperatorTable().getSubtract().addAltSymbol("\u2212");
//		calc.getJep().getOperatorTable().getSubtract().addAltSymbol("\u2013");
//		calc.getJep().reinitializeComponents();
		char c = calc.getDefinition().getEquation().charAt(2);
		System.out.printf("Char %s %x %d%n",c,(int) c,(int) c);
		calc.build();
		if(!calc.isGood()) {
			System.out.println(calc.getMsg());
			return;
		}
		PrintVisitor pv = calc.getJep().getPrintVisitor();
		BoxClevA boxclev = new BoxClevVrml(PlotMode.JustSurface,new File(outfilename),def.toString());

		EquationPolynomialConverter ec = new EquationPolynomialConverter(calc.getJep()); //, calc.getField());
		double[][][] coeffs = ec.convert3D(calc.getRawEqns(),
				new String[] { def.getVar(0).getName(),def.getVar(1).getName(),def.getVar(2).getName() },
				calc.getParams());
		if (coeffs.length == 1 && coeffs[0].length == 1 && coeffs[0][0].length == 1)
			throw new AsurfException("Equation is a constant");

		//    	bc.RESOLUTION = def.getOption(")

		useIntOption("coarse",i -> coarse = i);
		useIntOption("singPower", i -> singmul = i);
		useIntOption("facePower", i -> facemul = i);
		useIntOption("edgePower", i -> edgemul = i);
		//		useIntOption("showFace", this.cbShowFace.getState());
		//		useIntOption("showEdge", this.cbShowEdge.getState());
		//		useIntOption("showVert", this.cbShowVert.getState());
		//		useIntOption("showCurve", this.cbShowCurves.getState());
		//		useIntOption("showPoint", this.cbShowPoints.getState());
		//		useIntOption("showBoundary", this.cbShowBoundary.getState());
		useBoolOption("calcSkeleton", i -> plotMode = PlotMode.Skeleton);
		useBoolOption("calcDgen", i -> plotMode = PlotMode.Degenerate);
		Option copt = def.getOption("surfColour");
		String colour = copt != null ? copt.getStringVal() : "";
		if("Gaussian curvature".equals(colour))
			boxclev.setColortype(BoxClevA.COLOUR_GAUSSIAN_CURVATURE);
		if("Mean curvature".equals(colour))
			boxclev.setColortype(BoxClevA.COLOUR_MEAN_CURVATURE);
		useDoubleOption("colourMinVal",f -> boxclev.setColourMin((float) f));
		useDoubleOption("colourMaxVal",f -> boxclev.setColourMax((float) f));
		boxclev.setCurvatureLevel1(boxclev.getColourMax()/4);
		boxclev.setCurvatureLevel2(boxclev.getColourMax()/2);
		boxclev.setCurvatureLevel3(boxclev.getColourMax());
		boxclev.setCurvatureLevel4(boxclev.getColourMax()*2);

		useBoolOption("adaptiveMesh", b -> boxclev.setKnitFacets(b));
		useBoolOption("refineByCurvature",b -> boxclev.setRefineCurvature(b));
		useBoolOption("triangulate", b -> boxclev.setTriangulate( b ? 1 : 0));


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

			useIntSetting(n, "triangulate", i-> boxclev.setTriangulate(i));
			useIntSetting(n, "littlefacet", i-> boxclev.setLittleFacets(i!=0));
			useIntSetting(n, "cleanmesh", i-> boxclev.setCleanmesh(i));
			useIntSetting(n, "tagbad", i-> boxclev.setTagbad(i));
			useIntSetting(n, "tagsing", i-> boxclev.setTagSing(i));
			useIntSetting(n, "blowup", i-> boxclev.setBlowup(i));

			useDoubleSetting(n,"convtol",d -> boxclev.setConvtol(d));
			useDoubleSetting(n,"curvatureLevel1",d -> boxclev.setCurvatureLevel1(d));
			useDoubleSetting(n,"curvatureLevel2",d -> boxclev.setCurvatureLevel2(d));
			useDoubleSetting(n,"curvatureLevel3",d -> boxclev.setCurvatureLevel3(d));
			useDoubleSetting(n,"curvatureLevel4",d -> boxclev.setCurvatureLevel4(d));

			singmul = getIntSetting(n, "singresmul",singmul);
			facemul = getIntSetting(n, "faceresmul",facemul);
			edgemul = getIntSetting(n, "edgeresmul",edgemul);
		}

		fine = coarse*singmul;
		face = fine*facemul;
		edge = face*facemul;

//		useIntOption("fine", i -> fine = i);
//		useIntOption("face", i -> face = i);
//		useIntOption("edge", i -> edge = i);

		Region_info region = new Region_info(def.getVar(0).getMin(),def.getVar(0).getMax(),
				def.getVar(1).getMin(),def.getVar(1).getMax(),
				def.getVar(2).getMin(),def.getVar(2).getMax());

		boxclev.marmain(coeffs, region, coarse, fine, face, edge);
	}


	void useIntSetting(Node n,String tag,IntConsumer setter) {
		if (tag.equals(((ASTVarNode) n.jjtGetChild(0)).getName()) )
			setter.accept( ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).intValue());
	}

	void useDoubleSetting(Node n,String tag,DoubleConsumer setter) {
		if (tag.equals(((ASTVarNode) n.jjtGetChild(0)).getName()) )
			setter.accept( ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).doubleValue());
	}

	int getIntSetting(Node n,String tag,int defaultVal) {
		if (tag.equals(((ASTVarNode) n.jjtGetChild(0)).getName()) )
			return  ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).intValue();
		return defaultVal;
	}

	private void useIntOption(String string, IntConsumer setter) {
		if(def.getOpt(string)!=null)
			setter.accept(def.getOption(string).getIntegerVal());
	}

	private void useDoubleOption(String string, DoubleConsumer setter) {
		if(def.getOpt(string)!=null)
			setter.accept(def.getOption(string).getDoubleVal());
	}

	private void useBoolOption(String string, Consumer<Boolean> setter) {
		if(def.getOpt(string)!=null)
			setter.accept(def.getOption(string).getBoolVal());
	}

	public static void main(String[] args) {
		System.out.println("Arguments "+Arrays.deepToString(args));
		System.out.println("Default Charset=" + Charset.defaultCharset());
		String filename=null;
		String defname=null;
		String modelname=null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			if(args.length>=1) {
				filename = args[0];

			}
			else {
				System.out.println("filename");
				filename = br.readLine();

			}
			if(args.length>=2) {
				defname = args[1];
			} else {
				System.out.println("defname");
				defname = br.readLine();
			}

			if(args.length>=3) {
				modelname = args[2];
			}
			else {
				System.out.println("modelname");
				modelname = br.readLine();					
			}


			ASurfCL asurf = new ASurfCL(filename,defname);
			if(asurf.def!=null)
				asurf.go(modelname);
		}
		catch(Exception ex) {
			//			System.out.println(ex);
			ex.printStackTrace();
		}
	}}
