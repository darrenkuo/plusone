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

    public void write(String content) {
	try {
	    out.write(content);
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }

    public void close() {
	try {
	    out.close();
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }
}