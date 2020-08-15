/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

import static org.singsurf.singsurf.asurf.Face_info.same;
import static org.singsurf.singsurf.asurf.Face_info.within;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.singsurf.singsurf.asurf.Knitter.BoxWithAdjacency;

public class Box_info {
    public int xl;
	public int yl;
	public int zl;
	public int denom;
    public Key3D type;
    public int status;
    public short num_sings;
    public Box_info lfd, lfu, lbd, lbu, rfd, rfu, rbd, rbu;
    public Face_info ll, rr, ff, bb, dd, uu;
    public List<Node_link_info> node_links;
    public List<Chain_info> chains;
    public List<Sing_info> sings;
    public List<Facet_info> facets;
    public BoxWithAdjacency adjacency=null;
    public Box_info parent;

    public Box_info(int xl, int yl, int zl, int denom, Box_info parent) {
	this.xl = xl;
	this.yl = yl;
	this.zl = zl;
	this.denom = denom;
	this.status = BoxClevA.EMPTY;
	this.lfu = this.lfd = this.lbu = this.lbd = null;
	this.rfu = this.rfd = this.rbu = this.rbd = null;
	this.ll = this.rr = this.ff = this.bb = this.dd = this.uu = null;
	this.node_links = null;
	this.sings = null;
	this.chains = null;
	this.facets = null;
	this.parent = parent;
    }

    /*
     * Function: make_box_face action: let face contain the info about the face
     * 'type' of 'box'.
     */

    Face_info make_box_face(Key3D type1) {
	switch (type1) {
	case FACE_LL:
	    return new Face_info(Key3D.FACE_LL, this.xl, this.yl, this.zl, this.denom);
	case FACE_RR:
	    return new Face_info(Key3D.FACE_RR, this.xl + 1, this.yl, this.zl, this.denom);
	case FACE_FF:
	    return new Face_info(Key3D.FACE_FF, this.xl, this.yl, this.zl, this.denom);
	case FACE_BB:
	    return new Face_info(Key3D.FACE_BB, this.xl, this.yl + 1, this.zl, this.denom);
	case FACE_DD:
	    return new Face_info(Key3D.FACE_DD, this.xl, this.yl, this.zl, this.denom);
	case FACE_UU:
	    return new Face_info(Key3D.FACE_UU, this.xl, this.yl, this.zl + 1, this.denom);
	default:
	    System.err.printf("make_box_face: bad type %s", type1.toString());
	    break;
	}
	return null;
    }

    /*
     * Function: sub_devide_box action: create the apropriate information for all
     * the sub boxes, does not play about with the faces.
     */

    public void sub_devide_box() {
	this.lfd = new Box_info(2 * this.xl, 2 * this.yl, 2 * this.zl, 2 * this.denom, this);
	this.rfd = new Box_info(2 * this.xl + 1, 2 * this.yl, 2 * this.zl, 2 * this.denom, this);
	this.lbd = new Box_info(2 * this.xl, 2 * this.yl + 1, 2 * this.zl, 2 * this.denom, this);
	this.rbd = new Box_info(2 * this.xl + 1, 2 * this.yl + 1, 2 * this.zl, 2 * this.denom, this);
	this.lfu = new Box_info(2 * this.xl, 2 * this.yl, 2 * this.zl + 1, 2 * this.denom, this);
	this.rfu = new Box_info(2 * this.xl + 1, 2 * this.yl, 2 * this.zl + 1, 2 * this.denom, this);
	this.lbu = new Box_info(2 * this.xl, 2 * this.yl + 1, 2 * this.zl + 1, 2 * this.denom, this);
	this.rbu = new Box_info(2 * this.xl + 1, 2 * this.yl + 1, 2 * this.zl + 1, 2 * this.denom, this);
    }

    /*
     * Function: calc_pos_in_box action: vec is the position of sol on the box
     */

    public double[] calc_pos_in_box(Sol_info sol) {
	double vec[] = new double[3];
	switch (sol.type) {
	case X_AXIS:
	    vec[0] = this.denom * (sol.xl + sol.getRoot()) / sol.denom - this.xl;
	    vec[1] = this.denom * ((double) sol.yl) / sol.denom - this.yl;
	    vec[2] = this.denom * ((double) sol.zl) / sol.denom - this.zl;
	    break;

	case Y_AXIS:
	    vec[0] = this.denom * ((double) sol.xl) / sol.denom - this.xl;
	    vec[1] = this.denom * (sol.yl + sol.getRoot()) / sol.denom - this.yl;
	    vec[2] = this.denom * ((double) sol.zl) / sol.denom - this.zl;
	    break;

	case Z_AXIS:
	    vec[0] = this.denom * ((double) sol.xl) / sol.denom - this.xl;
	    vec[1] = this.denom * ((double) sol.yl) / sol.denom - this.yl;
	    vec[2] = this.denom * (sol.zl + sol.getRoot()) / sol.denom - this.zl;
	    break;

	case FACE_LL:
	case FACE_RR:
	    vec[0] = this.denom * ((double) sol.xl) / sol.denom - this.xl;
	    vec[1] = this.denom * (sol.yl + sol.getRoot()) / sol.denom - this.yl;
	    vec[2] = this.denom * (sol.zl + sol.getRoot2()) / sol.denom - this.zl;
	    break;
	case FACE_FF:
	case FACE_BB:
	    vec[0] = this.denom * (sol.xl + sol.getRoot()) / sol.denom - this.xl;
	    vec[1] = this.denom * ((double) sol.yl) / sol.denom - this.yl;
	    vec[2] = this.denom * (sol.zl + sol.getRoot2()) / sol.denom - this.zl;
	    break;
	case FACE_DD:
	case FACE_UU:
	    vec[0] = this.denom * (sol.xl + sol.getRoot()) / sol.denom - this.xl;
	    vec[1] = this.denom * (sol.yl + sol.getRoot2()) / sol.denom - this.yl;
	    vec[2] = this.denom * ((double) sol.zl) / sol.denom - this.zl;
	    break;

	case BOX:
	    vec[0] = this.denom * (sol.xl + sol.getRoot()) / sol.denom - this.xl;
	    vec[1] = this.denom * (sol.yl + sol.getRoot2()) / sol.denom - this.yl;
	    vec[2] = this.denom * (sol.zl + sol.getRoot3()) / sol.denom - this.zl;
	    break;

	default:
	    System.err.printf("calc_pos_in_box: bad types ");
	    System.err.printf(" sol %s", sol.type.toString());
	    System.err.printf("\n");
	    break;
	}
	return vec;
    }

    public void add_node_link_simple(Node_info node1, Node_info node2) {
	Node_link_info link = new Node_link_info(node1, node2, LinkStatus.NODE);
	if (node_links == null)
	    node_links = new ArrayList<Node_link_info>();
	node_links.add(link);
    }

    /*
     * Function: add_node_link action: given a link between two nodes and a list of
     * exsisting links in the box do the following: if neither sol in list add link
     * to list; if one sol matches a sol in list extend the existing link; if link
     * joins two exsisting links remove one and join the two together.
     *
     * basically do the right thing to the list with the given link.
     */

    public void add_node_link(Node_info node1, Node_info node2) {
	Node_link_info link1 = null, link2 = null;
	boolean link1_keepA = false;
	boolean link2_keepA = false;

	if (node_links == null) {
	    add_node_link_simple(node1, node2);
	    return;
	}
	for (Node_link_info link : node_links) {
	    if (node1 == link.A && node1.sol.type != Key3D.BOX) {
		link1 = link;
		if (link.B == null) {
		    link.B = node2;
		    link1_keepA = true;
		} else {
		    link.A = node2;
		    link1_keepA = false;
		}
	    } else if (node1 == link.B && node1.sol.type != Key3D.BOX) {
		link1 = link;
		if (link.A == null) {
		    link.A = node2;
		    link1_keepA = false;
		} else {
		    link.B = node2;
		    link1_keepA = true;
		}
	    } else if (node2 == link.A && node2.sol.type != Key3D.BOX) {
		link2 = link;
		if (link.B == null) {
		    link.B = node1;
		    link2_keepA = true;
		} else {
		    link.A = node1;
		    link2_keepA = false;
		}
	    } else if (node2 == link.B && node2.sol.type != Key3D.BOX) {
		link2 = link;
		if (link.A == null) {
		    link.A = node1;
		    link2_keepA = false;
		} else {
		    link.B = node1;
		    link2_keepA = true;
		}
	    }

	} /* end for */

	if (link1 == null && link2 == null) /* Didn't find link add it */
	{
	    add_node_link_simple(node1, node2);
	} else if (link1 != null) /* join two links together */
	{
	    if (link2 != null) {
		if (link1_keepA)
		    if (link2_keepA)
			link1.B = link2.A;
		    else
			link1.B = link2.B;
		else if (link2_keepA)
		    link1.A = link2.A;
		else
		    link1.A = link2.B;

		node_links.remove(link2);
	    }

	    /*** Do nothing if only one end of a simple link. ***/
	}
    }

    /*
     * Function: add_sing(box,sol) action: add the sing to the list for the this.
     * simple version where we don't try to join sings together.
     */

    public void add_sing(Sol_info sol) {
	Sing_info sing = new Sing_info(sol, LinkStatus.NODE);
	if (sings == null)
	    sings = new ArrayList<Sing_info>();
	sings.add(sing);
    }

    public String print_nodes_on_box() {
	StringBuilder sb = new StringBuilder();
	sb.append(ll.print_nodes_on_face());
	sb.append(rr.print_nodes_on_face());
	sb.append(ff.print_nodes_on_face());
	sb.append(bb.print_nodes_on_face());
	sb.append(dd.print_nodes_on_face());
	sb.append(uu.print_nodes_on_face());
	return sb.toString();
    }

    /**
     * Checks if the test box exactly matches one of the 
     * faces of this box. The denoms must be the same 
     * @param testbox 
     * @return the key of the matching face, NONE if no match, BOX is identical box
     */
    
	public Key3D matchingFace(Box_info testbox) {
		if(this.denom != testbox.denom)
			return Key3D.NONE;
		if(this.xl == testbox.xl+1
			&& this.yl == testbox.yl
			&& this.zl == testbox.zl ) {
			return Key3D.FACE_LL;
		}
		if(this.xl +1 == testbox.xl
				&& this.yl == testbox.yl
				&& this.zl == testbox.zl ) {
				return Key3D.FACE_RR;
			}
		if(this.yl== testbox.yl+1
				&& this.xl == testbox.xl
				&& this.zl == testbox.zl ) {
				return Key3D.FACE_FF;
			}
		if(this.yl+1 == testbox.yl
				&& this.xl == testbox.xl
				&& this.zl == testbox.zl ) {
				return Key3D.FACE_BB;
			}
		if(this.zl== testbox.zl+1
				&& this.xl == testbox.xl
				&& this.yl == testbox.yl ) {
				return Key3D.FACE_DD;
			}
		if(this.zl+1 == testbox.zl
				&& this.xl == testbox.xl
				&& this.yl == testbox.yl ) {
				return Key3D.FACE_UU;
			}
		if(this.xl == testbox.xl
				&& this.yl == testbox.yl
				&& this.zl == testbox.zl ) {
				return Key3D.BOX;
			}
		return Key3D.NONE;
	}

	/** the test box has a face which is contained in a face of this box
	 * 
	 * @param testbox
	 * @return
	 */
	public Key3D containsFace(Box_info testbox) {
		
		if(same(this.xl, this.denom, testbox.xl+1, testbox.denom ) 
				&& within(this.yl,this.denom, testbox.yl, testbox.denom)
				&& within(this.zl,this.denom, testbox.zl, testbox.denom) ) {
				return Key3D.FACE_LL;
		}
		if(same(this.xl+1, this.denom, testbox.xl, testbox.denom ) 
				&& within(this.yl,this.denom, testbox.yl, testbox.denom)
				&& within(this.zl,this.denom, testbox.zl, testbox.denom) ) {
				return Key3D.FACE_RR;
		}
		if(within(this.xl, this.denom, testbox.xl, testbox.denom ) 
				&& same(this.yl,this.denom, testbox.yl+1, testbox.denom)
				&& within(this.zl,this.denom, testbox.zl, testbox.denom) ) {
				return Key3D.FACE_FF;
		}
		if(within(this.xl, this.denom, testbox.xl, testbox.denom ) 
				&& same(this.yl+1,this.denom, testbox.yl, testbox.denom)
				&& within(this.zl,this.denom, testbox.zl, testbox.denom) ) {
				return Key3D.FACE_BB;
		}
		if(within(this.xl, this.denom, testbox.xl, testbox.denom ) 
				&& within(this.yl,this.denom, testbox.yl, testbox.denom)
				&& same(this.zl,this.denom, testbox.zl+1, testbox.denom) ) {
				return Key3D.FACE_DD;
		}
		if(        within(this.xl, this.denom, testbox.xl, testbox.denom ) 
				&& within(this.yl,this.denom, testbox.yl, testbox.denom)
				&&   same(this.zl+1,this.denom, testbox.zl, testbox.denom) ) {
				return Key3D.FACE_UU;
		}
		return Key3D.NONE;
	}
	
    public String print_box_header() {
	StringBuilder sb = new StringBuilder();
	sb.append(String.format("BOX: (%d,%d,%d)/%d status %d\n", xl, yl, zl, denom, status));
	return sb.toString();
    }

    public String getHeader() {
    	return String.format("BOX: (%d,%d,%d)/%d", xl, yl, zl, denom);
    }

    public String toString_brief() {
	StringBuilder sb = new StringBuilder();
	sb.append(String.format("BOX: (%d,%d,%d)/%d status %d\n", xl, yl, zl, denom, status));
	sb.append(this.print_nodes_on_box());
	if (sings != null)
	    for (Sing_info sing : sings)
		sb.append(sing);
	if (node_links != null)
	    for (Node_link_info link : node_links)
		sb.append(link);
	return sb.toString();
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("BOX: (%d,%d,%d)/%d status %d\n", xl, yl, zl, denom, status));

	sb.append("face l: ");
	if (ll != null)
	    sb.append(ll.toString(false));
	else
	    sb.append("null\n");
	sb.append("face r: ");
	if (rr != null)
	    sb.append(rr.toString(false));
	else
	    sb.append("null\n");
	sb.append("face f: ");
	if (ff != null)
	    sb.append(ff.toString(false));
	else
	    sb.append("null\n");
	sb.append("face b: ");
	if (bb != null)
	    sb.append(bb.toString(false));
	else
	    sb.append("null\n");
	sb.append("face u: ");
	if (uu != null)
	    sb.append(uu.toString(false));
	else
	    sb.append("null\n");
	sb.append("face d: ");
	if (dd != null)
	    sb.append(dd.toString(false));
	else
	    sb.append("null\n");

	if (sings != null)
	    for (Sing_info sing : sings)
		sb.append(sing);
	else
	    sb.append("no sings\n");

	if (node_links != null)
	    for (Node_link_info link : node_links)
		sb.append(link);
	else
	    sb.append("no node links\n");

	if (chains != null)
	    for (Chain_info chain : chains)
		sb.append(chain);
	else
	    sb.append("no chains\n");

	if( this.facets != null) {
		for(Facet_info facet:facets) {
			sb.append(facet.toString());
		}
	}
	return sb.toString();
    }

    /*
     * Function: collect_sings action: simplyfy the list of node_links and sings for
     * each sing check whats adjacient to it if there are only two adjaciencies
     * eliminate the node if a node_link is adjacient to a sing then change the face
     * to be a node.
     */

    public void collect_sings() {
	Node_link_info n2;// ,p1,p2;
	// Sing_info sing;

	/* remove any repeated node links */
	/*
	 * print_node_links(this.node_links);
	 */
	if (this.node_links != null) {
	    ListIterator<Node_link_info> it1 = this.node_links.listIterator();
	    while (it1.hasNext()) {
		Node_link_info n1;
		n1 = it1.next();
		n1.singA = n1.singB = null;

		int index = it1.nextIndex();
		ListIterator<Node_link_info> it2 = this.node_links.listIterator(index);
		while (it2.hasNext()) {
		    n2 = it2.next();
		    // }
		    // }
		    // for(Node_link_info n1:this.node_links)
		    // //for(p1=NULL,n1=this.node_links;n1!=NULL;p1=p1,n1=n1.next)
		    // {
		    // for(p2=n1,n2=n1.next;n2!=NULL;p2=n2,n2=n2.next)
		    // {
		    if (((n1.A.sol == n2.A.sol) && (n1.B.sol == n2.B.sol))
			    || ((n1.B.sol == n2.A.sol) && (n1.A.sol == n2.B.sol))) {
			it1.remove();
			break;
			// node_links.remove(n1);
			/*
			 * fprintf(stderr,"rm nodel_link\n"); print_node(n1.A); fprintf(stderr,"\t");
			 * print_node(n1.B); fprintf(stderr,"\n"); print_node(n2.A);
			 * fprintf(stderr,"\t"); print_node(n2.B); fprintf(stderr,"\n");
			 */
			// p2.next = n2.next;
			// n2 = p2;
		    }
		}
	    }
	}
	/* count up how many node_links adjacent to each sing */

	this.num_sings = 0;
	if (this.sings != null)
	    for (Sing_info sing : this.sings) {
		++this.num_sings;
		sing.numNLs = 0;
		if (this.node_links != null)
		    for (Node_link_info n1 : this.node_links) {
			/*
			 * if( n1.A.sol.type != BOX && n1.B.sol.type != BOX ) continue;
			 */
			if (n1.A.sol == sing.sing) {
			    ++sing.numNLs;
			}
			if (n1.B.sol == sing.sing) {
			    ++sing.numNLs;
			}
		    }
		sing.adjacentNLs = new Node_link_info[sing.numNLs];
		sing.numNLs = 0;
		if (this.node_links != null)
		    for (Node_link_info n1 : this.node_links) {
			/*
			 * if( n1.A.sol.type != BOX && n1.B.sol.type != BOX ) continue;
			 */
			if (n1.A.sol == sing.sing) {
			    sing.adjacentNLs[sing.numNLs++] = n1;
			    n1.singA = sing;
			}
			if (n1.B.sol == sing.sing) {
			    sing.adjacentNLs[sing.numNLs++] = n1;
			    n1.singB = sing;
			}
		    }

	    }

    }

    public void free_bits_of_box() {

	/*
	 * free_sings(box->sings);
	 */
	this.sings = null;
	if (this.chains != null)
	    for (Chain_info chain : this.chains) {
		chain.free();
	    }
	this.chains = null;

	// this.node_links.free();
	this.node_links = null;

	this.ll.free_bits_of_face();
	this.ff.free_bits_of_face();
	this.dd.free_bits_of_face();

	if (this.ll != null && this.ll.x_low != null) {
	    this.ll.x_low.free();
	    this.ll.x_low = null;
	} else if (this.ff != null && this.ff.x_low != null) {
	    this.ff.x_low.free();
	    this.ff.x_low = null;
	}

	if (this.ll != null && this.ll.y_low != null) {
	    this.ll.y_low.free();
	    this.ll.y_low = null;
	} else if (this.dd != null && this.dd.x_low != null) {
	    this.dd.x_low.free();
	    this.dd.x_low = null;
	}

	if (this.ff != null && this.ff.y_low != null) {
	    this.ff.y_low.free();
	    this.ff.y_low = null;
	} else if (this.dd != null && this.dd.y_low != null) {
	    this.dd.y_low.free();
	    this.dd.y_low = null;
	}

	this.ll = this.ff = this.dd = null;

    }

    public void free_bit(boolean rr1, boolean bb1, boolean uu1) {

    	if (this.lfd != null) {
    		this.lfd.free_bit(false, false, false);
    		this.lfd = null;
    	}
    	if (this.lfu != null) {
    		this.lfu.free_bit(false, false, uu1);
    		if (!uu1)
    			this.lfu = null;
    	}
    	if (this.lbd != null) {
    		this.lbd.free_bit(false, bb1, false);
    		if (!bb1)
    			this.lbd = null;
    	}
    	if (this.lbu != null) {
    		this.lbu.free_bit(false, bb1, uu1);
    		if (!bb1 && !uu1)
    			this.lbu = null;
    	}
    	if (this.rfd != null) {
    		this.rfd.free_bit(rr1, false, false);
    		if (!rr1)
    			this.rfd = null;
    	}
    	if (this.rfu != null) {
    		this.rfu.free_bit(rr1, false, uu1);
    		if (!rr1 && !uu1)
    			this.rfu = null;
    	}
    	if (this.rbd != null) {
    		this.rbd.free_bit(rr1, bb1, false);
    		if (!rr1 && !bb1)
    			this.rbd = null;
    	}
    	if (this.rbu != null) {
    		this.rbu.free_bit(rr1, bb1, uu1);
    		if (!rr1 && !bb1 && !uu1)
    			this.rbu = null;
    	}

    	this.sings = null;
    	if (this.chains != null)
    		for (Chain_info chain : this.chains) {
    			chain.free();
    		}
    	this.chains = null;

    	this.node_links = null;

    	if (!bb1 && !uu1) {
    		this.ll = null;
    	}
    	if (!rr1 && !uu1) {
    		this.ff = null;
    	}
    	if (!rr1 && !bb1) {
    		this.dd = null;
    	}
    	if (!bb1 && !rr1 && !uu1) {
    		this.rr = this.ff = this.uu = null;
    	}
    	
    	this.facets = null;
    	
    	if(this.adjacency != null) {
    		this.adjacency.box = null;
    		this.adjacency = null;
    	}
    }

	public Face_info getFace(Key3D key) {
		switch(key) {
		case FACE_BB:
			return bb;
		case FACE_DD:
			return dd;
		case FACE_FF:
			return ff;
		case FACE_LL:
			return ll;
		case FACE_RR:
			return rr;
		case FACE_UU:
			return uu;
		default:
			throw new IllegalArgumentException("Bad key "+ key);
		}
	}

	public Key3D faceInCommon(Box_info box) {

		
		
		return null;
	}

	public boolean sol_on_box_edge(Sol_info sol) {
		
		switch (sol.type) {
		case X_AXIS:
			return within(this.xl, this.denom, sol.xl, sol.denom ) 
					&& ( same(this.yl,this.denom,sol.yl,sol.denom) || same(this.yl+1,this.denom,sol.yl,sol.denom) ) 
					&& ( same(this.zl,this.denom,sol.zl,sol.denom) || same(this.zl+1,this.denom,sol.zl,sol.denom) );

		case Y_AXIS:
			return
					   ( same(this.xl,this.denom,sol.xl,sol.denom) || same(this.xl+1,this.denom,sol.xl,sol.denom) ) 
					&& within(this.yl, this.denom, sol.yl, sol.denom ) 
					&& ( same(this.zl,this.denom,sol.zl,sol.denom) || same(this.zl+1,this.denom,sol.zl,sol.denom) );

		case Z_AXIS:
			return  ( same(this.xl,this.denom,sol.xl,sol.denom) || same(this.xl+1,this.denom,sol.xl,sol.denom) ) 
					&& ( same(this.yl,this.denom,sol.yl,sol.denom) || same(this.yl+1,this.denom,sol.yl,sol.denom) ) 
			   &&  within(this.zl, this.denom, sol.zl, sol.denom ) ;

		case FACE_LL:
		case FACE_RR:
		case FACE_FF:
		case FACE_BB:
		case FACE_DD:
		case FACE_UU:
		case BOX:
			return false;
		default:
			throw new IllegalArgumentException("Bad type "+sol.type);
		}
	}

	public int count_sols() {
		int count =0;
		count += ll != null ? ll.count_sol()/2 + ll.count_nodes_on_face() : 0;
		count += rr != null ? rr.count_sol()/2 + rr.count_nodes_on_face() : 0;
		count += ff != null ? ff.count_sol()/2 + ff.count_nodes_on_face() : 0;
		count += bb != null ? bb.count_sol()/2 + bb.count_nodes_on_face() : 0;
		count += dd != null ? dd.count_sol()/2 + dd.count_nodes_on_face() : 0;
		count += uu != null ? uu.count_sol()/2 + uu.count_nodes_on_face() : 0;
		
		return count;
	}
	
	public void add_sols_to_list(List<Sol_info> list) {
		if(ll!=null) {
			if(ll.x_low !=null )
				ll.x_low.add_sols_to_list(list);
			if(ll.x_high !=null )
				ll.x_high.add_sols_to_list(list);
			if(ll.y_low !=null )
				ll.y_low.add_sols_to_list(list);
			if(ll.y_high !=null )
				ll.y_high.add_sols_to_list(list);
		}
		if(rr!=null) {
			if(rr.x_low !=null )
				rr.x_low.add_sols_to_list(list);
			if(rr.x_high !=null )
				rr.x_high.add_sols_to_list(list);
			if(rr.y_low !=null )
				rr.y_low.add_sols_to_list(list);
			if(rr.y_high !=null )
				rr.y_high.add_sols_to_list(list);
		}
		if(ff!=null) {
			if(ff.y_low !=null )
				ff.y_low.add_sols_to_list(list);
			if(ff.y_high !=null )
				ff.y_high.add_sols_to_list(list);
		}
		if(bb!=null) {
			if(bb.y_low !=null )
				bb.y_low.add_sols_to_list(list);
			if(bb.y_high !=null )
				bb.y_high.add_sols_to_list(list);
		}
		
		if(ll!=null)
			ll.add_nodes_to_list(list);
		if(rr!=null)
			rr.add_nodes_to_list(list);
		if(ff!=null)
			ff.add_nodes_to_list(list);
		if(bb!=null)
			bb.add_nodes_to_list(list);
		if(dd!=null)
			dd.add_nodes_to_list(list);
		if(uu!=null)
			uu.add_nodes_to_list(list);
	}
	
	public void release_from_parent() {
		if(parent!=null)
			parent.release_child(this);
	}

	private void release_child(Box_info child) {
		if(child == lfd) lfd = null;
		if(child == lfu) lfu = null;
		if(child == lbd) lbd = null;
		if(child == lbu) lbu = null;
		if(child == rfd) rfd = null;
		if(child == rfu) rfu = null;
		if(child == rbd) rbd = null;
		if(child == rbu) rbu = null;
		
		int count=0;
		if(lfd == null) ++count;
		if(lfu == null) ++count;
		if(lbd == null) ++count;
		if(lbu == null) ++count;
		if(rfd == null) ++count;
		if(rfu == null) ++count;
		if(rbd == null) ++count;
		if(rbu == null) ++count;
		
		if(count==8) {
			release_from_parent();
		}
	}
	
	/**
	 * returns the number of nodes round a box,
	 *		the first two of these solutions are put in the array nodes.
	 */

	List<Node_info> get_nodes_on_box_faces()
	{
		List<Node_info> nodes = new ArrayList<Node_info>();
		if(ll!=null)
			ll.get_nodes_on_face(nodes);
		if(rr!=null)
			rr.get_nodes_on_face(nodes);
		if(ff!=null)
			ff.get_nodes_on_face(nodes);
		if(bb!=null)
			bb.get_nodes_on_face(nodes);
		if(dd!=null)
			dd.get_nodes_on_face(nodes);
		if(uu!=null)
			uu.get_nodes_on_face(nodes);
		return nodes;
	}

	public void free_safe_bit(boolean rr1, boolean bb1, boolean uu1) {


    	if (this.lfd != null) {
    		this.lfd.free_bit(false, false, false);
    		this.lfd = null;
    	}
    	if (this.lfu != null) {
    		this.lfu.free_bit(false, false, uu1);
    		if (!uu1)
    			this.lfu = null;
    	}
    	if (this.lbd != null) {
    		this.lbd.free_bit(false, bb1, false);
    		if (!bb1)
    			this.lbd = null;
    	}
    	if (this.lbu != null) {
    		this.lbu.free_bit(false, bb1, uu1);
    		if (!bb1 && !uu1)
    			this.lbu = null;
    	}
    	if (this.rfd != null) {
    		this.rfd.free_bit(rr1, false, false);
    		if (!rr1)
    			this.rfd = null;
    	}
    	if (this.rfu != null) {
    		this.rfu.free_bit(rr1, false, uu1);
    		if (!rr1 && !uu1)
    			this.rfu = null;
    	}
    	if (this.rbd != null) {
    		this.rbd.free_bit(rr1, bb1, false);
    		if (!rr1 && !bb1)
    			this.rbd = null;
    	}
    	if (this.rbu != null) {
    		this.rbu.free_bit(rr1, bb1, uu1);
    		if (!rr1 && !bb1 && !uu1)
    			this.rbu = null;
    	}

    	this.sings = null;
    	if (this.chains != null)
    		for (Chain_info chain : this.chains) {
    			chain.free();
    		}
    	this.chains = null;

    	this.node_links = null;

    	if (!bb1 && !uu1) {
    		if(ll!=null && ll.xl * 2 != ll.denom)
    			this.ll = null;
    	}
    	if (!rr1 && !uu1) {
    		if(ff !=null && ff.yl * 2 != ff.denom)
    			this.ff = null;
    	}
    	if (!rr1 && !bb1) {
    		if(dd!=null && dd.zl * 2 != dd.denom)
    			this.dd = null;
    	}
    	if (!bb1 && !rr1 && !uu1) {
    		if(rr!=null && rr.xl * 2 != rr.denom)
    			this.rr = null;
    		if(bb !=null && bb.yl * 2 != bb.denom)
    			this.bb = null;
    		if(uu!=null && uu.zl * 2 != uu.denom)
    			this.uu = null;
    	}
    	
    	this.facets = null;
    	
    	if(this.adjacency != null) {
    		this.adjacency.box = null;
    		this.adjacency = null;
    	}
    }

	public Face_info[] facesAsArray() {
		return new Face_info[] { ll, rr, ff, bb, dd, uu };
	}


}