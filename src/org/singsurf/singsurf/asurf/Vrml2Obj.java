package org.singsurf.singsurf.asurf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Vrml2Obj {
	File file;
	File file_v;
	File file_n;
	File file_f;
	int vert_count=0;
	int face_count=0;
	int norm_count=0;
	
	public Vrml2Obj(String vrmlfile,String outfile) {
		file = new File(outfile);
		file_v = new File(vrmlfile);
	}

	enum State {HEADER, VERTS, BEFORENORM, NORMS, BEFOREFACE, FACES , FINISHED};

	
	public void scan() {
		State  state = State.HEADER;
		Pattern comment = Pattern.compile("#.*",Pattern.MULTILINE);
		
		try(PrintWriter output = new PrintWriter(file)) {
		try(Scanner scanner = new Scanner(file_v) ) {
			scanner.useDelimiter("[\\s,]+");
			while(scanner.hasNextLine()) {
				
//				String line = skip ?  line : in.readLine();
//				if(line==null) break;
//				skip= false;
				switch(state) {
				case HEADER: {
					if(scanner.hasNext(comment)) {
						String header = scanner.nextLine();
						output.println(header);
					} else if(scanner.hasNextDouble()) {
						state = State.VERTS;
						break;
					} else {
						scanner.nextLine();
					}
					break;
				}
				case VERTS: {
					if(scanner.hasNextDouble()) {
						float x = (float) scanner.nextDouble();
						float y = (float) scanner.nextDouble();
						float z = (float) scanner.nextDouble();
						output.format("v %9.6f %9.6f %9.6f%n",x,y,z);
						++this.vert_count;
					} else {
						state = State.BEFORENORM;	
						System.out.println("Done "+vert_count+" verts");
					}
					break;
				}
				case BEFORENORM:
					if(scanner.hasNextDouble()) {
						state = State.NORMS;
					} else {
						scanner.nextLine();
					}
				break;
				
				case NORMS:
					if(scanner.hasNextDouble()) {
						float x = (float) scanner.nextDouble();
						float y = (float) scanner.nextDouble();
						float z = (float) scanner.nextDouble();
						output.format("vn %9.6f %9.6f %9.6f%n",x,y,z);
						++norm_count;
					} else {
						state = State.BEFOREFACE;						
						System.out.println("Done "+norm_count+" norms");
					}
					break;
					
				case BEFOREFACE:
					if(scanner.hasNext("coordIndex")) {
						scanner.nextLine();
						state = State.FACES;
					} else {
						scanner.nextLine();
					}		
					break;
					
				case FACES:
					if(scanner.hasNextInt()) {
						int a = scanner.nextInt();
						int b = scanner.nextInt();
						int c = scanner.nextInt();
						output.format("f %1$d//%1$d %2$d//%2$d %3$d//%3$d", 
								a+1,b+1,c+1);
						int d=0;
						while(true) {
							d = scanner.nextInt();
							if(d!=-1) {
								output.format(" %1$d//%1$d", 
									d+1);
							} else {
								output.println();
								break;
							}
						}							
						++face_count;
					}
					else {
						System.out.println("Done "+face_count+" faces");
						state = State.FINISHED;
					}
					break;
				
				case FINISHED:
				default:
					scanner.nextLine();
					break;
				
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}	
		}
	
	public static void main(String[] args) {
		Vrml2Obj vo = new Vrml2Obj(args[0],args[1]);
		vo.scan();
	}
}
