package plusone.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class KBestList<T> {
    class PlusoneLinkedList<T> implements Iterable<T> {
	public T element;
	public PlusoneLinkedList previous;
	public PlusoneLinkedList next;

	public PlusoneLinkedList(T element) {
	    this.element = element;
	    this.previous = null;
	    this.next = null;
	}

	public Iterator<T> iterator() {
	    final PlusoneLinkedList<T> lst = this;
	    return new Iterator<T>() {
		PlusoneLinkedList<T> cur = lst;
		public boolean hasNext() {
		    return cur != null;
		}
		public T next() {
		    T r = cur.element;
		    cur = cur.next;
		    return r;
		}
		public void remove() {
		    // do nothing
		}
	    };
	}
    }
    
    PlusoneLinkedList<T> start;
    PlusoneLinkedList<T> end;
    Map<T, Double> score;
    public int size;
    int k;

    public KBestList(int k) {
	this.start = this.end = null;
	this.score = new HashMap<T, Double>();
	this.size = 0;
	this.k = k;
    }

    public void insert(T element, double score) {
	if (this.size == 0) {
	    this.start = this.end = new PlusoneLinkedList(element);
	    this.score.put(element, score);
	    this.size = 1;
	    return;
	}
	
	PlusoneLinkedList ll = this.start;
	do {
	    if (this.score.get(ll.element) <= score) {
		PlusoneLinkedList n = new PlusoneLinkedList(element);
		if (ll.previous == null) {
		    n.next = ll;
		    ll.previous = n;
		    this.start = n;
		} else {
		    ll.previous.next = n;
		    n.previous = ll.previous;
		    n.next = ll;
		    ll.previous = n;
		}
		
		this.score.put(element, score);
		
		if (this.size == k) {
		    this.end = this.end.previous;
		    this.end.next = null;
		} else {
		    this.size ++;
		}
		return;
	    }
	    ll = ll.next;
	} while (ll != this.end && ll != null);
	
	if (this.size < this.k) {
	    PlusoneLinkedList n = new PlusoneLinkedList(element);
	    n.previous = this.end;
	    this.end.next = n;
	    this.end = n;

	    this.size ++;
	    this.score.put(element, score);
	}
    }

    public Iterator<T> iterator() {
	return this.start.iterator();
    }

    public int actualSize() {
	PlusoneLinkedList cur = this.start;
	int s = 1;
	while (cur != this.end) {
	    cur = cur.next;
	    s ++;
	}
	return s;
    }

    public void verify() {
	PlusoneLinkedList cur = this.start;
	int s = 1;
	while (cur != this.end) {
	    if (cur == cur.next)
		System.out.println("ERROR!");
	    cur = cur.next;
	    s ++;
	}
	if (s == size && cur.next == null) {
	    System.out.println("verify complete.");
	} else {
	    System.out.println("K Best verify failed...");
	    System.out.println("s: " + s + " size: " + size);
	    System.out.println("cur.next: " + cur.next);
	}
    }
}