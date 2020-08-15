/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class Face_info {
	/*** Some definitions to assist in making edges on faces ***/

	public static final int X_LOW =1;
	public static final int  X_HIGH=2;
	public static final int  Y_LOW=3;
	public static final int  Y_HIGH=4;
	public static final int  MID_FACE=5;
	public static final int  X_LOW_Y_LOW=6;
	public static final int  X_LOW_Y_HIGH=7;
	public static final int  X_HIGH_Y_LOW=8;
	
	public static final int  X_HIGH_Y_HIGH=9;
	private static final boolean PRINT_INCLUDE_LINK = false;
	private static final boolean PRINT_INCLUDE_NODE_LINK = false;
    private static final boolean RAW_LINKS = true;

	public int xl,yl,zl,denom;
    public Key3D type;
    public int status;
    public Edge_info x_low, y_low, x_high, y_high;
    public List<Link_info> links;
    /** These are the links without attempting to join them */
    List<Link_info> rawLinks;
    public List<Node_info> nodes;
    public Face_info lb,rb,lt,rt;

    /*
     * Function:	make_face
     * action	fill the structure pointed to by face with info.
     */

    public Face_info(Key3D type,int xl,int yl,int zl,int denom)
    {
    	this.type = type;
    	this.xl = xl;
    	this.yl = yl;
    	this.zl = zl;
    	this.denom = denom;
    	this.status = BoxClevA.EMPTY;
    	this.x_low = this.x_high = this.y_low = this.y_high = null;
    	this.lb = this.rb = this.lt = this.rt = null;
    	this.links = null;
    	this.nodes = null;    	
    }

	/*
	 * Function:	make_sub_faces
	 * action:	creates the four sub faces of a face
	 */

	public Face_info[] make_sub_faces()
	{
		Face_info res[] = new Face_info[4];
		switch(this.type)
		{
		case FACE_LL: case FACE_RR:
			res[0] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2,this.denom*2);
			res[1] = new Face_info(this.type,
				this.xl*2,this.yl*2+1,this.zl*2,this.denom*2);
			res[2] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2+1,this.denom*2);
			res[3] = new Face_info(this.type,
				this.xl*2,this.yl*2+1,this.zl*2+1,this.denom*2);
			break;
		case FACE_FF: case FACE_BB:
			res[0] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2,this.denom*2);
			res[1] = new Face_info(this.type,
				this.xl*2+1,this.yl*2,this.zl*2,this.denom*2);
			res[2] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2+1,this.denom*2);
			res[3] = new Face_info(this.type,
				this.xl*2+1,this.yl*2,this.zl*2+1,this.denom*2);
			break;
		case FACE_DD: case FACE_UU:
			res[0] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2,this.denom*2);
			res[1] = new Face_info(this.type,
				this.xl*2+1,this.yl*2,this.zl*2,this.denom*2);
			res[2] = new Face_info(this.type,
				this.xl*2,this.yl*2+1,this.zl*2,this.denom*2);
			res[3] = new Face_info(this.type,
				this.xl*2+1,this.yl*2+1,this.zl*2,this.denom*2);
			break;
		default:
			throw new RuntimeException("Bad type in make subface");
		}
		return res;
	}

    
    /*
     * Function:	make_edge_on_face
     * action:	fill loctaion pointed to by sol with information
     *		about the edge on the face refered to by code.
     */

    Edge_info make_face_edge(int code)
    {
    	switch(type)
    	{
    	case FACE_LL: case FACE_RR:
    		switch(code)
    		{
    		case X_LOW:
    			return new Edge_info(Key3D.Z_AXIS,xl,yl,
    				zl,denom);
    			
    		case X_HIGH:
    			return new Edge_info(Key3D.Z_AXIS,xl,yl+1,
    				zl,denom);
    			
    		case Y_LOW:
    			return new Edge_info(Key3D.Y_AXIS,xl,yl,
    				zl,denom);
    			
    		case Y_HIGH:
    			return new Edge_info(Key3D.Y_AXIS,xl,yl,
    				zl+1,denom);
    			
    		case MID_FACE:
    			return new Edge_info(type,xl,yl,
    				zl,denom);
    			
    		}
    		break;

    	case FACE_FF: case FACE_BB:
    		switch(code)
    		{
    		case X_LOW:
    			return new Edge_info(Key3D.Z_AXIS,xl,yl,
    				zl,denom);
    			
    		case X_HIGH:
    			return new Edge_info(Key3D.Z_AXIS,xl+1,yl,
    				zl,denom);
    			
    		case Y_LOW:
    			return new Edge_info(Key3D.X_AXIS,xl,yl,
    				zl,denom);
    			
    		case Y_HIGH:
    			return new Edge_info(Key3D.X_AXIS,xl,yl,
    				zl+1,denom);
    			
    		case MID_FACE:
    			return new Edge_info(type,xl,yl,
    				zl,denom);
    			
    		}
    		break;

    	case FACE_DD: case FACE_UU:
    		switch(code)
    		{
    		case X_LOW:
    			return new Edge_info(Key3D.Y_AXIS,xl,yl,
    				zl,denom);
    			
    		case X_HIGH:
    			return new Edge_info(Key3D.Y_AXIS,xl+1,yl,
    				zl,denom);
    			
    		case Y_LOW:
    			return new Edge_info(Key3D.X_AXIS,xl,yl,
    				zl,denom);
    			
    		case Y_HIGH:
    			return new Edge_info(Key3D.X_AXIS,xl,yl+1,
    				zl,denom);
    			
    		case MID_FACE:
    			return new Edge_info(type,xl,yl,
    				zl,denom);
    			
    		default:
    			throw new RuntimeException("Bad type in make subface");
    		}

    	default:
			throw new RuntimeException("Bad type in make subface");    		
    	}
		return null;
    }

    /*
     * Function:	calc_pos_on_face
     * action:	vec is the position of sol on the face
     */

    public double[] calc_pos_on_face(Sol_info sol)
    {
    	double[] vec = new double[2];
    	switch(this.type)
    	{
    	case FACE_LL: case FACE_RR:
    		switch(sol.type)
    		{
    		case Y_AXIS:
    			vec[0] = this.denom * (sol.yl + sol.getRoot())/sol.denom
    				- this.yl;
    			vec[1] = this.denom * ((double) sol.zl) / sol.denom - this.zl;
    			break;
    		case Z_AXIS:
    			vec[0] = this.denom * ((double) sol.yl) / sol.denom - this.yl;
    			vec[1] = this.denom * (sol.zl + sol.getRoot())/sol.denom
    				- this.zl;
    			break;
    		default:
    			BoxClevA.log.printf("calc_pos_on_face: bad types face %s",this.type.toString());
    			BoxClevA.log.printf(" sol %s",sol.type.toString());
    			BoxClevA.log.printf("\n");
    			break;
    		}
    		break;
    	case FACE_FF: case FACE_BB:
    		switch(sol.type)
    		{
    		case X_AXIS:
    			vec[0] = this.denom * (sol.xl + sol.getRoot())/sol.denom
    				- this.xl;
    			vec[1] = this.denom * ((double) sol.zl) / sol.denom - this.zl;
    			break;
    		case Z_AXIS:
    			vec[0] = this.denom * ((double) sol.xl) / sol.denom - this.xl;
    			vec[1] = this.denom * (sol.zl + sol.getRoot())/sol.denom
    				- this.zl;
    			break;
    		default:
    			BoxClevA.log.printf("calc_pos_on_face: bad types face %s",this.type.toString());
    			BoxClevA.log.printf(" sol %s",sol.type.toString());
    			BoxClevA.log.printf("\n");
    			break;
    		}
    		break;
    	case FACE_DD: case FACE_UU:
    		switch(sol.type)
    		{
    		case X_AXIS:
    			vec[0] = this.denom * (sol.xl + sol.getRoot())/sol.denom
    				- this.xl;
    			vec[1] = this.denom * ((double) sol.yl) / sol.denom - this.yl;
    			break;
    		case Y_AXIS:
    			vec[0] = this.denom * ((double) sol.xl) / sol.denom - this.xl;
    			vec[1] = this.denom * (sol.yl + sol.getRoot())/sol.denom
    				- this.yl;
    			break;
    		default:
    			BoxClevA.log.printf("calc_pos_on_face: bad types face %s",this.type.toString());
    			BoxClevA.log.printf(" sol %s",sol.type.toString());
    			BoxClevA.log.printf("\n");
    			break;
    		}
    		break;
    	default:
			throw new RuntimeException("Bad type in make subface");
    	}	/* end switch(this.type) */
    	return vec;
    }

    
    /*
     * Function:	include_link
     * action:	given a link between two solutions and a list of existing
     *		links on the face do the following:
     *		if neither sol in list add link to list;
     *		if one sol matches a sol in list extend the existing link;
     *		if link joins two existing links remove one and
     *		join the two together.
     *
     *		basically do the right thing to the list with the given link.
     */


    public void include_link(Sol_info sol1, Sol_info sol2)
    {
    	Link_info link1=null, link2=null;
    	boolean link1_keepA = false, link2_keepA = false;

    if(PRINT_INCLUDE_LINK)
    /*
    	if( 512 * this.yl == 264 * this.denom)
    */
    	{
    	BoxClevA.log.printf("include_link\n");
    	BoxClevA.log.println(this);
    	BoxClevA.log.println(sol1);
    	BoxClevA.log.println(sol2);
    	}
    
    if(RAW_LINKS) {
        if(this.rawLinks==null) 
            this.rawLinks = new ArrayList<Link_info>();
        Link_info link = new Link_info(sol1,sol2);
        rawLinks.add(link);
    }
    
    if(this.links==null) 
    	this.links = new ArrayList<Link_info>();
 
    	for(Link_info link:links)
    	{
    		if( sol1 == link.A && sol1.type.compareTo( Key3D.FACE_LL) < 0 )
    		{
    			if(link.B == sol2) return;

    			link1 = link;
    			if( link.B == null )
    			{
    				link.B = sol2;
    				link1_keepA = true;
    			}
    			else
    			{
    				link.A = sol2;
    				link1_keepA = false;
    			}
    		}
    		else if( sol1 == link.B && sol1.type.compareTo( Key3D.FACE_LL) < 0 )
    		{
    			if(link.A == sol2) return;

    			link1 = link;
    			if( link.A == null )
    			{
    				link.A = sol2;
    				link1_keepA = false;
    			}
    			else
    			{
    				link.B = sol2;
    				link1_keepA = true;
    			}
    		}
    		else if( sol2 == link.A && sol2.type.compareTo( Key3D.FACE_LL) < 0 )
    		{
    			link2 = link;
    			if( link.B == null )
    			{
    				link.B = sol1;
    				link2_keepA = true;
    			}
    			else
    			{
    				link.A = sol1;
    				link2_keepA = false;
    			}
    		}
    		else if( sol2 == link.B && sol2.type.compareTo( Key3D.FACE_LL) < 0 )
    		{
    			link2 = link;
    			if( link.A == null )
    			{
    				link.A = sol1;
    				link2_keepA = false;
    			}
    			else
    			{
    				link.B = sol1;
    				link2_keepA = true;
    			}
    		}

    	} /* end while */

    	if( link1 == null && link2 == null )	/* Didn't find link add it */
    	{
    		Link_info link = new Link_info(sol1,sol2);
    		this.links.add(link);
    	}
    	else if( link1 != null ) /* join two links together */
    	{
    		if( link2 != null )
    		{
    			if( link1_keepA )
    				if( link2_keepA )
    					link1.B = link2.A;
    				else
    					link1.B = link2.B;
    			else
    				if( link2_keepA )
    					link1.A = link2.A;
    				else
    					link1.A = link2.B;

    			links.remove(link2);
    		}

    		/*** Do nothing if only one end of a simple link. ***/
    	}
    if( PRINT_INCLUDE_LINK) 
    	{
    	BoxClevA.log.printf("include_link done\n");
    	BoxClevA.log.println(this);
    	}
    
    }

    void add_node(Node_info node) {
    	if(nodes==null)
    		nodes=new ArrayList<Node_info>();
    	nodes.add(node);
    }
    /*
     * Function:	add_node(face,sol)
     * action:	add the node to the list for the face.
     *		simple version where we don't try to join nodes together.
     */

    public void add_node(Sol_info sol)
    {
    	Node_info node;

    /*	System.err.printf("add_node: "); print_Key3D(this.type); 
    	System.err.printf(" (%d,%d,%d)/%d\n",this.xl,this.yl,this.zl,this.denom);
    */
    	if(sol.type.compareTo(Key3D.X_AXIS) < 0 || sol.type.compareTo(Key3D.BOX)>0 )
    	{
			throw new RuntimeException("Bad type in make subface");
    	}
    	node = new Node_info(sol);
    	if(this.nodes==null)
    		this.nodes=new ArrayList<Node_info>();
    	nodes.add(node);

    if(PRINT_INCLUDE_NODE_LINK) 
    	{
    	BoxClevA.log.printf("add_node\n");
    	BoxClevA.log.println(node.sol);
    	BoxClevA.log.print(this);
    	}
    

    }


    int count_nodes_on_face()
    {
    	if(nodes==null) return 0;
    	int count=0;

    	count = nodes.size();

    	if( this.lb != null )
    	{
    		count += lb.count_nodes_on_face()
    		 + lt.count_nodes_on_face()
    		 + rb.count_nodes_on_face()
    		 + rt.count_nodes_on_face();
    	}
    	return(count);
    }

    /*
     * Function:	get_nth_node_on_face
     * action:	finds the nth node on a face and returns a pointer to it.
     *		If there is no nth node return (nil).
     */

    Node_info get_nth_node_on_face(int n)
    {
    	Node_info temp;
    	if(n<nodes.size())
    		return nodes.get(n);
    	n-=nodes.size();
    	
    	/* Now try sub faces */

    	if( this.lb != null )
    	{
    		temp = lb.get_nth_node_on_face(n);
    		if(temp != null ) return(temp);
    		n -= lb.count_nodes_on_face();
    	}

    	if( this.lt != null )
    	{
    		temp = lt.get_nth_node_on_face(n);
    		if(temp != null ) return(temp);
    		n -= lt.count_nodes_on_face();
    	}

    	if( this.rb != null )
    	{
    		temp = rb.get_nth_node_on_face(n);
    		if(temp != null ) return(temp);
    		n -= rb.count_nodes_on_face();
    	}

    	if( this.rt != null )
    	{
    		temp = rt.get_nth_node_on_face(n);
    		if(temp != null ) return(temp);
    		n -= rt.count_nodes_on_face();
    	}

    	/* couldn't find an nth node */

    	return(null);
    }

    /*
     * Function:    printnodes_on_face(face)
     * action:      prints the nodes lying on the face.
     */

    String print_nodes_on_face()
    {
        StringBuilder sb = new StringBuilder();
            if(nodes != null ) 
            {
            	for(Node_info node:nodes)
            		sb.append(node);
            }
            	
            if(lb != null ) sb.append(lb.print_nodes_on_face());
            if(lb != null ) sb.append(rb.print_nodes_on_face());
            if(lt != null ) sb.append(lt.print_nodes_on_face());
            if(rt != null ) sb.append(rt.print_nodes_on_face());
            
            return sb.toString();
    }

	public void add_nodes_to_list(List<Sol_info> list) {
		if(nodes !=null) {
			list.addAll(nodes.stream().map(node -> node.sol).collect(Collectors.toList()));
		}
		if(lb!=null) lb.add_nodes_to_list(list);
		if(lt!=null) lt.add_nodes_to_list(list);
		if(rb!=null) rb.add_nodes_to_list(list);
		if(rt!=null) rt.add_nodes_to_list(list);
	}

    @Override
    public String toString() {
		return toString(true);
	}


    /**
     * Print the face, with all sols
     * @param showEdgeHeader
     * @return
     */
	public String toString(boolean showEdgeHeader) {
        StringBuilder sb = new StringBuilder();

        sb.append(type);
        sb.append(String.format(" (%d,%d,%d)/%d,status %d\n",
                xl,yl,zl,denom,status));
        if(x_low!=null)
        	sb.append(x_low.toString(showEdgeHeader));
        if(x_high!=null)
        sb.append(x_high.toString(showEdgeHeader));
        if(y_low!=null)
        sb.append(y_low.toString(showEdgeHeader));
        if(y_high!=null)
        sb.append(y_high.toString(showEdgeHeader));
        sb.append(this.print_nodes_on_face());
        if(links!=null) {
        	for(Link_info link:links)
        		sb.append(link);
        }
        
        return sb.toString();
    }

    public String print_face_brief()
    {
        StringBuilder sb = new StringBuilder();
            sb.append("FACE: type ");
            sb.append(type);
            sb.append(String.format(" (%d,%d,%d)/%d,status %d\n",
                    xl,yl,zl,denom,status));
            if(links!=null) {
            	for(Link_info link:links)
            		sb.append(link);
            }
            return sb.toString();
    }

    public String toStringHeader()
    {
        StringBuilder sb = new StringBuilder();
            sb.append("FACE: type ");
            sb.append(type);
            sb.append(String.format(" (%d,%d,%d)/%d,status %d\n",
                    xl,yl,zl,denom,status));
            return sb.toString();
    }

	void free_bits_of_face() {
		//if(this.nodes!=null) this.nodes.free(); 
		this.nodes = null;
		//if(this.links!=null) this.links.free(); 
		this.links = null;
		if(this.lb != null )
		{
			this.lb.x_high.free();
			this.lb.y_high.free();
			this.rt.x_low.free();
			this.rt.y_low.free();
			this.lb.free();
			this.rb.free();
			this.lt.free();
			this.rt.free();
		}
		this.lb = this.rb = this.lt = this.rt = null;
		this.rawLinks=null;
}

	private void free() {
		//if(this.nodes!=null) this.nodes.free(); 
		this.nodes = null;
		//if(this.links!=null) this.links.free(); 
		this.links = null;
		if(this.lb != null )
		{
			this.lb.x_high.free();
			this.lb.y_high.free();
			this.rt.x_low.free();
			this.rt.y_low.free();
			this.lb.free();
			this.rb.free();
			this.lt.free();
			this.rt.free();
		}
		this.lb = this.rb = this.lt = this.rt = null;
		this.rawLinks=null;
	}

	public static boolean same(int this_x,int this_denom,int that_x,int that_denom) {
		return (that_x * this_denom == this_x * that_denom); 
	}

	public static boolean within(int this_x,int this_denom,int that_x,int that_denom) {
		if(that_x * this_denom < this_x * that_denom) 
			return false;
		
		if((that_x+1) * this_denom > (this_x+1) * that_denom) 
			return false;
		return true;
	}

	private static boolean withinInclusive(int this_x,int this_denom,int that_x,int that_denom) {
		if(that_x * this_denom < this_x * that_denom) 
			return false;
		
		if((that_x) * this_denom > (this_x+1) * that_denom) 
			return false;
		return true;
	}

	private boolean withinExclusive(int this_x,int this_denom,int that_x,int that_denom) {
		if(that_x * this_denom <= this_x * that_denom) 
			return false;
		
		if((that_x+1) * this_denom > (this_x+1) * that_denom) 
			return false;
		return true;
	}

	/**
	 * Whether a side of the box is completely inside this face
	 * @param box
	 * @return
	 */
	public boolean contains(Box_info box) {
		switch(type) {
		case FACE_LL:
		case FACE_RR:
			return (   same(xl,denom,box.xl,box.denom)
					|| same(xl,denom,box.xl+1,box.denom) )
					&& within(yl,denom,box.yl,box.denom)
					&& within(zl,denom,box.zl,box.denom);
		case FACE_FF:
		case FACE_BB:
			return within(xl,denom,box.xl,box.denom)
					&& (same(yl,denom,box.yl,box.denom)
					|| same(yl,denom,box.yl+1,box.denom) )
					&& within(zl,denom,box.zl,box.denom);
		case FACE_UU:
		case FACE_DD:
			return within(xl,denom,box.xl,box.denom)
					&& within(yl,denom,box.yl,box.denom)
					&& (same(zl,denom,box.zl,box.denom)
					 || same(zl,denom,box.zl+1,box.denom) );
		default:
			throw new IllegalArgumentException("Bad key "+ type);		
		}
	}

	public boolean contains(Sol_info sol) {
		if(!sol.type.isEdge())
			return false;
		
		switch(type) {
		case FACE_LL:
		case FACE_RR:
			return same(xl,denom,sol.xl,sol.denom)
					&& within(yl,denom,sol.yl,sol.denom)
					&& within(zl,denom,sol.zl,sol.denom);
		case FACE_FF:
		case FACE_BB:
			return within(xl,denom,sol.xl,sol.denom)
					&& same(yl,denom,sol.yl,sol.denom)
					&& within(zl,denom,sol.zl,sol.denom);
		case FACE_UU:
		case FACE_DD:
			return within(xl,denom,sol.xl,sol.denom)
					&& within(yl,denom,sol.yl,sol.denom)
					&& same(zl,denom,sol.zl,sol.denom);
		default:
			throw new IllegalArgumentException("Bad key "+ type);		
		}
	}

	public boolean containsInclusive(Sol_info sol) {
		
		switch(type) {
		case FACE_LL:
		case FACE_RR:
			switch(sol.type) {
			case X_AXIS:
				return false;
			case Y_AXIS:
				return same(xl,denom,sol.xl,sol.denom)
						&& within(yl,denom,sol.yl,sol.denom)
						&& withinInclusive(zl,denom,sol.zl,sol.denom);
			case Z_AXIS:
				return same(xl,denom,sol.xl,sol.denom)
						&& withinInclusive(yl,denom,sol.yl,sol.denom)
						&& within(zl,denom,sol.zl,sol.denom);
			case BOX:
			case FACE_FF:
			case FACE_BB:
			case FACE_DD:
			case FACE_UU:
				return false;
			default:
				return same(xl,denom,sol.xl,sol.denom)
						&& within(yl,denom,sol.yl,sol.denom)
						&& within(zl,denom,sol.zl,sol.denom);
			}
		case FACE_FF:
		case FACE_BB:
			switch(sol.type) {
			case X_AXIS:
				return within(xl,denom,sol.xl,sol.denom)
						&& same(yl,denom,sol.yl,sol.denom)
						&& withinInclusive(zl,denom,sol.zl,sol.denom);
			case Y_AXIS:
				return false;
			case Z_AXIS:
				return withinInclusive(xl,denom,sol.xl,sol.denom)
						&& same(yl,denom,sol.yl,sol.denom)
						&& within(zl,denom,sol.zl,sol.denom);
			case BOX:
			case FACE_LL:
			case FACE_RR:
			case FACE_DD:
			case FACE_UU:
				return false;
			default:				
				return within(xl,denom,sol.xl,sol.denom)
					&& same(yl,denom,sol.yl,sol.denom)
					&& within(zl,denom,sol.zl,sol.denom);
			}
		case FACE_UU:
		case FACE_DD:
			switch(sol.type) {
			case X_AXIS:
				return within(xl,denom,sol.xl,sol.denom)
						&& withinInclusive(yl,denom,sol.yl,sol.denom)
						&& same(zl,denom,sol.zl,sol.denom);
			case Y_AXIS:
				return withinInclusive(xl,denom,sol.xl,sol.denom)
						&& within(yl,denom,sol.yl,sol.denom)
						&& same(zl,denom,sol.zl,sol.denom);
			case Z_AXIS:
				return false;
			case BOX:
			case FACE_LL:
			case FACE_RR:
			case FACE_FF:
			case FACE_BB:
				return false;
			default:
			return within(xl,denom,sol.xl,sol.denom)
					&& within(yl,denom,sol.yl,sol.denom)
					&& same(zl,denom,sol.zl,sol.denom);
			}
		default:
			throw new IllegalArgumentException("Bad key "+ type);		
	}
	}
	
	public static class Tester {
		
	@Test
	public void test_within() {
		assertTrue(within(3,8,3,8));
		assertFalse(within(3,8,2,8));
		assertFalse(within(3,8,4,8));
		
		assertTrue(within(3,8,6,16));
		assertTrue(within(3,8,7,16));
		assertFalse(within(3,8,5,16));
		assertFalse(within(3,8,8,16));

		assertFalse(withinInclusive(3,8,2,8));
		assertTrue( withinInclusive(3,8,3,8));
		assertTrue( withinInclusive(3,8,4,8));
		assertFalse(withinInclusive(3,8,5,8));

		assertFalse(withinInclusive(3,8,5,16));
		assertTrue( withinInclusive(3,8,6,16));
		assertTrue( withinInclusive(3,8,7,16));
		assertTrue( withinInclusive(3,8,8,16));
		assertFalse(withinInclusive(3,8,9,16));
	}
	
	@Test
	public void test_contains_Sol_inclusive() {
		Face_info face =  new Face_info(Key3D.FACE_LL,4,4,4,8);
		
		Sol_info sol_x = new Sol_info(Key3D.X_AXIS,4,4,4,8,0.5);
		assertFalse(face.containsInclusive(sol_x));
		
		Sol_info sol_y = new Sol_info(Key3D.Y_AXIS,4,4,4,8,0.5);
		assertTrue(face.containsInclusive(sol_y));
		
		Sol_info sol_y2 = new Sol_info(Key3D.Y_AXIS,4,5,4,8,0.5);
		assertTrue(face.containsInclusive(sol_y2));
		
		
	}
	}

	public boolean containsExclusive(Sol_info sol) {
		if(!sol.type.isEdge())
			return false;
		
		switch(type) {
		case FACE_LL:
		case FACE_RR:
			return same(xl,denom,sol.xl,sol.denom)
					&& withinExclusive(yl,denom,sol.yl,sol.denom)
					&& withinExclusive(zl,denom,sol.zl,sol.denom);
		case FACE_FF:
		case FACE_BB:
			return withinExclusive(xl,denom,sol.xl,sol.denom)
					&& same(yl,denom,sol.yl,sol.denom)
					&& withinExclusive(zl,denom,sol.zl,sol.denom);
		case FACE_UU:
		case FACE_DD:
			return withinExclusive(xl,denom,sol.xl,sol.denom)
					&& withinExclusive(yl,denom,sol.yl,sol.denom)
					&& same(zl,denom,sol.zl,sol.denom);
		default:
			throw new IllegalArgumentException("Bad key "+ type);		
		}
	}

	public int count_sol() {
		if(lb!=null) {
			return lb.count_sol() + lt.count_sol()
				+ rb.count_sol() + rt.count_sol();
		} else {
			return (x_low != null ? x_low.count_sols() : 0)
				+  (x_high !=null ? x_high.count_sols() : 0)
				+  (y_low != null ? y_low.count_sols() : 0)
				+ (y_high != null ? y_high.count_sols() : 0);		
		}
	}

	public List<Sol_info> get_all_sols_on_edges() {
		List<Sol_info> list = new ArrayList<>();
		if(x_low!=null)
			x_low.add_sols_to_list(list);
		if(x_high!=null)
			x_high.add_sols_to_list(list);
		if(y_low!=null)
			y_low.add_sols_to_list(list);
		if(y_high!=null)
			y_high.add_sols_to_list(list);
		return list;
	}

    int get_nodes_on_face(List<Node_info> list) {
        int count=0;
        if(nodes!=null)
            for(Node_info temp:this.nodes)
            {
                list.add(temp);
                ++count;
            }
        if( lb != null )
            count += lb.get_nodes_on_face(list);
        if( rb != null )
            count += rb.get_nodes_on_face(list);
        if( lt != null )
            count += lt.get_nodes_on_face(list);
        if( rt != null )
            count += rt.get_nodes_on_face(list);
        return count;
    }

}

