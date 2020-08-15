/*
Created 25-Apr-2006 - Richard Morris
 */
package org.singsurf.singsurf.operators;

import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.jepwrapper.EvaluationException;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

/**
 * Extrude a curve based on a calculator
 */
public abstract class AbstractExtrude extends AbstractOperator {
	protected static final boolean PRINTDEBUG=false;

	DefVariable varY;
	boolean asLineBundle=false;
	public AbstractExtrude(DefVariable varY) {
		super();
		this.varY = varY;
	}

	public void setAsLineBundle(boolean state) {
		this.asLineBundle = state;
	}

	abstract PdVector extrude(PdVector vec,double y) throws EvaluationException;


	protected void extrudePolygon(int baseIndex,PgPolygonSet geom, PiVector element, PgPointSet outGeom) throws EvaluationException
	{
		for(int i=0;i<element.getSize();++i)
		{
			for (int j = 0; j < varY.getSteps(); ++j) {
				double y = varY.getSteps() > 1 
						? varY.getMin() + ((varY.getMax() - varY.getMin()) * j) / (varY.getSteps()-1)
								: (varY.getMax() - varY.getMin())/2;
						PdVector pt = extrude(getVertexOrTexture(geom, element.getEntry(i)),y);
						int ind = (baseIndex + i) * varY.getSteps() + j;
						outGeom.setVertex(ind,pt);
			}
		}


	}

	@Override
	public PgPolygonSet operatePoints(PgPointSet geom) throws EvaluationException {
		PgPolygonSet outGeom = new PgPolygonSet(3);
		int count = geom.getNumVertices();
		outGeom.setNumPolygons(count);
		for(int i=0;i<count;++i) {
			PiVector poly = new PiVector(varY.getSteps());
			for (int j = 0; j < varY.getSteps(); ++j) {
				double y = varY.getSteps() > 1 
						? varY.getMin() + ((varY.getMax() - varY.getMin()) * j) / (varY.getSteps()-1)
								: (varY.getMax() - varY.getMin())/2;
						PdVector pt = extrude(getVertexOrTexture(geom,i),y);
						int ind = geom.addVertex(pt);
						poly.addEntry(ind);
			}
			outGeom.addPolygon(poly);
		}
		return outGeom;
	}

	@Override
	public PgElementSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		PgElementSet outGeom = new PgElementSet(3);
		int count=0;
		for(int i=0;i<geom.getNumPolygons();++i)
			count+=geom.getPolygon(i).getSize();
		outGeom.setNumVertices(count * varY.getSteps() );
		outGeom.makeQuadrConn(count, varY.getSteps());
		//		outGeom.setDimOfElements(4 or -1);


		count =0;
		for(int i=0;i<geom.getNumPolygons();++i)
		{
			extrudePolygon(count,geom,geom.getPolygon(i),outGeom);
			count+=geom.getPolygon(i).getSize();
		}
		return outGeom;
	}

	private PgGeometryIf operateCurveAsBundle(PgPolygonSet geom) throws EvaluationException {
		PgPolygonSet outGeom = new PgPolygonSet(3);
		int count=0;
		for(int i=0;i<geom.getNumPolygons();++i)
			count+=geom.getPolygon(i).getSize();

		outGeom.setNumPolygons(count);

		int steps = varY.getSteps();
		count =0;
		for(int i=0;i<geom.getNumPolygons();++i) {
			extrudePolygon(count,geom,geom.getPolygon(i),outGeom);
			for(int k=0;k<geom.getPolygon(i).getSize();++k) {
				int indices[] = new int[steps];
				for(int j=0;j<steps;++j) {
					indices[j]=(count+k)*steps+j;
				}
				outGeom.setPolygon(count+k, new PiVector(indices));
			}
			count+=geom.getPolygon(i).getSize();
		}

		return outGeom;
	}


	@Override
	public PgElementSet operateSurface(PgElementSet geom) {
		return null;
	}

	@Override
	public PgGeometryIf operate(PgGeometryIf geom) throws UnSuportedGeometryException, EvaluationException {
		findTextureRange(geom);

		if(this.asLineBundle) {
			if(geom instanceof PgPolygonSet)
				return operateCurveAsBundle((PgPolygonSet) geom);			
		} else {
			//		if(geom instanceof PgElementSet)
			//			return operateSurface((PgElementSet) geom);
			if(geom instanceof PgPolygonSet)
				return operateCurve((PgPolygonSet) geom);
			if(geom instanceof PgPointSet)
				return operatePoints((PgPointSet) geom);
		}
		throw new UnSuportedGeometryException("Bad geometry type: "+geom.getClass().getName());
	}




}
