package org.singsurf.singsurf.asurf;

import java.util.ListIterator;

public class Topology {
	Box_info whole_box;

	public Topology(Box_info whole_box) {
	    super();
	    this.whole_box = whole_box;
	}

	/**
	 * returns a pointer to the box specified by xl,yl,zl,denom
	 *		returns null if box does not exist.
	 */
	Box_info get_box(int x,int y,int z,int denom)
	{
		Box_info temp;

		if( x < 0 || x >= denom || y < 0 || y >= denom || z < 0 || z >= denom )
			return( null );
		temp = whole_box;

		while( temp.denom < denom )
		{
		    if( 2*temp.denom*x < ( 2*temp.xl+1 )*denom )
		    {
			if( 2*temp.denom*y < ( 2*temp.yl+1 )*denom )
			{
			    if( 2*temp.denom*z < ( 2*temp.zl+1 )*denom )
				temp = temp.lfd;
			    else
				temp = temp.lfu;
			}
			else
			{
			    if( 2*temp.denom*z < ( 2*temp.zl+1 )*denom )
				temp = temp.lbd;
			    else
				temp = temp.lbu;
			}
		    }
		    else
		    {
			if( 2*temp.denom*y < ( 2*temp.yl+1 )*denom )
			{
			    if( 2*temp.denom*z < ( 2*temp.zl+1 )*denom )
				temp = temp.rfd;
			    else
				temp = temp.rfu;
			}
			else
			{
			    if( 2*temp.denom*z < ( 2*temp.zl+1 )*denom )
				temp = temp.rbd;
			    else
				temp = temp.rbu;
			}
		    }

		    if( temp == null ) return( null );
		}
//		if( temp.status == 0) return(null);
		return(temp);
	}

	/*
	 * Function	get_existing_faces
	 * action:	if any of the faces on box have been found on any
	 *		other box use that information.
	 */

	public void get_existing_faces(Box_info box)
	{
		Box_info adjacient_box;

		adjacient_box = get_box(box.xl-1,box.yl,box.zl,box.denom);
		if( adjacient_box != null )
			box.ll = adjacient_box.rr;

		adjacient_box = get_box(box.xl+1,box.yl,box.zl,box.denom);
		if( adjacient_box != null )
			box.rr = adjacient_box.ll;

		adjacient_box = get_box(box.xl,box.yl-1,box.zl,box.denom);
		if( adjacient_box != null )
			box.ff = adjacient_box.bb;

		adjacient_box = get_box(box.xl,box.yl+1,box.zl,box.denom);
		if( adjacient_box != null )
			box.bb = adjacient_box.ff;

		adjacient_box = get_box(box.xl,box.yl,box.zl-1,box.denom);
		if( adjacient_box != null )
			box.dd = adjacient_box.uu;

		adjacient_box = get_box(box.xl,box.yl,box.zl+1,box.denom);
		if( adjacient_box != null )
			box.uu = adjacient_box.dd;
	}

	/*
	 * Function:	create_new_faces
	 * action:	if any of the faces have not already been found
	 *		create the apropriate information.
	 */

	public void create_new_faces(Box_info box)
	{
		if( box.ll == null )
		{
			box.ll = box.make_box_face(Key3D.FACE_LL);
		}
		if( box.rr == null )
		{
			box.rr = box.make_box_face(Key3D.FACE_RR);
		}
		if( box.ff == null )
		{
			box.ff = box.make_box_face(Key3D.FACE_FF);
		}
		if( box.bb == null )
		{
			box.bb = box.make_box_face(Key3D.FACE_BB);
		}
		if( box.dd == null )
		{
			box.dd = box.make_box_face(Key3D.FACE_DD);
		}
		if( box.uu == null )
		{
			box.uu = box.make_box_face(Key3D.FACE_UU);
		}
	}

	/*
	 * Function:	get_existing_edges
	 * action:	if any of the edges lie on existing faces of existing
	 *		boxes link the information.
	 */

	 public void get_existing_edges(Box_info box,Face_info face,Key3D code)
	{
		Box_info adjacient_box;

		switch( code )
		{
		case FACE_LL:

			/*** Do edge x_low of face LL ***/

			if( face.x_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: ll.x_low != null\n");
	*/
			else
			if(box.ff.x_low != null ) face.x_low = box.ff.x_low;
			else
			{
				adjacient_box = get_box(box.xl-1,box.yl-1,
					box.zl,box.denom);
				if( adjacient_box != null && adjacient_box.rr != null )
					face.x_low = adjacient_box.rr.x_high;
			}

			/*** Do edge x_high of face LL ***/

			if( face.x_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: ll.x_high != null\n");
	*/
			else
			if(box.bb.x_low != null ) face.x_high = box.bb.x_low;
			else
			{
				adjacient_box = get_box(box.xl-1,box.yl+1,
					box.zl,box.denom);
				if( adjacient_box != null && adjacient_box.rr != null )
					face.x_high = adjacient_box.rr.x_low;
			}

			/*** Do edge y_low of face LL ***/

			if( face.y_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: ll.y_low != null\n");
	*/
			else
			if(box.dd.x_low != null ) face.y_low = box.dd.x_low;
			else
			{
				adjacient_box = get_box(box.xl-1,box.yl,
					box.zl-1,box.denom);
				if( adjacient_box != null && adjacient_box.rr != null )
					face.y_low = adjacient_box.rr.y_high;
			}

			/*** Do edge y_high of face LL ***/

			if( face.y_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: ll.y_high != null\n");
	*/
			else
			if(box.uu.x_low != null ) face.y_high = box.uu.x_low;
			else
			{
				adjacient_box = get_box(box.xl-1,box.yl,
					box.zl+1,box.denom);
				if( adjacient_box != null && adjacient_box.rr != null )
					face.y_high = adjacient_box.rr.y_low;
			}
			break;

		case FACE_RR:

			/*** Do edge x_low of face RR ***/

			if( face.x_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: rr.x_low != null\n");
	*/
			else
			if(box.ff.x_high != null ) face.x_low = box.ff.x_high;
			else
			{
				adjacient_box = get_box(box.xl+1,box.yl-1,
					box.zl,box.denom);
				if( adjacient_box != null && adjacient_box.ll != null )
					face.x_low = adjacient_box.ll.x_high;
			}

			/*** Do edge x_high of face RR ***/

			if( face.x_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: rr.x_high != null\n");
	*/
			else
			if(box.bb.x_high != null ) face.x_high = box.bb.x_high;
			else
			{
				adjacient_box = get_box(box.xl+1,box.yl+1,
					box.zl,box.denom);
				if( adjacient_box != null && adjacient_box.ll != null )
					face.x_high = adjacient_box.ll.x_low;
			}

			/*** Do edge y_low of face LL ***/

			if( face.y_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: rr.y_low != null\n");
	*/
			else
			if(box.dd.x_high != null ) face.y_low = box.dd.x_high;
			else
			{
				adjacient_box = get_box(box.xl+1,box.yl,
					box.zl-1,box.denom);
				if( adjacient_box != null && adjacient_box.ll != null )
					face.y_low = adjacient_box.ll.y_high;
			}

			/*** Do edge y_high of face LL ***/

			if( face.y_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: rr.y_high != null\n");
	*/
			else
			if(box.uu.x_high != null ) face.y_high = box.uu.x_high;
			else
			{
				adjacient_box = get_box(box.xl+1,box.yl,
					box.zl+1,box.denom);
				if( adjacient_box != null && adjacient_box.ll != null )
					face.y_high = adjacient_box.ll.y_low;
			}
			break;

		case FACE_FF:

			/*** Do edge x_low of face FF ***/

			if( face.x_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: ff.x_low != null\n");
	*/
			else
			if(box.ll.x_low != null ) face.x_low = box.ll.x_low;
			else
			{
				adjacient_box = get_box(box.xl-1,box.yl-1,
					box.zl,box.denom);
				if( adjacient_box != null && adjacient_box.bb != null )
					face.x_low = adjacient_box.bb.x_high;
			}

			/*** Do edge x_high of face FF ***/

			if( face.x_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: ff.x_high != null\n");
	*/
			else
			if(box.rr.x_low != null ) face.x_high = box.rr.x_low;
			else
			{
				adjacient_box = get_box(box.xl+1,box.yl-1,
					box.zl,box.denom);
				if( adjacient_box != null && adjacient_box.bb != null )
					face.x_high = adjacient_box.bb.x_low;
			}

			/*** Do edge y_low of face FF ***/

			if( face.y_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: ff.y_low != null\n");
	*/
			else
			if(box.dd.y_low != null ) face.y_low = box.dd.y_low;
			else
			{
				adjacient_box = get_box(box.xl,box.yl-1,
					box.zl-1,box.denom);
				if( adjacient_box != null && adjacient_box.bb != null )
					face.y_low = adjacient_box.bb.y_high;
			}

			/*** Do edge y_high of face FF ***/

			if( face.y_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: ff.y_high != null\n");
	*/
			else
			if(box.uu.y_low != null ) face.y_high = box.uu.y_low;
			else
			{
				adjacient_box = get_box(box.xl,box.yl-1,
					box.zl+1,box.denom);
				if( adjacient_box != null && adjacient_box.bb != null )
					face.y_high = adjacient_box.bb.y_low;
			}
			break;

		case FACE_BB:

			/*** Do edge x_low of face BB ***/

			if( face.x_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: bb.x_low != null\n");
	*/
			else
			if(box.ll.x_high != null ) face.x_low = box.ll.x_high;
			else
			{
				adjacient_box = get_box(box.xl-1,box.yl+1,
					box.zl,box.denom);
				if( adjacient_box != null && adjacient_box.ff != null )
					face.x_low = adjacient_box.ff.x_high;
			}

			/*** Do edge x_high of face BB ***/

			if( face.x_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: bb.x_high != null\n");
	*/
			else
			if(box.rr.x_high != null ) face.x_high = box.rr.x_high;
			else
			{
				adjacient_box = get_box(box.xl+1,box.yl+1,
					box.zl,box.denom);
				if( adjacient_box != null && adjacient_box.ff != null )
					face.x_high = adjacient_box.ff.x_low;
			}

			/*** Do edge y_low of face BB ***/

			if( face.y_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: bb.y_low != null\n");
	*/
			else
			if(box.dd.y_high != null ) face.y_low = box.dd.y_high;
			else
			{
				adjacient_box = get_box(box.xl,box.yl+1,
					box.zl-1,box.denom);
				if( adjacient_box != null && adjacient_box.ff != null )
					face.y_low = adjacient_box.ff.y_high;
			}

			/*** Do edge y_high of face BB ***/

			if( face.y_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: bb.y_high != null\n");
	*/
			else
			if(box.uu.y_high != null ) face.y_high = box.uu.y_high;
			else
			{
				adjacient_box = get_box(box.xl,box.yl+1,
					box.zl+1,box.denom);
				if( adjacient_box != null && adjacient_box.ff != null )
					face.y_high = adjacient_box.ff.y_low;
			}
			break;

		case FACE_DD:

			/*** Do edge x_low of face DD ***/

			if( face.x_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: dd.x_low != null\n");
	*/
			else
			if(box.ll.y_low != null ) face.x_low = box.ll.y_low;
			else
			{
				adjacient_box = get_box(box.xl-1,box.yl,
					box.zl-1,box.denom);
				if( adjacient_box != null && adjacient_box.uu != null )
					face.x_low = adjacient_box.uu.x_high;
			}

			/*** Do edge x_high of face DD ***/

			if( face.x_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: dd.x_high != null\n");
	*/
			else
			if(box.rr.y_low != null ) face.x_high = box.rr.y_low;
			else
			{
				adjacient_box = get_box(box.xl+1,box.yl,
					box.zl-1,box.denom);
				if( adjacient_box != null && adjacient_box.uu != null )
					face.x_high = adjacient_box.uu.x_low;
			}

			/*** Do edge y_low of face DD ***/

			if( face.y_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: dd.y_low != null\n");
	*/
			else
			if(box.ff.y_low != null ) face.y_low = box.ff.y_low;
			else
			{
				adjacient_box = get_box(box.xl,box.yl-1,
					box.zl-1,box.denom);
				if( adjacient_box != null && adjacient_box.uu != null )
					face.y_low = adjacient_box.uu.y_high;
			}

			/*** Do edge y_high of face DD ***/

			if( face.y_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: dd.y_high != null\n");
	*/
			else
			if(box.bb.y_low != null ) face.y_high = box.bb.y_low;
			else
			{
				adjacient_box = get_box(box.xl,box.yl+1,
					box.zl-1,box.denom);
				if( adjacient_box != null && adjacient_box.uu != null )
					face.y_high = adjacient_box.uu.y_low;
			}
			break;

		case FACE_UU:

			/*** Do edge x_low of face UU ***/

			if( face.x_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: uu.x_low != null\n");
	*/
			else
			if(box.ll.y_high != null ) face.x_low = box.ll.y_high;
			else
			{
				adjacient_box = get_box(box.xl-1,box.yl,
					box.zl+1,box.denom);
				if( adjacient_box != null && adjacient_box.dd != null )
					face.x_low = adjacient_box.dd.x_high;
			}

			/*** Do edge x_high of face UU ***/

			if( face.x_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: uu.x_high != null\n");
	*/
			else
			if(box.rr.y_high != null ) face.x_high = box.rr.y_high;
			else
			{
				adjacient_box = get_box(box.xl+1,box.yl,
					box.zl+1,box.denom);
				if( adjacient_box != null && adjacient_box.dd != null )
					face.x_high = adjacient_box.dd.x_low;
			}

			/*** Do edge y_low of face UU ***/

			if( face.y_low != null )
				{}
	/*
				System.err.printf("get_existing_edge: uu.y_low != null\n");
	*/
			else
			if(box.ff.y_high != null ) face.y_low = box.ff.y_high;
			else
			{
				adjacient_box = get_box(box.xl,box.yl-1,
					box.zl+1,box.denom);
				if( adjacient_box != null && adjacient_box.dd != null )
					face.y_low = adjacient_box.dd.y_high;
			}

			/*** Do edge y_high of face UU ***/

			if( face.y_high != null )
				{}
	/*
				System.err.printf("get_existing_edge: uu.y_high != null\n");
	*/
			else
			if(box.bb.y_high != null ) face.y_high = box.bb.y_high;
			else
			{
				adjacient_box = get_box(box.xl,box.yl+1,
					box.zl+1,box.denom);
				if( adjacient_box != null && adjacient_box.dd != null )
					face.y_high = adjacient_box.dd.y_low;
			}

			break;
        default:
            break;
		} /* end switch */
	}

	/*
	 * Function:	create_new_face
	 * action:	if any of the edge have not already been found
	 *		create the apropriate information.
	 */

	public void create_new_edges(Face_info face)
	{
		if( face.x_low == null )
		{
			face.x_low = face.make_face_edge(Face_info.Type.X_LOW);
		}
		if( face.x_high == null )
		{
			face.x_high = face.make_face_edge(Face_info.Type.X_HIGH);
		}
		if( face.y_low == null )
		{
			face.y_low = face.make_face_edge(Face_info.Type.Y_LOW);
		}
		if( face.y_high == null )
		{
			face.y_high = face.make_face_edge(Face_info.Type.Y_HIGH);
		}
	}


	/**
	 * Takes information about face and puts it in the
	 *		sub faces. Does not find internal edges.
	 */

	public void split_face(Face_info face,Face_info face1,Face_info face2,
				Face_info face3,Face_info face4)
	{
		/*** First put all the edges on the appropriate sub faces ***/

		if( face.x_low != null )
		{
			face.x_low.split_edge();
			face1.x_low = face.x_low.left;
			face3.x_low = face.x_low.right;
		}

		if( face.x_high != null )
		{
			face.x_high.split_edge();
			face2.x_high = face.x_high.left;
			face4.x_high = face.x_high.right;
		}

		if( face.y_low != null )
		{
			face.y_low.split_edge();
			face1.y_low = face.y_low.left;
			face2.y_low = face.y_low.right;
		}

		if( face.y_high != null )
		{
			face.y_high.split_edge();
			face3.y_high = face.y_high.left;
			face4.y_high = face.y_high.right;
		}

		/*** Now create the internal edges ***/

		face1.x_high = face2.x_low = 
			 face1.make_face_edge(Face_info.Type.X_HIGH);

		face3.x_high = face4.x_low = 
			 face3.make_face_edge(Face_info.Type.X_HIGH);

		face1.y_high = face3.y_low = 
			 face1.make_face_edge(Face_info.Type.Y_HIGH);

		face2.y_high = face4.y_low = 
			 face2.make_face_edge(Face_info.Type.Y_HIGH);
	}

	/**
	 * Take all the nodes on face and put them on the correct subface.
	 */

	void distribute_nodes(Face_info face,Face_info face1,Face_info face2,
				Face_info face3,Face_info face4)
	{
		
		if(face.nodes!=null)
		{
			ListIterator<Node_info> li =  face.nodes.listIterator();
		while(li.hasNext())
		{
			Node_info node = li.next();

		    switch(face.type)
		    {
		    case FACE_LL: case FACE_RR:
			if(node.sol.yl * face4.denom
				<  node.sol.denom * face4.yl )

				if(node.sol.zl * face4.denom
					< node.sol.denom * face4.zl )
				{
					/* add to face1 */
					
					li.remove();
					face1.add_node(node);
				}
				else
				{
					/* add to face 3 */
			
					li.remove();
					face3.add_node(node);
				}
			else
				if(node.sol.zl * face4.denom
					< node.sol.denom * face4.zl )
				{
					/* add to face2 */

					li.remove();
					face2.add_node(node);
				}
				else
				{
					/* add to face 4 */
			
					li.remove();
					face4.add_node(node);
				}
			break;

		    case FACE_FF: case FACE_BB:
			if(node.sol.xl * face4.denom
				<  node.sol.denom * face4.xl )

				if(node.sol.zl * face4.denom
					< node.sol.denom * face4.zl )
				{
					/* add to face1 */

					li.remove();
					face1.add_node(node);
				}
				else
				{
					/* add to face 3 */
			
					li.remove();
					face3.add_node(node);
				}
			else
				if(node.sol.zl * face4.denom
					< node.sol.denom * face4.zl )
				{
					/* add to face2 */

					li.remove();
					face2.add_node(node);
				}
				else
				{
					/* add to face 4 */
			
					li.remove();
					face4.add_node(node);
				}
			break;

		    case FACE_UU: case FACE_DD:
			if(node.sol.xl * face4.denom
				<  node.sol.denom * face4.xl )

				if(node.sol.yl * face4.denom
					< node.sol.denom * face4.yl )
				{
					/* add to face1 */

					li.remove();
					face1.add_node(node);
				}
				else
				{
					/* add to face 3 */
			
					li.remove();
					face3.add_node(node);
				}
			else
				if(node.sol.yl * face4.denom
					< node.sol.denom * face4.yl )
				{
					/* add to face2 */

					li.remove();
					face2.add_node(node);
				}
				else
				{
					/* add to face 4 */
			
					li.remove();
					face4.add_node(node);
				}
			break;
		default:
			BoxClevA.log.printf("distribute_nodes: bad type %d\n",face.type);
			break;
		    }  /* end switch */
		} /* end while */
		}
	}

	
	/** 
	 * Takes the information from box and puts it into
	 *		the sub boxes. Creates internal faces but does not
	 *		find the internal solutions.
	 */

	public void split_box(Box_info box,Box_info lfd,Box_info rfd,Box_info lbd,Box_info rbd,
				Box_info lfu,Box_info rfu,Box_info lbu,Box_info rbu)
	{
		if( box.ll.lb == null )
		{
			/*** create new faces ***/
			Face_info faces[] = box.ll.make_sub_faces();
			lfd.ll = faces[0];
			lbd.ll = faces[1];
			lfu.ll = faces[2];
			lbu.ll = faces[3];
		
			box.ll.lb = lfd.ll;
                        box.ll.lt = lfu.ll;
                        box.ll.rb = lbd.ll;
                        box.ll.rt = lbu.ll;
			split_face(box.ll,lfd.ll,lbd.ll,lfu.ll,lbu.ll);
		}
		else
		{
			/*** face has already been split so use this info **/

			lfd.ll = box.ll.lb;
			lbd.ll = box.ll.rb;
			lfu.ll = box.ll.lt;
			lbu.ll = box.ll.rt;
		}
		distribute_nodes(box.ll,lfd.ll,lbd.ll,lfu.ll,lbu.ll);

		if( box.rr.lb == null )
		{
			/*** create new faces ***/

			Face_info faces[] = box.rr.make_sub_faces();
			rfd.rr = faces[0];
			rbd.rr = faces[1];
			rfu.rr = faces[2];
			rbu.rr = faces[3];
		
                        box.rr.lb = rfd.rr;//0
                        box.rr.lt = rfu.rr;//2
                        box.rr.rb = rbd.rr;//1
                        box.rr.rt = rbu.rr;//3
			split_face(box.rr,rfd.rr,rbd.rr,rfu.rr,rbu.rr);
		}
		else
		{
			/*** face has already been split so use this info **/

			rfd.rr = box.rr.lb;
			rbd.rr = box.rr.rb;
			rfu.rr = box.rr.lt;
			rbu.rr = box.rr.rt;
		}
		distribute_nodes(box.rr,rfd.rr,rbd.rr,rfu.rr,rbu.rr);

		if( box.ff.lb == null )
		{
			/*** create new faces ***/

			Face_info faces[] = box.ff.make_sub_faces();
			lfd.ff = faces[0];
			rfd.ff = faces[1];
			lfu.ff = faces[2];
			rfu.ff = faces[3];
		
                        box.ff.lb = lfd.ff;
                        box.ff.lt = lfu.ff;
                        box.ff.rb = rfd.ff;
                        box.ff.rt = rfu.ff;
			split_face(box.ff,lfd.ff,rfd.ff,lfu.ff,rfu.ff);
		}
		else
		{
			/*** face has already been split so use this info **/

			lfd.ff = box.ff.lb;
			rfd.ff = box.ff.rb;
			lfu.ff = box.ff.lt;
			rfu.ff = box.ff.rt;
		}
		distribute_nodes(box.ff,lfd.ff,rfd.ff,lfu.ff,rfu.ff);

		if( box.bb.lb == null )
		{
			/*** create new faces ***/

			Face_info faces[] = box.bb.make_sub_faces();
			lbd.bb = faces[0];
			rbd.bb = faces[1];
			lbu.bb = faces[2];
			rbu.bb = faces[3];
		
                        box.bb.lb = lbd.bb; //0
                        box.bb.lt = lbu.bb; //2
                        box.bb.rb = rbd.bb; //1
                        box.bb.rt = rbu.bb; //3
			split_face(box.bb,lbd.bb,rbd.bb,lbu.bb,rbu.bb);
		}
		else
		{
			/*** face has already been split so use this info **/

			lbd.bb = box.bb.lb;
			rbd.bb = box.bb.rb;
			lbu.bb = box.bb.lt;
			rbu.bb = box.bb.rt;
		}
		distribute_nodes(box.bb,lbd.bb,rbd.bb,lbu.bb,rbu.bb);

		if( box.dd.lb == null )
		{
			/*** create new faces ***/

			Face_info faces[] = box.dd.make_sub_faces();
			lfd.dd = faces[0];
			rfd.dd = faces[1];
			lbd.dd = faces[2];
			rbd.dd = faces[3];
		
                        box.dd.lb = lfd.dd;
                        box.dd.lt = lbd.dd;
                        box.dd.rb = rfd.dd;
                        box.dd.rt = rbd.dd;
			split_face(box.dd,lfd.dd,rfd.dd,lbd.dd,rbd.dd);
		}
		else
		{
			/*** face has already been split so use this info **/

			lfd.dd = box.dd.lb;
			rfd.dd = box.dd.rb;
			lbd.dd = box.dd.lt;
			rbd.dd = box.dd.rt;
		}
		distribute_nodes(box.dd,lfd.dd,rfd.dd,lbd.dd,rbd.dd);

		if( box.uu.lb == null )
		{
			/*** create new faces ***/

			Face_info faces[] = box.uu.make_sub_faces();
			lfu.uu = faces[0];
			rfu.uu = faces[1];
			lbu.uu = faces[2];
			rbu.uu = faces[3];
		
                        box.uu.lb = lfu.uu; //0
                        box.uu.lt = lbu.uu; //2
                        box.uu.rb = rfu.uu; //1
                        box.uu.rt = rbu.uu; //3
			split_face(box.uu,lfu.uu,rfu.uu,lbu.uu,rbu.uu);
		}
		else
		{
			/*** face has already been split so use this info **/

			lfu.uu = box.uu.lb;
			rfu.uu = box.uu.rb;
			lbu.uu = box.uu.lt;
			rbu.uu = box.uu.rt;
		}
		distribute_nodes(box.uu,lfu.uu,rfu.uu,lbu.uu,rbu.uu);

		/*** Now create internal faces ***/

		lfd.rr = rfd.ll = new Face_info(Key3D.FACE_RR,box.xl*2+1,box.yl*2,box.zl*2,
				box.denom*2);

		lbd.rr = rbd.ll = new Face_info(Key3D.FACE_RR,box.xl*2+1,box.yl*2+1,box.zl*2,
				box.denom*2);

		lfu.rr = rfu.ll = new Face_info(Key3D.FACE_RR,box.xl*2+1,box.yl*2,box.zl*2+1,
				box.denom*2);

		lbu.rr = rbu.ll = new Face_info(Key3D.FACE_RR,box.xl*2+1,box.yl*2+1,box.zl*2+1,
				box.denom*2);

		/* Now internal front/back faces */

		lfd.bb = lbd.ff = new Face_info(Key3D.FACE_BB,box.xl*2,box.yl*2+1,box.zl*2,
				box.denom*2);

		rfd.bb = rbd.ff = new Face_info(Key3D.FACE_BB,box.xl*2+1,box.yl*2+1,box.zl*2,
				box.denom*2);

		lfu.bb = lbu.ff = new Face_info(Key3D.FACE_BB,box.xl*2,box.yl*2+1,box.zl*2+1,
				box.denom*2);

		rfu.bb = rbu.ff = new Face_info(Key3D.FACE_BB,box.xl*2+1,box.yl*2+1,box.zl*2+1,
				box.denom*2);

		/* Now internal down/up faces */

		lfd.uu = lfu.dd = new Face_info(Key3D.FACE_UU,box.xl*2,box.yl*2,box.zl*2+1,
				box.denom*2);

		rfd.uu = rfu.dd = new Face_info(Key3D.FACE_UU,box.xl*2+1,box.yl*2,box.zl*2+1,
				box.denom*2);

		lbd.uu = lbu.dd = new Face_info(Key3D.FACE_UU,box.xl*2,box.yl*2+1,box.zl*2+1,
				box.denom*2);

		rbd.uu = rbu.dd = new Face_info(Key3D.FACE_UU,box.xl*2+1,box.yl*2+1,box.zl*2+1,
				box.denom*2);
	}

}
