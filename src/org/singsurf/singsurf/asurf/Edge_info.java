/*
Created 14 Jun 2010 - Richard Morris
 */
package org.singsurf.singsurf.asurf;

import java.util.List;

public class Edge_info {
	private static final boolean PRI_SPLIT_EDGE = false;
	public int xl,yl,zl,denom;
	public Key3D type;
	public int status;
	short refcount;
	public Sol_info sol;
	public Edge_info left,right;

	/*
	 * Function:	make_edge
	 *		define an edge
	 */

	public Edge_info(Key3D type, int xl,int yl,int zl,int denom)
	{
		this.type = type;
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.status = BoxClevA.EMPTY;
		this.sol =  null;
		this.left = this.right =  null;
	}

	public Edge_info[]  subdevideedge()
	{
		Edge_info edge1=null;
		Edge_info edge2=null;
		
		switch(type)
		{
		case X_AXIS: 
			edge1 = new Edge_info(this.type,this.xl*2,
				this.yl*2,this.zl*2,this.denom*2);
			edge2 = new Edge_info(this.type,this.xl*2 + 1,
				this.yl*2,this.zl*2,this.denom*2);
			break;
		case Y_AXIS: 
			edge1 = new Edge_info(this.type,this.xl*2,
				this.yl*2,this.zl*2,this.denom*2);
			edge2 = new Edge_info(this.type,this.xl*2,
				this.yl*2 + 1,this.zl*2,this.denom*2);
			break;
		case Z_AXIS: 
			edge1 = new Edge_info(this.type,this.xl*2,
				this.yl*2,this.zl*2,this.denom*2);
			edge2 = new Edge_info(this.type,this.xl*2,
				this.yl*2,this.zl*2 + 1,this.denom*2);
			break;
		default:
			System.err.printf("bad type %d in subdevideedge\n",this.type);
		    throw new IllegalArgumentException("subdevideedge" +type.toString());
		}
		return new Edge_info[]{edge1,edge2};
	}


	/*
	 * Function:	split_edge
	 * action:	ensures that edge comprises of two halves and that
	 *		if a solution exists then it lies in one of the two
	 *		halves.
	 */

	void split_edge()
	{
		if((left == null && right!=null ) || (right==null && left!=null ) ) {
		    throw new IllegalStateException("split_edge l "+left+" r "+right);			
		}
			
		if( left == null )
		{
			left = new Edge_info(type,xl*2,yl*2,zl*2,denom*2);
			left.status = status;
		}

		if( right == null )
		{

			switch( type )
			{
			case X_AXIS: 
				right = new Edge_info(type,xl*2 + 1,yl*2,zl*2,denom*2);
				break;
			case Y_AXIS: 
				right = new Edge_info(type,xl*2,yl*2 + 1,zl*2,denom*2);
				break;
			case Z_AXIS: 
				right = new Edge_info(type,xl*2,yl*2,zl*2 + 1,denom*2);
				break;
			default:
				System.err.printf("bad type %d in split_edge\n",type);
			    throw new IllegalArgumentException("split_edge" +type.toString());
			}
			right.status = status;
		}

		if( sol != null )
		{
			if( sol.getRoot() >= 0.0 && sol.getRoot() <= 0.5 )
			{
				if( left.sol == null )
				{
					left.sol = sol;
					sol = null;
					left.sol.setRoot(left.sol.getRoot() * 2.0);
					left.sol.xl = left.xl;
					left.sol.yl = left.yl;
					left.sol.zl = left.zl;
					left.sol.denom = left.denom;
				}
				else
				{
					if( PRI_SPLIT_EDGE) {
						System.err.printf("split_edge: left.sol != null\n");
						System.err.println(this.toString());
					}
				}
			}
			else if( sol.getRoot() > 0.5 && sol.getRoot() <= 1.0 )
			{
				if( right.sol == null )
				{
					right.sol = sol;
					sol = null;
					right.sol.setRoot((right.sol.getRoot() * 2.0)-1.0);
//					right.sol.setRoot(right.sol.getRoot() - 1.0);
					right.sol.xl = right.xl;
					right.sol.yl = right.yl;
					right.sol.zl = right.zl;
					right.sol.denom = right.denom;
				}
				else
				{
					if(PRI_SPLIT_EDGE){
						System.err.printf("split_edge: right.sol != null\n");
						System.err.println(this.toString());
					}
				}
			}
			else
			{
				if(PRI_SPLIT_EDGE){
					System.err.printf("split_edge: sol.root = %f\n",
							sol.getRoot());
				}
			    throw new IllegalStateException("split_edge: sol.root = "+sol.getRoot());
			}
		}
	}



	/*
	 * Function:    printsols_on_edge(edge)
	 * action:      prints the solutions lying on the edge.
	 */

	public void printsols(StringBuilder sb) {
		if(sol != null ) {
			sb.append(sol.toString());
			sb.append('\n');
		}
		if(left != null )
		{
			left.printsols(sb);
		}
		if(right != null )
		{
			right.printsols(sb);
		}
	}

	@Override
	public String toString() {
		return toString(true);
	}
	
	
	/**
	 * Print edge
	 * @param showHeader 
	 * @return 
	 */
	public String toString(boolean showHeader) {
		StringBuilder sb = new StringBuilder();
		if(showHeader) {
			sb.append("EDGE: type ");
			sb.append(type);
			sb.append(String.format(" (%d,%d,%d)/%d,status %d\n",
					xl,yl,zl,denom,status));
		}
		printsols(sb);
		return sb.toString();
	}

	public void free() {
		if(this.left!=null)
			this.left.free();
		if(this.right!=null)
			this.right.free();
		this.left=this.right=null;
		this.sol=null;
	}

	public int count_sols() {
		
		if(this.left!=null) {
			return left.count_sols() + right.count_sols();
		}
		return sol != null ? 1 : 0;
	}

	public void add_sols_to_list(List<Sol_info> list) {
		if(left!=null) {
			left.add_sols_to_list(list);
			right.add_sols_to_list(list);
		} else if(sol!=null) {
			list.add(sol);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + denom;
		result = prime * result + ((sol == null) ? 0 : sol.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + xl;
		result = prime * result + yl;
		result = prime * result + zl;
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
		Edge_info other = (Edge_info) obj;
		if (denom != other.denom)
			return false;
		if (type != other.type)
			return false;
		if (xl != other.xl)
			return false;
		if (yl != other.yl)
			return false;
		if (zl != other.zl)
			return false;
		return true;
	}

	
}
