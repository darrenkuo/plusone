package plusone.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PlusoneFileWriter {
    BufferedWriter out;
    public PlusoneFileWriter(String filename) {
	try {
	    FileWriter fstream = new FileWriter(filename);
	    out = new BufferedWriter(fstream);
	}catch (Exception e) {
	    System.err.println("Error: " + e.getMessage());
	}
    }

    public PlusoneFileWriter(File file) {
	try {
	    FileWriter fstream = new FileWriter(file);
	    out = new BufferedWriter(fstream);
	}catch (Exception e) {
	    System.err.println("Error: " + e.getMessage());
	}
    }

    /* Constructs a PlusoneFileWriter that does nothing. */
    public PlusoneFileWriter() {
        out = null;
    }

    public void write(String content) {
        if (out == null) return;
	try {
	    out.write(content);
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }

    public void close() {
        if (out == null) return;
	try {
	    out.close();
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }
}