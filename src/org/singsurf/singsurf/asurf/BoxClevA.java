package org.singsurf.singsurf.asurf;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern2D;

public abstract class BoxClevA implements BoxCleverBean {

	/************ Constants **********/
	public static final int MAX_EDGE_LEVEL = 32768;
	public static final int EMPTY = 0;
	public static final int FOUND_EVERYTHING = 2;
	public static final int FOUND_FACES = 3;
	/*** A value returned by follow when there a sol not on an edge is found. ***/
	static final int NEW_NODE = 2;
	public static final boolean NOT_DEF = false;
    public static final boolean FACEHASH = true; // whether to use a hash-table for storing faces

    public static final int COLOUR_GAUSSIAN_CURVATURE = 2;
	public static final int COLOUR_MEAN_CURVATURE = 1;

	/*********** Global variables ******/
	
	public static Region_info unsafeRegion; // This is an unsafe region used only for debugging
	static Bern3D unsafeBern;
	static PrintStream log;

	Region_info globalRegion;
	Box_info whole_box;
	
    protected Bern3DContext b3context;
	
	protected double[][][] AA; // Input polynomial not a Bernstein
	String description;

	public int RESOLUTION;
    public int LINK_SING_LEVEL;
    public int LINK_FACE_LEVEL;
    public int LINK_EDGE_LEVEL;
    
	/**** various options and flags****/

	public int global_selx = -1;
	public int global_sely=0;
	public int global_selz=0;
	public int global_denom=0;
	public Box_info global_sel_box;
	
	public boolean littleFacets = false;
	public int triangulate=1;
	public int cleanmesh=-1; // off by default
	public int rotderiv=0; // off by default
	public int tagbad=0;
	public int tagSing=0;
	public int blowup=0;
	public int global_mode = 0; // has know sings
	public double convtol = 1e-6;
	public boolean knitFacets = false;
	public boolean refineCurvature = false;
	public double curvatureLevel1 = 2.0;
	public double curvatureLevel2 = 4.0;
	public double curvatureLevel3 = 8.0;
	public double curvatureLevel4 = 16.0;
	protected double normlenlevel1 = 0.01;
	protected double normlenlevel2 = 0.001;
	protected double normlenlevel3 = 0.0001;
	protected double normlenlevel4 = 0.00001;

	public int colortype=0;
	float colourMin=-1f;
	float colourMax=1f;

	public Sol_info[] known_sings;
	public int num_known_sings;	
	

	/*** Major components **/
	public Facets facets;
	public Plotter plotter;
	Topology topology;
	protected AbstractMeshCleaner cleaner;
	protected TriangulatorI triangulator;
	protected Knitter knitter;
	protected int parallel;
	
	/******* Main entry point **********/
	
	public BoxClevA(String description) {
		this.description = description;
	}
	
	abstract public boolean marmain(double[][][] coeffs, Region_info region,int coarse, int fine, int face, int edge) throws AsurfException;

	/******* Utility output **********/
	
	public void printInput(double[][][] aa, Region_info region,int coarse, int fine, int face, int edge) {
	    StringBuilder sb = getPolyCoeffientsString(aa);
	    sb.append("["+region.xmin+","+region.xmax+"],["+region.ymin+","+region.ymax+"],["+region.zmin+","+region.zmax+"]\n");

	    sb.append(String.format("coarse=%d; fine=%d; face=%d; edge=%d;%n", 
	    		coarse,fine,face,edge));
	    if(this.global_denom>0) {
	    	sb.append(String.format("selx=%d; sely=%d; selz=%d; seld=%d;%n",
	    			global_selx, global_sely, global_selz, global_denom
	    			));
	    }
	    System.out.print(sb.toString());
	}

	
	/**
	 * @param aa
	 * @return
	 */
	public StringBuilder getPolyCoeffientsString(double[][][] aa) {
		StringBuilder sb = new StringBuilder();
	    sb.append("double aa[][][] = new double[][][] {\n");
	    for(int i=0;i<aa.length;++i) {
	        sb.append("{");
	        for(int j=0;j<aa[0].length;++j) {
	            if(j>0) sb.append(' ');
	            sb.append('{');
	            for(int k=0;k<aa[0][0].length;++k) {
	                sb.append(aa[i][j][k]);
	                if(k<aa[0][0].length-1)
	                    sb.append(",");
	            }
	            sb.append("}");
	            if(j<aa[0].length-1)
	                sb.append(",\n");
	        }
	        sb.append("}");
	        if(i<aa.length-1)
	            sb.append(",\n");
	    }
	    sb.append("};\n");
		return sb;
	}

	public void printRegion(Region_info region) {
	    System.out.println("["+region.xmin+","+region.xmax+"],["+region.ymin+","+region.ymax+"],["+region.zmin+","+region.zmax+"]\n");
	}
	
	public void printResolution() {
	    System.out.printf("coarse=%d; fine=%d; face=%d; edge=%d;%n", 
	    		RESOLUTION,LINK_SING_LEVEL,LINK_FACE_LEVEL,LINK_EDGE_LEVEL);

	}
	
	public String getResolutionString() {
	    return String.format("coarse=%d; fine=%d; face=%d; edge=%d;%n", 
	    		RESOLUTION,LINK_SING_LEVEL,LINK_FACE_LEVEL,LINK_EDGE_LEVEL);
	}
	
	public String getOptionsString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Selection region: (%d,%d,%d)/%d%n",
				global_selx, global_sely, global_selz, global_denom));
		sb.append(String.format("triangulate=%d; knitFacets=%b; refineCurvature=%b; littleFacets=%b; cleanmesh=%d;%n",
				triangulate,knitFacets,refineCurvature,littleFacets,cleanmesh));
		sb.append(String.format("curvatureLevel1=%4.2f; curvatureLevel2=%4.2f; curvatureLevel3=%4.2f; curvatureLevel4=%4.2f;%n",
				curvatureLevel1,curvatureLevel2,curvatureLevel3,curvatureLevel4));
		sb.append(String.format("normlenlevel1=%9.6f; normlenlevel2=%9.6f; normlenlevel3=%9.6f; normlenlevel4=%9.6f;%n",
				normlenlevel1,normlenlevel2,normlenlevel3,normlenlevel4));
		sb.append(String.format("tagbad=%d; tagSing=%d; blowup=%d; global_mode=%d;%n",
				tagbad,tagSing,blowup,global_mode));

		sb.append(String.format("convtol=%3.1e; colortype=%d; colourMin=%4.1f; colourMax=%4.1f;%n",
				convtol,colortype,colourMin,colourMax));
		if(known_sings!=null) {
			sb.append(String.format("num_known_sings=%d;%n",num_known_sings));
			for(int i=0;i<known_sings.length;++i)
				sb.append(known_sings[i]+"\n");
		}
		return sb.toString();
	}

	public static class CN_context {
        public Bern2D bb;
		public Bern2D dx;
		public Bern2D dy;
		public Bern2D dz; 
		public double vec[];
        public int signDx,signDy,signDz;
        public Sol_info sol;
    }

	public int getEuler() {
		return 	getNumVerts() + getNumFaces()
		 - getNumEdges();			
	}

	public int getNumVerts() {
		return plotter.numVertices();
	}

	public int getNumEdges() {
		return triangulator.numDoubleEdges()/2;
	}

	public int getNumDoubleEdges() {
		return triangulator.numDoubleEdges();
	}

	public int getNumFaces() {
		return plotter.numFaces();
	}

	public int getNumSings() {
		return plotter.getNumSings();
	}
	
	public int getGlobal_selx() {
		return global_selx;
	}

	public void setGlobal_selx(int global_selx) {
		this.global_selx = global_selx;
	}

	public int getGlobal_sely() {
		return global_sely;
	}

	public void setGlobal_sely(int global_sely) {
		this.global_sely = global_sely;
	}

	public int getGlobal_selz() {
		return global_selz;
	}

	public void setGlobal_selz(int global_selz) {
		this.global_selz = global_selz;
	}

	public int getGlobal_denom() {
		return global_denom;
	}

	public void setGlobal_denom(int global_denom) {
		this.global_denom = global_denom;
	}

	public boolean isLittleFacets() {
		return littleFacets;
	}

	public void setLittleFacets(boolean global_lf) {
		this.littleFacets = global_lf;
	}

	public int getTriangulate() {
		return triangulate;
	}

	public void setTriangulate(int triangulate) {
		this.triangulate = triangulate;
	}

	public int getCleanmesh() {
		return cleanmesh;
	}

	public void setCleanmesh(int cleanmesh) {
		this.cleanmesh = cleanmesh;
	}

	public int getTagbad() {
		return tagbad;
	}

	public void setTagbad(int tagbad) {
		this.tagbad = tagbad;
	}

	public int getTagSing() {
		return tagSing;
	}

	public void setTagSing(int tagSing) {
		this.tagSing = tagSing;
	}

	public int getBlowup() {
		return blowup;
	}

	public void setBlowup(int blowup) {
		this.blowup = blowup;
	}

	public double getConvtol() {
		return convtol;
	}

	public void setConvtol(double convtol) {
		this.convtol = convtol;
	}

	public int getColortype() {
		return colortype;
	}

	public void setColortype(int colortype) {
		this.colortype = colortype;
	}

	public float getColourMin() {
		return colourMin;
	}

	public void setColourMin(float colourMin) {
		this.colourMin = colourMin;
	}

	public float getColourMax() {
		return colourMax;
	}

	public void setColourMax(float colourMax) {
		this.colourMax = colourMax;
	}

	public boolean isKnitFacets() {
		return knitFacets;
	}

	public void setKnitFacets(boolean knitFacets) {
		this.knitFacets = knitFacets;
	}

	public boolean isRefineCurvature() {
		return refineCurvature;
	}

	public void setRefineCurvature(boolean refineCurvature) {
		this.refineCurvature = refineCurvature;
	}

	public double getCurvatureLevel1() {
		return curvatureLevel1;
	}

	public void setCurvatureLevel1(double curvatureLevel1) {
		this.curvatureLevel1 = curvatureLevel1;
	}

	public double getCurvatureLevel2() {
		return curvatureLevel2;
	}

	public void setCurvatureLevel2(double curvatureLevel2) {
		this.curvatureLevel2 = curvatureLevel2;
	}

	public double getCurvatureLevel3() {
		return curvatureLevel3;
	}

	public void setCurvatureLevel3(double curvatureLevel3) {
		this.curvatureLevel3 = curvatureLevel3;
	}

	public double getCurvatureLevel4() {
		return curvatureLevel4;
	}

	public void setCurvatureLevel4(double curvatureLevel4) {
		this.curvatureLevel4 = curvatureLevel4;
	}

	public void dumpRemaining() {
		dumpAllocation(this.whole_box,0);
	}

	private void dumpAllocation(Box_info box, int depth) {
		if(box==null) return;
//		if(box.xl+1==box.denom || box.yl+1==box.denom || box.zl+1==box.denom) {}
//		else {
		System.out.print("Remaining ");
		for(int i=0;i<depth;++i) System.out.print(' ');
		System.out.print(box.print_box_header());
//		}
		
		dumpAllocation(box.lfd,depth+1);
		dumpAllocation(box.lfu,depth+1);
		dumpAllocation(box.lbd,depth+1);
		dumpAllocation(box.lbu,depth+1);
		dumpAllocation(box.rfd,depth+1);
		dumpAllocation(box.rfu,depth+1);
		dumpAllocation(box.rbd,depth+1);
		dumpAllocation(box.rbu,depth+1);
	}

	public double getNormlenlevel1() {
		return normlenlevel1;
	}

	public void setNormlenlevel1(double normlenlevel1) {
		this.normlenlevel1 = normlenlevel1;
	}

	public double getNormlenlevel2() {
		return normlenlevel2;
	}

	public void setNormlenlevel2(double normlenlevel2) {
		this.normlenlevel2 = normlenlevel2;
	}

	public double getNormlenlevel3() {
		return normlenlevel3;
	}

	public void setNormlenlevel3(Double normlenlevel3) {
		this.normlenlevel3 = normlenlevel3;
	}

	public Double getNormlenlevel4() {
		return normlenlevel4;
	}

	public void setNormlenlevel4(Double normlenlevel4) {
		this.normlenlevel4 = normlenlevel4;
	}

	protected abstract long getComputeTime();

	protected abstract long getMemoryUsed();

	protected abstract void triangulate_and_plot(Box_info box);

	protected abstract void facet_triangulate_plot_and_free(Box_info box, List<Face_info> faces);

	protected abstract void report_progress(Box_info bigbox, double percent);

	protected final void print_vertex_count() {
		System.out.println("\n---------------------------------------\n");

 		System.out.printf("Vertices %d edges %d%s faces %d χ = %d, sings %d isolated sings %d edges %d%n",
				getNumVerts(),
				getNumEdges(),
				getNumDoubleEdges() %2 == 0 ? "" : ".5",
				getNumFaces(),
				getEuler(),				
				getNumSings(),
				getNumIsolatedSings(),
				getNumDejenerateEdges());
		Sol_info sol = plotter.least_acurate_vertex();
		if(sol!=null) {
			System.out.printf("Least accurate solution %6.3e%n",sol.calcValue(b3context));
			System.out.println(sol);
		}
	}

	/**
	 * @return
	 */
	public int getNumDejenerateEdges() {
		return plotter.numDejectEdges();
	}

	/**
	 * @return
	 */
	public int getNumIsolatedSings() {
		return plotter.numIsolatedSings();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getParallel() {
		return parallel;
	}

	public void setParallel(int parallel) {
		this.parallel = parallel;
	}




	@Override
	public int getCourseRes() {
		return RESOLUTION;
	}

	@Override
	public void setCourseRes(int x) {
		RESOLUTION = x;
	}

	@Override
	public int getFineRes() {
			return LINK_SING_LEVEL;
	}

	@Override
	public void setFineRes(int x) {
		LINK_SING_LEVEL = x;		
	}

	@Override
	public int getFaceRes() {
		return LINK_FACE_LEVEL;
	}

	@Override
	public void setFaceRes(int x) {
		LINK_FACE_LEVEL = x;		
	}

	@Override
	public int getEdgeRes() {
		return LINK_EDGE_LEVEL;
	}

	@Override
	public void setEdgeRes(int x) {
		LINK_EDGE_LEVEL = x;		
	}

//	@Override
	public void setNormlenlevel3(double x) {
		this.normlenlevel3 =x;
	}

//	@Override
	public void setNormlenlevel4(double x) {
		this.normlenlevel3 =x;
	}

//	@Override
	public RegionBean getRegionBean() {
		return this.globalRegion;
	}

	public void setRegion(Region_info r) {
		this.globalRegion = r;
	}
	
	public Region_info getRegion() {
		return globalRegion;
	}
//	@Override
	public void setRegionBean(RegionBean bean) {
		this.globalRegion  = new Region_info(bean);
	}

	public double[][][] getCoeffs() { return AA; }
	public void setCoeffs(double[][][] co) { AA = co; }
	
	@Override
	public List<Double> getCoeffsAsList() {
		List<Double> res = new ArrayList<>();

		for(int i=0;i<AA.length;++i) {
			for(int j=0;j<AA[0].length;++j) {
				for(int k=0;k<AA[0][0].length;++k) {
					res.add(AA[i][j][k]);
				}
			}
		}
		return res;
	}

	List<Double> coeffList;
	int degX,degY,degZ;
	@Override
	public void setCoeffsAsList(List<Double> co) {
		coeffList = co;
	}

	public void buildCoeffs() {
		AA = new double[degX+1][degY+1][degZ+1];
		int index=0;
		for(int i=0;i<AA.length;++i) {
			for(int j=0;j<AA[0].length;++j) {
				for(int k=0;k<AA[0][0].length;++k) {
					AA[i][j][k] = coeffList.get(index);
					++index;
				}
			}
		}
		
	}
	@Override
	public int getDegX() {
		return AA.length-1;
	}

	@Override
	public void setDegX(int n) {
		degX=n;
	}

	@Override
	public int getDegY() {
		return AA[0].length-1;
	}

	@Override
	public void setDegY(int n) {
		degY=n;
	}

	@Override
	public int getDegZ() {
		return AA[0][0].length-1;
	}

	@Override
	public void setDegZ(int n) {
		degZ=n;
	}

	public void setRotderiv(int i) {
		this.rotderiv=i;
	}

	public Sol_info addKnownSing(Region_info region,double x,double y,double z) {
		double[] xrel = region.relative_position(x,y,z);
		Sol_info sol = new Sol_info(Key3D.BOX,0,0,0,1, xrel[0],xrel[1],xrel[2]);
		Sol_info[] old = known_sings;
		int len = old!=null ? old.length : 0;
		known_sings = new Sol_info[len+1];
		known_sings[len] = sol;
		if(old!=null) {
			System.arraycopy(old, 0, known_sings, 0, len);
		}
		num_known_sings++;
		return sol;
		
	}
}