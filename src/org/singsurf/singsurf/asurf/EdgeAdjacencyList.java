package org.singsurf.singsurf.asurf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class EdgeAdjacencyList {

	public CountingListMap<Sol_info,TriEdge> edge_adjacent_to_vert = new CountingListMap<>();
	interface FAE {
		public Integer getCount(TriEdge key);
		public int size();
		public Iterator<Entry<TriEdge, List<Facet_info>>> iterator();
		public Iterable<Entry<TriEdge, List<Facet_info>>> iterable();
		public List<Facet_info> add(TriEdge edge, Facet_info facet);
		public boolean contains(TriEdge edge);
	}
	
	class SimpleFAE extends CountingListMap<TriEdge,Facet_info> implements FAE {
		private static final long serialVersionUID = 1L;

		@Override
		public Iterator<Entry<TriEdge, List<Facet_info>>> iterator() {
			return super.entrySet().iterator();
		}

		@Override
		public Iterable<Entry<TriEdge, List<Facet_info>>> iterable() {
			return super.entrySet();
		}

		@Override
		public boolean contains(TriEdge edge) {
			return this.containsKey(edge);
		}
	}
	
	private FAE facets_adjacent_to_edge;
	public Set<TriEdge> nonManifEdges = new HashSet<>();

	static int numVerts=0;

	public static class TriEdge {
		Sol_info A, B;
		int numA;
		int numB;
		public TriEdge(Sol_info a, Sol_info b) {
			numA = getVertNum(a);
			numB = getVertNum(b);
			if(numB< numA) {
				numA = b.adjNum;
				numB = a.adjNum;
				A = b;
				B = a;
			} else if(numB> numA) {
				A = a;
				B = b;
			} else {
				BoxClevA.log.println("Identical solutions on edge");
			}
		}

		private synchronized static int getVertNum(Sol_info sol) {
			if(sol.adjNum<0)
				sol.adjNum = numVerts++;
			return sol.adjNum;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + numA;
			result = prime * result + numB;
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
			TriEdge other = (TriEdge) obj;
			if (numA != other.numA)
				return false;
			if (numB != other.numB)
				return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(A.toStringCore(String.format("A%6d: ",numA)));
			sb.append('\n');
			sb.append(B.toStringCore(String.format("B%6d: ",numB)));
			sb.append('\n');
			return sb.toString();
		}
		public String toStringBrief() {
			return " Edge ["+numA+" "+numB+"]";
		}
	}


	
	
	public EdgeAdjacencyList() {
		this.facets_adjacent_to_edge = new SimpleFAE();
	}

	/**
	 * Adds an edge of a facet.
	 * @param a
	 * @param b
	 * @param facet
	 */
	public void addEdge(Sol_info a,Sol_info b,Facet_info facet) {
		TriEdge edge = getEdge(a, b);
		if(edge==null) {
			edge = new TriEdge(a,b);
			edge_adjacent_to_vert.add(a, edge);
			edge_adjacent_to_vert.add(b, edge);
		}
		FAE fae2 = getFacets_adjacent_to_edge();
		fae2.add(edge, facet);
		if(getFacets_adjacent_to_edge().getCount(edge)>2) {
			nonManifEdges.add(edge);
		}
	}

	/**
	 * Gets the edge if it all ready exists between two sols
	 * @return the edge or null if edge not already created
	 */
	public TriEdge getEdge(Sol_info a, Sol_info b) {
		List<TriEdge> list = edge_adjacent_to_vert.get(a);
		if(list==null) return null;

		TriEdge edge=null;
		for(TriEdge e:list) {
			if( (e.A == a && e.B == b)
					|| (e.A == b && e.B == a) ) {
				edge = e;
				break;
			}
		}
		return edge;
	}

	/**
	 * Adds all the edges surrounding a facet,
	 * builds the adjacency lists.
	 * @param facet
	 */
	public void add(Facet_info facet) {
		
		if(facet.size()<3) 
			return;
		Sol_info start=null;
		Sol_info prev=null;
		Sol_info cur;
		
		Iterator<Sol_info> itt = facet.solsItt().iterator();
		while(itt.hasNext()) {
			cur = itt.next();
			if(start==null) start=cur;
			if(prev!=null) 
				addEdge(prev,cur,facet);
			prev = cur;
		}
		addEdge(prev,start,facet);
	}
	
	public Integer count_facets_adjacent_to_edge(TriEdge key) {
		return getFacets_adjacent_to_edge().getCount(key);
	}

	public FAE getFacets_adjacent_to_edge() {
		return facets_adjacent_to_edge;
	}

	public Iterator<Entry<TriEdge, List<Facet_info>>> allEdgesItt() {
		return getFacets_adjacent_to_edge().iterator();
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("edge_adjacent_to_vert\n");
		for (Entry<Sol_info, List<TriEdge>> ent : edge_adjacent_to_vert.entrySet()) {
			Sol_info vert = ent.getKey();
			sb.append(vert.toStringCore("Vert " + vert.adjNum + " "));
			sb.append('\n');
			for (TriEdge edge : ent.getValue()) {
				sb.append(edge.toStringBrief());
			}
		}

		sb.append("facets_adjacent_to_edge\n");
		for(Entry<TriEdge, List<Facet_info>> ent:getFacets_adjacent_to_edge().iterable()) {
			sb.append(ent.getKey());
			for(Facet_info facet:ent.getValue()) {
				sb.append("  ");
				if(facet==null)
					sb.append("    null\n");
				else
					sb.append(facet.toStringBrief());
			}
		}

		return sb.toString();
	}

	public CountingListMap<Sol_info, TriEdge> getEdge_adjacent_to_vert() {
		return edge_adjacent_to_vert;
	}

	public String print_verts_and_edges() {
		StringBuilder sb = new StringBuilder();
		for (Entry<Sol_info, List<TriEdge>> ent : edge_adjacent_to_vert.entrySet()) {
			Sol_info vert = ent.getKey();
			sb.append("Vert " + vert.adjNum + " ");
			sb.append('\n');
		}
		for(Entry<TriEdge, List<Facet_info>> ent:getFacets_adjacent_to_edge().iterable()) {
			sb.append(ent.getKey().toStringBrief() + " count " + ent.getValue().size() + "\n");
		}	
		return sb.toString();
	}

	
}
