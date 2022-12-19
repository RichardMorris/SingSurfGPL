package org.singsurf.singsurf.asurf;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class PlotAbstract implements Plotter {
	private static final int BAD_VERTEX = -2;
	private static final int BLOW_UP = -3;
    
	private static final boolean PRINT_FACET=false;

    public enum PlotMode {
    	JustSurface, Degenerate, Skeleton, KnownSings
    }
    final BoxClevA boxclev;
    Bern3DContext ctx;

    /************************************************************************/
    /*									                                    */
    /*	some sub programs to plot the boxes.		                		*/
    /*	The general process is as follows:			                    	*/
    /*	for each box {						                            	*/
    /*	{   for each solution					                        	*/
    /*		if solution already used continue;		                    	*/
    /*		plot first face adjacient to solution, update solution          */
    /*									                                    */
    /************************************************************************/

    boolean draw_lines;
    boolean blowup_singularities=false;
    String global_geomname;

    final PlotMode mode;
    Sol_info known_sings[];	/* The singularities known from external data */
    int num_known_sings;	/* number of such */

    Region_info region;

	/** number of faces */
    int face_count = 0;

	/** number of solutions for calculating Euler chracteristic */
    int sol_count;
    
	/** The number of plotted vertices.
	 *  Singularities may be represented by  multiple vert **/
    int total_face_sol_count;
    
	/** number of blown up singularities */
	int blown_up_sols = 0;

	/** number of points on degenerate lines */
    int vect_point_count = 0;	
    /** number of line segments */
    int vect_count = 0;
    /** number of singularities */
	int sing_count = 0;
	/** number of isolated points */
	private int num_isolated;

    short goodnorm[]=new short[3];

    boolean problem=false;
	private int failCountA;
  	
    public Map<Integer,Sol_info> badIndicies = new HashMap<Integer,Sol_info>();
	protected Map<Integer, Sol_info> singIndicies = new HashMap<Integer,Sol_info>();
	Map<Integer,Integer> boxCounts = new HashMap<Integer,Integer>();

    Sol_info worseSol=null;
	private double worseValue=0.0;
	private int blowup_copies;

    public PlotAbstract(BoxClevA boxclev,PlotMode mode) {
        super();
        this.boxclev = boxclev;
        this.mode = mode;
//        total_tri_count = 0;
        total_face_sol_count = 0;
        vect_point_count = 0;
        vect_count = 0;
    }

	@Override
	public void init(Bern3DContext bern3dContext) {
		this.ctx = bern3dContext;
    	this.blowup_singularities = (boxclev.blowup == 1);
    }
    
    /********** Main entry point for routines *****************/

    public void plot_box(Box_info box)
    {
        plot_all_facets(box);

        /* Now draw the node_links */

        if(mode != PlotMode.JustSurface) {
        	plot_all_node_links(box);
            plot_all_sings(box);
        }

    }

    /************************************************************************/
    /*									*/
    /*	draws a box.							*/
    /*									*/
    /************************************************************************/

    public void plot_all_facets(Box_info box)
    {
        if(box.facets==null) return;
        boxCounts.merge(box.denom, 1, Integer::sum);
        
//        if(box.xl == 5 && box.yl == 13 && box.zl ==9) {} else return;
        if(boxclev.global_denom>=0 && (
             box.xl * boxclev.global_denom < boxclev.global_selx * box.denom 
          || box.xl * boxclev.global_denom > (boxclev.global_selx+1) * box.denom ) ) return;
        if(boxclev.global_sely >=0 && (
             box.yl * boxclev.global_denom < boxclev.global_sely * box.denom 
          || box.yl * boxclev.global_denom > (boxclev.global_sely+1) * box.denom ) ) return; 
        if(boxclev.global_selz >=0 && (
             box.zl * boxclev.global_denom < boxclev.global_selz * box.denom 
          || box.zl * boxclev.global_denom > (boxclev.global_selz+1) * box.denom ) ) return;
        if(boxclev.global_denom>0)
        {
        	System.out.print("PlotJavaView: ");
        	System.out.println(box.print_box_header());
//        	System.out.println(box.toString());
            for(Facet_info f1:box.facets)
            {
            	System.out.println(f1);
            }        	

        }
        
        for(Facet_info f1:box.facets)
        {
            plot_facet(f1,box);
        }
    }

	private void plot_facet(Facet_info f1, Box_info box) {
        Facet_sol s1;
        problem=false;
        //		System.out.println(f1);

        s1 = f1.sols;
        if(s1 == null) return;
        /* check the facet has at least 3 sols */
        if(s1.next == null) return;
        if(s1.next.next == null) return;

        bgnfacet();
        int nSols=0;
        while(s1 != null)
        {
        	plot_sol(s1.sol);
            if(s1.sol.plotindex>=0)
            	++nSols;
            if(s1.sol.plotindex == BLOW_UP)
            	++nSols;
            s1 = s1.next;
        }
        int[] ind = new int[nSols];
        Sol_info[] sols = new Sol_info[nSols];

        int[] blowups = new int[nSols];
        int nblowups=0;
        s1 = f1.sols;
        int pos=0;
        while(s1 != null)
        {
            if(s1.sol.plotindex>=0) {
                ind[pos]=s1.sol.plotindex;
                sols[pos]=s1.sol;
                ++pos;
            } else if(s1.sol.plotindex == BLOW_UP) {
            	Sol_info s2 = s1.sol.duplicate();
            	blowups[nblowups++] = pos;
            	sols[pos++]=s2;
            }
            s1=s1.next;
        }

        if(nblowups>0) {
        	calcBlowupNormals(sols,blowups,nblowups);
        	for(int i=0;i<nblowups;++i) {
        		ind[blowups[i]] = plot_blowup(sols[blowups[i]]);
        	}
        	blowup_copies += nblowups;
        }
        
        if(!testClockwise(f1,ind,sols)) {
        	int l=ind.length;
        	for(int i=0;i<l/2;++i) {
        		int tm = ind[l-i-1];
        		ind[l-i-1]=ind[i];
        		ind[i]=tm;
        	}
        }
        addFace(ind);
        ++face_count;
        endfacet();
    }
	
	protected abstract void addFace(int[] indices);
	
    private void calcBlowupNormals(Sol_info[] sols, int[] blowups, int nblowups) {
    	for(int i=0;i<nblowups;++i) {
    		int cur = blowups[i];
    		int prev = cur > 0 ? cur - 1 : sols.length-1;
    		int next = cur < sols.length - 1 ? cur + 1 : 0;
    		double[] posA = sols[cur].calc_pos_actual(boxclev.globalRegion,ctx);
    		double[] posB = sols[prev].calc_pos_actual(boxclev.globalRegion,ctx);
    		double[] posC = sols[next].calc_pos_actual(boxclev.globalRegion,ctx);
    		double[] Norm = Vec3D.cross(Vec3D.subVec(posB, posA), Vec3D.subVec(posC, posA));
    		Vec3D.normalise(Norm);
    		sols[cur].setNorm(Norm);    		
    	}
    	
    	boolean flip=false;
    	if(nblowups == sols.length) {
    		// All sols are singularities, no way to calculate correct orientation
    	} else {
    		int ordinary=-1;
    		for(int i=0;i<nblowups;++i) {
    			if(blowups[i]!=i) {
    				ordinary = i;
    				break;
    			}
    		}
    		if(ordinary==-1) {
    			ordinary = nblowups;
    		}
    		int prev = ordinary > 0 ? ordinary - 1 : sols.length-1;
    		int next = ordinary < sols.length - 1 ? ordinary + 1 : 0;
    		double[] posA = sols[ordinary].calc_pos_actual(boxclev.globalRegion,ctx);
    		double[] posB = sols[prev].calc_pos_actual(boxclev.globalRegion,ctx);
    		double[] posC = sols[next].calc_pos_actual(boxclev.globalRegion,ctx);
    		double[] Norm = Vec3D.cross(Vec3D.subVec(posB, posA), Vec3D.subVec(posC, posA));
    		Vec3D.normalise(Norm);
    		
    		flip = (Vec3D.dot(sols[ordinary].calc_norm_actual_unsafe(boxclev.globalRegion,ctx), Norm) < 0);
    	}
		if(flip) {
    		for(int i=0;i<nblowups;++i) {
    			sols[blowups[i]].flipNormal();
    		}
		}
	}

	Color calcSkelColour(Sol_info sol) {
        int r = (sol.getDx() == 0  ? 255 : 0);
        int g = (sol.getDy() == 0  ? 255 : 0);
        int b = (sol.getDz() == 0  ? 255 : 0);
        return new Color(r,g,b);
	}
		
	double minCurvature = Double.MAX_VALUE;
	double maxCurvature = Double.MIN_VALUE;
	Color calcSolColour(Sol_info sol) {
		switch(this.boxclev.colortype) {
		case BoxClevA.COLOUR_MEAN_CURVATURE:
		{
			float H = (float) sol.calcMeanCurvature(boxclev.globalRegion,ctx);
			float Hmax = boxclev.colourMax;
			float Hmin = boxclev.colourMin;
			float rf = H>0 ? 1 : 0;
    		float gf = H>0 ? 1-H/Hmax : 1-H/Hmin;
    		float gclip = gf > 1 ? 1 : (gf < 0 ? 0 : gf);
    		float bf = H>0 ? 0 : 1;
    		if(Math.abs(H)<boxclev.convtol) {
    			rf=0; gf=1;bf=0;
    		}
    		if(H>maxCurvature) maxCurvature=H;
    		if(H<minCurvature) minCurvature=H;
			return new Color(rf,gclip,bf);
		}
		case BoxClevA.COLOUR_GAUSSIAN_CURVATURE:
		{
			float G = (float) sol.calcGaussianCurvature(boxclev.globalRegion,ctx);
			float Gmax = boxclev.colourMax;
			float Gmin = boxclev.colourMin;
			float rf = G>0 ? 1 : 0;
    		float gf = G>0 ? 1-G/Gmax : 1-G/Gmin;
    		float gclip = gf > 1 ? 1 : (gf < 0 ? 0 : gf);
    		float bf = G>0 ? 0 : 1;
    		if(Math.abs(G)<boxclev.convtol) {
    			rf=0; gf=1;bf=0;
    		}
    		if(G>maxCurvature) maxCurvature=G;
    		if(G<minCurvature) minCurvature=G;
			return new Color(rf,gclip,bf);
		}
		default:
			return new Color(256,256,256);
		}
    }

    void plot_sol(Sol_info sol)
    {
        double vec[],norm[];
        if(sol == null)
        {
            System.out.printf("Error: plot_sol: sol == null\n");
            return;
        }

        /* First calculate the position */

        if(sol.plotindex == BAD_VERTEX) return;
        
        if(sol.plotindex == BLOW_UP) return;

        if(sol.plotindex == Sol_info.UNPLOTTED_VERTEX )
        {
            vec = sol.calc_pos_actual(boxclev.globalRegion,ctx);

            if(vec[0] != vec[0] || vec[1] != vec[1] || vec[2] != vec[2] )
            {
            	if(failCountA++==0) {
            		System.out.printf("NaN in plot_sol\n");
            		print_sol(sol);
            	}
                sol.plotindex = BAD_VERTEX;
                return;
            }

			double value = sol.calcValue(ctx);
			if(Math.abs(value) > worseValue) {
				worseSol = sol;
				worseValue = Math.abs(value);
			}

    		if(sol.getDx() == 0 && sol.getDy() ==0 && sol.getDz() ==0) {
    			++sing_count;
    			if(blowup_singularities) {
        			sol.plotindex = BLOW_UP;
        			++sol_count;
        			++blown_up_sols;
        			return;
        		} 
        	}

            norm = sol.calc_unit_norm(boxclev.globalRegion,ctx);
            //unit3drobust(norm);

            sol.plotindex = total_face_sol_count++;
            sol_count++;
            
            addVertex(vec);
            addNormal(norm);
            
            if(boxclev.colortype>0) {
            	addColour(calcSolColour(sol));
            }

            if(sol.conv_failed) {
            	badIndicies.put(sol.plotindex,sol);
            }
//            if(boxclev.tagSing>0) {
        	if(sol.getDx() == 0 && sol.getDy() ==0 && sol.getDz() ==0) {
        			singIndicies.put(sol.plotindex,sol);
//        		}
            }
        }
        if(sol.getDx()==0&&sol.getDy()==0&&sol.getDz()==0)
            problem=true;
        if(PRINT_FACET) {
            System.out.printf("No %d ",sol.plotindex);
            print_sol(sol);
        }

        return;
    }


	protected abstract void addColour(Color calcSolColour);

	protected abstract void addNormal(double[] norm);

	protected abstract void addVertex(double[] vec);

	private int plot_blowup(Sol_info sol) {
		double[] pos = sol.calc_pos_actual(boxclev.globalRegion,ctx);
		double[] norm = sol.calc_norm_actual_unsafe(boxclev.globalRegion,ctx);

        sol.plotindex = total_face_sol_count++;
        
        addVertex(pos);
        addNormal(norm);
        if(boxclev.colortype>0) {
        	addColour(calcSolColour(sol));
        }
		return sol.plotindex;
	}


    private double tripleScalar(Sol_info A,Sol_info B,Sol_info C) {
		double posA[] = A.calc_pos_actual(boxclev.globalRegion,ctx);
		double posB[] = B.calc_pos_actual(boxclev.globalRegion,ctx);
		double posC[] = C.calc_pos_actual(boxclev.globalRegion,ctx);

		//double normA[] = A.calc_norm_actual(boxclev);
		double normB[] = B.calc_norm_actual_unsafe(boxclev.globalRegion,ctx);
		//double normC[] = C.calc_norm_actual(boxclev);
		
		double[] BA = Vec3D.subVec(posA, posB);
		double[] BC = Vec3D.subVec(posC, posB);
		double[] norm = Vec3D.cross(BA, BC);
		double dot = Vec3D.dot(normB, norm);
		return dot;
    }
    
    boolean testClockwise(Facet_info f,int ind[], Sol_info[] sols) {
    	int l=ind.length;
    	if(l<3) return false;
    	double dots[]=new double[l];
    	double max=0;
    	double min=0;
    	@SuppressWarnings("unused")
		int npos=0;
    	@SuppressWarnings("unused")
		int nneg=0;
    	for(int i=0;i<l;++i) {
    		//int a = i-1>=0 ? i-1 : i-1+l;
    		dots[i]=tripleScalar(sols[(i-1+l)%l],sols[i],sols[(i+1)%l]);
//    		dots[i]=tripleScalar(ind[(i-1+l)%l],ind[i],ind[(i+1)%l]);
    		if(dots[i]>max) max=dots[i];
    		if(dots[i]<min) min=dots[i];
    		if(dots[i]>0) ++npos;
    		if(dots[i]<0) ++nneg;
    	}
//    	if(npos>0 && nneg>0) {
//    		StringBuilder sb = new StringBuilder("Non convex "+npos+" "+nneg+" "+eles.size()+"[");
//    		for(int i=0;i<l;++i) {
//    			sb.append(String.format("%+5.3f ", dots[i]));
//    		}
//    		sb.append("]\n");
//    		for(Sol_info sol:sols) {
//    			sb.append(sol.toStringNorm());
//    			sb.append('\n');
//    		}
//    		System.out.println(sb.toString());
//    	}
    	return (-min>max);
    }

    void plot_all_sings(Box_info box)
    {
        if(box.lfd != null)
        {
            plot_all_sings(box.lfd);
            plot_all_sings(box.lfu);
            plot_all_sings(box.lbd);
            plot_all_sings(box.lbu);
            plot_all_sings(box.rfd);
            plot_all_sings(box.rfu);
            plot_all_sings(box.rbd);
            plot_all_sings(box.rbu);
        }

    	if(box.sings==null) return;
        for(Sing_info sing:box.sings)
        {
            Sol_info sol = sing.sing;
            
            switch(mode) {
            case KnownSings:
            {
                int i;
                for(i=0;i<num_known_sings;++i)
                {
                    if( sol.xl == known_sings[i].xl
                            && sol.yl == known_sings[i].yl
                            && sol.zl == known_sings[i].zl
                            && sol.getRoot() == known_sings[i].getRoot()
                            && sol.getRoot2() == known_sings[i].getRoot2()
                            && sol.getRoot3() == known_sings[i].getRoot3() )
                        plot_point(sing.sing);
                }
                break;
            }
			case Degenerate:
			{
				if(sing.numNLs==0)
	                plot_point(sing.sing);
				break;
			}
			case JustSurface:
				break;
			case Skeleton:
				if(sol.getDx() == 0 && sol.getDy() == 0 && sol.getDz() == 0)
	                plot_point(sing.sing);
				break;
			default:
				break;
            }
        }
    }

    void plot_all_node_links(Box_info box)
    {
        if(box.lfd != null)
        {
            plot_all_node_links(box.lfd);
            plot_all_node_links(box.lfu);
            plot_all_node_links(box.lbd);
            plot_all_node_links(box.lbu);
            plot_all_node_links(box.rfd);
            plot_all_node_links(box.rfu);
            plot_all_node_links(box.rbd);
            plot_all_node_links(box.rbu);
        }

        if(box.node_links==null) return;
        for(Node_link_info node_link: box.node_links)
        {
               if(mode == PlotMode.Skeleton 
            	|| (mode == PlotMode.Degenerate
                      && node_link.A.sol.getDx() == 0
                      && node_link.A.sol.getDy() == 0
                      && node_link.A.sol.getDz() == 0
                      && node_link.B.sol.getDx() == 0
                      && node_link.B.sol.getDy() == 0
                      && node_link.B.sol.getDz() == 0 ) )
                plot_line(node_link.A.sol,node_link.B.sol);

        }

    }


    void bgnfacet()
    {
        if(PRINT_FACET) {
            System.out.printf("bgnfacet:\n");
        }
    }

    void endfacet()
    {
    }

    /********* test using much bigger polygons ********/


    private void print_sol(Sol_info sol) {
        System.out.print(sol);
    }

    @Override
	public void plot_point(Sol_info sol)
    {

        if(sol == null)
        {
            System.out.printf("Error: plot_sol: sol == null\n");
            return;
        }

        /* First calculate the position */

        double[]  vec = sol.calc_pos_actual(boxclev.globalRegion,ctx);
        if(PRINT_FACET) {
            System.out.printf("point:\n");
            print_sol(sol);
        }
        ++num_isolated;
        addPoint(vec);
    }

    protected abstract void addPoint(double[] vec);

	Map<Sol_info,Integer> lineIndices = new HashMap<>();
    int maxLineIndex = 0;
    
    int plotLinePoint(Sol_info sol) {
        Integer indexA = lineIndices.get(sol);
        if(indexA!=null) return indexA;
    	indexA = maxLineIndex++;
    	lineIndices.put(sol, indexA);
        
        double vec[]=sol.calc_pos_actual(boxclev.globalRegion,ctx);
        addLineVertex(vec);
        ++vect_point_count;
        return indexA;
    }

	@Override
	public void plot_line(Sol_info sol1,Sol_info sol2)
    {
        if(PRINT_FACET) {
            System.out.printf("line: \n");
            print_sol(sol1);
            print_sol(sol2);
        }
        int indexA = plotLinePoint(sol1);
        int indexB = plotLinePoint(sol2);

        addLineEdge(indexA,indexB);
        
        Color lineColor;
        if(sol1.num_zero_derivs() <= sol2.num_zero_derivs()) {
        	lineColor = calcSkelColour(sol1);
        } else {
        	lineColor = calcSkelColour(sol2);        	
        }
        if(lineColor.equals(Color.white))
        	lineColor= Color.black;
        
        setLineColour(lineColor);
        ++vect_count;


    }

    protected abstract void addLineVertex(double[] vec);

	protected abstract void addLineEdge(int indexA, int indexB);

	protected abstract void setLineColour(Color lineColor);

	@Override
	public Sol_info least_acurate_vertex() {
		return worseSol;
	}

	@Override
	public int numVertices() {
		return sol_count;
	}

	@Override
	public int numFaces() {
		return face_count;
	}

	@Override
	public int numIsolatedSings() {
		return num_isolated;
	}

	@Override
	public int numDejectEdges() {
		return vect_count;
	}

	@Override
	public int getNumSings() {
		return sing_count;
	}

	@Override
	public void printResults() {
		if(this.minCurvature!= Double.MAX_VALUE || this.maxCurvature != Double.MIN_VALUE)
			System.out.format("Plotter: Min curvature %6.3g max %6.3g %n",minCurvature,maxCurvature);
		System.out.format("Plotter: failCountA %d blowup sols %d (%d copies)%n",failCountA,blown_up_sols,blowup_copies);
		if(singIndicies.size() < 100) {
			System.out.format("Plotter: %d singlarities%n",singIndicies.size());
			singIndicies.forEach((k,sol) -> {
				double[] pos = sol.calc_pos_actual(boxclev.globalRegion,ctx);
				System.out.format("Sing\t%9.6f %9.6f %9.6f%n",pos[0],pos[1],pos[2]);
			});
		} else {
			System.out.format("Plotter: %d singlarities%n",singIndicies.size());
		}
		
		System.out.println("Plotter: box counts "+
				boxCounts.entrySet().stream()
				.sorted(Comparator.comparingInt(Map.Entry::getValue))
				.map(Map.Entry::toString)
				.collect(Collectors.joining(", "))+".");
	}

	
	
}
