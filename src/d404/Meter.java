package d404;

import java.math.BigInteger;
import java.util.Random;


public class Meter {
	private int id;
	private int ai;
	private int group;
	private BigInteger p;
	private BigInteger q;
	private BigInteger g;
	public BigInteger pi;
	public BigInteger ki2;
	private BigInteger[] wi;
	private BigInteger[] wimj;
	private BigInteger[] wjmi;
	private BigInteger Wmi;
	private String[] AES_wimj;
	private BigInteger ri;
	private BigInteger gri;
	private BigInteger si0;
	private BigInteger[] sij;
	private BigInteger Ci;
	private TimeStamp ts;
	private Random rnd;
	private BigInteger p_pow;
	public BigInteger Xi;
	
	public Meter(final int n, BigInteger bp, BigInteger bq, BigInteger bg) {
		this.p = bp;
		this.q = bq;
		this.g = bg;
		this.sij = new BigInteger[n + 1];
		this.wi = new BigInteger[n];
		this.wimj = new BigInteger[n + 1];
		this.wjmi = new BigInteger[n + 1];
		this.AES_wimj = new String[n + 1];
		
		ts = new TimeStamp(p, q);
		rnd = new Random();
		p_pow = p.pow(2);
		Xi = new BigInteger(10, rnd);
	}
	
	public int getId() {
		return this.id;
	}
	
	public int getGroup() {
		return this.group;
	}
	
	// send gri to aggregator
	public void send_gri(int i, Aggregator aggr) {
		aggr.receive_gri(i, this.gri);
	}
	
	// receive g0, compute si0
	public void receive_g0(BigInteger g0) {
		this.si0 = g0.modPow(this.ri, p_pow);
	}
	
	// receive gj(i != j, same group), compute sij
	public void receive_gj(BigInteger gj, final int j) {
		this.sij[j] = gj.modPow(this.ri, p_pow);
	}
	
	// generate private number ri, gri
	public void Private_Num() {
		Random rnd = new Random();
		this.ri = new BigInteger(512, rnd).mod(this.q);
		this.gri = this.g.modPow(ri, p_pow);
	}
	
	// Initial identity group
	public void Init_Id_Group(final int i, final int n) {
		this.id = i + 1;
		this.group = ((i + 1) % n == 0) ? ((i + 1) / n) : ((i + 1) / n) + 1;
		this.ai = ((i + 1) % n == 0) ? n : ((i + 1) % n);
	}
	
	// Generate a random polynomial
	public void Gene_poly(BigInteger q, final int n, final int N) {
		int i, j;
		BigInteger w;
		for (i = 0; i < n; ++i) {
			wi[i] = new BigInteger(512, rnd).mod(q);
		}
		this.pi = wi[0];
		
		if (N % n != 0) {
			if (this.id >= 1 && this.id <= (((N / n) + 1) * n - N)) {
				BigInteger pii = new BigInteger(512, rnd).mod(q);
				this.pi = this.pi.add(pii).mod(q);
				this.wi[0] = this.pi;
			}
		}

		
		for (i = 1; i <= n; ++i) {
			w = BigInteger.ZERO;
			for (j = 0; j < n; ++j) {
				w = w.add(this.wi[j].multiply(BigInteger.valueOf(i).pow(j)));
			}
			this.wimj[i] = w;
		}
	}
	
	// send wmj to Aggragator j != ai
	public void send_Group_wimj(Aggregator aggr, final int n)  throws Exception {
		int j;
		AES aes = new AES();
		
		for (j = 1; j <= n; ++j) {
			//if (j != this.ai) {
				//System.out.println("wimj before encrypt : " + this.wimj[j]);
				this.AES_wimj[j] = aes.Encrypt(this.wimj[j].toString(), this.si0.toString());
				//System.out.println("wimj after encrypt  : " + this.AES_wimj[j]);
				aggr.receive_Group_wimj(this.id, j, this.AES_wimj[j]);
			//}
		}
	}
	
	// receive wmj in the same Group
	public void receive_Group_wjmi(String wjmi, final int j) throws Exception {
		AES aes = new AES();
		String deStr = aes.Decrypt(wjmi, this.si0.toString());
		this.wjmi[j] = new BigInteger(deStr);
		//System.out.println("wimj after decrypt : " + deStr);
	}
	
	public void calculate_Wmi(final int n) {
		this.wjmi[this.ai] = this.wimj[this.ai];
		this.Wmi = BigInteger.ZERO;
		
		for (int j = 1; j <= n; j++) {
			this.Wmi = this.Wmi.add(this.wjmi[j]);
		}
	}
	
	public void send_Wmi(Aggregator aggr, final int n, final int N) throws Exception {
		AES aes = new AES();
		String AES_Wmi = aes.Encrypt(this.Wmi.toString(), this.si0.toString());
		aggr.receive_Wmi(this.id, AES_Wmi);
	}
	
	public void calculate_ki2(Aggregator aggr, final int n, final int N) {
		BigInteger dg, Si;
		this.ki2 = BigInteger.ZERO;
		
		if ((this.id % n == 0) || (this.id == N)) {
			dg = aggr.send_diffSubGroup_gj(this.id, n, N);
			Si = dg.modPow(this.ri, p_pow);
			this.ki2 = ts.PRF(this.sij[this.ai - 1]).subtract(ts.PRF(Si));
		} else if ((this.id - 1) % n == 0) {
			dg = aggr.send_diffSubGroup_gj(this.id, n, N);
			Si = dg.modPow(this.ri, p_pow);
			this.ki2 = ts.PRF(Si).subtract(ts.PRF(this.sij[this.ai + 1]));
		} else {
			this.ki2 = ts.PRF(this.sij[this.ai - 1]).subtract(ts.PRF(this.sij[this.ai + 1]));
		}
	}
	
	public void Encrypt() {
		this.Ci = ((BigInteger.ONE.add(Xi.multiply(p))).multiply(ts.Hash(g, 1).modPow(pi, p_pow))).multiply(g.modPow(ki2, p_pow)).mod(p_pow);
	}
	
	public void send_Ci(Aggregator aggr) {
		aggr.receive_Ci(this.Ci, this.id);
	}
	
}

