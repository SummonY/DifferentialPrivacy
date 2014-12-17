package d404;

import java.math.*;
import java.util.*;

public class Differential {

	public static void main(String[] args)  throws Exception {
		// TODO Auto-generated method stub
		final float r = (float)0.2;
		int N = 400;
		int n = (int)(r * N + 2);
		int i, k = 2;
		BigInteger p ,q, h, g;
		
		// product p, q, h, g
		Random rnd = new Random(0);
		q = BigInteger.probablePrime(512, rnd);
		p = q.multiply(BigInteger.valueOf(2L)).add(BigInteger.ONE);
		if (q.isProbablePrime(1)) {
			BigInteger tmp;
			for (k = 2; k <= 30; ++k)
			{
				BigInteger mul = BigInteger.valueOf(k);
				tmp = q.multiply(mul).add(BigInteger.ONE);
				if (tmp.isProbablePrime(1)) {
					p = tmp;
					System.out.println("k = " + k);
					break;
				}
			}
			if (p.subtract(BigInteger.ONE).mod(q) == BigInteger.ZERO && p.isProbablePrime(1))
				System.out.println("q = " + q + "\np = " + p);
		}
		h = new BigInteger(512, rnd).mod(p);
		g = h.modPow(p.multiply(BigInteger.valueOf(k)), p.pow(2));
		System.out.println("g = " + g);
		n = 10;
		N = 50;
		
		
		// Initial identity number、group、auxiliary identity number
		Meter[] me = new Meter[N];
		Aggregator aggr = new Aggregator(N, n, p, q);
		aggr.Private_Num(g, q);
		
		System.out.println("Setup:");
		long start = System.currentTimeMillis();
		for (i = 0; i < N; ++i) {
			me[i] = new Meter(n, p, q, g);
			me[i].Init_Id_Group(i, n);
			me[i].Private_Num();
			me[i].send_gri(i, aggr);
			me[i].Gene_poly(q, n, N);
		}
		//System.out.println("Aggregator send g0 gj to meter");
		for (i = 0; i < N; ++i) {
			aggr.send_g0(me[i]);
			aggr.send_gj(me[i], n, N);
		}
		
		//System.out.println("meter send wimj to aggregator");
		for (i = 0; i < N; ++i) {
			me[i].send_Group_wimj(aggr, n);
		}
		
		//System.out.println("Aggregator send wjmi to meter");
		for (i = 0; i < N; ++i) {
			aggr.send_Group_wjmi(me[i], n, N);
		}
		
		//System.out.println("meter send Wmi to Aggragator");
		for (i = 0; i < N; ++i) {
			me[i].send_Wmi(aggr, n, N);
		}
				
		aggr.calculate_Ws(n, N);
		aggr.interpolate_Poly_k01(n);
		System.out.println("N = " + N + " setup usetime is: " + ((System.currentTimeMillis() - start) / N) + "ms");
	
		BigInteger ke1 = BigInteger.ZERO;
		for  (int l = 0; l < N; ++l) {
			ke1 = ke1.add(me[l].pi);
		}
		System.out.println("ke1 = " + ke1.add(aggr.k01).mod(q));
		
		System.out.println("Phase 2: ");
		for (i = 0; i < N; i++) {
			me[i].calculate_ki2(aggr, n, N);
		}
		aggr.caculate_k02(N, g, q);

		long en_Start = System.currentTimeMillis();
		for (i = 0; i < N; i++) {
			me[i].Encrypt();
		}
		System.out.println("Encrypt usetime is: " + ((System.currentTimeMillis() - en_Start) / N) + "ms");
		
		BigInteger ke2 = BigInteger.ZERO;
		for  (int l = 0; l < N; ++l) {
			ke2 = ke2.add(me[l].ki2);
		}
		System.out.println("ke2 = " + ke2.add(aggr.k02).mod(q));
		
		BigInteger XN = BigInteger.ZERO;
		for (i = 0; i < N; i++) {
			XN = XN.add(me[i].Xi);
			me[i].send_Ci(aggr);
		}
		System.out.println("encrypt xN = " + XN);
		
		long de_Start = System.currentTimeMillis();
		aggr.Decrypt(g, N);
		System.out.println("Decrypt usetime is: " + (System.currentTimeMillis() - de_Start) + "ms");
	}
	
}

