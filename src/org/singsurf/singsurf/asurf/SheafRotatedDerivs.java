package org.singsurf.singsurf.asurf;

import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern2D;
import org.singsurf.singsurf.acurve.Bern2D.QuadBern;

public class SheafRotatedDerivs extends Sheaf2D {
		Bern2D xpy;
		Bern2D xmy;
		Bern2D xpz;
		Bern2D xmz;
		Bern2D ypz;
		Bern2D ymz;
		
		public SheafRotatedDerivs(Bern2D aa, Bern2D dx, Bern2D dy, Bern2D dz, 
				Bern2D xpy, Bern2D xmy, Bern2D xpz, Bern2D xmz,Bern2D ypz, Bern2D ymz) {
			super(aa,dx,dy,dz);
			this.xpy = xpy;
			this.xmy = xmy;
			this.xpz = xpz;
			this.xmz = xmz;
			this.ypz = ypz;
			this.ymz = ymz;
		}

		public SheafRotatedDerivs(Bern2D aa, Bern2D dx, Bern2D dy, Bern2D dz) {
			super(aa,dx,dy,dz);
			this.xpy = Bern2D.addBern2D(dx, dy);
			this.xmy = Bern2D.subtractBern2D(dx, dy);
			this.xpz = Bern2D.addBern2D(dx, dz);
			this.xmz = Bern2D.subtractBern2D(dx, dz);
			this.ypz = Bern2D.addBern2D(dy, dz);
			this.ymz = Bern2D.subtractBern2D(dy, dz);
		}

		public QuadSheaf reduce(Face_info face, int f1, int f2, int f3) throws AsurfException {
			QuadSheaf qs = super.reduce(face, f1, f2, f3);

			QuadBern xpy2 = xpy.reduce();
			QuadBern xmy2 = xmy.reduce();
			QuadBern xpz2 = xpz.reduce();
			QuadBern xmz2 = xmz.reduce();
			QuadBern ypz2 = ypz.reduce();
			QuadBern ymz2 = ymz.reduce();

			((SheafRotatedDerivs) qs.lb).xpy = xpy2.lb;
			((SheafRotatedDerivs) qs.lb).xmy = xmy2.lb;
			((SheafRotatedDerivs) qs.lb).xpz = xpz2.lb;
			((SheafRotatedDerivs) qs.lb).xmz = xmz2.lb;
			((SheafRotatedDerivs) qs.lb).ypz = ypz2.lb;
			((SheafRotatedDerivs) qs.lb).ymz = ymz2.lb;

			((SheafRotatedDerivs) qs.lt).xpy = xpy2.lt;
			((SheafRotatedDerivs) qs.lt).xmy = xmy2.lt;
			((SheafRotatedDerivs) qs.lt).xpz = xpz2.lt;
			((SheafRotatedDerivs) qs.lt).xmz = xmz2.lt;
			((SheafRotatedDerivs) qs.lt).ypz = ypz2.lt;
			((SheafRotatedDerivs) qs.lt).ymz = ymz2.lt;

			((SheafRotatedDerivs) qs.rb).xpy = xpy2.rb;
			((SheafRotatedDerivs) qs.rb).xmy = xmy2.rb;
			((SheafRotatedDerivs) qs.rb).xpz = xpz2.rb;
			((SheafRotatedDerivs) qs.rb).xmz = xmz2.rb;
			((SheafRotatedDerivs) qs.rb).ypz = ypz2.rb;
			((SheafRotatedDerivs) qs.rb).ymz = ymz2.rb;

			((SheafRotatedDerivs) qs.rt).xpy = xpy2.rt;
			((SheafRotatedDerivs) qs.rt).xmy = xmy2.rt;
			((SheafRotatedDerivs) qs.rt).xpz = xpz2.rt;
			((SheafRotatedDerivs) qs.rt).xmz = xmz2.rt;
			((SheafRotatedDerivs) qs.rt).ypz = ypz2.rt;
			((SheafRotatedDerivs) qs.rt).ymz = ymz2.rt;
			
			return qs;
		}

		@Override
		Sheaf2D makeSheaf(Bern2D a, Bern2D dx, Bern2D dy, Bern2D dz) {
			SheafRotatedDerivs res = new SheafRotatedDerivs(a,dx,dy,dz,null,null,null,null,null,null);
			return res;
		}
		
		
}