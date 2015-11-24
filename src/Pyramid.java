import java.util.ArrayList;

/*
 * Pyramid class creates a new instance of the pyramid primitive based on the length, width, and height
 */

public class Pyramid {
	public static Object instance(String id, float l, float w, float h)
	{
		Vertex[] vertices = new Vertex[5];
			
		float x, z;
		
		// define vertics based on quadrants
		for (int j = 0; j < 4; j++)
		{
			x = l/2;
			z = w/2;
			
			if(j==1) x*=-1;
			if(j==2) { x*=-1; z*=-1; }
			if(j==3) { z*=-1; }
				
			vertices[j] = new Vertex(x,0,z);
		}
		
		// the the top vertex
		vertices[4] = new Vertex(0,h,0);
		
		ArrayList<Integer[]> faces = new ArrayList<Integer[]>();
		faces.add(new Integer[]{4,3,2,1});
		faces.add(new Integer[]{1,5,4});
		faces.add(new Integer[]{5,3,4});
		faces.add(new Integer[]{2,3,5});
		faces.add(new Integer[]{1,2,5});
		
		// edges created manually as they do not change at all
		Integer[] edges = {1,2,2,3,3,4,1,4,1,5,5,4,2,5,3,5};
		
		Object o = new Object(id, vertices, edges);
		o.setFace(faces);
		
		return o;
	}
	
	// parameters for the box primitive
	public static String[] getLabels()
	{
		return new String[]{"Length", "Width", "Height"};
	}
}
