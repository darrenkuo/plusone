package algorithms;
import WordIndex;

import java.util.*;

public class LSI extends Algorithm {
	static final double THRESHOLD = 0.00001;
	
	int K;
	HashMap<Integer,Double>[] M, MT;
	
	double[][] UT, V, VT; // UT=kxd, VT=kxt, V=txk
	double[] S; // k
	
	public LSI( int K ) {
		super( "LSI-"+K );
		this.K = K;
	}

    public void train( List<HashMap<Integer,Double>> traindocs ) {
		M = new HashMap[traindocs.size()];
		MT = new HashMap[WordIndex.size()];
		
		for( int i = 0; i < MT.length; i++ )
			MT[i] = new HashMap<Integer,Double>();
		
		for( int i = 0; i < traindocs.size(); i++ ) {
			M[i] = traindocs.get( i );
			
			for( int word : M[i].keySet() ) {
				MT[word].put( i, MT[i].containsKey( word ) ? MT[i].get( word ) + 1 : 1 );
			}
		}
		
		int d = M.length;
		int t = MT.length;
		UT = new double[K][d];
		S = new double[K];
		VT = new double[K][t];
		
		for( int k = 0; k < K; k++ ) {
			double[] u = new double[d], up = new double[d];
			double[] v = new double[t], vp = new double[t];
			
			for( int i = 0; i < d; i++ )
				up[i] = 1.0;
			
			for( int i = 0; i < t; i++ )
				vp[i] = 1.0;
			
			double norm2u, norm2up = norm2( up );
			double norm2v, norm2vp = norm2( vp );
			
			while( true ) {
				for( int i = 0; i < d; i++ )
					u[i] = up[i];
				
				for( int j = 0; j < t; j++ )
					v[j] = vp[j];
				
				norm2u = norm2up;
				norm2v = norm2vp;
				
				double[] t1 = new double[k];
				
				for( int a = 0; a < k; a++ )
					t1[a] = dotproduct( v, VT[a] );
				
				for( int i = 0; i < d; i++ ) {
					up[i] = 0.0;
					
					for( int word : M[i].keySet() )
						up[i] += M[i].get( word ) * v[word];
					
					for( int a = 0; a < k; a++ )
						up[i] -= UT[a][i] * S[a] * t1[a];
					
					up[i] /= norm2v;
				}
				
				norm2up = norm2( up );
				
				if( norm2up < THRESHOLD*THRESHOLD )
					break;
				
				for( int a = 0; a < k; a++ )
					t1[a] = dotproduct( up, UT[a] );
				
				for( int j = 0; j < t; j++ ) {
					vp[j] = 0.0;
					
					for( int paper : MT[j].keySet() )
						vp[j] += MT[j].get( paper ) * up[paper];
					
					for( int a = 0; a < k; a++ )
						vp[j] -= VT[a][j] * S[a] * t1[a];
					
					vp[j] /= norm2up;
				}
				
				norm2vp = norm2( vp );
				
				if( norm2vp < THRESHOLD*THRESHOLD )
					break;
				
				if( Math.abs( norm2u * norm2v - norm2up * norm2vp ) < THRESHOLD * norm2u * norm2v )
					break;
			}
			
			double normu = norm( u );
			double normv = norm( v );
			
			for( int i = 0; i < d; i++ )
				UT[k][i] = up[i] / normu;
			
			for( int j = 0; j < t; j++ )
				VT[k][j] = vp[j] / normv;
			
			S[k] = normu * normv;
		}
		
		V = new double[VT[0].length][VT.length];
		
		for( int i = 0; i < V.length; i++ )
			for( int j = 0; j < VT.length; j++ )
				V[i][j] = VT[j][i];
    }

    public double[] predict( HashMap<Integer,Double> givenwords ) {
    	double[][] c = new double[1][WordIndex.size()];
		
		for( int word : givenwords.keySet() )
			c[0][word] = givenwords.get( word );
		
		c = matmul( c, V );
		
		for( int i = 0; i < c.length; i++ )
			c[0][i] /= S[i];
		
		c = matmul( c, VT );
		return c[0];
    }
	
	private static final double[][] matmul( double[][] a, double[][] b ) {
		double[][] c = new double[a.length][b[0].length];
		
		for( int i = 0; i < c.length; i++ ) {
			for( int j = 0; j < c[0].length; j++ ) {
				for( int k = 0; k < b.length; k++ ) {
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		
		return c;
	}
	
	private static final double norm2( double[] a ) {
		double norm2 = 0.0;
		
		for( int i = 0; i < a.length; i++ ) {
			norm2 += a[i] * a[i];
		}
		
		return norm2;
	}
	
	private static final double norm( double[] a ) {
		return Math.sqrt( norm2( a ) );
	}
	
	private static final double dotproduct( double[] a, double[] b ) {
		double dp = 0.0;
		
		for( int i = 0; i < a.length; i++ )
			dp += a[i] * b[i];
		
		return dp;
	}
}
