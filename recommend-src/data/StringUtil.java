package data;

public final class StringUtil {
	public static String textBetween( String str, String start, String end, int index ) {
		if( str == null || start == null || end == null )
			return null;
		
		int s = str.indexOf( start, index );
		
		if( s != -1 ) {
			int e = str.indexOf( end, s+start.length() );
			
			if( e != -1 )
				return str.substring( s+start.length(), e );
			
		}
		
		return null;
	}
	
	public static String textBetween( String str, String start, String end ) {
		return textBetween( str, start, end, 0 );
	}
}
