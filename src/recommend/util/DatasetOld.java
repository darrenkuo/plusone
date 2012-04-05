package recommend.util;

import java.io.*;
import java.util.*;
import org.json.*;

public class DatasetOld {
    /* Loads a set of documents and sets up WordIndex. */
    public static HashMap<Integer, Double>[] loadDataset(String DATASET) throws IOException, JSONException {
        if (WordIndex.size() > 0)
            throw new RuntimeException("Dataset.loadDataset: please start from an empty WordIndex!");

        BufferedReader in = new BufferedReader( new FileReader( DATASET ) );
        JSONObject json = new JSONObject( in.readLine() );

        int ndocs = json.getInt( "ndocs" );
        HashMap<Integer, Double>[] docs = new HashMap[ndocs];
        JSONArray arr = json.getJSONArray( "docs" );

        for( int i = 0; i < ndocs; i++ ) {
            JSONObject doc = arr.getJSONObject( i );
            docs[i] = new HashMap<Integer,Double>();
            JSONArray terms = doc.getJSONArray( "terms" );

            for( int j = 0; j < terms.length(); j++ ) {
                String term = terms.getString( j );
                WordIndex.add( terms.getString( j ) );
                int index = WordIndex.indexOf( term );
                WordIndex.incrementDF( index );
                docs[i].put( index, 1.0 );
            }
        }

        for( int w = 0; w < WordIndex.size(); w++ ) {
            WordIndex.setIDF( w, Math.log( (double)ndocs / WordIndex.getDF( w ) ) );
        }

        return docs;
    }
}
