/*
 * Sphere class creates a new instance of the Sphere primitive based on the radius, segments X, and segments Y
 */

import java.util.ArrayList;

public class Sphere {
	
	public static Object instance(String id, float radius, int slices, int stacks)
	{		
		// any given point on the sphere has two angles, phi and theta.
		int vPRow = slices + 1, vPCol = stacks + 1;
		
		// total number of vertices
		int nV = vPRow * vPCol;
		
		Vertex[] vts = new Vertex[nV];
		int j = 0;
		
		float theta = 0.0f, phi = 0.0f;
		float vNgStride = (float) Math.PI / (float) stacks;
		float hNgStride = ((float) Math.PI * 2) / (float) slices;
		
		// use the horizontal stride, vertical stride, phi, and theta to calculate individual points on the sphere
		for (int v=0; v<vPCol; v++)
		{
			theta = ((float) Math.PI / 2.0f) - vNgStride * v;
			for (int h=0; h<vPRow; h++)
			{
				phi = hNgStride * h;
				
				float x = radius * (float) Math.cos(theta) * (float) Math.cos(phi);
				float y = radius * (float) Math.cos(theta) * (float) Math.sin(phi);
				float z = radius * (float) Math.sin(theta);
								
				vts[j] = new Vertex(x, y, z);
				j++;
			}
		}
		
		ArrayList<Integer> edges = new ArrayList<Integer>();
		ArrayList<Integer[]> faces = new ArrayList<Integer[]>();
		
		// calculate the edges for the vertices
		for(int v=0; v<stacks; v++)
		{
			for(int h=0; h<slices; h++)
			{
				int a = (h+v*vPRow)+1;
				int b = ((h+1)+v*vPRow)+1;
				int c = (h+(v+1)*vPRow)+1;
				int d = ((h+1)+(v+1)*vPRow)+1;
				
//				faces.add(new Integer[]{a,c,d});
//				faces.add(new Integer[]{b,d,c});
				
				edges.add(a);
				edges.add(b);
				edges.add(a);
				edges.add(c);
				edges.add(a);
				edges.add(d);
				
				edges.add(b);
				edges.add(c);
				edges.add(b);
				edges.add(d);
				
				edges.add(c);
				edges.add(d);
			}
		}
		
		Integer[] edg = new Integer[edges.size()];
		
		j = 0;
		for(Integer i: edges)
		{
			edg[j] = i;
			j++;
		}
				
		Object o =new Object(id, vts, edg);
		
		System.out.println(faces.size());
//		o.setFace(faces);
		return o;
	}
	
	public static String[] getLabels()
	{
		return new String[]{"Radius", "Segments X", "Segments Y"};
	}
}