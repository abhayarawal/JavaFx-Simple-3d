import java.util.ArrayList;

/*
 * Cylinder class creates a new instance of the cylinder primitive based on the radius, height, and segments on radius
 */

public class Cylinder {
	public static Object instance(String id, float r, float h, int s)
	{
		Vertex[] vertices = new Vertex[(s*2)];
		
		// minimum number of segments is 3
		if(s<3) s = 3;
		
		// angle divided into segments
		float x, z, angle = (float) 360 / s;
		int t = 0;
		float deg = 0;
		
		for (int i = 1; i < 3; i++)
			for (int j = 0; j < s; j++)
			{
				// increments/reset angle as necessary
				if(i==2 && j==0)
					deg = angle;
				else
					deg += angle;
								
				
				// find the x, z points from the origin based on radius and angle
				x = (float) (r * Math.cos( Math.toRadians(deg) ));
				z = (float) (r * Math.sin( Math.toRadians(deg) ));
				
				// h is the total height, base starts at the origin
				vertices[t] = (i > 1) ? new Vertex(x,h,z) : new Vertex(x,0,z);
				t++;
			}
		
		
		// create the edges for the vertices
		t = 0;
		Integer[] edges = new Integer[s*3*2];
		
		ArrayList<Integer[]> faces = new ArrayList<Integer[]>();		
		
		for (int i=1; i<=s; i++)
		{
			int tx = i+1;
			if (i == s) tx = 1;
			
			faces.add(new Integer[]{i,tx,tx+s,i+s});
		}
		
		for (int i = 1; i < s; i++)
		{
			edges[t] = edges[t+2] = i;
			edges[t+1] = i+1;
			edges[t+3] = edges[t+4] = i+s;
			edges[t+5] = i+s+1;
			t+=6;
		}
		
		edges[t] = 1;
		edges[t+1] = edges[t+2] = s;
		edges[t+3] = edges[t+4] = s*2;
		edges[t+5] = s+1;
		
		Object o = new Object(id, vertices, edges);
		o.setFace(faces);
		
		return o;
	}
	
	// parameters for the cylinder primitive
	public static String[] getLabels()
	{
		return new String[]{"Radius", "Height", "Segments"};
	}
}
