/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Region_info implements RegionBean {
    final public double xmin;
    final public double xmax;
    final public double ymin;
    final public double ymax;
    final public double zmin;
    final public double zmax;
    final private double xwid;
    private final double ywid;
    private final double zwid;

	public Region_info(double xmin, double xmax, double ymin, double ymax,
			double zmin, double zmax) {
		super();
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.zmin = zmin;
		this.zmax = zmax;
		this.xwid = xmax - xmin;
		this.ywid = ymax - ymin;
		this.zwid = zmax - zmin;
	}

	public Region_info(RegionBean bean) {
		this(bean.getXMin(),bean.getXMax(),bean.getYMin(),bean.getYMax(),bean.getZMin(),bean.getZMax());
	}

	@Override
	public String toString() {
		return String.format(
		"range [%f %f] [%f %f] [%f %f]",
	  	xmin,xmax,ymin,ymax,zmin,zmax);
	}
	
	double[] relative_position(double[] actual) {
		double relative[] = new double[3];
		relative[0] = (actual[0]-xmin)/xwid;
		relative[1] = (actual[1]-ymin)/ywid;
		relative[2] = (actual[2]-zmin)/zwid;
		return relative;
	}
	
	public double[] actualPosition(double relx,double rely,double relz) {
		return new double[] {
				xmin + relx * xwid,
				ymin + rely * ywid,
				zmin + relz * zwid
		};
	}

	public double[] actualPosition(double[] rel_pos) {
		return actualPosition(rel_pos[0],rel_pos[1],rel_pos[2]);
	}
	
	public double[] size() {
		return new double[] { (xwid), (ywid), (zwid) };
	}

	/**
	 * @param norm
	 * @return
	 */
	double[] calc_norm_actual(double[] norm) {
		double res[] = new double[3];
	    res[0] = norm[0] / (xwid);
	    res[1] = norm[1] / (ywid);
	    res[2] = norm[2] / (zwid);
		return res;
	}

	double[] calc_second_derivs_actual(double[] relative) {
		double res[] = new double[6];
	    res[0] = relative[0] / (xwid * xwid);
	    res[1] = relative[1] / (xwid * ywid);
	    res[2] = relative[2] / (xwid * zwid);
	    res[3] = relative[3] / (ywid * ywid);
	    res[4] = relative[4] / (ywid * zwid);
	    res[5] = relative[5] / (zwid * zwid);
		return res;
	}

	@Override
	public double getXMin() {
		return xmin;
	}

	@Override
	public void setXMin(double x) {
	}

	@Override
	public double getXMax() {
		return xmax;
	}

	@Override
	public void setXMax(double x) {
	}

	@Override
	public double getYMin() {
		return ymin;
	}

	@Override
	public void setYMin(double y) {
	}

	@Override
	public double getYMax() {
		return ymax;
	}

	@Override
	public void setYMax(double y) {
	}

	@Override
	public double getZMin() {
		return zmin;
	}

	@Override
	public void setZMin(double z) {
	}

	@Override
	public double getZMax() {
		return zmax;
	}

	@Override
	public void setZMax(double z) {
	}
}
