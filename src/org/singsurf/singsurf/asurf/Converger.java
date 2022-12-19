package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.List;

//import org.eclipse.jdt.annotation.NonNull;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern1D;
import org.singsurf.singsurf.acurve.Bern2D;
import org.singsurf.singsurf.asurf.BoxSolver.BoxPos;
import org.singsurf.singsurf.asurf.FaceSolver.FacePos;

/**
 * Splits out convergence routines
 */
public class Converger {

 	public static double GOOD_SOL_TOL = 1e-5;
	private int failCountA=0;
	private int failCountB=0;
	public static boolean PRINT_CONVERGE = false;
	final Bern3DContext ctx;
	private int failCountC=0;
	private int failCountD=0;
	private int failCountE=0;
	
	public Converger(Bern3DContext ctx) {
		this.ctx = ctx;
	}
	
	public void fini() {
	}

	public static class Solve2DresultWithSig {
		final double x,y;
		final double f_val, g_val, h_val, i_val;
		final boolean good;
		final int sig_x, sig_y, sig_z;

		public Solve2DresultWithSig(Solve2Dresult res,int sig_x,int sig_y,int sig_z) {
			this.x = res.x;
			this.y = res.y;
			this.f_val = res.f_val;
			this.g_val = res.g_val;
			this.h_val = res.h_val;
			this.i_val = res.i_val;
			this.good = res.good;
			this.sig_x = sig_x;
			this.sig_y = sig_y;
			this.sig_z = sig_z;			
		}
	}
	
	public static class Solve2Dresult {
		final double x,y;
		final double f_val, g_val, h_val, i_val;
		boolean good;
		int sig_x, sig_y, sig_z;


		public Solve2Dresult(double x, double y, double f_val, double g_val, boolean in_box) {
			super();
			this.x = x;
			this.y = y;
			this.f_val = f_val;
			this.g_val = g_val;
			this.h_val = Double.NaN;
			this.i_val = Double.NaN;
			this.good = in_box;
		}

		public Solve2Dresult(double x, double y, double fval, double gval, double hval, boolean in_box) {
			this.x = x;
			this.y = y;
			this.f_val = fval;
			this.g_val = gval;
			this.h_val = hval;
			this.i_val = Double.NaN;
			this.good = in_box;
		}

		public Solve2Dresult(double x, double y, double fval, double gval, double hval, double ival, boolean b) {
			this.x = x;
			this.y = y;
			this.f_val = fval;
			this.g_val = gval;
			this.h_val = hval;
			this.i_val = ival;
			this.good = b;
		}

		public Solve2Dresult(double x, double y, double f, boolean b) {
			this.x = x;
			this.y = y;
			this.f_val = f;
			this.g_val = Double.NaN;
			this.h_val = Double.NaN;
			this.i_val = Double.NaN;
			this.good = b;
		}

		@Override
		public String toString() {
			return "Solve2Dresult ["+(good?"good":"bad") +" (" + x + ", " + y + ") f()=" + f_val + ", g=" + g_val + ", h=" + h_val
					+ ", i=" + i_val + "]";
		}
	}
		
		public class Solve3Dresult {
			final double x,y,z;
			final double f_val, g_val, h_val, i_val;
			final boolean good;

			public Solve3Dresult(boolean good,double x, double y, double z, double f_val, double g_val 	) {
				super();
				this.good = good;
				this.x = x;
				this.y = y;
				this.z = z;
				this.f_val = f_val;
				this.g_val = g_val;
				this.h_val = Double.NaN;
				this.i_val = Double.NaN;
			}

			public Solve3Dresult(boolean good,double x, double y, double z, double fval, double gval, double hval, 
					int sig_x, int sig_y, int sig_z) {
				this.good = good;
				this.x = x;
				this.y = y;
				this.z = z;
				this.f_val = fval;
				this.g_val = gval;
				this.h_val = hval;
				this.i_val = Double.NaN;
			}

			public Solve3Dresult(boolean good,double x, double y, double z, double fval, double gval, double hval, double ival, 
					int sig_x, int sig_y, int sig_z) {
				this.good = good;
				this.x = x;
				this.y = y;
				this.z = z;
				this.f_val = fval;
				this.g_val = gval;
				this.h_val = hval;
				this.i_val = ival;
			}

			public Solve3Dresult(boolean good, Vec3D vec, double f0, double fx, double fy, double fz) {
				this.good = good;
				this.x = vec.x;
				this.y = vec.y;
				this.z = vec.z;
				this.f_val = f0;
				this.g_val = fx;
				this.h_val = fy;
				this.i_val = fz;
			}

			public Solve3Dresult(boolean good,Vec3D vec, double f, double g, double h) {
				this.good = good;
				this.x = vec.x;
				this.y = vec.y;
				this.z = vec.z;
				this.f_val = f;
				this.g_val = g;
				this.h_val = h;
				this.i_val = Double.NaN;
			}

			public Solve3Dresult(boolean good, double[] vec, double f, double g) {
				this.good = good;
				this.x = vec[0];
				this.y = vec[1];
				this.z = vec[2];
				this.f_val = f;
				this.g_val = g;
				this.h_val = Double.NaN;
				this.i_val = Double.NaN;
			}

			public Solve3Dresult(boolean good, double[] vec, double f, double g, double h) {
				this.good = good;
				this.x = vec[0];
				this.y = vec[1];
				this.z = vec[2];
				this.f_val = f;
				this.g_val = g;
				this.h_val = h;
				this.i_val = Double.NaN;
			}

			public Solve3Dresult(boolean good, double[] vec, double f, double g, double h, double i) {
				this.good = good;
				this.x = vec[0];
				this.y = vec[1];
				this.z = vec[2];
				this.f_val = f;
				this.g_val = g;
				this.h_val = h;
				this.i_val = i;
			}

			public Solve3Dresult(boolean good, double[] vec, double f) {
				this.good = good;
				this.x = vec[0];
				this.y = vec[1];
				this.z = vec[2];
				this.f_val = f;
				this.g_val = Double.NaN;
				this.h_val = Double.NaN;
				this.i_val = Double.NaN;
			}

			@Override
			public String toString() {
				return "Solve3Dresult ["+(good?"good":"bad") +" (" + x + ", " + y + ", "+ z+ ") f()=" + f_val + ", g=" + g_val + ", h=" + h_val
						+ ", i=" + i_val + "]";
			}
			
		}

		public class Solve3DresultWithSig {
			final double x,y,z;
			final double f_val, g_val, h_val, i_val;
			final int sig_x, sig_y, sig_z;
			final boolean good;

			public Solve3DresultWithSig(double x, double y, double z, double f_val, double g_val, 
					int sig_x, int sig_y, int sig_z, boolean good) {
				super();
				this.x = x;
				this.y = y;
				this.z = z;
				this.f_val = f_val;
				this.g_val = g_val;
				this.h_val = Double.NaN;
				this.i_val = Double.NaN;
				this.sig_x = sig_x;
				this.sig_y = sig_y;
				this.sig_z = sig_z;
				this.good = good;
			}

			public Solve3DresultWithSig(double x, double y, double z, double fval, double gval, double hval, 
					int sig_x, int sig_y, int sig_z, boolean good) {
				this.x = x;
				this.y = y;
				this.z = z;
				this.f_val = fval;
				this.g_val = gval;
				this.h_val = hval;
				this.i_val = Double.NaN;
				this.sig_x = sig_x;
				this.sig_y = sig_y;
				this.sig_z = sig_z;
				this.good = good;
			}

			public Solve3DresultWithSig(double x, double y, double z, double fval, double gval, double hval, double ival, 
					int sig_x, int sig_y, int sig_z, boolean good) {
				this.x = x;
				this.y = y;
				this.z = z;
				this.f_val = fval;
				this.g_val = gval;
				this.h_val = hval;
				this.i_val = ival;
				this.sig_x = sig_x;
				this.sig_y = sig_y;
				this.sig_z = sig_z;

				this.good = good;
			}

			public Solve3DresultWithSig(Solve3Dresult res, int i, int j, int k) {
				this.x = res.x;
				this.y = res.y;
				this.z = res.z;
				this.f_val = res.f_val;
				this.g_val = res.g_val;
				this.h_val = res.h_val;
				this.i_val = res.i_val;
				this.sig_x = i;
				this.sig_y = j;
				this.sig_z = k;
				this.good=res.good;
			}

			public Solve3DresultWithSig(Solve3Dresult res, int i, int j, int k,boolean good) {
				this.x = res.x;
				this.y = res.y;
				this.z = res.z;
				this.f_val = res.f_val;
				this.g_val = res.g_val;
				this.h_val = res.h_val;
				this.i_val = res.i_val;
				this.sig_x = i;
				this.sig_y = j;
				this.sig_z = k;
				this.good=good;
			}

			@Override
			public String toString() {
				return "Solve3Dresult ["+(good?"good":"bad") +" (" + x + ", " + y + ",="+ z+ "), f_val=" + f_val + ", g_val=" + g_val + ", h_val=" + h_val
						+ ", i_val=" + i_val + "]";
			}
			
		}
	
	static class Solve1DResult {
		double root;
		double val;
		public Solve1DResult(double pos, double val) {
			super();
			this.root = pos;
			this.val = val;
		}
		
	}
	
	public Solve1DResult converge_edge(Bern1D bb, Bern1D dx) {
		double best_pos = 0.5;
		double best_val = bb.evaluate(best_pos);
		Bern1D dx2;
		try {
			 dx2 = bb.diff();
			 dx = dx2;
		} catch (AsurfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		double dx_val;
		double delta;
		double cur_pos = 0.5;
		double cur_val = best_val;
		for(int i=0;i<6;++i) {
			dx_val = dx.evaluate(cur_pos);
			if(dx_val==0.0) break;
			delta = cur_val / dx_val;
			cur_pos -= delta;
			cur_val = bb.evaluate(cur_pos);
			if(cur_pos >=0 && cur_pos <= 1 && Math.abs(cur_val) < Math.abs(best_val)) {
				best_val = cur_val;
				best_pos = cur_pos;
			}
			if(cur_val==0) break;
		}
		if(best_pos==0.5) {
			// Conver didn't work try sub division
			return binary_subdivision(bb,best_pos,best_val);
		}
		return new Solve1DResult(best_pos,best_val);
	}

	private Solve1DResult binary_subdivision(Bern1D bb, double mp, double m) {
		double l = bb.evaluate(0.0);
		double h = bb.evaluate(1.0);
		double lp = 0.0;
		double hp = 1.0;
		for(int i=0;i<10;++i) {
			if(l>0) {
				if(m>0) {
					if(h>0) { 
						break;
					} else {
						// + + -
						l = h;
						h = m;
						lp = hp;
						hp = mp;
					}
				} else {
					if(h>0) {
						// + - +
						h = l;
						l = m;
						hp = lp;
						lp = mp;
					} else {
						// + - -
						h = l;
						l = m;
						hp = lp;
						lp = mp;
					}
				}
			} else {
				if(m>0) {
					if(h>0) { 
						// - + +
						h = m;
						hp = mp;
					} else {
						// - + -
						l = h;
						h = m;
						lp = hp;
						hp = mp;
					}
				} else {
					if(h>0) {
						// - - +
						l = m;
						lp = mp;
					} else {
						// - - -
						break;
					}
				}
			}
			mp = 0.5 * (lp + hp);
			m = bb.evaluate(mp);
		}
		return new Solve1DResult(mp,m);
	}

	private Solve2Dresult exact_solve(double start_x, double start_y, Bern2D bb, Bern2D bb_x, Bern2D bb_y, Bern2D gg, Bern2D gg_x, Bern2D gg_y) {
		double x = start_x, y = start_y;
		double f0 = bb.evalbern2D(x,y);
		double g0 = gg.evalbern2D(x,y);
		Solve2Dresult lastgood = new Solve2Dresult(x,y, f0, g0, true);

		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("start %6.3f %6.3f f %9.6f g %9.6f%n", start_x, start_y, f0, g0);
		}

		for (int i = 0; i < 5; ++i) {

			double fx = bb_x.evalbern2D(x, y);
			double fy = bb_y.evalbern2D(x, y);

			double gx = gg_x.evalbern2D(x, y);
			double gy = gg_y.evalbern2D(x, y);

			double det = fx * gy - fy * gx;
			double m00 = gy, m01 = -fy, m10 = -gx, m11 = fx;
			double dx = (-m00 * f0 - m01 * g0) / det;
			double dy = (-m10 * f0 - m11 * g0) / det;

			x += dx;
			y += dy;
			f0 = bb.evalbern2D(x, y);
			g0 = gg.evalbern2D(x, y);

			if (in_unit_square(x, y) && Math.abs(f0 * g0) < Math.abs(lastgood.f_val * lastgood.g_val)) {
				lastgood = new Solve2Dresult(x, y, f0, g0, true);
			}

			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("soln %6.3f %6.3f f %12.9f g %12.9f%n", x, y, f0, g0);
			}
		}

		if (!in_unit_square(x, y) || Math.abs(f0) > GOOD_SOL_TOL || Math.abs(g0) > GOOD_SOL_TOL) {
			lastgood.good = false;
		}

		return lastgood;
	}

	public Solve2DresultWithSig converge_node(FacePos pos, Sheaf2D s, int signDx, int signDy, int signDz)
			throws AsurfException {

		int n_zero_deriv = (signDx == 0 ? 1 : 0) + (signDy == 0 ? 1 : 0) + (signDz == 0 ? 1 : 0);
		switch(n_zero_deriv) {
		case 1:
			return converge_node_exactly_one_deriv(pos, s, signDx, signDy, signDz);
		case 2:
			return converge_node_two_deriv(pos, s, signDx, signDy, signDz);
		case 3:
			return converge_node_three_deriv(pos, s, signDx, signDy, signDz);
		default:
			if(failCountB++==0) {
				BoxClevA.log.println("Wrong number of zero derivs");
				BoxClevA.log.println(pos);
			}
			return converge_node_zero_deriv(pos,s);
		}
	}

	
	private Solve2DresultWithSig converge_node_exactly_one_deriv(FacePos pos, 
			Bern2D f, Bern2D fx, Bern2D fy, Bern2D g, Bern2D gx, Bern2D gy,
			Bern2D dx, Bern2D dy, Bern2D dz) throws AsurfException {
				
		Solve2Dresult la_conv = exact_solve(pos.x, pos.y, f, fx, fy, g, gx, gy);
		Solve2Dresult tgt_conv = itterate_node_one_deriv(pos.x, pos.y, f, fx, fy, g, gx, gy);

		if(tgt_conv.good) {
			if(la_conv.good) {
				if(Math.abs(la_conv.f_val) < Math.abs(tgt_conv.f_val)) {
					if(PRINT_CONVERGE) {
						BoxClevA.log.println("La better than tgt");
						BoxClevA.log.println(la_conv);
						BoxClevA.log.println(tgt_conv);
					}
					int valx = dx.evalbern2Dsign(la_conv.x, la_conv.y);
					int valy = dy.evalbern2Dsign(la_conv.x, la_conv.y);
					int valz = dz.evalbern2Dsign(la_conv.x, la_conv.y);
					return new Solve2DresultWithSig(la_conv,valx,valy,valz);
				}
			} else {
				int valx = dx.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
				int valy = dy.evalbern2Dsign(la_conv.x, la_conv.y);
				int valz = dz.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
				return new Solve2DresultWithSig(tgt_conv,valx,valy,valz);
			}
		} else {
			if(la_conv.good) {
				int valx = dx.evalbern2Dsign(la_conv.x, la_conv.y);
				int valy = dy.evalbern2Dsign(la_conv.x, la_conv.y);
				int valz = dz.evalbern2Dsign(la_conv.x, la_conv.y);
				return new Solve2DresultWithSig(la_conv,valx,valy,valz);
			} else {
				int valx = dx.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
				int valy = dy.evalbern2Dsign(la_conv.x, la_conv.y);
				int valz = dz.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
				return new Solve2DresultWithSig(tgt_conv,valx,valy,valz);
			}
		}
		int valx = dx.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
		int valy = dy.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
		int valz = dz.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
		return new Solve2DresultWithSig(tgt_conv,valx,valy,valz);

	}

	private static class BernPair {
		Bern2D f_x, f_y;

		public BernPair(Bern2D f_x, Bern2D f_y) {
			this.f_x = f_x;
			this.f_y = f_y;
		}			
	}
	
	private BernPair getBernsOfFace(FacePos pos, Bern2D bb, Bern2D dx, Bern2D dy, Bern2D dz) throws AsurfException {
		Bern2D f_x, f_y;		

		switch (pos.face.type) {
		case FACE_LL:
		case FACE_RR:
			f_x = dy;
			f_y = dz;
			break;

		case FACE_FF:
		case FACE_BB:
			f_x = dx;
			f_y = dz;
			break;

		case FACE_DD:
		case FACE_UU:
			f_x = dx;
			f_y = dy;
			break;

		default:
			throw new AsurfException("Bad sol type " + pos.face);
		}
		if (f_x instanceof Bern2D.NegBern2D || f_x instanceof Bern2D.PosBern2D || f_x instanceof Bern2D.ZeroBern2D) {
			f_x = bb.diffX();
		}
		if (f_y instanceof Bern2D.NegBern2D || f_y instanceof Bern2D.PosBern2D || f_y instanceof Bern2D.ZeroBern2D) {
			f_y = bb.diffY();
		}
		return new BernPair(f_x,f_y);
	}
	
	Solve2DresultWithSig converge_node_rotated_diff(FacePos pos, Sheaf2D s, Bern2D g) throws AsurfException {
		Bern2D dgdx = g.diffX();
		Bern2D dgdy = g.diffY();
		BernPair bp = getBernsOfFace(pos,s.aa,s.dx,s.dy,s.dz);
		
		Solve2Dresult tgt_conv = itterate_node_one_deriv(pos.x, pos.y, 
				s.aa, bp.f_x, bp.f_y, 
				g, dgdx, dgdy);

		if(tgt_conv.good) {

					int valx = s.dx.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
					int valy = s.dy.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
					int valz = s.dz.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
					return new Solve2DresultWithSig(tgt_conv,valx,valy,valz);
		}
		
		Solve2Dresult la_conv = exact_solve(pos.x, pos.y, s.aa, bp.f_x, bp.f_y, 
				g, dgdx, dgdy);
		if(la_conv.good) {

			int valx = s.dx.evalbern2Dsign(la_conv.x, la_conv.y);
			int valy = s.dy.evalbern2Dsign(la_conv.x, la_conv.y);
			int valz = s.dz.evalbern2Dsign(la_conv.x, la_conv.y);
			return new Solve2DresultWithSig(la_conv,valx,valy,valz);
}

		return null;
	}
	
	Solve2DresultWithSig converge_node_exactly_one_deriv(FacePos pos, Sheaf2D s, 
			int signDx, int signDy, int signDz) throws AsurfException {

		BernPair bp = getBernsOfFace(pos,s.aa,s.dx,s.dy,s.dz);

		if (signDx == 0) {
			Bern2D dx_x = s.dx.diffX();
			Bern2D dx_y = s.dx.diffY();

			return converge_node_exactly_one_deriv(pos, 
					s.aa,bp.f_x,bp.f_y,
					s.dx,dx_x,dx_y, 
					Bern2D.zeroBern2D, s.dy, s.dz);
		}
		if (signDy == 0) {
			Bern2D dy_x = s.dy.diffX();
			Bern2D dy_y = s.dy.diffY();
			return converge_node_exactly_one_deriv(pos,s.aa,bp.f_x,bp.f_y,s.dy,dy_x,dy_y, s.dx,Bern2D.zeroBern2D, s.dz);
		}
		if (signDz == 0) {
			Bern2D dz_x = s.dz.diffX(); 
			Bern2D dz_y = s.dz.diffY();
			return converge_node_exactly_one_deriv(pos,s.aa,bp.f_x,bp.f_y,s.dz,dz_x,dz_y, s.dx, s.dy, Bern2D.zeroBern2D);
		}

		if(failCountA++==0) {
			BoxClevA.log.println("converge_node_exactly_one_deriv: No signs are zero");
		}
		Solve2Dresult res = itterate_node_zero_deriv(pos, s);
		Solve2DresultWithSig ressig = new Solve2DresultWithSig(res,signDx,signDy,signDy);
		return ressig;
	}

	/**
	 * Converge to soln f(x)=0, g(x)=0 (here g(x) will be one of derivs of f(x))
	 * Algorithm first uses one step Newton method along gradient to converge to
	 * surface f(x,y) ~= f(0,0) + x f_x(0,0) + y f_y(0,0) ~= 0 and x = lambda f_x, y
	 * = lambda f_y So f(0) + lambda (f_x^2 +f_y^2) = 0 and lambda = -f(0)/(f_x^2
	 * +f_y^2).
	 * 
	 * Next converge a one step iteration to g(x)=0 along the tangent to f(x), u =
	 * (-f_y, f_x). dg<u> = dg_x * ux + g_y * uy g(x,y) ~= g(0,0) + lambda dg<u>
	 * lambda = - g(0,0)/dg<u>
	 * 
	 * Next one step along tangent to g(x,y) find a point with f(x,y)~=0, g(x,y)~=0.
	 * 
	 * Repeat the last two steps a few times.
	 * 
	 * A bug can occur is (f_x,f_y) . (g_x,g_y) = 0 leading to division by zero,
	 * generally only occurs if g(x,y) is very small so abort loop early.
	 * 
	 * There can be problems if the initial point is close to fx = fy = 0 making
	 * first approximation to f hard.
	 * 
	 * @param sol
	 * @param f
	 * @param g
	 * @param dx_y
	 * @param dx_x
	 * @param f_y2
	 * @param f_x2
	 * @return
	 * @throws AsurfException
	 */
	private Solve2Dresult itterate_node_one_deriv(double start_x, double start_y, Bern2D f, Bern2D f_x, Bern2D f_y, Bern2D g, Bern2D g_x,
			Bern2D g_y) throws AsurfException {

		double x = start_x, y = start_y;
		// BoxClevA.log.printf("org %s %9.6f dx %9.6f%n",cur,
		// bb.evalbern2D(cur),dx.evalbern2D(cur));

		double fval = f.evalbern2D(x, y);
		double gval = g.evalbern2D(x, y);
		Solve2Dresult lastgood = new Solve2Dresult(x, y, fval, gval, true);
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("st  %6.3f %6.3f %9.6f dx %9.6f%n", x, y, fval, gval);
		}

		// First onto surface, assuming linear approx for f,
		// move along normal to closest point on plane f()=0
		double fx = f_x.evalbern2D(x, y);
		double fy = f_y.evalbern2D(x, y);
		double sumsq = fx * fx + fy * fy;
		double delta_x = -fval * fx / sumsq;
		double delta_y = -fval * fy / sumsq;
		x += delta_x;
		y += delta_y;
		fval = f.evalbern2D(x, y);
		gval = g.evalbern2D(x, y);

		if (in_unit_square(x, y) && Math.abs(fval) < Math.abs(lastgood.f_val)) {
			lastgood = new Solve2Dresult(x, y, fval, gval, true);
		}
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("sol %6.3f %6.3f %9.6f dx %9.6f%n", x, y, fval, gval);
		}

		for (int i = 0; i < 5; ++i) {
			// find the point on the line approximating f()
			// which intersects the line approximating g()
			fx = f_x.evalbern2D(x, y); // tangent is (-fy, fx)
			fy = f_y.evalbern2D(x, y);

			double gy = g_y.evalbern2D(x, y);
			double gx = g_x.evalbern2D(x, y);
			double dx_u = fx * gy - fy * gx; // direction derivative along tangent
			double lambda = -gval / dx_u; // multiple of tangent
			if (Math.abs(lambda) > 1e9) { // failed
				if(failCountC++==0) {
				BoxClevA.log.printf("exploded%nsol %6.3f %6.3f %9.6f dx %9.6f%n", x, y, fval, gval);
				BoxClevA.log.printf("fx %6.3f fy %6.3f gx %9.6f gy %9.6f dg<u> %9.6f lambda %9.6f%n", fx, fy, gx, gy,
						dx_u, lambda);
				}
				break;
			}
			x += lambda * -fy; // move along tangent
			y += lambda * fx;
			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("sol %6.3f %6.3f %9.6f dx %9.6f%n", x, y, fval, gval);
			}

			// Then along g() line to best approximation to f()
			gy = g_y.evalbern2D(x, y);
			gx = g_x.evalbern2D(x, y);
			fx = f_x.evalbern2D(x, y);
			fy = f_y.evalbern2D(x, y);
			double fu = gx * fy - gy * fx;
			lambda = -fval / fu;
			delta_x = -lambda * gy;
			delta_y = lambda * gx;
			x += delta_x;
			y += delta_y;

			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			if (in_unit_square(x, y) && Math.abs(fval * gval) < Math.abs(lastgood.f_val * lastgood.g_val)) {
				lastgood = new Solve2Dresult(x, y, fval, gval, true);
			}
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("sol %6.3f %6.3f %9.6f dx %9.6f%n", x, y, fval, gval);
			}
			if (Math.abs(lastgood.f_val) > GOOD_SOL_TOL && Math.abs(gval) < GOOD_SOL_TOL * GOOD_SOL_TOL)
				break;
		}

		// if(Math.abs(val)>1e-6 || Math.abs(dx_val)>1e-6)
		// BoxClevA.log.printf("fin %s %9.6f dx %9.6f%n",cur,
		// val,dx_val);

		if (!in_unit_square(x, y))
			lastgood.good = false;
		if(	Math.abs(lastgood.f_val) > GOOD_SOL_TOL)
			lastgood.good = false;

		return lastgood;
	}

	private Solve2Dresult itterate_node_three_deriv(double start_x, double start_y, Bern2D f, Bern2D f_x, Bern2D f_y, Bern2D g,
			Bern2D g_x, Bern2D g_y, Bern2D h, Bern2D h_x, Bern2D h_y, Bern2D i, Bern2D i_x, Bern2D i_y)
			throws AsurfException {

		double x = start_x, y = start_y;
		// BoxClevA.log.printf("org %s %9.6f dx %9.6f%n",cur,
		// bb.evalbern2D(cur),dx.evalbern2D(cur));

		double fval = f.evalbern2D(x, y);
		double gval = g.evalbern2D(x, y);
		double hval = h.evalbern2D(x, y);
		double ival = i.evalbern2D(x, y);
		Solve2Dresult lastgood = new Solve2Dresult(x, y, fval, gval, hval, ival, true);

		// First onto surface, assuming linear approx for f,
		// move along normal to closest point on plane f()=0
		double fx = f_x.evalbern2D(x, y);
		double fy = f_y.evalbern2D(x, y);
		double sumsq = fx * fx + fy * fy;
		double delta_x = -fval * fx / sumsq;
		double delta_y = -fval * fy / sumsq;
		x += delta_x;
		y += delta_y;
		fval = f.evalbern2D(x, y);
		gval = g.evalbern2D(x, y);
		hval = h.evalbern2D(x, y);
		ival = i.evalbern2D(x, y);

		if (in_unit_square(x, y) && Math.abs(fval) < Math.abs(lastgood.f_val)) {
			lastgood = new Solve2Dresult(x, y, fval, gval, hval, ival, true);
		}
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("sol %6.3f %6.3f f %9.6f g %9.6f h %9.6f i %9.6f%n", x, y, fval, gval, hval, ival);
		}

		for (int j = 0; j < 5; ++j) {
			// find the point on the line approximating f()
			// which intersects the line approximating g()
			fx = f_x.evalbern2D(x, y); // tangent is (-fy, fx)
			fy = f_y.evalbern2D(x, y);

			double gy = g_y.evalbern2D(x, y);
			double gx = g_x.evalbern2D(x, y);
			double dx_u = fx * gy - fy * gx; // direction derivative along tangent
			double lambda = -gval / dx_u; // multiple of tangent
			if (Math.abs(lambda) > 1e9) { // failed
				if(failCountD++==0) {
				BoxClevA.log.printf("exploded%n");
				BoxClevA.log.printf("sol %6.3f %6.3f f %9.6f g %9.6f h %9.6f%n", x, y, fval, gval, hval);
				BoxClevA.log.printf("fx %6.3f fy %6.3f gx %9.6f gy %9.6f dg<u> %9.6f lambda %9.6f%n", fx, fy, gx, gy,
						dx_u, lambda);
				}
				break;
			}
			x += lambda * -fy; // move along tangent
			y += lambda * fx;
			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			hval = h.evalbern2D(x, y);
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("g   %6.3f %6.3f f %9.6f g %9.6f h %9.6f i %9.6f%n", x, y, fval, gval, hval, ival);
			}

			double hx, hy;
			// Then along g() line to best approximation to h()
			gy = g_y.evalbern2D(x, y);
			gx = g_x.evalbern2D(x, y);
			hx = h_x.evalbern2D(x, y);
			hy = h_y.evalbern2D(x, y);
			double fu = gx * hy - gy * hx;
			lambda = -hval / fu;
			delta_x = -lambda * gy;
			delta_y = lambda * gx;
			x += delta_x;
			y += delta_y;

			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			hval = h.evalbern2D(x, y);
			ival = i.evalbern2D(x, y);

			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("g   %6.3f %6.3f f %9.6f g %9.6f h %9.6f i %9.6f%n", x, y, fval, gval, hval, ival);
			}
			if (Math.abs(gval) < GOOD_SOL_TOL * GOOD_SOL_TOL)
				break;

			double ix, iy;
			// Then along h() line to best approximation to i()
			hx = h_x.evalbern2D(x, y);
			hy = h_y.evalbern2D(x, y);
			iy = i_y.evalbern2D(x, y);
			ix = i_x.evalbern2D(x, y);
			fu = -gx * hy + gy * hx;
			lambda = -ival / fu;
			delta_x = -lambda * hy;
			delta_y = lambda * hx;
			x += delta_x;
			y += delta_y;

			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			hval = h.evalbern2D(x, y);
			ival = i.evalbern2D(x, y);

			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("g   %6.3f %6.3f f %9.6f g %9.6f h %9.6f i %9.6f%n", x, y, fval, gval, hval, ival);
			}
			if (Math.abs(gval) < GOOD_SOL_TOL * GOOD_SOL_TOL)
				break;

			// Then along i() line to best approximation to f()
			iy = i_y.evalbern2D(x, y);
			ix = i_x.evalbern2D(x, y);
			fx = f_x.evalbern2D(x, y);
			fy = f_y.evalbern2D(x, y);
			fu = ix * fy - iy * fx;
			lambda = -fval / fu;
			delta_x = -lambda * iy;
			delta_y = lambda * ix;
			x += delta_x;
			y += delta_y;

			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			hval = h.evalbern2D(x, y);
			ival = i.evalbern2D(x, y);

			if (in_unit_square(x, y) && Math.abs(fval * gval * hval * ival) < Math
					.abs(lastgood.f_val * lastgood.g_val * lastgood.h_val * lastgood.i_val)) {
				lastgood = new Solve2Dresult(x, y, fval, gval, hval, ival, true);
			}
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("i   %6.3f %6.3f f %9.6f g %9.6f h %9.6f i %9.6f%n", x, y, fval, gval, hval, ival);
			}
			if (Math.abs(gval) < GOOD_SOL_TOL * GOOD_SOL_TOL)
				break;
		}

		// if(Math.abs(val)>1e-6 || Math.abs(dx_val)>1e-6)
		// BoxClevA.log.printf("fin %s %9.6f dx %9.6f%n",cur,
		// val,dx_val);

		if (!in_unit_square(x, y))
			lastgood.good = false;
		if(	Math.abs(lastgood.f_val) > GOOD_SOL_TOL)
			lastgood.good = false;

		return lastgood;
	}

	/**
	 * Iterate where f may be a self intersection or nodal line. f_x, f_y vanishes so cant use those.
	 * Possible that g, h are tangent.
	 * @param start_x
	 * @param start_y
	 * @param f
	 * @param f_x
	 * @param f_y
	 * @param g
	 * @param g_x
	 * @param g_y
	 * @param h
	 * @param h_x
	 * @param h_y
	 * @param i
	 * @param i_x
	 * @param i_y
	 * @return
	 * @throws AsurfException
	 */
	private Solve2Dresult itterate_node_just_three_deriv(double start_x, double start_y, Bern2D f, Bern2D f_x, Bern2D f_y, Bern2D g,
			Bern2D g_x, Bern2D g_y, Bern2D h, Bern2D h_x, Bern2D h_y, Bern2D i, Bern2D i_x, Bern2D i_y)
			throws AsurfException {

		double x = start_x, y = start_y;
		// BoxClevA.log.printf("org %s %9.6f dx %9.6f%n",cur,
		// bb.evalbern2D(cur),dx.evalbern2D(cur));

		double fval = f.evalbern2D(x, y);
		double gval = g.evalbern2D(x, y);
		double hval = h.evalbern2D(x, y);
		double ival = i.evalbern2D(x, y);
		Solve2Dresult lastgood = new Solve2Dresult(x, y, fval, gval, hval, ival, true);

		// First onto surface, assuming linear approx for f,
		// move along normal to closest point on plane f()=0
		double fx = f_x.evalbern2D(x, y);
		double fy = f_y.evalbern2D(x, y);
		double sumsq = fx * fx + fy * fy;
		double delta_x = -fval * fx / sumsq;
		double delta_y = -fval * fy / sumsq;
		x += delta_x;
		y += delta_y;
		fval = f.evalbern2D(x, y);
		gval = g.evalbern2D(x, y);
		hval = h.evalbern2D(x, y);
		ival = i.evalbern2D(x, y);

		if (in_unit_square(x, y) && Math.abs(fval) < Math.abs(lastgood.f_val)) {
			lastgood = new Solve2Dresult(x, y, fval, gval, hval, ival, true);
		}
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("sol %6.3f %6.3f f %9.6f g %9.6f h %9.6f i %9.6f%n", x, y, fval, gval, hval, ival);
		}
		double gx,gy,hx,hy,ix,iy,det_gh,det_gi,det_hi;
		for (int j = 0; j < 5; ++j) {
			// find the point on the line approximating f()
			// which intersects the line approximating g()
//			fx = f_x.evalbern2D(x, y); // tangent is (-fy, fx)
//			fy = f_y.evalbern2D(x, y);

			gx = g_x.evalbern2D(x, y); // inefficient as at least two of these are equal
			gy = g_y.evalbern2D(x, y);
			hx = h_x.evalbern2D(x, y);
			hy = h_y.evalbern2D(x, y);
			ix = i_x.evalbern2D(x, y);
			iy = i_y.evalbern2D(x, y);
			
			det_gh = gx * hy - gy * hx;
			det_gi = gx * iy - gy * ix;
			det_hi = hx * iy - hy * ix;

			if(Math.abs(det_gh) >= Math.abs(det_gi) && Math.abs(det_gh) >= Math.abs(det_hi)) {
				delta_x = (- hy * gval + gy * hval) / det_gh;
				delta_y = ( hx * gval - gx * hval) / det_gh;
			} else if(Math.abs(det_gi) >= Math.abs(det_gh) && Math.abs(det_gi) >= Math.abs(det_hi)) {
				delta_x = (- iy * gval + gy * ival) / det_gi;
				delta_y = ( ix * gval - gx * ival) / det_gi;			
			} else {
				delta_x = (- iy * hval + hy * ival) / det_hi;
				delta_y = ( ix * hval - hx * ival) / det_hi;
			}

			x += delta_x;
			y += delta_y;				
			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			hval = h.evalbern2D(x, y);
			ival = i.evalbern2D(x, y);
			if (in_unit_square(x, y) && Math.abs(fval * gval * hval * ival) < Math
					.abs(lastgood.f_val * lastgood.g_val * lastgood.h_val * lastgood.i_val)) {
				lastgood = new Solve2Dresult(x, y, fval, gval, hval, ival, true);
			}
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("i   %6.3f %6.3f f %9.6f g %9.6f h %9.6f i %9.6f%n", x, y, fval, gval, hval, ival);
			}
			if (Math.abs(gval) < GOOD_SOL_TOL * GOOD_SOL_TOL)
				break;
		}

		// if(Math.abs(val)>1e-6 || Math.abs(dx_val)>1e-6)
		// BoxClevA.log.printf("fin %s %9.6f dx %9.6f%n",cur,
		// val,dx_val);

		if (!in_unit_square(x, y))
			lastgood.good = false;
		if(	Math.abs(lastgood.f_val) > GOOD_SOL_TOL)
			lastgood.good = false;

		return lastgood;
	}

	private Solve2DresultWithSig converge_node_three_deriv(FacePos pos, Sheaf2D s, int signDx,
			int signDy, int signDz) throws AsurfException {

		Bern2D f_x;
		Bern2D f_y;
		switch (pos.face.type) {
		case FACE_LL:
		case FACE_RR:
			f_x = s.dy;
			f_y = s.dz;
			break;

		case FACE_FF:
		case FACE_BB:
			f_x = s.dx;
			f_y = s.dz;
			break;

		case FACE_DD:
		case FACE_UU:
			f_x = s.dx;
			f_y = s.dy;
			break;

		default:
			throw new AsurfException("Bad sol type " + pos.face);
		}
		if (f_x instanceof Bern2D.NegBern2D || f_x instanceof Bern2D.PosBern2D || f_x instanceof Bern2D.ZeroBern2D) {
			f_x = s.aa.diffX();
		}
		if (f_y instanceof Bern2D.NegBern2D || f_y instanceof Bern2D.PosBern2D || f_y instanceof Bern2D.ZeroBern2D) {
			f_y = s.aa.diffY();
		}

		Solve2Dresult tgt_conv = null;

		Bern2D dx_x = s.dx.diffX();
		Bern2D dx_y = s.dx.diffY();
		Bern2D dy_x = s.dy.diffX();
		Bern2D dy_y = s.dy.diffY();
		Bern2D dz_x = s.dz.diffX();
		Bern2D dz_y = s.dz.diffY();

		tgt_conv = itterate_node_three_deriv(pos.x,pos.y, s.aa, f_x, f_y, s.dx, dx_x, dx_y, s.dy, dy_x, dy_y, s.dz, dz_x, dz_y);
		if(tgt_conv.good)
				return new Solve2DresultWithSig(tgt_conv,0,0,0);
		
		// Failed three deriv try 2 deriv
		Solve2DresultWithSig best_res=null;
		{
		Solve2Dresult res_xy = this.itterate_node_two_deriv(pos.x, pos.y, s.aa, f_x, f_y, s.dx, dx_x, dx_y, s.dy, dy_x, dy_y);
		if(res_xy.good ) {
			double val = s.dz.evalbern2D(res_xy.x, res_xy.y);
			best_res = new Solve2DresultWithSig(res_xy,0,0,val>0?1:(val<0?-1:0));
		}
	}
		{
		Solve2Dresult res_xz = this.itterate_node_two_deriv(pos.x, pos.y, s.aa, f_x, f_y, s.dx, dx_x, dx_y, s.dz, dz_x, dz_y);
		if(res_xz.good && ( best_res == null || Math.abs(res_xz.f_val) < Math.abs(best_res.f_val) )) {
			int val = s.dy.evalbern2Dsign(res_xz.x, res_xz.y);
			best_res = new Solve2DresultWithSig(res_xz,0,val,0);
		}
	}
		{
		Solve2Dresult res_yz = this.itterate_node_two_deriv(pos.x, pos.y, s.aa, f_x, f_y, s.dy, dy_x, dy_y, s.dz, dz_x, dz_y);
		if(res_yz.good && ( best_res == null || Math.abs(res_yz.f_val) < Math.abs(best_res.f_val) )) {
			int val = s.dx.evalbern2Dsign(res_yz.x, res_yz.y);
			best_res = new Solve2DresultWithSig(res_yz,val,0,0);
		}
		}
		if(best_res != null)
			return best_res;

		// Try 1 deriv
		{
			Solve2Dresult res_x = this.itterate_node_one_deriv(pos.x, pos.y, s.aa, f_x, f_y, s.dx, dx_x, dx_y);
			if(res_x.good ) {
				int valy = s.dy.evalbern2Dsign(res_x.x, res_x.y);
				int valz = s.dz.evalbern2Dsign(res_x.x, res_x.y);
				best_res = new Solve2DresultWithSig(res_x,0, valy, valz);
			}
	}
			{
		Solve2Dresult res_y = this.itterate_node_one_deriv(pos.x, pos.y, s.aa, f_x, f_y, s.dy, dy_x, dy_y);

			if(res_y.good && ( best_res == null || Math.abs(res_y.f_val) < Math.abs(best_res.f_val) )) {
				int valx = s.dx.evalbern2Dsign(res_y.x, res_y.y);
				int valz = s.dz.evalbern2Dsign(res_y.x, res_y.y);
				best_res = new Solve2DresultWithSig(res_y, valx, 0, valz);
			}
	}
			{
		Solve2Dresult res_z = this.itterate_node_one_deriv(pos.x, pos.y, s.aa, f_x, f_y, s.dz, dz_x, dz_y);
			if(res_z.good && ( best_res == null || Math.abs(res_z.f_val) < Math.abs(best_res.f_val) )) {
				int valx = s.dx.evalbern2Dsign(res_z.x, res_z.y);
				int valy = s.dy.evalbern2Dsign(res_z.x, res_z.y);
				best_res = new Solve2DresultWithSig(res_z, valx, valy, 0);
		}
	}
		if(best_res != null)
			return best_res;
		
		{
			Solve2Dresult res0 = itterate_node_zero_deriv(pos,s);
			int valx = s.dx.evalbern2Dsign(res0.x, res0.y);
			int valy = s.dy.evalbern2Dsign(res0.x, res0.y);
			int valz = s.dz.evalbern2Dsign(res0.x, res0.y);
			// nothing works use the best of a bad bunch
			best_res = new Solve2DresultWithSig(res0, valx, valy,  valz);
		}
		/*
		if( Math.abs(res_xy.f_val) < Math.abs(best_res.f_val)) {
			double val = dz.evalbern2D(res_xy.x, res_xy.y);
			best_res = new Solve2DresultWithSig(res_xy,0,0,val>0?1:(val<0?-1:0));
		}
		if(Math.abs(res_xz.f_val) < Math.abs(best_res.f_val)) {
			double val = dy.evalbern2D(res_xz.x, res_xz.y);
			best_res = new Solve2DresultWithSig(res_xz,0,val>0?1:(val<0?-1:0),0);
		}
		if(Math.abs(res_yz.f_val) < Math.abs(best_res.f_val)) {
			double val = dx.evalbern2D(res_yz.x, res_yz.y);
			best_res = new Solve2DresultWithSig(res_yz,val>0?1:(val<0?-1:0),0,0);
		}
		if( Math.abs(res_x.f_val) < Math.abs(best_res.f_val)) {
			double valy = dy.evalbern2D(res_x.x, res_x.y);
			double valz = dz.evalbern2D(res_x.x, res_x.y);
			best_res = new Solve2DresultWithSig(res_x,0, valy>0?1:(valy<0?-1:0), valz>0?1:(valz<0?-1:0));
		}
		if(Math.abs(res_y.f_val) < Math.abs(best_res.f_val)) {
			best_res = res_y;
		}
		if(Math.abs(res_z.f_val) < Math.abs(best_res.f_val)) {
			best_res = res_z;
		}
		*/
		return best_res;
	}

	
	Solve2DresultWithSig converge_node_just_three_deriv(FacePos pos, Sheaf2D s, int signDx,
			int signDy, int signDz) throws AsurfException {
		Bern2D bb = s.aa;
		Bern2D dx = s.dx;
		Bern2D dy = s.dy;
		Bern2D dz = s.dz;
		Bern2D f_x;
		Bern2D f_y;
		switch (pos.face.type) {
		case FACE_LL:
		case FACE_RR:
			f_x = dy;
			f_y = dz;
			break;

		case FACE_FF:
		case FACE_BB:
			f_x = dx;
			f_y = dz;
			break;

		case FACE_DD:
		case FACE_UU:
			f_x = dx;
			f_y = dy;
			break;

		default:
			throw new AsurfException("Bad sol type " + pos.face);
		}
		if (f_x instanceof Bern2D.NegBern2D || f_x instanceof Bern2D.PosBern2D || f_x instanceof Bern2D.ZeroBern2D) {
			f_x = bb.diffX();
		}
		if (f_y instanceof Bern2D.NegBern2D || f_y instanceof Bern2D.PosBern2D || f_y instanceof Bern2D.ZeroBern2D) {
			f_y = bb.diffY();
		}

		Solve2Dresult tgt_conv = null;

		Bern2D dx_x = dx.diffX();
		Bern2D dx_y = dx.diffY();
		Bern2D dy_x = dy.diffX();
		Bern2D dy_y = dy.diffY();
		Bern2D dz_x = dz.diffX();
		Bern2D dz_y = dz.diffY();

		tgt_conv = itterate_node_just_three_deriv(pos.x,pos.y, bb, f_x, f_y, dx, dx_x, dx_y, dy, dy_x, dy_y, dz, dz_x, dz_y);
		return new Solve2DresultWithSig(tgt_conv,0,0,0);
	}

	/**
	 * Converge to soln f(x)=0, g(x)=0 (here g(x) will be one of derivs of f(x))
	 * Algorithm first uses one step Newton method along gradient to converge to
	 * surface f(x,y) ~= f(0,0) + x f_x(0,0) + y f_y(0,0) ~= 0 and x = lambda f_x, y
	 * = lambda f_y So f(0) + lambda (f_x^2 +f_y^2) = 0 and lambda = -f(0)/(f_x^2
	 * +f_y^2).
	 * 
	 * Next converge a one step iteration to g(x)=0 along the tangent to f(x), u =
	 * (-f_y, f_x). dg<u> = dg_x * ux + g_y * uy g(x,y) ~= g(0,0) + lambda dg<u>
	 * lambda = - g(0,0)/dg<u>
	 * 
	 * Next one step along tangent to g(x,y) find a point with f(x,y)~=0, g(x,y)~=0.
	 * 
	 * Repeat the last two steps a few times.
	 * 
	 * A bug can occur is (f_x,f_y) . (g_x,g_y) = 0 leading to division by zero,
	 * generally only occurs if g(x,y) is very small so about loop early.
	 * 
	 * @param sol
	 * @param f
	 * @param g
	 * @param dx_y
	 * @param dx_x
	 * @param f_y2
	 * @param f_x2
	 * @return
	 * @throws AsurfException
	 */
	private Solve2Dresult itterate_node_two_deriv(double start_x, double start_y, Bern2D f, Bern2D f_x, Bern2D f_y, Bern2D g, Bern2D g_x,
			Bern2D g_y, Bern2D h, Bern2D h_x, Bern2D h_y) throws AsurfException {

		double x = start_x, y = start_y;
		// BoxClevA.log.printf("org %s %9.6f dx %9.6f%n",cur,
		// bb.evalbern2D(cur),dx.evalbern2D(cur));

		double fval = f.evalbern2D(x, y);
		double gval = g.evalbern2D(x, y);
		double hval = h.evalbern2D(x, y);
		Solve2Dresult lastgood = new Solve2Dresult(x, y, fval, gval, hval, true);

		// First onto surface, assuming linear approx for f,
		// move along normal to closest point on plane f()=0
		double fx = f_x.evalbern2D(x, y);
		double fy = f_y.evalbern2D(x, y);
		double sumsq = fx * fx + fy * fy;
		double delta_x = -fval * fx / sumsq;
		double delta_y = -fval * fy / sumsq;
		x += delta_x;
		y += delta_y;
		fval = f.evalbern2D(x, y);
		gval = g.evalbern2D(x, y);
		hval = h.evalbern2D(x, y);

		if (in_unit_square(x, y) && Math.abs(fval) < Math.abs(lastgood.f_val)) {
			lastgood = new Solve2Dresult(x, y, fval, gval, hval, true);
		}
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("sol %6.3f %6.3f f %9.6f g %9.6f h %9.6f%n", x, y, fval, gval, hval);
		}

		for (int i = 0; i < 5; ++i) {
			// find the point on the line approximating f()
			// which intersects the line approximating g()
			fx = f_x.evalbern2D(x, y); // tangent is (-fy, fx)
			fy = f_y.evalbern2D(x, y);

			double gy = g_y.evalbern2D(x, y);
			double gx = g_x.evalbern2D(x, y);
			double dx_u = fx * gy - fy * gx; // direction derivative along tangent
			double lambda = -gval / dx_u; // multiple of tangent
			if (Math.abs(lambda) > 1e9) { // failed
				if(failCountE++==0) {
				BoxClevA.log.printf("exploded%n");
				BoxClevA.log.printf("sol %6.3f %6.3f f %9.6f g %9.6f h %9.6f%n", x, y, fval, gval, hval);
				BoxClevA.log.printf("fx %6.3f fy %6.3f gx %9.6f gy %9.6f dg<u> %9.6f lambda %9.6f%n", fx, fy, gx, gy,
						dx_u, lambda);
				}
				break;
			}
			delta_x = lambda * -fy;
			delta_y = lambda * fx;
			
			x += delta_x; // move along tangent
			y += delta_y;
			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			hval = h.evalbern2D(x, y);
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("g   %6.3f %6.3f f %9.6f g %9.6f h %9.6f%n", x, y, fval, gval, hval);
			}

			double hx, hy;
			// Then along g() line to best approximation to h()
			gy = g_y.evalbern2D(x, y);
			gx = g_x.evalbern2D(x, y);
			hx = h_x.evalbern2D(x, y);
			hy = h_y.evalbern2D(x, y);
			double fu = gx * hy - gy * hx;
			lambda = -hval / fu;
			delta_x = -lambda * gy;
			delta_y = lambda * gx;
			x += delta_x;
			y += delta_y;

			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			hval = h.evalbern2D(x, y);

			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("h   %6.3f %6.3f f %9.6f g %9.6f h %9.6f%n", x, y, fval, gval, hval);
			}

			// Then along h() line to best approximation to f()
			hy = h_y.evalbern2D(x, y);
			hx = h_x.evalbern2D(x, y);
			fx = f_x.evalbern2D(x, y);
			fy = f_y.evalbern2D(x, y);
			fu = hx * fy - hy * fx;
			lambda = -fval / fu;
			delta_x = -lambda * hy;
			delta_y = lambda * hx;
			x += delta_x;
			y += delta_y;

			fval = f.evalbern2D(x, y);
			gval = g.evalbern2D(x, y);
			hval = h.evalbern2D(x, y);

			if (in_unit_square(x, y)
					&& Math.abs(fval * gval * hval) < Math.abs(lastgood.f_val * lastgood.g_val * lastgood.h_val)) {
				lastgood = new Solve2Dresult(x, y, fval, gval, hval, true);
			}
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("sol %6.3f %6.3f %9.6f dx %9.6f%n", x, y, fval, gval);
			}
		}

		if (!in_unit_square(x, y))
			lastgood.good = false;
		if(	Math.abs(lastgood.f_val) > GOOD_SOL_TOL)
			lastgood.good = false;
		return lastgood;
	}

	Solve2DresultWithSig converge_node_two_deriv(FacePos pos, Sheaf2D s, int signDx,
			int signDy, int signDz) throws AsurfException {
		Bern2D bb = s.aa;
		Bern2D dx = s.dx;
		Bern2D dy = s.dy;
		Bern2D dz = s.dz;
		
		BernPair bp = getBernsOfFace(pos,bb,dx,dy,dz);

		if (signDx == 0 && signDy == 0) {
			Bern2D dx_x = dx.diffX();
			Bern2D dx_y = dx.diffY();
			Bern2D dy_x = dy.diffX();
			Bern2D dy_y = dy.diffY();

			Solve2Dresult tgt_conv = itterate_node_two_deriv(pos.x,pos.y, bb, bp.f_x, bp.f_y, dx, dx_x, dx_y, dy, dy_x, dy_y);
			
			int valx = 0;
			int valy = 0;
			int valz = dz.evalbern2Dsign(tgt_conv.x, tgt_conv.y);

			return  new Solve2DresultWithSig(tgt_conv, valx, valy,  valz);

		} else if (signDy == 0 && signDz == 0) {
			Bern2D dy_x = dy.diffX();
			Bern2D dy_y = dy.diffY();
			Bern2D dz_x = dz.diffX();
			Bern2D dz_y = dz.diffY();

			Solve2Dresult tgt_conv = itterate_node_two_deriv(pos.x, pos.y, bb, bp.f_x, bp.f_y, dy, dy_x, dy_y, dz, dz_x, dz_y);

			int valx = dx.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
			int valy = 0;
			int valz = 0;

			return  new Solve2DresultWithSig(tgt_conv, valx, valy,  valz);

		} else if (signDx == 0 && signDz == 0) {
			Bern2D dx_x = dx.diffX();
			Bern2D dx_y = dx.diffY();
			Bern2D dz_x = dz.diffX();
			Bern2D dz_y = dz.diffY();

			Solve2Dresult tgt_conv = itterate_node_two_deriv(pos.x, pos.y, bb, bp.f_x, bp.f_y, dx, dx_x, dx_y, dz, dz_x, dz_y);

			int valx = 0;
			int valy = dy.evalbern2Dsign(tgt_conv.x, tgt_conv.y);
			int valz = 0;
			return  new Solve2DresultWithSig(tgt_conv, valx, valy,  valz);

		} else {
			throw new AsurfException("Need at least two derivs zero");
		}
	}

	/**
	 * Used with second derivative where no info about signs
	 * 
	 * @param bb
	 * @param dx
	 * @param dy  may be null
	 * @param dz  may be null
	 * @return
	 * @throws AsurfException
	 */
	public Solve3DresultWithSig converge_sing(BoxPos pos, Bern3D bb, Bern3D dx, Bern3D dy, Bern3D dz) throws AsurfException {
		int num_zero_derivs = (dx != null ? 1 : 0) + (dy != null ? 1 : 0) + (dz != null ? 1 : 0);

		if (num_zero_derivs == 3) {
			Solve3Dresult res = itterate_sing_matrix_three_deriv_zero(pos, bb, dx, dy, dz);
			if (res.good) {
				if(PRINT_CONVERGE)
					BoxClevA.log.println("Conv sing matrix 3 deriv success ");
					return new Solve3DresultWithSig(res,0,0,0);
			}
			if(PRINT_CONVERGE)
			BoxClevA.log.println("Conv sing matrix 3 deriv failed " + " pos " + res);
		} else if (num_zero_derivs == 2) {
		} else if (num_zero_derivs == 1) {
		}
		throw new AsurfException("converge_sing with only 2 derivs zero");
	}


	public Solve3DresultWithSig converge_sing(BoxPos pos, Bern3D bb, Bern3D dx, Bern3D dy, Bern3D dz, int signDx, int signDy, int signDz)
			throws AsurfException {
		int num_zero_derivs = (signDx == 0 ? 1 : 0) + (signDy == 0 ? 1 : 0) + (signDz == 0 ? 1 : 0);

		if (signDx == 0 && signDy == 0 && signDz == 0 && dx != null && dy != null && dz != null) {
			Solve3Dresult res3deriv = itterate_sing_matrix_three_deriv_zero(pos, bb, dx, dy, dz);
			if(res3deriv.good) {
				return new Solve3DresultWithSig(res3deriv,0,0,0);
			}
			Solve3Dresult res_xy = itterate_sing_matrix_two_deriv_zero(pos, bb, dx, dy);
			Solve3Dresult res_xz = itterate_sing_matrix_two_deriv_zero(pos, bb, dx, dz);
			Solve3Dresult res_yz = itterate_sing_matrix_two_deriv_zero(pos, bb, dy, dz);

			Solve3DresultWithSig best_result=null;
			if(res_xy.good) {
				best_result = new Solve3DresultWithSig(res_xy, 
						0, 
						0, 
						ctx.evalbern3D(dz, res_xy.x,res_xy.y, res_xy.z) > 0 ? 1 : -1);
			}
			if(res_xz.good) {
				if(best_result== null || Math.abs(res_xy.f_val) < Math.abs(best_result.f_val)) {
					best_result = new Solve3DresultWithSig(res_xy, 
						0, 
						ctx.evalbern3D(dy, res_xz.x,res_xz.y, res_xz.z) > 0 ? 1 : -1, 
						0);
				}
			}
			if(res_yz.good) {
				if(best_result== null || Math.abs(res_yz.f_val) < Math.abs(best_result.f_val)) {
					best_result = new Solve3DresultWithSig(res_yz, 
						ctx.evalbern3D(dx, res_yz.x,res_yz.y, res_yz.z) > 0 ? 1 : -1,
						0,
						0);
				}
			}
			if(best_result!=null)
				return best_result;		
			
			Solve3Dresult res = this.converge_sing_zero_deriv(pos, bb);
			best_result = new Solve3DresultWithSig(res_yz, 
					ctx.evalbern3D(dx, res.x,res.y, res.z) > 0 ? 1 : -1,
					ctx.evalbern3D(dy, res.x,res.y, res.z) > 0 ? 1 : -1,
					ctx.evalbern3D(dz, res.x,res.y, res.z) > 0 ? 1 : -1, false);
			return best_result;
			
		} else if (num_zero_derivs == 2) {
			Bern3D first = (signDx == 0 ? dx : dy);
			Bern3D second = (signDx == 0 ? (signDy == 0 ? dy : dz) : dz);

			Solve3Dresult res = itterate_sing_matrix_two_deriv_zero(pos, bb, first, second);
			if (res.good) {
				if (PRINT_CONVERGE)
					BoxClevA.log.println("Conv sing 2 deriv matrix success ");
				return new Solve3DresultWithSig(res,signDx,signDy,signDz);
			} else {
				if (PRINT_CONVERGE)
					BoxClevA.log.println("Conv sing 2 deriv matrix failed " + res);
			}
		}
		
		Solve3Dresult res_x=null, res_y=null, res_z=null, best_res=null; 
		Integer sig_x=null, sig_y=null, sig_z=null;
		if (signDx == 0 && dx != null) {
			res_x = converge_sing_one_deriv(pos, bb, dx);
			if(res_x.good) {
				best_res = res_x;
				sig_x = 0;
			}
			if (PRINT_CONVERGE) {
				if(res_x.good) {
					BoxClevA.log.println("Conv sing dx success ");
				} else {
					BoxClevA.log.println("Conv sing dx failed ");
				}
				BoxClevA.log.println(res_x);
			}
		}
		if (signDy == 0 && dy != null) {
			res_y = converge_sing_one_deriv(pos, bb, dy);
			if(res_y.good) { 
				if(best_res == null 
				 || Math.abs(res_y.f_val) < Math.abs(best_res.f_val)) {
						best_res = res_y;
				}
				sig_y = 0;
			}
			if (PRINT_CONVERGE) {
				if(res_y.good) {
					BoxClevA.log.println("Conv sing dy success ");
				} else {
					BoxClevA.log.println("Conv sing dy failed ");
				}
				BoxClevA.log.println(res_y);
			}
		}
		if (signDz == 0 && dz != null) {
			res_z = converge_sing_one_deriv(pos, bb, dz);
			if(res_z.good) {
				if(best_res == null
				 ||  Math.abs(res_z.f_val) < Math.abs(best_res.f_val)) {
						best_res = res_z;
				}
				sig_z = 0;
			}
			if (PRINT_CONVERGE) {
				if(res_z.good) {
					BoxClevA.log.println("Conv sing dz success ");
				} else {
					BoxClevA.log.println("Conv sing dz failed ");
				}
				BoxClevA.log.println(res_z);
			}
		}

		if(best_res == null) { // they all failed, at least try to return a point on the surface
			best_res = new Solve3Dresult(false,pos.x,pos.y,pos.z,ctx.evalbern3D(bb, pos.x, pos.y, pos.z),0.0);
		}
		
		if(sig_x ==null) {
			if(signDx !=0) {
				sig_x = signDx;
			} else {
				double dxval = ctx.evalbern3D(dx, best_res.x, best_res.y, best_res.z);
				sig_x = dxval < 0 ? -1 : dxval > 0 ? 1 : 0; 
			}
		}
		if(sig_y ==null) {
			if(signDy !=0) {
				sig_y = signDy;
			} else {
				double dyval = ctx.evalbern3D(dy, best_res.x, best_res.y, best_res.z);
				sig_y = dyval < 0 ? -1 : dyval > 0 ? 1 : 0; 
			}
		}
		if(sig_z ==null) {
			if(signDz !=0) {
				sig_z = signDz;
			} else {
				double dzval = ctx.evalbern3D(dx, best_res.x, best_res.y, best_res.z);
				sig_z = dzval < 0 ? -1 : dzval > 0 ? 1 : 0; 
			}
		}
		return new Solve3DresultWithSig(best_res,sig_x,sig_y,sig_z);
	}
	
	
	/**
	 * @param bb
	 * @param dx
	 * @param dy
	 * @param dz
	 */
	public Solve3DresultWithSig converge_sing_3deriv(BoxPos pos, Bern3D bb, Bern3D dx, Bern3D dy, Bern3D dz) {

		Solve3Dresult res = itterate_sing_matrix_three_deriv_zero(pos, bb, dx, dy, dz);
		if (res.good) {
			if (PRINT_CONVERGE)
				BoxClevA.log.println("Conv sing matrix 3 deriv success ");
				return new Solve3DresultWithSig(res,0,0,0);
		} else {
			if (PRINT_CONVERGE)
				BoxClevA.log.println("Conv sing matrix 3 deriv failed " + res);
		}
		
		Solve3Dresult res_xy = itterate_sing_matrix_two_deriv_zero(pos, bb, dx, dy);
		Solve3Dresult res_xz = itterate_sing_matrix_two_deriv_zero(pos, bb, dx, dz);
		Solve3Dresult res_yz = itterate_sing_matrix_two_deriv_zero(pos, bb, dy, dz);
		
		boolean flag_xy = res_xy.good;
		boolean flag_xz = res_xz.good;
		boolean flag_yz = res_yz.good;
		
		if(flag_xy && !flag_xz && !flag_yz) {
			double dzval = ctx.evalbern3D(dz, res_xy.x, res_xy.y, res_xy.z);
			int sig_z = dzval < 0 ? -1 : dzval > 0 ? 1 : 0; 
			return new Solve3DresultWithSig(res_xy,0,0,sig_z);
		}

		if(flag_xz && !flag_xy && !flag_yz) {
			double dyval = ctx.evalbern3D(dy, res_xz.x, res_xz.y, res_xz.z);
			int sig_y = dyval < 0 ? -1 : dyval > 0 ? 1 : 0; 
			return new Solve3DresultWithSig(res_xy,0,sig_y,0);
		}

		if(flag_yz && !flag_xy && !flag_xz) {
			double dxval = ctx.evalbern3D(dx, res_xz.x, res_xz.y, res_xz.z);
			int sig_x = dxval < 0 ? -1 : dxval > 0 ? 1 : 0; 
			return new Solve3DresultWithSig(res_yz,sig_x,0,0);
		}

		BoxClevA.log.println("Bad number of 2 deriv solutions\n");
		BoxClevA.log.println("xy "+ flag_xy + " "+res_xy.toString());
		BoxClevA.log.println("xz "+ flag_xz + " "+res_xz.toString());
		BoxClevA.log.println("yz "+ flag_yz + " "+res_yz.toString());
		return null;
		
	}


	/**
	 * This version converges to df/dx = df/dy = df/dz = 0 and checks the final
	 * result also has f() = 0. It relies on the fact that d2f/dxdy = d2f/dydx to
	 * achieve some efficiency
	 * 
	 * @param sol
	 * @param bb
	 * @param dx
	 * @param dy
	 * @param dz
	 * @return never null, always a position in the box, but with the good flag set
	 */
	private Solve3Dresult itterate_sing_matrix_three_deriv_zero(BoxPos pos, Bern3D bb, Bern3D dx, Bern3D dy, Bern3D dz) {

		Vec3D vec = new Vec3D(pos.x,pos.y,pos.z);
		double[] oldvec = new double[] { pos.x,pos.y,pos.z };

		double f0 = ctx.evalbern3D(bb, vec.x,vec.y,vec.z);
		double fx = ctx.evalbern3D(dx, vec.x,vec.y,vec.z);
		double fy = ctx.evalbern3D(dy, vec.x,vec.y,vec.z);
		double fz = ctx.evalbern3D(dz, vec.x,vec.y,vec.z);

		Bern3D dxx = dx.diffX();
		Bern3D dxy = dx.diffY();
		Bern3D dxz = dx.diffZ();

		Bern3D dyy = dy.diffY();
		Bern3D dyz = dy.diffZ();

		Bern3D dzz = dz.diffZ();

		if (PRINT_CONVERGE)
			BoxClevA.log.printf("start [%6.3f %6.3f %6.3f] f %9.6f dx %9.6f %9.6f %9.6f%n", 
					vec.x, vec.y, vec.z, f0, fx, fy, fz);

		double best_val = f0;

		for (int i = 0; i < 10; ++i) {

			double fxx = ctx.evalbern3D(dxx, vec.x,vec.y,vec.z);
			double fxy = ctx.evalbern3D(dxy, vec.x,vec.y,vec.z);
			double fxz = ctx.evalbern3D(dxz, vec.x,vec.y,vec.z);

			double fyy = ctx.evalbern3D(dyy, vec.x,vec.y,vec.z);
			double fyz = ctx.evalbern3D(dyz, vec.x,vec.y,vec.z);

			double fzz = ctx.evalbern3D(dzz, vec.x,vec.y,vec.z);

			Matrix3D mat = new Matrix3D(fxx, fxy, fxz, fxy, fyy, fyz, fxz, fyz, fzz);
			double det=mat.det();
			if(Math.abs(det)<GOOD_SOL_TOL*GOOD_SOL_TOL*GOOD_SOL_TOL) {
				if (PRINT_CONVERGE)
					BoxClevA.log.printf("Det %12.9f break%n",det);
				break;
			}
			Matrix3D inv = mat.inverse();
			Vec3D b = new Vec3D(-fx, -fy, -fz);
			Vec3D res = inv.mult(b);

			vec = vec.add(res);
			f0 = ctx.evalbern3D(bb, vec.x,vec.y,vec.z);
			fx = ctx.evalbern3D(dx, vec.x,vec.y,vec.z);
			fy = ctx.evalbern3D(dy, vec.x,vec.y,vec.z);
			fz = ctx.evalbern3D(dz, vec.x,vec.y,vec.z);
			if (PRINT_CONVERGE)
				BoxClevA.log.printf("itt [%9.6f %9.6f %9.6f] f %9.6f dx %9.6f %9.6f %9.6f det %9.6f%n", 
					vec.x, vec.y, vec.z, 
					f0, fx,	fy, fz, det);
			
			if(Math.abs(f0) < Math.abs(best_val) && in_unit_box(vec)) {
				best_val = f0;
				oldvec[0] = vec.x;
				oldvec[1] = vec.y;
				oldvec[2] = vec.z;
			}


		}
		return new Solve3Dresult(in_unit_box(vec) && Math.abs(best_val) < GOOD_SOL_TOL,oldvec,best_val,fx,fy,fz);
	}

	/**
	 * This converges to f() = g() = h(), g and h will be derivatives of f 
	 * 
	 * @param sol
	 * @param ff
	 * @param gg
	 * @param hh
	 * @return
	 */
	private Solve3Dresult itterate_sing_matrix_two_deriv_zero(BoxPos pos, Bern3D ff, Bern3D gg, Bern3D hh) {

		Vec3D vec = new Vec3D(pos);
		double[] oldvec = new double[] { pos.x,pos.y,pos.z };
		double f = ctx.evalbern3D(ff, vec.x,vec.y,vec.z);
		double g = ctx.evalbern3D(gg, vec.x,vec.y,vec.z);
		double h = ctx.evalbern3D(hh, vec.x,vec.y,vec.z);

		
		Bern3D f_x = ff.diffX();
		Bern3D f_y = ff.diffY();
		Bern3D f_z = ff.diffZ();

		Bern3D g_x = gg.diffX();
		Bern3D g_y = gg.diffY();
		Bern3D g_z = gg.diffZ();

		Bern3D h_x = hh.diffX();
		Bern3D h_y = hh.diffY();
		Bern3D h_z = hh.diffZ();

		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("start [%6.3f %6.3f %6.3f] f %9.6f g %9.6f h %9.6f%n", vec.x, vec.y, vec.z, f, g, h);
		}

		double best_val = f;
		
		for (int i = 0; i < 10; ++i) {
			double fx = ctx.evalbern3D(f_x, vec.x,vec.y,vec.z);
			double fy = ctx.evalbern3D(f_y, vec.x,vec.y,vec.z);
			double fz = ctx.evalbern3D(f_z, vec.x,vec.y,vec.z);

			double gx = ctx.evalbern3D(g_x, vec.x,vec.y,vec.z);
			double gy = ctx.evalbern3D(g_y, vec.x,vec.y,vec.z);
			double gz = ctx.evalbern3D(g_z, vec.x,vec.y,vec.z);

			double hx = ctx.evalbern3D(h_x, vec.x,vec.y,vec.z);
			double hy = ctx.evalbern3D(h_y, vec.x,vec.y,vec.z);
			double hz = ctx.evalbern3D(h_z, vec.x,vec.y,vec.z);

			Matrix3D mat = new Matrix3D(fx, fy, fz, gx, gy, gz, hx, hy, hz);
			double det = mat.det(); 
			if(Math.abs(det)< GOOD_SOL_TOL * GOOD_SOL_TOL * GOOD_SOL_TOL) {
				if (PRINT_CONVERGE)
					BoxClevA.log.printf("Det %12.9f break%n",det);
				break;
			}

			Matrix3D inv = mat.inverse();
			Vec3D b = new Vec3D(-f, -g, -h);
			Vec3D res = inv.mult(b);
			double distsq = res.distSq();
			vec = vec.add(res);
			f = ctx.evalbern3D(ff, vec.x,vec.y,vec.z);
			g = ctx.evalbern3D(gg, vec.x,vec.y,vec.z);
			h = ctx.evalbern3D(hh, vec.x,vec.y,vec.z);
			
			if(Math.abs(f) < Math.abs(best_val) && in_unit_box(vec)) {
				best_val = f;
				oldvec[0] = vec.x;
				oldvec[1] = vec.y;
				oldvec[2] = vec.z;
			}
			if(Math.abs(f) < GOOD_SOL_TOL * GOOD_SOL_TOL && Math.abs(g) < GOOD_SOL_TOL * GOOD_SOL_TOL && Math.abs(h) < GOOD_SOL_TOL * GOOD_SOL_TOL)
				return new Solve3Dresult(in_unit_box(vec),oldvec,best_val,g,h);
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("start [%6.3f %6.3f %6.3f] f %9.6f g %9.6f h %9.6f%n", vec.x, vec.y, vec.z, f, g, h);
			}

			if(distsq < 1e-12) break;
		}
		return new Solve3Dresult(in_unit_box(vec) && Math.abs(best_val) < GOOD_SOL_TOL,oldvec,best_val,g,h);
	}

	/**
	 * Converge 
	 * @param pos
	 * @param bb
	 * @param A
	 * @return Never null, always returns the last best possible solution
	 */
	public Solve3Dresult converge_sing_one_deriv(BoxPos pos, Bern3D bb, Bern3D A) {

		double vec[] = new double[3], oldvec[] = new double[3];
		double val, dx, dy, dz, Aval;
		int i;
		double sumsq;
		Bern3D bbx = null, bby = null, bbz = null, Ax = null, Ay = null, Az = null;

		vec[0] = pos.x;
		vec[1] = pos.y;
		vec[2] = pos.z;

			bbx = bb.diffX();
			bby = bb.diffY();
			bbz = bb.diffZ();

		Ax = A.diffX();
		Ay = A.diffY();
		Az = A.diffZ();
		Aval = ctx.evalbern3D(A, vec[0],vec[1],vec[2]);
		val = ctx.evalbern3D(bb, vec[0],vec[1],vec[2]);

		oldvec[0] = vec[0];
		oldvec[1] = vec[1];
		oldvec[2] = vec[2];
		double best_val = val;
		double best_aval = Aval;
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("converge_sing 1 deriv:%n");
			BoxClevA.log.printf("ini ");
			BoxClevA.log.printf("[%9.6f, %9.6f, %9.6f] %9.6f dx %9.6f%n", vec[0], vec[1], vec[2], ctx.evalbern3D(bb, vec[0],vec[1],vec[2]),
					Aval);
		}

		for (i = 0; i < 10; ++i) {
			/* first converge onto surface */

			dx = ctx.evalbern3D(bbx, vec[0],vec[1],vec[2]);
			dy = ctx.evalbern3D(bby, vec[0],vec[1],vec[2]);
			dz = ctx.evalbern3D(bbz, vec[0],vec[1],vec[2]);
			sumsq = dx * dx + dy * dy + dz * dz;
			vec[0] -= val * dx / sumsq;
			vec[1] -= val * dy / sumsq;
			vec[2] -= val * dz / sumsq;
			double distsq = vec[0]*vec[0]+vec[1]*vec[1]+vec[2]*vec[2];
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf(" %d ", i);
				BoxClevA.log.printf("[%9.6f, %9.6f, %9.6f] %9.6f dx %9.6f%n", vec[0], vec[1], vec[2], ctx.evalbern3D(bb, vec[0],vec[1],vec[2]),
						ctx.evalbern3D(A, vec[0],vec[1],vec[2]));
			}

			/* then converge onto dx */

			Aval = ctx.evalbern3D(A, vec[0],vec[1],vec[2]);
			dx = ctx.evalbern3D(Ax, vec[0],vec[1],vec[2]);
			dy = ctx.evalbern3D(Ay, vec[0],vec[1],vec[2]);
			dz = ctx.evalbern3D(Az, vec[0],vec[1],vec[2]);
			sumsq = dx * dx + dy * dy + dz * dz;
			vec[0] -= Aval * dx / sumsq;
			vec[1] -= Aval * dy / sumsq;
			vec[2] -= Aval * dz / sumsq;
			distsq += vec[0]*vec[0]+vec[1]*vec[1]+vec[2]*vec[2];

			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("dx ");
				BoxClevA.log.printf("[%9.6f, %9.6f, %9.6f] %9.6f dx %9.6f%n", vec[0], vec[1], vec[2], ctx.evalbern3D(bb, vec[0],vec[1],vec[2]),
						ctx.evalbern3D(A, vec[0],vec[1],vec[2]));
			}

			if (vec[0] != vec[0]) {
				BoxClevA.log.printf("ERR: NaN in converge_sing1\n");
				BoxClevA.log.printf("ERR: %f %f %f %f %f\n", val, dx, dy, dz, sumsq);

				vec[0] = oldvec[0];
				vec[1] = oldvec[1];
				vec[2] = oldvec[2];
				Solve3Dresult res = new Solve3Dresult(false,vec,val,Aval);
				return res;
			}
			val = ctx.evalbern3D(bb, vec[0],vec[1],vec[2]);
			if(Math.abs(val) < Math.abs(best_val) && in_unit_box(vec)) {
				oldvec[0] = vec[0];
				oldvec[1] = vec[1];
				oldvec[2] = vec[2];
				best_val = val;
				best_aval = Aval;
			}
			if(distsq < 1e-12) break;
		}
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("converge_sing 1 deriv done: ");
		}
		Solve3Dresult res = new Solve3Dresult(in_unit_box(vec) && Math.abs(best_val) < GOOD_SOL_TOL,oldvec,best_val,best_aval);
		return res;
	}

	
	public Solve3Dresult converge_sing_zero_deriv(BoxPos pos, Bern3D bb) {

		double vec[] = new double[3], oldvec[] = new double[3];
		double val, dx, dy, dz;
		int i;
		double sumsq;
		Bern3D bbx = null, bby = null, bbz = null;

		vec[0] = pos.x;
		vec[1] = pos.y;
		vec[2] = pos.z;

			bbx = bb.diffX();
			bby = bb.diffY();
			bbz = bb.diffZ();

		val = ctx.evalbern3D(bb, vec[0],vec[1],vec[2]);

		oldvec[0] = vec[0];
		oldvec[1] = vec[1];
		oldvec[2] = vec[2];
		double best_val = val;
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("converge_sing 0 deriv:%n");
			BoxClevA.log.printf("ini ");
			BoxClevA.log.printf("[%9.6f, %9.6f, %9.6f] %9.6f%n", vec[0], vec[1], vec[2], ctx.evalbern3D(bb, vec[0],vec[1],vec[2]));
		}

		for (i = 0; i < 10; ++i) {
			/* first converge onto surface */

			dx = ctx.evalbern3D(bbx, vec[0],vec[1],vec[2]);
			dy = ctx.evalbern3D(bby, vec[0],vec[1],vec[2]);
			dz = ctx.evalbern3D(bbz, vec[0],vec[1],vec[2]);
			sumsq = dx * dx + dy * dy + dz * dz;
			vec[0] -= val * dx / sumsq;
			vec[1] -= val * dy / sumsq;
			vec[2] -= val * dz / sumsq;

			if (PRINT_CONVERGE) {
				BoxClevA.log.printf(" %d ", i);
				BoxClevA.log.printf("[%9.6f, %9.6f, %9.6f] %9.6f%n", vec[0], vec[1], vec[2], ctx.evalbern3D(bb, vec[0],vec[1],vec[2]));
			}

			if (vec[0] != vec[0]) {
				BoxClevA.log.printf("ERR: NaN in converge_sing2\n");
				BoxClevA.log.printf("ERR: %f %f %f %f %f\n", val, dx, dy, dz, sumsq);

				vec[0] = oldvec[0];
				vec[1] = oldvec[1];
				vec[2] = oldvec[2];
				Solve3Dresult res = new Solve3Dresult(false,vec,val);
				return res;
			}
			val = ctx.evalbern3D(bb, vec[0],vec[1],vec[2]);
			if(Math.abs(val) < Math.abs(best_val) && in_unit_box(vec)) {
				oldvec[0] = vec[0];
				oldvec[1] = vec[1];
				oldvec[2] = vec[2];
				best_val = val;
			}
		}
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("converge_sing 0 deriv done: ");
		}
		Solve3Dresult res = new Solve3Dresult(true,vec,best_val);
		return res;
	}

	
	private boolean in_unit_square(double x, double y) {
		return x >= 0 && x <= 1 && y >= 0 && y <= 1;
	}

	private boolean in_unit_box(Vec3D vec) {
		return vec.x >= 0 && vec.x <= 1 && vec.y >= 0 && vec.y <= 1 && vec.z >=0 && vec.z <= 1;
	}

	private boolean in_unit_box(double[] vec) {
		return vec[0] >= 0 && vec[0] <= 1 && vec[1] >= 0 && vec[1] <= 1 && vec[2] >=0 && vec[2] <= 1;
	}

	private Solve2Dresult itterate_node_zero_deriv(FacePos pos, Bern2D f) throws AsurfException {

		double x = pos.x, y = pos.y;
		Bern2D f_x = f.diffX();
		Bern2D f_y = f.diffY();
		return itterate_node_zero_deriv(x,y,f,f_x,f_y);
	}
	
	private Solve2Dresult itterate_node_zero_deriv(FacePos pos, Sheaf2D s) throws AsurfException {

		double x = pos.x, y = pos.y;
		Bern2D f_x,f_y;
		switch (pos.face.type) {
		case FACE_LL:
		case FACE_RR:
			f_x = s.dy;
			f_y = s.dz;
			break;

		case FACE_FF:
		case FACE_BB:
			f_x = s.dx;
			f_y = s.dz;
			break;

		case FACE_DD:
		case FACE_UU:
			f_x = s.dx;
			f_y = s.dy;
			break;

		default:
			throw new AsurfException("Bad sol type " + pos.face);
		}
		return itterate_node_zero_deriv(x,y,s.aa,f_x,f_y);
	}

	
	private Solve2Dresult itterate_node_zero_deriv(double x,double y, Bern2D f,Bern2D f_x, Bern2D f_y) throws AsurfException {
		double fval = f.evalbern2D(x, y);
		Solve2Dresult lastgood = new Solve2Dresult(x, y, fval, true);
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("st  %6.3f %6.3f %9.6f%n", x, y, fval);
		}

		// First onto surface, assuming linear approx for f,
		// move along normal to closest point on plane f()=0
		double fx = f_x.evalbern2D(x, y);
		double fy = f_y.evalbern2D(x, y);
		double sumsq = fx * fx + fy * fy;
		double delta_x = -fval * fx / sumsq;
		double delta_y = -fval * fy / sumsq;
		x += delta_x;
		y += delta_y;
		fval = f.evalbern2D(x, y);

		if (in_unit_square(x, y) && Math.abs(fval) < Math.abs(lastgood.f_val)) {
			lastgood = new Solve2Dresult(x, y, fval, true);
		}
		if (PRINT_CONVERGE) {
			BoxClevA.log.printf("sol %6.3f %6.3f %9.6f%n", x, y, fval);
		}

		for (int i = 0; i < 5; ++i) {
			fx = f_x.evalbern2D(x, y); // tangent is (-fy, fx)
			fy = f_y.evalbern2D(x, y);
			sumsq = fx * fx + fy * fy;
			delta_x = -fval * fx / sumsq;
			delta_y = -fval * fy / sumsq;
			x += delta_x;
			y += delta_y;
			fval = f.evalbern2D(x, y);
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("sol %6.3f %6.3f %9.6f%n", x, y, fval);
			}
			if (in_unit_square(x, y) && Math.abs(fval) < Math.abs(lastgood.f_val)) {
				lastgood = new Solve2Dresult(x, y, fval, true);
			}
			if (PRINT_CONVERGE) {
				BoxClevA.log.printf("sol %6.3f %6.3f %9.6f%n", x, y, fval);
			}
		}


		if (!in_unit_square(x, y))
			lastgood.good = false;
		return lastgood;

	}

	public Solve2DresultWithSig converge_node_zero_deriv(FacePos fp, Sheaf2D s) throws AsurfException {
		Solve2Dresult result =  itterate_node_zero_deriv(fp,s.aa);
		int valx = s.dx.evalbern2Dsign(result.x, result.y);
		int valy = s.dy.evalbern2Dsign(result.x, result.y);
		int valz = s.dz.evalbern2Dsign(result.x, result.y);
		return new Solve2DresultWithSig(result,valx,valy,valz);
	}

	public void printResults() {
		System.out.format("Converger: fail A %d B %d C %d D %d E %d%n",
				failCountA,failCountB,failCountC,failCountD,failCountE);
	}

	public List<Double> allSols(Bern1D bern,double min,double max) throws AsurfException {
		Bern1D dx = bern.diff();
		return allSols(bern,dx,new ArrayList<Double>(),min,max,0);
	}
	
	List<Double> allSols(Bern1D bern,Bern1D dx,List<Double> sols,double min,double max,int depth) {
		if(bern.getSign()!=0)
			return sols;
		if(dx.getSign()==0 && depth<20) {
			var br = bern.reduce();
			var dr = dx.reduce();
			double mid = (min+max)/2.0;
			allSols(br.l,dr.l,sols,min,mid,depth+1);
			allSols(br.r,dr.r,sols,mid,max,depth+1);
			return sols;
		}
		
		Solve1DResult res = converge_edge(bern, dx);
		double pos = min + res.root * (max - min);
		sols.add(pos);
		return sols;
	}

}
