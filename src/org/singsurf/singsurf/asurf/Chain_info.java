/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.List;

public class Chain_info {
    public boolean used;             /* whether this chain has already been used to split a facet */
    public List<Sol_info> sols;        /* an array of sols */
    public double metric_length;    /* the length of the chain */
    public double[] metLens;
    //Chain_info next;        /* pointer to the next chain in the list */
    
    
	public Chain_info() {
		sols = new ArrayList<Sol_info>();
	}

	@Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Chain: used %b%n",used));
		for(Sol_info sol:sols)	{
			sb.append(sol);
			sb.append('\n');
		}
		return sb.toString();
	}
    
//	public String printList() {
//    	StringBuilder sb = new StringBuilder();
//    	Chain_info s=this;
//    	while(s!=null)
//    	{
//    		sb.append(s.toString());
//    		s = s.next;
//    	}
//    	return sb.toString();
//
//	}

	public Sol_info getSol(int i) {
		return sols.get(i);
	}

	public int length() {
		return sols.size();
	}

	public void addSol(Sol_info sol) {
			
			sols.add(sol);
	}

	public void free() {
		this.sols=null;
		this.metLens=null;
		
	}

	public boolean has_coherent_signs() {
		int sigx = 0, sigy = 0, sigz = 0;

		for(Sol_info sol:sols) {
			if(sigx * sol.dx < 0) return false;
			if(sigy * sol.dy < 0) return false;
			if(sigz * sol.dz < 0) return false;
			if(sigx ==0) sigx = sol.dx;
			if(sigy ==0) sigy = sol.dy;
			if(sigz ==0) sigz = sol.dz;
		}
		return true;	
	}


}
