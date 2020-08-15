package org.singsurf.singsurf.asurf;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.text.NumberFormat;

public class PlotVrml extends PlotAbstract {
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
	int col_count=0;
	NumberFormat fmt;

	public PlotVrml(BoxClevA boxclev, PlotMode mode, File file) {
		super(boxclev, mode);
		this.file = file;
		fmt = NumberFormat.getNumberInstance();
		fmt.setMaximumFractionDigits(6);
	}	
	
	@Override
	public void init(Bern3DContext bern3dContext) {
		super.init(bern3dContext);
		try {
			file_v = File.createTempFile("asurf", ".verts");
			file_n = File.createTempFile("asurf", ".norms");
			file_f = File.createTempFile("asurf", ".faces");
			pw_v = new PrintWriter(file_v);
			pw_n = new PrintWriter(file_n);
			pw_f = new PrintWriter(file_f);
				file_c = File.createTempFile("asurf", ".cols");
				pw_c = new PrintWriter(file_c);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}


	@Override
	protected void addFace(int[] indices) {
		for(int i:indices) {
			pw_f.append(""+i+" ");
		}
		pw_f.println("-1");
		++face_count;
	}

	@Override
	protected void addColour(Color c) {
		if(boxclev.colortype>0) {
		float[] fcols = new float[3];
		c.getColorComponents(fcols);
		StringBuilder sb = new StringBuilder(fmt.format(fcols[0]));
		sb.append(' ');
		sb.append(fmt.format(fcols[1]));
		sb.append(' ');
		sb.append(fmt.format(fcols[2]));
		sb.append('\n');
		
//		String s = String.format("%9.6f %9.6f %9.6f%n",fcols[0],fcols[1],fcols[2]);		
		pw_c.append(sb.toString());
		++col_count;
		}
	}

	@Override
	protected void addNormal(double[] vec) {
//		float[] vert= new float[] {(float) vec[0],(float) vec[1],(float) vec[2]};
//		String s = String.format("%9.6f %9.6f %9.6f%n",vert[0],vert[1],vert[2]);

		StringBuilder sb = new StringBuilder(fmt.format(vec[0]));
		sb.append(' ');
		sb.append(fmt.format(vec[1]));
		sb.append(' ');
		sb.append(fmt.format(vec[2]));
		sb.append('\n');
		
		pw_n.append(sb.toString());
		++norm_count;
	}

	@Override
	protected void addVertex(double[] vec) {
//		float[] vert= new float[] {(float) vec[0],(float) vec[1],(float) vec[2]};
//		String s = String.format("%9.6f %9.6f %9.6f%n",vert[0],vert[1],vert[2]);
//		pw_v.append(s);

		StringBuilder sb = new StringBuilder(fmt.format(vec[0]));
		sb.append(' ');
		sb.append(fmt.format(vec[1]));
		sb.append(' ');
		sb.append(fmt.format(vec[2]));
		sb.append('\n');
		
		pw_v.append(sb.toString());

		++vert_count;
	}

	@Override
	protected void addPoint(double[] vec) {
	}

	@Override
	protected void addLineVertex(double[] vec) {
	}

	@Override
	protected void addLineEdge(int indexA, int indexB) {
	}

	@Override
	protected void setLineColour(Color lineColor) {
	}

	@Override
	public void fini() {
		pw_v.close();
		pw_n.close();
		pw_c.close();
		pw_f.close();

		System.out.println(file_v);
		System.out.println(file_n);
		if(boxclev.colortype>0)
			System.out.println(file_c);
		System.out.println(file_f);
		PrintWriter out=null;
		
		try {
			out = new PrintWriter(file);
			System.out.println("Writing to "+file );

		} catch(FileNotFoundException ex) {
			try {
				final File file2 = new File("asurf.wrl");
				out = new PrintWriter(file2);
				System.out.println("Writing to "+file2 );
			} catch (FileNotFoundException e) {
				try {
					final File f2 = File.createTempFile("asurf", ".wrl");
					out = new PrintWriter(f2 );
					System.out.println("Writing to "+f2 );
				} catch ( IOException e1) {
					System.out.println(e1);
					return;
				}
			}
		}

		try {
			final PrintWriter output = out;
			output.println("#VRML V2.0 utf8");
			output.println("#\n");
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
			output.printf("# Time %,dms memory %dMB %n",boxclev.getComputeTime(),boxclev.getMemoryUsed()/1024);
			output.println();

			output.println("NavigationInfo {");
			output.println("  headlight TRUE");
			output.println("  type      [ \"EXAMINE\", \"WALK\", \"ANY\" ]");
			output.println("}");
			output.println("Group {");
			output.println("  children [");
			output.println("    Shape {");
			output.println("      geometry IndexedFaceSet {");
			output.println("        solid FALSE");
			output.println("          coord Coordinate {");
			output.println("            point [");

			try(LineNumberReader in_v
					= new LineNumberReader(new FileReader(file_v)) ) {
				in_v.lines()
				.map(line -> line + ( in_v.getLineNumber() < vert_count ? "," : "") )
				.forEach(line -> output.println(line));
			}

			output.println();
			output.println("]");
			output.println("}");
			output.println("normalPerVertex TRUE");
			output.println("normal Normal {");
			output.println("vector [");

			try(LineNumberReader in_n
					= new LineNumberReader(new FileReader(file_n)) ) {
				in_n.lines().map(
						line -> line + ( in_n.getLineNumber() < norm_count ? "," : "") )
				.forEach(line -> output.println(line));
			}			
			output.println("]");
			output.println("}");

			if(boxclev.colortype>0) {
				output.println("colorPerVertex TRUE");
				output.println("color Color {");
				output.println("color [");

				try(LineNumberReader in_c
						= new LineNumberReader(new FileReader(file_c)) ) {
					in_c.lines().map(
							line -> line + ( in_c.getLineNumber() < col_count ? "," : "") )
					.forEach(line -> output.println(line));
				}
				output.println("]");
				output.println("}");
			}

			output.println("coordIndex [");

			try(LineNumberReader in_f
					= new LineNumberReader(new FileReader(file_f)) ) {
				in_f.lines().map(
						line -> line + ( in_f.getLineNumber() < face_count ? "," : "") )
				.forEach(line -> output.println(line));
			}
			output.println("]");
			output.println("}");
			output.println("appearance Appearance {");
			output.println("  material Material {");
			output.println("	diffuseColor 0.5882353 0.8627451 1.0");
			output.println("  }");
			output.println("}");
			output.println("}");
			output.println("]");
			output.println("}");
		}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}

	}


}
