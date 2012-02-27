package recommend.algorithms;
import recommend.WordIndex;

import java.util.*;

public class LSIWKNN extends Algorithm {
	static final double THRESHOLD = 0.00001;
	
	int K1,K2;
	HashMap<Integer,Double>[] M, MT;
	
	double[][] U, UT, V, VT; // U=dxk, UT=kxd, VT=kxt, V=txk
	double[] S; // k
	
	List<HashMap<Integer,Double>> traindocs;
	
	public LSIWKNN( int K1, int K2 ) {
		super( "LSI-"+K1+":WKNN-"+K2 );
		this.K1 = K1;
		this.K2 = K2;
	}

    public void train( List<HashMap<Integer,Double>> traindocs ) {
    	this.traindocs = traindocs;
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
		UT = new double[K1][d];
		S = new double[K1];
		VT = new double[K1][t];
		
		for( int k = 0; k < K1; k++ ) {
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
		
		U = new double[UT[0].length][UT.length];
		
		for( int i = 0; i < U.length; i++ )
			for( int j = 0; j < UT.length; j++ )
				U[i][j] = UT[j][i];
		
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
		
		PriorityQueue<Pair> pq = new PriorityQueue<Pair>();
    	
    	for( int i = 0; i < U.length; i++ ) {
    		double similarity = similarity( U[i], c[0] );
    		
    		if( pq.size() < K2 ) {
    			pq.add( new Pair( i, similarity ) );
    		} else if( similarity > pq.peek().similarity ) {
    			pq.poll();
    			pq.add( new Pair( i, similarity ) );
    		}
    	}
		
    	double[] scores = new double[WordIndex.size()];
    	
    	while( !pq.isEmpty() ) {
    		Pair p = pq.poll();
    		HashMap<Integer,Double> traindoc = traindocs.get( p.doc );
    		
    		for( int word : traindoc.keySet() ) {
    			scores[word] += p.similarity*traindoc.get( word );
    		}
    	}
    	
    	return scores;
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
	
	private double similarity( double[] traindoc, double[] testdoc ) {
		double dp = 0;
		double norm2 = 0;
		
		for( int i = 0; i < traindoc.length; i++ ) {
			dp += traindoc[i]*testdoc[i];
			norm2 += testdoc[i]*testdoc[i];
		}
		
		return dp/( Math.sqrt( norm2 ) );
    }
    
	private static class Pair implements Comparable<Pair> {
		int doc;
		double similarity;
		
		public Pair( int doc, double similarity ) {
			this.doc = doc;
			this.similarity = similarity;
		}
		
		public int compareTo( Pair p ) {
			return similarity > p.similarity ? 1 : -1;
		}
	}
}
