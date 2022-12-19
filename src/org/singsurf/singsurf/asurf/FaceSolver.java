package org.singsurf.singsurf.asurf;

import java.util.List;

import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern2D;
import org.singsurf.singsurf.asurf.Converger.Solve2Dresult;
import org.singsurf.singsurf.asurf.Converger.Solve2DresultWithSig;
import org.singsurf.singsurf.asurf.Sheaf2D.QuadSheaf;

public class FaceSolver {

	static class DerivContext {
		Face_info face;
		boolean DerivFlag;
		Sheaf2D s;
		//Bern2D dx, dy, dz;
		public DerivContext(Face_info face, Bern2D aa, Bern2D dx, Bern2D dy, Bern2D dz) {
			super();
			this.face = face;
			this.s = new Sheaf2D(aa,dx,dy,dz);
		}
		
		public DerivContext(Face_info face, Sheaf2D s2) {
			this.face = face;
			this.s = s2;
		}

		public Sheaf2D getSheaf(Bern2D aa) {
			Sheaf2D ss = s.makeSheaf(aa, s.dx, s.dy, s.dz);
			return ss;
		}
	}

	static class Face_context {
		public Face_context(Face_info face2, List<Sol_info> sols2) {
			face = face2;
			sols = sols2;
			count = sols.size();
		}
		int count;
		Face_info face;
		List<Sol_info> sols;
	}

	static class FacePos {
		Face_info face;
		double x, y;

		public FacePos(Face_info face, double x, double y) {
			super();
			this.face = face;
			this.x = x;
			this.y = y;
		}
	}

	private static final boolean PRINT_FACE4SOL_FAILED = false;
	static final private boolean PRINT_LINK_FACE = false;

	static final private boolean PRINT_LINK_FACE_ALL = false;
	static final private boolean PRINT_LINKFACE04 = false;
	BoxClevA boxclev;
	BoxGenerator boxgen;
	Bern3DContext ctx;
	
	private int failCountA = 0;
	private int failCountB = 0;
	private int failCountC = 0;
	private int failCountD = 0;
	private int failCountE = 0;
	private int failCountF = 0;
	private int failCountG = 0;
	private int failCountH = 0;
	private int failCountI = 0;
	private int failCountJ = 0;
	private int failCountK = 0;
	private int failCountL = 0;
	private int failCountM = 0;
	private int failCountN = 0;
	private int failCountO = 0;
	
	public FaceSolver(BoxClevA boxclev, BoxGenerator boxgen, Bern3DContext ctx) {
		this.boxclev = boxclev;
		this.boxgen = boxgen;
		this.ctx = ctx;
	}

//	@SuppressWarnings("unused")
//	private void calc_2nd_derivs(Sol_info sol, Bern2D dx, Bern2D dy, Bern2D dz, Bern2D d2) throws AsurfException {
//		Bern2D dxx = null, dxy = null, dxz = null, dyy = null, dyz = null, dzz = null;
//
//		BoxClevA.log.printf("ERR: Calc 2nd derivs\n");
//		if (sol.type == FACE_LL || sol.type == FACE_RR) { /* s=y, t = z */
//			dxx = d2;
//			dxy = dx.diffX(); /* dyx */
//			dxz = dx.diffY(); /* dzx */
//			dyy = dy.diffX();
//			dyz = dy.diffY(); /* dzy */
//			dzz = dz.diffY();
//		} else if (sol.type == FACE_FF || sol.type == FACE_BB) { /* s=x, t = z */
//			dxx = dx.diffX();
//			dxy = dy.diffX();
//			dxz = dz.diffX(); /* dz dx */
//			dyy = d2;
//			dyz = dy.diffY(); /* dz dy */
//			dzz = dz.diffY();
//		} else if (sol.type == FACE_UU || sol.type == FACE_DD) {
//			dxx = dx.diffX();
//			dxy = dy.diffX(); /* dydx */
//			dxz = dz.diffX(); /* dzdx */
//			dyy = dy.diffY();
//			dyz = dz.diffY(); /* dzdy */
//			dzz = d2;
//		}
//	}

	
	static class CalcCrossRes {
		boolean DerivFlag;
		double pos_x, pos_y;
	}

	/**
	 * Calculate the intersection of two lines 
	 * @param face being studied
	 * @param fsols sols on edge to f(x,y)=0 must have at least two elements
	 * @param gsols sols of edges to g(x,y)=0, must have at least two elements
	 * @return on success point coordinate of point 
	 */
	private CalcCrossRes calcCross(Face_info face, List<Sol_info> fsols, List<Sol_info> gsols) {
		double vec0[] = face.calc_pos_on_face(fsols.get(0));
		double vec1[] = face.calc_pos_on_face(fsols.get(1));
		double vec2[] = face.calc_pos_on_face(gsols.get(0));
		double vec3[] = face.calc_pos_on_face(gsols.get(1));

		double lam = -((vec3[1] - vec2[1]) * (vec0[0] - vec2[0]) - (vec3[0] - vec2[0]) * (vec0[1] - vec2[1]))
				/ ((vec3[1] - vec2[1]) * (vec1[0] - vec0[0]) - (vec3[0] - vec2[0]) * (vec1[1] - vec0[1]));

		if (Double.isNaN(lam)) {
			return null;
		} else if (lam >= 0.0 && lam <= 1.0) {
			CalcCrossRes ccr = new CalcCrossRes();
			ccr.pos_x = lam * vec1[0] + (1.0 - lam) * vec0[0];
			ccr.pos_y = lam * vec1[1] + (1.0 - lam) * vec0[1];
			ccr.DerivFlag = false;
			return ccr;
		}
		return null;
	}

	private void combine_links(Face_info face) {
		face.links = null;

		if (face.lb.links != null)
			for (Link_info l1 : face.lb.links)
				face.include_link(l1.A, l1.B);
		if (face.lt.links != null)
			for (Link_info l1 : face.lt.links)
				face.include_link(l1.A, l1.B);
		if (face.rb.links != null)
			for (Link_info l1 : face.rb.links)
				face.include_link(l1.A, l1.B);
		if (face.rt.links != null)
			for (Link_info l1 : face.rt.links)
				face.include_link(l1.A, l1.B);
	}

	/**
	 * Work out if curves df/dx=0 and df/dy=0 cross on a face. Condition is exactly
	 * two sols for df/dx on edges and exactly two sols for df/dy and the solutions
	 * alternating round the face.
	 * 
	 * @param dc
	 * @param a_face
	 * @param da
	 * @param fa
	 * @param b_face
	 * @param db
	 * @param fb
	 * @throws AsurfException
	 */
	private void derivTest(DerivContext dc, Face_info a_face, Bern2D da, int fa, Face_info b_face, Bern2D db, int fb)
			throws AsurfException {
		int a_count = 0, b_count = 0;
//		Sol_info a_sols[] = new Sol_info[2], b_sols[] = new Sol_info[2];
		
		if (dc.DerivFlag && fa == 0 && fb == 0 && (da.xord != 0 || da.yord != 0) && (db.xord != 0 || db.yord != 0)) {
			if (a_face == null) {
				a_face = calcDerivFace(dc, da);
				List<Sol_info >a_sols = a_face.get_all_sols_on_edges();
				a_count = a_sols.size();
//				a_count = boxclev.topology.get_sols_on_face(a_face, a_sols);
				if (a_count == 2) {
					if (b_face == null) {
						b_face = calcDerivFace(dc, db);
						List<Sol_info> b_sols = b_face.get_all_sols_on_edges();
						b_count = b_sols.size();
//						b_count = boxclev.topology.get_sols_on_face(b_face, b_sols);
						if (b_count == 2) {
							calcCross(dc.face, a_sols, b_sols);
						} else if (b_count != 0)
							dc.DerivFlag = false;
					}
				} else if (a_count != 0)
					dc.DerivFlag = false;
			}
		}
	}

	private void fillSolWith2Dres(Sol_info sol, Solve2DresultWithSig cres) {
		sol.conv_failed = !cres.good;
		sol.setRoots(cres.x, cres.y);
		sol.setDerivs(cres.sig_x,cres.sig_y,cres.sig_z);
		sol.setValue(cres.f_val);
	}

	private FacePos calcMidPoint(Face_context fc) {
		double pos_x = 0.0, pos_y = 0.0;
		for (int i = 0; i < fc.count; ++i) {
			double[] vec = fc.face.calc_pos_on_face(fc.sols.get(i));
			pos_x += vec[0];
			pos_y += vec[1];
		}
		if (fc.count == 0) {
			pos_x = pos_y = 0.5;
		} else {
			pos_x /= fc.count;
			pos_y /= fc.count;
		}
		if (pos_x == 0.0 || pos_x == 1.0)
			pos_x = 0.5;
		if (pos_y == 0.0 || pos_y == 1.0)
			pos_y = 0.5;
		return new FacePos(fc.face, pos_x, pos_y);
	}

	private FacePos interp(Face_context fc, Sol_info sol1, Sol_info sol2, int num, int steps) {
		double vec1[] = fc.face.calc_pos_on_face(sol1);
		double vec2[] = fc.face.calc_pos_on_face(sol2);

		double pos_x = vec1[0] + (num) / (steps + 1.0) * (vec2[0] - vec1[0]);
		double pos_y = vec1[1] + (num) / (steps + 1.0) * (vec2[1] - vec1[1]);
		return new FacePos(fc.face, pos_x, pos_y);
	}

	public void link_face(Face_info big_face, Face_info face, Sheaf2D s, boolean internal) 
			throws AsurfException {
		Bern2D bb = s.aa;
		Bern2D dx = s.dx;
		Bern2D dy = s.dy;
		Bern2D dz = s.dz;
		
		int f1, f2, f3, count;
		
//		Sol_info sols[] = new Sol_info[5];
		if (bb.allOneSign() != 0)
			return;
		f1 = dx.allOneSign();
		f2 = dy.allOneSign();
		f3 = dz.allOneSign();

		List<Sol_info> sols =  face.get_all_sols_on_edges();
		count = sols.size();

		final Sol_info sol0 = count > 0 ? sols.get(0) : null;
		final Sol_info sol1 = count > 1 ? sols.get(1) : null;
		final Sol_info sol2 = count > 2 ? sols.get(2) : null;
		final Sol_info sol3 = count > 3 ? sols.get(3) : null;

		if (PRINT_LINK_FACE_ALL) {
			BoxClevA.log.printf("ERR: link_face: ");
			BoxClevA.log.print(face.type);
			BoxClevA.log.printf(" (%d,%d,%d)/%d count %d f1 %d f2 %d f3 %d\n", face.xl, face.yl, face.zl, face.denom,
					count, f1, f2, f3);
		}

		switch (count) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			if (f1 == 0 && f2 == 0 && f3 == 0)
				break;

			if (sol0.match_derivs(sol1) 
					&& sol0.match_derivs(f1, f2, f3)) {
				face.include_link(sol0, sol1);
				
				calcRotatedDerivs(face,s);
//				calcRotatedDerivs(face,dx,dz);
//				calcRotatedDerivs(face,dy,dz);
				
				return;
			}
			break;
		case 3:
			break;
		case 4:

			if (f1 == 0 && f2 == 0 && f3 == 0) {
			}
			else {
				if (sol0.match_derivs(sol1) && sol2.match_derivs(sol3)) {
					if (sol0.match_derivs(sol2)) {
						
					}
					else if (straddleDeriv(sol0, sol2, f1, f2, f3)) {
						face.include_link(sol0, sol1);
						face.include_link(sol2, sol3);
						return;
					} 

				} else if (sol0.match_derivs(sol2) && sol1.match_derivs(sol3)) {
					if (straddleDeriv(sol0, sol1, f1, f2, f3)) {
						face.include_link(sol0, sol2);
						face.include_link(sol1, sol3);
						return;
					}

				} else if (sol0.match_derivs(sol3) && sol1.match_derivs(sol2)) {
					if (straddleDeriv(sol0, sol1, f1, f2, f3)) {
						face.include_link(sol0, sol3);
						face.include_link(sol1, sol2);
						return;
					}
				}
			}
			break;
		default:
			break;
		}

			if (face.denom < boxclev.LINK_FACE_LEVEL) {
				// String s = big_face.toString();
				ReduceFace(big_face, face, s, internal, f1, f2, f3);
			} else {
				
//				calcRotatedDerivs(face,dx,dy);
//				calcRotatedDerivs(face,dx,dz);
//				calcRotatedDerivs(face,dy,dz);
				
				switch (count) {
				case 0: {
//					if (face.denom < boxclev.LINK_EDGE_LEVEL) {
//						ReduceFace(big_face, face, bb, dx, dy, dz, d2, internal, f1, f2, f3);
//					} else {
						link_face0sols(face, sols, s, f1, f2, f3);
//					}
					break;
				}
				case 2:
					link_face2sols(face, sols, s, f1, f2, f3);
					break;
				case 3:
					link_face3sols(face, sols, s, f1, f2, f3);
					break;
				case 4:
					link_face4sols(face, sols, s, f1, f2, f3);
					break;
				default:
					link_facemanysols(face, sols, s, f1, f2, f3);
					break;
				}
			}
		// fini_link_face:
	}

	private void link_face0sols(Face_info face, List<Sol_info> sols, Sheaf2D s, int f1,
			int f2, int f3) throws AsurfException {
		DerivContext dc = new DerivContext(face,s);

		Face_info x_face = null, y_face = null, z_face = null;
		if(f1!=0 || f2!=0 || f3 !=0 ) return;
		double pos_x = 0.0, pos_y = 0.0;
		int sign;
		Bern2D det, dxs, dxt, dys, dyt, dzs, dzt, dss, dst, dtt;
		Bern2D dxx, dxy, dxz, dyy, dyz, dzz;

		if (PRINT_LINKFACE04) {
			BoxClevA.log.printf("link0: %d %d %d\n", f1, f2, f3);
			BoxClevA.log.print(face);
		}
				
		final Bern2D dx = s.dx;
		final Bern2D dy = s.dy;
		final Bern2D dz = s.dz;
		switch (face.type) {
		case FACE_LL:
		case FACE_RR:
			dss = dy.diffX();
			dst = dy.diffY();
			dtt = dz.diffY();
			dxs = dx.diffX();
			dxt = dx.diffY();
			dys = dss;
			dyt = dst;
			dzs = dst;
			dzt = dtt;
			dxx = s instanceof SheafSecondDeriv ? ((SheafSecondDeriv) s).d2 : Bern2D.zeroBern2D; 
			dxy = dxs; 
			dxz = dxt;
			dyy = dss; dyz = dst; dzz = dtt;
			break;
		case FACE_FF:
		case FACE_BB:
			dss = dx.diffX();
			dst = dx.diffY();
			dtt = dz.diffY();
			dxs = dss;
			dxt = dst;
			dys = dy.diffX();
			dyt = dy.diffY();
			dzs = dst;
			dzt = dtt;
			dxx = dss; dxy = dys; dxz = dtt;
			dyy = s instanceof SheafSecondDeriv ? ((SheafSecondDeriv) s).d2 : Bern2D.zeroBern2D;
			dyz = dyt; dzz = dtt;
			
			break;
		case FACE_UU:
		case FACE_DD:
			dss = dx.diffX();
			dst = dx.diffY();
			dtt = dy.diffY();
			dxs = dss;
			dxt = dst;
			dys = dst;
			dyt = dtt;
			dzs = dz.diffX();
			dzt = dz.diffY();
			dxx = dss; dxy = dst; dxz = dzs;
			dyy = dtt; dyz = dzt; 
			dzz = s instanceof SheafSecondDeriv ? ((SheafSecondDeriv) s).d2 : Bern2D.zeroBern2D;
			break;
		default:
			throw new AsurfException("Bad face type");
		}

		x_face = calcDerivFace(face, dx,dxx,dxy,dxz);
		if(x_face.count_sol()<2) return;
		y_face = calcDerivFace(face, dy, dxy,dyy,dyz);
		if(y_face.count_sol()<2) return;
		z_face = calcDerivFace(face, dz, dxz,dyz,dzz);
		if(z_face.count_sol()<2) return;
				
		// Determinant of the 2D Hessian of the face
		det = Bern2D.symetricDet2D(dss, dst, dtt);
		if (det == null) {
			BoxClevA.log.printf("ERR: null det\n");
			BoxClevA.log.printf("link_face0sols: %d %d %d\n", f1, f2, f3);
			BoxClevA.log.print(face);
			BoxClevA.log.print(dx);
			BoxClevA.log.print(dy);
			BoxClevA.log.print(dz);
			BoxClevA.log.print(dxx);
			BoxClevA.log.print(dxy);
			BoxClevA.log.print(dyy);
			sign = 0;
			throw new AsurfException("Null det in link face 0 sols");
		} else
			sign = det.allOneSign();
		if (sign < 0)
			return;

		Solve2DresultWithSig conv = boxgen.converger.converge_node_just_three_deriv(new FacePos(face,0.5,0.5), s, 0, 0, 0);


		dc.DerivFlag = true;
		derivTest(dc, x_face, dx, f1, y_face, dy, f2);
		derivTest(dc, x_face, dx, f1, z_face, dz, f3);
		derivTest(dc, y_face, dy, f2, z_face, dz, f3);

//		if (dc.DerivFlag) {
//			if (PRINT_LINKFACE04) {
//				BoxClevA.log.printf("ERR: DerivFlag %d\n", dc.DerivFlag);
//			}
//			return;
//		}
		/*
		 * if( pos_x != pos_x || pos_y != pos_y )
		 * BoxClevA.log.printf("ERR: pos_x %f pos_y %f\n",pos_x,pos_y);
		 */
//		if (pos_x == 0.0 || pos_x == 1.0 || pos_y == 0.0 || pos_y == 1.0) {
//			if (PRINT_LINKFACE04) {
//				BoxClevA.log.printf("ERR: Pos on boundary %f %f\n", pos_x, pos_y);
//			}
//			return;
//		}
		Sol_info nodesol = MakeNode(face, pos_x, pos_y, f1, f2, f3, s);

		fillSolWith2Dres(nodesol, conv);
		if (!conv.good) {
			if (PRINT_LINKFACE04) {
				BoxClevA.log.printf("ERR: conv_failed\n");
			}
		} else
			face.add_node(nodesol);

		if (PRINT_LINKFACE04) {
			BoxClevA.log.printf("ERR: link_face: count %d f1 %d f2 %d f3 %d\n", 0, f1, f2, f3);
			BoxClevA.log.println(nodesol);
			/*
			 * BoxClevA.log .print(x_face); BoxClevA.log .print(y_face); BoxClevA.log
			 * .print(z_face);
			 */
		}
	}

//	private void calcRotatedDerivs(Face_info face, Bern2D dx, Bern2D dy) throws AsurfException {
//		if(dx.getClass()!=Bern2D.class || dy.getClass()!=Bern2D.class)
//			return;
//		
//		Bern2D du = Bern2D.addBern2D(dx, dy);
//		Bern2D dv = Bern2D.subtractBern2D(dx, dy);
//		Bern2D dudx = du.diffX();
//		Bern2D dudy = du.diffY();
//		Bern2D dvdx = dv.diffX();
//		Bern2D dvdy = dv.diffY();
//		
//		Face_info p_face = new Face_info(face);
//		boxclev.topology.create_new_edges(p_face);
//		Sheaf2D su = new Sheaf2D(du,dudx)
//		boxgen.find_edge(p_face.x_low, du, dudx, dudy, Face_info.Type.X_LOW);
//		boxgen.find_edge(p_face.x_high, du, dudx, dudy, Face_info.Type.X_HIGH);
//		boxgen.find_edge(p_face.y_low, du, dudx, dudy, Face_info.Type.Y_LOW);
//		boxgen.find_edge(p_face.y_high, du, dudx, dudy, Face_info.Type.Y_HIGH);
//
//		Face_info q_face = new Face_info(face);
//		boxclev.topology.create_new_edges(q_face);
//		boxgen.find_edge(q_face.x_low, dv, dvdx, dvdy, Face_info.Type.X_LOW);
//		boxgen.find_edge(q_face.x_high, dv, dvdx, dvdy, Face_info.Type.X_HIGH);
//		boxgen.find_edge(q_face.y_low, dv, dvdx, dvdy, Face_info.Type.Y_LOW);
//		boxgen.find_edge(q_face.y_high, dv, dvdx, dvdy, Face_info.Type.Y_HIGH);
//
//		List<Sol_info> list = new ArrayList<>();
//		p_face.x_low.add_sols_to_list(list);
//		p_face.x_high.add_sols_to_list(list);
//		p_face.y_low.add_sols_to_list(list);
//		p_face.y_high.add_sols_to_list(list);
//
//		q_face.x_low.add_sols_to_list(list);
//		q_face.x_high.add_sols_to_list(list);
//		q_face.y_low.add_sols_to_list(list);
//		q_face.y_high.add_sols_to_list(list);
//		
//		list.forEach(sol ->  {
//			System.out.println(sol.toString(boxclev.globalRegion));
//			boxclev.plotter.plot_point(sol);
//		}
//		);
//}

	private void link_face2sols(Face_info face, List<Sol_info> sols, Sheaf2D s, int f1,
			int f2, int f3) throws AsurfException {
		Face_context fc = new Face_context(face,sols);
		int dxSign, dySign, dzSign;

		if (PRINT_LINKFACE04) {
			BoxClevA.log.printf("ERR: link_face: count %d f1 %d f2 %d f3 %d\n", 2, f1, f2, f3);
			BoxClevA.log.print(face);
		}
		final Sol_info sol0 = sols.get(0);
		final Sol_info sol1 = sols.get(1);
		
		if (sol0.match_derivs(sol1)) {
			face.include_link(sol0, sol1);
			return;
		}

		FacePos fp = calcMidPoint(fc);

		dxSign = f1;
		dySign = f2;
		dzSign = f3;
		if (sol0.getDx() == sol1.getDx())
			dxSign = sol0.getDx();
		if (sol0.getDy() == sol1.getDy())
			dySign = sol0.getDy();
		if (sol0.getDz() == sol1.getDz())
			dzSign = sol0.getDz();

		int zeroCount = (dxSign == 0 ? 1 : 0) + (dySign == 0 ? 1 : 0) + (dzSign == 0 ? 1 : 0);
		/*
		 * f1 = f1a; f2 = f2a; f3 = f3a;
		 */
		/* do we want a duplicate node */
		if (zeroCount == 3 
				|| sol0.getDx() == 0 || sol0.getDy() == 0 || sol0.getDz() == 0 
				|| sol1.getDx() == 0 || sol1.getDy() == 0 || sol1.getDz() == 0) {
			Solve2DresultWithSig res2;

			Sol_info nodesol = MakeNode(face, fp.x, fp.y, f1, f2, f3, s);
			res2 = boxgen.converger.converge_node(fp, s, f1, f2, f3);
			fillSolWith2Dres(nodesol, res2);
			if (!res2.good) {
				if (this.failCountN++ == 0) {
					BoxClevA.log.println("ERR: link_face2: default conv failed N");
					BoxClevA.log.println(nodesol);
				}

			}
			face.add_node(nodesol);
			face.include_link(sol0, nodesol);
			face.include_link(sol1, nodesol);
			if (PRINT_LINKFACE04) {
				BoxClevA.log.printf("ERR: link_face2sols: All three zero conv %d\n", res2);
				BoxClevA.log.print(face.print_face_brief());
			}
			return;
		}

		if (zeroCount == 2) {
			link_face2sols_2_deriv(face, sols, s, f1, f2, f3, fc, dxSign, dySign, dzSign);
			return;
		}

		// zeroCount == 1
		if (f1 != dxSign || f2 != dySign || f3 != dzSign) {
			Sol_info nodesol = MakeNode(face, fp.x, fp.y, dxSign, dySign, dzSign, s);
			Solve2DresultWithSig res2 = boxgen.converger.converge_node(fp, s, dxSign, dySign, dzSign);
			fillSolWith2Dres(nodesol, res2);
			if (!res2.good) {
				nodesol.setDerivs(f1,f2,f3); // Assumption about linking may be incorrect
				Solve2DresultWithSig res3 = boxgen.converger.converge_node(fp, s, f1, f2, f3);

				if (!res3.good) {
					nodesol.conv_failed = true;

					if (failCountI++ == 0) {
						BoxClevA.log.println("ERR: link_face2: default conv failed I");
						BoxClevA.log.println(nodesol);
					}
				} else {
					fillSolWith2Dres(nodesol, res3);
				}

			}
			face.add_node(nodesol);
			face.include_link(sol0, nodesol);
			face.include_link(sol1, nodesol);
			return;

		}

		{
		// The normal case where one deriv vanishes
		Sol_info nodesol = MakeNode(face, fp.x, fp.y, f1, f2, f3, s);
		Solve2DresultWithSig res2 = boxgen.converger.converge_node_exactly_one_deriv(fp, s, f1, f2, f3);
		fillSolWith2Dres(nodesol, res2);
		if (!res2.good) { 
			if (failCountJ++ == 0) {
				BoxClevA.log.println("ERR: link_face2: default conv failed J");
				BoxClevA.log.println(nodesol);
			}
		}
		face.add_node(nodesol);
		face.include_link(sol0, nodesol);
		face.include_link(sol1, nodesol);
		}
	}

	private void link_face2sols_2_deriv(Face_info face, List<Sol_info> sols, Sheaf2D s, int f1, int f2, int f3, Face_context fc, int dxSign, int dySign, int dzSign)
			throws AsurfException {
		Solve2DresultWithSig res1 = null, res2 = null, resBoth = null; 
		double vec1[], vec2[], dist1, dist2, dist3, dist4;
		FacePos fp = calcMidPoint(fc);
		Sol_info nodeA = MakeNode(face, fp.x, fp.y, f1, f2, f3, s);
		Sol_info nodeB = MakeNode(face, fp.x, fp.y, f1, f2, f3, s);
		Sol_info nodeC = MakeNode(face, fp.x, fp.y, dxSign, dySign, dzSign, s);
		final Sol_info sol0 = sols.get(0);
		final Sol_info sol1 = sols.get(1);
		vec1 = face.calc_pos_on_face(sol0);
		vec2 = face.calc_pos_on_face(sol1);
		if (dxSign == 0 && dySign == 0) {
			resBoth = boxgen.converger.converge_node_two_deriv(fp, s, 0, 0, dzSign);
			res1 = boxgen.converger.converge_node_exactly_one_deriv(fp, s, 0, 1, 1);
			res2 = boxgen.converger.converge_node_exactly_one_deriv(fp, s, 1, 0, 1);
		}
		if (dxSign == 0 && dzSign == 0) {
			resBoth = boxgen.converger.converge_node_two_deriv(fp, s, 0, dySign, 0);
			res1 = boxgen.converger.converge_node_exactly_one_deriv(fp, s, 0, 1, 1);
			res2 = boxgen.converger.converge_node_exactly_one_deriv(fp, s, 1, 1, 0);
		}
		if (dySign == 0 && dzSign == 0) {
			resBoth = boxgen.converger.converge_node_two_deriv(fp, s, dxSign, 0, 0);
			res1 = boxgen.converger.converge_node_exactly_one_deriv(fp, s, 1, 0, 1);
			res2 = boxgen.converger.converge_node_exactly_one_deriv(fp, s, 1, 1, 0);
		}
		if (resBoth.good) {
			fillSolWith2Dres(nodeC, resBoth); 
			face.add_node(nodeC);
			face.include_link(sol0, nodeC);
			face.include_link(sol1, nodeC);
			return;
		}

		if (!res1.good || !res2.good) {
			link_face2sols_conv_failed(face, sols, s, f1, f2, f3, fc);
			return;
		}

		fillSolWith2Dres(nodeA, res1);
		fillSolWith2Dres(nodeB, res2);
		fillSolWith2Dres(nodeC, resBoth);

		dist1 = Math.sqrt((vec1[0] - nodeA.getRoot()) * (vec1[0] - nodeA.getRoot())
				+ (vec1[1] - nodeA.getRoot2()) * (vec1[1] - nodeA.getRoot2()));
		dist2 = Math.sqrt((vec1[0] - nodeB.getRoot()) * (vec1[0] - nodeB.getRoot())
				+ (vec1[1] - nodeB.getRoot2()) * (vec1[1] - nodeB.getRoot2()));
		dist3 = Math.sqrt((vec2[0] - nodeA.getRoot()) * (vec2[0] - nodeA.getRoot())
				+ (vec2[1] - nodeA.getRoot2()) * (vec2[1] - nodeA.getRoot2()));
		dist4 = Math.sqrt((vec2[0] - nodeB.getRoot()) * (vec2[0] - nodeB.getRoot())
				+ (vec2[1] - nodeB.getRoot2()) * (vec2[1] - nodeB.getRoot2()));
		if (dist1 < dist2 && dist4 < dist3) {
			if (dxSign == 0 && dySign == 0) {
				nodeA.setDerivs(0,sol0.getDy(), dzSign);
				nodeB.setDerivs(sol1.getDx(),0, dzSign);				
//				nodeA.dx = 0;
//				nodeA.dy = sols.get(0).getDy();
//				nodeB.dx = sols.get(1).getDx();
//				nodeB.dy = 0;
//				nodeA.dz = nodeB.dz = dzSign;
			}
			if (dxSign == 0 && dzSign == 0) {
				nodeA.setDerivs(0, dySign, sol0.getDz());
				nodeB.setDerivs(sol1.getDx(), dySign, 0);				
//				nodeA.dx = 0;
//				nodeB.dx = sols.get(1).getDx();
//				nodeA.dy = nodeB.dy = dySign;
//				nodeA.dz = sols.get(0).getDz();
//				nodeB.dz = 0;
			}
			if (dySign == 0 && dzSign == 0) {
				nodeA.setDerivs(dxSign, 0, sol0.getDz());
				nodeB.setDerivs(dxSign, sol1.getDy(),0);				
//				nodeA.dx = nodeB.dx = dxSign;
//				nodeA.dy = 0;
//				nodeB.dy = sols.get(1).getDy();
//				nodeA.dz = sols.get(0).getDz();
//				nodeB.dz = 0;
			}
			face.add_node(nodeA);
			face.add_node(nodeB);
			face.include_link(sol0, nodeA);
			face.include_link(nodeA, nodeB);
			face.include_link(nodeB, sol1);
		} else if (dist1 > dist2 && dist4 > dist3) {
			if (dxSign == 0 && dySign == 0) {
				nodeA.setDerivs(0,sol1.getDy(), dzSign);
				nodeB.setDerivs(sol0.getDx(),0, dzSign);				
//				nodeA.dx = 0;
//				nodeB.dx = sols.get(0).getDx();
//				nodeA.dy = sols.get(1).getDy();
//				nodeB.dy = 0;
//				nodeA.dz = nodeB.dz = dzSign;
			}
			if (dxSign == 0 && dzSign == 0) {
				nodeA.setDerivs(0, dySign, sol1.getDz());
				nodeB.setDerivs(sol0.getDx(), dySign, 0);				
//				nodeA.dx = 0;
//				nodeB.dx = sols.get(0).getDx();
//				nodeA.dy = nodeB.dy = dySign;
//				nodeA.dz = sols.get(1).getDz();
//				nodeB.dz = 0;
			}
			if (dySign == 0 && dzSign == 0) {
				nodeA.setDerivs(dxSign, 0, sol1.getDz());
				nodeB.setDerivs(dxSign, sol0.getDy(), 0);				
//				nodeA.dx = nodeB.dx = dxSign;
//				nodeA.dy = 0;
//				nodeB.dy = sols.get(0).getDy();
//				nodeA.dz = sols.get(1).getDz();
//				nodeB.dz = 0;
			}
			face.add_node(nodeA);
			face.add_node(nodeB);
			face.include_link(sol1, nodeA);
			face.include_link(nodeA, nodeB);
			face.include_link(nodeB, sol0);
		} else {
			if (PRINT_LINKFACE04) {
				BoxClevA.log.println(sol0);
				BoxClevA.log.println(sol1);
				BoxClevA.log.println(nodeA);
				BoxClevA.log.println(nodeB);
			}
			nodeB = MakeNode(face, fp.x, fp.y, f1, f2, f3, s);
			Solve2DresultWithSig res4 = boxgen.converger.converge_node(fp, s, f1, f2, f3);
			if (!res4.good) {
				if (failCountH++ == 0) {
					BoxClevA.log.printf("ERR: link_face2sols: Wierd distances %f %f %f %f\n", dist1, dist2, dist3, dist4);
					BoxClevA.log.println("ERR: link_face2: default conv failed H");
					BoxClevA.log.println(nodeB);
				}
			}

			face.add_node(nodeB);
			face.include_link(sol0, nodeB);
			face.include_link(sol1, nodeB);
		}
		if (PRINT_LINKFACE04) {
			BoxClevA.log.printf("ERR: link_face2sols: added two nodes %d %d %d %d %d\n", f1, f2, f3, res1, res2);
			BoxClevA.log.print(face.print_face_brief());
		}
		return;
	}

	private void link_face2sols_conv_failed(Face_info face, List<Sol_info> sols, Sheaf2D s, 
			int f1, int f2, int f3, Face_context fc) throws AsurfException {
		if (failCountO++ == 0) {
			BoxClevA.log.printf("ERR: link_face2sols: converge failed! %d %d %d\n", f1, f2, f3);
//			BoxClevA.log.println(sols.get(0).toStringNorm(boxclev));
//			BoxClevA.log.println(sols.get(1).toStringNorm(boxclev));
		}
		int bit_swaps = 0;
		if (sols.get(0).getDx() != sols.get(1).getDx())
			++bit_swaps;
		if (sols.get(0).getDy() != sols.get(1).getDy())
			++bit_swaps;
		if (sols.get(0).getDz() != sols.get(1).getDz())
			++bit_swaps;
		FacePos fp = calcMidPoint(fc);

		int dx1 = sols.get(0).getDx(), dy1 = sols.get(0).getDy(), dz1 = sols.get(0).getDz();
		int pos = 1;
		Sol_info last_sol = sols.get(0);
		Sol_info nodesol = null;
		if (sols.get(0).getDx() != sols.get(1).getDx()) {
			interp(fc, sols.get(0), sols.get(1), pos, bit_swaps);
			nodesol = MakeNode(face, fp.x, fp.y, 0, dy1, dz1, s);
			nodesol.conv_failed = true;
			face.add_node(nodesol);
			face.include_link(last_sol, nodesol);
			last_sol = nodesol;
			++pos;
			dx1 = -dx1;
		}
		if (sols.get(0).getDy() != sols.get(1).getDy()) {
			interp(fc, sols.get(0), sols.get(1), pos, bit_swaps);
			nodesol = MakeNode(face, fp.x, fp.y, dx1, 0, dz1, s);
			nodesol.conv_failed = true;
			face.add_node(nodesol);
			face.include_link(last_sol, nodesol);
			last_sol = nodesol;
			++pos;
			dy1 = -dy1;
		}
		if (sols.get(0).getDz() != sols.get(1).getDz()) {
			interp(fc, sols.get(0), sols.get(1), pos, bit_swaps);
			nodesol = MakeNode(face, fp.x, fp.y, dx1, dy1, 0, s);
			nodesol.conv_failed = true;
			face.add_node(nodesol);
			face.include_link(last_sol, nodesol);
			last_sol = nodesol;
			++pos;
			dz1 = -dz1;
		}
		face.include_link(last_sol, sols.get(1));
		return;
	}


	/**
	 * @param bb
	 */
	private void link_face3sols(Face_info face, List<Sol_info> sols, Sheaf2D s, int f1,
			int f2, int f3) throws AsurfException {
		Face_context fc = new Face_context(face,sols);
//		fc.face = face;
//		fc.sols = sols;

		FacePos fp = calcMidPoint(fc);
		/*
		 * if( pos_x != pos_x || pos_y != pos_y )
		 * BoxClevA.log.println("ERR: pos_x %f pos_y %f\n",pos_x,pos_y);
		 */
		Sol_info nodesol = MakeNode(face, fp.x, fp.y, f1, f2, f3, s);
		Solve2DresultWithSig res2 = boxgen.converger.converge_node(fp, s, f1, f2, f3);
		fillSolWith2Dres(nodesol, res2);
		if (!res2.good) {
			if (failCountK++ == 0) {
				BoxClevA.log.println("ERR: link_face2: default conv failed K");
				BoxClevA.log.println(nodesol);
			}
		}

		face.add_node(nodesol);
		face.include_link(sols.get(0), nodesol);
		face.include_link(sols.get(1), nodesol);
		face.include_link(sols.get(2), nodesol);
		if (PRINT_LINK_FACE) {
			BoxClevA.log.printf("ERR: link_face3sols: count %d f1 %d f2 %d f3 %d\n", fc.count, f1, f2, f3);
			BoxClevA.log.println(nodesol);
			BoxClevA.log.println(sols.get(0));
			BoxClevA.log.println(sols.get(1));
			BoxClevA.log.println(sols.get(2));
		}

	}

	private void link_face4sols(Face_info face, List<Sol_info> sols, Sheaf2D s, int f1,
			int f2, int f3) throws AsurfException {
		Face_info x_face = null, y_face = null, z_face = null;
		double vec[], pos_x = 0.5, pos_y = 0.5;
		Bern2D dxx = null, dxy = null, dyy = null, det = null;
		int sign, order[] = new int[4];
		Solve2DresultWithSig res1;
		char signStr[] = new char[80];

		int sig_x = sols.get(0).getDx(), sig_y = sols.get(0).getDy(), sig_z = sols.get(0).getDz();
		for (int i = 0; i < 4; ++i) {
			if (sols.get(i).getDx() != sig_x)
				sig_x = 0;
			if (sols.get(i).getDy() != sig_y)
				sig_y = 0;
			if (sols.get(i).getDz() != sig_z)
				sig_z = 0;
		}
		// sig_x = f1; sig_y = f2; sig_z = f3;

		SignTest.BuildSolSigns(sols, signStr);
		if (SignTest.TestSigns(signStr, 4, 3, "+++|+++|++-|+--", "+++|++-|+-+|+--|-++|-+-|--+|---", "abc|bca|cab",
				order)
				|| SignTest.TestSigns(signStr, 4, 3, "+++|+++|+-+|+--", "+++|++-|+-+|+--|-++|-+-|--+|---",
						"abc|bca|cab", order)) {
			// BoxClevA.log.printf("ERR: Node and Link\n");
			face.include_link(sols.get(order[0]), sols.get(order[1]));
			Sol_info solA = sols.get(order[2]);
			vec = face.calc_pos_on_face(solA);
			pos_x = vec[0];
			pos_y = vec[1];

			// double val = bb.evalbern2D(vec);
			// double dfdx = dx.evalbern2D(vec);
			// double dfdy = dy.evalbern2D(vec);
			// double dfdz = dz.evalbern2D(vec);
			// BoxClevA.log.println(solA);
			// BoxClevA.log.printf("[%9.6f, %9.6f] %9.6f%n",
			// vec[0],vec[1],val);

			Sol_info solB = sols.get(order[3]);
			vec = face.calc_pos_on_face(solB);
			pos_x += vec[0];
			pos_y += vec[1];
			pos_x /= 2;
			pos_y /= 2;

			vec = face.calc_pos_on_face(solB);
			// val = bb.evalbern2D(vec);
			// dfdx = dx.evalbern2D(vec);
			// dfdy = dy.evalbern2D(vec);
			// dfdz = dz.evalbern2D(vec);
			// BoxClevA.log.println(solB);
			// BoxClevA.log.printf("[%9.6f, %9.6f] %9.6f%n",
			// vec[0],vec[1],val);
			// BoxClevA.log.printf("[%9.6f, %9.6f] %9.6f dx %9.6f %9.6f %9.6f%n",
			// vec[0],vec[1],val,dfdx,dfdy,dfdz);
			int f1a = solA.getDx() == solB.getDx() ? solA.getDx() : 0;
			int f2a = solA.getDy() == solB.getDy() ? solA.getDy() : 0;
			int f3a = solA.getDz() == solB.getDz() ? solA.getDz() : 0;
			Sol_info nodesol = MakeNode(face, pos_x, pos_y, f1a, f2a, f3a, s);
			Solve2DresultWithSig res2 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s,
					f1a, f2a, f3a);
			this.fillSolWith2Dres(nodesol, res2);
			if (!res2.good) {
				if (failCountA++ == 0) {
					BoxClevA.log.println("ERR: link_face2: default conv failed A");
					BoxClevA.log.println(nodesol);
				}
			} else {
				face.add_node(nodesol);
				face.include_link(sols.get(order[2]), nodesol);
				face.include_link(sols.get(order[3]), nodesol);
				if (PRINT_LINKFACE04) {
					BoxClevA.log.print(face);
				}
				return;
			}
		} else if (SignTest.TestSigns(signStr, 4, 3, "+++|+++|++-|+-+", "+++|++-|+-+|+--|-++|-+-|--+|---",
				"abc|bca|cab", order)) {
			// BoxClevA.log.printf("ERR: 2 Nodes and a Link\n");

			face.include_link(sols.get(order[0]), sols.get(order[1]));
			Sol_info solA = sols.get(order[2]);
			vec = face.calc_pos_on_face(solA);
			pos_x = vec[0];
			pos_y = vec[1];

			Sol_info solB = sols.get(order[3]);
			vec = face.calc_pos_on_face(solB);
			pos_x += vec[0];
			pos_y += vec[1];
			pos_x /= 2;
			pos_y /= 2;

			int f1a = solA.getDx() == solB.getDx() ? solA.getDx() : 0;
			int f2a = solA.getDy() == solB.getDy() ? solA.getDy() : 0;
			int f3a = solA.getDz() == solB.getDz() ? solA.getDz() : 0;

			Sol_info nodesol = MakeNode(face, pos_x, pos_y, f1a, f2a, f3a, s);
			Solve2DresultWithSig res2 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s,
					f1a, f2a, f3a);
			this.fillSolWith2Dres(nodesol, res2);
			if (res2.good) {
				if (failCountB++ == 0) {
					BoxClevA.log.println("ERR: link_face2: default conv failed B");
					BoxClevA.log.println(nodesol);
				}
			}

			face.add_node(nodesol);
			face.include_link(sols.get(order[2]), nodesol);
			face.include_link(sols.get(order[3]), nodesol);
			if (PRINT_LINKFACE04) {
				BoxClevA.log.print(face);
			}
			return;
		}

		final Bern2D dx = s.dx;
		final Bern2D dy = s.dy;
		final Bern2D dz = s.dz;
		switch (face.type) {
		case FACE_LL:
		case FACE_RR:
			if (f2 != 0 || f3 != 0)
				break;
			dxx = dy.diffX();
			dxy = dy.diffY();
			dyy = dz.diffY();
			break;
		case FACE_FF:
		case FACE_BB:
			if (f1 != 0 || f3 != 0)
				break;
			dxx = dx.diffX();
			dxy = dx.diffY();
			dyy = dz.diffY();
			break;
		case FACE_UU:
		case FACE_DD:
			if (f1 != 0 || f2 != 0)
				break;
			dxx = dx.diffX();
			dxy = dx.diffY();
			dyy = dy.diffY();
			break;
		case BOX:
			break;
		case NONE:
			break;
		case VERTEX:
			break;
		case X_AXIS:
			break;
		case Y_AXIS:
			break;
		case Z_AXIS:
			break;
		default:
			break;
		}
		if (dxx != null)
			det = Bern2D.symetricDet2D(dxx, dxy, dyy);
		if (det == null) {
			if (PRINT_LINKFACE04) {
				BoxClevA.log.printf("ERR: null det\n");
			}
			sign = 0;
		} else
			sign = det.allOneSign();

		if (PRINT_LINKFACE04) {
			BoxClevA.log.printf("ERR: link4: %d %d %d %d\n", f1, f2, f3, sign);
			BoxClevA.log.print(face);
		}
		if (sign > 0) {
			link_face4solsPos(face, sols, s, f1, f2, f3);
			return;
		}
		if (sign == 0) {
			if (PRINT_LINKFACE04) {
				BoxClevA.log.printf("ERR: Zero det\n");
				BoxClevA.log.print(face);
				BoxClevA.log.print(s.aa);
				BoxClevA.log.print(dx);
				BoxClevA.log.print(dy);
				BoxClevA.log.print(dz);
				BoxClevA.log.print(dxx);
				BoxClevA.log.print(dxy);
				BoxClevA.log.print(dyy);
				BoxClevA.log.print(det);
			}
		}

		DerivContext dc = new DerivContext(face,s);
		Sol_info nodesol;
		if (sols.get(0).match_derivs(sols.get(1)) && sols.get(2).match_derivs(sols.get(3)) && !sols.get(0).match_derivs(sols.get(2))) {
			dc.DerivFlag = true;
			derivTest(dc, x_face, dx, f1, y_face, dy, f2);
			derivTest(dc, x_face, dx, f1, z_face, dz, f3);
			derivTest(dc, y_face, dy, f2, z_face, dz, f3);
			if (dc.DerivFlag) {

				face.include_link(sols.get(0), sols.get(1));
				face.include_link(sols.get(2), sols.get(3));
				return; // goto fini_link_face;
			}
			nodesol = MakeNode(face, pos_x, pos_y, sig_x, sig_y, sig_z, s);
			res1 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s, sig_x, sig_y,
					sig_z);
			this.fillSolWith2Dres(nodesol, res1);

			if (!res1.good) {
				face.include_link(sols.get(0), sols.get(1));
				face.include_link(sols.get(2), sols.get(3));
				return; // goto fini_link_face;
			}
		} else if (sols.get(0).match_derivs(sols.get(2)) && sols.get(1).match_derivs(sols.get(3)) && !sols.get(0).match_derivs(sols.get(1))) {
			dc.DerivFlag = true;
			derivTest(dc, x_face, dx, f1, y_face, dy, f2);
			derivTest(dc, x_face, dx, f1, z_face, dz, f3);
			derivTest(dc, y_face, dy, f2, z_face, dz, f3);
			if (dc.DerivFlag) {
				face.include_link(sols.get(0), sols.get(2));
				face.include_link(sols.get(1), sols.get(3));
				return;
			}
			nodesol = MakeNode(face, pos_x, pos_y, sig_x, sig_y, sig_z, s);
			res1 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s, sig_x, sig_y,
					sig_z);
			this.fillSolWith2Dres(nodesol, res1);
			if (!res1.good) {
				face.include_link(sols.get(0), sols.get(2));
				face.include_link(sols.get(1), sols.get(3));
				return;
			}
		} else if (sols.get(0).match_derivs(sols.get(3)) && sols.get(1).match_derivs(sols.get(2)) && !sols.get(0).match_derivs(sols.get(1))) {
			dc.DerivFlag = true;
			derivTest(dc, x_face, dx, f1, y_face, dy, f2);
			derivTest(dc, x_face, dx, f1, z_face, dz, f3);
			derivTest(dc, y_face, dy, f2, z_face, dz, f3);
			if (dc.DerivFlag) {
				face.include_link(sols.get(0), sols.get(3));
				face.include_link(sols.get(1), sols.get(2));
				return;
			}
			nodesol = MakeNode(face, pos_x, pos_y, sig_x, sig_y, sig_z, s);
			res1 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s, sig_x, sig_y,
					sig_z);
			this.fillSolWith2Dres(nodesol, res1);
			if (!res1.good) {
				face.include_link(sols.get(0), sols.get(3));
				face.include_link(sols.get(1), sols.get(2));
				return; // goto fini_link_face;
			}
		} else {
			nodesol = MakeNode(face, pos_x, pos_y, sig_x, sig_y, sig_z, s);
			res1 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s, sig_x, sig_y,
					sig_z);
			this.fillSolWith2Dres(nodesol, res1);
			if (!res1.good) {
				nodesol.conv_failed = true;
				Face_context fc = new Face_context(face,sols);

				link_face4sols_conv_failed(face, sols, fc, s, sig_x, sig_y, sig_z);
				if (failCountM++ == 0) {
					BoxClevA.log.println("ERR: link_face2: default conv failed M");
					BoxClevA.log.println(nodesol);
					BoxClevA.log.println(face);
				}
				return;
			}
		}

		face.add_node(nodesol);
		face.include_link(sols.get(0), nodesol);
		face.include_link(sols.get(1), nodesol);
		face.include_link(sols.get(2), nodesol);
		face.include_link(sols.get(3), nodesol);

		if (PRINT_LINKFACE04) {
			BoxClevA.log.printf("ERR: link_face4: finished DerivFlag %d res1 %d\n", dc.DerivFlag, res1);
			BoxClevA.log.print(face.print_face_brief());
		}
	}

	private void link_face4sols_conv_failed(Face_info face, List<Sol_info> sols, Face_context fc, Sheaf2D s, int sig_x, int sig_y, int sig_z) throws AsurfException {
		boolean oldPC = Converger.PRINT_CONVERGE;
		Converger.PRINT_CONVERGE = PRINT_FACE4SOL_FAILED;
		int zero_count = (sig_x == 0 ? 1 : 0) + (sig_y == 0 ? 1 : 0) + (sig_z == 0 ? 1 : 0);

		FacePos fp = calcMidPoint(fc);
		Solve2DresultWithSig res_xy = null;
		Solve2DresultWithSig res_xz = null;
		Solve2DresultWithSig res_yz = null;
		Solve2DresultWithSig res_x = null;
		Solve2DresultWithSig res_y = null, res_z = null;
		Sol_info sol_xy = null, sol_xz = null, sol_yz = null, sol_x = null, sol_y = null, sol_z = null;
		if (sig_x == 0 && sig_y == 0) {
			if (PRINT_FACE4SOL_FAILED)
				BoxClevA.log.println("Conv XY");
			res_xy = boxgen.converger.converge_node_two_deriv(fp, s, 0, 0, 1);
			sol_xy = makeNode(face, res_xy);
		}

		if (sig_x == 0 && sig_z == 0) {
			if (PRINT_FACE4SOL_FAILED)
				BoxClevA.log.println("Conv XZ");
			res_xz = boxgen.converger.converge_node_two_deriv(fp, s, 0, 1, 0);
			sol_xz = makeNode(face, res_xz);
		}

		if (sig_y == 0 && sig_z == 0) {
			if (PRINT_FACE4SOL_FAILED)
				BoxClevA.log.println("Conv YZ");
			res_yz = boxgen.converger.converge_node_two_deriv(fp, s, 1, 0, 0);
			sol_yz = makeNode(face, res_yz);
		}

		if (sig_x == 0) {
			if (PRINT_FACE4SOL_FAILED)
				BoxClevA.log.println("Conv X");
			res_x = boxgen.converger.converge_node_exactly_one_deriv(fp, s, 0, 1, 1);
			sol_x = makeNode(face, res_x);
		}

		if (sig_y == 0) {
			if (PRINT_FACE4SOL_FAILED)
				BoxClevA.log.println("Conv Y");
			res_y = boxgen.converger.converge_node_exactly_one_deriv(fp, s, 1, 0, 1);
			sol_y = makeNode(face, res_y);
		}

		if (sig_z == 0) {
			if (PRINT_FACE4SOL_FAILED)
				BoxClevA.log.println("Conv Z");
			res_z = boxgen.converger.converge_node_exactly_one_deriv(fp, s, 1, 1, 0);
			sol_z = makeNode(face, res_z);
		}
		if (PRINT_FACE4SOL_FAILED)
			BoxClevA.log.printf("link_face4sols_conv_failed zc %d xy %b %b %b x %b %b %b%n", zero_count,
					res_xy != null && res_xy.good, res_xz != null && res_xz.good, res_yz != null && res_yz.good,
					res_x != null && res_x.good, res_y != null && res_y.good, res_z != null && res_z.good);

		Converger.PRINT_CONVERGE = oldPC;

		Solve2DresultWithSig base_res = new Solve2DresultWithSig(
				new Solve2Dresult(fp.x, fp.y, Double.MAX_VALUE, 0.0, false), sig_x, sig_y, sig_z);
		Solve2DresultWithSig best_res = base_res;
		Sol_info best_sol = null;
		if (res_xy != null && res_xy.good && Math.abs(res_xy.f_val) < Math.abs(best_res.f_val)) {
			best_res = res_xy;
			best_sol = sol_xy;
		}
		if (res_xz != null && res_xz.good && Math.abs(res_xz.f_val) < Math.abs(best_res.f_val)) {
			best_res = res_xz;
			best_sol = sol_xz;
		}
		if (res_yz != null && res_yz.good && Math.abs(res_yz.f_val) < Math.abs(best_res.f_val)) {
			best_res = res_yz;
			best_sol = sol_yz;
		}
		if (!best_res.good) { // if none of the 2 deriv soln work try the 1 deriv soln
			if (res_x != null && res_x.good && Math.abs(res_x.f_val) < Math.abs(best_res.f_val)) {
				best_res = res_x;
				best_sol = sol_x;
			}
			if (res_y != null && res_y.good && Math.abs(res_y.f_val) < Math.abs(best_res.f_val)) {
				best_res = res_y;
				best_sol = sol_y;
			}
			if (res_z != null && res_z.good && Math.abs(res_z.f_val) < Math.abs(best_res.f_val)) {
				best_res = res_z;
				best_sol = sol_z;
			}
		}
		if (best_res == base_res) { // if nothing worked just try and find a point on surface
			best_res = boxgen.converger.converge_node_zero_deriv(fp, s);
			best_sol = makeNode(face, best_res);
		}

		face.add_node(best_sol);
		face.include_link(best_sol, sols.get(0));
		face.include_link(best_sol, sols.get(1));
		face.include_link(best_sol, sols.get(2));
		face.include_link(best_sol, sols.get(3));
	}

	private void link_face4solsPos(Face_info face, List<Sol_info> sols, Sheaf2D s, int f1, int f2, int f3) throws AsurfException {

		double vec[] = new double[2], pos_x = 0.0, pos_y = 0.0;
		int Aind = -1, Bind = -1, Cind = -1, Dind = -1;

		if (sols.get(0).match_derivs(sols.get(1))) {
			Aind = 0;
			Bind = 1;
			Cind = 2;
			Dind = 3;
		}
		if (sols.get(0).match_derivs(sols.get(2))) {
			Aind = 0;
			Bind = 2;
			Cind = 1;
			Dind = 3;
		}
		if (sols.get(0).match_derivs(sols.get(3))) {
			Aind = 0;
			Bind = 3;
			Cind = 1;
			Dind = 2;
		}
		if (sols.get(1).match_derivs(sols.get(2))) {
			Aind = 1;
			Bind = 2;
			Cind = 0;
			Dind = 3;
		}
		if (sols.get(1).match_derivs(sols.get(3))) {
			Aind = 1;
			Bind = 3;
			Cind = 0;
			Dind = 2;
		}
		if (sols.get(2).match_derivs(sols.get(3))) {
			Aind = 2;
			Bind = 3;
			Cind = 0;
			Dind = 1;
		}
		if (Aind != -1) {
			face.include_link(sols.get(Aind), sols.get(Bind));
			if (sols.get(Cind).match_derivs(sols.get(Dind))) {
				face.include_link(sols.get(Cind), sols.get(Dind));
				return;
			}
			pos_x = pos_y = 0.0;
			Sol_info solC = sols.get(Cind);
			vec = face.calc_pos_on_face(solC);
			pos_x += vec[0];
			pos_y += vec[1];
			Sol_info solD = sols.get(Dind);
			vec = face.calc_pos_on_face(solD);
			pos_x += vec[0];
			pos_y += vec[1];

			int sgnx = solC.getDx() == solD.getDx() ? solC.getDx() : 0;
			int sgny = solC.getDy() == solD.getDy() ? solC.getDy() : 0;
			int sgnz = solC.getDz() == solD.getDz() ? solC.getDz() : 0;

			Sol_info nodesol = MakeNode(face, pos_x, pos_y, sgnx, sgny, sgnz, s);
			Solve2DresultWithSig res2 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s, sgnx, sgny, sgnz);
			fillSolWith2Dres(nodesol, res2);
			if (!res2.good) {
				if (failCountC++ == 0) {
					BoxClevA.log.println("ERR: link_face2: default conv failed C");
					BoxClevA.log.println(nodesol);
				}
			}

			face.add_node(nodesol);
			face.include_link(sols.get(Cind), nodesol);
			face.include_link(sols.get(Dind), nodesol);
			return;
		}

		/* None of the point match */
		/* Test we have (1,1) (1,-1) (-1,1) (-1,-1) */

		Aind = Bind = Cind = Dind = -1;
		for (int i = 0; i < 4; ++i)
			switch (face.type) {
			case FACE_LL:
			case FACE_RR:
				if (sols.get(i).getDy() == 1 && sols.get(i).getDz() == 1)
					Aind = i;
				if (sols.get(i).getDy() == 1 && sols.get(i).getDz() == -1)
					Bind = i;
				if (sols.get(i).getDy() == -1 && sols.get(i).getDz() == 1)
					Cind = i;
				if (sols.get(i).getDy() == -1 && sols.get(i).getDz() == -1)
					Dind = i;
				break;
			case FACE_FF:
			case FACE_BB:
				if (sols.get(i).getDx() == 1 && sols.get(i).getDz() == 1)
					Aind = i;
				if (sols.get(i).getDx() == 1 && sols.get(i).getDz() == -1)
					Bind = i;
				if (sols.get(i).getDx() == -1 && sols.get(i).getDz() == 1)
					Cind = i;
				if (sols.get(i).getDx() == -1 && sols.get(i).getDz() == -1)
					Dind = i;
				break;
			case FACE_DD:
			case FACE_UU:
				if (sols.get(i).getDx() == 1 && sols.get(i).getDy() == 1)
					Aind = i;
				if (sols.get(i).getDx() == 1 && sols.get(i).getDy() == -1)
					Bind = i;
				if (sols.get(i).getDx() == -1 && sols.get(i).getDy() == 1)
					Cind = i;
				if (sols.get(i).getDx() == -1 && sols.get(i).getDy() == -1)
					Dind = i;
				break;
			default:
				break;
			}
		if (Aind != -1 && Bind != -1 && Cind != -1 && Dind != -1) {
			/* Now a nicely behaved example */
			/* I think all sols should be on two opposite edges */
			if (Boxclev.sameEdge(sols.get(Aind), sols.get(Bind)) && Boxclev.sameEdge(sols.get(Cind), sols.get(Dind))) {
				if (PRINT_LINKFACE04) {
					BoxClevA.log.printf("ERR: link4+ AB CD: %d %d %d %d\n", Aind, Bind, Cind, Dind);
				}
				pos_x = pos_y = 0.0;
				vec = face.calc_pos_on_face(sols.get(Aind));
				pos_x += vec[0];
				pos_y += vec[1];
				vec = face.calc_pos_on_face(sols.get(Cind));
				pos_x += vec[0];
				pos_y += vec[1];
				Sol_info nodesol = MakeNode(face, pos_x, pos_y, f1, f2, f3, s);
				Solve2DresultWithSig res2 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s, f1, f2, f3);
				fillSolWith2Dres(nodesol, res2);
				if (!res2.good) {
					if (failCountD++ == 0) {
						BoxClevA.log.println("ERR: link_face2: default conv failed D");
						BoxClevA.log.println(nodesol);
					}
				}

				if (sols.get(Aind).getDx() != 0 && sols.get(Aind).getDx() == sols.get(Cind).getDx())
					nodesol.setDx(sols.get(Aind).getDx());
				if (sols.get(Aind).getDy() != 0 && sols.get(Aind).getDy() == sols.get(Cind).getDy())
					nodesol.setDy(sols.get(Aind).getDy());
				if (sols.get(Aind).getDz() != 0 && sols.get(Aind).getDz() == sols.get(Cind).getDz())
					nodesol.setDz(sols.get(Aind).getDz());

				face.add_node(nodesol);
				face.include_link(sols.get(Aind), nodesol);
				face.include_link(sols.get(Cind), nodesol);
				pos_x = pos_y = 0.0;
				vec = face.calc_pos_on_face(sols.get(Bind));
				pos_x += vec[0];
				pos_y += vec[1];
				vec = face.calc_pos_on_face(sols.get(Dind));
				pos_x += vec[0];
				pos_y += vec[1];

				nodesol = MakeNode(face, pos_x, pos_y, f1, f2, f3, s);
				Solve2DresultWithSig res4 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s, f1, f2, f3);
				fillSolWith2Dres(nodesol, res4);
				if (!res4.good) {
					if (failCountE++ == 0) {
						BoxClevA.log.println("ERR: link_face2: default conv failed E");
						BoxClevA.log.println(nodesol);
					}
				}

				if (sols.get(Bind).getDx() != 0 && sols.get(Bind).getDx() == sols.get(Dind).getDx())
					nodesol.setDx(sols.get(Bind).getDx());
				if (sols.get(Bind).getDy() != 0 && sols.get(Bind).getDy() == sols.get(Dind).getDy())
					nodesol.setDy(sols.get(Bind).getDy());
				if (sols.get(Bind).getDz() != 0 && sols.get(Bind).getDz() == sols.get(Dind).getDz())
					nodesol.setDz(sols.get(Bind).getDz());

				face.add_node(nodesol);
				face.include_link(sols.get(Bind), nodesol);
				face.include_link(sols.get(Dind), nodesol);
				if (PRINT_LINKFACE04) {
					BoxClevA.log.print(face);
				}
				return;
			} else if (Boxclev.sameEdge(sols.get(Aind), sols.get(Cind)) && Boxclev.sameEdge(sols.get(Bind), sols.get(Dind))) {
				if (PRINT_LINKFACE04) {
					BoxClevA.log.printf("ERR: link4+ AC BD: %d %d %d %d\n", Aind, Bind, Cind, Dind);
				}
				pos_x = pos_y = 0.0;
				vec = face.calc_pos_on_face(sols.get(Aind));
				pos_x += vec[0];
				pos_y += vec[1];
				vec = face.calc_pos_on_face(sols.get(Bind));
				pos_x += vec[0];
				pos_y += vec[1];
				Sol_info nodesol = MakeNode(face, pos_x, pos_y, f1, f2, f3, s);
				Solve2DresultWithSig res2 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s, f1, f2, f3);
				fillSolWith2Dres(nodesol, res2);
				if (!res2.good) {
					if (failCountF++ == 0) {
						BoxClevA.log.println("ERR: link_face2: default conv failed F");
						BoxClevA.log.println(nodesol);
					}
				}

				if (sols.get(Aind).getDx() != 0 && sols.get(Aind).getDx() == sols.get(Bind).getDx())
					nodesol.setDx(sols.get(Aind).getDx());
				if (sols.get(Aind).getDy() != 0 && sols.get(Aind).getDy() == sols.get(Bind).getDy())
					nodesol.setDy(sols.get(Aind).getDy());
				if (sols.get(Aind).getDz() != 0 && sols.get(Aind).getDz() == sols.get(Bind).getDz())
					nodesol.setDz(sols.get(Aind).getDz());

				face.add_node(nodesol);
				face.include_link(sols.get(Aind), nodesol);
				face.include_link(sols.get(Bind), nodesol);
				pos_x = pos_y = 0.0;
				vec = face.calc_pos_on_face(sols.get(Cind));
				pos_x += vec[0];
				pos_y += vec[1];
				vec = face.calc_pos_on_face(sols.get(Dind));
				pos_x += vec[0];
				pos_y += vec[1];
				nodesol = MakeNode(face, pos_x, pos_y, f1, f2, f3, s);
				Solve2DresultWithSig res4 = boxgen.converger.converge_node(new FacePos(face, pos_x, pos_y), s, f1, f2, f3);
				fillSolWith2Dres(nodesol, res4);
				if (!res4.good) {
					if (failCountG++ == 0) {
						BoxClevA.log.println("ERR: link_face2: default conv failed G");
						BoxClevA.log.println(nodesol);
					}
				}

				if (sols.get(Cind).getDx() != 0 && sols.get(Cind).getDx() == sols.get(Dind).getDx())
					nodesol.setDx(sols.get(Cind).getDx());
				if (sols.get(Cind).getDy() != 0 && sols.get(Cind).getDy() == sols.get(Dind).getDy())
					nodesol.setDy(sols.get(Cind).getDy());
				if (sols.get(Cind).getDz() != 0 && sols.get(Cind).getDz() == sols.get(Dind).getDz())
					nodesol.setDz(sols.get(Cind).getDz());

				face.add_node(nodesol);
				face.include_link(sols.get(Cind), nodesol);
				face.include_link(sols.get(Dind), nodesol);
				return;
			}
		}
		if (PRINT_LINKFACE04) {
			BoxClevA.log.printf("ERR: linkFace4Pos: odd sols not in expected posn\n");
			BoxClevA.log.print(face);
		}

		Face_context fc = new Face_context(face,sols);
		FacePos fp = calcMidPoint(fc);
		{
		Sol_info nodesol = MakeNode(face, pos_x, pos_y, f1, f2, f3, s);
		Solve2DresultWithSig cres = boxgen.converger.converge_node_zero_deriv(fp, s);
		fillSolWith2Dres(nodesol, cres);
		face.add_node(nodesol);
		face.include_link(sols.get(0), nodesol);
		face.include_link(sols.get(1), nodesol);
		face.include_link(sols.get(2), nodesol);
		face.include_link(sols.get(3), nodesol);
		}
	}

	/**
	 * @param bb2
	 */
	private void link_facemanysols(Face_info face, List<Sol_info> sols, Sheaf2D s, int f1, int f2, int f3) throws AsurfException {

		Face_context fc = new Face_context(face,sols);

		FacePos fp = calcMidPoint(fc);
		Sol_info nodesol = MakeNode(face, fp.x, fp.y, f1, f2, f3, s);
		Solve2DresultWithSig res2 = boxgen.converger.converge_node(fp, s, f1, f2, f3);
		fillSolWith2Dres(nodesol, res2);
		if (!res2.good) {
			if (failCountL++ == 0) {
				BoxClevA.log.println("ERR: link_face2: default conv failed L");
				BoxClevA.log.println(nodesol);
			}
		}

		face.add_node(nodesol);
		if (PRINT_LINKFACE04) {
			BoxClevA.log.printf("ERR: link_face many sols: ");
			BoxClevA.log.print(face.type);
			BoxClevA.log.printf(" (%d,%d,%d)/%d count %d f1 %d f2 %d f3 %d\n", face.xl, face.yl, face.zl, face.denom,
					sols.size(), f1, f2, f3);
		}
		for (int i = 0; i < sols.size(); ++i) {
			face.include_link(sols.get(i), nodesol);
		}
	}

	private Sol_info makeNode(Face_info face, Solve2DresultWithSig res) {
		Sol_info temp;

		temp = new Sol_info(face.type, face.xl, face.yl, face.zl, face.denom, res.x, res.y);
		temp.setDerivs(res.sig_x,res.sig_y,res.sig_z);
		return (temp);
	}

	private Sol_info MakeNode(Face_info face, double pos_x, double pos_y, int f1, int f2, int f3, Sheaf2D s) throws AsurfException {
		Sol_info temp;

		temp = new Sol_info(face.type, face.xl, face.yl, face.zl, face.denom, pos_x, pos_y);
		temp.setDerivs(f1, f2, f3);
//		if (Boxclev.USE_2ND_DERIV) {
//			calc_2nd_derivs(temp, dx, dy, dz, d2);
//		}
		return (temp);
	}

	private void ReduceFace(Face_info big_face, Face_info face, Sheaf2D s, boolean internal, int f1, int f2, int f3) throws AsurfException {

		QuadSheaf qs = s.reduce(face, f1, f2, f3);

		Face_info[] faces = face.make_sub_faces();
		face.lb = faces[0];
		face.rb = faces[1];
		face.lt = faces[2];
		face.rt = faces[3];
		boxclev.topology.split_face(face, face.lb, face.rb, face.lt, face.rt);

		boxgen.find_edge(face.lb.x_high, qs.lb, Face_info.Type.X_HIGH);
		boxgen.find_edge(face.lb.y_high, qs.lb, Face_info.Type.Y_HIGH);
		boxgen.find_edge(face.rt.x_low, qs.rt, Face_info.Type.X_LOW);
		boxgen.find_edge(face.rt.y_low, qs.rt, Face_info.Type.Y_LOW);

		link_face(big_face, face.lb, qs.lb, internal);
		face.lb.status = BoxClevA.FOUND_EVERYTHING;
		link_face(big_face, face.rb, qs.rb, internal);
		face.rb.status = BoxClevA.FOUND_EVERYTHING;
		link_face(big_face, face.lt, qs.lt, internal);
		face.lt.status = BoxClevA.FOUND_EVERYTHING;
		link_face(big_face, face.rt, qs.rt, internal);
		face.rt.status = BoxClevA.FOUND_EVERYTHING;
		
		/* Now need to combine links from sub face to big face */
		combine_links(face);
	}

	private boolean straddleDeriv(Sol_info A, Sol_info B, int f1, int f2, int f3) {
		return (f1 != 0 || (A.getDx() == 1 && B.getDx() == -1) || (A.getDx() == -1 && B.getDx() == 1))
				&& (f2 != 0 || (A.getDy() == 1 && B.getDy() == -1) || (A.getDy() == -1 && B.getDy() == 1))
				&& (f3 != 0 || (A.getDz() == 1 && B.getDz() == -1) || (A.getDz() == -1 && B.getDz() == 1));
	}

	private Face_info calcDerivFace(DerivContext dc, Bern2D aa) throws AsurfException {
		Face_info q_face;

		q_face = new Face_info(dc.face.type, dc.face.xl, dc.face.yl, dc.face.zl, dc.face.denom);
		boxclev.topology.create_new_edges(q_face);
		final Sheaf2D sheaf = dc.getSheaf(aa);
		boxgen.find_edge(q_face.x_low, sheaf, Face_info.Type.X_LOW);
		boxgen.find_edge(q_face.x_high, sheaf, Face_info.Type.X_HIGH);
		boxgen.find_edge(q_face.y_low, sheaf, Face_info.Type.Y_LOW);
		boxgen.find_edge(q_face.y_high, sheaf, Face_info.Type.Y_HIGH);
		return q_face;
	}

	private Face_info calcDerivFace(Face_info face,Bern2D bb,Bern2D dx,Bern2D dy,Bern2D dz) throws AsurfException {
		Face_info q_face;

		q_face = new Face_info(face.type, face.xl, face.yl, face.zl, face.denom);
		boxclev.topology.create_new_edges(q_face);
		Sheaf2D s = new Sheaf2D(bb, dx, dy, dz);
		boxgen.find_edge(q_face.x_low, s, Face_info.Type.X_LOW);
		boxgen.find_edge(q_face.x_high, s, Face_info.Type.X_HIGH);
		boxgen.find_edge(q_face.y_low, s, Face_info.Type.Y_LOW);
		boxgen.find_edge(q_face.y_high, s, Face_info.Type.Y_HIGH);
		return q_face;
	}
	
	
	private void calcRotatedDerivs(Face_info face, Sheaf2D s) throws AsurfException {
		if(boxclev.rotderiv==0) return;
		SheafRotatedDerivs r = (SheafRotatedDerivs) s;
		FacePos fp = new FacePos(face,0.5,0.5);
		calcRotatedDeriv(face, s, fp, r.xpy);
		calcRotatedDeriv(face, s, fp, r.xmy);
		calcRotatedDeriv(face, s, fp, r.xpz);
		calcRotatedDeriv(face, s, fp, r.xmz);
		calcRotatedDeriv(face, s, fp, r.ypz);
		calcRotatedDeriv(face, s, fp, r.ymz);
	}

	/**
	 * @param face
	 * @param s
	 * @param fp
	 * @param rot
	 * @throws AsurfException
	 */
	public void calcRotatedDeriv(Face_info face, Sheaf2D s, FacePos fp, final Bern2D rot) throws AsurfException {
		if(rot.allOneSign()!=0) return;
		Solve2DresultWithSig sol = boxgen.converger.converge_node_rotated_diff(fp, 
				s, rot);
		if(sol!=null) {
			Sol_info nodesol = MakeNode(face, fp.x, fp.y, 0, 0, 0, s);
			this.fillSolWith2Dres(nodesol, sol);

			boxclev.plotter.plot_point(nodesol);
		}
	}



	public void printResults() {
		System.out.printf("FaceSolver Fail counts A %d B %d C %d D %d E %d F %d G %d H %d I %d J %d K %d L %d M %d%n", failCountA,
				failCountB, failCountC, failCountD, failCountE, failCountF, failCountG, failCountH, failCountI,
				failCountJ, failCountK, failCountL, failCountM);
		System.out.printf("N %d O %d %n", 
				failCountN, failCountO);
		
	}


}
