package org.singsurf.singsurf.asurf;

import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern2D;

public class SheafSecondDeriv extends Sheaf2D {
		Bern2D d2;
		
		public SheafSecondDeriv(Bern2D aa, Bern2D dx, Bern2D dy, Bern2D dz, Bern2D d2) {
			super(aa,dx,dy,dz);
			this.d2 = d2;
		}

		public QuadSheaf reduce(Face_info face, int f1, int f2, int f3) throws AsurfException {
			QuadSheaf qs = super.reduce(face, f1, f2, f3);
			Bern2D.QuadBern dd2 = null;

			
			if (Boxclev.USE_2ND_DERIV) {
				dd2 = d2.reduce();
			}
		
			((SheafSecondDeriv) qs.lb).d2 = dd2==null?null:dd2.lb;
			((SheafSecondDeriv) qs.lt).d2 = dd2==null?null:dd2.lt;
			((SheafSecondDeriv) qs.rb).d2 = dd2==null?null:dd2.rb;
			((SheafSecondDeriv) qs.rt).d2 = dd2==null?null:dd2.rt;

			return qs;
		}

		@Override
		Sheaf2D makeSheaf(Bern2D a, Bern2D dx, Bern2D dy, Bern2D dz) {
			SheafSecondDeriv res = new SheafSecondDeriv(a,dx,dy,dz,null);
			return res;
		}
		
		
}