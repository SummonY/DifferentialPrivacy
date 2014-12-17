package d404;

import java.math.BigInteger;
import java.util.Random;

public class Aggregator {
	private BigInteger p;
	private BigInteger q;
	private BigInteger r0;
	private BigInteger gr0;
	public BigInteger k01;
	public BigInteger k02;
	private BigInteger[] gi;
	private BigInteger[] s0i;
	private String[][] AES_wimj;
	private String[][] wimj;
	private BigInteger[] Wmi;
	private BigInteger[] Ws;
	private BigInteger[] wp;
	private BigInteger[] Ci;
	private BigInteger C;
	private BigInteger xN;
	private TimeStamp ts;
	private BigInteger p_pow;
	private Random rnd;

	
	public Aggregator(final int N, final int n, BigInteger bp, BigInteger bq) {
		this.p = bp;
		this.q = bq;
		this.gi = new BigInteger[N];
		this.s0i = new BigInteger[N];
		this.Wmi = new BigInteger[N];
		this.Ws = new BigInteger[n + 1];
		this.wp = new BigInteger[n];
		this.Ci = new BigInteger[N];
		this.wimj = new String[N][n + 1];
		this.AES_wimj = new String[N][n + 1];
		
		rnd = new Random();
		ts = new TimeStamp(p, q);
		p_pow = p.pow(2);
	}
	
	public void receive_gri(int i, BigInteger gri) {
		this.gi[i] = gri;
		this.s0i[i] = gri.modPow(r0, this.p.pow(2));
	}
	
	public void send_g0(Meter me) {
		me.receive_g0(this.gr0);
	}
	
	public void send_gj(Meter me, final int n, final int N) {
		int id = me.getId();
		int group = me.getGroup();
		int tmp = (group - 1) * n; 
		int ai;
		
		for (int j = tmp; j < tmp + n; ++j) {
			if (j != id - 1) {
				ai = ((j + 1) % n == 0) ? n : ((j + 1) % n);
				if (j < N) {			
					me.receive_gj(this.gi[j], ai);
				} else {
					me.receive_gj(this.gi[j - N], ai);
				}
			}
		}
	}
	
	public BigInteger send_diffSubGroup_gj(final int id, final int n, final int N) {
		if ((id == N) || (id == 1)) {
			return this.gr0;
		} else if (id % n == 0) {
			return this.gi[id];
		} else if ((id - 1) % n == 0) {
			return this.gi[id - 2];
		}
		return this.gr0;
	}
	
	public void Private_Num(BigInteger g, BigInteger q) {
		this.r0 = new BigInteger(512, rnd).mod(q);
		this.gr0 = g.modPow(r0, this.p.pow(2));
	}
	
	public void receive_Group_wimj(int id, int j, String wimj) throws Exception {
		AES aes = new AES();
		this.wimj[id - 1][j] = aes.Decrypt(wimj, this.s0i[id - 1].toString());
		//System.out.println("wimj after decrypt : " + this.wimj[id - 1][j]);
	}
	
	public void send_Group_wjmi(Meter me, final int n, final int N) throws Exception {
		int id = me.getId();
		int tmp = (me.getGroup() - 1) * n;
		int ai = id - tmp;
		AES aes = new AES();
		
		for (int j = tmp; j < tmp + n; ++j) {
			if (j != id - 1) {
				if (j < N) {
					//System.out.println("wimj before encrypt : " + this.wimj[j][ai]);
					me.receive_Group_wjmi(aes.Encrypt(this.wimj[j][ai], s0i[id - 1].toString()), (j % n) + 1);
				} else {
					//System.out.println("wimj before encrypt : " + this.wimj[j - N][ai]);
					me.receive_Group_wjmi(aes.Encrypt(this.wimj[j - N][ai], s0i[id - 1].toString()), (j % n) + 1);
				}
			}
		}
		me.calculate_Wmi(n);
	}
	
	public void receive_Wmi(int id, String Wmi) throws Exception {
		AES aes = new AES();
		this.Wmi[id - 1] = new BigInteger(aes.Decrypt(Wmi, this.s0i[id - 1].toString()));
		//System.out.println("after decrypt = " + this.Wmi[id - 1]);
	}
	
	public void calculate_Ws(final int n, final int N) {
		int i, j;
		
		int sumGroup = (N % n == 0) ? (N / n) : (N / n) + 1;
		
		for (i = 1; i <= n; i++) {
			this.Ws[i] = BigInteger.ZERO;
			for (j = i; j <= sumGroup * n; j += n) {
				if (j <= N) {
					this.Ws[i] = this.Ws[i].add(this.Wmi[j - 1]);
				} else {
					this.Ws[i] = this.Ws[i].add(this.Wmi[j - N - 1]);
				}
			}
		}
	}
	
	public void interpolate_Poly_k01(final int n) {
		BigInteger fz, fm;
		int i, j;
		
		this.k01 = BigInteger.ZERO;
		//this.k01 = q;
		for (i = 1; i <= n; i++) {
			fz = BigInteger.ONE;
			fm = BigInteger.ONE;
			for (j = 1; j <= n; j++) {
				if (j != i) {
					fz = fz.multiply(BigInteger.valueOf(0 - j));
					fm = fm.multiply(BigInteger.valueOf(i - j));
				}
			}
			this.k01 = this.k01.subtract(this.Ws[i].multiply(fz).divide(fm)).mod(q);
		}
		System.out.println("k01 = " + this.k01);
	}
	
	public void caculate_k02(final int N, BigInteger g, BigInteger q) {
		this.k02 = ts.PRF(this.s0i[N - 1]).subtract(ts.PRF(s0i[0]));
	}
	
	public void receive_Ci(BigInteger bCi, final int id) {
		this.Ci[id - 1] = bCi;
	}
	
	public void Decrypt(BigInteger g, final int N) {
		this.C = (ts.Hash(g, 1).modPow(k01, p_pow)).multiply(g.modPow(k02, p_pow)).mod(p_pow);

		for (int i = 0; i < N; i++) {
			this.C = this.C.multiply(this.Ci[i]).mod(p_pow);
		}
		this.xN = (this.C.subtract(BigInteger.ONE)).divide(this.p);
		System.out.println("decrypt xN = " + xN);
	}
	
}
