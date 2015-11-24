/*
 * Plane class creates a new instance of the plane primitive based on the length, width, segments length, and segments width
 */

import java.util.ArrayList;

public class Plane {
	public static Object instance(String id, float l, float w, int segL, int segW)
	{		
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		
		float ls = l/(segL+1), ws = w/(segW+1);
		int t = 0;
		
		// find all the vertices and populate the arraylist
		for (int i=0; i<=segW; i++)
		{
			float ls0 = ls;
			for (int j=0; j<=segL; j++)
			{
				vertices.add(new Vertex(i*ws,0,ls0));
				ls0 += ls;
			}
		}
							
		ArrayList<Integer> edges = new ArrayList<Integer>();
		
		t = 0;
		// create the edges for the plane
		for (@SuppressWarnings("unused") Vertex v: vertices)
		{
			t++;
			
			if ((t) % (segL+1) != 0)
			{
				edges.add(t);
				edges.add(t+1);
			}
			
			if (!(t > vertices.size()-segL-1))
			{
				edges.add(t);
				edges.add(t+segL+1);
			}
		}		
		
		return new Object(id, vertices, edges);
	}
	
	// parameters for the box primitive
	public static String[] getLabels()
	{
		return new String[]{"Length", "Width", "Segments L", "Segments W"};
	}
}