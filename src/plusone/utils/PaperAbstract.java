package plusone.utils;

import java.util.Arrays;

public class PaperAbstract {
    public int index;
    public int[] inReferences;
    public int[] outReferences;
    public String abstractText; 
    
    public PaperAbstract(int index, int[] inReferences, 
			 int[] outReferences, String abstractText) {
	this.index = index;

	this.inReferences = inReferences;
	if (this.inReferences == null) 
	    this.inReferences = new int[0];

	this.outReferences = outReferences;
	if (this.outReferences == null)
	    this.outReferences = new int[0];

	this.abstractText = abstractText;
    }

    public String toString() {
	String results = "";
	results += "INDEX " + this.index + "\n";
	results += "IN REF " + Arrays.toString(this.inReferences) + "\n";
	results += "OUT REF " + Arrays.toString(this.outReferences) + "\n";
	results += "ABSTRACT " + this.abstractText + "\n";

	return results;
    }
}
