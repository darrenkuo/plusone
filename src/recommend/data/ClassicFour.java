package recommend.data;

import java.io.*;
import java.util.*;

public class ClassicFour {
	public static void main( String[] args ) throws Throwable {
	    BufferedReader in = new BufferedReader( new FileReader( "rawdata/classic4docbyterm.mat" ) );
	    BufferedReader in2 = new BufferedReader( new FileReader( "rawdata/classic4terms.txt" ) );
	    PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "med.out" ) ) );
	    
	    ArrayList<String> dict = new ArrayList<String>();
	    HashMap<String,Integer> index = new HashMap<String,Integer>();
	    
	    StringTokenizer st = new StringTokenizer( in.readLine() );
	    String s;
	    
	    int D = Integer.parseInt( st.nextToken() );
	    int T = Integer.parseInt( st.nextToken() );
	    int X = Integer.parseInt( st.nextToken() );
	    
	    for( int i = 0; i < T; i++ ) {
	    	s = in2.readLine();
	    	dict.add( i, s );
	    	index.put( s, i );
	    }
	    
	    for( int i = 0; i < 3204+1460+1398; i++ ) {
	    	in.readLine();
	    }
	    
	    for( int i = 0; i < 1033; i++ ) {
	    	out.println( "<!--" );
	    	out.println( "#INDEX " + (i+1) );
	    	out.println( "#IN-REF" );
	    	out.println( "#OUT-REF" );
	    	out.print( "#ABSTRACT" );
	    	st = new StringTokenizer( in.readLine() );
	    	
	    	int length = st.countTokens()/2;
	    	
	    	for( int j = 0; j < length; j++ ) {
	    		int word = Integer.parseInt( st.nextToken() );
	    		int freq = Integer.parseInt( st.nextToken() );
	    		s = dict.get( word-1 );
	    		
	    		for( int k = 0; k < freq; k++ ) {
	    			out.print( " " + s );
	    		}
	    	}
	    	
	    	out.println();
	    	out.println( "-->" );
	    }
	    
	    out.close();
    }
}
