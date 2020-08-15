package org.singsurf.singsurf.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import org.singsurf.singsurf.IntFractometer;
import org.singsurf.singsurf.asurf.CountingMap;
import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;

import jv.geom.PgElementSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public class DualCloudOperator extends AbstractOperatorClient {
	private static final long serialVersionUID = 1L;

	IntFractometer numLines;
	IntFractometer resolution;
	
	
	public DualCloudOperator(GeomStore store, String projName) {
		super(store, projName);
		myinit();
	}
	
	public void myinit() {
		super.init();
		this.numLines = new IntFractometer(10, 1);
		this.resolution = new IntFractometer(20,1);
//		numLines.setParent(this);
//		resolution.setParent(this);
	}

	@Override
	public void geometryDefHasChanged(AbstractClient client, Calculator inCalc) {
	}

	public void calculate() {
		this.calcGeoms();
	}

	
	@Override
	public PgGeometryIf calcGeomThread(GeomPair pair) {
		PgElementSet input = (PgElementSet) pair.getInput();

		int nLines = this.numLines.getIntValue();
		int resolution = this.resolution.getIntValue();
		PgPolygonSet polygons = new PgPolygonSet();
		
		/*
		PgPointSet out = new PgPointSet(3);
		out.setNumVertices(num);
		for(int i=0;i<num;++i) {
			PdVector pt = input.getVertex(i);
			PdVector norm = input.getVertexNormal(i);
			double dist = pt.dot(norm);
			PdVector mul = PdVector.copyNew(norm);
			mul.multScalar(dist);
			out.setVertex(i, mul);
		}
		*/
		
		
		PdVector[] bds = input.getBounds();
		LineCounter lineCounter = new LineCounter(bds,resolution);
		
		int numVertices = input.getNumVertices();
		for(int i=0;i<numVertices;++i) {
			for(int j=i+1;j<numVertices;++j) {
				lineCounter.addLine(input.getVertex(i), input.getVertex(j));
			}
		}
		
		List<BoxPairValue> lines = lineCounter.topHits(nLines);
		for(BoxPairValue line:lines) {
			double xl = (line.p.x1+0.5) * (bds[1].getEntry(0)-bds[0].getEntry(0))/resolution + bds[0].getEntry(0);
			double yl = (line.p.y1+0.5) * (bds[1].getEntry(1)-bds[0].getEntry(1))/resolution + bds[0].getEntry(1);
			double zl = (line.p.z1+0.5) * (bds[1].getEntry(2)-bds[0].getEntry(2))/resolution + bds[0].getEntry(2);

			double xh = (line.p.x2+0.5) * (bds[1].getEntry(0)-bds[0].getEntry(0))/resolution + bds[0].getEntry(0);
			double yh = (line.p.y2+0.5) * (bds[1].getEntry(1)-bds[0].getEntry(1))/resolution + bds[0].getEntry(1);
			double zh = (line.p.z2+0.5) * (bds[1].getEntry(2)-bds[0].getEntry(2))/resolution + bds[0].getEntry(2);

			System.out.printf("%d (%6.3f,%6.3f,%6.3f) (%6.3f,%6.3f,%6.3f)%n",
					line.val,xl,yl,zl, xh,yh,zh);
			
			int i = polygons.addVertex(new PdVector(xl,yl,zl));
			int j = polygons.addVertex(new PdVector(xh,yh,zh));
			
			PiVector poly = new PiVector(i,j);
			polygons.addPolygon(poly);
		}
		return polygons;
	}

	@Override
	public void displayGeom(GeomPair p, PgGeometryIf result) {

		GeomStore.copySrcTgt(result, p.getOutput());
		setGeometryInfo(p.getOutput(),p.getInput());
		store.geomChanged(p.getOutput());
	}

	@Override
	public void newActiveInput(String name) {
		if (activePairs.containsKey(name)) {
			showStatus(name + " is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		PgGeometryIf output = store.aquireCurve(getPreferredOutputName(name), this);
		GeomPair p = new GeomPair(input, output);
		activePairs.put(name, p);
		activeInputNames.add(name);
		setDisplayProperties(p.getOutput());
		calcGeom(p);
		store.newPair(this, p);
	}

	@Override
	public void setDefinitionOptions(Definition def) {
	}

	@Override
	public void loadDefinition(Definition newdef) {
	}

	@Override
	public Definition createDefaultDef() {
		return null;
	}

	enum Face { LEFT,RIGHT,FRONT,BACK,TOP,BOTTOM }
	
	static class BoxPair {
//		final Face face1,face2;
		final int x1,y1,z1, x2,y2,z2;
		
		public BoxPair(int x1, int y1, int z1, int x2, int y2, int z2) {
			super();
//			this.face1 = face1;
			this.x1 = x1;
			this.y1 = y1;
			this.z1 = z1;
//			this.face2 = face2;
			this.x2 = x2;
			this.y2 = y2;
			this.z2 = z2;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
//			result = prime * result + ((face1 == null) ? 0 : face1.hashCode());
//			result = prime * result + ((face2 == null) ? 0 : face2.hashCode());
			result = prime * result + x1;
			result = prime * result + x2;
			result = prime * result + y1;
			result = prime * result + y2;
			result = prime * result + z1;
			result = prime * result + z2;
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
			BoxPair other = (BoxPair) obj;
//			if (face1 != other.face1)
//				return false;
//			if (face2 != other.face2)
//				return false;
			if (x1 != other.x1)
				return false;
			if (x2 != other.x2)
				return false;
			if (y1 != other.y1)
				return false;
			if (y2 != other.y2)
				return false;
			if (z1 != other.z1)
				return false;
			if (z2 != other.z2)
				return false;
			return true;
		}
	}
	
	class BoxPairValue {
		BoxPair p;
		Integer val;
		public BoxPairValue(BoxPair p, Integer val) {
			super();
			this.p = p;
			this.val = val;
		}
		
		@Override
		public String toString() {
			return String.format("%d (%d %d %d) (%d %d %d)%n",
					val,p.x1,p.y1,p.z1, p.x2,p.y2,p.z2);

		}
		
	}

	
	class LineCounter {
		double xlow,ylow,zlow, xhigh,yhigh,zhigh;
		CountingMap<BoxPair> linecounts = new CountingMap<>();
		int resolution;
		int fails=0;
		LineCounter(PdVector[] bds,int divisions) {
			xlow = bds[0].getEntry(0);
			ylow = bds[0].getEntry(1);
			zlow = bds[0].getEntry(2);
			xhigh = bds[1].getEntry(0);
			yhigh = bds[1].getEntry(1);
			zhigh = bds[1].getEntry(2);
			resolution = divisions;
		}
		
		void addLine(PdVector A,PdVector B) {

			int count=0;
			PdVector[] ends = new PdVector[6];
			Face[] faces = new Face[6];

			{			
			double xl_lam = (xlow - A.getEntry(0)) / ( B.getEntry(0) - A.getEntry(0));
			double xl_y = (1-xl_lam) * A.getEntry(1) + xl_lam * B.getEntry(1);
			double xl_z = (1-xl_lam) * A.getEntry(2) + xl_lam * B.getEntry(2);
			if(xl_y > ylow && xl_y < yhigh && xl_z > zlow && xl_z < zhigh) {
				faces[count] = Face.LEFT;
				ends[count++] = new PdVector(xlow,xl_y,xl_z);
			}
			}
			{
			double xh_lam = (xhigh - A.getEntry(0)) / ( B.getEntry(0) - A.getEntry(0));
			double xh_y = (1-xh_lam) * A.getEntry(1) + xh_lam * B.getEntry(1);
			double xh_z = (1-xh_lam) * A.getEntry(2) + xh_lam * B.getEntry(2);
			if(xh_y > ylow && xh_y < yhigh && xh_z > zlow && xh_z < zhigh) {
				faces[count] = Face.RIGHT;
				ends[count++] = new PdVector(xhigh,xh_y,xh_z);
			}
			}
			{
			double yl_lam = (ylow - A.getEntry(1)) / ( B.getEntry(1) - A.getEntry(1));
			double yl_x = (1-yl_lam) * A.getEntry(0) + yl_lam * B.getEntry(0);
			double yl_z = (1-yl_lam) * A.getEntry(2) + yl_lam * B.getEntry(2);
			if(yl_x > xlow && yl_x < yhigh && yl_z > zlow && yl_z < zhigh) {
				faces[count] = Face.FRONT;
				ends[count++] = new PdVector(yl_x,ylow,yl_z);
			}
			}
			{
			double yh_lam = (yhigh - A.getEntry(1)) / ( B.getEntry(1) - A.getEntry(1));
			double yh_x = (1-yh_lam) * A.getEntry(0) + yh_lam * B.getEntry(0);
			double yh_z = (1-yh_lam) * A.getEntry(2) + yh_lam * B.getEntry(2);
			if(yh_x > ylow && yh_x < yhigh && yh_z > zlow && yh_z < zhigh) {
				faces[count] = Face.BACK;
				ends[count++] = new PdVector(yh_x,yhigh,yh_z);
			}
			}
			{
			double zl_lam = (zlow - A.getEntry(2)) / ( B.getEntry(2) - A.getEntry(2));
			double zl_x = (1-zl_lam) * A.getEntry(0) + zl_lam * B.getEntry(0);
			double zl_y = (1-zl_lam) * A.getEntry(1) + zl_lam * B.getEntry(1);
			if(zl_y > ylow && zl_y < yhigh && zl_x > xlow && zl_x < xhigh) {
				faces[count] = Face.BOTTOM;
				ends[count++] = new PdVector(zl_x,zl_y,zlow);
			}
			}
			{
			double zh_lam = (zhigh - A.getEntry(2)) / ( B.getEntry(2) - A.getEntry(2));
			double zh_x = (1-zh_lam) * A.getEntry(0) + zh_lam * B.getEntry(0);
			double zh_y = (1-zh_lam) * A.getEntry(1) + zh_lam * B.getEntry(1);
			if(zh_y > ylow && zh_y < yhigh && zh_x > xlow && zh_x < xhigh) {
				faces[count] = Face.TOP;
				ends[count++] = new PdVector(zh_x,zh_y,zhigh);
			}
			}
			if(count!=2) {
//				System.out.println("Wrong count for box intersections "+count);
//				System.out.printf("(%6.3f,%6.3f,%6.3f) (%6.3f,%6.3f,%6.3f)%n",
//						A.getEntry(0),A.getEntry(1),A.getEntry(2),
//						B.getEntry(0),B.getEntry(1),B.getEntry(2));
				++fails;
			}
			if(count>=2) {
				int x1 = (int) (resolution*(ends[0].getEntry(0) - xlow) / (xhigh - xlow));
				int y1 = (int) (resolution*(ends[0].getEntry(1) - ylow) / (yhigh - ylow));
				int z1 = (int) (resolution*(ends[0].getEntry(2) - zlow) / (zhigh - zlow));

				int x2 = (int) (resolution*(ends[1].getEntry(0) - xlow) / (xhigh - xlow));
				int y2 = (int) (resolution*(ends[1].getEntry(1) - ylow) / (yhigh - ylow));
				int z2 = (int) (resolution*(ends[1].getEntry(2) - zlow) / (zhigh - zlow));

				linecounts.increment(new BoxPair(x1,y1,z1,x2,y2,z2));
			}
		}
		
		
		List<BoxPairValue> topHits(int num) {
			List<BoxPairValue> hits = new ArrayList<>();
			for(Entry<BoxPair, Integer> ent:linecounts.entrySet()) {
				if(hits.isEmpty()) {
					hits.add(new BoxPairValue(ent.getKey(),ent.getValue()));
					continue;
				}
				if(hits.size()>=num && ent.getValue() < hits.get(num-1).val) {
					continue;
				}
				boolean inserted=false;
				ListIterator<BoxPairValue> itt = hits.listIterator();
				while(itt.hasNext()) {
					BoxPairValue item = itt.next();
					if(ent.getValue() > item.val) {
						item = itt.previous();
						itt.add(new BoxPairValue(ent.getKey(),ent.getValue()));
						inserted=true;
						break;
					}
				}
				if(!inserted && hits.size()<num) {
					hits.add(hits.size(), new BoxPairValue(ent.getKey(),ent.getValue()));
				}
				while(hits.size()>num) {
					hits.remove(num);
				}
			}
			return hits;
		}
	}
}
