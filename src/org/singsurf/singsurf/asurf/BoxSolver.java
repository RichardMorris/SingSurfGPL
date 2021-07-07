package org.singsurf.singsurf.asurf;

import static org.singsurf.singsurf.asurf.Key3D.BOX;
import static org.singsurf.singsurf.asurf.Key3D.FACE_BB;
import static org.singsurf.singsurf.asurf.Key3D.FACE_DD;
import static org.singsurf.singsurf.asurf.Key3D.FACE_FF;
import static org.singsurf.singsurf.asurf.Key3D.FACE_LL;
import static org.singsurf.singsurf.asurf.Key3D.FACE_RR;
import static org.singsurf.singsurf.asurf.Key3D.FACE_UU;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.asurf.Converger.Solve3Dresult;
import org.singsurf.singsurf.asurf.Converger.Solve3DresultWithSig;

public class BoxSolver {

	static class BoxPos {
		double x, y, z;

		public BoxPos(double x, double y, double z) {
			super();
			this.x = x;
			this.y = y;
			this.z = z;
		}

	}

	private static final boolean FAKE_SINGS = false;
	private static final boolean PRINT_LINK_CROSSCAP = false;
	static final private boolean PRINT_LINK_NODES = false;
	static final private boolean PRINT_LINK_SING = false;
	private static final boolean PRINT_SING = false;
	private static final boolean USE_ACTUAL_CURVATURE = false;

	private static final boolean USE_SINGULARITIES_ON_FACES = true;
	BoxClevA boxclev;
	BoxGenerator boxgen;
	private int failCountP = 0;
	private int failCountQ = 0;
	private int failCountR = 0;
	private int failCountS = 0;
	private int failCountT = 0;
	private int failCountU = 0;

	private int failCountV = 0;

	private int failCountW = 0;

	Bern3DContext ctx;
	public BoxSolver(BoxClevA boxclev, BoxGenerator boxgen, Bern3DContext ctx) {
		super();
		this.boxclev = boxclev;
		this.boxgen = boxgen;
		this.ctx = ctx;
	}

	
	boolean link_nodes(Box_info box, Bern3D bb) throws AsurfException {
		boolean f1 = false, f2 = false, f3 = false, doReduce;
		int count;
		List<Node_info> nodes = null;
		Bern3D dx, dy, dz;
		boolean flag = true;

		if (bb.allOneSign())
			return (true);

		dx = bb.diffX();
		dy = bb.diffY();
		dz = bb.diffZ();
		f1 = dx.allOneSign();
		f2 = dy.allOneSign();
		f3 = dz.allOneSign();

//		if (f1 && f2 && f3)
//			count = 0;
//		else {
			nodes = box.get_nodes_on_box_faces();
			count = nodes.size();
//		}

		if (PRINT_LINK_NODES) {
			BoxClevA.log.printf("ERR: link_nodes (%d,%d,%d)/%d: dx %d %d %d count %d\n", box.xl, box.yl, box.zl,
					box.denom, f1, f2, f3, count);
			BoxClevA.log.print(box.toString_brief());
		}

		doReduce = true;

		if (f1 && f2 && f3) {
			doReduce = false;
		} else if (count == 0) {

			/* test for isolated zeros: require !f1 !f2 !f3 and */
			/* no solutions on faces. */

			if (f1 || f2 || f3) {
				doReduce = false;
			} else if (box.ll.count_sol() != 0
					|| box.rr.count_sol() != 0
					|| box.ff.count_sol() != 0
					|| box.bb.count_sol() != 0
					|| box.dd.count_sol() != 0
					|| box.uu.count_sol() != 0 ) {
				/* non zero count return */

				doReduce = false;
			}

			/* no solutions, possible isolated zero */
		} else if (count == 2) {
			/* Only add links whose derivs match */

			if (matchNodes(nodes.get(0),nodes.get(1)) && (nodes.get(0).sol.getDx() != 0 || nodes.get(0).sol.getDy() != 0 || nodes.get(0).sol.getDz() != 0)) {
				box.add_node_link(nodes.get(0), nodes.get(1));
				doReduce = false;
			}
		} else if (count == 4) {
			int i;
			/*** possible for two different node_links across box ***/

			boolean flagReduce = false;
			for (i = 0; i < 4; ++i)
				if (nodes.get(i).sol.getDx() == 0 && nodes.get(i).sol.getDy() == 0 && nodes.get(i).sol.getDz() == 0)
					flagReduce = true;

			if (flagReduce) {
			} else if (matchNodes(nodes.get(0),nodes.get(1)) && matchNodes(nodes.get(2),nodes.get(3))) {
				if (matchNodes(nodes.get(0),nodes.get(2))) {
					flagReduce = true;
				} else {
					box.add_node_link(nodes.get(0), nodes.get(1));
					box.add_node_link(nodes.get(2), nodes.get(3));
					doReduce = false;
				}
			} else if (matchNodes(nodes.get(0),nodes.get(2)) && matchNodes(nodes.get(1),nodes.get(3))) {
				if (matchNodes(nodes.get(0),nodes.get(1))) {
					flagReduce = true;
				} else {

					box.add_node_link(nodes.get(0), nodes.get(2));
					box.add_node_link(nodes.get(1), nodes.get(3));
					doReduce = false;
				}
			} else if (matchNodes(nodes.get(0),nodes.get(3)) && matchNodes(nodes.get(1),nodes.get(2))) {
				if (matchNodes(nodes.get(0),nodes.get(2))) {
					flagReduce = true;
				} else {

					box.add_node_link(nodes.get(0), nodes.get(3));
					box.add_node_link(nodes.get(1), nodes.get(2));
					doReduce = false;
				}
			}
		}

		if (!doReduce && boxclev.refineCurvature) {
			// Sol_info test_sol = topology.get_first_sol_on_box(box);
			Double meancurve = calc_maximum_abs_mean_curvature(box);
			Double minnormlen = calc_min_norm_len(box);

			if (meancurve != null) { // only refine if at least one sol found
				int reqdepth = boxclev.RESOLUTION;
				if (meancurve > boxclev.curvatureLevel4 || minnormlen < boxclev.normlenlevel4) {
					reqdepth = boxclev.RESOLUTION * 16;
				} else if (meancurve > boxclev.curvatureLevel3 || minnormlen < boxclev.normlenlevel3) {
					reqdepth = boxclev.RESOLUTION * 8;
				} else if (meancurve > boxclev.curvatureLevel2 || minnormlen < boxclev.normlenlevel2) {
					reqdepth = boxclev.RESOLUTION * 4;
				} else if (meancurve > boxclev.curvatureLevel1 || minnormlen < boxclev.normlenlevel1) {
					reqdepth = boxclev.RESOLUTION * 2;
				}
				if (box.denom < reqdepth) {
					doReduce = true;
				}
			}
		}

		if (doReduce) {
			/*** Too difficult to handle, either sub-devide or create a node ***/

			if (box.denom >= boxclev.LINK_SING_LEVEL) {
				short sigx = dx.signOf();
				short sigy = dy.signOf();
				short sigz = dz.signOf();
				flag = link_node_sing(box,nodes, bb, dx, dy, dz, sigx, sigy, sigz);
			} else {
				flag = link_nodes_reduce(box,nodes, bb, dx, dy, dz);
				// free_bern3D(dx);
				// free_bern3D(dy);
				// free_bern3D(dz);
				return flag;
			}
		}
		// fini_nodes:
		if (PRINT_LINK_SING) {
			BoxClevA.log.printf("ERR: link_nodes: done %d %d %d count %d\n", f1, f2, f3, count);
			BoxClevA.log.print(box.toString_brief());
		}

		if (boxclev.littleFacets) {
			boxclev.facets.make_facets(box);
			boxclev.plotter.plot_box(box);
		}
		return (flag);
	}

	/**
	 * 
	 * @param box
	 * @return
	 */
	private double calc_maximum_abs_mean_curvature(Box_info box) {

		List<Sol_info> list = new ArrayList<>();
		box.add_sols_to_list(list);
		double max = 0;

		if (USE_ACTUAL_CURVATURE) {
			for (Sol_info sol : list) {
				double curve = sol.calcMeanCurvature(boxclev.globalRegion,ctx);
				double m = Math.abs(curve);
				if (m > max)
					max = m;
			}
		} else {
			max = 0;
			for (int i = 0; i < list.size(); ++i) {
				for (int j = i + 1; j < list.size(); ++j) {
					double[] pA = list.get(i).calc_pos_actual(boxclev.globalRegion,ctx);
					double[] nA = list.get(i).calc_unit_norm(boxclev.globalRegion,ctx);
					double[] pB = list.get(j).calc_pos_actual(boxclev.globalRegion,ctx);
					double[] nB = list.get(j).calc_unit_norm(boxclev.globalRegion,ctx);
					double[] pAB = Vec3D.subVec(pB, pA);
					double[] nAB = Vec3D.subVec(nB, nA);
					double kAB = Math.sqrt(Vec3D.dot(nAB, nAB) / Vec3D.dot(pAB, pAB));
					if (kAB > max)
						max = kAB;
				}
			}
		}
		return max;
	}

	private Double calc_min_norm_len(Box_info box) {
		List<Sol_info> list = new ArrayList<>();
		box.add_sols_to_list(list);
		OptionalDouble min = list.stream().mapToDouble(sol -> {
			double[] n = sol.calc_norm_actual_unsafe(boxclev.globalRegion,ctx);
			double val = n[0] * n[0] + n[1] * n[1] + n[2] * n[2];
			return val;
		}).min();
		return min.orElse(10.0);
	}

	private void calc_pos_in_box(Box_info box, Sol_info sol, double[] vec) {
		double v[] = box.calc_pos_in_box(sol);
		vec[0] = v[0];
		vec[1] = v[1];
		vec[2] = v[2];

	}

	/**
	 * Count how many nodes are unmatched
	 * 
	 * @param count number of nodes
	 * @param done  flags for if nodes are done
	 * @return number of nodes when done[i] is false
	 */
	private int count_unmatched(int count, boolean[] done) {
		int i;
		int unmatched;
		unmatched = 0;
		for (i = 0; i < count; ++i)
			if (!done[i])
				++unmatched;
		return unmatched;
	}

	/* All calcs in rescaled function */

	private void fillSolWith3Dres(Sol_info sol, Solve3DresultWithSig conv_res) {
		sol.setRoots(conv_res.x, conv_res.y, conv_res.z);
		sol.setDerivs(conv_res);

		sol.conv_failed = !conv_res.good;
		sol.setValue(conv_res.f_val);
		if(Double.isFinite(conv_res.i_val)) {
			sol.setNorm(conv_res.g_val,conv_res.h_val,conv_res.i_val);
		}
	}

	private boolean find_known_sing(Sol_info sol) {

		int i;
		if (boxclev.global_mode != Boxclev.MODE_KNOWN_SING)
			return false;

		for (i = 0; i < boxclev.num_known_sings; ++i) {
			if (sol.xl == boxclev.known_sings[i].xl && sol.yl == boxclev.known_sings[i].yl
					&& sol.zl == boxclev.known_sings[i].zl) {
				BoxClevA.log.printf("ERR: converger.converge_sing: matched ");
				BoxClevA.log.print(boxclev.known_sings[i]);
				sol.setRoots(boxclev.known_sings[i].getRoot(), boxclev.known_sings[i].getRoot2(),
						boxclev.known_sings[i].getRoot3());
				return true;
			}
		}
		return false;
	}

	private boolean force_sing(Box_info box, List<Node_info> nodes, boolean done[], Sol_info sol, int unmatched) {
		//		double pos_x, pos_y, pos_z;
		//		double vec0[];
		//		BoxClevA.log.printf("ERR: force_sing: (%d,%d,%d)/%d\n", box.xl, box.yl, box.zl, box.denom);

		if (boxclev.global_mode == Boxclev.MODE_KNOWN_SING)
			return true;

		//		pos_x = pos_y = pos_z = 0.0;
		//		for (int i = 0; i < count; ++i) {
		//			if (!done[i]) {
		//				vec0 = box.calc_pos_in_box(nodes.get(i).sol);
		//				pos_x += vec0[0];
		//				pos_y += vec0[1];
		//				pos_z += vec0[2];
		//			}
		//		}
		//		pos_x /= count;
		//		pos_y /= count;
		//		pos_z /= count;
		//		sol.root = pos_x;
		//		sol.root2 = pos_y;
		//		sol.root3 = pos_z;
		//		sol.hasPosNorm = false;
		//		if (sol.root < 0.0 || sol.root > 1.0 || sol.root2 < 0.0 || sol.root2 > 1.0 || sol.root3 < 0.0
		//				|| sol.root3 > 1.0) {
		//			BoxClevA.log.printf("ERR: link_sing: odd posn C %f %f %f\n", sol.root, sol.root2, sol.root3);
		//			if (PRINT_LINK_SING) {
		//				BoxClevA.log.print(box.print_box_brief());
		//			}
		//		}

		// 		Solve3DresultWithSig conv_res = converger.converge_sing(
		//				new BoxPos(pos_x, pos_y, pos_z), 
		//				bb, dx, dy, dz, f1, f2, f3);

		box.add_sing(sol);

		Node_info midnode = new Node_info(sol);
		// if(TEST_ALLOC){
		// ++nodecount; ++nodemax; ++nodenew;
		// }
		for (int i = 0; i < nodes.size(); ++i) {
			if (!done[i])
				box.add_node_link_simple(midnode, nodes.get(i));
		}
		return true;
	}

	private boolean link_node_sing(Box_info box, List<Node_info> nodes, Bern3D bb, Bern3D dx, Bern3D dy,
			Bern3D dz, int f1, int f2, int f3) throws AsurfException {
		double pos_x, pos_y, pos_z, vec0[] = new double[3];
		Sol_info sol;
		final int num_nodes = nodes.size();
		int i, j, unmatched;//, order[] = new int[num_nodes];
		boolean converged_to_sing;
		Node_info midnode;
		char signStr[];

		if(f1 !=0 && f2 !=0 && f3 !=0) {
			return false;
		}
		if (PRINT_LINK_SING) {
			BoxClevA.log.print(box.toString_brief());
		}
 
		converged_to_sing = false;
		// solveSing(box,bb, dx,dy,dz, f1, f2, f3, count);

		// Node_info resNodes[] = new Node_info[count];
		boolean done[] = new boolean[num_nodes];
		int undone[] = new int[num_nodes];
		// number of nodes with all zeros derivs
		int all_zero_count = 0;

		pos_x = pos_y = pos_z = 0.0;

		for (i = 0; i < num_nodes; ++i) {

			vec0 = box.calc_pos_in_box(nodes.get(i).sol);
			pos_x += vec0[0];
			pos_y += vec0[1];
			pos_z += vec0[2];

			done[i] = false;
			undone[i] = -1;
			if (nodes.get(i).sol.num_zero_derivs() == 3)
				++all_zero_count;
		}
		if (num_nodes == 0)
			pos_x = pos_y = pos_z = 0.5;
		else {
			pos_x /= num_nodes;
			pos_y /= num_nodes;
			pos_z /= num_nodes;
		}
		if (pos_x < 0 || pos_x > 1.0 || pos_y < 0.0 || pos_y > 1.0 || pos_z < 0.0 || pos_z > 1.0) {
			if (PRINT_LINK_SING) {
				BoxClevA.log.printf("link_sing: odd posn A %f %f %f\n", pos_x, pos_y, pos_z);
				BoxClevA.log.print(box.toString_brief());
			}
		}

		if (all_zero_count == 2 && boxclev.global_mode != Boxclev.MODE_KNOWN_SING) {
			converged_to_sing = link_sing_many_zeros(box, nodes, bb, dx, dy, dz, f1, f2, f3);
			if (PRINT_LINK_SING) {
				BoxClevA.log.printf("Crosscap test %d\n", converged_to_sing);
			}
			if (converged_to_sing)
				return true;
		}
		if (all_zero_count == 6 && boxclev.global_mode != Boxclev.MODE_KNOWN_SING) {
			converged_to_sing = link_node_three_planes(box, nodes, bb, dx, dy, dz, f1, f2, f3);
			if (converged_to_sing)
				return true;
		}

		// Try converging to a singularity, it may fail
		signStr = SignTest.BuildNodeSigns2(nodes);
		sol = new Sol_info(BOX, box.xl, box.yl, box.zl, box.denom, pos_x, pos_y, pos_z);
		sol.setDerivs(f1, f2, f3);
		Solve3DresultWithSig conv_res = boxgen.converger.converge_sing(new BoxPos(pos_x, pos_y, pos_z), bb, dx, dy, dz,
				f1, f2, f3);
		int nzero = (conv_res.sig_x == 0 ? 1 : 0) + (conv_res.sig_y == 0 ? 1 : 0) + (conv_res.sig_z == 0 ? 1 : 0);
		fillSolWith3Dres(sol, conv_res);

		converged_to_sing = conv_res.good;
		if (nzero == 1)
			converged_to_sing = false;
		// if (!convered_to_sing)
		// sol.conv_failed = true;
		if (PRINT_LINK_SING) {
			BoxClevA.log.printf("Link_sing converge test %d\n", converged_to_sing);
			double[] vec = box.calc_pos_in_box(sol);
			double val = ctx.evalbern3D(bb, vec[0], vec[1], vec[2]);
			// double norm[] = sol.calc_norm_actual(boxclev);
			// BoxClevA.log.printf("f %6.3f dx %6.3f %6.3f %6.3f%n", val, norm[0], norm[1],
			// norm[2]);
			BoxClevA.log.println(sol);
		}

		if ((boxclev.global_mode == Boxclev.MODE_KNOWN_SING) && converged_to_sing) {
			BoxClevA.log.printf("Sing with known sings\n");
			box.add_sing(sol);

			if (num_nodes == 0)
				return true;
			midnode = new Node_info(sol);
			// #ifdef TEST_ALLOC
			// ++nodecount; ++nodemax; ++nodenew;
			// }
			for (i = 0; i < num_nodes; ++i) {
				box.add_node_link(midnode, nodes.get(i));
			}
			return true;
		}

		if (num_nodes == 0) {
			if (converged_to_sing) {
				if (sol.getRoot() < 0.0 || sol.getRoot() > 1.0 || sol.getRoot2() < 0.0 || sol.getRoot2() > 1.0
						|| sol.getRoot3() < 0.0 || sol.getRoot3() > 1.0) {
					if (failCountP++ == 0) {
						BoxClevA.log.printf("Link_sing: odd posn B %f %f %f\n", sol.getRoot(), sol.getRoot2(),
								sol.getRoot3());
						BoxClevA.log.print(box.toString_brief());
					}
				}
				box.add_sing(sol);

				if (num_nodes == 0)
					return true;
				midnode = new Node_info(sol);
			}
			return true;
		}

		if (num_nodes == 4) {
			int[] order = new int[4];
			if (SignTest.TestSigns(signStr, 4, 3, "++0|+-0|+0+|+0-", "+++|-++", "abc|bca|cab", order)
					|| SignTest.TestSigns(signStr, 4, 3, "--0|+-0|0-0|0-0", "+++|+-+", "abc|bca|cab", order)
					|| SignTest.TestSigns(signStr, 4, 3, "--0|+-0|0+0|0+0", "+++|+-+", "abc|bca|cab", order)) {
				unmatched = 4;
				return force_sing(box, nodes, done, sol, unmatched);
			}
		}

		unmatched = match_all_nodes(nodes, done, undone);

		if (unmatched == 3) { /* if there are three unmatched then one might be degenerate */
			/* typically its (1,0,1)--(1,0,0)--(1,0,-1) */
			/* or (0,1,-1) -- (0,1,0) -- (-1,1,0) */

			int k;
			boolean matchedX = false, matchedY = false, matchedZ = false;

			i = undone[0];
			j = undone[1];
			k = undone[2];
			Node_info node_i = nodes.get(i);
			Node_info node_j = nodes.get(j);
			Node_info node_k = nodes.get(k);
			/*
			 * BoxClevA.log.printf("ERR: Linking 3 unmatched\n"); print_sol(nodes[i].sol);
			 * print_sol(node_j.sol); print_sol(node_k.sol);
			 */
			if (node_i.sol.getDx() == node_j.sol.getDx() && node_i.sol.getDx() == node_k.sol.getDx())
				matchedX = true;
			if (node_i.sol.getDy() == node_j.sol.getDy() && node_i.sol.getDy() == node_k.sol.getDy())
				matchedY = true;
			if (node_i.sol.getDz() == node_j.sol.getDz() && node_i.sol.getDz() == node_k.sol.getDz())
				matchedZ = true;

			if (matchedX && matchedY) {
				if (node_i.sol.getDz() == 0) {
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_i, node_k);
				} else if (node_j.sol.getDz() == 0) {
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_j, node_k);
				} else {
					box.add_node_link_simple(node_i, node_k);
					box.add_node_link_simple(node_j, node_k);
				}
				done[i] = done[j] = done[k];
				unmatched = 0;
			} else if (matchedX && matchedZ) {
				if (node_i.sol.getDy() == 0) {
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_i, node_k);
				} else if (node_j.sol.getDy() == 0) {
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_j, node_k);
				} else {
					box.add_node_link_simple(node_i, node_k);
					box.add_node_link_simple(node_j, node_k);
				}
				done[i] = done[j] = done[k];
				unmatched = 0;
			} else if (matchedY && matchedZ) {
				if (node_i.sol.getDx() == 0) {
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_i, node_k);
				} else if (node_j.sol.getDx() == 0) {
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_j, node_k);
				} else {
					box.add_node_link_simple(node_i, node_k);
					box.add_node_link_simple(node_j, node_k);
				}
				done[i] = done[j] = done[k];
				unmatched = 0;
			} else if (matchedX) {
				/* or (0,1,-1) -- (0,1,0) -- (-1,1,0) */

				if (node_i.sol.getDx() != 0 && node_i.sol.getDy() == 0 && node_i.sol.getDz() == 0) {
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_i, node_k);
				} else if (node_j.sol.getDx() != 0 && node_j.sol.getDy() == 0 && node_j.sol.getDz() == 0) {
					box.add_node_link_simple(node_j, node_i);
					box.add_node_link_simple(node_j, node_k);
				} else if (node_k.sol.getDx() != 0 && node_k.sol.getDy() == 0 && node_k.sol.getDz() == 0) {
					box.add_node_link_simple(node_k, node_i);
					box.add_node_link_simple(node_k, node_j);
				} else {
					if (failCountQ++ == 0) {
						BoxClevA.log.printf("ERR: link_sing: wierdness\n");
						BoxClevA.log.println(node_i);
						BoxClevA.log.println(node_j);
						BoxClevA.log.println(node_k);
					}
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_i, node_k);
				}
				done[i] = done[j] = done[k];
				unmatched = 0;
			} else if (matchedY) {
				/* or (0,1,-1) -- (0,1,0) -- (-1,1,0) */

				if (node_i.sol.getDx() == 0 && node_i.sol.getDz() == 0 && node_i.sol.getDy() != 0) {
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_i, node_k);
				} else if (node_j.sol.getDx() == 0 && node_j.sol.getDz() == 0 && node_j.sol.getDy() != 0) {
					box.add_node_link_simple(node_j, node_i);
					box.add_node_link_simple(node_j, node_k);
				} else if (node_k.sol.getDx() == 0 && node_k.sol.getDz() == 0 && node_k.sol.getDy() != 0) {
					box.add_node_link_simple(node_k, node_i);
					box.add_node_link_simple(node_k, node_j);
				} else {
					if (failCountQ++ == 0) {
						BoxClevA.log.printf("ERR: link_sing: wierdness\n");
						BoxClevA.log.println(node_i);
						BoxClevA.log.println(node_j);
						BoxClevA.log.println(node_k);
					}
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_i, node_k);
				}
				done[i] = done[j] = done[k];
				unmatched = 0;
			} else if (matchedZ) {
				/* or (0,1,-1) -- (0,1,0) -- (-1,1,0) */

				if (node_i.sol.getDx() == 0 && node_i.sol.getDy() == 0 && node_i.sol.getDz() != 0) {
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_i, node_k);
				} else if (node_j.sol.getDx() == 0 && node_j.sol.getDy() == 0 && node_j.sol.getDz() != 0) {
					box.add_node_link_simple(node_j, node_i);
					box.add_node_link_simple(node_j, node_k);
				} else if (node_k.sol.getDx() == 0 && node_k.sol.getDy() == 0 && node_k.sol.getDz() != 0) {
					box.add_node_link_simple(node_k, node_i);
					box.add_node_link_simple(node_k, node_j);
				} else {
					if (failCountQ++ == 0) {
						BoxClevA.log.printf("ERR: link_sing: wierdness\n");
						BoxClevA.log.println(node_i);
						BoxClevA.log.println(node_j);
						BoxClevA.log.println(node_k);
					}
					box.add_node_link_simple(node_i, node_j);
					box.add_node_link_simple(node_i, node_k);
				}
				done[i] = done[j] = done[k];
				unmatched = 0;
			} else if (node_i.sol.getDx() == 0 && node_i.sol.getDy() == 0 && node_i.sol.getDz() == 0) {
				BoxClevA.log.printf("ERR: link_sing: matching 000\n");
				box.add_node_link_simple(node_i, node_j);
				box.add_node_link_simple(node_i, node_k);
				done[i] = done[j] = done[k];
				unmatched = 0;
			} else if (node_j.sol.getDx() == 0 && node_j.sol.getDy() == 0 && node_j.sol.getDz() == 0) {
				BoxClevA.log.printf("ERR: link_sing: matching 000\n");
				box.add_node_link_simple(node_j, node_i);
				box.add_node_link_simple(node_j, node_k);
				done[i] = done[j] = done[k];
				unmatched = 0;
			} else if (node_k.sol.getDx() == 0 && node_k.sol.getDy() == 0 && node_k.sol.getDz() == 0) {
				BoxClevA.log.printf("ERR: link_sing: matching 000\n");
				box.add_node_link_simple(node_k, node_i);
				box.add_node_link_simple(node_k, node_j);
				done[i] = done[j] = done[k];
				unmatched = 0;
			} else {
				BoxClevA.log.printf("ERR: No two unmatched\n");
			}
		} // end of unmatched == 3

		if (unmatched == 0 || unmatched == 2) {
			boolean force_deriv_cross_sing = false;
			int[] order = new int[nodes.size()];
			if (num_nodes >= 4 && Test4nodesLike011(nodes, order)) {
				// double vecs[][] = new double[4][3];
				int A, B, C, D;
				Bern3D mat = null, ddx, ddy, ddz;
				double distAB, distAC, distAD, distBC, distBD, distCD;
				double dist1x, dist1y, dist1z, dist2x, dist2y, dist2z;
				double dist3x, dist3y, dist3z, dist4x, dist4y, dist4z;
				double dist5x, dist5y, dist5z, dist6x, dist6y, dist6z;

				final Node_info node0 = nodes.get(order[0]);
				final Node_info node1 = nodes.get(order[1]);
				final Node_info node2 = nodes.get(order[2]);
				final Node_info node3 = nodes.get(order[3]);
				if (node0.sol.getDx() == 0)
					mat = dx;
				else if (node0.sol.getDy() == 0)
					mat = dy;
				else if (node0.sol.getDz() == 0)
					mat = dz;

				ddx = mat.diffX();
				ddy = mat.diffY();
				ddz = mat.diffZ();

				pos_x = pos_y = pos_z = 0.0;
				int n_match = 0;
				for (i = 0; i < num_nodes; ++i) {
					if (order[i] >= 0) {
						vec0 = box.calc_pos_in_box(nodes.get(order[i]).sol);
						pos_x += vec0[0];
						pos_y += vec0[1];
						pos_z += vec0[2];
						++n_match;
					}
				}
				pos_x /= n_match;
				pos_y /= n_match;
				pos_z /= n_match;
				if (n_match > 4 || (!ddx.allOneSign() && !ddy.allOneSign() && !ddz.allOneSign())) {

					if (failCountR++ == 0) {
						BoxClevA.log.printf("Second derivs all zero\n");
						BoxClevA.log.printf("Calculated posn %f %f %f\n", pos_x, pos_y, pos_z);
					}
					sol.setRoots(pos_x, pos_y, pos_z);
					if (sol.getRoot() < 0.0 || sol.getRoot() > 1.0 || sol.getRoot2() < 0.0 || sol.getRoot2() > 1.0
							|| sol.getRoot3() < 0.0 || sol.getRoot3() > 1.0) {
						BoxClevA.log.printf("ERR: link_sing: odd posn C %f %f %f\n", sol.getRoot(), sol.getRoot2(),
								sol.getRoot3());
						BoxClevA.log.print(box.toString_brief());
					}
					Solve3Dresult cres = boxgen.converger.converge_sing_one_deriv(new BoxPos(pos_x, pos_y, pos_z), bb,
							mat);
					sol.setRoots(cres.x, cres.y, cres.z);
					sol.conv_failed = !cres.good;
					sol.setDerivs(node0.sol);
					box.add_sing(sol);

					midnode = new Node_info(sol);
					// if(TEST_ALLOC) {
					// ++nodecount; ++nodemax; ++nodenew;
					// }
					for (i = 0; i < n_match; ++i) {
						box.add_node_link(midnode, nodes.get(order[i]));
						done[order[i]] = true;
					}
					force_deriv_cross_sing = true;
				}
				// free_bern3D(ddx); free_bern3D(ddy); free_bern3D(ddz);

				if (!force_deriv_cross_sing) {
					if (SameFace(node0.sol, node1.sol)) {
						A = order[0];
						B = order[1];
						C = order[2];
						D = order[3];
					} else if (SameFace(node0.sol, node2.sol)) {
						A = order[0];
						B = order[2];
						C = order[1];
						D = order[3];
					} else if (SameFace(node0.sol, node3.sol)) {
						A = order[0];
						B = order[3];
						C = order[2];
						D = order[1];
					} else if (SameFace(node1.sol, node2.sol)) {
						A = order[1];
						B = order[2];
						C = order[0];
						D = order[3];
					} else if (SameFace(node1.sol, node3.sol)) {
						A = order[1];
						B = order[3];
						C = order[0];
						D = order[2];
					} else if (SameFace(node2.sol, node3.sol)) {
						A = order[2];
						B = order[3];
						C = order[0];
						D = order[1];
					} else {
						if (failCountS++ == 0) {
							BoxClevA.log.printf("ERR: link_sing: 4 id nodes but non on same face\n");
							BoxClevA.log.println(node0);
							BoxClevA.log.println(node1);
							BoxClevA.log.println(node2);
							BoxClevA.log.println(node3);
						}
						A = order[0];
						B = order[1];
						C = order[2];
						D = order[3];
					}
					Node_info nodeA = nodes.get(A);
					Node_info nodeB = nodes.get(B);
					Node_info nodeC = nodes.get(C);
					Node_info nodeD = nodes.get(D);
					double[][] vecs = new double[4][];
					vecs[0] = nodeA.sol.calc_pos_actual(boxclev.globalRegion,ctx);
					vecs[1] = nodeB.sol.calc_pos_actual(boxclev.globalRegion,ctx);
					vecs[2] = nodeC.sol.calc_pos_actual(boxclev.globalRegion,ctx);
					vecs[3] = nodeD.sol.calc_pos_actual(boxclev.globalRegion,ctx);
					dist1x = vecs[0][0] - vecs[1][0];
					dist1y = vecs[0][1] - vecs[1][1];
					dist1z = vecs[0][2] - vecs[1][2];
					dist2x = vecs[0][0] - vecs[2][0];
					dist2y = vecs[0][1] - vecs[2][1];
					dist2z = vecs[0][2] - vecs[2][2];
					dist3x = vecs[0][0] - vecs[3][0];
					dist3y = vecs[0][1] - vecs[3][1];
					dist3z = vecs[0][2] - vecs[3][2];
					dist4x = vecs[1][0] - vecs[2][0];
					dist4y = vecs[1][1] - vecs[2][1];
					dist4z = vecs[1][2] - vecs[2][2];
					dist5x = vecs[1][0] - vecs[3][0];
					dist5y = vecs[1][1] - vecs[3][1];
					dist5z = vecs[1][2] - vecs[3][2];
					dist6x = vecs[2][0] - vecs[3][0];
					dist6y = vecs[2][1] - vecs[3][1];
					dist6z = vecs[2][2] - vecs[3][2];

					distAB = Math.sqrt(dist1x * dist1x + dist1y * dist1y + dist1z * dist1z);
					distAC = Math.sqrt(dist2x * dist2x + dist2y * dist2y + dist2z * dist2z);
					distAD = Math.sqrt(dist3x * dist3x + dist3y * dist3y + dist3z * dist3z);
					distBC = Math.sqrt(dist4x * dist4x + dist4y * dist4y + dist4z * dist4z);
					distBD = Math.sqrt(dist5x * dist5x + dist5y * dist5y + dist5z * dist5z);
					distCD = Math.sqrt(dist6x * dist6x + dist6y * dist6y + dist6z * dist6z);
					if (PRINT_LINK_SING) {
						BoxClevA.log.printf("4 indentical nodes, distances %f %f %f %f %f %f\n", distAB, distAC, distAD,
								distBC, distBD, distCD);
					}
					/* found 4 nodes with identical sign pattern */

					if (distAC < distBC && distBD < distAD) {
						if (PRINT_LINK_SING) {
							BoxClevA.log.printf("Linking nodes %d, %d and  %d, %d\n", A, C, B, D);
						}
						box.add_node_link_simple(nodeA, nodeC);
						box.add_node_link_simple(nodeB, nodeD);
						done[A] = done[B] = done[C] = done[D] = true;
					} else if (distAC > distBC && distBD > distAD) {
						if (PRINT_LINK_SING) {
							BoxClevA.log.printf("Linking nodes %d, %d and  %d, %d\n", A, D, B, C);
						}
						box.add_node_link_simple(nodeA, nodeD);
						box.add_node_link_simple(nodeB, nodeC);
						done[A] = done[B] = done[C] = done[D] = true;
					} else {
						if (failCountT++ == 0) {
							BoxClevA.log.printf("ERR: link_sing: wierd distances \n");
							BoxClevA.log.printf("distances %f %f %f %f %f %f\n", distAB, distAC, distAD, distBC, distBD,
									distCD);
							BoxClevA.log.println(nodeA);
							BoxClevA.log.println(nodeB);
							BoxClevA.log.println(nodeC);
							BoxClevA.log.println(nodeD);
						}
						box.add_node_link_simple(nodeA, nodeC);
						box.add_node_link_simple(nodeB, nodeD);
						done[A] = done[B] = done[C] = done[D] = true;
					}
				}
			}
			if (force_deriv_cross_sing) {
				if (failCountU++ == 0) {
					BoxClevA.log.printf("ERR: At fdcs: unmatched %d\n", unmatched);
				}
			}
		}

		unmatched = count_unmatched(num_nodes, done);
		if (unmatched == 0) {
			return true;
		}

		if (PRINT_LINK_SING) {
			BoxClevA.log.printf("ERR: unmatched %d\n", unmatched);
			BoxClevA.log.print(box.toString_brief());
		}

		if (converged_to_sing && sol.num_zero_derivs() == 3) {
			box.add_sing(sol);
			midnode = new Node_info(sol);
			for (i = 0; i < num_nodes; ++i) {
				if (!done[i]) {
					box.add_node_link_simple(nodes.get(i), midnode);
				}
			}
			return true;
		}

		for (i = 0; i < num_nodes; ++i) {
			if (done[i])
				continue;
			Node_info node_i = nodes.get(i);
			for (j = i + 1; j < num_nodes; ++j) {
				if (done[i] || done[j])
					continue;
				Node_info node_j = nodes.get(j);
				if (matchNodes(node_i,node_j) && (node_i.sol.getDx() != 0 || node_i.sol.getDy() != 0 || node_i.sol.getDz() != 0)) {
					if (PRINT_LINK_SING) {
						BoxClevA.log.printf("ERR: Linking nodes fdcs: %d and %d done %d %d\n", i, j, done[i], done[j]);
					}
					if (converged_to_sing && ((sol.getDx() == 0 && node_i.sol.getDx() == 0)
							|| (sol.getDy() == 0 && node_i.sol.getDy() == 0) || (sol.getDz() == 0 && node_i.sol.getDz() == 0)))
						continue;

					box.add_node_link_simple(node_i, node_j);
					done[i] = done[j] = true;
				}
			}
		}
		unmatched = count_unmatched(num_nodes, done);
		if (PRINT_LINK_SING)
			BoxClevA.log.printf("ERR: unmatched %d all zero %d\n", unmatched, all_zero_count);
		if (unmatched == 0)
			return true;

		if (USE_SINGULARITIES_ON_FACES) {
			if (all_zero_count == 1) {

				int zero_index = 0;
				Node_info node_zero = nodes.get(zero_index);
				double vec[] = new double[3];

				for (i = 0; i < num_nodes; ++i) {
					Node_info node_i = nodes.get(i);
					if (node_i.sol.getDx() == 0 && node_i.sol.getDy() == 0 && node_i.sol.getDz() == 0)
						zero_index = i;
				}
				vec = box.calc_pos_in_box(node_zero.sol);
				sol.setRoots(vec[0], vec[1], vec[2]);
				// box.add_sing(sol);
				box.add_sing(node_zero.sol);
				midnode = new Node_info(node_zero.sol);
				// if(TEST_ALLOC){
				// ++nodecount; ++nodemax; ++nodenew;
				// }
				for (i = 0; i < num_nodes; ++i) {
					if (!done[i]) {
						box.add_node_link_simple(nodes.get(i), midnode);
					}
				}
				if (PRINT_LINK_SING) {
					BoxClevA.log.printf("ERR: link_sing: one all zero\n");
					BoxClevA.log.print(box.toString_brief());
				}
				return true;
			}
		}

		/* now if converger.converged to sing add that. */

		if (converged_to_sing) {
			if (sol.getRoot() < 0.0 || sol.getRoot() > 1.0 || sol.getRoot2() < 0.0 || sol.getRoot2() > 1.0
					|| sol.getRoot3() < 0.0 || sol.getRoot3() > 1.0) {
				if (failCountU++ == 0) {
					BoxClevA.log.printf("ERR: link_sing: odd posn B %f %f %f\n", sol.getRoot(), sol.getRoot2(),
							sol.getRoot3());
					BoxClevA.log.print(box.toString_brief());
				}
			}
			box.add_sing(sol);
			if (num_nodes == 0)
				return true;
			midnode = new Node_info(sol);
			for (i = 0; i < num_nodes; ++i) {
				box.add_node_link_simple(midnode, nodes.get(i));
			}
			return true;
		}

		/*
		 * Now lets get really hacky if there is a node (1,0,0) link it to all nodes
		 * (1,+/-1,0) and (1,0,+/-0) then if there is a node (0,0,0) link to all undone
		 * nodes and all nodes like (1,0,0)
		 */

		for (i = 0; i < num_nodes; ++i) {
			if (done[i])
				continue;
			Node_info node_i = nodes.get(i);

			for (j = i + 1; j < num_nodes; ++j) {
				if (done[i] || done[j])
					continue;
				Node_info node_j = nodes.get(j);
				if (matchNodes(node_i,node_j)) {
					if (PRINT_LINK_SING) {
						BoxClevA.log.printf("Linking nodes %d and %d done %d %d\n", i, j, done[i], done[j]);
						box.add_node_link_simple(node_i, node_j);
						done[i] = done[j] = true;
					}
				}
			}
		}

		/* Link (1,1,0) to (1,0,0) or (0,1,0) */

		for (i = 0; i < num_nodes; ++i) {
			if (done[i])
				continue;
			Node_info node_i = nodes.get(i);
			if ((node_i.sol.getDx() == 0 && node_i.sol.getDy() == 0) || (node_i.sol.getDx() == 0 && node_i.sol.getDz() == 0)
					|| (node_i.sol.getDy() == 0 && node_i.sol.getDz() == 0))
				continue;
			for (j = 0; j < num_nodes; ++j) {
				if (j == i)
					continue;
				Node_info node_j = nodes.get(j);
				if (node_j.sol.getDx() == 0 && node_j.sol.getDy() == 0 && node_j.sol.getDz() == 0)
					continue;
				if (SameFace(node_i.sol, node_j.sol))
					continue;
				if (node_i.sol.getDx() == 0) {
					if (node_j.sol.getDx() != 0)
						continue;
					if (node_j.sol.getDy() != 0 && node_j.sol.getDy() != node_i.sol.getDy())
						continue;
					if (node_j.sol.getDz() != 0 && node_j.sol.getDz() != node_i.sol.getDz())
						continue;
					if (PRINT_LINK_SING) {
						BoxClevA.log.printf("Linking nodes2a %d and %d done %d %d\n", i, j, done[i], done[j]);
					}
					box.add_node_link_simple(node_i, node_j);
					done[i] = done[j] = true;
					break;
				}
				if (node_i.sol.getDy() == 0) {
					if (node_j.sol.getDy() != 0)
						continue;
					if (node_j.sol.getDx() != 0 && node_j.sol.getDx() != node_i.sol.getDx())
						continue;
					if (node_j.sol.getDz() != 0 && node_j.sol.getDz() != node_i.sol.getDz())
						continue;
					if (PRINT_LINK_SING) {
						BoxClevA.log.printf("'Linking nodes2b %d and %d done %d %d\n", i, j, done[i], done[j]);
					}
					box.add_node_link_simple(node_i, node_j);
					done[i] = done[j] = true;
					break;
				}
				if (node_i.sol.getDz() == 0) {
					if (node_j.sol.getDz() != 0)
						continue;
					if (node_j.sol.getDx() != 0 && node_j.sol.getDx() != node_i.sol.getDx())
						continue;
					if (node_j.sol.getDy() != 0 && node_j.sol.getDy() != node_i.sol.getDy())
						continue;
					if (PRINT_LINK_SING) {
						BoxClevA.log.printf("Linking nodes2c %d and %d done %d %d\n", i, j, done[i], done[j]);
					}
					box.add_node_link_simple(node_i, node_j);
					done[i] = done[j] = true;
					break;
				}
			}
		}

		for (i = 0; i < num_nodes; ++i) {
			Node_info node_i = nodes.get(i);
			if (node_i.sol.getDx() == 0 && node_i.sol.getDy() == 0 && node_i.sol.getDz() == 0) {

				for (j = 0; j < num_nodes; ++j) {
					if (j == i)
						continue;
					Node_info node_j = nodes.get(j);
					if (!done[j] && !SameFace(node_i.sol, node_j.sol))
						/*
						 * || (node_j.sol.dx != 0 && node_j.sol.dy == 0 && node_j.sol.dz == 0 ) ||
						 * (node_j.sol.dx == 0 && node_j.sol.dy != 0 && node_j.sol.dz == 0 ) ||
						 * (node_j.sol.dx == 0 && node_j.sol.dy == 0 && node_j.sol.dz != 0 ) )
						 */
					{
						if (PRINT_LINK_SING) {
							BoxClevA.log.printf("Linking nodes3 %d and %d done %d %d\n", i, j, done[i], done[j]);
						}
						box.add_node_link_simple(node_i, node_j);
						done[i] = done[j] = true;
					}
				}
				if (FAKE_SINGS) {
					if (done[i]) {
						box.add_sing(node_i.sol);
					}
				}
			}
		}

		unmatched = 0;
		for (i = 0; i < num_nodes; ++i)
			if (!done[i]) {
				if (PRINT_LINK_SING) {
					Node_info node_i = nodes.get(i);
					BoxClevA.log.println(node_i);
				}
				++unmatched;
			}
		if (unmatched == 0 || unmatched == 1)
			return true;
		if (failCountV++ == 0) {
			BoxClevA.log.printf("ERR: unmatched %d\n", unmatched);
		}
		if (boxclev.global_mode == Boxclev.MODE_KNOWN_SING)
			return true;

		return force_sing(box, nodes, done, sol, unmatched);
	}

	/**
	 * @param bb2
	 * @param dx
	 * @param dy
	 * @param dz
	 * @param f1
	 * @param f2
	 * @param f3
	 * 
	 */
	private boolean link_node_three_planes(Box_info box, List<Node_info> nodes,Bern3D bb2, Bern3D dx,
			Bern3D dy, Bern3D dz, int f1, int f2, int f3) {
		int num_all_zero = 0, i;
		double pos_x, pos_y, pos_z;
		double vec[] = new double[3];
		Sol_info sol;
		Node_info midnode;
		int count = nodes.size();
		if (count < 6)
			return false;
		for (i = 0; i < count; ++i) {
			if (nodes.get(i).sol.getDx() == 0 && nodes.get(i).sol.getDy() == 0 && nodes.get(i).sol.getDz() == 0)
				++num_all_zero;
		}
		if (num_all_zero < 6)
			return false;

		if (PRINT_SING) {
			BoxClevA.log.printf("link_three_planes sucess\n");
			BoxClevA.log.print(box.toString_brief());
		}

		pos_x = pos_y = pos_z = 0.0;
		for (i = 0; i < count; ++i) {
			calc_pos_in_box(box, nodes.get(i).sol, vec);
			pos_x += vec[0];
			pos_y += vec[1];
			pos_z += vec[2];
		}
		pos_x /= count;
		pos_y /= count;
		pos_z /= count;

		sol = make_sol3(BOX, box.xl, box.yl, box.zl, box.denom, pos_x, pos_y, pos_z);
		sol.setDerivs(0, 0, 0);

		find_known_sing(sol);

		box.add_sing(sol);
		midnode = new Node_info(sol);
		for (i = 0; i < count; ++i) {
			box.add_node_link(midnode, nodes.get(i));
		}
		return true;
	}

	/**
	 * Links together the nodes surrounding a box. adds the links to the list in
	 * big_box.
	 */


	/**
	 * @param dx
	 * @param dy
	 * @param dz
	 */
	private boolean link_nodes_reduce(Box_info box, List<Node_info> nodes, Bern3D bb, Bern3D dx, Bern3D dy,
			Bern3D dz) throws AsurfException {
		Bern3DContext.OctBern aa;
		boolean flag;

		aa = ctx.reduce(bb);
		box.sub_devide_box();
		boxclev.topology.split_box(box, box.lfd, box.rfd, box.lbd, box.rbd, box.lfu, box.rfu, box.lbu, box.rbu);

		boxgen.find_all_faces(box.lfd, aa.lfd);
		boxgen.find_all_faces(box.lfu, aa.lfu);
		boxgen.find_all_faces(box.lbd, aa.lbd);
		boxgen.find_all_faces(box.lbu, aa.lbu);
		boxgen.find_all_faces(box.rfd, aa.rfd);
		boxgen.find_all_faces(box.rfu, aa.rfu);
		boxgen.find_all_faces(box.rbd, aa.rbd);
		boxgen.find_all_faces(box.rbu, aa.rbu);

		box.lfd.status = BoxClevA.FOUND_FACES;
		box.rbd.status = BoxClevA.FOUND_FACES;
		box.rfu.status = BoxClevA.FOUND_FACES;
		box.rfd.status = BoxClevA.FOUND_FACES;
		box.lbu.status = BoxClevA.FOUND_FACES;
		box.lfu.status = BoxClevA.FOUND_FACES;
		box.lbd.status = BoxClevA.FOUND_FACES;
		box.rbu.status = BoxClevA.FOUND_FACES;

		boolean f_lfd = link_nodes(box.lfd, aa.lfd);
		boolean f_rfd = link_nodes(box.rfd, aa.rfd);
		boolean f_lbd = link_nodes(box.lbd, aa.lbd);
		boolean f_rbd = link_nodes(box.rbd, aa.rbd);
		boolean f_lfu = link_nodes(box.lfu, aa.lfu);
		boolean f_rfu = link_nodes(box.rfu, aa.rfu);
		boolean f_lbu = link_nodes(box.lbu, aa.lbu);
		boolean f_rbu = link_nodes(box.rbu, aa.rbu);
		flag = f_lfd && f_rfd && f_lbd && f_rbd && f_lfu && f_rfu && f_lbu && f_rbu;
		/*
		 * #ifdef FACETS combine_facets(box); }
		 */
		// free_octbern3D(aa);
		return (flag);
	}

	private boolean link_sing_many_zeros(Box_info box, List<Node_info> nodes,Bern3D bb, Bern3D dx,
			Bern3D dy, Bern3D dz, int f1, int f2, int f3)
					throws AsurfException {

		Bern3D dxx, dxy, dxz, dyy, dyz, dzz, mat1, mat2, mat3;
		Node_info midnode;
		double vec0[] = new double[3], val, val_array[];
		short fxx, fxy, fxz, fyy, fyz, fzz;
		int i, j, unmatched;
		boolean flag;
		int sign_array[];
		int negxx, negxy, negxz, negyy, negyz, negzz;
		int posxx, posxy, posxz, posyy, posyz, poszz;
		Sol_info sol;
		int count = nodes.size();
		if (PRINT_LINK_CROSSCAP)
			BoxClevA.log.printf("link_crosscap (%d,%d,%d)/%d\n", box.xl, box.yl, box.zl, box.denom);

		dxx = dx.diffX();
		dxy = dx.diffY();
		dxz = dx.diffZ();
		dyy = dy.diffY();
		dyz = dy.diffZ();
		dzz = dz.diffZ();
		fxx = dxx.signOf();
		fxy = dxy.signOf();
		fxz = dxz.signOf();
		fyy = dxx.signOf();
		fyz = dyz.signOf();
		fzz = dzz.signOf();
		sign_array = new int[count * 6];
		val_array = new double[count * 6];

		negxx = negxy = negxz = negyy = negyz = negzz = 0;
		posxx = posxy = posxz = posyy = posyz = poszz = 0;
		for (i = 0; i < count; ++i) {
			if (PRINT_LINK_CROSSCAP)
				BoxClevA.log.println(nodes.get(i).sol);
			/*
			 * if(nodes[i].sol.dx == 0 && nodes[i].sol.dy == 0 && nodes[i].sol.dz == 0 ) {
			 */
			calc_pos_in_box(box, nodes.get(i).sol, vec0);
			if (fxx == 0) {
				val = ctx.evalbern3D(dxx, vec0[0], vec0[1], vec0[2]);
				if (val < 0.0) {
					negxx = 1;
					sign_array[i * 6 + 0] = -1;
				} else if (val > 0.0) {
					posxx = 1;
					sign_array[i * 6 + 0] = 1;
				} else
					sign_array[i * 6 + 0] = 0;
				val_array[i * 6 + 0] = val;
			} else
				sign_array[i * 6 + 0] = 0;

			if (fxy == 0) {
				val = ctx.evalbern3D(dxy, vec0[0], vec0[1], vec0[2]);
				if (val < 0.0) {
					negxy = 1;
					sign_array[i * 6 + 1] = -1;
				} else if (val > 0.0) {
					posxy = 1;
					sign_array[i * 6 + 1] = 1;
				} else
					sign_array[i * 6 + 1] = 0;
				val_array[i * 6 + 1] = val;
			} else
				sign_array[i * 6 + 1] = fxy;

			if (fxz == 0) {
				val = ctx.evalbern3D(dxz, vec0[0], vec0[1], vec0[2]);
				if (val < 0.0) {
					negxz = 1;
					sign_array[i * 6 + 2] = -1;
				} else if (val > 0.0) {
					posxz = 1;
					sign_array[i * 6 + 2] = 1;
				} else
					sign_array[i * 6 + 2] = 0;
				val_array[i * 6 + 2] = val;
			} else
				sign_array[i * 6 + 2] = fxz;

			if (fyy == 0) {
				val = ctx.evalbern3D(dyy, vec0[0], vec0[1], vec0[2]);
				if (val < 0.0) {
					negyy = 1;
					sign_array[i * 6 + 3] = -1;
				} else if (val > 0.0) {
					posyy = 1;
					sign_array[i * 6 + 3] = 1;
				} else
					sign_array[i * 6 + 3] = 0;
				val_array[i * 6 + 3] = val;
			} else
				sign_array[i * 6 + 3] = fyy;

			if (fyz == 0) {
				val = ctx.evalbern3D(dyz, vec0[0], vec0[1], vec0[2]);
				if (val < 0.0) {
					negyz = 1;
					sign_array[i * 6 + 4] = -1;
				} else if (val > 0.0) {
					posyz = 1;
					sign_array[i * 6 + 4] = 1;
				} else
					sign_array[i * 6 + 4] = 0;
				val_array[i * 6 + 4] = val;
			} else
				sign_array[i * 6 + 4] = fyz;

			if (fzz == 0) {
				val = ctx.evalbern3D(dzz, vec0[0], vec0[1], vec0[2]);
				if (val < 0.0) {
					negzz = 1;
					sign_array[i * 6 + 5] = -1;
				} else if (val > 0.0) {
					poszz = 1;
					sign_array[i * 6 + 5] = 1;
				} else
					sign_array[i * 6 + 5] = 0;
				val_array[i * 6 + 5] = val;
			} else
				sign_array[i * 6 + 5] = fzz;

			if (Boxclev.USE_2ND_DERIV) {
				/*
				 * if (nodes[i].sol.dxx > 0) posxx = 1; if (nodes[i].sol.dxx < 0) negxx = 1; if
				 * (nodes[i].sol.dxy > 0) posxy = 1; if (nodes[i].sol.dxy < 0) negxy = 1; if
				 * (nodes[i].sol.dxz > 0) posxz = 1; if (nodes[i].sol.dxz < 0) negxz = 1;
				 * 
				 * if (nodes[i].sol.dyy > 0) posyy = 1; if (nodes[i].sol.dyy < 0) negyy = 1; if
				 * (nodes[i].sol.dyz > 0) posyz = 1; if (nodes[i].sol.dyz < 0) negyz = 1; if
				 * (nodes[i].sol.dzz > 0) poszz = 1; if (nodes[i].sol.dzz < 0) negzz = 1;
				 * BoxClevA.log.printf("ERR: signs %d %d %d yy %d %d %d\t", nodes[i].sol.dxx,
				 * nodes[i].sol.dxy, nodes[i].sol.dxz, nodes[i].sol.dyy, nodes[i].sol.dyz,
				 * nodes[i].sol.dzz); BoxClevA.log.printf("ERR: signs %d %d %d yy %d %d %d\n",
				 * sign_array[i * 6 + 0], sign_array[i * 6 + 1], sign_array[i * 6 + 2],
				 * sign_array[i * 6 + 3], sign_array[i * 6 + 4], sign_array[i * 6 + 5]);
				 * BoxClevA.log.printf("ERR: fxx %d %d %d yy %d %d %d\t", fxx, fxy, fxz, fyy, fyz,
				 * fzz); BoxClevA.log.printf("ERR: vals %f %f %f yy %f %f %f\n", val_array[i * 6 +
				 * 0], val_array[i * 6 + 1], val_array[i * 6 + 2], val_array[i * 6 + 3],
				 * val_array[i * 6 + 4], val_array[i * 6 + 5]);
				 */
			}
		}

		mat1 = mat2 = mat3 = null;
		if ((negxx != 0 && posxx != 0))
			mat1 = dxx;
		if ((negxy != 0 && posxy != 0)) {
			if (mat1 == null)
				mat1 = dxy;
			else
				mat2 = dxy;
		}
		if ((negxz != 0 && posxz != 0)) {
			if (mat1 == null)
				mat1 = dxz;
			else if (mat2 == null)
				mat2 = dxz;
			else
				mat3 = dxz;
		}
		if ((negyy != 0 && posyy != 0)) {
			if (mat1 == null)
				mat1 = dyy;
			else if (mat2 == null)
				mat2 = dyy;
			else
				mat3 = dyy;
		}
		if ((negyz != 0 && posyz != 0)) {
			if (mat1 == null)
				mat1 = dyz;
			else if (mat2 == null)
				mat2 = dyz;
			else
				mat3 = dyz;
		}
		if ((negzz != 0 && poszz != 0)) {
			if (mat1 == null)
				mat1 = dzz;
			else if (mat2 == null)
				mat2 = dzz;
			else
				mat3 = dzz;
		}
		if (mat1 != null) {
			sol = make_sol3(BOX, box.xl, box.yl, box.zl, box.denom, 0.5, 0.5, 0.5);
			Solve3DresultWithSig cres = boxgen.converger.converge_sing(new BoxPos(0.5, 0.5, 0.5), bb, mat1, mat2, mat3,
					mat1 != null ? 0 : 1, mat2 != null ? 0 : 1, mat3 != null ? 0 : 1);
			fillSolWith3Dres(sol, cres);
			sol.setDerivs(f1, f2, f3);
			if (PRINT_LINK_CROSSCAP)
				BoxClevA.log.printf("link_sing_many_zeros conv %b%n", flag);
			if (!cres.good)
				return false;
			if (PRINT_LINK_CROSSCAP)
				BoxClevA.log.println(sol);
			if (sol.getRoot() < 0.0 || sol.getRoot() > 1.0 || sol.getRoot2() < 0.0 || sol.getRoot2() > 1.0
					|| sol.getRoot3() < 0.0 || sol.getRoot3() > 1.0)
				if (PRINT_LINK_CROSSCAP)
					BoxClevA.log.printf("link_crosscap: odd posn D %f %f %f\n", sol.getRoot(), sol.getRoot2(),
							sol.getRoot3());

			box.add_sing(sol);

			midnode = new Node_info(sol);
			for (i = 0; i < count; ++i) {
				box.add_node_link_simple(midnode, nodes.get(i));
			}
			if (PRINT_LINK_SING) {
				if (PRINT_LINK_CROSSCAP)
					BoxClevA.log.print(box.toString_brief());
			}
			return true;
		}
		unmatched = count;
		if (PRINT_LINK_CROSSCAP)
			BoxClevA.log.printf("link_sing_many_zeros; mat1 == null count %d\n", count);
		if (unmatched == 2) {
			box.add_node_link_simple(nodes.get(0), nodes.get(1));
			return true;
		}
		if (unmatched == 3) {
			box.add_node_link_simple(nodes.get(0), nodes.get(1));
			box.add_node_link_simple(nodes.get(0), nodes.get(2));
			box.add_node_link_simple(nodes.get(1), nodes.get(2));
			return true;
		}
		if (unmatched == 4) {
			boolean matchAB = true, matchAC = true, matchAD = true, matchBC = true, matchBD = true, matchCD = true;

			for (j = 0; j < 6; ++j) {
				if (sign_array[0 * 6 + j] != sign_array[1 * 6 + j])
					matchAB = false;
				if (sign_array[0 * 6 + j] != sign_array[2 * 6 + j])
					matchAC = false;
				if (sign_array[0 * 6 + j] != sign_array[3 * 6 + j])
					matchAD = false;
				if (sign_array[1 * 6 + j] != sign_array[2 * 6 + j])
					matchBC = false;
				if (sign_array[1 * 6 + j] != sign_array[3 * 6 + j])
					matchBD = false;
				if (sign_array[2 * 6 + j] != sign_array[3 * 6 + j])
					matchCD = false;
			}
			if (matchAB && matchCD && !matchAC) {
				box.add_node_link_simple(nodes.get(0), nodes.get(1));
				box.add_node_link_simple(nodes.get(2), nodes.get(3));
				return true;
			}
			if (matchAC && matchBD && !matchAB) {
				box.add_node_link_simple(nodes.get(0), nodes.get(2));
				box.add_node_link_simple(nodes.get(1), nodes.get(3));
				return true;
			}
			if (matchAD && matchBC && !matchAB) {
				box.add_node_link_simple(nodes.get(0), nodes.get(3));
				box.add_node_link_simple(nodes.get(1), nodes.get(2));
				return true;
			}
			if (failCountW++ == 0) {
				BoxClevA.log.printf("ERR: link 4 with zeros failed\n");
			}
			return false;
		}
		return false;
	}

	private Sol_info make_sol3(Key3D key, int xl, int yl, int zl, int denom, double posX, double posY, double posZ) {
		return new Sol_info(key, xl, yl, zl, denom, posX, posY, posZ);
	}

	/**
	 * Match pairs of nodes which have not been previously done.
	 * 
	 * @param count  number of nodes
	 * @param nodes  nodes on the box
	 * @param done   flag recording if a node has been done, set on matches
	 * @param undone indices of the still undone nodes
	 * @return number not yet matched
	 */
	private int match_all_nodes(List<Node_info> nodes, boolean[] done, int[] undone) {
		int i;
		int j;
		int unmatched;
		int count = nodes.size();
		for (i = 0; i < count; ++i) {
			if (done[i])
				continue;
			for (j = i + 1; j < count; ++j) {
				if (done[i] || done[j])
					continue;
				if (matchNodes(nodes.get(i),nodes.get(j)))
					done[i] = done[j] = true;
			}
		}
		unmatched = 0;
		for (i = 0; i < count; ++i) {
			if (!done[i]) {
				undone[unmatched] = i;
				++unmatched;
			}
			done[i] = false;
		}
		return unmatched;
	}

	private boolean matchNodes(Node_info A,Node_info B) {
		return ((A.sol.getDx() == B.sol.getDx()) && (A.sol.getDy() == B.sol.getDy())
				&& (A.sol.getDz() == B.sol.getDz()));
	}

	public void printResults() {
		System.out.printf("BoxSolver Fail counts P %d Q %d R %d S %d T %d U %d V %d W %d%n", failCountP, failCountQ, failCountR,
				failCountS, failCountT, failCountU, failCountV, failCountW);
	}

	private boolean SameFace(Sol_info s1, Sol_info s2) {
		switch (s1.type) {
		case FACE_LL:
		case FACE_RR:
			if (s2.type != FACE_LL && s2.type != FACE_RR)
				return false;
			if (s1.xl * s2.denom == s2.xl * s1.denom)
				return true;
			else
				return false;
		case FACE_FF:
		case FACE_BB:
			if (s2.type != FACE_FF && s2.type != FACE_BB)
				return false;
			if (s1.yl * s2.denom == s2.yl * s1.denom)
				return true;
			else
				return false;
		case FACE_UU:
		case FACE_DD:
			if (s2.type != FACE_UU && s2.type != FACE_DD)
				return false;
			if (s1.zl * s2.denom == s2.zl * s1.denom)
				return true;
			else
				return false;
		default:
			return false;
		}
	}

	private boolean Test4nodesLike011(List<Node_info> nodes, int order[]) {
		int num_match = 0;

		for (int i = 0; i < nodes.size(); ++i) {
			if (nodes.get(i).sol.num_zero_derivs() != 1)
				continue;
			num_match = 1;
			order[0] = i;
			for (int j = i + 1; j < nodes.size(); ++j) {
				if (nodes.get(i).sol.match_derivs(nodes.get(j).sol)) {
					order[num_match++] = j;
				}
			}
			if (num_match >= 4) {
				for (int j = num_match; j < nodes.size(); ++j) {
					order[j] = -1;
				}
				return true;
			}
		}
		return false;
	}

}
