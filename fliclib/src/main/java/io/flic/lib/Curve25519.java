/* Ported from C to Java by Dmitry Skiba [sahn0], 23/02/08.
 * Original: http://cds.xs4all.nl:8081/ecdh/
 */
/* Generic 64-bit integer implementation of Curve25519 ECDH
 * Written by Matthijs van Duin, 200608242056
 * Public domain.
 *
 * Based on work by Daniel J Bernstein, http://cr.yp.to/ecdh.html
 */
package io.flic.lib;

class Curve25519 {

	/* key size */
	public static final int KEY_SIZE = 32;

	/* 0 */
	public static final byte[] ZERO = {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};

	/********* KEY AGREEMENT *********/
	
	/* Private key clamping
	 *   k [out] your private key for key agreement
	 *   k  [in]  32 random bytes
	 */
	public static final void clamp(byte[] k) {
		k[31] &= 0x7F;
		k[31] |= 0x40;
		k[ 0] &= 0xF8;
	}

	/* Key-pair generation
	 *   P  [out] your public key
	 *   k  [out] your private key for key agreement
	 *   k  [in]  32 random bytes
	 * s may be NULL if you don't care
	 *
	 * WARNING: if s is not NULL, this function has data-dependent timing */
	public static final void keygen(byte[] P, byte[] k) {
		clamp(k);
		core(P, k, null);
	}

	/* Key agreement
	 *   Z  [out] shared secret (needs hashing before use)
	 *   k  [in]  your private key for key agreement
	 *   P  [in]  peer's public key
	 */
	public static final void curve(byte[] Z, byte[] k, byte[] P) {
		core(Z, k, P);
	}

	/////////////////////////////////////////////////////////////////////////// 

	/* sahn0:
	 * Using this class instead of long[10] to avoid bounds checks. */
	private static final class long10 {
		public long10() {}
		public long10(
				long _0, long _1, long _2, long _3, long _4,
				long _5, long _6, long _7, long _8, long _9)
		{
			this._0=_0; this._1=_1; this._2=_2;
			this._3=_3; this._4=_4; this._5=_5;
			this._6=_6; this._7=_7; this._8=_8;
			this._9=_9;
		}
		public long _0,_1,_2,_3,_4,_5,_6,_7,_8,_9;
	}

	/********************* radix 2^25.5 GF(2^255-19) math *********************/

	private static final int P25=33554431;	/* (1 << 25) - 1 */
	private static final int P26=67108863;	/* (1 << 26) - 1 */

	/* Convert to internal format from little-endian byte format */
	private static final void unpack(long10 x,byte[] m) {
		x._0 = ((m[0] & 0xFF))         | ((m[1] & 0xFF))<<8 |
				(m[2] & 0xFF)<<16      | ((m[3] & 0xFF)& 3)<<24;
		x._1 = ((m[3] & 0xFF)&~ 3)>>2  | (m[4] & 0xFF)<<6 |
				(m[5] & 0xFF)<<14 | ((m[6] & 0xFF)& 7)<<22;
		x._2 = ((m[6] & 0xFF)&~ 7)>>3  | (m[7] & 0xFF)<<5 |
				(m[8] & 0xFF)<<13 | ((m[9] & 0xFF)&31)<<21;
		x._3 = ((m[9] & 0xFF)&~31)>>5  | (m[10] & 0xFF)<<3 |
				(m[11] & 0xFF)<<11 | ((m[12] & 0xFF)&63)<<19;
		x._4 = ((m[12] & 0xFF)&~63)>>6 | (m[13] & 0xFF)<<2 |
				(m[14] & 0xFF)<<10 |  (m[15] & 0xFF)    <<18;
		x._5 =  (m[16] & 0xFF)         | (m[17] & 0xFF)<<8 |
				(m[18] & 0xFF)<<16 | ((m[19] & 0xFF)& 1)<<24;
		x._6 = ((m[19] & 0xFF)&~ 1)>>1 | (m[20] & 0xFF)<<7 |
				(m[21] & 0xFF)<<15 | ((m[22] & 0xFF)& 7)<<23;
		x._7 = ((m[22] & 0xFF)&~ 7)>>3 | (m[23] & 0xFF)<<5 |
				(m[24] & 0xFF)<<13 | ((m[25] & 0xFF)&15)<<21;
		x._8 = ((m[25] & 0xFF)&~15)>>4 | (m[26] & 0xFF)<<4 |
				(m[27] & 0xFF)<<12 | ((m[28] & 0xFF)&63)<<20;
		x._9 = ((m[28] & 0xFF)&~63)>>6 | (m[29] & 0xFF)<<2 |
				(m[30] & 0xFF)<<10 |  (m[31] & 0xFF)    <<18;
	}

	/* Check if reduced-form input >= 2^255-19 */
	private static final boolean is_overflow(long10 x) {
		return (
				((x._0 > P26-19)) &&
						((x._1 & x._3 & x._5 & x._7 & x._9) == P25) &&
						((x._2 & x._4 & x._6 & x._8) == P26)
		) || (x._9 > P25);
	}

	/* Convert from internal format to little-endian byte format.  The 
	 * number must be in a reduced form which is output by the following ops:
	 *     unpack, mul, sqr
	 *     set --  if input in range 0 .. P25
	 * If you're unsure if the number is reduced, first multiply it by 1.  */
	private static final void pack(long10 x,byte[] m) {
		int ld = 0, ud = 0;
		long t;
		ld = (is_overflow(x)?1:0) - ((x._9 < 0)?1:0);
		ud = ld * -(P25+1);
		ld *= 19;
		t = ld + x._0 + (x._1 << 26);
		m[ 0] = (byte)t;
		m[ 1] = (byte)(t >> 8);
		m[ 2] = (byte)(t >> 16);
		m[ 3] = (byte)(t >> 24);
		t = (t >> 32) + (x._2 << 19);
		m[ 4] = (byte)t;
		m[ 5] = (byte)(t >> 8);
		m[ 6] = (byte)(t >> 16);
		m[ 7] = (byte)(t >> 24);
		t = (t >> 32) + (x._3 << 13);
		m[ 8] = (byte)t;
		m[ 9] = (byte)(t >> 8);
		m[10] = (byte)(t >> 16);
		m[11] = (byte)(t >> 24);
		t = (t >> 32) + (x._4 <<  6);
		m[12] = (byte)t;
		m[13] = (byte)(t >> 8);
		m[14] = (byte)(t >> 16);
		m[15] = (byte)(t >> 24);
		t = (t >> 32) + x._5 + (x._6 << 25);
		m[16] = (byte)t;
		m[17] = (byte)(t >> 8);
		m[18] = (byte)(t >> 16);
		m[19] = (byte)(t >> 24);
		t = (t >> 32) + (x._7 << 19);
		m[20] = (byte)t;
		m[21] = (byte)(t >> 8);
		m[22] = (byte)(t >> 16);
		m[23] = (byte)(t >> 24);
		t = (t >> 32) + (x._8 << 12);
		m[24] = (byte)t;
		m[25] = (byte)(t >> 8);
		m[26] = (byte)(t >> 16);
		m[27] = (byte)(t >> 24);
		t = (t >> 32) + ((x._9 + ud) << 6);
		m[28] = (byte)t;
		m[29] = (byte)(t >> 8);
		m[30] = (byte)(t >> 16);
		m[31] = (byte)(t >> 24);
	}

	/* Copy a number */
	private static final void cpy(long10 out, long10 in) {
		out._0=in._0;	out._1=in._1;
		out._2=in._2;	out._3=in._3;
		out._4=in._4;	out._5=in._5;
		out._6=in._6;	out._7=in._7;
		out._8=in._8;	out._9=in._9;
	}

	/* Set a number to value, which must be in range -185861411 .. 185861411 */
	private static final void set(long10 out, int in) {
		out._0=in;	out._1=0;
		out._2=0;	out._3=0;
		out._4=0;	out._5=0;
		out._6=0;	out._7=0;
		out._8=0;	out._9=0;
	}

	/* Add/subtract two numbers.  The inputs must be in reduced form, and the 
	 * output isn't, so to do another addition or subtraction on the output, 
	 * first multiply it by one to reduce it. */
	private static final void add(long10 xy, long10 x, long10 y) {
		xy._0 = x._0 + y._0;	xy._1 = x._1 + y._1;
		xy._2 = x._2 + y._2;	xy._3 = x._3 + y._3;
		xy._4 = x._4 + y._4;	xy._5 = x._5 + y._5;
		xy._6 = x._6 + y._6;	xy._7 = x._7 + y._7;
		xy._8 = x._8 + y._8;	xy._9 = x._9 + y._9;
	}
	private static final void sub(long10 xy, long10 x, long10 y) {
		xy._0 = x._0 - y._0;	xy._1 = x._1 - y._1;
		xy._2 = x._2 - y._2;	xy._3 = x._3 - y._3;
		xy._4 = x._4 - y._4;	xy._5 = x._5 - y._5;
		xy._6 = x._6 - y._6;	xy._7 = x._7 - y._7;
		xy._8 = x._8 - y._8;	xy._9 = x._9 - y._9;
	}

	/* Multiply a number by a small integer in range -185861411 .. 185861411.
	 * The output is in reduced form, the input x need not be.  x and xy may point
	 * to the same buffer. */
	private static final long10 mul_small(long10 xy, long10 x, long y) {
		long t;
		t = (x._8*y);
		xy._8 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x._9*y);
		xy._9 = (t & ((1 << 25) - 1));
		t = 19 * (t >> 25) + (x._0*y);
		xy._0 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x._1*y);
		xy._1 = (t & ((1 << 25) - 1));
		t = (t >> 25) + (x._2*y);
		xy._2 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x._3*y);
		xy._3 = (t & ((1 << 25) - 1));
		t = (t >> 25) + (x._4*y);
		xy._4 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x._5*y);
		xy._5 = (t & ((1 << 25) - 1));
		t = (t >> 25) + (x._6*y);
		xy._6 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x._7*y);
		xy._7 = (t & ((1 << 25) - 1));
		t = (t >> 25) + xy._8;
		xy._8 = (t & ((1 << 26) - 1));
		xy._9 += (t >> 26);
		return xy;
	}

	/* Multiply two numbers.  The output is in reduced form, the inputs need not 
	 * be. */
	private static final long10 mul(long10 xy, long10 x, long10 y) {
		/* sahn0:
		 * Using local variables to avoid class access.
		 * This seem to improve performance a bit...
		 */
		long
				x_0=x._0,x_1=x._1,x_2=x._2,x_3=x._3,x_4=x._4,
				x_5=x._5,x_6=x._6,x_7=x._7,x_8=x._8,x_9=x._9;
		long
				y_0=y._0,y_1=y._1,y_2=y._2,y_3=y._3,y_4=y._4,
				y_5=y._5,y_6=y._6,y_7=y._7,y_8=y._8,y_9=y._9;
		long t;
		t = (x_0*y_8) + (x_2*y_6) + (x_4*y_4) + (x_6*y_2) +
				(x_8*y_0) + 2 * ((x_1*y_7) + (x_3*y_5) +
				(x_5*y_3) + (x_7*y_1)) + 38 *
				(x_9*y_9);
		xy._8 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x_0*y_9) + (x_1*y_8) + (x_2*y_7) +
				(x_3*y_6) + (x_4*y_5) + (x_5*y_4) +
				(x_6*y_3) + (x_7*y_2) + (x_8*y_1) +
				(x_9*y_0);
		xy._9 = (t & ((1 << 25) - 1));
		t = (x_0*y_0) + 19 * ((t >> 25) + (x_2*y_8) + (x_4*y_6)
				+ (x_6*y_4) + (x_8*y_2)) + 38 *
				((x_1*y_9) + (x_3*y_7) + (x_5*y_5) +
						(x_7*y_3) + (x_9*y_1));
		xy._0 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x_0*y_1) + (x_1*y_0) + 19 * ((x_2*y_9)
				+ (x_3*y_8) + (x_4*y_7) + (x_5*y_6) +
				(x_6*y_5) + (x_7*y_4) + (x_8*y_3) +
				(x_9*y_2));
		xy._1 = (t & ((1 << 25) - 1));
		t = (t >> 25) + (x_0*y_2) + (x_2*y_0) + 19 * ((x_4*y_8)
				+ (x_6*y_6) + (x_8*y_4)) + 2 * (x_1*y_1)
				+ 38 * ((x_3*y_9) + (x_5*y_7) +
				(x_7*y_5) + (x_9*y_3));
		xy._2 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x_0*y_3) + (x_1*y_2) + (x_2*y_1) +
				(x_3*y_0) + 19 * ((x_4*y_9) + (x_5*y_8) +
				(x_6*y_7) + (x_7*y_6) +
				(x_8*y_5) + (x_9*y_4));
		xy._3 = (t & ((1 << 25) - 1));
		t = (t >> 25) + (x_0*y_4) + (x_2*y_2) + (x_4*y_0) + 19 *
				((x_6*y_8) + (x_8*y_6)) + 2 * ((x_1*y_3) +
				(x_3*y_1)) + 38 *
				((x_5*y_9) + (x_7*y_7) + (x_9*y_5));
		xy._4 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x_0*y_5) + (x_1*y_4) + (x_2*y_3) +
				(x_3*y_2) + (x_4*y_1) + (x_5*y_0) + 19 *
				((x_6*y_9) + (x_7*y_8) + (x_8*y_7) +
						(x_9*y_6));
		xy._5 = (t & ((1 << 25) - 1));
		t = (t >> 25) + (x_0*y_6) + (x_2*y_4) + (x_4*y_2) +
				(x_6*y_0) + 19 * (x_8*y_8) + 2 * ((x_1*y_5) +
				(x_3*y_3) + (x_5*y_1)) + 38 *
				((x_7*y_9) + (x_9*y_7));
		xy._6 = (t & ((1 << 26) - 1));
		t = (t >> 26) + (x_0*y_7) + (x_1*y_6) + (x_2*y_5) +
				(x_3*y_4) + (x_4*y_3) + (x_5*y_2) +
				(x_6*y_1) + (x_7*y_0) + 19 * ((x_8*y_9) +
				(x_9*y_8));
		xy._7 = (t & ((1 << 25) - 1));
		t = (t >> 25) + xy._8;
		xy._8 = (t & ((1 << 26) - 1));
		xy._9 += (t >> 26);
		return xy;
	}

	/* Square a number.  Optimization of  mul25519(x2, x, x)  */
	private static final long10 sqr(long10 x2, long10 x) {
		long
				x_0=x._0,x_1=x._1,x_2=x._2,x_3=x._3,x_4=x._4,
				x_5=x._5,x_6=x._6,x_7=x._7,x_8=x._8,x_9=x._9;
		long t;
		t = (x_4*x_4) + 2 * ((x_0*x_8) + (x_2*x_6)) + 38 *
				(x_9*x_9) + 4 * ((x_1*x_7) + (x_3*x_5));
		x2._8 = (t & ((1 << 26) - 1));
		t = (t >> 26) + 2 * ((x_0*x_9) + (x_1*x_8) + (x_2*x_7) +
				(x_3*x_6) + (x_4*x_5));
		x2._9 = (t & ((1 << 25) - 1));
		t = 19 * (t >> 25) + (x_0*x_0) + 38 * ((x_2*x_8) +
				(x_4*x_6) + (x_5*x_5)) + 76 * ((x_1*x_9)
				+ (x_3*x_7));
		x2._0 = (t & ((1 << 26) - 1));
		t = (t >> 26) + 2 * (x_0*x_1) + 38 * ((x_2*x_9) +
				(x_3*x_8) + (x_4*x_7) + (x_5*x_6));
		x2._1 = (t & ((1 << 25) - 1));
		t = (t >> 25) + 19 * (x_6*x_6) + 2 * ((x_0*x_2) +
				(x_1*x_1)) + 38 * (x_4*x_8) + 76 *
				((x_3*x_9) + (x_5*x_7));
		x2._2 = (t & ((1 << 26) - 1));
		t = (t >> 26) + 2 * ((x_0*x_3) + (x_1*x_2)) + 38 *
				((x_4*x_9) + (x_5*x_8) + (x_6*x_7));
		x2._3 = (t & ((1 << 25) - 1));
		t = (t >> 25) + (x_2*x_2) + 2 * (x_0*x_4) + 38 *
				((x_6*x_8) + (x_7*x_7)) + 4 * (x_1*x_3) + 76 *
				(x_5*x_9);
		x2._4 = (t & ((1 << 26) - 1));
		t = (t >> 26) + 2 * ((x_0*x_5) + (x_1*x_4) + (x_2*x_3))
				+ 38 * ((x_6*x_9) + (x_7*x_8));
		x2._5 = (t & ((1 << 25) - 1));
		t = (t >> 25) + 19 * (x_8*x_8) + 2 * ((x_0*x_6) +
				(x_2*x_4) + (x_3*x_3)) + 4 * (x_1*x_5) +
				76 * (x_7*x_9);
		x2._6 = (t & ((1 << 26) - 1));
		t = (t >> 26) + 2 * ((x_0*x_7) + (x_1*x_6) + (x_2*x_5) +
				(x_3*x_4)) + 38 * (x_8*x_9);
		x2._7 = (t & ((1 << 25) - 1));
		t = (t >> 25) + x2._8;
		x2._8 = (t & ((1 << 26) - 1));
		x2._9 += (t >> 26);
		return x2;
	}

	/* Calculates a reciprocal.  The output is in reduced form, the inputs need not 
	 * be.  Simply calculates  y = x^(p-2)  so it's not too fast. */
	/* When sqrtassist is true, it instead calculates y = x^((p-5)/8) */
	private static final void recip(long10 y, long10 x, int sqrtassist) {
		long10
				t0=new long10(),
				t1=new long10(),
				t2=new long10(),
				t3=new long10(),
				t4=new long10();
		int i;
		/* the chain for x^(2^255-21) is straight from djb's implementation */
		sqr(t1, x);	/*  2 == 2 * 1	*/
		sqr(t2, t1);	/*  4 == 2 * 2	*/
		sqr(t0, t2);	/*  8 == 2 * 4	*/
		mul(t2, t0, x);	/*  9 == 8 + 1	*/
		mul(t0, t2, t1);	/* 11 == 9 + 2	*/
		sqr(t1, t0);	/* 22 == 2 * 11	*/
		mul(t3, t1, t2);	/* 31 == 22 + 9
					== 2^5   - 2^0	*/
		sqr(t1, t3);	/* 2^6   - 2^1	*/
		sqr(t2, t1);	/* 2^7   - 2^2	*/
		sqr(t1, t2);	/* 2^8   - 2^3	*/
		sqr(t2, t1);	/* 2^9   - 2^4	*/
		sqr(t1, t2);	/* 2^10  - 2^5	*/
		mul(t2, t1, t3);	/* 2^10  - 2^0	*/
		sqr(t1, t2);	/* 2^11  - 2^1	*/
		sqr(t3, t1);	/* 2^12  - 2^2	*/
		for (i = 1; i < 5; i++) {
			sqr(t1, t3);
			sqr(t3, t1);
		} /* t3 */		/* 2^20  - 2^10	*/
		mul(t1, t3, t2);	/* 2^20  - 2^0	*/
		sqr(t3, t1);	/* 2^21  - 2^1	*/
		sqr(t4, t3);	/* 2^22  - 2^2	*/
		for (i = 1; i < 10; i++) {
			sqr(t3, t4);
			sqr(t4, t3);
		} /* t4 */		/* 2^40  - 2^20	*/
		mul(t3, t4, t1);	/* 2^40  - 2^0	*/
		for (i = 0; i < 5; i++) {
			sqr(t1, t3);
			sqr(t3, t1);
		} /* t3 */		/* 2^50  - 2^10	*/
		mul(t1, t3, t2);	/* 2^50  - 2^0	*/
		sqr(t2, t1);	/* 2^51  - 2^1	*/
		sqr(t3, t2);	/* 2^52  - 2^2	*/
		for (i = 1; i < 25; i++) {
			sqr(t2, t3);
			sqr(t3, t2);
		} /* t3 */		/* 2^100 - 2^50 */
		mul(t2, t3, t1);	/* 2^100 - 2^0	*/
		sqr(t3, t2);	/* 2^101 - 2^1	*/
		sqr(t4, t3);	/* 2^102 - 2^2	*/
		for (i = 1; i < 50; i++) {
			sqr(t3, t4);
			sqr(t4, t3);
		} /* t4 */		/* 2^200 - 2^100 */
		mul(t3, t4, t2);	/* 2^200 - 2^0	*/
		for (i = 0; i < 25; i++) {
			sqr(t4, t3);
			sqr(t3, t4);
		} /* t3 */		/* 2^250 - 2^50	*/
		mul(t2, t3, t1);	/* 2^250 - 2^0	*/
		sqr(t1, t2);	/* 2^251 - 2^1	*/
		sqr(t2, t1);	/* 2^252 - 2^2	*/
		if (sqrtassist!=0) {
			mul(y, x, t2);	/* 2^252 - 3 */
		} else {
			sqr(t1, t2);	/* 2^253 - 2^3	*/
			sqr(t2, t1);	/* 2^254 - 2^4	*/
			sqr(t1, t2);	/* 2^255 - 2^5	*/
			mul(y, t1, t0);	/* 2^255 - 21	*/
		}
	}

	/********************* Elliptic curve *********************/
	
	/* y^2 = x^3 + 486662 x^2 + x  over GF(2^255-19) */
	
	/* t1 = ax + az
	 * t2 = ax - az  */
	private static final void mont_prep(long10 t1, long10 t2, long10 ax, long10 az) {
		add(t1, ax, az);
		sub(t2, ax, az);
	}

	/* A = P + Q   where
	 *  X(A) = ax/az
	 *  X(P) = (t1+t2)/(t1-t2)
	 *  X(Q) = (t3+t4)/(t3-t4)
	 *  X(P-Q) = dx
	 * clobbers t1 and t2, preserves t3 and t4  */
	private static final void mont_add(long10 t1, long10 t2, long10 t3, long10 t4,long10 ax, long10 az, long10 dx) {
		mul(ax, t2, t3);
		mul(az, t1, t4);
		add(t1, ax, az);
		sub(t2, ax, az);
		sqr(ax, t1);
		sqr(t1, t2);
		mul(az, t1, dx);
	}

	/* B = 2 * Q   where
	 *  X(B) = bx/bz
	 *  X(Q) = (t3+t4)/(t3-t4)
	 * clobbers t1 and t2, preserves t3 and t4  */
	private static final void mont_dbl(long10 t1, long10 t2, long10 t3, long10 t4,long10 bx, long10 bz) {
		sqr(t1, t3);
		sqr(t2, t4);
		mul(bx, t1, t2);
		sub(t2, t1, t2);
		mul_small(bz, t2, 121665);
		add(t1, t1, bz);
		mul(bz, t1, t2);
	}

	/* P = kG   and  s = sign(P)/k  */
	private static final void core(byte[] Px, byte[] k, byte[] Gx) {
		long10
				dx=new long10(),
				t1=new long10(),
				t2=new long10(),
				t3=new long10(),
				t4=new long10();
		long10[]
				x=new long10[]{new long10(),new long10()},
				z=new long10[]{new long10(),new long10()};
		int i, j;

		/* unpack the base */
		if (Gx!=null)
			unpack(dx, Gx);
		else
			set(dx, 9);

		/* 0G = point-at-infinity */
		set(x[0], 1);
		set(z[0], 0);

		/* 1G = G */
		cpy(x[1], dx);
		set(z[1], 1);

		for (i = 32; i--!=0; ) {
			if (i==0) {
				i=0;
			}
			for (j = 8; j--!=0; ) {
				/* swap arguments depending on bit */
				int bit1 = (k[i] & 0xFF) >> j & 1;
				int bit0 = ~(k[i] & 0xFF) >> j & 1;
				long10 ax = x[bit0];
				long10 az = z[bit0];
				long10 bx = x[bit1];
				long10 bz = z[bit1];

				/* a' = a + b	*/
				/* b' = 2 b	*/
				mont_prep(t1, t2, ax, az);
				mont_prep(t3, t4, bx, bz);
				mont_add(t1, t2, t3, t4, ax, az, dx);
				mont_dbl(t1, t2, t3, t4, bx, bz);
			}
		}

		recip(t1, z[0], 0);
		mul(dx, x[0], t1);
		pack(dx, Px);
	}
}