package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jv.geom.PgEdgeStar;
import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsObject;
import jv.vecmath.PiVector;

public class MeshCleaner implements AbstractMeshCleaner {
	BoxClevA boxclev;
	PgElementSet outSurf;
	PgPolygonSet outCurve;
	PgPointSet outPoints;
	int tag;
	Plotter plotter;

	public MeshCleaner(BoxClevA boxclev, PgElementSet outSurf, PgPolygonSet outCurve, PgPointSet outPoints) {
		super();
		this.boxclev = boxclev;
		this.outSurf = outSurf;
		this.outCurve = outCurve;
		this.outPoints = outPoints;
		plotter = (Plotter) boxclev.plotter;
	}

	/* (non-Javadoc)
	 * @see org.singsurf.singsurf.asurf.AbstractMeshCleaner#clean()
	 */
	@Override
	public void clean() {
		if (boxclev.cleanmesh == 1) {
			tag = PsObject.IS_DELETED;
		} else if (boxclev.cleanmesh == -1) {
			outSurf.makeNeighbour();
			return;
		} else {
			tag = PsObject.IS_SELECTED;
		}
		
		MeshTopology mt = new MeshTopology(outSurf);
		PgEdgeStar[] stars = outSurf.makeEdgeStars();
		if (stars == null) {
			BoxClevA.log.println("empty list of edge stars");
			return;
		}
		outSurf.makeNeighbour();
		List<PgEdgeStar> nonManif = new ArrayList<PgEdgeStar>();
		CountingMap<Integer> matchedVerts = new CountingMap<Integer>();

		// Build list of non mainfold edges and
		// list of vertices on those edges with a count of how many times they occur
		for (PgEdgeStar star : stars) {
			if (star.getValence() > 2) {
				nonManif.add(star);

				for (int i : star.m_data) {
					matchedVerts.increment(i);
				}
			}
		}

		remove_duplicate_triangles(mt, nonManif);

		remove_vertex_surronded_by_nonmanif_edges(mt, nonManif);

		remove_vertices_on_nonmainif_edge(nonManif, matchedVerts);

		if (boxclev.cleanmesh == 1) {
			outSurf.removeMarkedElements();
			outSurf.removeMarkedVertices();
			outSurf.makeEdgeStars();
			outSurf.makeNeighbour();
		}
	}

	public void remove_vertices_on_nonmainif_edge(List<PgEdgeStar> nonManif, CountingMap<Integer> matchedVerts) {
		// remove vertices when they are on more than two non-manifold edges
		// but keep those marked as convergence failures
		Iterator<PgEdgeStar> itt = nonManif.iterator();
		while (itt.hasNext()) {
			PgEdgeStar star = itt.next();
			int indA = star.getVertexInd(0);
			int indB = star.getVertexInd(1);
			Sol_info vecA = ((PlotAbstract) boxclev.plotter).badIndicies.get(indA);
			Sol_info vecB = ((PlotAbstract) boxclev.plotter).badIndicies.get(indB);

			if (vecA != null || vecB != null)
				continue;

			if (matchedVerts.get(indA) > 1 || matchedVerts.get(indB) > 1) {
				itt.remove();
			}
		}
	}

	public void remove_vertex_surronded_by_nonmanif_edges(MeshTopology mt, List<PgEdgeStar> nonManif) {
		for (int badVert : ((PlotAbstract) boxclev.plotter).badIndicies.keySet()) {
			List<Integer> eles = mt.elementsBy(badVert);
			boolean remove = true;
			for (int ele : eles) {
				PiVector vertsOnEle = outSurf.getElement(ele);
				if (vertsOnEle.getSize() != 3) {
					remove = false;
					break;
				}
				int vertA = -1, vertB = -1;
				for (int i = 0; i < 3; ++i) {
					int vert = vertsOnEle.getEntry(i);
					if (vert != badVert) {
						if (vertA == -1) {
							vertA = vert;
						} else {
							vertB = vert;
						}
					}
				}
				boolean isNonManifEdge = false;
				for (PgEdgeStar star : nonManif) {
					if ((star.getFirstEntry() == vertA && star.getLastEntry() == vertB)
							|| (star.getFirstEntry() == vertB && star.getLastEntry() == vertA)) {
						isNonManifEdge = true;
					}
				}
				if (!isNonManifEdge) {
					remove = false;
					break;
				}
			}
			if (remove) { // all opposite edges are non manifold
				for (int ele : eles) {
					outSurf.setTagElement(ele, tag);
				}
				outSurf.setTagVertex(badVert, tag);
			}
		}
	}

	static class SwappedElements {
		Integer dup_ele;
		Integer other_ele;
		int edgeVertA, edgeVertB, dupVertC, otherVertD;
		public SwappedElements(Integer dup_ele, Integer other_ele, int edgeVertA, int edgeVertB, int dupVertC,
				int otherVertD) {
			super();
			this.dup_ele = dup_ele;
			this.other_ele = other_ele;
			this.edgeVertA = edgeVertA;
			this.edgeVertB = edgeVertB;
			this.dupVertC = dupVertC;
			this.otherVertD = otherVertD;
		}

	}

	public void remove_duplicate_triangles(MeshTopology mt, List<PgEdgeStar> nonManif) {
		// Removes triangles created when two facets share more than two edges
		// the triangulator can then make two identical triangles
		
		List<SwappedElements> swaps = new ArrayList<>();
		for (PgEdgeStar star : nonManif) {
			Map<Integer, Set<Integer>> elements_matching_vert = new HashMap<>();
			int vertA = star.getFirstEntry();
			int vertB = star.getLastEntry();
			for (int eleIndex : star.getElementInd()) {
				PiVector ele = outSurf.getElement(eleIndex);
				int vertC = this.getOtherVertex(ele, vertA, vertB);
				if (vertC == -1)
					continue;
				Set<Integer> set = elements_matching_vert.get(vertC);
				if (set == null) {
					set = new HashSet<>();
					elements_matching_vert.put(vertC, set);
				}
				set.add(eleIndex);
			}
			if(elements_matching_vert.size() == 3) {
				Integer dupEntA = null;
				Integer dupEntB = null;
				Integer otherEntA = null;
				Integer otherEntB = null;
				Integer otherVertA = null;
				Integer otherVertB = null;
				Integer dupVert = null;
				for(Entry<Integer, Set<Integer>> ent:elements_matching_vert.entrySet()) {
					int key = ent.getKey();
					Set<Integer> elesset = ent.getValue();
					Integer[] eles = elesset.toArray(new Integer[2]);
					switch(elesset.size()) {
					case 1:
						if(otherEntA == null) {
							otherEntA = eles[0];
							otherVertA = key;
						}
						else {
							otherEntB = eles[0];
							otherVertB = key;
						}
						break;
					case 2:
						dupEntA = eles[0];
						dupEntB = eles[1];
						dupVert = key;
						break;
					default:
						break;
					}
				}
				if(dupEntA==null || dupEntB==null|| otherEntA == null || otherEntB == null)
					continue;
				SwappedElements swapA = new SwappedElements(dupEntA,otherEntA,vertA,vertB,dupVert,otherVertA);
				SwappedElements swapB = new SwappedElements(dupEntB,otherEntB,vertA,vertB,dupVert,otherVertB);
				swaps.add(swapA);
				swaps.add(swapB);
			}
			
//			
//			for (Entry<Integer, Set<Integer>> ent : elements_matching_vert.entrySet()) {
//				Set<Integer> set = ent.getValue();
//				if (set.size() == 2) {
//					for (int ele : set) {
//						outSurf.setTagElement(ele, tag);
//					}
//					if (mt.elementsBy(ent.getKey()).size() == 2)
//						outSurf.setTagVertex(ent.getKey(), tag);
//				}
//			}

		}
		
		for(SwappedElements swap:swaps) {
			PiVector dup_ele = outSurf.getElement(swap.dup_ele);
			PiVector other_ele = outSurf.getElement(swap.other_ele);
			if(boxclev.cleanmesh == 1) {
				// there a chance the its been swapped before
				int dupAindex = -1, dupBindex = -1, otherAindex = -1, otherBindex = -1, dupCindex = -1, otherDindex = -1;
				for(int i=0;i<3;++i) {
					if(dup_ele.m_data[i] == swap.edgeVertA)
						dupAindex = i;
					else if(dup_ele.m_data[i] == swap.edgeVertB)
						dupBindex = i;
					else
						dupCindex = i;
				}
				for(int i=0;i<3;++i) {
					if(other_ele.m_data[i] == swap.edgeVertA)
						otherAindex = i;
					else if(other_ele.m_data[i] == swap.edgeVertB)
						otherBindex = i;
					else
						otherDindex = i;
				}
				if(dupAindex == -1 || dupBindex == -1 || otherAindex == -1
						|| otherBindex == -1 || dupCindex == -1 || otherDindex == -1) {
					BoxClevA.log.println("Expected verts not found on element");
					continue;
				}
				int vertC = dup_ele.m_data[dupCindex];
				int vertD = other_ele.m_data[otherDindex];
				dup_ele.m_data[0] = swap.edgeVertA;
				dup_ele.m_data[1] = vertC;
				dup_ele.m_data[2] = vertD;
				other_ele.m_data[0] = vertC;
				other_ele.m_data[1] = swap.edgeVertB;
				other_ele.m_data[2] = vertD;
			} else {
				outSurf.setTagElement(swap.dup_ele, PsObject.IS_SELECTED);
				outSurf.setTagElement(swap.other_ele, PsObject.IS_SELECTED);
			}
			
		}
	}

	public void bad_vertex_opposite_non_manif_edge(MeshTopology mt, List<PgEdgeStar> nonManif) {
		Iterator<PgEdgeStar> itt;
		// For each existing edge now delete
		itt = nonManif.iterator();
		while (itt.hasNext()) {
			PgEdgeStar star = itt.next();

			int vertA = star.getFirstEntry();
			int vertB = star.getLastEntry();
			List<Integer> eles = mt.elementsBy(vertA);
			for (int ele : eles) {
				PiVector vertsOnEle = outSurf.getElement(ele);
				if (vertsOnEle.contains(vertB)) {
					int vertC = getOtherVertex(vertsOnEle, vertA, vertB);
					if (vertC == -1)
						continue;
					Sol_info vecC = ((PlotAbstract) boxclev.plotter).badIndicies.get(vertC);
					if (vecC == null) {
						continue;
					}
					List<Integer> elesByC = mt.elementsBy(vertC);
					if (elesByC.size() > 2)
						continue;
					boolean flag = true;
					for (int ele2 : elesByC) {
						PiVector vertsOnEle2 = outSurf.getElement(ele2);

						for (int vertD : vertsOnEle2.getEntries()) {
							if (vertD != vertA && vertD != vertB && vertD != vertC) {
								flag = false;
								break;
							}
						}
					}
					if (!flag)
						continue;

					// Now just a vertex opposite a non manifold edge
					// with a bad conv flag
					// and only on faces attach to the non manif edge

					outSurf.setTagElement(ele, tag);
					outSurf.setTagVertex(vertC, tag);

				}
			}

		}
	}

	/**
	 * Gets the other vertex on a triangular face
	 * 
	 * @param vertsOnEle
	 * @param vertA
	 * @param vertB
	 * @return index of vert or -1 if non triangular or does not contain both verts
	 */
	private int getOtherVertex(PiVector vertsOnEle, int vertA, int vertB) {
		if (vertsOnEle.getSize() != 3)
			return -1;
	
		if(vertA == vertB) return -1;
		boolean matchA = false;
		boolean matchB = false;
		int vertC=-1;
		for (int vert : vertsOnEle.getEntries()) {
			if(vert == vertA)
				matchA = true;
			else if(vert == vertB)
				matchB = true;
			else
				vertC = vert;
		}
		if(matchA && matchB)
			return vertC;
		return -1;
	}
}
