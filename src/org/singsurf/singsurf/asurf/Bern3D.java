package org.singsurf.singsurf.asurf;

import java.util.Arrays;

import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern1D;
import org.singsurf.singsurf.acurve.Bern2D;

public class Bern3D {
    /**
		 * 
		 */
//		final Bern3DContext bern3dContext;
	public static final short BERN_NO_SIGN = -2;
    public final int xord;
    public final int yord;
    public final int zord;
    public double[] coeff;
    public short sign = BERN_NO_SIGN;

    public Bern3D(int xord, int yord, int zord) {
	this.xord = xord;
	this.yord = yord;
	this.zord = zord;
	this.coeff = new double[(xord + 1) * (yord + 1) * (zord + 1)];
    }

    public Bern3D(Bern3D b) {
		this.xord = b.xord;
    	this.yord = b.yord;
    	this.zord = b.zord;
    	this.coeff = Arrays.copyOf(b.coeff, b.coeff.length);
	}

	public final void setCoeff(int i, int j, int k, double val) {
    	int pos = (i * (yord + 1) + j) * (zord + 1) + k;
    	coeff[pos] = val;
    }

	public final double getCoeff(int i, int j, int k) {
    	int pos = (i * (yord + 1) + j) * (zord + 1) + k;
    	return coeff[pos];
    }
	
    /**
     * Test if all coefficient are strictly the same sign.
     * 
     * @return tree if all positive or all negative false otherwise
     */

    public boolean allOneSign() {
	int i, j, k;

	if (sign != BERN_NO_SIGN)
	    return sign != 0;

	if (coeff[0] < 0) {
	    for (i = 0; i <= xord; i++)
		for (j = 0; j <= yord; j++)
		    for (k = 0; k <= zord; k++)
			if (coeff[(i * (yord + 1) + j) * (zord + 1) + k] >= 0.0) {
			    sign = 0;
			    return (false);
			}
	    sign = -1;
	    return (true);
	} else {
	    for (i = 0; i <= xord; i++)
		for (j = 0; j <= yord; j++)
		    for (k = 0; k <= zord; k++)
			if (coeff[(i * (yord + 1) + j) * (zord + 1) + k] <= 0.0) {
			    sign = 0;
			    return (false);
			}
	    sign = 1;
	    return (true);
	}
    }

    public short signOf() {
	int i, j, k;

	if (sign != BERN_NO_SIGN)
	    return sign;

	if (coeff[0] < 0) {
	    for (i = 0; i <= xord; i++)
		for (j = 0; j <= yord; j++)
		    for (k = 0; k <= zord; k++)
			if (coeff[(i * (yord + 1) + j) * (zord + 1) + k] >= 0.0) {
			    sign = 0;
			    return (sign);
			}
	    sign = -1;
	    return sign;
	} else {
	    for (i = 0; i <= xord; i++)
		for (j = 0; j <= yord; j++)
		    for (k = 0; k <= zord; k++)
			if (coeff[(i * (yord + 1) + j) * (zord + 1) + k] <= 0.0) {
			    sign = 0;
			    return (sign);
			}
	    sign = 1;
	    return (sign);
	}
    }

    public Bern3D diffX() {
	int row, col, stack;
	Bern3D xderiv;

	if (xord == 0) {
	    return Bern3DContext.ZERO_BERN_3D;
	}
	xderiv = new Bern3D(this.xord - 1, this.yord, this.zord);
	for (row = 0; row <= xord - 1; row++)
	    for (col = 0; col <= yord; col++)
		for (stack = 0; stack <= zord; stack++)
			xderiv.coeff[(row * (xderiv.yord + 1) + col) * (xderiv.zord + 1) + stack] = xord * (coeff[((row + 1) * (yord + 1) + col) * (zord + 1) + stack] - coeff[(row * (yord + 1) + col) * (zord + 1) + stack]);
	return (xderiv);
    }

    public Bern3D diffY() {
	int row, col, stack;
	Bern3D yderiv;

	if (yord == 0) {
	    return Bern3DContext.ZERO_BERN_3D;
	}
	yderiv = new Bern3D(this.xord, this.yord - 1, this.zord);
	for (row = 0; row <= xord; row++)
	    for (col = 0; col <= yord - 1; col++)
		for (stack = 0; stack <= zord; stack++)
			yderiv.coeff[(row * (yderiv.yord + 1) + col) * (yderiv.zord + 1) + stack] = yord * (coeff[(row * (yord + 1) + col + 1) * (zord + 1) + stack] - coeff[(row * (yord + 1) + col) * (zord + 1) + stack]);
	return (yderiv);
    }

    public Bern3D diffZ() {
	int row, col, stack;
	Bern3D zderiv;

	if (zord == 0) {
	    return Bern3DContext.ZERO_BERN_3D;
	}
	zderiv = new Bern3D(this.xord, this.yord, this.zord - 1);
	for (row = 0; row <= xord; row++)
	    for (col = 0; col <= yord; col++)
		for (stack = 0; stack <= zord - 1; stack++)
			zderiv.coeff[(row * (zderiv.yord + 1) + col) * (zderiv.zord + 1) + stack] = zord * (coeff[(row * (yord + 1) + col) * (zord + 1) + stack + 1] - coeff[(row * (yord + 1) + col) * (zord + 1) + stack]);
	return (zderiv);
    }
    



    
    
//	public final double evalbern3D(double[] vec) {
//		return evalbern3D(vec[0],vec[1],vec[2]);
//	}
//
//	public final double evalbern3D(Vec3D vec) {
//		return evalbern3D(vec.x,vec.y,vec.z);
//	}

    public final double threadSafeEvalbern3D(double x,double y,double z) {
	double oneminusroot, root;
	double wrkA[] = new double[xord*2+1];
	double wrkB[] = new double[yord*2+1];
	double wrkC[] = new double[zord*2+1];

	for (int i = 0; i <= xord; ++i) {
	    root = z;
	    oneminusroot = 1.0 - root;

	    for (int j = 0; j <= yord; ++j) {
		for (int element = 0; element <= zord; element++)
		    wrkC[2 * element] = coeff[(i * (yord + 1) + j) * (zord + 1) + element];

		for (int level = 1; level <= zord; level++)
		    for (int element = level; element <= 2 * zord - level; element += 2)
			wrkC[element] = oneminusroot * wrkC[element - 1] + root * wrkC[element + 1];

		wrkB[j * 2] = wrkC[zord];
	    }

	    root = y;
	    oneminusroot = 1.0 - root;

	    for (int level = 1; level <= yord; level++)
		for (int element = level; element <= 2 * yord - level; element += 2)
		    wrkB[element] = oneminusroot * wrkB[element - 1] + root * wrkB[element + 1];

	    wrkA[i * 2] = wrkB[yord];
	}
	root = x;
	oneminusroot = 1.0 - root;

	for (int level = 1; level <= xord; level++)
	    for (int element = level; element <= 2 * xord - level; element += 2)
		wrkA[element] = oneminusroot * wrkA[element - 1] + root * wrkA[element + 1];

	return (wrkA[xord]);
    }


    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("3D Bernstein (" + xord + "," + yord + "," + zord + ")\n");
	for (int k = 0; k <= zord; ++k) {
	    for (int j = 0; j <= yord; ++j) {
		sb.append("[");
		sb.append(coeff[(0 * (yord + 1) + j) * (zord + 1) + k]);
		for (int i = 1; i < xord + 1; ++i)
		    sb.append("," + coeff[(i * (yord + 1) + j) * (zord + 1) + k]);
		sb.append("]\n");
	    }
	    sb.append("\n");
	}
	return sb.toString();
    }

    public Bern2D make_bern2D_of_box(Key3D code) {

	int i, j;
	Bern2D aa = null;

	// if( bb == posbern3D ) return(posbern2D);
	// if( bb == negbern3D ) return(negbern2D);

	switch (code) {
	case FACE_LL:
	    aa = new Bern2D(this.yord, this.zord);
	    for (i = 0; i <= this.yord; ++i)
		for (j = 0; j <= this.zord; ++j)
		    aa.setCoeff(i, j, this.coeff[(0 * (this.yord + 1) + i) * (this.zord + 1) + j]);
	    break;
	case FACE_RR:
	    aa = new Bern2D(this.yord, this.zord);
	    for (i = 0; i <= this.yord; ++i)
		for (j = 0; j <= this.zord; ++j)
		    aa.setCoeff(i, j, this.coeff[(this.xord * (this.yord + 1) + i) * (this.zord + 1) + j]);
	    break;
	case FACE_FF:
	    aa = new Bern2D(this.xord, this.zord);
	    for (i = 0; i <= this.xord; ++i)
		for (j = 0; j <= this.zord; ++j)
		    aa.setCoeff(i, j, this.coeff[(i * (this.yord + 1) + 0) * (this.zord + 1) + j]);
	    break;
	case FACE_BB:
	    aa = new Bern2D(this.xord, this.zord);
	    for (i = 0; i <= this.xord; ++i)
		for (j = 0; j <= this.zord; ++j)
		    aa.setCoeff(i, j, this.coeff[(i * (this.yord + 1) + this.yord) * (this.zord + 1) + j]);
	    break;
	case FACE_DD:
	    aa = new Bern2D(this.xord, this.yord);
	    for (i = 0; i <= this.xord; ++i)
		for (j = 0; j <= this.yord; ++j)
		    aa.setCoeff(i, j, this.coeff[(i * (this.yord + 1) + j) * (this.zord + 1) + 0]);
	    break;
	case FACE_UU:
	    aa = new Bern2D(this.xord, this.yord);
	    for (i = 0; i <= this.xord; ++i)
		for (j = 0; j <= this.yord; ++j)
		    aa.setCoeff(i, j, this.coeff[(i * (this.yord + 1) + j) * (this.zord + 1) + this.zord]);
	    break;
	default:
	    BoxClevA.log.printf("bad type %d in make_bern2d_of_box\n", code);
	    throw new IllegalArgumentException("Make_bern2D_of_box" +code.toString());
	}
	return (aa);
    }

	/**
	 * Add two Bern3D's.
	 * First elevates each so they are of the same degrees.
	 * @param u
	 * @param v
	 * @return Bernstein representing u+v
	 * @throws AsurfException hopefully never
	 */
	public static Bern3D addBern3D(Bern3D u, Bern3D v) throws AsurfException {
		if(u.xord < v.xord) {
			u = u.elevateX(v.xord);
		} else if(u.xord > v.xord) {
			v = v.elevateX(u.xord);
		}
		if(u.yord < v.yord) {
			u = u.elevateY(v.yord);
		} else if(u.yord > v.yord) {
			v = v.elevateY(u.yord);
		}
		if(u.zord < v.zord) {
			u = u.elevateZ(v.zord);
		} else if(u.zord > v.zord) {
			v = v.elevateZ(u.zord);
		}

		Bern3D res = new Bern3D(u);
		for(int i=0;i<res.coeff.length;++i) {
			res.coeff[i] += v.coeff[i];
		}
		return res;
	}

	/**
	 * Subtracts two Bern3D's.
	 * First elevates each so they are of the same degrees.
	 * @param u
	 * @param v
	 * @return Bernstein representing u-v
	 * @throws AsurfException hopefully never
	 */
	public static Bern3D subtractBern3D(Bern3D u, Bern3D v) throws AsurfException {
		if(u.xord < v.xord) {
			u = u.elevateX(v.xord);
		} else if(u.xord > v.xord) {
			v = v.elevateX(u.xord);
		}
		if(u.yord < v.yord) {
			u = u.elevateY(v.yord);
		} else if(u.yord > v.yord) {
			v = v.elevateY(u.yord);
		}
		if(u.zord < v.zord) {
			u = u.elevateZ(v.zord);
		} else if(u.zord > v.zord) {
			v = v.elevateZ(u.zord);
		}

		Bern3D res = new Bern3D(u);
		for(int i=0;i<res.coeff.length;++i) {
			res.coeff[i] -= v.coeff[i];
		}
		return res;
	}
	

	/**
	 * 
	 * @param degx
	 * @return
	 * @throws AsurfException
	 */
	public Bern3D elevateX(int degx) throws AsurfException {
		if(degx==xord) return this;
		Bern3D res = new Bern3D(degx,yord,zord);
		for(int j=0;j<=yord;++j) {
			for(int k=0;k<=zord;++k) {
				Bern1D orig = new Bern1D(xord);
				for(int i=0; i<=xord;++i) {
					orig.coeff[i] = getCoeff(i,j,k);
				}
				Bern1D raised = orig.elevateTo(degx);
				for(int i=0; i<=degx;++i) {
					res.setCoeff(i, j, k, raised.coeff[i]);
				}
				
			}
		}
		return res;
	}

	public Bern3D elevateY(int degy) throws AsurfException {
		if(degy==yord) return this;
		Bern3D res = new Bern3D(xord,degy,zord);
		for(int i=0; i<=xord;++i) {
			for(int k=0;k<=zord;++k) {
				Bern1D orig = new Bern1D(yord);
				for(int j=0;j<=yord;++j) {
					orig.coeff[j] = getCoeff(i,j,k);
				}
				Bern1D raised = orig.elevateTo(degy);
				for(int j=0; j<=degy;++j) {
					res.setCoeff(i, j, k, raised.coeff[j]);
				}
				
			}
		}
		return res;
	}

	public Bern3D elevateZ(int degz) throws AsurfException {
		if(degz==zord) return this;
		Bern3D res = new Bern3D(xord,yord,degz);
		for(int i=0; i<=xord;++i) {
			for(int j=0;j<=yord;++j) {
				Bern1D orig = new Bern1D(zord);
				for(int k=0;k<=zord;++k) {
					orig.coeff[k] = getCoeff(i,j,k);
				}
				Bern1D raised = orig.elevateTo(degz);
				for(int k=0; k<=degz;++k) {
					res.setCoeff(i, j, k, raised.coeff[k]);
				}			
			}
		}
		return res;
	}

	//             u
	//         ----------
	//         |\        |\ 
	//         | \  b    | \
	//         |  \      |  \
	//  l      |   -----------    r
	//         |   |     |   |
	//         |   |     |   | 
	//         ----|-----\   |
	//          \  |      \  |
	//           \ |       \ |
	//            \|     f  \|
	//             0----------
	//                 d

	public Bern1D make_bern1D_of_box(Key3D key) {
		switch(key) {
		case EDGE_FD: {
			Bern1D resX = new Bern1D(xord);
			for(int x=0;x<=xord;++x) {
				resX.coeff[x] = getCoeff(x,0,0);
			}
			return resX;
		}
		case EDGE_FU: {
			Bern1D resX = new Bern1D(xord);
			for(int x=0;x<=xord;++x) {
				resX.coeff[x] = getCoeff(x,0,zord);
			}
			return resX;
		}
		case EDGE_BD: {
			Bern1D resX = new Bern1D(xord);
			for(int x=0;x<=xord;++x) {
				resX.coeff[x] = getCoeff(x,yord,0);
			}
			return resX;
		}
		case EDGE_BU: {
			Bern1D resX = new Bern1D(xord);
			for(int x=0;x<=xord;++x) {
				resX.coeff[x] = getCoeff(x,yord,zord);
			}
			return resX;
		}
		
		
		
		case EDGE_LD: {
			Bern1D resY = new Bern1D(yord);
			for(int y=0;y<=yord;++y) {
				resY.coeff[y] = getCoeff(0,y,0);
			}
			return resY;
		}
		case EDGE_LU: {
			Bern1D resY = new Bern1D(yord);
			for(int y=0;y<=yord;++y) {
				resY.coeff[y] = getCoeff(0,y,zord);
			}
			return resY;
		}
		case EDGE_RD: {
			Bern1D resY = new Bern1D(yord);
			for(int y=0;y<=yord;++y) {
				resY.coeff[y] = getCoeff(xord,y,0);
			}
			return resY;
		}
		case EDGE_RU: {
			Bern1D resY = new Bern1D(yord);
			for(int y=0;y<=yord;++y) {
				resY.coeff[y] = getCoeff(xord,y,zord);
			}
			return resY;
		}
		case EDGE_LF: {
			Bern1D resZ = new Bern1D(zord);
			for(int z=0;z<=zord;++z) {
				resZ.coeff[z] = getCoeff(0,0,z);
			}
			return resZ;
		}
		case EDGE_LB: {
			Bern1D resZ = new Bern1D(zord);
			for(int z=0;z<=zord;++z) {
				resZ.coeff[z] = getCoeff(0,yord,z);
			}
			return resZ;
		}
		case EDGE_RF: {
			Bern1D resZ = new Bern1D(zord);
			for(int z=0;z<=zord;++z) {
				resZ.coeff[z] = getCoeff(xord,0,z);
			}
			return resZ;
		}
		case EDGE_RB: {
			Bern1D resZ = new Bern1D(zord);
			for(int z=0;z<=zord;++z) {
				resZ.coeff[z] = getCoeff(xord,yord,z);
			}
			return resZ;
		}
		
		default:
		    BoxClevA.log.printf("bad type %d in make_bern1d_of_box\n", key);
		    throw new IllegalArgumentException("Make_bern2D_of_box" +key.toString());
		}
	}


}