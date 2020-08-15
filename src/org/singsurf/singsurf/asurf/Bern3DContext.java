/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

import org.singsurf.singsurf.acurve.Bern1D;

public class Bern3DContext { 

    static final Bern3D ZERO_BERN_3D = new Bern3D(0, 0, 0);

    final double[][][] pyramid;
    final double workA[], workB[], workC[];

    final int pyrX, pyrY, pyrZ;
    final int xord,yord,zord;
	Bern3D BB; 	// Bernstein polynomial for whole region
	Bern3D Dx;
	Bern3D Dy;
	Bern3D Dz;
	Bern3D Dxx;
	Bern3D Dxy;
	Bern3D Dxz;
	Bern3D Dyy;
	Bern3D Dyz;
	Bern3D Dzz;


    public Bern3DContext(int xord1, int yord1, int zord1) {
	    xord = xord1;
	    yord = yord1;
	    zord = zord1;
	if (xord1 * 2 + 1 > -1 || yord1 * 2 + 1 > -1 || zord1 * 2 + 1 > -1) {
	    pyrX = xord1 * 2 + 1;
	    pyrY = yord1 * 2 + 1;
	    pyrZ = zord1 * 2 + 1;
	    pyramid = new double[pyrX][pyrY][pyrZ];
	    workA = new double[pyrX];
	    workB = new double[pyrY];
	    workC = new double[pyrZ];
	} else {
		pyrX = -1;
		pyrY = -1;
		pyrZ = -1;
		pyramid = null;
		workA = null;
		workB = null;
		workC = null;
	}
    }

    public Bern3DContext(double aa[][][]) {
	this( aa.length - 1,
	 aa[0].length - 1,
	 aa[0][0].length - 1);

     }
    
    
    public Bern3DContext(Bern3DContext base) {
    	this(base.xord,base.yord,base.zord);
    	this.BB = new Bern3D(base.BB);
    	this.Dx = new Bern3D(base.Dx);
    	this.Dy = new Bern3D(base.Dy);
    	this.Dz = new Bern3D(base.Dz);
    	this.Dxx = new Bern3D(base.Dxx);
    	this.Dxy = new Bern3D(base.Dxy);
    	this.Dxz = new Bern3D(base.Dxz);
    	this.Dyy = new Bern3D(base.Dyy);
    	this.Dyz = new Bern3D(base.Dyz);
    	this.Dzz = new Bern3D(base.Dzz);
    }

    public Bern3D copy(Bern3D source) {
    	return new Bern3D(source);
    }
    
    public Bern3D makeBern3D(double aa[][][], Region_info region) {
	// double c[MAXORDER];
	Bern1D d;
	int row, col, stack;

	/*** first convert polynomials in z ***/

	int xord = aa.length - 1;
	int yord = aa[0].length - 1;
	int zord = aa[0][0].length - 1;
	Bern3D bb = new Bern3D(xord,yord,zord);

	double c[] = new double[zord + 1];
	for (row = 0; row <= xord; row++)
	    for (col = 0; col <= yord; col++) {
		for (stack = 0; stack <= zord; ++stack)
		    c[stack] = aa[row][col][stack];

		d = Bern1D.formbernstein1D(c, region.zmin, region.zmax);

		for (stack = 0; stack <= zord; ++stack)
			bb.coeff[(row * (bb.yord + 1) + col) * (bb.zord + 1) + stack] = d.coeff[stack];
	    }

	/*** next polynomials in y ***/

	c = new double[yord + 1];
	for (row = 0; row <= xord; row++)
	    for (stack = 0; stack <= zord; ++stack) {
		for (col = 0; col <= yord; col++)
		    c[col] = bb.coeff[(row * (bb.yord + 1) + col) * (bb.zord + 1) + stack];
		d = Bern1D.formbernstein1D(c, region.ymin, region.ymax);
		for (col = 0; col <= yord; col++)
			bb.coeff[(row * (bb.yord + 1) + col) * (bb.zord + 1) + stack] = d.coeff[col];
	    }
	/*** Finally polynomial in x ***/

	c = new double[xord + 1];
	for (col = 0; col <= yord; col++) {
	    for (stack = 0; stack <= zord; ++stack) {
		for (row = 0; row <= xord; row++)
		    c[row] = bb.coeff[(row * (bb.yord + 1) + col) * (bb.zord + 1) + stack];
		d = Bern1D.formbernstein1D(c, region.xmin, region.xmax);
		for (row = 0; row <= xord; row++)
			bb.coeff[(row * (bb.yord + 1) + col) * (bb.zord + 1) + stack] = d.coeff[row];
	    }
	}
	return bb;
    }

    
    public Bern3D zeroBern() {
		return ZERO_BERN_3D;
	}


	public double calc_val_actual(double[] rel_pos) {
	    return evalbern3D(BB, rel_pos[0],rel_pos[1],rel_pos[2]);
	}


	public double[] calc_norm_relative(double[] rel_pos) {
		double norm[] = new double[] {
		    evalbern3D(Dx, rel_pos[0],rel_pos[1],rel_pos[2]),
		    evalbern3D(Dy, rel_pos[0],rel_pos[1],rel_pos[2]),
		    evalbern3D(Dz, rel_pos[0],rel_pos[1],rel_pos[2])};
		return norm;
	}

	public double[] calc_second_derivs_relative(double[] relativePos) {
		return new double[] {
			evalbern3D(Dxx, relativePos[0],relativePos[1],relativePos[2]),
			evalbern3D(Dxy, relativePos[0],relativePos[1],relativePos[2]),
			evalbern3D(Dxz, relativePos[0],relativePos[1],relativePos[2]),
			evalbern3D(Dyy, relativePos[0],relativePos[1],relativePos[2]),
			evalbern3D(Dyz, relativePos[0],relativePos[1],relativePos[2]),
			evalbern3D(Dzz, relativePos[0],relativePos[1],relativePos[2])
		};
	}


	public OctBern reduce(Bern3D bern3d) {
	OctBern temp = this.new OctBern(bern3d.xord, bern3d.yord, bern3d.zord);
	
	for (int row = 0; row <= bern3d.xord; row++)
	    for (int col = 0; col <= bern3d.yord; col++)
		for (int stack = 0; stack <= bern3d.zord; stack++)
		    pyramid[2 * row][2 * col][2 * stack] = bern3d.coeff[(row * (bern3d.yord + 1) + col) * (bern3d.zord + 1) + stack];
	
	for (int level = 1; level <= bern3d.xord; level++)
	    for (int row = level; row <= 2 * bern3d.xord - level; row += 2)
		for (int col = 0; col <= 2 * bern3d.yord; col += 2)
		    for (int stack = 0; stack <= 2 * bern3d.zord; stack += 2)
			pyramid[row][col][stack] = 0.5 * (pyramid[row - 1][col][stack] + pyramid[row + 1][col][stack]);
	
	for (int level = 1; level <= bern3d.yord; level++)
	    for (int row = 0; row <= 2 * bern3d.xord; ++row)
		for (int col = level; col <= 2 * bern3d.yord - level; col += 2)
		    for (int stack = 0; stack <= 2 * bern3d.zord; stack += 2)
			pyramid[row][col][stack] = 0.5 * (pyramid[row][col - 1][stack] + pyramid[row][col + 1][stack]);
	
	for (int level = 1; level <= bern3d.zord; level++)
	    for (int row = 0; row <= 2 * bern3d.xord; ++row)
		for (int col = 0; col <= 2 * bern3d.yord; ++col)
		    for (int stack = level; stack <= 2 * bern3d.zord - level; stack += 2)
			pyramid[row][col][stack] = 0.5 * (pyramid[row][col][stack - 1] + pyramid[row][col][stack + 1]);
	
	for (int row = 0; row <= bern3d.xord; row++)
	    for (int col = 0; col <= bern3d.yord; col++)
		for (int stack = 0; stack <= bern3d.zord; stack++) {
		    temp.lfd.coeff[(row * (temp.lfd.yord + 1) + col) * (temp.lfd.zord + 1) + stack] = pyramid[row][col][stack];
		    temp.rfd.coeff[(row * (temp.rfd.yord + 1) + col) * (temp.rfd.zord + 1) + stack] = pyramid[row + bern3d.xord][col][stack];
		    temp.lbd.coeff[(row * (temp.lbd.yord + 1) + col) * (temp.lbd.zord + 1) + stack] = pyramid[row][col + bern3d.yord][stack];
		    temp.rbd.coeff[(row * (temp.rbd.yord + 1) + col) * (temp.rbd.zord + 1) + stack] = pyramid[row + bern3d.xord][col + bern3d.yord][stack];
		    temp.lfu.coeff[(row * (temp.lfu.yord + 1) + col) * (temp.lfu.zord + 1) + stack] = pyramid[row][col][stack + bern3d.zord];
		    temp.rfu.coeff[(row * (temp.rfu.yord + 1) + col) * (temp.rfu.zord + 1) + stack] = pyramid[row + bern3d.xord][col][stack + bern3d.zord];
		    temp.lbu.coeff[(row * (temp.lbu.yord + 1) + col) * (temp.lbu.zord + 1) + stack] = pyramid[row][col + bern3d.yord][stack + bern3d.zord];
		    temp.rbu.coeff[(row * (temp.rbu.yord + 1) + col) * (temp.rbu.zord + 1) + stack] = pyramid[row + bern3d.xord][col + bern3d.yord][stack + bern3d.zord];
		}
	return temp;
	}


	public final double evalbern3D(Bern3D bern, double x, double y, double z) {
	double oneminusroot, root;
	
	for (int i = 0; i <= bern.xord; ++i) {
	    root = z;
	    oneminusroot = 1.0 - root;
	
	    for (int j = 0; j <= bern.yord; ++j) {
		for (int element = 0; element <= bern.zord; element++)
		    workC[2 * element] = bern.coeff[(i * (bern.yord + 1) + j) * (bern.zord + 1) + element];
	
		for (int level = 1; level <= bern.zord; level++)
		    for (int element = level; element <= 2 * bern.zord - level; element += 2)
			workC[element] = oneminusroot * workC[element - 1] + root * workC[element + 1];
	
		workB[j * 2] = workC[bern.zord];
	    }
	
	    root = y;
	    oneminusroot = 1.0 - root;
	
	    for (int level = 1; level <= bern.yord; level++)
		for (int element = level; element <= 2 * bern.yord - level; element += 2)
		    workB[element] = oneminusroot * workB[element - 1] + root * workB[element + 1];
	
	    workA[i * 2] = workB[bern.yord];
	}
	root = x;
	oneminusroot = 1.0 - root;
	
	for (int level = 1; level <= bern.xord; level++)
	    for (int element = level; element <= 2 * bern.xord - level; element += 2)
		workA[element] = oneminusroot * workA[element - 1] + root * workA[element + 1];
	
	return (workA[bern.xord]);
	}


	public class OctBern {
	public Bern3D lfd;
	public Bern3D lfu;
	public Bern3D lbd;
	public Bern3D lbu;
	public Bern3D rfd;
	public Bern3D rfu;
	public Bern3D rbd;  
	public Bern3D rbu;
	    int xord;
	    int yord;
	    int zord;


	public OctBern(int xord, int yord, int zord) {
		this.xord = xord;
		this.yord = yord;
		this.zord = zord;
	    lfd = new Bern3D(xord, yord, zord);
	    lfu = new Bern3D(xord, yord, zord);
	    lbd = new Bern3D(xord, yord, zord);
	    lbu = new Bern3D(xord, yord, zord);
	    rfd = new Bern3D(xord, yord, zord);
	    rfu = new Bern3D(xord, yord, zord);
	    rbd = new Bern3D(xord, yord, zord);
	    rbu = new Bern3D(xord, yord, zord);
	}

	public void free() {
	    lfu = null;
	    lbd = null;
	    lbu = null;
	    rfd = null;
	    rfu = null;
	    rbd = null;
	    rbu = null;
	}
}


}

