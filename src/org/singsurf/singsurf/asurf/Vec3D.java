package org.singsurf.singsurf.asurf;

public final class Vec3D {
	public transient final double x,y,z;

	public Vec3D(double x, double y, double z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3D(org.singsurf.singsurf.asurf.BoxSolver.BoxPos pos) {
		x = pos.x; y = pos.y; z = pos.z;
	}

	@Override
	public String toString() {
		return String.format("[%6.3f %6.3f %6.3f]",x,y,z); 
	}
	
	public Vec3D add(double dx,double dy,double dz) {
		return new Vec3D(x+dx,y+dy,z+dz);
	}

	public Vec3D add(Vec3D v) {
		return new Vec3D(x+v.x, y+v.y, z+v.z);
	}
	
	public boolean in_unit_box() {
		return     x >=0 && x <=1 
				&& y >=0 && y <=1 	
				&& z >=0 && z <=1;
	}
	
	public final double distSq() {
		return x*x+y*y+z*z;
	}
	


	static double normalise(double normA[]) {
		double lenA = Math.sqrt(normA[0] * normA[0] + normA[1] * normA[1] + normA[2] * normA[2]);
		if (Double.isFinite(lenA) && lenA != 0.0) {
			normA[0] /= lenA;
			normA[1] /= lenA;
			normA[2] /= lenA;
			return lenA;
		} else {
			normA[0] = normA[1] =  normA[2] = 0.0;
			return 0.0;
 		}
	}

	static double[] subVec(double normB[], double normA[]) {
		return new double[] { normB[0] - normA[0], normB[1] - normA[1], normB[2] - normA[2] };
	}

	static double[] cross(double u[], double v[]) {
		return new double[] { (u[1] * v[2] - u[2] * v[1]), (u[2] * v[0] - u[0] * v[2]), (u[0] * v[1] - u[1] * v[0]) };
	}

	static double dot(double u[], double v[]) {
		return u[0] * v[0] + u[1] * v[1] + u[2] * v[2];
	}

	static void negate(double u[]) {
		u[0] = -u[0];
		u[1] = -u[1];
		u[2] = -u[2];
	}

}
