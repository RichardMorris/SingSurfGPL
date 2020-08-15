package org.singsurf.singsurf.asurf;

import static org.singsurf.singsurf.asurf.Key3D.FACE_BB;
import static org.singsurf.singsurf.asurf.Key3D.FACE_DD;
import static org.singsurf.singsurf.asurf.Key3D.FACE_FF;
import static org.singsurf.singsurf.asurf.Key3D.FACE_LL;
import static org.singsurf.singsurf.asurf.Key3D.FACE_RR;
import static org.singsurf.singsurf.asurf.Key3D.FACE_UU;
import static org.singsurf.singsurf.asurf.Key3D.Z_AXIS;

import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern1D;
import org.singsurf.singsurf.acurve.Bern2D;
import org.singsurf.singsurf.asurf.Converger.Solve1DResult;

class BoxGenerator implements Runnable {
		
		final Boxclev boxclev;
		final BoxSolver boxsolver;
		final FaceSolver facesolver;
		final Converger converger;
		final Bern3DContext ctx;
		Box_info box;
		Bern3D bern;
		final Box_info bigbox;
		int boxesdone=0;
		
	public BoxGenerator(Boxclev boxclev, Bern3DContext ctx,Box_info box, Bern3D bb) {
			super();
			this.boxclev = boxclev;
			this.ctx = ctx;
			this.boxsolver = new BoxSolver(boxclev,this,ctx);
			this.facesolver = new FaceSolver(boxclev,this,ctx);
			this.converger = new Converger(ctx);
			this.box = box;
			this.bern = bb;
			this.bigbox = box;
		}

	public void run() {
		try {
			System.out.println("Starting GenBox");
			generate_boxes(box,bern);
			System.out.println("Ending GenBox");
		} catch (AsurfException e) {
			e.printStackTrace();
		} finally {
			converger.fini();
		}
	}
	
	boolean generate_boxes(Box_info box, Bern3D bb) throws AsurfException {
		boolean flag;

		if (Boxclev.CHECK_INTERUPTS) {

				int xl, yl, zl, denom;
				double percent = 0.0;
				xl = box.xl - bigbox.xl * box.denom / bigbox.denom;
				yl = box.yl - bigbox.yl * box.denom / bigbox.denom;
				zl = box.zl - bigbox.zl * box.denom / bigbox.denom;

				for (denom = box.denom; denom > 1; denom /= 2) {
					percent += (xl % 2);
					percent /= 2.0;
					xl /= 2;
					percent += (yl % 2);
					percent /= 2.0;
					yl /= 2;
					percent += (zl % 2);
					percent /= 2.0;
					zl /= 2;
				}
				boxclev.report_progress(bigbox, percent);
		}

		if (boxclev.global_denom == box.denom) {
			if (boxclev.global_selx != -1)
				if (box.xl != boxclev.global_selx)
					return true;
			if (boxclev.global_sely != -1)
				if (box.yl != boxclev.global_sely)
					return true;
			if (boxclev.global_selz != -1)
				if (box.zl != boxclev.global_selz)
					return true;
		}

		if (bb.allOneSign()) /* no component in box */
		{
			if (Boxclev.PRINT_GEN_BOXES) {
				BoxClevA.log.printf("Generate_boxes: box (%d,%d,%d)/%d no conponant\n", box.xl, box.yl, box.zl,
						box.denom);
			}
			box.status = BoxClevA.EMPTY;
			if (boxclev.knitFacets) {
				boxclev.topology.get_existing_faces(box);
				boxclev.topology.create_new_faces(box);
				boxclev.knitter.queuebox(box);
			} else {
//				box.release_from_parent();
			}
			int levels = boxclev.RESOLUTION / box.denom;
			this.boxesdone += levels * levels * levels;
			return (true);
		}

		/*** If all derivatives non zero and the region is sufficiently ***/
		/*** small then draw the surface. ***/

		if (box.denom >= boxclev.RESOLUTION) {
			if (Boxclev.PRINT_GEN_BOXES) {
				BoxClevA.log.printf("Generate_boxes: box (%d,%d,%d)/%d LEAF\n", box.xl, box.yl, box.zl, box.denom);
			}

			boolean flg = find_box(box, bb);
			int levels = boxclev.RESOLUTION / box.denom;
			this.boxesdone += levels * levels * levels;
			return flg;
		} else { /**** Sub-divide the region into 8 sub boxes. ****/
			if (Boxclev.PRINT_GEN_BOXES) {
				BoxClevA.log.printf("Generate_boxes: box (%d,%d,%d)/%d NODE\n", box.xl, box.yl, box.zl, box.denom);
			}

			Bern3DContext.OctBern temp = ctx.reduce(bb);
			box.sub_devide_box();
			flag = (generate_boxes(box.lfd, temp.lfd) && generate_boxes(box.rfd, temp.rfd)
					&& generate_boxes(box.lbd, temp.lbd) && generate_boxes(box.rbd, temp.rbd)
					&& generate_boxes(box.lfu, temp.lfu) && generate_boxes(box.rfu, temp.rfu)
					&& generate_boxes(box.lbu, temp.lbu) && generate_boxes(box.rbu, temp.rbu));

			if (boxclev.knitFacets) {
				// box.lfd = null;
				// box.lfu = null;
				// box.lbd = null;
				// box.lbu = null;
				// box.rfd = null;
				// box.rfu = null;
				// box.rbd = null;
				// box.rbu = null;
			} else {
				box.lfd.free_bit(false, false, false);
				box.lfu.free_bit(false, false, true);
				box.lbd.free_bit(false, true, false);
				box.lbu.free_bit(false, true, true);
				box.rfd.free_bit(true, false, false);
				box.rfu.free_bit(true, false, true);
				box.rbd.free_bit(true, true, false);
				box.rbu.free_bit(true, true, true);
			}
			return (flag);
		}
	}

	/**
	 * Finds all solutions, nodes and singularities for a box together with the
	 * topological linkage information.
	 */
	boolean find_box(final Box_info box, Bern3D bb) throws AsurfException {
		boxclev.lockBox(box);
		
		try {
			boxclev.topology.get_existing_faces(box);
			boxclev.topology.create_new_faces(box);

			find_all_faces(box, bb);
			boxsolver.link_nodes(box, bb); 
			box.status = BoxClevA.FOUND_EVERYTHING;
		} finally {
			boxclev.unlockBox(box);
		}
		if (boxclev.knitFacets) {
			boxclev.knitter.queuebox(box);
		} else if (!boxclev.littleFacets) {
			boxclev.facets.make_facets(box);
			boxclev.triangulate_and_plot(box);
		}
		return (true);
	}




	/**
	 * For all the faces of the box find the information about the solutions and
	 * nodes. takes information already found about faces of adjacent boxes.
	 */

	void find_all_faces(Box_info box, Bern3D bb) throws AsurfException {

		/* none of the faces are internal */

		find_face(box, bb, box.ll, FACE_LL, false);
		find_face(box, bb, box.rr, FACE_RR, false);
		find_face(box, bb, box.ff, FACE_FF, false);
		find_face(box, bb, box.bb, FACE_BB, false);
		find_face(box, bb, box.dd, FACE_DD, false);
		find_face(box, bb, box.uu, FACE_UU, false);
	}



	/**
	 * Find all the information about solutions and nodes on face.
	 */

	void find_face(Box_info box, Bern3D bb, Face_info face, Key3D code, boolean internal)
			throws AsurfException {
		Bern2D aa, dx, dy, dz, d2 = null;
		Bern3D temp, temp2;

		if(face.status == BoxClevA.FOUND_EVERYTHING)
			return;
		
		aa = bb.make_bern2D_of_box(code);
		if (aa.allOneSign() != 0) {
			face.status = BoxClevA.FOUND_EVERYTHING;
			return;
		}
		if (face.type == FACE_LL || face.type == FACE_RR) {
			temp = bb.diffX();
			dx = temp.make_bern2D_of_box(code);
			if (Boxclev.USE_2ND_DERIV) {
				if (dx.allOneSign() == 0) {
					temp2 = temp.diffX();
					d2 = temp2.make_bern2D_of_box(code);
				}
			}
		} else
			dx = aa.diffX();

		if (face.type == FACE_FF || face.type == FACE_BB) {
			temp = bb.diffY();
			dy = temp.make_bern2D_of_box(code);
			if (Boxclev.USE_2ND_DERIV) {
				if (dx.allOneSign() == 0) {
					temp2 = temp.diffY();
					d2 = temp2.make_bern2D_of_box(code);
				}
			}
		} else if (face.type == FACE_LL || face.type == FACE_RR)
			dy = aa.diffX();
		else
			dy = aa.diffY();

		if (face.type == FACE_UU || face.type == FACE_DD) {
			temp = bb.diffZ();
			dz = temp.make_bern2D_of_box(code);
			if (Boxclev.USE_2ND_DERIV) {
				if (dz.allOneSign() == 0) {
					temp2 = temp.diffZ();
					d2 = temp2.make_bern2D_of_box(code);
				}
			}
		} else
			dz = aa.diffY();

		find_all_edges(box, face, aa, dx, dy, dz, code);

		facesolver.link_face(face, face, aa, dx, dy, dz, d2, internal);
		face.status = BoxClevA.FOUND_EVERYTHING;
	}

	/**
	 * Finds all the solutions on the edges of a face, uses the information already
	 * found from adjacent faces.
	 */

	private void find_all_edges(Box_info box, Face_info face, Bern2D bb, Bern2D dx, Bern2D dy, Bern2D dz, Key3D code)
			throws AsurfException {
//		boxclev.topology.get_make_edges(box, face, code);
		boxclev.topology.get_existing_edges(box, face, code);
		boxclev.topology.create_new_edges(face);
		find_edge(face.x_low, bb, dx, dy, dz, Face_info.X_LOW);
		find_edge(face.x_high, bb, dx, dy, dz, Face_info.X_HIGH);
		find_edge(face.y_low, bb, dx, dy, dz, Face_info.Y_LOW);
		find_edge(face.y_high, bb, dx, dy, dz, Face_info.Y_HIGH);
	}

	/*
	 * Finds all the solutions on an edge.
	 */

	/**
	 * @param edge
	 * @param bb
	 * @param dx
	 * @param dy
	 * @param dz
	 * @param code
	 * @throws AsurfException
	 */
	void find_edge(Edge_info edge, Bern2D bb, Bern2D dx, Bern2D dy, Bern2D dz, int code) throws AsurfException {
		Bern1D aa;
		Bern1D dx1;
		Bern1D dy1;
		Bern1D dz1;
		if (edge.status == BoxClevA.FOUND_EVERYTHING)
			return;
		aa = bb.make_bern1D_of_face(code);
		dx1 = dx.make_bern1D_of_face(code);
		dy1 = dy.make_bern1D_of_face(code);
		dz1 = dz.make_bern1D_of_face(code);
		find_sols_on_edge(edge, aa, dx1, dy1, dz1);
		edge.status = BoxClevA.FOUND_EVERYTHING;
	}

	/**
	 * Finds all the solutions on the edge
	 */
	void find_sols_on_edge(Edge_info edge, Bern1D bb, Bern1D dx, Bern1D dy, Bern1D dz) throws AsurfException {
		int f1, f2, f3;

		if (bb.getSign() != 0) {
			edge.status = BoxClevA.FOUND_EVERYTHING;
			return;
		}
		
		f1 = dx.getSign();
		f2 = dy.getSign();
		f3 = dz.getSign();

		if ((f1 == 0 || f2 == 0 || f3 == 0) && edge.denom < boxclev.LINK_EDGE_LEVEL) {
			Bern1D.BinBern aa, dx1, dy1, dz1;

			aa = bb.reduce();
			if (f1 > 0)
				dx1 = Bern1D.posBern1D.reduce();
			else if (f1 < 0)
				dx1 = Bern1D.negBern1D.reduce();
			else if (edge.type == Key3D.X_AXIS)
				dx1 = aa.binDiff1D();
			else
				dx1 = dx.reduce();

			if (f2 > 0)
				dy1 = Bern1D.posBern1D.reduce();
			else if (f2 < 0)
				dy1 = Bern1D.negBern1D.reduce();
			else if (edge.type == Key3D.Y_AXIS)
				dy1 = aa.binDiff1D();
			else
				dy1 = dy.reduce();

			if (f3 > 0)
				dz1 = Bern1D.posBern1D.reduce();
			else if (f3 < 0)
				dz1 = Bern1D.negBern1D.reduce();
			else if (edge.type == Z_AXIS)
				dz1 = aa.binDiff1D();
			else
				dz1 = dz.reduce();

			Edge_info edges[] = edge.subdevideedge();
			edge.left = edges[0];
			edge.right = edges[1];

			find_sols_on_edge(edge.left, aa.l, dx1.l, dy1.l, dz1.l);
			find_sols_on_edge(edge.right, aa.r, dx1.r, dy1.r, dz1.r);

			if (aa.r.coeff[0] == 0.0) {
				edge.sol = new Sol_info(edge.type, edge.xl, edge.yl, edge.zl, edge.denom, 0.5);
				edge.sol.dx = f1;
				edge.sol.dy = f2;
				edge.sol.dz = f3;
			}
			edge.status = BoxClevA.FOUND_EVERYTHING;
			return;
		}

		Bern1D deriv_bern = edge.type == Key3D.X_AXIS ? dx : edge.type == Key3D.Y_AXIS ? dy : dz;
		if (deriv_bern == Bern1D.posBern1D || deriv_bern == Bern1D.negBern1D || deriv_bern == Bern1D.zeroBern1D) {
			deriv_bern = bb.diff();
		}

		/*** Either a simple interval or at bottom of tree ***/

		Solve1DResult solveres;
		if ((f1 != 0 && f2 != 0 && f3 != 0) || bb.allOneSignDeriv()) {
			solveres = converger.converge_edge(edge, bb, deriv_bern);
		} else {

			if (Boxclev.NON_GENERIC_EDGE) {
				if ((bb.coeff[0]) * (bb.coeff[bb.xord]) > 0) {
					edge.status = BoxClevA.FOUND_EVERYTHING;
					return;
				}
			}
			solveres = converger.converge_edge(edge, bb, deriv_bern);
		}
		double rootm = solveres.root;
		if (rootm < 0.0 || rootm > 1.0)
		rootm = 0.5;
		
		edge.sol = new Sol_info(edge.type, edge.xl, edge.yl, edge.zl, edge.denom, rootm);
		edge.sol.dx = f1;
		edge.sol.dy = f2;
		edge.sol.dz = f3;
		edge.sol.setValue(solveres.val);
		if (f1 == 0 || f2 == 0 || f3 == 0) {
			/*** Can't work out derivatives easily ***/
			/*** use actual values ***/
			double res;
			double[] vec = edge.sol.calc_relative_pos();
			if (f1 == 0) {
				res = ctx.evalbern3D(ctx.Dx, vec[0], vec[1], vec[2]);
				if (res < 0)
					edge.sol.dx = -1;
				if (res > 0)
					edge.sol.dx = 1;
			}
			if (f2 == 0) {
				res = ctx.evalbern3D(ctx.Dy, vec[0], vec[1], vec[2]);
				if (res < 0)
					edge.sol.dy = -1;
				if (res > 0)
					edge.sol.dy = 1;
			}
			if (f3 == 0) {
				res = ctx.evalbern3D(ctx.Dz, vec[0], vec[1], vec[2]);
				if (res < 0)
					edge.sol.dz = -1;
				if (res > 0)
					edge.sol.dz = 1;
			}

			if (Boxclev.PRINT_FIND_EDGE_SOL) {
				BoxClevA.log.printf("ERR: find_sol_on_edge: f1 %d f2 %d f3 %d\n", f1, f2, f3);
				BoxClevA.log.println(edge.sol);
			}
		}
		edge.status = BoxClevA.FOUND_EVERYTHING;
		return;
	}

}