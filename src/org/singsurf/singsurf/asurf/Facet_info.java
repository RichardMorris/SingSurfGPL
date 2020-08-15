/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

import java.util.Iterator;

public class Facet_info {
    public Facet_sol sols=null;
    public int dx=0,dy=0,dz=0;
    
    //Facet_info next;
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FACET: "+dx+" "+dy+" "+dz+"\n");
		Facet_sol s1 = this.sols;
		while(s1!=null) {
			sb.append(s1.sol);
			sb.append('\n');
			s1 = s1.next;
		}
		return sb.toString();
	}
    
	public boolean matches_derivs(Sol_info sol) {
		if(dx * sol.dx < 0) return false;
		if(dy * sol.dy < 0) return false;
		if(dz * sol.dz < 0) return false;
		return true;
	}
	
	
    public CyclicList<Sol_info> getSols() {
        CyclicList<Sol_info> res = new CyclicList<>();
        Facet_sol s1 = this.sols;
        while(s1!=null) {
            res.add(s1.sol);
            s1 = s1.next;
        }
        return res;
    }
    
    class SolIterator implements Iterator<Sol_info> {
        Facet_sol s1 = sols;

		@Override
		public boolean hasNext() {
			return s1!=null;
		}

		@Override
		public Sol_info next() {
			Facet_sol res = s1;
            s1 = s1.next;
            return res.sol;
		}
    	
    }
    
    class SolIterable implements Iterable<Sol_info> {

		@Override
		public Iterator<Sol_info> iterator() {
			return new SolIterator();
		}
    	
    }
    public Iterable<Sol_info> solsItt() {
    	return new SolIterable();
    }
    
    public int size() {
    	int res=0;
        Facet_sol s1 = this.sols;
        while(s1!=null) {
            ++res;
            s1 = s1.next;
        }
        return res;
    }

	public void addSol(Sol_info a) {					
        Facet_sol fs = new Facet_sol();
        fs.sol = a;
        fs.next = null;
		fs.next = this.sols;
		this.sols = fs;
		calc_sig();		
	}

	private void calc_sig() {
		boolean x_pos=false, x_neg=false;
		boolean y_pos=false, y_neg=false;
		boolean z_pos=false, z_neg=false;
		
		Facet_sol s1 = this.sols;
		while(s1!=null) {
			if(s1.sol.dx>0) x_pos = true;
			if(s1.sol.dx<0) x_neg = true;
			if(s1.sol.dy>0) y_pos = true;
			if(s1.sol.dy<0) y_neg = true;
			if(s1.sol.dz>0) z_pos = true;
			if(s1.sol.dz<0) z_neg = true;
			s1 = s1.next;
		}
		dx = x_pos == x_neg ? 0 : (x_pos ? 1 : -1);
		dy = y_pos == y_neg ? 0 : (y_pos ? 1 : -1);
		dz = z_pos == z_neg ? 0 : (z_pos ? 1 : -1);
	}

	public boolean addSol_no_repeats(Sol_info a) {
		if(this.sol_on_facet(a)) {
			BoxClevA.log.println("repeated sol");
			BoxClevA.log.println(this);
			BoxClevA.log.println(a);
		}
		addSol(a);
		return true;
	}

	public void add_sol_to_facet_backwards(Sol_info s)
	{
		Facet_sol fs,fs1;
		if(this.sol_on_facet(s)) {
			BoxClevA.log.println("repeated sol");
			BoxClevA.log.println(this);
			BoxClevA.log.println(s);
		}

		fs = new Facet_sol();
		fs.sol = s;
		fs.next = null;
		if(this.sols == null)
		{
			this.sols = fs;
			return;
		}
		/* find end of list */
		for(fs1=this.sols;fs1.next!=null;fs1=fs1.next) {}
		fs1.next = fs;
		calc_sig();

	}


	public boolean sol_on_facet(Sol_info sol) {
		Facet_sol s1 = this.sols;
		while(s1!=null) {
			if(sol == s1.sol)
				return true;
			s1 = s1.next;
		}
		return false;
		
	}
	
	public boolean has_repeated_sol() {
		Facet_sol s1 = this.sols;
		while(s1!=null) {
			Facet_sol fs2 = s1.next;
			while(fs2!=null) {
				if(fs2.sol == s1.sol)
					return true;
				fs2 = fs2.next;
			}
			s1 = s1.next;
		}
		return false;
	}

	public boolean has_coherent_signs() {
		int sigx = 0, sigy = 0, sigz = 0;
		
		Facet_sol s1 = this.sols;
		while(s1!=null) {
			if(sigx * s1.sol.dx < 0) return false;
			if(sigy * s1.sol.dy < 0) return false;
			if(sigz * s1.sol.dz < 0) return false;
			if(sigx ==0) sigx = s1.sol.dx;
			if(sigy ==0) sigy = s1.sol.dy;
			if(sigz ==0) sigz = s1.sol.dz;
			s1 = s1.next;
		}
		return true;
	}

	public boolean addMatchingSol(Sol_info a) {
		if(!matches_derivs(a)) {
			BoxClevA.log.println("ERR: non matching sol");
			BoxClevA.log.println(a);
			BoxClevA.log.println(this);
			return false;
		}
		this.addSol(a);
		return true;
	}

	public Sol_info prevSol(Sol_info sol) {
		Facet_sol cur,prev;
		
		prev = null;
		for(cur = this.sols;cur!=null;cur=cur.next)
		{
			if(cur.sol == sol) {
				if(prev != null) return prev.sol;
			}
			prev = cur;
		}
		if(this.sols.sol == sol)
			return prev.sol;
		return null;
	}

	public Sol_info nextSol(Sol_info sol) {
		Facet_sol cur,next;
		
		next = null;
		for(cur = this.sols;cur!=null;cur=cur.next)
		{
			next = cur.next;
			if(cur.sol == sol) {
				if(next != null) {
					return next.sol;
				} else {
					return this.sols.sol;
				}
			}
		}
		return null;
	}

	void remove_sol_from_facet(Facet_sol fs1)
	{
		Facet_sol cur,prev,next;

		prev = null;
		for(cur = this.sols;cur!=null;cur=cur.next)
		{
			next = cur.next;

			if(cur == fs1 )
			{
				if(cur == this.sols) { this.sols = next; }
				else prev.next = next;
				return;
			}
			prev = cur;
		}
	}

	public void remove_sol_from_facet(Sol_info delsol) {
		Facet_sol cur,prev,next;

		prev = null;
		for(cur = this.sols;cur!=null;cur=cur.next)
		{
			next = cur.next;

			if(cur.sol == delsol )
			{
				if(cur == this.sols) { this.sols = next; }
				else prev.next = next;
				return;
			}
			prev = cur;
		}
		
	}

	public Object toStringBrief() {
		StringBuilder sb = new StringBuilder();
		sb.append("FACET: "+dx+" "+dy+" "+dz+" [");
		Facet_sol s1 = this.sols;
		while(s1!=null) {
			sb.append(s1.sol.adjNum);
			sb.append(' ');
			s1 = s1.next;
		}
		sb.append("]\n");
		return sb.toString();
	}

}
