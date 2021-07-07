package org.singsurf.singsurf.asurf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.singsurf.singsurf.asurf.Key3D.FACE_LL;
import static org.singsurf.singsurf.asurf.Key3D.FACE_UU;
import static org.singsurf.singsurf.asurf.Key3D.NONE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.asurf.CyclicList.Direction;

public class Facets {

	private static final boolean PRINT_FACET_ERR=true;
	private static final boolean PRINT_COMBINE_FACETS=false;
	private static final boolean PRINT_JOIN_CHAIN_POINT=false;
	private static final boolean PRINT_DRAW_BOX=false;
	private static final boolean PRINT_REFINE=false;
	private static final boolean PRINT_JOIN_FACETS=false;
	private static final boolean PRINT_FOLLOW_CHAIN = false;

	/************************************************************************/
	/*									*/
	/*	some sub programs to plot the boxes.				*/
	/*	The general process is as follows:				*/
	/*	for each box {							*/
	/*	{   for each solution						*/
	/*		if solution already used continue;			*/
	/*		plot first face adjacient to solution, update solution  */
	/*									*/
	/************************************************************************/

	int draw_lines;
//	boolean global_do_refine=true;
	//	boolean global_do_triangulate=true;

	int global_facet_count;
	BoxClevA boxclev;
	Bern3DContext ctx;
	int failCountA,failCountB,failCountC,failCountD,failCountE;
	int failCountF,failCountG,failCountH,failCountI,failCountJ;
	int failCountK,failCountL,failCountM,failCountN,failCountO,failCountP;
	int failCountQ;

	
	public Facets(BoxClevA boxclev) {
		super();
		this.boxclev = boxclev;
		failCountA = failCountB = failCountC = failCountD = failCountE = 1;
		failCountF = failCountG = failCountH = failCountI = failCountJ = 1;
		failCountK = failCountL = failCountM = failCountN = failCountO = 1;
		failCountP = failCountQ =1;
	}

	public void init(Bern3DContext ctx) {
		this.ctx = ctx;
	}

	public void printResults() {
		System.out.printf("Facets FailCounts A %d B %d C %d D %d E %d F %d G %d H %d%n",
				failCountA,failCountB,failCountC,failCountD,failCountE,failCountF,failCountG,failCountH);
		System.out.printf("I %d J %d K %d L %d M %d N %d O %d P %d Q %d%n",
				failCountI,failCountJ,failCountK,failCountL,failCountM,failCountN,
				failCountO,failCountP,failCountQ);
	}

	/**
	It all works as follows:

	A facet consists of an ordered set of solutions (facet_sols).
	Sols can be added to a facet at (front add_sol_to_facet)
	or at the end (add_sol_to_facet_backwards)
	A global list of facets (all_facets) is maintained.
	Facets can be added to this with (add_facet) and removed with
	(remove_facet) and the whole list is freed up with (free_facets).
	(plot_all_facets) plots the entire set of facets.
	(print_facets) prints details of them on stderr.
	(sol_on_facet) finds whether a sol lies on a facet and returns
	the corresponding facet_sol.
	(first_sol_on_facet) takes a pair of facet_sols and returns 
	1 if the first facet_sol occurs first.

	The first real routine is (split_facet_on_chains)
	A chain is an ordered list of sols which connects nodes on the
	faces of the box to to singularities in the interior.
	This first finds if a facet contains repeated sols, if so
	the facet is split into two and the fun is recursed on each 
	facet.
	If then finds pairs of sols on the facet which are on the faces
	of the box and connected by a chain and finds the shortest
	such chain. 
	If then checks that the chain does not form part of the boundary
	of the facet. 
	Finally if all the above is satisfied then the facet is split into
	two which share a common edge which is the chain.
	 * ---- *
		    /	     \
		   /   chain  \
	 *---*----*---*
		   \	      /
		    \	     /
	 * ---- *

	The next major routine is (join_facets_by_chain) this takes
	a pair of facets and finds a pair of chains which link the two facets
	these chains must not contain any points in common.
	When two such chains have been found two new facets are constructed
	which assumes that the facets form the opposite ends of a cylinder.
	There is a bit of a logical problem here we do not know which way
	the two facets should be connected
	either
		  a ---------- e
		 / \          / \
		d   b        h   f
		 \ /          \ /
		  c ---------- g
	or
		  a ---------- e
		 / \          / \
		d   b        f   h
		 \ /          \ /
		  c ---------- g
	it might be possible to play a bit with the normals, but we cheat
	by finding the path where the two dist a-c. This is wrong!

	(refine_facets) manages the splitting up of facets
	it starts with facets which are just bounded by edges lying on the
	facets of the box.
	First it calls (join_facets) for each pair of facets
	then if finds facets which contain the same solution twice
	and it splits them. It also duplicates any chains which start at the
	linked facet (any chain can only be used to split a facet
	once).
	Finally it calls (split_facet_on_chains) for each facet.

	The main entry point for the drawing routine is (draw_box)
	this loops through all the the links joining solutions on the
	edges and faces of the box. When it finds such a link it 
	it calls (create_facet) this repeatedly calls (get_net_link)
	which finds a link adjacent to the current one until it gets back to the
	start. There is potential bug possibilities here where more than
	one link is adjacent to a a node, hopefully cured by (refine_facets).

	The main routine then calls (refine_facets) and then (plot_all_facets).
	If drawing of degenerate lines is switched on then it will
	find those chains which have not been used and print them
	as well as sings which have not been used.
	 */


	List<Facet_info> all_facets = null;

	private Facet_info add_facet()
	{
		Facet_info ele = new Facet_info();
		if(all_facets==null)
			all_facets=new ArrayList<Facet_info>();
		all_facets.add(ele);
		return(ele);
	}

	private void print_all_facets()
	{
		if(all_facets==null) {
			 BoxClevA.log.printf("No facets\n");
			 return;
		}
			
		for(Facet_info f1:all_facets)
		{
			BoxClevA.log.println(f1);
		}
	}

	/***** Modifying facets by chains *****************************************/

	/* actually split facet on a sub-chain
		results in f2,f3
	 */

	private void cut_facet_on_sub_chain(Facet_info f1,Facet_info f2,Facet_info f3,
			Facet_sol fs1,Facet_sol fs2,
			Chain_info chain,int first,int last)
	{
		Facet_sol fs3;
		int i,j;

		/* first copy sols on facet to new facets */

		for(fs3=fs1;fs3!=fs2 && fs3!=null;fs3=fs3.next)
		{
			f2.addSol(fs3.sol);
		}
		if(fs3==null)
			for(fs3=f1.sols;fs3!=fs2;fs3=fs3.next)
			{
				f2.addSol(fs3.sol);
			}
		f2.addSol(fs2.sol);

		for(fs3=fs2;fs3!=fs1 && fs3!= null;fs3=fs3.next)
		{
			f3.addSol(fs3.sol);
		}
		if(fs3==null)
			for(fs3=f1.sols;fs3!=fs1;fs3=fs3.next)
			{
				f3.addSol(fs3.sol);
			}
		f3.addSol(fs1.sol);

		/* now add the sols on chain */

		if(fs1.sol == chain.getSol(first))
		{
			if(first < last)
				for(i=first+1,j=last-1;i<last;++i,--j)
				{
					f3.addSol(chain.getSol(i));
					f2.addSol(chain.getSol(j));
				}
			else
				for(i=first-1,j=last+1;i>last;--i,++j)
				{
					f3.addSol(chain.getSol(i));
					f2.addSol(chain.getSol(j));
				}
		}
		else if(fs1.sol == chain.getSol(last))
		{
			if(first < last)
				for(i=first+1,j=last-1;i<last;++i,--j)
				{
					f3.addSol(chain.getSol(j));
					f2.addSol(chain.getSol(i));
				}
			else
				for(i=first-1,j=last+1;i>last;--i,++j)
				{
					f3.addSol(chain.getSol(j));
					f2.addSol(chain.getSol(i));
				}

		}
		else
		{
			if(failCountA++==0) {
				BoxClevA.log.printf("Funny stuff happening with the chain sols\n");
				BoxClevA.log.println(f1);
				BoxClevA.log.println(f2);
				BoxClevA.log.println(chain);
			}
		}
	}



	private List<Facet_info> split_facet_by_sub_chains(Box_info box,Facet_info f1)
	{
		Facet_sol fs1,fs2,fs3;
		Facet_info f2,f3;
		Chain_info chain2;
		double chain_length;
		int i;
		boolean flag;
		int last_index=-1,first_index=-1;
		int found_first,found_second;

		if(f1.sols == null || f1.sols.next == null || f1.sols.next.next == null) return null;

		if(PRINT_REFINE){
			BoxClevA.log.printf("split_facet_by_sub_chains:\n");
			BoxClevA.log.println(f1);
			if(box.xl==16 && box.yl==11 && box.zl==4)
			{
				BoxClevA.log.printf("split_facet_by_sub_chains:\n");
				BoxClevA.log.println(f1);print_chains(box.chains);
			}
		}
		for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			if(fs1.sol.type.compareTo(FACE_LL)<0) continue;
			for(fs2=f1.sols;fs2!=null;fs2=fs2.next)
			{
				if(fs2 == fs1) continue;
				if(fs2.sol.type.compareTo(FACE_LL)<0) continue;

				/* Now got a reasonable pair of facet sols lets look for chains */


				chain_length = 100.0;
				chain2 = null;
				if(box.chains!=null)
					for(Chain_info chain:box.chains)
					{
						double curLen = 0.0;
						found_first = found_second = -1;

						for(i=0;i<chain.length();++i)
						{
							/*
	BoxClevA.log.printf("%p %p %p\t",fs1.sol,fs2.sol,chain.getSol(i));
	print_sol(chain.getSol(i));
							 */
							if(fs1.sol == chain.getSol(i)) found_first = i;
							if(fs2.sol == chain.getSol(i)) found_second = i;
						}
						/*
	BoxClevA.log.printf("found_first %d %d\n",found_first,found_second);
						 */
						if(found_first == -1 || found_second == -1) continue;
						if(found_first < found_second)
						{
							for(i=found_first;i<found_second;++i)
								curLen += chain.metLens[i];
						}
						else
						{
							for(i=found_second;i<found_first;++i)
								curLen += chain.metLens[i];
						}

						if(curLen < chain_length )
						{
							chain2 = chain;
							chain_length = curLen;
							if(found_first < found_second)
							{
								first_index = found_first;
								last_index = found_second;
							}
							else
							{
								first_index = found_second;
								last_index = found_first;
							}
						}
					} /* end loop through chains */

				if(chain2 == null) continue;

				/* need to check that this chain is not already included as 
					segments round the facet */

				flag = false;
				for(i=first_index+1;i<last_index;++i)
				{
					for(fs3=f1.sols;fs3!=null;fs3=fs3.next)
						if(fs3.sol == chain2.getSol(i)) { flag = true; break; }
					if(flag) break;
				}
				if(flag) continue;
				if(chain2.length()==2 &&
						(fs2 == fs1.next || fs1 == fs2.next
						|| ( fs1 == f1.sols && fs2.next == null )
						|| ( fs2 == f1.sols && fs1.next == null ) ) )
					continue;

			if(chain2.used) continue;

				if(last_index == first_index ) continue;
				if(last_index-first_index == 1 
						&& ( fs2 == fs1.next 
						|| fs1 == fs2.next 
						|| ( fs1 == f1.sols && fs2.next == null )
						|| ( fs2 == f1.sols && fs1.next == null ) ) ) 
					continue;
				chain2.used =true;

				if(PRINT_REFINE){
					BoxClevA.log.printf("Split on chain %d %d\n",first_index,last_index);
					BoxClevA.log.print(chain2);
				}
				/* now found a chain to split on */

				//				f2 = add_facet();
				//				f3 = add_facet();
				f2 = new Facet_info();
				f3 = new Facet_info();
				cut_facet_on_sub_chain(f1,f2,f3,fs1,fs2,
						chain2,first_index,last_index);


				if(PRINT_REFINE){
					BoxClevA.log.printf("splited facet_by_sub_chains:\n");
					BoxClevA.log.println(f2);
					BoxClevA.log.println(f3);
				}
				List<Facet_info> res = new ArrayList<Facet_info>();
				List<Facet_info> list1 = split_facet_by_sub_chains(box,f2);
				List<Facet_info> list2 = split_facet_by_sub_chains(box,f3);
				if(list1!= null && !list1.isEmpty())
					res.addAll(list1);
				else
					res.add(f2);

				if(list2!= null && !list2.isEmpty())
					res.addAll(list2);
				else
					res.add(f3);
				/* now have to remove f1 for list of facets */

				//remove_facet(f1);
				return res;
			} /* end fs2 loop */
		}  /* end fs1 loop */
		return null;
	}

	private void print_chains(List<Chain_info> chains) {
		if(chains==null) {
			BoxClevA.log.println("no chains");
			return;
		}
		for(Chain_info chain:chains)
			BoxClevA.log.print(chain);
	}

	private double calc_fs_dist(Facet_sol fs1,Facet_sol fs2)
	{
		double dx,dy,dz;

		double vec1[]= fs1.sol.calc_pos_actual(boxclev.globalRegion,ctx);
		double vec2[]= fs2.sol.calc_pos_actual(boxclev.globalRegion,ctx);
		dx = vec1[0]-vec2[0];
		dy = vec1[1]-vec2[1];
		dz = vec1[2]-vec2[2];
		return Math.sqrt(dx*dx+dy*dy+dz*dz);
	}


	private int calc_orint_of_joined_facets(Facet_info f1,Facet_info f2,
			Facet_sol fs1,Facet_sol fs2,Facet_sol fs3,Facet_sol fs4)
	{
		Facet_sol fs5,fs6,fs7,fs8;
		int test1=0,test2=0,test3=0,test4=0,test5=0,test6=0;
		int orient_error = 0; /* whether an error found in calculation */

		fs5 = fs1.next;
		if(fs5 == null ) fs5 = f1.sols;
		fs6 = fs2.next;
		if(fs6 == null ) fs6 = f2.sols;
		fs7 = fs3.next;
		if(fs7 == null ) fs7 = f1.sols;
		fs8 = fs4.next;
		if(fs8 == null ) fs8 = f2.sols;

		if(fs1.sol.getDx() == fs2.sol.getDx() 
				&& fs1.sol.getDy() == fs2.sol.getDy()
				&& fs1.sol.getDz() == fs2.sol.getDz() )
		{
			if(fs1.sol.getDx() == 0 && fs1.sol.getDy() != 0 && fs1.sol.getDz() != 0 )
			{
				test1 = fs5.sol.getDx();
				test2 = fs6.sol.getDx();
			}
			else if(fs1.sol.getDx() != 0 && fs1.sol.getDy() == 0 && fs1.sol.getDz() != 0 )
			{
				test1 = fs5.sol.getDy();
				test2 = fs6.sol.getDy();
			}
			else if(fs1.sol.getDx() != 0 && fs1.sol.getDy() != 0 && fs1.sol.getDz() == 0 )
			{
				test1 = fs5.sol.getDz();
				test2 = fs6.sol.getDz();
			}
			else
			{
				orient_error = 1;
			}
		}
		else
		{
			orient_error = 2;
		}

		if(fs3.sol.getDx() == fs4.sol.getDx() 
				&& fs3.sol.getDy() == fs4.sol.getDy()
				&& fs3.sol.getDz() == fs4.sol.getDz() )
		{
			if(fs3.sol.getDx() == 0 && fs3.sol.getDy() != 0 && fs3.sol.getDz() != 0 )
			{
				test3 = fs7.sol.getDx();
				test4 = fs8.sol.getDx();
			}
			else if(fs3.sol.getDx() != 0 && fs3.sol.getDy() == 0 && fs3.sol.getDz() != 0 )
			{
				test3 = fs7.sol.getDy();
				test4 = fs8.sol.getDy();
			}
			else if(fs3.sol.getDx() != 0 && fs3.sol.getDy() != 0 && fs3.sol.getDz() == 0 )
			{
				test3 = fs7.sol.getDz();
				test4 = fs8.sol.getDz();
			}
			else
			{
				orient_error = 3;
			}
		}
		else
		{
			orient_error = 4;
		}

		if(orient_error!=0 ) {}
		else if(test1 == 0 || test2 == 0 )
			orient_error = 5;
		else if(test1 == test2 )
			test5 = 1;
		else
			test5 = -1;

		if(orient_error!=0 ) {}
		else if(test1 == 0 || test2 == 0 )
			orient_error = 6;
		else if(test3 == test4 )
			test6 = 1;
		else
			test6 = -1;

		if(orient_error!=0 ) {}
		else if(test5 != test6 )
		{
			orient_error = 7; /* This is serious as get different info form the links */
		}
		else if(test5 != 0 ) return test5;

		/* well that failed */

		if(failCountB++==0) {
			BoxClevA.log.printf("Error calculation orientation %d\n",orient_error);
			if(PRINT_FACET_ERR){
				BoxClevA.log.println(f1);
				BoxClevA.log.println(f2);
			//			print_chain(chain1);
			//			print_chain(chain2);
		}
		}
		/* try calculation normals at each point */

		{
			//			double vec1[]=new double[3],vec2[]=new double[3],vec3[]=new double[3],vec4[]=new double[3],vec5[]=new double[3],vec6[]=new double[3],vec7[]=new double[3],vec8[]=new double[3];
			//			double norm1[]=new double[3],norm2[]=new double[3],norm3[]=new double[3],norm4[]=new double[3],norm5[]=new double[3],norm6[]=new double[3],norm7[]=new double[3],norm8[]=new double[3];
			//			double vec15[]=new double[3],vec12[]=new double[3],vec26[]=new double[3], vec34[]=new double[3],vec37[]=new double[3],vec48[]=new double[3];
			//			double vnorm1[]=new double[3],vnorm2[]=new double[3],vnorm3[]=new double[3],vnorm4[]=new double[3];
			double dist,dist1 = 0,dist2=0,dist3=0,dist4=0;
			//			int count1 = 0, count2 = 0, count3 = 0;
			boolean res2;
			boolean res1;


			dist = 0.0;
			for(fs5=f1.sols,fs6=null;fs5!=null;fs6=fs5,fs5=fs5.next)
			{
				if(fs6!=null) 
					dist += calc_fs_dist(fs5,fs6);
				if(fs5 == fs1) dist1 = dist;
				if(fs5 == fs3) dist3 = dist;
			}
			dist += calc_fs_dist(f1.sols,fs6);
			if(dist1<dist3) { res1 = ( ( 2.0 * ( dist3 - dist1 ) ) < dist ); }
			else		{ res1 = ( ( 2.0 * ( dist1 - dist3 ) ) > dist ); }

			dist = 0.0;
			for(fs5=f2.sols,fs6=null;fs5!=null;fs6=fs5,fs5=fs5.next)
			{
				if(fs6!=null) dist+= calc_fs_dist(fs5,fs6);
				if(fs5 == fs2) dist2 = dist;
				if(fs5 == fs4) dist4 = dist;
			}
			dist += calc_fs_dist(f2.sols,fs6);
			if(dist2<dist4) { res2 = ( ( 2.0 * ( dist4 - dist2 ) ) < dist ); }
			else		{ res2 = ( ( 2.0 * ( dist2 - dist4 ) ) > dist ); }

			/*
		BoxClevA.log.printf("dist %f %f %f  %f %f %f res %d %d\n",dist5,dist1,dist3,dist,dist2,dist4,res1,res2);
			 */

			if( ( res1 && res2 ) || ( !res1 && !res2 ) ) return 1;
			else return -1;
		}

	}


	private boolean join_on_chain_and_point(Facet_info f1,Facet_info f2,
			Chain_info chain,Facet_sol dp1,Facet_sol dp2,Facet_sol ch1,Facet_sol ch2)
	{
		int res,i;
		Facet_info f3=null,f4=null;
		Facet_sol fs5;

		if(dp1 == ch1 || dp2 == ch2 ) return false;

		int ndups = 0;
		if(ndups!=1) {
			if(failCountC++==0) {
				BoxClevA.log.printf("join on chain and point\n");
				BoxClevA.log.println("Duplicate sols");
				for(Sol_info fs1 : f1.solsItt()) {
					if( f2.sol_on_facet(fs1)) {
						BoxClevA.log.println(fs1);
					}
				}				
			}
			return false;
		}
		res = calc_orint_of_joined_facets(f1,f2,dp1,dp2,ch1,ch2);
		if(PRINT_JOIN_CHAIN_POINT){
			BoxClevA.log.printf("orient %d\n",res);
			BoxClevA.log.println(dp1.sol);
			BoxClevA.log.print(chain);
			BoxClevA.log.println(f1);
			BoxClevA.log.println(f2);
		}
		f3 = add_facet();
		f4 = add_facet();

		f3.addSol_no_repeats(dp1.sol);

		for(fs5=dp1.next;fs5!=ch1 && fs5!=null; fs5=fs5.next)
		{
			f3.addSol_no_repeats(fs5.sol);
		}
		if(fs5==null)
			for(fs5=f1.sols;fs5!=ch1; fs5=fs5.next)
			{
				f3.addSol_no_repeats(fs5.sol);
			}
		if(chain.getSol(0) == ch1.sol)
		{
			for(i=0;i<chain.length();++i)
				f3.addSol_no_repeats(chain.getSol(i));
		}
		else
		{
			for(i=chain.length()-1;i>=0;--i)
				f3.addSol_no_repeats(chain.getSol(i));
		}


		if(chain.getSol(0) == ch1.sol)
		{
			for(i=chain.length()-1;i>=0;--i)
				f4.addSol_no_repeats(chain.getSol(i));
		}
		else
		{
			for(i=0;i<chain.length();++i)
				f4.addSol_no_repeats(chain.getSol(i));
		}
		for(fs5=ch1.next;fs5!=dp1 && fs5!=null; fs5=fs5.next)
		{
			f4.addSol_no_repeats(fs5.sol);
		}
		if(fs5==null)
			for(fs5=f1.sols;fs5!=dp1; fs5=fs5.next)
			{
				f4.addSol_no_repeats(fs5.sol);
			}
		f4.addSol_no_repeats(dp2.sol);

		if(res>0)
		{
			for(fs5=dp2.next;fs5!=ch2 && fs5!=null; fs5=fs5.next)
			{
				f3.add_sol_to_facet_backwards(fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=ch2; fs5=fs5.next)
				{
					f3.add_sol_to_facet_backwards(fs5.sol);
				}

			for(fs5=ch2.next;fs5!=dp2 && fs5!=null; fs5=fs5.next)
			{
				f4.add_sol_to_facet_backwards(fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=dp2; fs5=fs5.next)
				{
					f4.add_sol_to_facet_backwards(fs5.sol);
				}
		}
		else
		{
			for(fs5=ch2.next;fs5!=dp2 && fs5!=null; fs5=fs5.next)
			{
				f3.addSol_no_repeats(fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=dp2; fs5=fs5.next)
				{
					f3.addSol_no_repeats(fs5.sol);
				}
			for(fs5=dp2.next;fs5!=ch2 && fs5!=null; fs5=fs5.next)
			{
				f4.addSol_no_repeats(fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=ch2; fs5=fs5.next)
				{
					f4.addSol_no_repeats(fs5.sol);
				}
		}

		if(PRINT_JOIN_CHAIN_POINT){
			BoxClevA.log.printf("after join on chain and point\n");
			BoxClevA.log.println(f3);
			BoxClevA.log.println(f4);
		}
		all_facets.remove(f1);
		all_facets.remove(f2);
		return(true);
	}

	private boolean join_facets_by_chains(Box_info box,Facet_info f1,Facet_info f2)
	{
		Facet_sol fs1=null,fs2=null,fs3=null,fs4=null,fs5=null;
		Chain_info chain2=null,chain3=null;
		int count = 0,i;
		Facet_info f3=null,f4=null;
		double chain_length;
		Sol_info chainsol1=null,chainsol2=null;
		int res;
		Facet_sol double_pointA=null,double_pointB=null;

		if(PRINT_REFINE){
			BoxClevA.log.printf("Join facets by chains\n");
			BoxClevA.log.println(f1);
			BoxClevA.log.println(f2);
		}

		for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			for(fs2=f2.sols;fs2!=null;fs2=fs2.next)
			{
				if(fs1.sol == fs2.sol)
				{
					double_pointA = fs1;
					double_pointB = fs2;
				}
			}
		}

		chain3 = null;
		for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			for(fs2=f2.sols;fs2!=null;fs2=fs2.next)
			{
				boolean flag5;

				if(fs1.sol == fs2.sol) continue;
				chain_length = 100.0;
				chain2 = null;
				if(box.chains!=null)
					for(Chain_info chain:box.chains)
					{
						int f1_index=-1,f2_index=-1;

						chainsol1 = chain.getSol(0);
						chainsol2 = chain.getSol(chain.length()-1);
						if(chainsol1 == fs1.sol ) f1_index = 0;
						if(chainsol2 == fs1.sol ) f1_index = chain.length()-1;

						if(chainsol1 == fs2.sol ) f2_index = 0;
						if(chainsol2 == fs2.sol ) f2_index = chain.length()-1;

						if(f1_index == -1 || f2_index == -1) continue;

						if(double_pointA != null &&
								(  chainsol1 == double_pointA.sol
								|| chainsol2 == double_pointA.sol ) ) continue;


						/* check that none of sols on chain are on facet */

						flag5 = false;

						for(fs5=f1.sols;fs5!=null;fs5=fs5.next)
						{
							for(i=1;i<chain.length()-1;++i)
							{
								if(chain.getSol(i)==fs5.sol)
								{
									flag5 = true;
									break;
								}
							}
							if(chain.getSol(f2_index) == fs5.sol)
								flag5 = true;

							if(flag5) break;
						}

						for(fs5=f2.sols;fs5!=null;fs5=fs5.next)
						{
							for(i=1;i<chain.length()-1;++i)
							{
								if(chain.getSol(i)==fs5.sol)
								{
									flag5 = true;
									break;
								}
							}
							if(chain.getSol(f1_index) == fs5.sol)
								flag5 = true;
							if(flag5) break;
						}

						if(flag5) continue;
						if(chain.metric_length < chain_length)
						{
							chain_length = chain.metric_length;
							chain2 = chain;
						}
					}
				if(chain2 == null) continue; /* didn't find a linking chain */
				if(chain2.used) continue;
				if(fs1 == fs3 || fs2 == fs4) continue;	/* ensure that start and end sols not the same */

				if(chain3!=null)
				{
					boolean flag2;
					int j;

					/* have two chains ensure they have no vertices in common */

					flag2 = false;
					for(i=0;i<chain2.length();++i)
						for(j=0;j<chain3.length();++j)
						{
							if(chain2.getSol(i)== chain3.getSol(j)) flag2 = true;
						}
					if(flag2) continue;
				}
				/* found a chain */
				++count;
				if(count == 2) break; /* have two OK chains */ 

				/* now got the first chain */

				fs3 = fs1; fs4 = fs2;
				chain3 = chain2;
			} /* end fs2 loop */
			if(count>=2) break;
		} /* end fs1 loop */

		if(count == 0) return false; /* no linking chains */
		if(double_pointA!=null)
		{
			return join_on_chain_and_point(f1,f2,chain3,
					double_pointA,double_pointB,fs3,fs4);
		}
		if(count == 1) return false;

		if(PRINT_REFINE){
			BoxClevA.log.printf("Found two linking chains\n");
			BoxClevA.log.print(chain2);
			BoxClevA.log.printf("and\n");
			BoxClevA.log.print(chain3);
		}
		if(fs1 == fs3 || fs2 == fs4)
		{
			if(failCountD++==0) {
				BoxClevA.log.printf("two of the linking facet sols are the same\n");
				BoxClevA.log.println(f1);
				BoxClevA.log.println(f2);
				BoxClevA.log.print(chain2);
				BoxClevA.log.print(chain3);				
			}			
			return false;
		}
		chain2.used = true;
		chain3.used = true;

		/*
			res = calc_orint_of_joined_facets(f1,f2,chain2,chain3,
				fs1,fs2,fs3,fs4);
		 */
		res = calc_orint_of_joined_facets(f1,f2,
				fs1,fs2,fs3,fs4);
		f3 = add_facet();
		f4 = add_facet();

		if(chain2.getSol(0) == fs1.sol)
		{
			for(i=chain2.length()-1;i>=0;--i)
				f3.addSol_no_repeats(chain2.getSol(i));
		}
		else
		{
			for(i=0;i<chain2.length();++i)
				f3.addSol_no_repeats(chain2.getSol(i));
		}
		for(fs5=fs1.next;fs5!=fs3 && fs5!=null; fs5=fs5.next)
		{
			f3.addSol_no_repeats(fs5.sol);
		}
		if(fs5==null)
			for(fs5=f1.sols;fs5!=fs3; fs5=fs5.next)
			{
				f3.addSol_no_repeats(fs5.sol);
			}
		if(chain3.getSol(0) == fs3.sol)
		{
			for(i=0;i<chain3.length();++i)
				f3.addSol_no_repeats(chain3.getSol(i));
		}
		else
		{
			for(i=chain3.length()-1;i>=0;--i)
				f3.addSol_no_repeats(chain3.getSol(i));
		}


		if(chain3.getSol(0) == fs3.sol)
		{
			for(i=chain3.length()-1;i>=0;--i)
				f4.addSol_no_repeats(chain3.getSol(i));
		}
		else
		{
			for(i=0;i<chain3.length();++i)
				f4.addSol_no_repeats(chain3.getSol(i));
		}
		for(fs5=fs3.next;fs5!=fs1 && fs5!=null; fs5=fs5.next)
		{
			f4.addSol_no_repeats(fs5.sol);
		}
		if(fs5==null)
			for(fs5=f1.sols;fs5!=fs1; fs5=fs5.next)
			{
				f4.addSol_no_repeats(fs5.sol);
			}
		if(chain2.getSol(0) == fs1.sol)
		{
			for(i=0;i<chain2.length();++i)
				f4.addSol_no_repeats(chain2.getSol(i));
		}
		else
		{
			for(i=chain2.length()-1;i>=0;--i)
				f4.addSol_no_repeats(chain2.getSol(i));
		}


		if(res>0)
		{
			for(fs5=fs2.next;fs5!=fs4 && fs5!=null; fs5=fs5.next)
			{
				f3.add_sol_to_facet_backwards(fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=fs4; fs5=fs5.next)
				{
					f3.add_sol_to_facet_backwards(fs5.sol);
				}
			for(fs5=fs4.next;fs5!=fs2 && fs5!=null; fs5=fs5.next)
			{
				f4.add_sol_to_facet_backwards(fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=fs2; fs5=fs5.next)
				{
					f4.add_sol_to_facet_backwards(fs5.sol);
				}
		}
		else
		{
			for(fs5=fs4.next;fs5!=fs2 && fs5!=null; fs5=fs5.next)
			{
				f3.addSol_no_repeats(fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=fs2; fs5=fs5.next)
				{
					f3.addSol_no_repeats(fs5.sol);
				}
			for(fs5=fs2.next;fs5!=fs4 && fs5!=null; fs5=fs5.next)
			{
				f4.addSol_no_repeats(fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=fs4; fs5=fs5.next)
				{
					f4.addSol_no_repeats(fs5.sol);
				}
		}

		if(f3.has_repeated_sol()) {
			if(failCountE++==0) {
				BoxClevA.log.printf("Join facets by chains\n");
				BoxClevA.log.println("Facet with repeated sol");
				BoxClevA.log.println(f3);
			}
		}

		if(f4.has_repeated_sol()) {
			if(failCountE++==0) {
				BoxClevA.log.printf("Join facets by chains\n");
				BoxClevA.log.println("Facet with repeated sol");
				BoxClevA.log.println(f4);
			}
		}


		all_facets.remove(f1);
		all_facets.remove(f2);
		return(true);
	}

	/**
	 * Splits facets on sols in common; join facets linked by chains
	 * @param box
	 */
	private void refine_facets(Box_info box)
	{
		if(all_facets==null)
			return;
		if(PRINT_REFINE){
			print_all_facets();
		}


		refine_by_splitting_on_repeated_solution();

		for(Facet_info facet:all_facets) {
			if(facet.has_repeated_sol()) {
				if(failCountF++==0) {
					BoxClevA.log.println("Refine facets");
					BoxClevA.log.println("Facet has repeated sol");
					BoxClevA.log.println(facet);
				}
			}
		}

		refine_by_joining_on_chains(box);

		all_facets = refine_by_splitting_on_chains(box);
	
		all_facets = refine_by_splitting_on_derivatives(box);

		if(PRINT_REFINE){
			BoxClevA.log.printf("Final facets\n");
			print_all_facets();
		}
	}


	private List<Facet_info> refine_by_splitting_on_derivatives(Box_info box) {
		List<Facet_info> cleanFacets2 = new ArrayList<>();

		boolean fhnAgain;
		fhnAgain = true;
		while(fhnAgain)
		{
			ListIterator<Facet_info> li = all_facets.listIterator();
			
			fhnAgain=false;
			while(li.hasNext())
			{
				Facet_info f1 = li.next();
		
				List<Facet_info> res2 = split_facet_by_derivs(f1);
				if(res2!=null && !res2.isEmpty()) {
					if(PRINT_REFINE){
						BoxClevA.log.println("Split by deriv");
						BoxClevA.log.println(f1);
						BoxClevA.log.println(res2);
					}
					// TODO There should be a better way of preventing this happening in the first place
					boolean all_smaller=true;
					for(Facet_info f2:res2) {
						if(f2.size()>=f1.size())
							all_smaller = false;
					}
					if(!all_smaller) {
						if(failCountG++==0) {
							BoxClevA.log.println("Split by deriv didn't reduce size");
							BoxClevA.log.println(f1);
							BoxClevA.log.println(res2);
						}
						cleanFacets2.add(f1);
						li.remove();
						continue;
					}
					li.remove();
					for(Facet_info f2:res2)
						li.add(f2);
					fhnAgain=true;
					break;
				}
				cleanFacets2.add(f1);
				li.remove();
			}
		}
		return cleanFacets2;
	}

	/**
	 *  Split facets where two of the nodes are linked by a chain
	 **/

	private List<Facet_info> refine_by_splitting_on_chains(Box_info box) {

		List<Facet_info> cleanFacets = new ArrayList<>();

		boolean fhnAgain = true;
		while(fhnAgain)
		{
			ListIterator<Facet_info> li = all_facets.listIterator();
			
			fhnAgain=false;
			while(li.hasNext())
			{
				Facet_info f1 = li.next();
				//			f2 = f1.next; 
				/* can't guarantee that f1 will still exist after splitting */

				List<Facet_info> res = split_facet_by_sub_chains(box,f1);
				if(res!=null && !res.isEmpty()) {
					if(PRINT_REFINE){
					BoxClevA.log.println("Split by chain");
					BoxClevA.log.println(f1);
					BoxClevA.log.println(res);
					}
					li.remove();
					for(Facet_info f2:res)
						li.add(f2);
					fhnAgain=true;
					break;
				}
				cleanFacets.add(f1);
				li.remove();
			} /* end loop through facets */
		}
		return cleanFacets;
	}


	/**
	 * Join facets linked by chains
	 * @param box
	 */
	private void refine_by_joining_on_chains(Box_info box) {
		boolean flag;

		flag = true;
		while(flag)
		{
			flag = false;
			ListIterator<Facet_info> it1 = all_facets.listIterator();
			while(it1.hasNext()) {
				Facet_info f1 = it1.next();
				int nextIndex = it1.nextIndex();
				ListIterator<Facet_info> it2 = all_facets.listIterator(nextIndex);
				while(it2.hasNext()) {
					Facet_info f2 = it2.next();

					flag = join_facets_by_chains(box,f1,f2);
					if(flag) {
						break;
					}
				}
				if(flag) break;
			}
		}	
		if(PRINT_REFINE){
			BoxClevA.log.printf("After joining\n");
			print_all_facets();
		}
	}

	/**
	 * May have a facet with repeated vertices
	 * if so split the facet 
	 */
	private void refine_by_splitting_on_repeated_solution() {
		boolean flag;
		flag = true;
		while(flag)
		{
			flag = false;
			for(Facet_info f1:all_facets)
			{
				Facet_info f2 = split_facet_by_reapeated_sol(f1);
				if(f2!=null) {
					all_facets.add(f2);
					flag=true;
					break;
				}
			} /* end f1 loop */
		}
	}

	/**
	 * @param flag
	 * @param f1
	 * @return
	 */
	public Facet_info split_facet_by_reapeated_sol(Facet_info f1) {

		for(Facet_sol fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			for(Facet_sol fs2=fs1.next,fs3=fs1;fs2!=null;fs3=fs2,fs2=fs2.next)
			{
				if(fs1.sol == fs2.sol)
				{
					if(PRINT_REFINE){
						BoxClevA.log.printf("Split on sol\n");
						BoxClevA.log.println(fs1.sol);
						BoxClevA.log.println(f1);
					}
					Facet_sol fs4 = fs1.next;
					fs1.next = fs2.next;
					if(fs2 != fs4)
						fs2.next = fs4;
					if(fs3!=fs1)
						fs3.next = null;
					Facet_info f2 = new Facet_info();
					f2.sols = fs2;	
					if(PRINT_REFINE){
						BoxClevA.log.printf("after Split on sol\n");
						BoxClevA.log.println(f1);
						BoxClevA.log.println(f2);
					}
					return f2;
				}
			} /* end fs2 loop */
		} /* end fs1 loop */
		return null;
	}						

	static class DerivSig {
		int dx,dy,dz;

		public DerivSig(int dx, int dy, int dz) {
			super();
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
		}

		public DerivSig(Sol_info sol) {
			this(sol.getDx(),sol.getDy(),sol.getDz());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dx;
			result = prime * result + dy;
			result = prime * result + dz;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DerivSig other = (DerivSig) obj;
			if (dx != other.dx)
				return false;
			if (dy != other.dy)
				return false;
			if (dz != other.dz)
				return false;
			return true;
		}
		/**
		 * True unless one deriv has opposite signs
		 * @param sol
		 * @return
		 */
		public boolean weekMatch(Sol_info sol) {
			if(dx * sol.getDx() <0 || dy * sol.getDy() <0 || dz * sol.getDz() <0) 
				return false;
			return true;
		}

		public boolean weekMatch(DerivSig sol) {
			if(dx * sol.dx <0 || dy * sol.dy <0 || dz * sol.dz <0) 
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DerivSig [dx=" + dx + ", dy=" + dy + ", dz=" + dz + "]";
		}

		public DerivSig merge(DerivSig that) {
			if(this.dx * that.dx <0) return null;
			if(this.dy * that.dy <0) return null;
			if(this.dz * that.dz <0) return null;
			int x = (this.dx == 1 || that.dx == 1) ? 1 : (this.dx == -1 || that.dx == -1) ? -1 : 0;
			int y = (this.dy == 1 || that.dy == 1) ? 1 : (this.dy == -1 || that.dy == -1) ? -1 : 0;
			int z = (this.dz == 1 || that.dz == 1) ? 1 : (this.dz == -1 || that.dz == -1) ? -1 : 0;
			return new DerivSig(x,y,z);
		}



	}

	/**
	 * Splits a facet into facets where each facet has matching derivative.
	 * @param box
	 * @param f1
	 * @return
	 */
	private List<Facet_info> split_facet_by_derivs(Facet_info f1) {

		Set<DerivSig> sigs = new HashSet<DerivSig>();
		Set<DerivSig> weeksigs = new HashSet<DerivSig>();

		for(Sol_info sol:f1.solsItt()) {
			if(sol.getDx()!=0 && sol.getDy()!=0 && sol.getDz()!=0)
				sigs.add(new DerivSig(sol));
			else
				weeksigs.add(new DerivSig(sol));
		}
		if(sigs.size()==1) {
			DerivSig strongSig = sigs.toArray(new DerivSig[1])[0];
			boolean allMatch=true;
			for(DerivSig weekSig:weeksigs) {
				if(!strongSig.weekMatch(weekSig))
					allMatch=false;
			}
			if(allMatch) {
				f1.dx = strongSig.dx;
				f1.dy = strongSig.dy;
				f1.dz = strongSig.dz;
				return null;
			}
		}
		else if(sigs.size()==0) {
			DerivSig mergedSig=null;
			for(DerivSig weekSig:weeksigs) {
				if(mergedSig==null) {
					mergedSig=weekSig;
				}
				else {
					mergedSig = mergedSig.merge(weekSig);
				}
				if(mergedSig==null)
					break;
			}
			if(mergedSig!=null) {
				f1.dx = mergedSig.dx;
				f1.dy = mergedSig.dy;
				f1.dz = mergedSig.dz;
				return null;
			}
		}

		List<Facet_info> res = new ArrayList<Facet_info>();

		Set<Sol_info> done = new HashSet<>();
		for(DerivSig sig:sigs) {
			Facet_info f2 = new Facet_info();
			for(Sol_info sol:f1.solsItt()) {
				if(sig.weekMatch(sol)) {
					f2.addMatchingSol(sol);
					done.add(sol);
				}
			}
			f2.dx = sig.dx;
			f2.dy = sig.dy;
			f2.dz = sig.dz;
			res.add(f2);
			break;
		}
		if(res.isEmpty())
			return res;
		if(res.get(0).getSols().size()==2) {
			return null;
		}
		
		CyclicList<Sol_info> sols = f1.getSols();
		for(Sol_info sol:sols) {
			if(done.contains(sol)) continue;
			// go backward from sol until done
			Iterator<Sol_info> itt1 = sols.backwardIteratorFrom(sol);
			itt1.next();
			Sol_info prev=null;
			while(itt1.hasNext()) {
				prev = itt1.next();
				if(done.contains(prev))
					break;
			}
			Facet_info f2 = new Facet_info();
			Iterator<Sol_info> itt2 = sols.forwardIteratorFrom(prev);
			f2.addSol(itt2.next());
			while(itt2.hasNext()) {
				Sol_info next = itt2.next();
				f2.addSol(next);
				if(done.contains(next))
					break;
				done.add(next);
			}
			res.add(f2);
		}
		
		return res;
	}

	/************************************************************************/
	/*									*/
	/*	Working out chains of singularities through a box		*/
	/*	make_chains starts from each node_link				*/
	/*	if the node link has two faces at its end then we have a simple */
	/* 	two element chain.						*/
	/*	if it has two sings then we ignore it				*/
	/*	if it has only one sing then use the follow_chain procedure	*/
	/*	to follow the chain until it reaches another face node		*/
	/*	or if it joins back on itself					*/
	/*	If an intermediate sing has more than two adjacent node_links 	*/
	/*	create a new chain and recursivly call follow_chain on than	*/
	/*									*/
	/************************************************************************/

	private void make_chains(Box_info box)
	{
		Chain_info chain;

		float dx,dy,dz;
		boolean flag;
		int i;

		box.chains = null;
		if(box.node_links!=null)
			for(Node_link_info n1:box.node_links)
			{
				if(n1.singA == null && n1.singB == null)
				{
					chain = new Chain_info();
					//				if(TEST_ALLOC){
					//					++chaincount; ++chainmax; ++chainnew;
					//				}
					//chain.length =2;
					chain.used = false;
					//chain.sols = new Sol_info[2];
					chain.addSol(n1.A.sol);
					chain.addSol(n1.B.sol);

					dx = ((float) chain.getSol(0).xl) / chain.getSol(0).denom
							- ((float) chain.getSol(1).xl) / chain.getSol(1).denom;
					dy = ((float) chain.getSol(0).yl) / chain.getSol(0).denom
							- ((float) chain.getSol(1).yl) / chain.getSol(1).denom;
					dz = ((float) chain.getSol(0).zl) / chain.getSol(0).denom
							- ((float) chain.getSol(1).zl) / chain.getSol(1).denom;
					chain.metric_length = Math.sqrt( dx * dx + dy * dy + dz * dz);

					chain.metLens = new double[2];
					chain.metLens[0] = chain.metric_length;
					if(box.chains==null)
						box.chains=new ArrayList<Chain_info>();
					box.chains.add(chain);
				}
				else if(n1.singA != null && n1.singB != null)
				{
				}
				else
				{
					chain = new Chain_info();
					//				if(TEST_ALLOC){
					//					++chaincount; ++chainmax; ++chainnew;
					//				}
					//chain.length =2;
					chain.used = false;
					//chain.sols = (Sol_info *) malloc(sizeof(Sol_info ) * (box.num_sings+2));
					chain.metLens = new double[box.num_sings+1];
					if(n1.singA == null)
					{
						chain.addSol(n1.A.sol);
						chain.addSol(n1.B.sol);
					}
					else
					{
						chain.addSol(n1.B.sol);
						chain.addSol(n1.A.sol);
					}

					dx = ((float) chain.getSol(0).xl) / chain.getSol(0).denom
							- ((float) chain.getSol(1).xl) / chain.getSol(1).denom;
					dy = ((float) chain.getSol(0).yl) / chain.getSol(0).denom
							- ((float) chain.getSol(1).yl) / chain.getSol(1).denom;
					dz = ((float) chain.getSol(0).zl) / chain.getSol(0).denom
							- ((float) chain.getSol(1).zl) / chain.getSol(1).denom;
					chain.metric_length = Math.sqrt( dx * dx + dy * dy + dz * dz);
					chain.metLens[0] = chain.metric_length;

					if(box.chains==null)
						box.chains = new ArrayList<Chain_info>();
						box.chains.add(chain);

						/* Now recurse along the chain */

						if(n1.singA != null)
						{
							follow_chain(box,chain,n1.singA,n1);
						}
						else
						{
							follow_chain(box,chain,n1.singB,n1);
						}
				}
			}

		/* Some chains may not end on a face - remove them */


		if(box.chains==null) 
			return;
		
			ListIterator<Chain_info> li=box.chains.listIterator();

			while(li.hasNext())
			{
				Chain_info chain1 = li.next();

				if(chain1.getSol(0).type.compareTo(FACE_LL) < 0 || chain1.getSol(0).type.compareTo(FACE_UU) > 0 
						|| chain1.getSol(chain1.length()-1).type.compareTo(FACE_LL) < 0 
						|| chain1.getSol(chain1.length()-1).type.compareTo(FACE_UU) > 0 )
				{
					li.remove();
				}
			}


			/* Now want to remove duplicate chains */

			li=box.chains.listIterator();
			while(li.hasNext())
			{
				Chain_info chain1 = li.next();
				int ind = li.nextIndex();
				ListIterator<Chain_info> li2=box.chains.listIterator(ind);
				while(li2.hasNext())
				{
					Chain_info chain2 = li2.next();
					if(chain1.length() != chain2.length()) continue;
					flag = true;
					if(chain1.getSol(0) == chain2.getSol(0) 
							&& chain1.getSol(chain1.length()-1) == chain2.getSol(chain2.length()-1) )
					{
						for(i=1;i<chain1.length()-1;++i)
						{
							if(chain1.getSol(i) != chain2.getSol(i)) flag = false;
						}
					}
					else if(chain1.getSol(0) == chain2.getSol(chain2.length()-1) 
							&& chain1.getSol(chain1.length()-1) == chain2.getSol(0) )
					{
						for(i=1;i<chain1.length()-1;++i)
						{
							if(chain1.getSol(i) != chain2.getSol(chain2.length()-1-i)) flag = false;
						}
					}
					else flag = false;

					if(flag)
					{
						li.remove();
						//					chain3.next = chain2.next;
						//					free(chain2.sols);
						//					free(chain2.metLens);
						//					free(chain2);
						break;
					}
				}
			}

			/* Now want to remove chains with a sign conflict */

			li=box.chains.listIterator();
			while(li.hasNext())
			{
				Chain_info chain1 = li.next();
				if(!chain1.has_coherent_signs()) {
//					BoxClevA.log.println("Chain with sign conflict");
//					BoxClevA.log.println(chain1);
					li.remove();
				}
				
			}		
					
	}

	private void follow_chain(Box_info box,Chain_info chain,Sing_info sing,Node_link_info nl)
	{
		Sol_info next_sol=null;
		Sing_info next_sing=null;
		Node_link_info next_nl=null;
		Chain_info chain2=null;
		int i,j,k;
		float dx,dy,dz;

		if(PRINT_FOLLOW_CHAIN){
			BoxClevA.log.printf("follow_chain:\n");
			BoxClevA.log.print(sing);
			BoxClevA.log.print(nl);
		}
		while(true)
		{
			if(sing.numNLs == 0)
			{
				BoxClevA.log.printf("Sing has zero adjNLs\n");
				break;
			}
			else if(sing.numNLs == 1)
			{
				BoxClevA.log.printf("Sing has only one adjNLs\n");
				break;
			}
			else if(sing.numNLs == 2)
			{
				if(PRINT_FOLLOW_CHAIN){
					BoxClevA.log.printf("Simple add\n");
				}
				if(sing.adjacentNLs[0] == nl)
				{
					next_nl = sing.adjacentNLs[1];
				}
				else if(sing.adjacentNLs[1] == nl)
				{
					next_nl = sing.adjacentNLs[0];
				}
				else
				{
					BoxClevA.log.printf("node_link not adjacet to sing\n");
				}

				if(next_nl.singA == sing)
				{
					next_sing = next_nl.singB;
					next_sol = next_nl.B.sol;
				}
				else if(next_nl.singB == sing)
				{
					next_sing = next_nl.singA;
					next_sol = next_nl.A.sol;
				}
				else
				{
					BoxClevA.log.printf("Sing not adjacent to NL\n");
					break;
				}

				/* now check that the next sol is not already in the chain */

				for(i=0;i<chain.length();++i)
				{
					if(next_sol == chain.getSol(i))
					{
						break;
					}
				}
				if(i!=chain.length()) break;

				/* everything OK add sol to the end of the chain */

				chain.addSol(next_sol);

				dx = ((float) chain.getSol(chain.length()-2).xl) / chain.getSol(chain.length()-2).denom
						- ((float) chain.getSol(chain.length()-1).xl) / chain.getSol(chain.length()-1).denom;
				dy = ((float) chain.getSol(chain.length()-2).yl) / chain.getSol(chain.length()-2).denom
						- ((float) chain.getSol(chain.length()-1).yl) / chain.getSol(chain.length()-1).denom;
				dz = ((float) chain.getSol(chain.length()-2).zl) / chain.getSol(chain.length()-2).denom
						- ((float) chain.getSol(chain.length()-1).zl) / chain.getSol(chain.length()-1).denom;
				chain.metLens[chain.length()-2] = Math.sqrt( dx * dx + dy * dy + dz * dz);
				chain.metric_length += chain.metLens[chain.length()-2];
				nl = next_nl;
				sing = next_sing;

				if(sing==null) break;	/* reached the end of the chain */
				continue;
			}

			/* now have more than two sings in the chain */
			if(PRINT_FOLLOW_CHAIN){
				BoxClevA.log.printf("Sing with %d node links\n",sing.numNLs);
				BoxClevA.log.print(sing);
			}
			j = 0;
			for(i=0;i<sing.numNLs;++i)
			{
				if(sing.adjacentNLs[i] == nl) continue;
				if(PRINT_FOLLOW_CHAIN){
					BoxClevA.log.printf("Trying link no %d\n",i);
				}

				next_nl = sing.adjacentNLs[i];
				if(next_nl.singA == sing)
				{
					next_sing = next_nl.singB;
					next_sol = next_nl.B.sol;
				}
				else if(next_nl.singB == sing)
				{
					next_sing = next_nl.singA;
					next_sol = next_nl.A.sol;
				}
				else
				{
					BoxClevA.log.printf("Sing not adjacent to NL\n");
					break;
				}
				if(PRINT_FOLLOW_CHAIN){
					BoxClevA.log.print(next_nl);
					BoxClevA.log.print(next_sing);
					BoxClevA.log.println(next_sol);
				}
				/* now check that the next sol is not already in the chain */


				for(k=0;k<chain.length();++k)
				{
					if(next_sol == chain.getSol(k))
					{
						break;
					}
				} 

				++j; /* always increment this so can pick up last adjNL to do */
				if(j < sing.numNLs -1 )
				{
					if(k!=chain.length()) continue;

					/* need to make a new chain */
					if(PRINT_FOLLOW_CHAIN){
						BoxClevA.log.printf("Making a new chain\n");
					}
					chain2 =  new Chain_info();
					//					if(TEST_ALLOC){
					//						++chaincount; ++chainmax; ++chainnew;
					//					}
					//					chain2.length() = chain.length;
					chain2.metric_length = chain.metric_length;
					chain2.used = false;
					//chain2.sols = (Sol_info *) malloc(sizeof(Sol_info )*(box.num_sings+2));
					chain2.metLens = new double[box.num_sings+1];
					chain2.sols.addAll(chain.sols);
					//memcpy(chain2.sols,chain.sols,sizeof(Sol_info )*chain.length());
					//memcpy(chain2.metLens,chain.metLens,sizeof(float)*(chain.length-1));
					System.arraycopy(chain.metLens, 0, chain2.metLens, 0, chain.length()-1);
					chain2.addSol(next_sol);

					dx = ((float) chain2.getSol(chain2.length()-2).xl) / chain2.getSol(chain2.length()-2).denom
							- ((float) chain2.getSol(chain2.length()-1).xl) / chain2.getSol(chain2.length()-1).denom;
					dy = ((float) chain2.getSol(chain2.length()-2).yl) / chain2.getSol(chain2.length()-2).denom
							- ((float) chain2.getSol(chain2.length()-1).yl) / chain2.getSol(chain2.length()-1).denom;
					dz = ((float) chain2.getSol(chain2.length()-2).zl) / chain2.getSol(chain2.length()-2).denom
							- ((float) chain2.getSol(chain2.length()-1).zl) / chain2.getSol(chain2.length()-1).denom;
					chain2.metLens[chain2.length()-2] = Math.sqrt( dx * dx + dy * dy + dz * dz);
					chain2.metric_length += chain2.metLens[chain2.length()-2];

					box.chains.add(chain2);
					if(next_sing != null) 
					{
						follow_chain(box,chain2,next_sing,next_nl);
					}
				}
				else
				{
					if(k!=chain.length())
					{
						sing = null;
						break;
					}
					if(PRINT_FOLLOW_CHAIN){
						BoxClevA.log.printf("Adding to existing chain\n");
					}
					/* Just add to this chain */

					chain.addSol(next_sol);

					dx = ((float) chain.getSol(chain.length()-2).xl) / chain.getSol(chain.length()-2).denom
							- ((float) chain.getSol(chain.length()-1).xl) / chain.getSol(chain.length()-1).denom;
					dy = ((float) chain.getSol(chain.length()-2).yl) / chain.getSol(chain.length()-2).denom
							- ((float) chain.getSol(chain.length()-1).yl) / chain.getSol(chain.length()-1).denom;
					dz = ((float) chain.getSol(chain.length()-2).zl) / chain.getSol(chain.length()-2).denom
							- ((float) chain.getSol(chain.length()-1).zl) / chain.getSol(chain.length()-1).denom;
					chain.metLens[chain.length()-2] = Math.sqrt( dx * dx + dy * dy + dz * dz);
					chain.metric_length += chain.metLens[chain.length()-2];

					nl = next_nl;
					sing = next_sing;

					if(sing==null) break;	/* reached the end of the chain */
					continue;
				}
			} /* end for i */

			if(sing==null) break;	/* reached the end of the chain */
		} /* end while */
	}

	/*****	Combining Facets Routines **************************************/

	private boolean sol_on_box_boundary_or_halfplane(Box_info box,Sol_info sol,Key3D plane)
	{
		boolean testX,testY,testZ;

		testX = (   sol.xl * box.denom == box.xl * sol.denom 
				|| sol.xl * box.denom == (box.xl+1) * sol.denom );
		testY = (   sol.yl * box.denom == box.yl * sol.denom 
				|| sol.yl * box.denom == (box.yl+1) * sol.denom );
		testZ = (   sol.zl * box.denom == box.zl * sol.denom 
				|| sol.zl * box.denom == (box.zl+1) * sol.denom );

		//		if(plane == X_AXIS)
		//			testY = testY
		//			|| 2 * sol.yl * box.denom == (2*box.yl+1) * sol.denom;
		//		if(plane == FACE_DD || plane == X_AXIS)
		//			testZ = testZ 
		//			|| 2 * sol.zl * box.denom == (2*box.zl+1) * sol.denom;

		switch(sol.type)
		{
		case X_AXIS:
			return testY && testZ;
		case Y_AXIS:
			return testX && testZ;
		case Z_AXIS:
			return testX && testY;
		case FACE_LL: case FACE_RR:
			return testX;
		case FACE_FF: case FACE_BB:
			return testY;
		case FACE_UU: case FACE_DD:
			return testZ;
		default:
			BoxClevA.log.printf("sol_on_bny_halfplane bad Key3D %s %s\n",sol.type,plane);
			return true;
		}
		//return false;
	}

	private boolean sol_on_box_boundary(Box_info box,Sol_info sol)
	{
		return sol_on_box_boundary_or_halfplane(box,sol,NONE);
	}


	private Facet_sol link_on_facet(Facet_info  facet2,Facet_sol f1a,Facet_sol f1b)
	{
		Facet_sol f2a,f2b;

		for(f2a = facet2.sols;f2a!=null;f2a=f2a.next)
		{
			f2b = f2a.next; if(f2b==null) f2b=facet2.sols;

			if(f1a.sol == f2a.sol && f1b.sol == f2b.sol )
				return(f2a);
			if(f1a.sol == f2b.sol && f1b.sol == f2a.sol )
				return(f2a);
		}
		return null;
	}

	private Facet_info link_facet2(Facet_info facet1,Facet_info facet2)
	{
		if(facet1.dx != facet2.dx || facet1.dy != facet2.dy || facet1.dz != facet2.dz )
			return null;

		CyclicList<Sol_info> solsA = facet1.getSols();
		CyclicList<Sol_info> solsB = facet2.getSols();
		
		List<Sol_info> matches = new ArrayList<>();
		
		for(Sol_info s1:solsA) {
			if(solsB.contains(s1))
				matches.add(s1);
		}
		Sol_info start,end; 
		// start and end of chain linking matching sols in forward direction
		//
		//       --- Start ----
		//             |
		//  Facet1  |  |  | same
		//          V  |  V  dir
		//             |
		//       ---  End  ----
		//
		boolean sameDirection;
		switch(matches.size()) {
		case 0:
			return null;
		case 1:
			return null;
		case 2:
		{
			Sol_info pt1 = matches.get(0);
			Sol_info pt2 = matches.get(1);
			Direction dirA = solsA.adjacent(pt1, pt2);			
			switch(dirA) {
			case Forward:
				start = pt1;
				end = pt2;
				break;
			case Backward:
				start = pt2;
				end = pt1;
				break;
			case Identical:
			case NoFound:
			case Separate:
			default:
				if(failCountH++==0) {
				BoxClevA.log.println("Error with link_facet");
				BoxClevA.log.println(facet1);
				BoxClevA.log.println(facet2);
				}
				return null;
			}
			Direction dirB = solsB.adjacent(start, end);

			switch(dirB) {
			case Backward:
				sameDirection =false;
				break;
			case Forward:
				sameDirection =true;
				break;
			case Identical:
			case NoFound:
			case Separate:
			default:
				if(failCountH++==0) {
				BoxClevA.log.println("Error with link_facet");
				BoxClevA.log.println(facet1);
				BoxClevA.log.println(facet2);
				}
				return null;			
			}
			break;
		}
		default:
		{
			// multiple matches
			start = end = matches.get(0);
			int length=1;
			Iterator<Sol_info> itt = solsA.forwardIteratorFrom(start);
			itt.next();
			while(itt.hasNext()) {
				Sol_info sol = itt.next();
				if(matches.contains(sol)) {
					end = sol;
					++length;
				} else {
					break;
				}
			}
			Iterator<Sol_info> bitt = solsA.backwardIteratorFrom(start);
			bitt.next();
			while(bitt.hasNext()) {
				Sol_info sol = bitt.next();
				if(matches.contains(sol)) {
					start = sol;
					++length;
				} else {
					break;
				}
			}
			if(length!=matches.size()) {
				if(failCountI++==0) {
					BoxClevA.log.println("Not all matches in chain");
				}
				return null;
			}
			Sol_info poststart = solsA.nextCyclic(start);
			switch(solsB.adjacent(start, poststart)) {
			case Backward:
				sameDirection =false;
				break;
			case Forward:
				sameDirection =true;
				break;
			case Identical:
			case NoFound:
			case Separate:
			default:
				if(failCountQ++==0) {
				BoxClevA.log.println("Poststart should be on second facet");
				BoxClevA.log.println(facet1);
				BoxClevA.log.println(facet2);					
				}
				return null;
			}
		}
		} // end of case
		
		Facet_info facet3 = new Facet_info();
		Iterator<Sol_info> ittB;
		if( sameDirection ) {
			ittB = solsB.backwardIteratorFromTo(start,end);
		} else {
			ittB = solsB.forwardIteratorFromTo(start,end);
		}
		while(ittB.hasNext()) {
			facet3.addSol(ittB.next());
		}			
		Iterator<Sol_info> ittA = solsA.forwardIteratorFromTo(end,start);
		while(ittA.hasNext()) {
			facet3.addSol(ittA.next());
		}			

		return facet3;
	}
	
	/** if facet1 and facet2 extend facet2 and return true. **/

	private Facet_info link_facet(Facet_info facet2,Facet_info facet1)
	{
		Facet_sol f1a,f1b = null,f2a,f2b,fs1,fs2;
		Facet_info facet3;
		int orientation = 0;
		boolean include_next_point;
		boolean missed_prev_point;

		List<Facet_sol> matchedSols = new ArrayList<>();
		if(PRINT_JOIN_FACETS){
			BoxClevA.log.printf("link_facet:\n");
		}
		f2a = null;

		if(facet1.dx != facet2.dx || facet1.dy != facet2.dy || facet1.dz != facet2.dz )
			return null;

		for(f1a = facet1.sols;f1a!=null;f1a=f1a.next)
		{
			f1b = f1a.next; if(f1b==null) f1b=facet1.sols;

	//		if((!f1a.sol.type.isEdge() && !f1b.sol.type.isEdge()))
	//				continue;
			f2a = link_on_facet(facet2,f1a,f1b);
			if(f2a!=null) {
				matchedSols.add(f2a);
				matchedSols.add(f1a);
				matchedSols.add(f1b);
			}
		}
		if(matchedSols.isEmpty())
			return null;
		
		if(PRINT_JOIN_FACETS){ 
		if(matchedSols.size()>3) {
			if(failCountJ++==0) {
			BoxClevA.log.println("Multiple matches of facets");
			BoxClevA.log.println(facet1);
			BoxClevA.log.println(facet2);
			}
		}
		}
		
		f2a = matchedSols.get(0);
		f1a = matchedSols.get(1);
		f1b = matchedSols.get(2);
		
		if(PRINT_JOIN_FACETS){
			BoxClevA.log.printf("linking_facet:\n");
			BoxClevA.log.println(facet1);
			BoxClevA.log.println(facet2);
		}
		f2b = f2a.next; if(f2b==null) f2b=facet2.sols;

		if(f1a.sol == f2a.sol && f1b.sol == f2b.sol )
			orientation = -1;
		if(f1a.sol == f2b.sol && f1b.sol == f2a.sol )
			orientation = 1;

		/* now add all soln from facet2 add all sols from facet1
				if a sol is on a linking edge and a non linking edge
				add for facet2 but not for facet1 */

		//		if(! test_coherent_signs(facet1,facet2)) return null;

		facet3 = new Facet_info();
		facet3.dx = facet1.dx;
		facet3.dy = facet1.dy;
		facet3.dz = facet1.dz;

		fs1 = f2b;
		missed_prev_point = true;
		while(true)
		{
			fs2 = fs1.next; if(fs2==null) fs2=facet2.sols;
			if(/* is_node_link(fs1.sol,fs2.sol)
				 || */ link_on_facet(facet1,fs1,fs2) == null)
			{
				if(missed_prev_point)
					facet3.addSol(fs1.sol);
				facet3.addSol(fs2.sol);
				missed_prev_point = false;
			}
			else
				missed_prev_point = true;
			fs1 = fs2;
			if(fs1==f2a) break;
		}
		include_next_point = false;
		fs1 = f1b;
		while(true)
		{
			fs2 = fs1.next; if(fs2==null) fs2=facet1.sols;
			if(/* is_node_link(fs1.sol,fs2.sol)
				 || */ link_on_facet(facet2,fs1,fs2) == null)
			{
				if(include_next_point)
				{
					if(orientation==1)
						facet3.addSol(fs1.sol);
					else
						facet3.add_sol_to_facet_backwards(fs1.sol);
				}
				include_next_point = true;
			}
			else
				include_next_point = false;
			fs1 = fs2;
			if(fs1==f1a) break;
		}
		if(PRINT_JOIN_FACETS){
			BoxClevA.log.printf("linking_facet: done\n");
			BoxClevA.log.println(facet3);
		}
		return(facet3);
	}

	private boolean checkSame(Facet_info facet1,Facet_info facet2) {
		if(facet2==null) return false;
		if(facet1.size() != facet2.size())
			return false;
		
		Iterator<Sol_info> solsA = facet1.solsItt().iterator();
		Sol_info start = solsA.next();
		Iterator<Sol_info> solsB = facet2.getSols().forwardIteratorFrom(start);
		if(!solsB.hasNext()) return false;
		Sol_info start2 = solsB.next();
		if(start!=start2)
			return false;
		while(solsA.hasNext() && solsB.hasNext()) {
			Sol_info A = solsA.next();
			Sol_info B = solsB.next();
			if(A!=B)
				return false;
		}
		if(solsA.hasNext()) return false;
		if(solsB.hasNext()) return false;
		return true;
	}

	private void include_facet(List<Facet_info> existing,Facet_info facet1)
	{
		Facet_info target=facet1;

		if(PRINT_JOIN_FACETS){
			BoxClevA.log.printf("inc_facet\n");
			BoxClevA.log.println(facet1);
		}

		boolean flag = true;
		while(flag) {
			flag = false;
			ListIterator<Facet_info> li = existing.listIterator(); 
			while(li.hasNext())
			{
				Facet_info facet2 = li.next();
				Facet_info facet3 = link_facet(facet2,target);
				if(facet3!=null) {
					Facet_info facet4 = link_facet2(facet2,target);
					if(!checkSame(facet3,facet4)) {
						if(failCountK++==0) {
							BoxClevA.log.println("Different link faces results");
							BoxClevA.log.println(facet2);
							BoxClevA.log.println(target);
							BoxClevA.log.println(facet3);
							BoxClevA.log.println(facet4);
						}
					}
					else {
					target=facet4!=null ? facet4 : facet3;
					li.remove();
					flag=true;
					}
				}
			}
		}
		existing.add(target);
	}

	private void include_facets(List<Facet_info> facetlist,List<Facet_info> boxfacets)
	{
		if(boxfacets==null) return;
		for(Facet_info facet1:boxfacets)
		{
			if(PRINT_JOIN_FACETS){
				BoxClevA.log.println(facet1);
			}
			include_facet(facetlist,facet1);
		}
		return;
	}

	/*
	 * Function:	combine_facets
	 * Action:	combines all the facets in the sub boxes
	 *		to form the facets of the main box.
	 *		Removes the facets from the sub boxes.
	 */

	private void combine_facets(Box_info box)
	{

		if(PRINT_COMBINE_FACETS){
			BoxClevA.log.printf("Combine facets (%d,%d,%d)/%d\n",box.xl,box.yl,box.zl,box.denom);
			BoxClevA.log.printf("lfd "); print_facets(box.lfd.facets);
			BoxClevA.log.printf("lfu "); print_facets(box.lfu.facets);
			BoxClevA.log.printf("lbd "); print_facets(box.lbd.facets);
			BoxClevA.log.printf("lbu "); print_facets(box.lbu.facets);
			BoxClevA.log.printf("rfd "); print_facets(box.rfd.facets);
			BoxClevA.log.printf("rfu "); print_facets(box.rfu.facets);
			BoxClevA.log.printf("rbd "); print_facets(box.rbd.facets);
			BoxClevA.log.printf("rbu "); print_facets(box.rbu.facets);
		}
		box.facets = new ArrayList<Facet_info>();
		
		if(box.denom > 128) {
		include_facets(box.facets,box.lbd.facets);
		include_facets(box.facets,box.lbu.facets);
		include_facets(box.facets,box.lfd.facets);
		include_facets(box.facets,box.lfu.facets);
		include_facets(box.facets,box.rbd.facets);
		include_facets(box.facets,box.rbu.facets);
		include_facets(box.facets,box.rfd.facets);
		include_facets(box.facets,box.rfu.facets);
		}
		else {
			include_facets(box.facets,box.lbd.facets);
			include_facets(box.facets,box.lbu.facets);
			include_facets(box.facets,box.lfd.facets);
			include_facets(box.facets,box.lfu.facets);
			include_facets(box.facets,box.rbd.facets);
			include_facets(box.facets,box.rbu.facets);
			include_facets(box.facets,box.rfd.facets);
			include_facets(box.facets,box.rfu.facets);
			
		}
		box.lbd.facets = null;
		box.lbu.facets = null;
		box.lfd.facets = null;
		box.lfu.facets = null;
		box.rbd.facets = null;
		box.rbu.facets = null;
		box.rfd.facets = null;
		box.rfu.facets = null;

		if(PRINT_COMBINE_FACETS){
			BoxClevA.log.printf("Combine facets done\n");
			print_facets(box.facets);
		}

	}


	private void print_facets(List<Facet_info> facets) {
		if(facets==null) {
			BoxClevA.log.println("No facets");
			return;
		}
		for(Facet_info facet:facets)
			BoxClevA.log.print(facet);
	}

	/********** Construct cycles round the boundary of box **************/



	/**
	 * Starting from a link, loop all the way round until
	 *		you get back to the beginning.
	 */

	private void create_facet(Link_info startlink,List<Link_info> list)
	{
		Link_info link;
		Sol_info startingsol,presentsol;
		Facet_info f;

		/* Now have a link which has not been plotted */

		link = startlink;

		f = add_facet();
		f.addSol(link.A);
		f.addSol(link.B);

		link.plotted = true;
		startingsol = link.A;
		presentsol = link.B;

		while( presentsol != startingsol )
		{
			boolean triedAll=true;
			for(Link_info link2:list) {
				if(link2.plotted) continue;

				if(link2.A == presentsol) {// && f.matches_derivs(link2.B)) {
					if(link2.B != startingsol) 
						f.addSol(link2.B);
					presentsol = link2.B;
					link2.plotted = true;
					triedAll=false;
					break;
				}
				if(link2.B == presentsol) { //) && f.matches_derivs(link2.A)) {
					if(link2.A != startingsol) 
						f.addSol(link2.A);
					presentsol = link2.A;
					link2.plotted = true;
					triedAll=false;
					break;
				}
			}
			if(triedAll) {
				if(failCountL++==0) {
					BoxClevA.log.println("create_facet: no matching link found");
					BoxClevA.log.println(startingsol);
					BoxClevA.log.println(presentsol);
					BoxClevA.log.print(list);
				}
				break;
			}
		}
	}

	private void create_3node_link_facets(Box_info box)
	{

		int i=0;
		if(box.node_links!=null) {
			i = box.node_links.size();
			//		    for(Node_link_info nl:box.node_links) ++i;
		}
		if(i<3) return;
		//TODO
	}

	/********** Main entry point for routines 
	 * @throws AsurfException *****************/


	public void make_facets(Box_info box) throws AsurfException
	{
		all_facets = null;

//		if(boxclev.knitFacets) {
//			make_kitted_facets(box);			
//		} else 
		if(box.lfd == null) {
			make_facets_leaf(box);
		} else
			make_facets_recurse(box);
		
		if(box.facets==null)
			return;
		
		boolean repeated=false;
		for(Facet_info facet:box.facets) {
			if(facet.has_repeated_sol()) {
				if(failCountM++==0) {
				BoxClevA.log.println("two identical sols in facet");
				BoxClevA.log.println(box.print_box_header());
				BoxClevA.log.println(facet);
				}
				repeated = true;
			}
			if(!facet.has_coherent_signs()) {
				if(failCountN++==0) {
				BoxClevA.log.println("wrong signs for facet");
				BoxClevA.log.println(box.print_box_header());
				BoxClevA.log.println(facet); 
				}
				
			}
		}
		if(repeated)
			fix_repeated_sols(box);
	}
	


 	
	private void fix_repeated_sols(Box_info box) {
		Iterator<Facet_info> itt = box.facets.listIterator();
		while(itt.hasNext()) {
			Facet_info f1 = itt.next();
			if(f1.has_repeated_sol()) {
				Facet_info f2 = this.split_facet_by_reapeated_sol(f1);
				box.facets.add(f2);
				fix_repeated_sols(box);
				return;
			}
		}
	}

	private void make_facets_leaf(Box_info box) {
		
		box.collect_sings();

		make_chains(box);

		if(box.chains!=null ) {
		ListIterator<Chain_info> li=box.chains.listIterator();
		while(li.hasNext())
		{
			Chain_info chain1 = li.next();
			if(!chain1.has_coherent_signs()) {
				if(failCountO++==0) {
					BoxClevA.log.println("Chain with sign conflict");
					BoxClevA.log.println(chain1);
				}
			}
			
		}		
		}
		if(PRINT_DRAW_BOX){
			BoxClevA.log.printf("\nmake_facets_leaf: box (%d,%d,%d)/%d\n",
					box.xl,box.yl,box.zl,box.denom);
			BoxClevA.log.println(box.toString_brief());
			print_chains(box.chains);
		}

		/*** First find a link to start from. ***/

		List<Link_info> listOfAllLinks = get_all_links_on_box(box);

		for(Link_info link:listOfAllLinks)
			if(!(link.plotted) )
				create_facet(link,listOfAllLinks);

		if(PRINT_DRAW_BOX){
			BoxClevA.log.printf("Leaf: Initial facets\n");
			print_facets(all_facets);
		}
		/* Now divide up the facets */

		create_3node_link_facets(box);

		refine_facets(box);

		if(PRINT_DRAW_BOX){
			BoxClevA.log.printf("Facet before clean\n");
			print_facets(all_facets);
		}

		box.facets = all_facets;
		all_facets = null;

		clean_facets(box);

		
		if(PRINT_DRAW_BOX){
			BoxClevA.log.printf("Facet after clean\n");
			print_facets(box.facets);
		}
	}

	/**
	 * Make the facets using specified list of faces. 
	 * @param box
	 * @param plotFaces
	 */
	public void make_facets(Box_info box, List<Face_info> plotFaces) {
		box.collect_sings();

		make_chains(box);

		if(box.chains!=null ) {
			ListIterator<Chain_info> li=box.chains.listIterator();
			while(li.hasNext())
			{
				Chain_info chain1 = li.next();
				if(!chain1.has_coherent_signs()) 
				{
					if(failCountO++==0) {
						BoxClevA.log.println("Chain with sign conflict");
						BoxClevA.log.println(chain1);
					}
				}		
			}		
		}
		if(PRINT_DRAW_BOX){
			BoxClevA.log.printf("\nmake_facets_leaf: box (%d,%d,%d)/%d\n",
					box.xl,box.yl,box.zl,box.denom);
			BoxClevA.log.println(box.toString_brief());
			print_chains(box.chains);
		}

		List<Link_info> listOfAllLinks= get_all_links_from_faces(plotFaces);
		listOfAllLinks.forEach(link -> link.plotted=false);
		for(Link_info link:listOfAllLinks)
			if(!(link.plotted) )
				create_facet(link,listOfAllLinks);

		if(PRINT_DRAW_BOX){
			BoxClevA.log.printf("Leaf: Initial facets\n");
			print_facets(all_facets);
		}
		/* Now divide up the facets */

		create_3node_link_facets(box);

		refine_facets(box);

		if(PRINT_DRAW_BOX){
			BoxClevA.log.printf("Facet before clean\n");
			print_facets(all_facets);
		}

		box.facets = all_facets;
		all_facets = null;

//		clean_facets(box);

		if(PRINT_DRAW_BOX){
			BoxClevA.log.printf("Facet after clean\n");
			print_facets(box.facets);
		}

	}

	private List<Link_info> get_all_links_from_faces(List<Face_info> plotFaces) {
		List<Link_info> list = new ArrayList<>();
		
		plotFaces.forEach(face -> add_links_from_face(face,list));
		return list;
	}

	
	
	private Object add_links_from_face(Face_info face, List<Link_info> list) {
		if(face.links != null) {
			list.addAll( face.links );
		}
		return list;
	}
	
	

	private List<Link_info> get_all_links_on_box(Box_info box) {
		List<Link_info> listOfAllLinks = new ArrayList<Link_info>();
		if(box.ll != null && box.ll.links!=null)
			listOfAllLinks.addAll(box.ll.links);
		if(box.rr != null && box.rr.links!=null)
			listOfAllLinks.addAll(box.rr.links);
		if(box.ff != null && box.ff.links!=null)
			listOfAllLinks.addAll(box.ff.links);
		if(box.bb != null && box.bb.links!=null)
			listOfAllLinks.addAll(box.bb.links);
		if(box.uu != null && box.uu.links!=null)
			listOfAllLinks.addAll(box.uu.links);
		if(box.dd != null && box.dd.links!=null)
			listOfAllLinks.addAll(box.dd.links);
		
		for(Link_info link:listOfAllLinks)
			link.plotted=false;

		return listOfAllLinks;
	}

	private void make_facets_recurse(Box_info box) throws AsurfException {
		make_facets(box.lfd);
		make_facets(box.lfu);
		make_facets(box.lbd);
		make_facets(box.lbu);
		make_facets(box.rfd);
		make_facets(box.rfu);
		make_facets(box.rbd);
		make_facets(box.rbu);

			if(PRINT_DRAW_BOX){
				BoxClevA.log.printf("Combining Facets for \n");
				box.toString_brief();
				//					print_facets(box.facets);
			}
		
			combine_facets(box);
			clean_facets(box);
	
		if(PRINT_DRAW_BOX){
			BoxClevA.log.printf("Facet after clean\n");
			print_facets(box.facets);
		}
		return;
	}


	/**
	 * Does a final cleanup of facets
	 * removes thin identical facets
	 * removes any sols not on boundary
	 * which are only on two facets.
	 */

	private void clean_facets(Box_info box)
	{
		boolean more_to_do;
		Facet_sol fs1,fs2;

		if(box.facets==null)
			return;

		CountingListMap<Sol_info,Facet_info> hit_count = new CountingListMap<>();

		for(Facet_info facet1:box.facets)
		{
			for(Sol_info sol: facet1.solsItt()) {
				if(sol.type.compareTo(FACE_UU)<=0
						&& !sol_on_box_boundary(box,sol) ) {
					hit_count.add(sol,facet1);
				}
			}
		}
		
		Iterator<Entry<Sol_info, List<Facet_info>>> itt = hit_count.entrySet().iterator();
		while(itt.hasNext()) {
			Entry<Sol_info, List<Facet_info>> ent = itt.next();
			List<Facet_info> list = ent.getValue();
			switch(list.size()) {
			case 2: {
				Facet_info f1 = list.get(0);
				Facet_info f2 = list.get(1);
				Sol_info delsol = ent.getKey();
				Sol_info f1p = f1.prevSol(delsol);
				Sol_info f1n = f1.nextSol(delsol);
				
				Sol_info f2p = f2.prevSol(delsol);
				Sol_info f2n = f2.nextSol(delsol);
	
	//			if(f1.size() > 3 && f2.size() > 3) {
	//				f1.remove_sol_from_facet(delsol);
	//				f2.remove_sol_from_facet(delsol);				
	//			}
				if( ( f1p == f2p || f1p == f2n)
				 && ( f1n == f2p || f1n == f2n) ) {
					f1.remove_sol_from_facet(delsol);
					f2.remove_sol_from_facet(delsol);
					
					if(f1.sol_on_facet(delsol)) {
						if(failCountP++==0) {
							BoxClevA.log.println("Deleted Sol still on facet");
							BoxClevA.log.println(delsol);
							BoxClevA.log.println(f1);
						}
					}
					if(f2.sol_on_facet(delsol)) {
						if(failCountP++==0) {
							BoxClevA.log.println("Deleted Sol still on facet");
							BoxClevA.log.println(delsol);
							BoxClevA.log.println(f2);
						}
					}
					
				}
			}
			break;
			case 1: {
				Facet_info f1 = list.get(0);
				Sol_info delsol = ent.getKey();
				f1.remove_sol_from_facet(delsol);				
			}
			break;
			default:

			}
		}

		more_to_do =true;
		while(more_to_do)
		{
			more_to_do = false;
			Facet_info removeThis=null;
			ListIterator<Facet_info> it1 = box.facets.listIterator();
			outer: while(it1.hasNext()) {
				Facet_info facet1 = it1.next();
				int nextIndex = it1.nextIndex();
				ListIterator<Facet_info> it2 = box.facets.listIterator(nextIndex);
				while(it2.hasNext()) {
					Facet_info facet2 = it2.next();
					// 			for(Facet_info facet1:box.facets)
					//			for(facet1=box.facets;facet1!=null;facet1=facet1.next)
					//			{
					//				for(facet2=facet1.next;facet2!=null;facet2=facet2.next)
					//				{
					boolean matched_all_sols = true;

					for(fs1=facet1.sols;fs1!=null;fs1=fs1.next)
					{
						boolean matched_sol=false;
						for(fs2=facet2.sols;fs2!=null;fs2=fs2.next)
							if(fs1.sol == fs2.sol)
							{	
								matched_sol = true; 
								break;
							}
						if(!matched_sol)
						{
							matched_all_sols = false;
							break;
						}
					}

					for(fs1=facet2.sols;fs1!=null;fs1=fs1.next)
					{
						boolean matched_sol=false;
						for(fs2=facet1.sols;fs2!=null;fs2=fs2.next)
							if(fs1.sol == fs2.sol)
							{	
								matched_sol = true; 
								break;
							}
						if(!matched_sol)
						{
							matched_all_sols = false;
							break;
						}
					}

					if(matched_all_sols)
					{
						/* now remove facet1 and facet2 from list */
						it1.remove();
						removeThis = facet2;
						more_to_do = true;
						break outer;
					}
					if(more_to_do) break;
				} /* end facet2 loop */
				if(more_to_do) break;
			} /* end facet1 loop */
			if(removeThis!=null)
				box.facets.remove(removeThis);
		}
		return;
		//fix_crossing_gaps(box);
	}


	public String toString() {
		StringBuilder sb = new StringBuilder();
		if( this.all_facets != null) {
			for(Facet_info facet:this.all_facets) {
				sb.append(facet.toString());
			}
		}
		return sb.toString();
	}
	
	
	public static class Tester {

		public Facets facets;
		private BoxClevJavaView boxclev;

		@Before
		public void setUp() throws Exception {
			boxclev = new  BoxClevJavaView(null, null, null, null,"");
			facets = new Facets(boxclev);
			BoxClevA.unsafeRegion = new Region_info(0,1,0,1,0,1);
//			Bern3DContext ctx = new Bern3DContext(1, 1, 1);
//			BoxClevA.unsafeBern =  ctx.zeroBern();
		}

		
		
		@Test
		public void testSplitDerivBug1() {
			Facet_info facet1 = new Facet_info();
		
			Sol_info s1 = new Sol_info(Key3D.BOX,1, 0, 0, 8, 0);
			s1.setDerivs(0, 0, 0);
			Sol_info s2 = new Sol_info(Key3D.BOX,2, 0, 0, 8, 0);
			s2.setDerivs(0, 1, 1);
			Sol_info s3 = new Sol_info(Key3D.BOX,3, 0, 0, 8, 0);
			s3.setDerivs(-1,1,1);
			Sol_info s4 = new Sol_info(Key3D.BOX,4, 0, 0, 8, 0);
			s4.setDerivs(-1,1,-1);
		
			facet1.addSol(s4);
			facet1.addSol(s3);
			facet1.addSol(s2);
			facet1.addSol(s1);
			
			List<Facet_info> res = facets.split_facet_by_derivs(facet1);
			BoxClevA.log.println(res);
			if(res!=null)
			for(Facet_info f1:res) {
				assertTrue("Facets must be smaller than orig",f1.size() < facet1.size());
			}
			assertTrue("Should never produce an input facet with this signature",false);
		}

		
		@Test
		public void testSplitDerivBug2() {
			Facet_info facet1 = new Facet_info();
		
			Sol_info s1 = new Sol_info(Key3D.BOX,1, 0, 0, 8, 0);
			s1.setDerivs(0, -1, 1);
			Sol_info s2 = new Sol_info(Key3D.BOX,2, 0, 0, 8, 0);
			s2.setDerivs(-1,-1,1);
			Sol_info s3 = new Sol_info(Key3D.BOX,3, 0, 0, 8, 0);
			s3.setDerivs(-1, 0, 1);
			Sol_info s4 = new Sol_info(Key3D.BOX,4, 0, 0, 8, 0);
			s4.setDerivs(-1, 1, 1);
			Sol_info s5 = new Sol_info(Key3D.BOX,5, 0, 0, 8, 0);
			s5.setDerivs(0, 1, 1);
			Sol_info s6 = new Sol_info(Key3D.BOX,6, 0, 0, 8, 0);
			s6.setDerivs(1, 0, 1);
		
			facet1.addSol(s6);
			facet1.addSol(s5);
			facet1.addSol(s4);
			facet1.addSol(s3);
			facet1.addSol(s2);
			facet1.addSol(s1);
			
			List<Facet_info> res = facets.split_facet_by_derivs(facet1);
			assertEquals(2,res.size());
			Facet_info f1 = res.get(0);
			Facet_info f2 = res.get(1);
			assertTrue(f1.has_coherent_signs());

			List<Facet_info> res2 = facets.split_facet_by_derivs(f2);

			BoxClevA.log.println(res2);
			for(Facet_info f3:res2) {
				assertTrue(f3.has_coherent_signs());
			}
		}

	@Test
		public void testBug1() {
			Facet_info facet1 = new Facet_info();
		
			Sol_info s1 = new Sol_info(Key3D.BOX,1, 0, 0, 8, 0);
			Sol_info s2 = new Sol_info(Key3D.BOX,2, 0, 0, 8, 0);
			Sol_info s3 = new Sol_info(Key3D.BOX,3, 0, 0, 8, 0);
			Sol_info s4 = new Sol_info(Key3D.BOX,4, 0, 0, 8, 0);
			Sol_info s5 = new Sol_info(Key3D.BOX,5, 0, 0, 8, 0);
		
			Sol_info s6 = new Sol_info(Key3D.BOX,6, 0, 0, 8, 0);		
			Sol_info s7 = new Sol_info(Key3D.BOX,7, 0, 0, 8, 0);
			Sol_info s8 = new Sol_info(Key3D.BOX,8, 0, 0, 8, 0);
			Sol_info s9 = new Sol_info(Key3D.BOX,9, 0, 0, 8, 0);
		
			facet1.addSol(s8);
			facet1.addSol(s7);
			facet1.addSol(s6);
			facet1.addSol(s5);
			facet1.addSol(s4);
			facet1.addSol(s3);
			facet1.addSol(s2);
			facet1.addSol(s1);
			
			Facet_info facet2 = new Facet_info();
			facet2.addSol(s4);
			facet2.addSol(s5);
			facet2.addSol(s6);
			facet2.addSol(s9);
			
			Facet_info facet3 = facets.link_facet(facet1, facet2);
			Facet_info facet4 = facets.link_facet2(facet1, facet2);
			
			BoxClevA.log.println(facet1);
			BoxClevA.log.println(facet2);
			BoxClevA.log.println(facet3);
			BoxClevA.log.println(facet4);
			checkSame(facet3,facet4);
		}

	@Test
		public void testChubBug1() {
			Facet_info facet1 = new Facet_info();
		
			Sol_info s1 = new Sol_info(Key3D.BOX,1, 0, 0, 8, 0);
			Sol_info s2 = new Sol_info(Key3D.BOX,2, 0, 0, 8, 0);
			Sol_info s3 = new Sol_info(Key3D.BOX,3, 0, 0, 8, 0);
			Sol_info s4 = new Sol_info(Key3D.BOX,4, 0, 0, 8, 0);
			Sol_info s5 = new Sol_info(Key3D.BOX,5, 0, 0, 8, 0);
		
			Sol_info s6 = new Sol_info(Key3D.BOX,6, 0, 0, 8, 0);		
			Sol_info s7 = new Sol_info(Key3D.BOX,7, 0, 0, 8, 0);
			Sol_info s8 = new Sol_info(Key3D.BOX,8, 0, 0, 8, 0);
			Sol_info s9 = new Sol_info(Key3D.BOX,9, 0, 0, 8, 0);
		
			facet1.addSol(s5);
			facet1.addSol(s4);
			facet1.addSol(s3);
			facet1.addSol(s2);
			facet1.addSol(s1);
			
			Facet_info facet2 = new Facet_info();
			facet2.addSol(s2);
			facet2.addSol(s1);
			facet2.addSol(s9);
			facet2.addSol(s8);
			facet2.addSol(s7);
			facet2.addSol(s6);
			facet2.addSol(s5);
			facet2.addSol(s4);
			facet2.addSol(s3);
			
			Facet_info facet3 = facets.link_facet(facet2, facet1);
			Facet_info facet4 = facets.link_facet2(facet2, facet1);
			
			BoxClevA.log.println(facet2);
			BoxClevA.log.println(facet1);
			BoxClevA.log.println(facet3);
			BoxClevA.log.println(facet4);
			checkSame(facet3,facet4);
		}

	@Test
	public void test_link_facets() {
		Sol_info s1 = new Sol_info(Key3D.BOX,1, 0, 0, 8, 0);
		Sol_info s2 = new Sol_info(Key3D.BOX,2, 0, 0, 8, 0);
		Sol_info s3 = new Sol_info(Key3D.BOX,3, 0, 0, 8, 0);
		Sol_info s4 = new Sol_info(Key3D.BOX,4, 0, 0, 8, 0);
		Sol_info s5 = new Sol_info(Key3D.BOX,5, 0, 0, 8, 0);
	
		Sol_info s6 = new Sol_info(Key3D.BOX,6, 0, 0, 8, 0);		
		Sol_info s7 = new Sol_info(Key3D.BOX,7, 0, 0, 8, 0);
		Sol_info s8 = new Sol_info(Key3D.BOX,8, 0, 0, 8, 0);
	//		Sol_info I = new Sol_info(Key3D.BOX,9, 0, 0, 8, 0);
	//		Sol_info J = new Sol_info(Key3D.BOX,10, 0, 0, 8, 0);
	//		Sol_info K = new Sol_info(Key3D.BOX,11, 0, 0, 8, 0);
	//		Sol_info L = new Sol_info(Key3D.BOX,12, 0, 0, 8, 0);
		
		Facet_info facet1 = new Facet_info();
		facet1.addSol(s1);
		facet1.addSol(s2);
		facet1.addSol(s3);
		facet1.addSol(s4);
		facet1.addSol(s5);
		
		Facet_info facet2 = new Facet_info();
		facet2.addSol(s6);
		facet2.addSol(s3); // match
		facet2.addSol(s4); // match
		facet2.addSol(s7);
		facet2.addSol(s8);
		
		//      A - B - C - F - G
		//      |       |       |
		//      E ----- D ----- H
		//
		//                 *
		//      A - B - C  F - G
		//      |              |  
		//      E ----- D ---- H
		
		Facet_info facet3 = facets.link_facet(facet1, facet2);
		BoxClevA.log.println(facet3);
		
		Facet_info facet4 = facets.link_facet2(facet1, facet2);
		BoxClevA.log.println(facet4);
		
		checkSame(facet3,facet4);
	
		//      C - D - E - F - G
		//      |       |       |
		//      B ----- A ----- H
		//
		//                 *
		//      A - B - C  F - G
		//      |              |  
		//      E ------------ H
	
		facet1 = new Facet_info();
		facet1.addSol(s5);
		facet1.addSol(s4);
		facet1.addSol(s3);
		facet1.addSol(s2);
		facet1.addSol(s1);
	
		Facet_info facet5 = new Facet_info();
		facet5.addSol(s5);
		facet5.addSol(s6);
		facet5.addSol(s7);
		facet5.addSol(s8);
		facet5.addSol(s1);
		
		Facet_info facet6 = facets.link_facet(facet1, facet5);
		Facet_info facet7 = facets.link_facet2(facet1, facet5);
		System.out.println(facet6);
		System.out.println(facet7);
		checkSame(facet6,facet7);
		
	}

	public void checkSame(Facet_info facet1,Facet_info facet2) {
		Iterator<Sol_info> solsA = facet1.solsItt().iterator();
		Sol_info start = solsA.next();
		Iterator<Sol_info> solsB = facet2.getSols().forwardIteratorFrom(start);
		assertTrue(solsB.hasNext());
		Sol_info start2 = solsB.next();
		assertEquals(start,start2);
		while(solsA.hasNext() && solsB.hasNext()) {
			Sol_info A = solsA.next();
			Sol_info B = solsB.next();
			assertEquals(A,B);
		}
		assertFalse(solsA.hasNext());
		assertFalse(solsB.hasNext());
	}

		
	}


	
}