
package recommend.data;

import java.io.*;
import java.util.*;
import org.json.*;

public class ClassicFour2 {
	static BufferedReader in;
	static BufferedReader in2;
	static StringTokenizer st;
	static String s;
	
	static PrintWriter out;
	static ArrayList<String> dict;
	static HashMap<String,Integer> index;
	
	static int D, T, X;
	
	public static void main( String[] args ) throws Throwable {
		in = new BufferedReader( new FileReader( "rawdata/classic4docbyterm.mat" ) );
		in2 = new BufferedReader( new FileReader( "rawdata/classic4terms.txt" ) );
		
		st = new StringTokenizer( in.readLine() );
		D = Integer.parseInt( st.nextToken() );
		T = Integer.parseInt( st.nextToken() );
		X = Integer.parseInt( st.nextToken() );
		dict = new ArrayList<String>();
		index = new HashMap<String,Integer>();
		
		for( int i = 0; i < T; i++ ) {
			s = in2.readLine();
			dict.add( i, s );
			index.put( s, i );
		}
		
		helper( 3204, "cacm", "reg_cacm.json" );
		helper( 1460, "cisi", "reg_cisi.json" );
		helper( 1398, "cran", "reg_cran.json" );
		helper( 1033, "med", "reg_med.json" );
	}
	
	public static void helper( int n, String name, String file ) throws Throwable {
		out = new PrintWriter( new BufferedWriter( new FileWriter( file ) ) );
		
		JSONObject json = new JSONObject();
		json.put( "num_users", n );
		JSONArray users = new JSONArray();
		
		for( int i = 0; i < n; i++ ) {
			st = new StringTokenizer( in.readLine() );
			JSONObject user = new JSONObject();
			user.put( "name", name+i );
			JSONArray items = new JSONArray();
			JSONArray scores = new JSONArray();
			
			int length = st.countTokens() / 2;
			
			for( int j = 0; j < length; j++ ) {
				items.put( dict.get( Integer.parseInt( st.nextToken() )-1 ) );
				scores.put( Integer.parseInt( st.nextToken() ) );
			}

			user.put( "items", items );
			user.put( "scores", scores );
			users.put( user );
		}
		
		json.put( "users", users );
		out.println( json.toString() );
		out.close();
	}
}
