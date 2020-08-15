package org.singsurf.singsurf;

import java.awt.Color;

import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;

/**
 * Class to hold all the global display attributes of a geometry. Note: anything
 * to do with textures or vector fields has been ignored.
 **/

public class LmsPolygonSetMaterial extends LmsPointSetMaterial {
    public Color gPolyCol;
    public Color gPolyNormCol;
    public double gPolyNormLen;
    public double gPolyNormSize;
    public double gPolySize;

    public boolean showEdgeLabels;
    public boolean showPolyCols;
    public boolean showPolyEndArrow;
    public boolean showPolyLabels;
    public boolean showPolyNormArrow;
    public boolean showPolyNorms;
    public boolean showPolys;
    public boolean showPolyStartArrow;

    /** Reads the display attributes from a geometry. **/

    public LmsPolygonSetMaterial(PgPolygonSet geom) {
	super(geom);

	gPolyCol = geom.getGlobalPolygonColor();
	gPolyNormCol = geom.getGlobalPolygonNormalColor();
	gPolyNormLen = geom.getGlobalPolygonNormalLength();
	gPolyNormSize = geom.getGlobalPolygonNormalSize();
	gPolySize = geom.getGlobalPolygonSize();

	showEdgeLabels = geom.isShowingEdgeLabels();
	showPolyCols = geom.isShowingPolygonColors();
	showPolyEndArrow = geom.isShowingPolygonEndArrow();
	showPolyLabels = geom.isShowingPolygonLabels();
	showPolyNormArrow = geom.isShowingPolygonNormalArrow();
	showPolyNorms = geom.isShowingPolygonNormals();
	showPolys = geom.isShowingPolygons();
	showPolyStartArrow = geom.isShowingPolygonStartArrow();
    }

    /** Sets the display attributes of a geometry **/

    @Override
    public void apply(PgGeometryIf geom) {
    	PgPolygonSet curve = (PgPolygonSet) geom; 
	super.apply(curve);

	curve.setGlobalPolygonColor(gPolyCol);
	curve.setGlobalPolygonNormalColor(gPolyNormCol);
	curve.setGlobalPolygonNormalLength(gPolyNormLen);
	curve.setGlobalPolygonNormalSize(gPolyNormSize);
	curve.setGlobalPolygonSize(gPolySize);

	curve.showEdgeLabels(showEdgeLabels);
	curve.showPolygonColors(showPolyCols);
	curve.showPolygonEndArrow(showPolyEndArrow);
	curve.showPolygonLabels(showPolyLabels);
	curve.showPolygonNormalArrow(showPolyNormArrow);
	curve.showPolygonNormals(showPolyNorms);
	curve.showPolygons(showPolys);
	curve.showPolygonStartArrow(showPolyStartArrow);
	// useGlobalPolygonSize()
    }

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer(super.toString());
	sb.append("<polygonSetMaterial");
	appendShowHide(sb, "line", showPolys);
	appendShowHide(sb, "arrow", showPolyEndArrow);
	appendShowHide(sb, "color", showPolyCols);
	appendShowHide(sb, "arrowStart", showPolyStartArrow);
	sb.append(">\n");

	appendColor(sb, "color", gPolyCol);
	appendNumber(sb, "thickness", gPolySize);

	sb.append("</polygonSetMaterial>\n");
	return sb.toString();
    }
}
