/*
 * Vertex hold the X, Y, and Z coordinates
 * 
 * */
public class Vertex {
	private double X, Y, Z, smx, smy, smz;
	double ir, ig, ib;
	
	/* Initializes all coordinates as 0 */
	public Vertex()
	{
		X = Y = Z = 0;
	}
	
	/* Initialized all coordinates to match the parameters */
	public Vertex(double x, double y, double z) 
	{ 
		X = x; 
		Y = y; 
		Z = z;
	}	

	/* Setters and Getters for X, Y, and Z */
	public double X() { return X; }

	public void setX(double x) { X = x; }

	public double Y() { return Y; }

	public void setY(double y) { Y = y; }

	public double Z() { return Z; }

	public void setZ(double z) { Z = z; }
	
	public void setPoints(double x, double y, double z) 
	{ 
		X = x; 
		Y = y; 
		Z = z; 
	}
	
	public double[] points() { return new double[]{X, Y, Z}; }

	public double getSmx() {
		return smx;
	}

	public void setSmx(double smx) {
		this.smx = smx;
	}

	public double getSmy() {
		return smy;
	}

	public void setSmy(double smy) {
		this.smy = smy;
	}

	public double getSmz() {
		return smz;
	}

	public void setSmz(double smz) {
		this.smz = smz;
	}
}
