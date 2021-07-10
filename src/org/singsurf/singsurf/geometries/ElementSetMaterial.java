package org.singsurf.singsurf.geometries;

import jv.geom.PgElementSet;
import jv.project.PgGeometryIf;

import java.awt.Color;

/**
 * Class to hold all the global display attributes of a geometry. Note: anything
 * to do with textures or vector fields has been ignored.
 **/

public class ElementSetMaterial extends PointSetMaterial {
	public Color gEleCol;
	public Color gEleBackCol;
	public Color gEleNormCol;
	public Color gEdgeCol;
	public Color gBndCol;
	public double gEleNormLen;
	public double gEleNormSize;
	public double gBndSize;
	public double gEdgeSize;

	public boolean showEdgeLabels;
	public boolean showEleCols;
	public boolean showEleLabels;
	public boolean showEleNormArrow;
	public boolean showEleNorms;
	public boolean showEles;
	public boolean showElementColorFromVertex;

	public boolean showEdge;
	public boolean showBnd;
	public boolean showEdgeCols;
	public boolean showBack;
	public boolean showEleBackCols;

	/** Reads the display attributes from a geometry. **/

	public ElementSetMaterial(PgElementSet geom) {
		super(geom);

		gEleCol = geom.getGlobalElementColor();
		gEleBackCol = geom.getGlobalElementBackColor();
		gEleNormCol = geom.getGlobalElementNormalColor();
		gEleNormLen = geom.getGlobalElementNormalLength();
		gEleNormSize = geom.getGlobalElementNormalSize();

		gEdgeCol = geom.getGlobalEdgeColor();
		gEdgeSize = geom.getGlobalEdgeSize();

		gBndCol = geom.getGlobalBndColor();
		gBndSize = geom.getGlobalBndSize();

		showEdgeLabels = geom.isShowingEdgeLabels();
		showEleCols = geom.isShowingElementColors();
		showEleLabels = geom.isShowingElementLabels();
		showEleNormArrow = geom.isShowingElementNormalArrow();
		showEleNorms = geom.isShowingElementNormals();
		showEles = geom.isShowingElements();
		showElementColorFromVertex = geom.isShowingElementColorFromVertices();

		showEdge = geom.isShowingEdges();
		showBnd = geom.isShowingBoundaries();
		showEdgeCols = geom.isShowingEdgeColors();
		showBack = geom.isShowingBackface();
		showEleBackCols = geom.isShowingElementBackColors();

	}

	/** Sets the display attributes of a geometry **/

	@Override
	public void apply(PgGeometryIf geom) {
		PgElementSet surf = (PgElementSet) geom;
		super.apply(surf);

		surf.setGlobalElementColor(gEleCol);
		surf.setGlobalElementBackColor(gEleBackCol);
		surf.setGlobalElementNormalColor(gEleNormCol);
		surf.setGlobalElementNormalLength(gEleNormLen);
		surf.setGlobalElementNormalSize(gEleNormSize);

		surf.setGlobalEdgeColor(gEdgeCol);
		surf.setGlobalEdgeSize(gEdgeSize);
		surf.setGlobalBndColor(gBndCol);
		surf.setGlobalBndSize(gBndSize);

		surf.showEdgeLabels(showEdgeLabels);
		surf.showElementColors(showEleCols);
		surf.showElementLabels(showEleLabels);
		surf.showElementNormalArrow(showEleNormArrow);
		surf.showElementNormals(showEleNorms);
		surf.showElements(showEles);
		surf.showElementColorFromVertices(showElementColorFromVertex);

		surf.showEdges(showEdge);
		surf.showBoundaries(showBnd);
		surf.showEdgeColors(showEdgeCols);
		surf.showBackface(showBack);
		surf.showElementBackColors(showEleBackCols);
		// useGlobalElementSize()

	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("<elementSetMaterial");
		appendShowHide(sb, "face", showEles);
		appendShowHide(sb, "edge", showEdge);
		appendShowHide(sb, "color", showEleCols);
		appendShowHide(sb, "normal", showEleNorms);
		appendShowHide(sb, "normalArrow", showEleNormArrow);
		appendShowHide(sb, "backface", showBack);
		appendShowHide(sb, "boundary", showBnd);
		sb.append(">\n");

		appendColor(sb, "color", gEleCol);
		appendColor(sb, "colorBack", gEleBackCol);
		appendColor(sb, "normColor", gEleNormCol);
		appendColor(sb, "edgeColor", gEdgeCol);

		appendNumber(sb, "normLen", gEleNormLen);
		appendNumber(sb, "normThickness", gEleNormSize);
		appendNumber(sb, "edgeThickness", gEdgeSize);
		appendNumber(sb, "boundaryThickness", gBndSize);
		appendShowHide(sb,"transparancy",showTrans);
		appendNumber(sb,"transparancy",alpha);

		sb.append("</elementSetMaterial>\n");
		return sb.toString();
	}
}
