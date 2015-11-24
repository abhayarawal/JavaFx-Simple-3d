/*
 * Point holds the projection coordinates X, and Y
 * 
 */
public class Point {
	private double X, Y, Z;
	double nx, ny, nz;
	double ir, ig, ib;
	
	/* Initialize X, and Y to 0 */
	public Point() { X = 0; Y = 0; Z = 0; }
	
	/* Initialize points X, and Y with the viewport offsets */
	public Point(double[] p, double viewportX, double viewportY) 
	{ 
		X = p[0] + viewportX;
		Y = p[1] + viewportY;
		Z = p[2];
	}
	
	/* Getters and Setters for X, and Y */
	public double X() {
		return X;
	}

	public void setX(double x) {
		X = x;
	}

	public double Y() {
		return Y;
	}

	public void setY(double y) {
		Y = y;
	}	
	
	public double Z() {
		return Z;
	}

	public void setZ(double z) {
		Z = z;
	}	
}
