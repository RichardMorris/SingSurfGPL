/*
Created 14 Jun 2010 - Richard Morris
 */
package org.singsurf.singsurf.asurf;

import org.singsurf.singsurf.asurf.Converger.Solve3DresultWithSig;

public final class Sol_info {
	public static final int UNPLOTTED_VERTEX = -1;
	
	public int xl,yl,zl,denom;
	private double root,root2,root3;
	private int dx,dy,dz; 
//	public short dxx,dxy,dxz,dyy,dyz,dzz;
	public Key3D type;
	public boolean status=false; 
	public boolean is_sing=false;
	public int plotindex=UNPLOTTED_VERTEX;
	private boolean hasVal = false;
	private boolean hasPos = false;
	private boolean hasNorm = false;
	private double[] pos;
	private double[] norm;
	public boolean conv_failed=false;
	public int adjNum=UNPLOTTED_VERTEX;
	Double meanCurvature;

	public Sol_info(Key3D type, int xl, int yl, int zl, int denom, double root) {
		this.type = type;
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.root =root;
		this.root2 = 0.0;
		this.root3 = 0.0;
	}

	public Sol_info(Key3D type, int xl, int yl, int zl, int denom, double root1,double root2) {
		this.type = type;
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.root = root1;
		this.root2 = root2;
		this.root3 = 0.0;
	}

	public Sol_info(Key3D type, int xl, int yl, int zl, int denom, double root1,double root2,double root3) {
		this.type = type;
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.root = root1;
		this.root2 = root2;
		this.root3 = root3;
	}

	/**
	 * Calculates position of point relative to the big box
	 */

	public double[] calc_relative_pos()
	{
		double vec[]=new double[3];
		switch(this.type)
		{
		case X_AXIS:
			vec[0] = (this.xl+this.getRoot())/this.denom;
			vec[1] = ((double) this.yl)/this.denom;
			vec[2] = ((double) this.zl)/this.denom;
			break;
		case Y_AXIS:
			vec[0] = ((double) this.xl)/this.denom;
			vec[1] = (this.yl+this.getRoot())/this.denom;
			vec[2] = ((double) this.zl)/this.denom;
			break;
		case Z_AXIS:
			vec[0] = ((double) this.xl)/this.denom;
			vec[1] = ((double) this.yl)/this.denom;
			vec[2] = (this.zl+this.getRoot())/this.denom;
			break;
		case FACE_LL:
		case FACE_RR:
			vec[0] = ((double) this.xl)/this.denom;
			vec[1] = (this.yl + this.getRoot())/this.denom;
			vec[2] = (this.zl + this.getRoot2())/this.denom;
			break;
		case FACE_FF:
		case FACE_BB:
			vec[0] = (this.xl + this.getRoot())/this.denom;
			vec[1] = ((double) this.yl)/this.denom;
			vec[2] = (this.zl + this.getRoot2())/this.denom;
			break;
		case FACE_DD:
		case FACE_UU:
			vec[0] = (this.xl + this.getRoot())/this.denom;
			vec[1] = (this.yl + this.getRoot2())/this.denom;
			vec[2] = ((double) this.zl)/this.denom;
			break;
		case BOX:
			vec[0] = (this.xl + this.getRoot())/this.denom;
			vec[1] = (this.yl + this.getRoot2())/this.denom;
			vec[2] = (this.zl + this.getRoot3())/this.denom;
			break;
		default:
			vec[0] = ((double) this.xl)/this.denom;
			vec[1] = ((double) this.yl)/this.denom;
			vec[2] = ((double) this.zl)/this.denom;
			break;
		}
		return vec;
	}

	/**
	 * Count the number of derivative which are zero
	 * @return
	 */
	public int num_zero_derivs() {
		int res = (getDx()==0 ? 1 : 0) + (getDy()==0 ? 1 : 0) + (getDz()==0 ? 1 : 0);
		return res;
	}
	
	/**
	 * Whether the derivative signature is identical to this
	 * @param sol
	 * @return
	 */
	public boolean match_derivs(Sol_info sol) {
		return getDx() == sol.getDx() && getDy() == sol.getDy() && getDz() == sol.getDz();
	}
	
	/* get the values for the roots, inverse of calc_pos */

	void calc_roots(double relative_pos[])
	{
		root = relative_pos[0] * this.denom - this.xl;		
		root2 = relative_pos[1] * this.denom - this.yl;		
		root3 = relative_pos[2] * this.denom - this.zl;
	}

	public String toStringBrief() {
		return this.toStringCore().toString();
	}

	public String toString() {
		return toString(BoxClevA.unsafeRegion);
	}

	public StringBuilder toStringCore() {
		return toStringCore("    sol ");
	}
	public StringBuilder toStringCore(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		switch(type)
		{
		case X_AXIS: case Y_AXIS: case Z_AXIS:
			sb.append(type+" ");
			break;
		case FACE_LL: case FACE_RR: case FACE_FF: case FACE_BB:
		case FACE_DD: case FACE_UU:
			sb.append(type);
			break;
		case BOX:
			sb.append(type+"    ");
			break;
		default:
			sb.append(type);
			break;
		}

		sb.append(String.format(" (%2d,%2d,%2d)/%2d",
				xl,yl,zl,denom));
		sb.append(String.format("\tdx %2d %2d %2d ",
				getDx(),getDy(),getDz() ));

		if(conv_failed) 
			sb.append("f ");
		else
			sb.append("t ");
		switch(type)
		{
		case X_AXIS: case Y_AXIS: case Z_AXIS:
			sb.append(String.format("rts %6.3f   *   *   ",getRoot()));
			break;
		case FACE_LL: case FACE_RR: case FACE_FF: case FACE_BB:
		case FACE_DD: case FACE_UU:
			sb.append(String.format("rts %6.3f %6.3f *",getRoot(),getRoot2()));
			break;
		case BOX:
			sb.append(String.format("rts %6.3f %6.3f %6.3f",
					getRoot(),getRoot2(),getRoot3()));
			break;
		default:
			sb.append("Bad type:");
			break;
		}
		return sb;
	}

	public String toString(Region_info region1) {
		StringBuilder sb = this.toStringCore();
		Region_info region = region1; 
		
		double rel_pos[] = calc_relative_pos();
		double actual_pos[] = region.actualPosition(rel_pos);

		double value = BoxClevA.unsafeBern.threadSafeEvalbern3D(rel_pos[0],rel_pos[1],rel_pos[2]);
		sb.append(String.format("\t%6.3f %6.3f %6.3f val % 3.1e",
				actual_pos[0],actual_pos[1],actual_pos[2],value));
		
		if(this.meanCurvature!=null) 
			sb.append(String.format(" H=%4.1f",meanCurvature));
		return sb.toString();
	}

	/**
	 * Require {@link #calc_pos_actual(BoxClevA)} has been called before
	 * @return
	 */
	public String toStringNorm() {
		StringBuilder sb = this.toStringCore();
		sb.append(String.format(" pos [%6.3f %6.3f %6.3f] norm [%9.6f %9.6f %9.6f]",
			pos[0],pos[1],pos[2],norm[0],norm[1],norm[2]));
		if(this.meanCurvature!=null) 
			sb.append(String.format(" H=%6.3f",meanCurvature));
		return sb.toString();
	}

	public boolean match_derivs(int f1, int f2, int f3) {
		return getDx() == f1 && getDy() == f2 && getDz() == f3;
	}

	public double[] calc_pos_actual(Region_info region,Bern3DContext ctx) {
		if(this.hasPos)
			return pos;
		double relative_pos[] = this.calc_relative_pos();
		pos = region.actualPosition(relative_pos);
		hasPos=true;
		return pos;
	}

	public double[] calc_norm_actual_safe(Region_info region,Bern3DContext ctx) {
		calc_norm_internal(region, ctx);
		return norm.clone();
	}

	/**
	 * @param boxclev
	 * @param ctx
	 */
	private void calc_norm_internal(Region_info region, Bern3DContext ctx) {
		if(!hasNorm) {
			double relative_pos[] = this.calc_relative_pos();
			final double[] rel_norm = ctx.calc_norm_relative(relative_pos);
			norm = region.calc_norm_actual(rel_norm);
			hasNorm=true;
		}
	}

	public double[] calc_norm_actual_unsafe(Region_info region,Bern3DContext ctx) {
		calc_norm_internal(region, ctx);
		return norm;
	}

	double[] unit_normal=null;
	double normal_length;

	private double value;
	public double[] calc_unit_norm(Region_info region,Bern3DContext ctx) {
		calc_norm_internal(region, ctx);
		if(unit_normal!=null)
			return unit_normal;
		unit_normal = norm.clone();
		normal_length = Vec3D.normalise(unit_normal);
		return unit_normal;
	}
	
	
	public String toStringNorm(Region_info region,Bern3DContext ctx) {
		calc_norm_internal(region, ctx);
		return toStringNorm();
	}
	
	public Sol_info duplicate() {
		Sol_info sol = new Sol_info(type,xl,yl,zl,denom,root,root2,root3);
		sol.dx = getDx();
		sol.dy = getDy();
		sol.dz = getDz();
		sol.conv_failed = conv_failed;
		sol.hasVal = hasVal;
		sol.hasPos = hasPos;
		sol.hasNorm = hasNorm;
		if(hasVal) {
			sol.value = this.value;
		}
		if(hasPos) {
			sol.pos = new double[] {pos[0],pos[1],pos[2]};
		}
		if(hasNorm) {
			sol.setNorm(new double[] {norm[0],norm[1],norm[2]});
		}
		return sol;
	}

	public double calcMeanCurvature(Region_info region,Bern3DContext ctx) {
		if(meanCurvature!=null) return meanCurvature;
		double[] pos = calc_relative_pos();
		double[] norm = calc_norm_actual_unsafe(region,ctx);

		double[] second =  region.calc_second_derivs_actual(ctx.calc_second_derivs_relative(pos));
		
		double fx = norm[0], fy = norm[1], fz = norm[2];
		double fxx = second[0], fxy=second[1], fxz=second[2], 
				fyy = second[3], fyz = second[4], fzz = second[5];
		double numer = 
				+ fxx * (fy * fy + fz * fz) 
				+ fyy * (fx * fx + fz * fz) 
				+ fzz * (fx * fx + fy * fy)
				- 2 * fxy * fx * fy 
				- 2 * fxz * fx * fz 
				- 2 * fyz * fy * fz;
		double denom = 2 * Math.pow(fx * fx + fy * fy + fz * fz, 1.5);
		double H = numer / denom;

		meanCurvature = H;
		return meanCurvature;
	}

	public double getRoot() {
		return root;
	}

	public double getRoot2() {
		return root2;
	}

	public double getRoot3() {
		return root3;
	}

	public void flipNormal() {
		norm[0] = - norm[0];
		norm[1] = - norm[1];
		norm[2] = - norm[2];	
		if(unit_normal!=null) {
			unit_normal[0] = - unit_normal[0];
			unit_normal[1] = - unit_normal[1];
			unit_normal[2] = - unit_normal[2];
		}
	}


	public void setNorm(double[] norm) {
		this.norm = norm;
		this.unit_normal=null;
		this.hasNorm=true;
	}

	public void setNorm(double g_val, double h_val, double i_val) {
		setNorm(new double[] {g_val,h_val,i_val});
	}

	public void setRoots(double pos_x, double pos_y, double pos_z) {
		root = pos_x;
		root2 = pos_y;
		root3 = pos_z;
		hasVal = hasPos = hasNorm=false;
		this.unit_normal=null;
	}

	public void setRoots(double x, double y) {
		root = x;
		root2 = y;
		hasVal = hasPos = hasNorm=false;
		this.unit_normal=null;
	}
	
	public void setRoot(double d) {
		root = d;
		hasVal = hasPos = hasNorm=false;
		this.unit_normal=null;
	}

	public double calcValue(Bern3DContext ctx) {
		if(hasVal)
			return value;
		double[] rel_pos = calc_relative_pos();
		value = ctx.calc_val_actual(rel_pos);
		hasVal = true;
		return value;
	}

	public void setValue(double val) {
		value = val;
		hasVal = true;
	}
	
	public double calcGaussianCurvature(Region_info region, Bern3DContext ctx) {

		double[] pos = calc_relative_pos();
		double[] norm = calc_norm_actual_unsafe(region,ctx);

		double[] second =  region.calc_second_derivs_actual(ctx.calc_second_derivs_relative(pos));

		double fx = norm[0], fy = norm[1], fz = norm[2];
		double fxx = second[0], fxy=second[1], fxz=second[2], 
				fyy = second[3], fyz = second[4], fzz = second[5];

		double numer = 
				+ (fzz * fyy - fyz * fyz) * fx * fx 
				+ (fxx * fzz - fxz * fxz) * fy * fy
				+ (fxx * fyy - fxy * fxy) * fz * fz 
				+ 2 * (fxy * fxz - fxx * fyz) * fy * fz
				+ 2 * (fxy * fyz - fyy * fxz) * fx * fz 
				+ 2 * (fxz * fyz - fzz * fxy) * fx * fy;
		double lamsq = (fx * fx + fy * fy + fz * fz);
		double denom = lamsq * lamsq;
		double K = numer / denom;
		return K;
	}

	public int getDx() {
		return dx;
	}

	public int getDy() {
		return dy;
	}

	public int getDz() {
		return dz;
	}

	public void setDerivs(int f1, int f2, int f3) {
		dx = f1; dy=f2; dz=f3;
		
	}

	public void setDerivs(Solve3DresultWithSig conv_res) {
		dx = conv_res.sig_x;
		dy = conv_res.sig_y;
		dz = conv_res.sig_z;
		
	}

	public void setDerivs(Sol_info sol) {
		dx = sol.dx;
		dy = sol.dy;
		dz = sol.dz;
		
	}

	public void setDx(int dx) {
		this.dx = dx;
	}

	public void setDy(int dy) {
		this.dy = dy;
	}

	public void setDz(int dz) {
		this.dz = dz;
	}


}