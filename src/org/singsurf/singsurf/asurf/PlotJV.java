package org.singsurf.singsurf.asurf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsObject;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public class PlotJV extends PlotAbstract {
    PgElementSet elements;
    PgPointSet points;
    PgPolygonSet lines;
    
  List<PiVector> eles    = new ArrayList<PiVector>();
  List<PdVector> verts = new ArrayList<PdVector>();
  List<PdVector> norms = new ArrayList<PdVector>();
  List<Color> cols = new ArrayList<Color>();


    public PlotJV(BoxClevA boxclev, PgElementSet elements,
            PgPolygonSet lines, PgPointSet points,PlotMode mode) {
        super(boxclev,mode);
        this.elements = elements;
        this.points = points;
        this.lines = lines;
    }

    public void clear() {
    	this.eles.clear();
    	this.verts.clear();
    	this.norms.clear();
    }

	@Override
	protected void addFace(int[] ind) {
        PiVector indices = new PiVector(ind);
        this.eles.add(indices);
		
	}

	@Override
	protected void addColour(Color calcSolColour) {
    	cols.add(calcSolColour);
		
	}

	@Override
	protected void addNormal(double[] norm) {
	    norms.add(new PdVector(norm));
	}

	@Override
	protected void addVertex(double[] vec) {
	    verts.add(new PdVector(vec));
	}


    public void fini()
    {
        PiVector res[] = new PiVector[eles.size()];
        PdVector[] pts = new PdVector[verts.size()];
        PdVector[] ns  = new PdVector[norms.size()];

        elements.setNumVertices(verts.size());
        elements.setVertices(verts.toArray(pts));
        elements.setVertexNormals(norms.toArray(ns));
        if(boxclev.colortype>0) {
            Color[] co = cols.toArray(new Color[cols.size()]);
          elements.setVertexColors(co);        	
        }
        elements.setNumElements(res.length);
        elements.setElements(eles.toArray(res));
        if(boxclev.tagbad>0) {
        	for(int i:badIndicies.keySet()) {
        		elements.setTagVertex(i, PsObject.IS_SELECTED);
        	}
        }
        if(boxclev.tagSing>0) {
        	for(int i:singIndicies.keySet()) {
        		elements.setTagVertex(i, PsObject.IS_SELECTED);
        	}
        }
    }

	@Override
	protected void addPoint(double[] vec) {
        PdVector pdv = new PdVector(vec);
        this.points.addVertex(pdv);	
	}

	@Override
	protected void addLineVertex(double[] vec) {
        lines.addVertex(new PdVector(vec));
	}

	@Override
	protected void setLineColour(Color lineColor) {
        lines.setPolygonColor(lastLineIndex, lineColor);		
	}

	int lastLineIndex=0;
	@Override
	protected void addLineEdge(int indexA, int indexB) {
		lastLineIndex = lines.addPolygon(new PiVector(indexA,indexB));

	}

	
}
