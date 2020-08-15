package org.singsurf.singsurf.asurf;

public class Matrix3D {
	final transient double a,b,c, d,e,f, g,h,i;

	public Matrix3D(double a, double b, double c, 
			double d, double e, double f, 
			double g, double h, double i) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
		this.g = g;
		this.h = h;
		this.i = i;
	}
	
	public Matrix3D inverse() {
	    double A = (e*i-f*h), B = -(b*i-c*h), C=(b*f-c*e);
	    double D =-(d*i-f*g), E =  (a*i-c*g), F=-(a*f-c*d);
	    double G = (d*h-e*g), H = -(a*h-b*g), I=(a*e-b*d);
	    
		double det = this.det();
	    return new Matrix3D(A/det,B/det,C/det, D/det,E/det,F/det, G/det,H/det,I/det);
	}
	
	public double det() {
		return a*e*i + b*f*g + c*d*h - c*e*g - b*d*i - a*f*h;
	}
	
	public double trace() {
		return a+e+i;
	}
	
	public boolean equalsMat3D(Matrix3D mat) {
		return 
				(a==mat.a) && (b==mat.b) && (c==mat.c)
			&&	(d==mat.d) && (e==mat.e) && (f==mat.f)
			&&	(g==mat.g) && (h==mat.h) && (i==mat.i);
	}
	
	public Matrix3D mult(Matrix3D mat) {
		double A = a * mat.a + b * mat.d + c * mat.g;
		double B = a * mat.b + b * mat.e + c * mat.h;
		double C = a * mat.c + b * mat.f + c * mat.i;

		double D = d * mat.a + e * mat.d + f * mat.g;
		double E = d * mat.b + e * mat.e + f * mat.h;
		double F = d * mat.c + e * mat.f + f * mat.i;

		double G = g * mat.a + h * mat.d + i * mat.g;
		double H = g * mat.b + h * mat.e + i * mat.h;
		double I = g * mat.c + h * mat.f + i * mat.i;

	    return new Matrix3D(A,B,C, D,E,F, G,H,I);
	}
	
	public Vec3D mult(Vec3D vec) {
		double X = a * vec.x + b * vec.y + c * vec.z;
		double Y = d * vec.x + e * vec.y + f * vec.z;
		double Z = g * vec.x + h * vec.y + i * vec.z;
		return new Vec3D(X,Y,Z);
	}
	
	static Matrix3D id = new Matrix3D(1,0,0, 0,1,0, 0,0,1);
	
	public static Matrix3D identity() {
		return id;
	}

	@Override
	public String toString() {
		return String.format("[[%9.6f %9.6f %9.6f]%n [%9.6f %9.6f %9.6f]%n [%9.6f %9.6f %9.6f]]%n", 
				a,b,c, d,e,f, g,h,i);
	}
	
	
}
