public class Edge {
	double yMax, yMin, dx, x, b, ctf, ctr;
	double[] Ia, Ib;
	double Ya, Yb;
	
	double N[];
	
	int ctc = 0;
	
	Edge(
			double yMax, double yMin, 
			double dx, double x, 
			double ctf, double ctr, 
			double[] ia, double[] ib,
			double ya, double yb,
			double[] N
		)
	{
		this.yMax = yMax;
		this.yMin = yMin;
		this.dx = dx;
		this.x = x;
		
		this.ctf = ctf;
		this.ctr = ctr;
		
		Ia = ia;
		Ib = ib;
				
		Ya = ya;
		Yb = yb;
		
		this.N = N;
	}
	
	double CTF()
	{
		if (ctc != 0)
			ctr += ctf;
		
		ctc += 1;
		return ctr;
	}
}
 