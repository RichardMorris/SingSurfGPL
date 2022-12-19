package org.singsurf.singsurf.asurf;

import static org.singsurf.singsurf.asurf.Key3D.FACE_BB;
import static org.singsurf.singsurf.asurf.Key3D.FACE_DD;
import static org.singsurf.singsurf.asurf.Key3D.FACE_FF;
import static org.singsurf.singsurf.asurf.Key3D.FACE_LL;
import static org.singsurf.singsurf.asurf.Key3D.FACE_RR;
import static org.singsurf.singsurf.asurf.Key3D.FACE_UU;

import org.singsurf.singsurf.acurve.AsurfException; 
import org.singsurf.singsurf.acurve.Bern2D;

/**
 * Contains Bern2D's for all necessary derivatives.
 * Base class just have base polynomial and its first derivatives.
 */
public class Sheaf2D {
		Bern2D aa,  dx,  dy,  dz;
		
		public static class QuadSheaf {
			Sheaf2D lb;
			Sheaf2D rb;
			Sheaf2D lt;
			Sheaf2D rt;
		}

		public Sheaf2D(Bern2D aa, Bern2D dx, Bern2D dy, Bern2D dz) {
			this.aa = aa;
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
		}

		public Sheaf2D.QuadSheaf reduce(Face_info face, int f1, int f2, int f3) throws AsurfException {
			Bern2D.QuadBern b1;
			Bern2D.QuadBern dx1;
			Bern2D.QuadBern dy1;
			Bern2D.QuadBern dz1;
			
			b1 = aa.reduce();
			if (f1 > 0)
				dx1 = Bern2D.posBern2D.reduce();
			else if (f1 < 0)
				dx1 = Bern2D.negBern2D.reduce();
			else if (face.type == FACE_LL || face.type == FACE_RR)
				dx1 = dx.reduce();
			else
				dx1 = b1.quadDiff2Dx();

			if (f2 > 0)
				dy1 = Bern2D.posBern2D.reduce();
			else if (f2 < 0)
				dy1 = Bern2D.negBern2D.reduce();
			else if (face.type == FACE_FF || face.type == FACE_BB)
				dy1 = dy.reduce();
			else if (face.type == FACE_LL || face.type == FACE_RR)
				dy1 = b1.quadDiff2Dx();
			else
				dy1 = b1.quadDiff2Dy();

			if (f3 > 0)
				dz1 = Bern2D.posBern2D.reduce();
			else if (f3 < 0)
				dz1 = Bern2D.negBern2D.reduce();
			else if (face.type == FACE_UU || face.type == FACE_DD)
				dz1 = dz.reduce();
			else
				dz1 = b1.quadDiff2Dy();
		
			Sheaf2D.QuadSheaf qs = new QuadSheaf();
			qs.lb = makeSheaf(b1.lb,dx1.lb,dy1.lb,dz1.lb);
			qs.lt = makeSheaf(b1.lt,dx1.lt,dy1.lt,dz1.lt);
			qs.rb = makeSheaf(b1.rb,dx1.rb,dy1.rb,dz1.rb);
			qs.rt = makeSheaf(b1.rt,dx1.rt,dy1.rt,dz1.rt);
			return qs;
		}
		
		Sheaf2D makeSheaf(Bern2D a,Bern2D dx,Bern2D dy,Bern2D dz) {
			return new Sheaf2D(a,dx,dy,dz);
		}
}