package org.singsurf.singsurf.asurf;

public interface Plotter {

	void init(Bern3DContext bern3dContext);

	void fini();

    void plot_box(Box_info box);

	Sol_info least_acurate_vertex();

	int numVertices();

	int numFaces();

	int numIsolatedSings();

	int numDejectEdges();

	int getNumSings();

	void printResults();


}