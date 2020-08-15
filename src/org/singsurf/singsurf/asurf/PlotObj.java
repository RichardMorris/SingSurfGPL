package org.singsurf.singsurf.asurf;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class PlotObj extends PlotAbstract {
	File file;
	File file_v;
	File file_n;
	File file_f;
	File file_c;
	PrintWriter pw_v;
	PrintWriter pw_n;
	PrintWriter pw_f;
	PrintWriter pw_c;
	int vert_count=0;
	int face_count=0;
	int norm_count=0;
	
	public PlotObj(BoxClevA boxclev, PlotMode mode, File file) {
		super(boxclev, mode);
		this.file = file;
		try {
			file_v = File.createTempFile("asurf", "verts");
			file_n = File.createTempFile("asurf", "norms");
			file_f = File.createTempFile("asurf", "faces");
//			file_c = File.createTempFile("asurf", "cols");
			pw_v = new PrintWriter(file_v);
			pw_n = new PrintWriter(file_n);
			pw_f = new PrintWriter(file_f);
//			pw_c = new PrintWriter(file_c);
			
				System.out.println(file_v);
		} catch (IOException e) {
			System.out.println(e);
		}
	}


	@Override
	protected void addFace(int[] face) {
		pw_f.format("f %1$d//%1$d %2$d//%2$d %3$d//%3$d%n", 
				face[0]+1,face[1]+1,face[2]+1);
		++face_count;
	}

	@Override
	protected void addColour(Color calcSolColour) {
//		cols.add(calcSolColour);
	}

	@Override
	protected void addNormal(double[] vec) {
		float[] vert= new float[] {(float) vec[0],(float) vec[1],(float) vec[2]};
		pw_n.format("vn %9.6f %9.6f %9.6f%n",vert[0],vert[1],vert[2]);
		++norm_count;
	}

	@Override
	protected void addVertex(double[] vec) {
		float[] vert= new float[] {(float) vec[0],(float) vec[1],(float) vec[2]};
		pw_v.format("v %9.6f %9.6f %9.6f%n",vert[0],vert[1],vert[2]);
		++vert_count;
	}

	@Override
	protected void addPoint(double[] vec) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addLineVertex(double[] vec) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addLineEdge(int indexA, int indexB) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setLineColour(Color lineColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fini() {
		pw_v.close();
		pw_n.close();
		pw_f.close();
		
		
		try(PrintWriter output = new PrintWriter(file)) {
			output.println("# Created by SingSurf http://singsurf.org/");

			String descript = boxclev.description;
			output.println("# "+descript.replace("\n", "\n# "));

			String poly = boxclev.getPolyCoeffientsString(boxclev.AA).toString();
			String rep = poly.replace("\n", "\n# ");
			output.println("# "+rep);
			output.println("# "+boxclev.globalRegion);
			output.println("# "+boxclev.getResolutionString());
			output.println("# "+boxclev.getOptionsString().replace("\n", "\n# "));

			output.println("# Number of Vertices: "+ boxclev.getNumVerts());
			output.println("# Number of Edges: "+ boxclev.getNumEdges());
			output.println("# Number of Faces: "+ boxclev.getNumFaces());
			output.println("# Euler charateristic: "+ boxclev.getEuler());
			output.println();

			output.println("# Vertex List");
			output.println("# Number of Vertexs ="+vert_count); 

			try(BufferedReader in_v
			   = new BufferedReader(new FileReader(file_v)) ) {
			in_v.lines().forEach(line -> output.println(line));
			}
			
//			for(float[] vert: verts) {
//				output.format("v %9.6f %9.6f %9.6f%n",vert[0],vert[1],vert[2]);
//			}

			output.println("# Vertex Normal List");
			output.println("# Number of Vertex Normals = "+norm_count);

			try(BufferedReader in_n
					   = new BufferedReader(new FileReader(file_n)) ) {
				in_n.lines().forEach(line -> output.println(line));
			}
			
//			for(float[] vert: norms) {
//				output.format("vn %9.6f %9.6f %9.6f%n",vert[0],vert[1],vert[2]);
//			}			
//			if(boxclev.colortype>0) {
//
//				float[] fcols = new float[3];
//				for(Color c:cols) {
//					c.getColorComponents(fcols);
//					output.format("vt %6.3f %6.3f %6.3f%n",fcols[0],fcols[1],fcols[2]);				
//				}
//			}

			output.println("# Face List");
			output.println("# Number of Faces = "+face_count);

			try(BufferedReader in_f
					   = new BufferedReader(new FileReader(file_f)) ) {
				in_f.lines().forEach(line -> output.println(line));
			}

//			for(int[] face: faces) {
//				output.format("f %1$d//%1$d %2$d//%2$d %3$d//%3$d%n", 
//						face[0]+1,face[1]+1,face[2]+1);
//			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}


	@Override
	public void init(Bern3DContext bern3dContext) {
		super.init(bern3dContext);
		if(boxclev.triangulate!=1) {
			throw new IllegalStateException("Triangulation must be on");
		}
	}

}
