package org.singsurf.singsurf.asurf;

/**
 * Defines operation for the plotting stage
 */
public interface Plotter {
	/**
	 * Sets up the plotter
	 * @param bern3dContext
	 */
	void init(Bern3DContext bern3dContext);

	/**
	 * Called when all boxes plotted, may flush buffers and write output.
	 */
	void fini();

	/**
	 * Plot everything inside a box.
	 * @param box
	 */
    void plot_box(Box_info box);

	Sol_info least_acurate_vertex();

	int numVertices();

	int numFaces();

	int numIsolatedSings();

	int numDejectEdges();

	int getNumSings();

	void printResults();


}