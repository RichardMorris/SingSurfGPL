package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.singsurf.singsurf.asurf.EdgeAdjacencyList.TriEdge;

public class Triangulator implements TriangulatorI  {
	/**
	 * Stage of algorithm. 
	 */
	enum NonManifType {
		/** No check for non manifold edges in first run, 
		 * detected as final facets created. */
	SIMPLE, 
	/**
	 * Run through the input facets forwards.
	 */
	FORWARD, 
	/**
	 * Try running though the list backwards which can resolve some problems.
	 */
	BACKWARD}

	private static final boolean PRINT_CAL_METRICS = false;
	
	private NonManifType nonManifType;
	
	private BoxClevA boxclev;
	
	/** List completed facets so far */
	EdgeAdjacencyList temp_eal;
	
	EdgeAdjacencyList compleated_eal;
	List<TriEdge> nonmanif = new ArrayList<>();
	
	public int totalboxes=0;
	public int simpleFails=0;
	private int forwardFails=0;
	
	public int totalDoubledEdges=0;	
	
	int failCountA,failCountB,failCountC,failCountD,failCountE;
	int failCountF,failCountG,failCountH,failCountI;

	private boolean allGood;

	private int backwardFails=0;
	Bern3DContext ctx;

	Box_info current_box;
	
	public Triangulator(BoxClevA boxclev) {
		super();
		this.boxclev = boxclev;
		failCountA = failCountB = failCountC = failCountD = failCountE = 1;
		failCountF = failCountG = failCountH = failCountI = 1;
		EdgeAdjacencyList.numVerts=0;
	}

	public void init(Bern3DContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void printResults() {
		System.out.printf("Triangulator: edge*2 %d%n",totalDoubledEdges);
		System.out.printf("Triangulator: total boxes %d simple fails %d forward fails %d backward fails %d %n",
				totalboxes,simpleFails,forwardFails,backwardFails);
		System.out.printf("FailCounts A %d B %d C %d D %d E %d F %d G %d H %d I %d%n",
				failCountA,failCountB,failCountC,failCountD,failCountE,failCountF,failCountG,failCountH,failCountI);
	}

	class MetricResult {
		double val;
		Sol_info A, B, C;
		MetricResult left, right;

		public MetricResult(MetricResult left, MetricResult right) {
			super();
			this.left = left;
			this.right = right;
			this.val = left.val + right.val;
			if(nonManifType != NonManifType.SIMPLE && !test_non_manifold())
				this.val = Double.POSITIVE_INFINITY;
		}

		public MetricResult(double val, Sol_info a, Sol_info b, Sol_info c) {
			this.val = val;
			left = right = null;
			A = a;
			B = b;
			C = c;
			if(nonManifType != NonManifType.SIMPLE && !test_non_manifold())
				this.val = Double.POSITIVE_INFINITY;
		}

		/**
		 * Returns true if this set of triangle does not contain non manifold edges.
		 * If an edge is non-manifold in the incoming facets its considered OK.
		 * @return
		 */
		public boolean test_non_manifold() {
			EdgeAdjacencyList localEal = new EdgeAdjacencyList();
			addToEAL(localEal);
			for(Entry<TriEdge, List<Facet_info>> ent:localEal.getFacets_adjacent_to_edge().iterable()) {
				TriEdge edge = ent.getKey();
				if(nonmanif.contains(edge))
					continue;
				int global_count = isBoundaryEdge(edge) ? 1 : 0;
				int box_count = temp_eal.count_facets_adjacent_to_edge(edge);
				int local_count = localEal.count_facets_adjacent_to_edge(edge);
				if(local_count + box_count + global_count > 2) 
					return false;
			}
			return true;
		}
		
		/**
		 * Adds to an EAl without creating a facet
		 * @param eal
		 */
		void addToEAL(EdgeAdjacencyList eal) {
			if(left!=null) {
				left.addToEAL(eal);
				right.addToEAL(eal);
			} else {
				eal.addEdge(A, B, null);
				eal.addEdge(B, C, null);
				eal.addEdge(C, A, null);
			}
		}		
	}

	public void triangulate_facets(Box_info box) {
		if (box.facets == null || box.facets.isEmpty())
			return;
		this.current_box = box;

		triangulate_allfacets(box);

		calcStats(box.facets);
		return;

	}
	
	private void triangulate_allfacets(Box_info box) {

		
		this.nonManifType = NonManifType.SIMPLE;
		
		List<Facet_info> existing = box.facets;
		MetricResult res;
		++totalboxes;
		res = triangulate_facets(existing);
		if(res!=null) {
			box.facets = getFinalFacets(box,res);
			return;
		}
		
		++simpleFails;
		this.nonManifType = NonManifType.FORWARD;
		
		EdgeAdjacencyList pre_existing_eal = new EdgeAdjacencyList();
		for (Facet_info facet : box.facets) {
			pre_existing_eal.add(facet);
		}
		for(Entry<TriEdge, List<Facet_info>> ent:pre_existing_eal.getFacets_adjacent_to_edge().iterable()) {
			if(ent.getValue().size()>2)
				nonmanif.add(ent.getKey());
		}
		
		this.temp_eal = new EdgeAdjacencyList();
		
		existing = box.facets;
		res = triangulate_facets(existing);
		if(res!=null) {
			box.facets = getFinalFacets(box,res);
			return;
		}

		++forwardFails;

		this.nonManifType = NonManifType.BACKWARD;

		// failed going forward now try reverse order
		List<Facet_info> reversed = new ArrayList<>();
		for(int i=box.facets.size()-1;i>=0;--i) {
			reversed.add(box.facets.get(i));
		}
		
		res = triangulate_facets(reversed);
		
		if(!allGood)
			++backwardFails;
		
		box.facets = getFinalFacets(box,res);
	}

	public boolean isBoundaryEdge(TriEdge edge) {
		Sol_info A = edge.A;
		Sol_info B = edge.B;
		for(Face_info face:Arrays.asList(
				current_box.ll,current_box.rr,current_box.ff,
				current_box.bb,current_box.dd,current_box.uu)) {
			if(face.containsInclusive(A) && face.containsInclusive(B))
				return true;
		}
		return false;
	}

	public boolean isDomainEdge(TriEdge edge) {
		Sol_info A = edge.A;
		Sol_info B = edge.B;
		for(Face_info face:Arrays.asList(
				boxclev.global_sel_box.ll,boxclev.global_sel_box.rr,boxclev.global_sel_box.ff,
				boxclev.global_sel_box.bb,boxclev.global_sel_box.dd,boxclev.global_sel_box.uu)) {
			if(face.containsInclusive(A) && face.containsInclusive(B))
				return true;
		}
		return false;
	}

	private void calcStats(List<Facet_info> facets) {
		int num_new_edge=0;
		EdgeAdjacencyList finalEAL = new EdgeAdjacencyList();
		facets.forEach(facet -> finalEAL.add(facet));
		Iterator<Entry<TriEdge, List<Facet_info>>> itt = finalEAL.allEdgesItt();
		while(itt.hasNext()) {
			Entry<TriEdge, List<Facet_info>> ent = itt.next();
			TriEdge edge = ent.getKey();
			if(isDomainEdge(edge)) {
				num_new_edge += 2 ;
				
			} else if(isBoundaryEdge(edge)) {
				++num_new_edge;				
			} else {
				num_new_edge += 2 ;
			}
		}
		totalDoubledEdges += num_new_edge;
		
		for(Facet_info f:facets) {
			CyclicList<Sol_info> sols = f.getSols();
			Sol_info a = sols.get(0);
			Sol_info b = sols.get(1);
			Sol_info c = sols.get(2);
			for(Face_info face:Arrays.asList(current_box.ll,current_box.rr,current_box.ff,current_box.bb,current_box.dd,current_box.uu)) {
				if(face.containsInclusive(a) && face.containsInclusive(b) && face.containsInclusive(c)) {
					BoxClevA.log.println("Facet on face");
					BoxClevA.log.println(current_box.getHeader());
					BoxClevA.log.println(f);
				}
			}
			
			
		}
	}

	/**
	 * Add the facets from the results. 
	 * @param box
	 * @param res
	 * @return
	 */
	private List<Facet_info> getFinalFacets(Box_info box, MetricResult res) {
		List<Facet_info> list = new ArrayList<>();
		this.add_facets(list, res);
		
		return list;
	}
	
	public MetricResult triangulate_facets(List<Facet_info> existing) {
		
				MetricResult fullset=null;
				this.temp_eal = new EdgeAdjacencyList();				
		
				for (Facet_info facet : existing) {
					MetricResult combined=null;
		
					int size = facet.size();
					switch (size) {
					case 0:
					case 1:
					case 2:
						break;
					case 3:
					case 4: 
					case 5:
					case 6:
					case 7:
					case 8: {
						List<Sol_info> sols = facet.getSols();
						if(PRINT_CAL_METRICS) {
							
							sols.forEach(sol -> {
									sol.calcMeanCurvature(boxclev.globalRegion,ctx);
									BoxClevA.log.println(sol.toStringNorm(boxclev.globalRegion,ctx));} );
						}
						int indicies[] = new int[size];
						for (int i = 0; i < size; ++i)
							indicies[i] = i;
						combined = calc_metric(sols, indicies);
						break;
					}
		
					default: {
						combined = splitFacetManySols(facet.getSols(), combined);
						break;
					}
					}

					if(combined!=null) {
						if(nonManifType != NonManifType.SIMPLE)
							combined.addToEAL(temp_eal);
						if(fullset==null)
							fullset = combined;
						else
							fullset = new MetricResult(fullset,combined);
					}
				}
				
				if(fullset==null)
					return null;

				/* In the simple case each input facet has been considered independently
				 * Now check the full set.
				 * Nothing has been added to the temp_eal
				 */
				if(nonManifType==NonManifType.SIMPLE) {
					allGood = fullset.test_non_manifold();
					return allGood ? fullset : null;
				}
				 
				/* In the forward or backward case need to 
				 * clear out the temp eal before the non manif test
				 */
				this.temp_eal = new EdgeAdjacencyList();				
				allGood = fullset.test_non_manifold();
				
				if(allGood || nonManifType == NonManifType.BACKWARD ) {
					return fullset;
				}
				return null;
	}

	public MetricResult splitFacetManySols(CyclicList<Sol_info> sols, MetricResult combined) {
		if(sols.size()<=6) {
			int indicies[] = new int[sols.size()];
			for(int i=0;i<sols.size();++i) { indicies[i]=i; }
			MetricResult res = calc_metric(sols, indicies);
			if(combined==null) {
				combined = res;
			} else {
				combined = new MetricResult(combined,res);
			}
			return combined;
		}
		
		Sol_info[] solA = sols.toArray(new Sol_info[0]);
		boolean[][] sol_on_face = new boolean[solA.length][6];
		Face_info[] faces = current_box.facesAsArray();
		for(int i=0;i<solA.length;++i) {
			for(int j=0;j<6;++j) {
				sol_on_face[i][j] = (faces[j]!=null && faces[j].containsInclusive(solA[i]));
			}
		}
		int[] common_faces = new int[solA.length];
		for(int i=0;i<solA.length;++i) {
			for(int j=0;j<solA.length;++j) {
				if(i==j) continue;
				boolean match = false;
				for(int k=0;k<6;++k) {
					if(sol_on_face[i][k] && sol_on_face[j][k])
						match = true;
				}
				if(match) {
					++common_faces[i];
				}
			}
		}
		int min = sols.size();
		int minindex=0;
		for(int i=0;i<solA.length;++i) {
			if(common_faces[i] < min) {
				min = common_faces[i];
				minindex=i;
			}
		}
		int otherindex = minindex + sols.size()/2;
		CyclicList<Sol_info> listA = sols.subListCyclic(minindex, otherindex+1);
		CyclicList<Sol_info> listB = sols.subListCyclic(otherindex,sols.size()+minindex+1);

		combined = splitFacetManySols(listA,combined);
		combined = splitFacetManySols(listB,combined);
		return combined;
	}


	
	private double calc_metrics(Sol_info a, Sol_info b, Sol_info c) {
		if (a.num_zero_derivs() == 3 || b.num_zero_derivs() == 3 || c.num_zero_derivs() == 3)
			return 0;

		if (a == b || a == c || b == c)
			return 0;

		for(Face_info face:Arrays.asList(current_box.ll,current_box.rr,current_box.ff,current_box.bb,current_box.dd,current_box.uu)) {
			if(face.containsInclusive(a) && face.containsInclusive(b) && face.containsInclusive(c)) {
				return 0;
			}
		}

		double posA[] = a.calc_pos_actual(boxclev.globalRegion,ctx);
		double posB[] = b.calc_pos_actual(boxclev.globalRegion,ctx);
		double posC[] = c.calc_pos_actual(boxclev.globalRegion,ctx);

		if(posA[0] == posB[0] && posB[0]== posC[0]) return 1e3;
		if(posA[1] == posB[1] && posB[1]== posC[1]) return 1e3;
		if(posA[2] == posB[2] && posB[2]== posC[2]) return 1e3;
		
		double normA[] = a.calc_unit_norm(boxclev.globalRegion,ctx);
		double normB[] = b.calc_unit_norm(boxclev.globalRegion,ctx);
		double normC[] = c.calc_unit_norm(boxclev.globalRegion,ctx);

		double lenA = a.normal_length;
		double lenB = b.normal_length;
		double lenC = c.normal_length;

//		double Ha = 1;
//		double Hb = 1;
//		double Hc = 1;
//		double Hsum = 3;
//		if(boxclev.isRefineCurvature() || boxclev.colortype == BoxClevA.COLOUR_MEAN_CURVATURE) {
//			Ha = a.calcMeanCurvature(boxclev);
//			Hb = b.calcMeanCurvature(boxclev);
//			Hc = c.calcMeanCurvature(boxclev);
//			Hsum = Math.abs(Ha) + Math.abs(Hb) + Math.abs(Hc);
//		}
		double[] Norm = Vec3D.cross(Vec3D.subVec(posB, posA), Vec3D.subVec(posC, posA));
		double size = Vec3D.normalise(Norm);

		if (lenA == 0.0 || !Double.isFinite(lenA)) {
			return 0;
		}
		if (lenB == 0.0 || !Double.isFinite(lenB)) {
			return 0;
		}
		if (lenC == 0.0 || !Double.isFinite(lenC)) {
			return 0;
		}

		double u[] = new double[] { normB[0] - normA[0], normB[1] - normA[1], normB[2] - normA[2] };
		double v[] = new double[] { normC[0] - normA[0], normC[1] - normA[1], normC[2] - normA[2] };
		double cross = Math.abs(u[1] * v[2] - u[2] * v[1]) + Math.abs(u[2] * v[0] - u[0] * v[2])
				+ Math.abs(u[0] * v[1] - u[1] * v[0]);

		if (!Double.isFinite(size)) {
			if(failCountC++==0) {
				BoxClevA.log.println("Triangulator Bad size " + size);
				BoxClevA.log.println(a);
				BoxClevA.log.println(b);
				BoxClevA.log.println(c);
			}
			size = 1e3;
		}
		if (size == 0) {
			if(failCountD++==0) {
				BoxClevA.log.println("Triangulator Bad size " + size);
				BoxClevA.log.println(a);
				BoxClevA.log.println(b);
				BoxClevA.log.println(c);
			}
			size = 1e-6;
		}
		return Math.abs(cross) * Math.sqrt(size); // / Hsum;		
//		return Math.abs(cross) / size;
	}

	MetricResult calc_metric(List<Sol_info> sols, int indicies[]) {
		int len = indicies.length;
		if (len == 3) {
			Sol_info A = sols.get(indicies[0]);
			Sol_info B = sols.get(indicies[1]);
			Sol_info C = sols.get(indicies[2]);
			double v1 = calc_metrics(A, B, C);
			if(PRINT_CAL_METRICS)
			 BoxClevA.log.printf("calc_metric %s %9.5f%n",java.util.Arrays.toString(indicies),v1);
			return new MetricResult(v1, A, B, C);
		}

		MetricResult best_results = null;

		for (int i = 0; i < len; ++i) {
			int left_indicies[] = new int[] { indicies[(i - 1 + indicies.length) % indicies.length],
					indicies[(i + indicies.length) % indicies.length],
					indicies[(i + 1 + indicies.length) % indicies.length] };

			int right_indicies[] = new int[len - 1];
			for (int j = 0; j < i; ++j) {
				right_indicies[j] = indicies[j];
			}
			for (int j = i + 1; j < len; ++j) {
				right_indicies[j - 1] = indicies[j];
			}
			MetricResult left_results = calc_metric(sols, left_indicies);
			MetricResult right_results = calc_metric(sols, right_indicies);
			MetricResult combined = new MetricResult(left_results,right_results);
			if(PRINT_CAL_METRICS)
				BoxClevA.log.printf("calc_metric %s %9.5f%n",java.util.Arrays.toString(indicies),combined.val);

			if(best_results==null) {
				best_results = combined;
			} else if (combined.val < best_results.val) { // get the smallest result
				best_results = combined;				
			}
			if(len==4 && i==1) // only need two cases for quadrilaterals  
				break;
		}
		return best_results;
	}

	private void add_facets(List<Facet_info> list,MetricResult res) {
		if(res==null)
			return;
		if (res.left != null) {
			add_facets(list,res.left);
			add_facets(list,res.right);
			return;
		} else {
			Facet_info f1 = new Facet_info();
			f1.addSol(res.A);
			f1.addSol(res.B);
			f1.addSol(res.C);
			list.add(f1);
		}
	}

	@Override
	public int numDoubleEdges() {
		return totalDoubledEdges;
	}

	@Override
	public void count_edges(Box_info box) {
		int num_new_edge=0;
		if(box.facets==null) return;
		for(Facet_info facet:box.facets) {
			CyclicList<Sol_info> sols = facet.getSols();
			for(Sol_info  A:sols) {
				Sol_info B = sols.nextCyclic(A);
				TriEdge edge = new TriEdge(A,B);
				if(isDomainEdge(edge)) {
					num_new_edge += 2 ;
				} else {
					++num_new_edge;
				}
			}
		}
		this.totalDoubledEdges += num_new_edge;		
	}

}
