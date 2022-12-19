/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.acurve;

import org.nfunk.jep.function.Binomial;

public class Bern1D {
	public int xord;
	public double[] coeff;
	static final int numberofbitsinmantissaofrealnumber =27;
	public Bern1D(int xord)
	{
		this.xord = xord;
		this.coeff = new double[xord+1];
	}

	/**
	 * Test if all coefficient are strictly the same sign.
	 * @return +1, -1, 0 depending on whether signs are all positive, all negative or mixed
	 */

	public int getSign() {
	   boolean isNegative = (coeff[0] < 0);
	   for(int i=0;i<=xord;++i)
		   if( (coeff[i] == 0.0) ||
			   (coeff[i] < 0) != isNegative) return(0);
	   return(isNegative?-1:1);
	}
	
	/**
	 * @throws AsurfException  
	 */
	public Bern1D diff() throws AsurfException {
		if(this.xord ==0)
			return Bern1D.zeroBern1D;
		Bern1D res = new Bern1D(this.xord-1);
		for(int i=0;i<this.xord;++i)
			res.coeff[i]= this.xord *(this.coeff[i+1]-this.coeff[i]);
		return res;
	}

	public boolean allOneSignDeriv() {
		if(xord==0) return true;
		   boolean sign = (coeff[1]-coeff[0] < 0);
		   for(int i=0;i<xord;++i)
			   if( (coeff[i+1]-coeff[i] == 0.0) ||
				   (coeff[i+1]-coeff[i] < 0) != sign) return(false);
		   return(true);
		}
	
	public double evaluate(double root)
	{
	   double[] work = new double[xord*2+1];
	   double oneminusroot=1.0-root;
	   int element,level;


	   for(element=0;element<=xord;element++)
	      work[2*element] = coeff[element];

	   for(level=1;level<=xord;level++)
	      for(element=level;element<=2*xord-level;element+=2)
	         work[element] = oneminusroot * work[element-1] +
	             root * work[element+1];

	   return(work[xord]);
	}
	
	/**
	 * Finds a solution between l and h
	 * @param l
	 * @param h
	 * @return the position of solution or null if does not exist
	 */
	Double solve(double l,double h)
	{
	   double rootm=0.5; 
	   double valm;
	   double rootl=0.0; 
	   double rooth=1.0;
	   double vall=coeff[0];
	   int i;

	   if(coeff[0] == 0.0)
	   {
	      return(l);
	   }
	   if(coeff[xord] == 0.0) /* This prevents corner points being found twice */
	   {
	      return(h); 
	   }
	   else if(getSign()!=0) return(null);
	   
	     for(i=1;i<=numberofbitsinmantissaofrealnumber;i++)
	      {
	         rootm = (rootl +rooth) * 0.5;
	         valm = evaluate(rootm);
	         if((vall<0) != (valm<0)) rooth = rootm;
	         else
	         {
	            vall = valm; 
	            rootl = rootm;
	         }
	      }
	   return ((1.0-rootm)*l + rootm*h);
	/*
	   return(rooth != 1.0 );  
	*/
	/*****
	*     This was originally included to prevent points at the corners being
	*     found twice. However if the solution is infinitesimally close to the
	*     corner and bb[order] is not 0.0 then the corner point will not be 
	*     selected when an other side is tested so we return TRUE here.
	*****/

	}

	public class BinBern {
		public Bern1D l;
		public Bern1D r;
		public BinBern(int ord) {
			l = new Bern1D(ord);
			r = new Bern1D(ord);
		}
		public BinBern(Bern1D l, Bern1D r) {
			super();
			this.l = l;
			this.r = r;
		}
		
		public BinBern binDiff1D() throws AsurfException
		{
			BinBern temp = new BinBern(l.diff(),r.diff());
			return temp;
		}

		
	}
	public BinBern reduce() {
		BinBern res = new BinBern(this.xord);
		
		   double[] pyramid=new double[xord*2+1];
		   int col;
		   int level;

		   for(col=0;col<=xord;col++)
		         pyramid[2*col] = this.coeff[col];

		   for(level=1;level<=xord;level++)
		      for(col=level;col<= 2*xord -level;col+=2)
		            pyramid[col] = 0.5 * ( pyramid[col-1] +  pyramid[col+1] );

		   for(col=0;col<=xord;col++)
		      {
		         res.l.coeff[col] = pyramid[col];
		         res.r.coeff[col] = pyramid[col+xord];
		      }
		   return res;
	}

	@Override
    public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("1D Bernstein ("+xord+")\n");
			sb.append("[");
			sb.append(coeff[0]);
			for(int i=1;i<xord+1;++i)
				sb.append(","+coeff[i]);
			sb.append("]\n");
		return sb.toString();
	}

	/**************************************************************/
	/*                                                            */
	/*     input 'aa'   an array such that aa(i) is coeff of      */
	/*                  x^i                                       */
	/*                                                            */
	/**************************************************************/

	public static Bern1D formbernstein1D(double aa[],double min,double max)
	{
	    int ord = aa.length-1;
	    Bern1D bb = new Bern1D(ord);
	        double d[][] = new double[ord+2][ord+2];
	        int i,j;

	        /* We think of the polynomial a as                              */
	        /* a0 + ( a1 + ( a2 + a3 x ) x ) x                              */
	        /* And write d01 = a3                                           */
	        /* first we convert ( a2 + a3 x ) to bernstein form to give     */
	        /*      a2 (1-x) + (a2+a3) x =  d11 (1-x) + d12 x               */
	        /* Now we look at ( a1 + ( d11 (1-x) + d12 x ) x) )             */
	        /*  = a1 (1-x)^2 + (d11 + 2 a1)(1-x)x + (d12 - a1) x^2          */
	        /*  = d21 (1-x)^2 + d22 (1-x)x + d23 x^2                        */
	        /*      and so on                                               */
	        /*                                                              */

	      d[0][1] = aa[ord];
	      for(i = 1; i <= ord; i++)
	      {
	         d[i-1][0] = 0;
	         d[i-1][i+1] = 0;

	         for( j = 0 ;j <= i;j++)
	            d[i][j+1] = max*d[i-1][j] + min*d[i-1][j+1] + Binomial.binom(i,j)*aa[ord-i];
	      }
	      for( i = 0 ; i<= ord; i++)
	         bb.coeff[i] = d[ord][i+1] / Binomial.binom(ord,i);
	      
	      return bb;
	}
	
	public static class PosBern1D extends Bern1D {

		public PosBern1D() {
			super(0);
			coeff[0]=1.0;
		}

		@Override
		public int getSign() {
			return 1;
		}

		@Override
		public Bern1D diff() throws AsurfException {
			throw new AsurfException("Tried to differentiate a posative bern");
		}

		@Override
		public BinBern reduce() {
			return new BinBern(posBern1D,posBern1D);
		}

		@Override
		public String toString() {
			return "Positive Bern 1D\n";
		}

		@Override
		public
		double evaluate(double root) {
			System.err.println("Tyied to evaluate a posbern1D");
			return 1.0;
		}
		
		
	}

	public static class NegBern1D extends Bern1D {

		public NegBern1D() {
			super(0);
			coeff[0]=1.0;
		}

		@Override
		public int getSign() {
			return -1;
		}

		@Override
		public BinBern reduce() {
			return new BinBern(negBern1D,negBern1D);
		}

		@Override
		public Bern1D diff() throws AsurfException {
			throw new AsurfException("Tried to differentiate a posative bern");
		}

		@Override
		public String toString() {
			return "Negative Bern 1D\n";
		}

		@Override
		public
		double evaluate(double root) {
			System.err.println("Tyied to evaluate a posbern1D");
			return 1.0;
		}

	}

	public static class ZeroBern1D extends Bern1D {

		public ZeroBern1D() {
			super(0);
			coeff[0]=0.0;
		}

		@Override
		public int getSign() {
			return 0;
		}

		@Override
		public Bern1D diff() throws AsurfException {
			return zeroBern1D;
		}

		@Override
		public BinBern reduce() {
			return new BinBern(zeroBern1D,zeroBern1D);
		}

		@Override
		public String toString() {
			return "Zero Bern 1D\n";
		}

		@Override
		public
		double evaluate(double root) {
			//System.err.println("Tyied to evaluate a posbern1D");
			return 0.0;
		}
		
		
	}

	public static final PosBern1D posBern1D = new PosBern1D();
	public static final NegBern1D negBern1D = new NegBern1D();
	public static final ZeroBern1D zeroBern1D = new ZeroBern1D();
	
	/**
	 * Elevate the polynomial to an equivalent one one degree higher.
	 * If our polynomial is 
	 * {@code b0 (1-x)^n + b1 (1-x)^(n-1) x + ... bn x^n}
	 * then the polynomial 
	 * {@code b0 (1-x)^(n+1) + (b0+b1) (1-x)^n x + ... bn x^n}
	 * evaluates to the same value.
	 * The class represent polynomials of the form 
	 * {@code b0 (1-x)^n + nC1 (1-x)^(n-1) x + ...}
	 * So we need suitable re-scaling of coefficients.
	 * @see org.singsurf.singsurf.test.RaiseBernTest
	 * 
	 * @return a Bernstein polynomial which will evaluate to the same values
	 */
	public Bern1D elevate() {
		double co[] = new double[xord+1];
		for(int i=0;i<=xord;++i) {
			co[i] = this.coeff[i] * Binomial.binom(xord,i);
		}
		Bern1D res = new Bern1D(this.xord+1);
		res.coeff[0] = this.coeff[0];
		res.coeff[xord+1] = this.coeff[xord];
		for(int i=1;i<=this.xord;++i)
			res.coeff[i]= (co[i]+co[i-1])/Binomial.binom(xord+1, i);
		return res;
	}
	
	/**
	 * Elevate a polynomial to an equivalent specified degree
	 * @param deg degree of result
	 * @return polynomial of degree deg, 
	 * @throws AsurfException if the requested degree is lower than this one
	 */
	public Bern1D elevateTo(int deg) throws AsurfException {
		if(deg < xord) throw new AsurfException("raiseTo deg "+deg+" < xord "+xord);
		if(this.xord == deg) return this;
		Bern1D raised = this;
		do {
			raised = raised.elevate();
		} while(raised.xord < deg);
		return raised;
	}
	
}
