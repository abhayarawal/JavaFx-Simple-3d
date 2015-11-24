/*
 * A 3d object consists of vertices and edges
 * All the translation tools are implemented inside the object
 * 
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Object {

	/* ArrayList used instead of arrays as size is unknown */
	private ArrayList<Vertex> vertices; 
	private ArrayList<Integer> edges;
	private ArrayList<Integer[]> faces = new ArrayList<Integer[]>();
	private String id;
	
	boolean smoothGr = true;
	
	/* static enums constants */
	static enum Translate { FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN }
	static enum Rotate { LEFT, RIGHT, UP, DOWN, ZLEFT, ZRIGHT }
	static enum Scale { UP, DOWN }
	
	/* Rotation radian preset */
	private double rad = Math.PI/30;
	
	private boolean ts = true;
		
	/* Constructor method to initialize the vertices and edges */
	public Object(String id, Vertex[] v, Integer[] b)
	{	
		this.id = id;
		vertices = new ArrayList<Vertex>(Arrays.asList(v));
		edges = new ArrayList<Integer>(Arrays.asList(b));
	}
	
	@SuppressWarnings("unchecked")
	public Object(String id, ArrayList<Vertex> vts, ArrayList<Integer> ed)
	{
		this.id = id;
		vertices = (ArrayList<Vertex>) vts.clone();
		edges = (ArrayList<Integer>) ed.clone();
	}

	public String id()
	{
		return id;
	}
	
	
	public void gouroud(Renderer.Illumination ilm)
	{
		double tx, ty, tz, nx, ny, nz;
		boolean inF;
		
			for (Vertex v: vertices)
			{
				tx = 0; ty = 0; tz = 0;
				for (Integer[] face: faces)
				{
					inF = false;
					
					Vertex v1 = vertices.get(face[0]-1);
					Vertex v2 = vertices.get(face[1]-1);
					Vertex v3 = vertices.get(face[2]-1);
					
					nx = v1.Y()*(v2.Z()-v3.Z()) + v2.Y()*(v3.Z()-v1.Z()) + v3.Y()*(v1.Z()-v2.Z());
					ny = v1.Z()*(v2.X()-v3.X()) + v2.Z()*(v3.X()-v1.X()) + v3.Z()*(v1.X()-v2.X());
					nz = v1.X()*(v2.Y()-v3.Y()) + v2.X()*(v3.Y()-v1.Y()) + v3.X()*(v1.Y()-v2.Y());
					
					for (Integer i: face) if (v == vertices.get(i-1)) inF = true;
					
					if (inF)
					{
						tx += nx;
						ty += ny;
						tz += nz;
					}
				}
				
				double magnitude = Math.sqrt(tx*tx + ty*ty + tz*tz);
				v.setSmx(tx/magnitude);
				v.setSmy(ty/magnitude);
				v.setSmz(tz/magnitude);
							
				double[] inty = Util.intensity(v.getSmx(), v.getSmy(), v.getSmz(), ilm);
				
				v.ir = inty[0];  v.ig = inty[1];  v.ib = inty[2];
			}
	}
	
	public void gouroud(Renderer.Illumination ilm, int inx)
	{
		Integer[] face = faces.get(inx);
		double tx, ty, tz, nx, ny, nz;
		tx = 0; ty = 0; tz = 0;
			
		Vertex v1 = vertices.get(face[0]-1);
		Vertex v2 = vertices.get(face[1]-1);
		Vertex v3 = vertices.get(face[2]-1);
				
		nx = v1.Y()*(v2.Z()-v3.Z()) + v2.Y()*(v3.Z()-v1.Z()) + v3.Y()*(v1.Z()-v2.Z());
		ny = v1.Z()*(v2.X()-v3.X()) + v2.Z()*(v3.X()-v1.X()) + v3.Z()*(v1.X()-v2.X());
		nz = v1.X()*(v2.Y()-v3.Y()) + v2.X()*(v3.Y()-v1.Y()) + v3.X()*(v1.Y()-v2.Y());
				
		tx += nx;
		ty += ny;
		tz += nz;
			
		for (Integer i: face)
		{
			Vertex v = vertices.get(i-1);
			double magnitude = Math.sqrt(tx*tx + ty*ty + tz*tz);
			v.setSmx(tx/magnitude);
			v.setSmy(ty/magnitude);
			v.setSmz(tz/magnitude);
							
			double[] inty = Util.intensity(v.getSmx(), v.getSmy(), v.getSmz(), ilm);
				
			v.ir = inty[0];  v.ig = inty[1];  v.ib = inty[2];
		}
	}
	
	/* Getter for vertices */
	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	/* Getter for edges */
	public ArrayList<Integer> getEdges() {
		return edges;
	}
	
	/* Getter for faces */
	@SuppressWarnings("unchecked")
	public ArrayList<Integer[]> getFaces() {
		return (ArrayList<Integer[]>) faces.clone();
	}

	/* Setter for edges */
	public void setEdge(ArrayList<Integer> _edges) {
		edges = _edges;
	}
	
	/* Setter for faces */
	@SuppressWarnings("unchecked")
	public void setFace(ArrayList<Integer[]> _faces) {
		faces = (ArrayList<Integer[]>) _faces.clone();
	}

	/* Setter for vertices */
	public void setVertices(ArrayList<Vertex> _vertices) {
		vertices = _vertices;
	}
	
	/* Translate the vertices to simulate movement */
	public void translate(Translate t) {
		for (Vertex v: vertices)
		{			
			/* Uses enums to check cases */
			if (t == Translate.FORWARD)
				v.setZ(v.Z()-5);
			else if (t == Translate.BACKWARD)
				v.setZ(v.Z()+5);
			else if (t == Translate.LEFT)
				v.setX(v.X()-5);
			else if (t == Translate.RIGHT)
				v.setX(v.X()+5);
			else if (t == Translate.DOWN)
				v.setY(v.Y()-5);
			else if (t == Translate.UP)
				v.setY(v.Y()+5);
		}
		
		ts = true;
	}
	
	// mid point vertex, cached
	Vertex mid;
	
	// calculate the centerpoint of the object
	private void midpoint()
	{
		// only calculate if translation function has occured
		if (ts)
		{
			mid = vertices.get(0);
			double 	minX = mid.X(), 
					maxX = mid.X(), 
					minY = mid.Y(), 
					maxY = mid.Y(), 
					minZ = mid.Z(), 
					maxZ = mid.Z();
			
			Vertex v;
			
			Iterator<Vertex> iterator = vertices.iterator();
			while (iterator.hasNext())
			{
				v = iterator.next();
				if (v.X() > maxX) maxX = v.X();
				if (v.X() < minX) minX = v.X();
				if (v.Y() > maxY) maxY = v.Y();
				if (v.Y() < minY) minY = v.Y();
				if (v.Z() > maxZ) maxZ = v.Z();
				if (v.Z() < minZ) minZ = v.Z();
			}
			
			mid = new Vertex(
						(minX + maxX) / 2,
						(minY + maxY) / 2,
						(minZ + maxZ) / 2
					);
		}
	}
	
	/* Translates the vertices to simulate rotation */
	public void rotate(Rotate r)
	{		
		midpoint();
		
		/* Temporary variables */
		double tmpX, tmpY, tmpZ;
		Iterator<Vertex> iterator = vertices.iterator();
		while (iterator.hasNext())
		{
			Vertex v = iterator.next();
			
			tmpX = v.X()-mid.X(); tmpY = v.Y()-mid.Y(); tmpZ = v.Z()-mid.Z();
			
			/* Uses enums to check cases */
			if (r == Rotate.UP)
			{				
				tmpY = (double) ((v.Y()-mid.Y())*Math.cos(rad)-(v.Z()-mid.Z())*Math.sin(rad));
				tmpZ = (double) ((v.Y()-mid.Y())*Math.sin(rad)+(v.Z()-mid.Z())*Math.cos(rad));
			}
			else if (r == Rotate.DOWN)
			{
				tmpY = (double) ((v.Y()-mid.Y())*Math.cos(-rad)-(v.Z()-mid.Z())*Math.sin(-rad));
				tmpZ = (double) ((v.Y()-mid.Y())*Math.sin(-rad)+(v.Z()-mid.Z())*Math.cos(-rad));
			}
			else if (r == Rotate.LEFT)
			{
				tmpX = (double) ((v.X()-mid.X())*Math.cos(rad)+(v.Z()-mid.Z())*Math.sin(rad));
				tmpZ = (double) (-(v.X()-mid.X())*Math.sin(rad)+(v.Z()-mid.Z())*Math.cos(rad));
			}
			else if (r == Rotate.RIGHT)
			{
				tmpX = (double) ((v.X()-mid.X())*Math.cos(-rad)+(v.Z()-mid.Z())*Math.sin(-rad));
				tmpZ = (double) (-(v.X()-mid.X())*Math.sin(-rad)+(v.Z()-mid.Z())*Math.cos(-rad));
			}
			else if (r == Rotate.ZLEFT)
			{
				tmpX = (double) ((v.X()-mid.X())*Math.cos(rad)-(v.Y()-mid.Y())*Math.sin(rad));
				tmpY = (double) ((v.X()-mid.X())*Math.sin(rad)+(v.Y()-mid.Y())*Math.cos(rad));
			}
			else if (r == Rotate.ZRIGHT)
			{
				tmpX = (double) ((v.X()-mid.X())*Math.cos(-rad)-(v.Y()-mid.Y())*Math.sin(-rad));
				tmpY = (double) ((v.X()-mid.X())*Math.sin(-rad)+(v.Y()-mid.Y())*Math.cos(-rad));
			}
			
			v.setX(tmpX+mid.X());
			v.setY(tmpY+mid.Y());
			v.setZ(tmpZ+mid.Z());
		}
		
		ts = false;
	}
	
	/* Translates the vertices simulate scaling */
	public void scale(Scale s)
	{
		midpoint();
		
		double tmpX, tmpY, tmpZ;
		Iterator<Vertex> iterator = vertices.iterator();
		while (iterator.hasNext())
		{
			Vertex v = iterator.next();
			
			tmpX = v.X()-mid.X(); tmpY = v.Y()-mid.Y(); tmpZ = v.Z()-mid.Z();
			
			/* Uses enums to check cases */
			if (s == Scale.UP)
			{				
				tmpX = (double) (tmpX*1.05);
				tmpY = (double) (tmpY*1.05);
				tmpZ = (double) (tmpZ*1.05);
			}
			else if (s == Scale.DOWN)
			{
				tmpX = (double) (tmpX/1.05);
				tmpY = (double) (tmpY/1.05);
				tmpZ = (double) (tmpZ/1.05);
			}
			
			v.setX(tmpX+mid.X());
			v.setY(tmpY+mid.Y());
			v.setZ(tmpZ+mid.Z());
		}
		
		ts = false;
	}
}
