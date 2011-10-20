package plusone.utils;

public class ItemAndScore implements Comparable {
    public Object item;
    public double score;
    public boolean ascending;
    
    public ItemAndScore(Object item, double score, boolean ascending) {
	this.item = item;
	this.score = score;
	this.ascending = ascending;
    }
    
    public int compareTo(Object o) {
	ItemAndScore obj = (ItemAndScore)o;
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