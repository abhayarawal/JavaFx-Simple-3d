/*
 * The Renderer draws the 3d model onto the viewport.
 * The private KeyDispatcher dispatches key events.
 * 
 */

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class Renderer {
	
	static enum Illumination { AMBIENT, DIFFUSE, SPECULAR }
	static enum Shading { FACETED, GOUROUD, PHONG }

	/* FOV (Field of View) */
	private int viewportX, viewportY, FOV = 1000;	
	private int vertexC = 0;
	Object gridObj;
	
	private boolean wire = false;
	private boolean culling = true;
	private boolean face = true;
	
	int clR = 255, clG = 255, clB = 255;
		
	Hashtable<String, Float> z_buffer;
	
	Color[][] fbuffer;
	double[][] dbuffer;
	
	Illumination ilm = Illumination.SPECULAR;
	Shading shading = Shading.FACETED;
	
	int ri = -1;
	
	Color[] clrgroups = {Color.web("#4e3164"), Color.web("#512c51"), Color.web("#71305d"), Color.web("#86385d")};
							
	public Renderer(int vx, int vy)
	{
		/* Divide the viewport in half to render the axes */
		viewportX = (int) vx/2; 
		viewportY = (int) vy/2;
		
		gridObj = Plane.instance("Grid", 1200, 1200, 10, 10);
		for (int i=0; i<(109); i++)
			gridObj.translate(Object.Translate.LEFT);
		for (int i=0; i<32; i++)
		{
			gridObj.translate(Object.Translate.DOWN);
			gridObj.translate(Object.Translate.FORWARD);
		}
	}
	
	// set the viewport for resize, not used on this version
	public void setXY(int vx, int vy)
	{
		viewportX = (int) vx/2; 
		viewportY = (int) vy/2;
	}
	
	
	/* Takes in a 3d vertex and returns an array with projection coordinates */
	public double[] projection(Vertex v)
	{
		double x = FOV*v.X()  / (FOV+v.Z());
		double y = FOV*-v.Y() / (FOV+v.Z());
		double z = v.Z()  / (FOV+v.Z());
		return new double[]{x,y,z};
	}
	
	// get the vertices count
	public int getVC()
	{
		return vertexC;
	}
	
	public void setCulling(boolean cull)
	{
		culling = cull;
	}
	
	public boolean getCulling()
	{
		return culling;
	}
	
	public void setWire(boolean wire)
	{
		this.wire = wire;
	}
	
	public boolean getWire()
	{
		return wire;
	}
	
	public void setFace(boolean face)
	{
		this.face = face;
	}
	
	public boolean getFace()
	{
		return face;
	}
	
	/* Method that draws the model on the viewport */
	public void render(Canvas canvas, ArrayList<Object> objects, int selected, boolean grid)
	{		
		/* Draw the x and y axes */
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		gc.setFill(
				new LinearGradient(0, 1, 0, 0, true,
                        CycleMethod.REFLECT,
                        new Stop(0.0, Color.web("#484848")),
                        new Stop(1.0, Color.web("#141414")))
				);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// draw the grid if necessary
		if (grid)
		{
			gc.setStroke(Color.web("#59523e"));
			renderObject(gridObj, gc);
		}		
		
		z_buffer = new Hashtable<String, Float>();
		z_buffer.clear();
								
		/* Call the renderObject with the object to render and graphics g as arguments */
		renderObjects(objects, canvas, selected);
	}
	
	int Cr, Cg, Cb;
	
	/* renderObject draws the model onto the viewport */
	public void renderObject(Object object, GraphicsContext gc)
	{
		if (object.smoothGr)
			object.gouroud(ilm);
				
		/* Temporary variables */
		int tmpX, tmpY, x = 0, fsc = 0;
		double[] isy = null;
				
		ArrayList<Integer[]> faces = object.getFaces();
				
		if (faces.size() > 0)
		{
			Iterator<Integer[]> iterator = faces.iterator();
			while (iterator.hasNext())
			{
				if (!object.smoothGr)
					object.gouroud(ilm, fsc);
				
				double A, B, C, D;
				Integer[] fc = iterator.next();
				Vertex v1 = object.getVertices().get(fc[0]-1);
				Vertex v2 = object.getVertices().get(fc[1]-1);
				Vertex v3 = object.getVertices().get(fc[2]-1);
				
				A = v1.Y()*(v2.Z()-v3.Z()) + v2.Y()*(v3.Z()-v1.Z()) + v3.Y()*(v1.Z()-v2.Z());
				B = v1.Z()*(v2.X()-v3.X()) + v2.Z()*(v3.X()-v1.X()) + v3.Z()*(v1.X()-v2.X());
				C = v1.X()*(v2.Y()-v3.Y()) + v2.X()*(v3.Y()-v1.Y()) + v3.X()*(v1.Y()-v2.Y());
				D = v1.X()*(v2.Y()*v3.Z()-v3.Y()*v2.Z()) 
						 + v2.X()*(v3.Y()*v1.Z()-v1.Y()*v3.Z()) 
						 + v3.X()*(v1.Y()*v2.Z()-v2.Y()*v1.Z());
				
				if (shading == Shading.FACETED)
					isy = Util.intensity(A, B, C, ilm);
				
				double eq = C * (-FOV) - D;
												
				if (eq <= 0 || !culling)
				{					
					Point[] points = new Point[fc.length];
					
					x = 0;			
					for (Integer i: fc)
					{
						Vertex tmpVtx = object.getVertices().get(i-1);
						points[x] = new Point(projection(tmpVtx), viewportX, viewportY);
						points[x].ir = tmpVtx.ir;
						points[x].ig = tmpVtx.ig;
						points[x].ib = tmpVtx.ib;
						
						points[x].nx = tmpVtx.getSmx();
						points[x].ny = tmpVtx.getSmy();
						points[x].nz = tmpVtx.getSmz();
						
						x++;	
					}					
					
					double yMax, yMin, dx, xi, ctf, ctr, ya, yb;
					double[] ia, ib, N;
					List<Edge> edges = new ArrayList<>();
					
					// table construction
					for (int i=0, l=fc.length; i<l; i++)
					{
						if (i == l-1) x = 0;
						else x = i+1;
						
						if (points[i].Y() < points[x].Y())
						{
							xi = points[i].X();
							yMin = points[i].Y();
							yMax = points[x].Y();
							
							ia = new double[]{points[i].ir, points[i].ig, points[i].ib};
							ib = new double[]{points[x].ir, points[x].ig, points[x].ib};
							ya = points[i].Y();
							yb = points[x].Y();
							
							N = new double[]{points[i].nx, points[i].ny, points[i].nz, points[x].nx, points[x].ny, points[x].nz};
							
							dx = (
									(points[x].X() - points[i].X())/
							        (points[x].Y() - points[i].Y())
							     );
							
							ctf = (
									(points[x].Z() - points[i].Z())/
									(points[x].Y() - points[i].Y())
								  );
							ctr = (points[i].Z() + ctf);
						}
						else
						{
							xi = points[x].X();
							yMin = points[x].Y();
							yMax = points[i].Y();
								
							dx = (
									(points[i].X() - points[x].X())/
								    (points[i].Y() - points[x].Y())
								  );
															
							ctf = (
									(points[i].Z() - points[x].Z())/
									(points[i].Y() - points[x].Y())
								  );
							
							N = new double[]{points[x].nx, points[x].ny, points[x].nz, points[i].nx, points[i].ny, points[i].nz};
							
							ctr = (points[x].Z() + ctf);
							
							ia = new double[]{points[x].ir, points[x].ig, points[x].ib};
							ib = new double[]{points[i].ir, points[i].ig, points[i].ib};
							ya = points[x].Y();
							yb = points[i].Y();
						}
						
						edges.add(new Edge(yMax, yMin, dx, xi, ctf, ctr, ia, ib, ya, yb, N));
					}
																				
					// remove all horizontal edges
					edges.removeIf(e -> e.dx == Double.POSITIVE_INFINITY || e.dx == Double.NEGATIVE_INFINITY);
					
					// sort the main stack
					edges.sort((e1, e2) -> Double.compare(e1.x, e2.x));
					edges.sort((e1, e2) -> Double.compare(e1.yMin, e2.yMin));
					
					if (edges.size() != 0)
					{
						vertexC += (edges.size()*2);
						// (0, scanline)
						final double scanline = edges.get(0).yMin;
						
						// temporary active stack
						List<Edge> _active;
						
						// the active stack
						List<Edge> active = new ArrayList<>();
						
						// add edges to active stack
						active = edges.stream().filter(e -> (int) e.yMin == (int) scanline).collect(Collectors.toList());
						
						// remove edges from main stack
						edges.removeIf(e -> e.yMin == scanline);
						
						// sort the active stack
						active.sort((e1, e2) -> Double.compare(e1.x, e2.x));
												
						// the initial scanline
						double y = scanline;
						
						// loop from scanline to end of canvas height
						for (int h=(int) gc.getCanvas().getHeight(); y < h; y++)
						{							
							// if active stack is not empty
							if (active.size() != 0)
							{	
								// offset for the pair size
								int offset = 0;
								if (active.size() % 2 != 0) offset = 1;
								
								// loop through the pairs
								for (int lp=0; lp < active.size()-offset; lp+=2)
								{
									// temporary variables
									double x3, ztmp = 0, ztmp2 = 0, Ip1r = 0, Ip2r = 0, 
																	Ip1g = 0, Ip2g = 0, 
																	Ip1b = 0, Ip2b = 0,
																	Ipr, Ipg, Ipb,
																	s, t, Up,
																	Nax = 0, Nay = 0, Naz = 0, Nbx = 0, Nby = 0, Nbz = 0, Nsx = 0, Nsy = 0, Nsz = 0;
									
									double x1 = x3 = active.get(lp).x, x2 = active.get(lp+1).x;
									
									if (x1 < 0) x1 = x3 = 0;									
									if (x2 > gc.getCanvas().getWidth()) x2 = gc.getCanvas().getWidth()-1;
									
									// loop through x of the edge pair
									for (; x1<x2; x1++)
									{		
											// if it's the first point
											if (x1 == x3)
											{												
												if (shading == Shading.GOUROUD)
												{
													s = (y-active.get(lp).Ya) / (active.get(lp).Ya - active.get(lp).Yb);
													Ip1r = s * active.get(lp).Ia[0] + (1-s)*active.get(lp).Ib[0];
													Ip1g = s * active.get(lp).Ia[1] + (1-s)*active.get(lp).Ib[1]; 
													Ip1b = s * active.get(lp).Ia[2] + (1-s)*active.get(lp).Ib[2]; 
													
													t = (y-active.get(lp+1).Ya) / (active.get(lp+1).Ya - active.get(lp+1).Yb);
													Ip2r = t * active.get(lp+1).Ia[0] + (1-t)*active.get(lp+1).Ib[0];
													Ip2g = t * active.get(lp+1).Ia[1] + (1-t)*active.get(lp+1).Ib[1];
													Ip2b = t * active.get(lp+1).Ia[2] + (1-t)*active.get(lp+1).Ib[2];
												}
												else if (shading == Shading.PHONG)
												{
													Nax = (1/ (active.get(lp).Ya - active.get(lp).Yb)) * (active.get(lp).N[0] * (y - active.get(lp).Yb) + active.get(lp).N[3] * (active.get(lp).Ya - y));
													Nay = (1/ (active.get(lp).Ya - active.get(lp).Yb)) * (active.get(lp).N[1] * (y - active.get(lp).Yb) + active.get(lp).N[4] * (active.get(lp).Ya - y));
													Naz = (1/ (active.get(lp).Ya - active.get(lp).Yb)) * (active.get(lp).N[2] * (y - active.get(lp).Yb) + active.get(lp).N[5] * (active.get(lp).Ya - y));
													
													Nbx = (1/ (active.get(lp+1).Ya - active.get(lp+1).Yb)) * (active.get(lp+1).N[0] * (y - active.get(lp+1).Yb) + active.get(lp+1).N[3] * (active.get(lp+1).Ya - y));
													Nby = (1/ (active.get(lp+1).Ya - active.get(lp+1).Yb)) * (active.get(lp+1).N[1] * (y - active.get(lp+1).Yb) + active.get(lp+1).N[4] * (active.get(lp+1).Ya - y));
													Nbz = (1/ (active.get(lp+1).Ya - active.get(lp+1).Yb)) * (active.get(lp+1).N[2] * (y - active.get(lp+1).Yb) + active.get(lp+1).N[5] * (active.get(lp+1).Ya - y));
												}
												
												// z value for Za
												ztmp = active.get(lp).CTF();
												
												// render if wireframe is turned on
												if (dbuffer[(int)x1][(int)y] > ztmp)
												{
													// edges are drawn with black color
													dbuffer[(int)x1][(int)y] = ztmp;
													if (wire)
														fbuffer[(int)x1][(int)y] = Color.web("#f7efe3");
													else
														if (shading == Shading.FACETED)
														{
															rgb(isy[0], isy[1], isy[2]);
															fbuffer[(int)x1][(int)y] = Color.rgb(Cr, Cg, Cb);
														}
														else if (shading == Shading.GOUROUD)
														{
															Up = (x2 - x1)/(x2 - x3);
															Ipr = Up * Ip1r + (1-Up) * Ip2r;
															Ipg = Up * Ip1g + (1-Up) * Ip2g;
															Ipb = Up * Ip1b + (1-Up) * Ip2b;	
															rgb(Ipr, Ipg, Ipb);
															fbuffer[(int)x1][(int)y] = Color.rgb(Cr, Cg, Cb);
														}
														else if (shading == Shading.PHONG)
														{
															double[] inty = Util.intensity(Nax, Nay, Naz, ilm);
															rgb(inty[0], inty[1], inty[2]);
															fbuffer[(int)x1][(int)y] = Color.rgb(Cr, Cg, Cb);
														}
												}
												
												// the second Zb
												ztmp2 = active.get(lp+1).CTF();
												
												// render if wireframe is on				
												if (dbuffer[(int)x2][(int)y] > ztmp2)
												{
													// edges are drawn with black color
													dbuffer[(int)x2][(int)y] = ztmp2;
													if (wire)
														fbuffer[(int)x2][(int)y] = Color.web("#f7efe3");
													else
														if (shading == Shading.FACETED)
														{
															rgb(isy[0], isy[1], isy[2]);
															fbuffer[(int)x2][(int)y] = Color.rgb(Cr, Cg, Cb);
														}
														else if (shading == Shading.GOUROUD)
														{
															Up = (x2 - x2)/(x2 - x3);
															Ipr = Up * Ip1r + (1-Up) * Ip2r;
															Ipg = Up * Ip1g + (1-Up) * Ip2g;
															Ipb = Up * Ip1b + (1-Up) * Ip2b;
															rgb(Ipr, Ipg, Ipb);
															fbuffer[(int)x2][(int)y] = Color.rgb(Cr, Cg, Cb);
														}
														else if (shading == Shading.PHONG)
														{
															double[] inty = Util.intensity(Nbx, Nby, Nbz, ilm);
															rgb(inty[0], inty[1], inty[2]);
															fbuffer[(int)x2][(int)y] = Color.rgb(Cr, Cg, Cb);
														}
												}												
												
												// initialize the z first and the constant
												double ztmp3 = ztmp;
												ztmp = (ztmp2 - ztmp) / (x2 - x1);
												ztmp2 = ztmp3 + ztmp;
											}
											
											// if it's not the first value
											else
											{
												// update the z value for inside the polygon
												if ((int) x1 != ((int) x3+1))
													ztmp2 += ztmp;
																										
												// if face is turned on
												if (face)											
													if (dbuffer[(int)x1][(int)y] > ztmp2)
													{
														dbuffer[(int)x1][(int)y] = ztmp2;
														
														if (shading == Shading.FACETED)
														{
															rgb(isy[0], isy[1], isy[2]);
															fbuffer[(int)x1][(int)y] = Color.rgb(Cr, Cg, Cb);
														}
														else if (shading == Shading.GOUROUD)
														{
															Up = (x2 - x1)/(x2 - x3);
															Ipr = Up * Ip1r + (1-Up) * Ip2r;
															Ipg = Up * Ip1g + (1-Up) * Ip2g;
															Ipb = Up * Ip1b + (1-Up) * Ip2b;
															rgb(Ipr, Ipg, Ipb);
															fbuffer[(int)x1][(int)y] = Color.rgb(Cr, Cg, Cb);
														}
														else if (shading == Shading.PHONG)
														{
															Nsx = (1/ (x2 - x3)) * (Nax * (x2 - x1) + Nbx * (x1 - x3));
															Nsy = (1/ (x2 - x3)) * (Nay * (x2 - x1) + Nby * (x1 - x3));
															Nsz = (1/ (x2 - x3)) * (Naz * (x2 - x1) + Nbz * (x1 - x3));
															double[] inty = Util.intensity(Nsx, Nsy, Nsz, ilm);
															rgb(inty[0], inty[1], inty[2]);
															fbuffer[(int)x1][(int)y] = Color.rgb(Cr, Cg, Cb);
														}
													}
											}
									}						
								}	
									
								
								// temporary scanline
								final double sc = y+1;
								
								// remove done active edges
								active.removeIf(e -> (int) e.yMax == (int) sc || (int) e.yMax == (int) (sc-1));
								
								// update the x value
								for (Edge e: active) { e.x += e.dx; }
								
								// temporary holder
								_active = new ArrayList<>();
								
								// add edges to _active stack
								_active = edges.stream().filter(e -> (int) e.yMin == (int) sc).collect(Collectors.toList());
								edges.removeIf(e -> (int) e.yMin == (int) sc);
								
								// move edges from _active to active stack
								for (Edge e: _active) {	active.add(e); }								
								
								// sort the active stack
								active.sort((e1, e2) -> Double.compare(e1.x, e2.x));
								
								// clear the temporary holder
								_active.clear();
								
								// edge case
								if (active.size() == 1 && edges.size() == 0)
									active.clear();
							} else { break; }
						}
						
						// clear the active stack
						active.clear();
						
						// clear the main stack
						edges.clear();						
					}
				}
				
				fsc += 1;
			}
		}
		else
		{
			/* Create an array of projection points */
			Point[] points = new Point[object.getVertices().size()];
			Iterator<Vertex> iterator = object.getVertices().iterator();
			while (iterator.hasNext())
			{
				/* Convert vertex into a 2d point and append to the array */
				points[x] = new Point(projection(iterator.next()), viewportX, viewportY);
				x++;
			}
			
			x = 0;
			/* Iterate through the object edges and draw the lines */
			Iterator<Integer> eiterator = object.getEdges().iterator();
			while (eiterator.hasNext())
			{
				tmpX = eiterator.next() - 1;
				tmpY = eiterator.next() - 1;
				
				vertexC++;
				
				/* Draw the edge of the model */
				gc.strokeLine((int) points[tmpX].X(), (int) points[tmpX].Y(), (int) points[tmpY].X(), (int) points[tmpY].Y());
			}
		}
		
		faces.clear();
	}
	
	public void rgb(double r, double g, double b)
	{
		Cr = (int) (r * clR);
		Cg = (int) (g * clG);
		Cb = (int) (b * clB);
		
		if (Cr > 255) Cr = 255;
		if (Cg > 255) Cg = 255;
		if (Cb > 255) Cb = 255;
		
		if (Cr < 0) Cr = 0;
		if (Cg < 0) Cg = 0;
		if (Cb < 0) Cb = 0;
	}
	
	/* renderobjects loops through the objects arraylist and calls renderobject on each object */
	public void renderObjects(ArrayList<Object> objects, Canvas canvas, int selected)
	{
		GraphicsContext gc = canvas.getGraphicsContext2D();		
		
		// reset the vertices counter
		vertexC = 0;
		
		gc.setLineWidth(1);
		
		dbuffer = new double[(int) gc.getCanvas().getWidth()][(int) gc.getCanvas().getHeight()];
		fbuffer = new Color[(int) gc.getCanvas().getWidth()][(int) gc.getCanvas().getHeight()];
		
		for (int i=0, l=(int) gc.getCanvas().getWidth(); i<l; i++)
		{
			for (int j=0, l2=(int) gc.getCanvas().getHeight(); j<l2; j++)
			{
				dbuffer[i][j] = 9999; 
				fbuffer[i][j] = null;				
			}
		}
		
		int tmp = 0;
		Object object;
		Iterator<Object> oiterator = objects.iterator();
		while (oiterator.hasNext())
		{			
			if (tmp == selected)
				gc.setStroke(Color.WHITE);
			else
				gc.setStroke(Color.GREY);
			
			object = oiterator.next();
			
			if (ri == 4) { ri = 0; }
			else { ri += 1; }
			
			renderObject(object, gc);
			
			tmp++;
		}
		
		
		for (int i=0, l=(int) gc.getCanvas().getWidth(); i<l; i++)
		{
			for (int j=0, l2=(int) gc.getCanvas().getHeight(); j<l2; j++)
			{
				if (fbuffer[i][j] != null)
				{
					gc.setStroke(fbuffer[i][j]);
					gc.strokeLine(i, j, i+1, j);
				}
			}
		}
		
		fbuffer = null;
		dbuffer = null;
	}
}
