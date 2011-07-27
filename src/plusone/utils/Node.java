package plusone.utils;

public class Node {
    public int location;
    public Node left;
    public Node right;

    public Node() {
    }

    public Node(int location, Node left, Node right) {
	this.location = location;
	this.left = left;
	this.right = right;
    }

    /*
    public static Node kdtree(Document[] documents, int depth, 
			      int s, int e) {
	if (s == e) {
	    return null;
	}

	int k = documents[0].uniqueWords;
	int axis = (e - s + 1) % k;
	
	Arrays.sort(documents, s, e, Document.getComparator(axis));

	int median = (int)((e - s + 1) / 2.0);
	
	Node node = new Node();
	node.location = documents[median];
	node.left = kdTree(documents, depth + 1, s, median);
	
	Arrays.sort(documents, s, e, Document.getComparator(axis));
	node.right = kdTree(documents, depth + 1, median + 1, e);

	return node;	
    }
    */
}