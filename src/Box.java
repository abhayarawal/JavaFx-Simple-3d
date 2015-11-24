import java.util.ArrayList;

/*
 * Box class creates a new instance of the box primitive based on the length, width, and height
 */

public class Box {
	public static Object instance(String id, float l, float w, float h)
	{
		// 8 vertices altogether on a box
		Vertex[] vertices = new Vertex[8];
			
		float x, z;
		int t = 0;
		
		for (int i = 1; i < 3; i++)
			for (int j = 0; j < 4; j++)
			{
				x = l/2;
				z = w/2;
				
				// change sign of vertices based on the quadrants
				if(j==1) x*=-1;
				if(j==2) { z*=-1; }
				if(j==3) { x*=-1; z*=-1; }
				
				// the h indicates the height of the box. 
				vertices[t] = (i > 1) ? new Vertex(x,h,z) : new Vertex(x,0,z);
				t++;
			}		
		
		ArrayList<Integer[]> faces = new ArrayList<Integer[]>();
		faces.add(new Integer[]{1,3,4,2});
		faces.add(new Integer[]{5,6,8,7});
		faces.add(new Integer[]{1,2,6,5});
		faces.add(new Integer[]{2,4,8,6});
		faces.add(new Integer[]{4,3,7,8});
		faces.add(new Integer[]{3,1,5,7});
		
		// edges created manually as they do not change at all
		Integer[] edges = {1,3,3,4,2,4,1,2,2,6,4,8,3,7,1,5,5,7,7,8,8,6,6,5};
		
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
