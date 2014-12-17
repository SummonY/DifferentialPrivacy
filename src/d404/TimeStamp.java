package d404;

import java.math.BigInteger;
import java.util.Random;

public class TimeStamp {
	private BigInteger psquare;
	private int q_length;
	private BigInteger q;
	
	public TimeStamp(BigInteger p, BigInteger bq) {
		q_length = 512;
		psquare = p.pow(2);
		q = bq;
	}
	
	public BigInteger PRF(BigInteger seed) {
		BigInteger rnd = new BigInteger(q_length, new Random(seed.hashCode())).mod(q);
		return rnd;
	}
	
	public BigInteger Hash(BigInteger g, long time) {
		long h1 = BigInteger.valueOf(time).hashCode();
		BigInteger h = g.modPow(new BigInteger(q_length,new Random(h1)),psquare);		
		return h;
	}
	
}
