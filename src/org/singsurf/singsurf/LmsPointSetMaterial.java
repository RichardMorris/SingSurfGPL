package org.singsurf.singsurf;

import java.awt.Color;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;

/**
 * Class to hold all the global display attributes of a geometry. Note: anything
 * to do with textures or vector fields has been ignored.
 **/

public class LmsPointSetMaterial {
    public Color gVertCol;
    public Color gVertNormCol;
    public double gVertNormLen;
    public double gVertNormSize;
    public double gVertSize;
    public boolean defaultLabEnable;
    public boolean showIndices;
    public boolean showVertCols;
    public boolean showVertLabels;
    public boolean showVertNormArrow;
    public boolean showVertNorms;
    public boolean showVerts;

    /** Reads the display attributes from a geometry. **/

    public LmsPointSetMaterial(PgPointSet geom) {
	showVerts = geom.isShowingVertices();
	showVertCols = geom.isShowingVertexColors();
	gVertCol = geom.getGlobalVertexColor();
	gVertSize = geom.getGlobalVertexSize();
	defaultLabEnable = geom.isEnabledIndexLabels();
	showIndices = geom.isShowingIndices();
	showVertLabels = geom.isShowingVertexLabels();
	showVertNormArrow = geom.isShowingVertexNormalArrow();
	showVertNorms = geom.isShowingVertexNormals();
	gVertNormCol = geom.getGlobalVertexNormalColor();
	gVertNormLen = geom.getGlobalVertexNormalLength();
	gVertNormSize = geom.getGlobalVertexNormalSize();
    }

    /** Sets the display attributes of a geometry **/

    public void apply(PgGeometryIf geom) {
    	PgPointSet output = (PgPointSet) geom;
	output.setGlobalVertexColor(gVertCol);
	output.setGlobalVertexNormalColor(gVertNormCol);
	output.setGlobalVertexNormalLength(gVertNormLen);
	output.setGlobalVertexNormalSize(gVertNormSize);
	output.setGlobalVertexSize(gVertSize);
	output.setEnabledIndexLabels(defaultLabEnable);
	output.showIndices(showIndices);
	output.showVertexColors(showVertCols);
	output.showVertexLabels(showVertLabels);
	output.showVertexNormalArrow(showVertNormArrow);
	output.showVertexNormals(showVertNorms);
	output.showVertices(showVerts);
    }

    void appendShowHide(StringBuffer sb, String attName, boolean show) {
	if (show)
	    sb.append(" " + attName + "=\"show\"");
	else
	    sb.append(" " + attName + "=\"hide\"");
    }

    void appendColor(StringBuffer sb, String tag, Color c) {
	sb.append("\t<" + tag + ">" + c.getRed() + " " + c.getGreen() + " " + c.getBlue() + "</" + tag + ">\n");

    }

    void appendNumber(StringBuffer sb, String tag, double val) {
	sb.append("\t<" + tag + ">");
	sb.append(val);
	sb.append("</" + tag + ">\n");
    }

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("<pointSetMaterial");
	appendShowHide(sb, "point", showVerts);
	appendShowHide(sb, "color", showVertCols);
	appendShowHide(sb, "normal", showVertNorms);
	appendShowHide(sb, "normalArrow", showVertNormArrow);
	sb.append(">\n");
	appendColor(sb, "color", gVertCol);
	appendNumber(sb, "thickness", gVertSize);
	appendColor(sb, "normColor", gVertNormCol);
	appendNumber(sb, "normThickness", gVertNormSize);
	appendNumber(sb, "normLength", gVertNormLen);

	sb.append("</pointSetMaterial>\n");
	return sb.toString();
    }

    /**
     * Extract the material for a geometry.
     * 
     * @param geom
     * @return the correct material subclass for the geometry
     */
    public static LmsPointSetMaterial getMaterial(PgGeometryIf geom) {
	if (geom instanceof PgElementSet)
	    return new LmsElementSetMaterial((PgElementSet) geom);
	if (geom instanceof PgPolygonSet)
	    return new LmsPolygonSetMaterial((PgPolygonSet) geom);
	if (geom instanceof PgPointSet)
	    return new LmsPointSetMaterial((PgPointSet) geom);
	return null;
    }
}
