public class Util {
	// computes the reflection vector for a given surface normal direction and light direction
	static double[] intensity(double nx, double ny, double nz, Renderer.Illumination ilm)
	{
		double a, rx = 0, ry = 0, rz = 0, tcphi;
		double lx = -1, ly = -1, lz = 1;
		
		// normalize n
		a = Math.sqrt(nx*nx + ny*ny + nz*nz);
		nx /= a; ny /= a; nz /= a;
		
		// normalize l
		a = Math.sqrt(lx*lx + ly*ly + lz*lz);
		lx /= a; ly /= a; lz /= a;
		
		tcphi = 2 * (nx*lx + ny*ly + nz*lz);
		if (tcphi > 0)
		{
			rx = nx - lx/tcphi;
			ry = ny - ly/tcphi;
			rz = nz - lz/tcphi;
		}
		else if (tcphi == 0)
		{
			rx = -lx;
			ry = -ly;
			rz = -lz;
		}
		else if (tcphi < 0)
		{
			rx = -nx + lx/tcphi;
			ry = -ny + ly/tcphi;
			rz = -nz + lz/tcphi;
		}
		
		a = Math.sqrt(rx*rx + ry*ry +rz*rz);
		rx /= a; ry /= a; rz /= a; 
		
		double Ir = 0, Ig = 0, Ib = 0, Iar, Iag, Iab, Ipr, Ipg, Ipb, Kdr, Kdg, Kdb, Ksr, Ksg, Ksb;
		double vx = 0, vy = 0, vz = 1;
		
		Iar = 1; Iag = 1; Iab = 1;
		Ipr = 0.5; Ipg = 0.5; Ipb = 0.5;
		Kdr = 0.8; Kdg = 0.8; Kdb = 0.8;
		Ksr = 0.3; Ksg = 0.3; Ksb = 0.3;
		
		if (ilm == Renderer.Illumination.AMBIENT)
		{
			Ir = Iar * Kdr;
			Ig = Iag * Kdg;
			Ib = Iab * Kdb;
		}
		else if (ilm == Renderer.Illumination.DIFFUSE)
		{
			Ir = Iar * Kdr + Ipr * Kdr * (nx*lx + ny*ly + nz*lz);
			Ig = Iag * Kdg + Ipg * Kdg * (nx*lx + ny*ly + nz*lz);
			Ib = Iab * Kdb + Ipb * Kdb * (nx*lx + ny*ly + nz*lz);
		}
		else if (ilm == Renderer.Illumination.SPECULAR)
		{
			Ir = Iar * Kdr + Ipr * Kdr * (nx*lx + ny*ly + nz*lz)
						   + Ipr * Ksr * Math.pow((rx*vx + ry*vy + rz*vz), 6);
			Ig = Iag * Kdg + Ipg * Kdg * (nx*lx + ny*ly + nz*lz)
						   + Ipg * Ksg * Math.pow((rx*vx + ry*vy + rz*vz), 6);
			Ib = Iab * Kdb + Ipb * Kdb * (nx*lx + ny*ly + nz*lz)
						   + Ipb * Ksb * Math.pow((rx*vx + ry*vy + rz*vz), 6);
		}
		
		return new double[]{Ir, Ig, Ib};		
	}	
}
