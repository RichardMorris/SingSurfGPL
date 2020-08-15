package org.singsurf.singsurf.asurf;

public interface TriangulatorI {

	void triangulate_facets(Box_info box);

	void printResults();

	void init(Bern3DContext bern3dContext);

	/**
	 * Twice the number of edges.
	 * @return
	 */
	int numDoubleEdges();

	void count_edges(Box_info box);
}
