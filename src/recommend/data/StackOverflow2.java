package recommend.data;

import java.io.*;
import java.math.BigInteger;
import java.text.BreakIterator;
import java.util.*;
import org.json.*;
import net.htmlparser.jericho.*;

public class StackOverflow2 {
	static HashSet<String> stopwords = new HashSet<String>();
	
	public static void main( String[] args ) throws Throwable {
		BufferedReader in = new BufferedReader( new FileReader( "stopwords.txt" ) );
		String s;
		
		while( ( s = in.readLine() ) != null ) {
			stopwords.add( s );
		}
		
		in = new BufferedReader( new FileReader( "stackoverflow.1000.raw" ) );
		JSONObject json = new JSONObject( in.readLine() );
		
		JSONArray questions = json.getJSONArray( "questions" );
		int answer_id = 0;
		
		for( int i = 0; i < questions.length(); i++ ) {
			JSONObject question = questions.getJSONObject( i );
			int author;
			
			if( question.isNull( "owner" ) ) {
				author = -1;
			} else {
				author = question.getJSONObject( "owner" ).getInt( "user_id" );
			}
			
			JSONArray answers;
			
			if( question.getInt( "answer_count" ) == 0 ) {
				answers = new JSONArray();
			} else {
				answers = question.getJSONArray( "answers" );
			}
			
			for( Iterator<String> iter = question.keys(); iter.hasNext(); ) {
				String key = iter.next();
				
				if( !key.equals( "title" ) && !key.equals( "body" ) && !key.equals( "answer_count" ) && !key.equals( "answers" ) && !key.equals( "tags" ) ) {
					iter.remove();
				}
			}
			
			question.put( "body", format( question.getString( "body" ) ) );
			question.put( "author", author );
			question.put( "id", i );
			
			for( int j = 0; j < answers.length(); j++ ) {
				JSONObject answer = answers.getJSONObject( j );
				int answer_author;
				
				if( answer.isNull( "owner" ) ) {
					answer_author = -1;
				} else {
					answer_author = answer.getJSONObject( "owner" ).getInt( "user_id" );
				}
				
				for( Iterator<String> iter = answer.keys(); iter.hasNext(); ) {
					String key = iter.next();
					
					if( !key.equals( "body" ) ) {
						iter.remove();
					}
				}
				
				answer.put( "body", format( answer.getString( "body" ) ) );
				answer.put( "author", answer_author );
				answer.put( "id", answer_id++ );
			}
		}
		
		PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( "stackoverflow.1000.data" ) ) );
		out.println( json.toString() );
		out.close();
	}
	
	public static String format( String str ) {
		Source source = new Source( str.toLowerCase() );
		source.fullSequentialParse();
		OutputDocument output = new OutputDocument( source );
		output.remove( source.getAllElements( "code" ) );
		source = new Source( output.toString() );
		source.fullSequentialParse();
		TextExtractor extractor = new TextExtractor( source );
		str = extractor.toString();
		
		BreakIterator bi = BreakIterator.getWordInstance();
		bi.setText( str );
		StringBuilder sb = new StringBuilder();
		int index = 0;
		
		while( bi.next() != BreakIterator.DONE ) {
			String word = str.substring( index, index = bi.current() );
			
			if( Character.isLetter( word.charAt( 0 ) ) && !stopwords.contains( word ) ) {
				sb.append( ' ' ).append( word );
			}
		}
		
		if( sb.length() == 0 ) {
			return "";
		} else {
			return sb.substring( 1 );
		}
	}
}
