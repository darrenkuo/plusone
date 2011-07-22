package plusone.utils;

public class WordAndScore implements Comparable {
    public int wordID;
    public double score;
    public boolean ascending;

    public WordAndScore(int wordID, double score, boolean ascending) {
	this.wordID = wordID;
	this.score = score;
	this.ascending = ascending;
    }
    
    public int compareTo(Object o) {
	WordAndScore obj = (WordAndScore)o;
	int result;
	if (this.score < obj.score) {
	    result = -1;
	} else if (this.score > obj.score) {
	    result = 1;
	} else {
	    result = 0;
	}

	if (!this.ascending)
	    return result * -1;
	return result;
    }
}