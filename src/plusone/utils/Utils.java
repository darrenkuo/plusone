package plusone.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class Utils {

    public static void writeToFile(String filename, String content) {
	try {
	    FileWriter fstream = new FileWriter(filename);
	    BufferedWriter out = new BufferedWriter(fstream);
	    out.write(content);
	    out.close();
	} catch (Exception e) {
	    System.err.println("Error: " + e.getMessage());
	}
    }
    
    /**
     * Executes a command line input (eg ls -a) with the option to hide the
     * output (to System.out) that command usually would give. If the command
     * fails, will display all error output from the command line.
     * 
     * @param command		the command to be run, exactly as if typed
     * 						into the command line (a String)
     * @param streamOutput	a flag to determine whether or not to display
     * 						any output the command might give
     */
    public static void runCommand(String command, boolean streamOutput) {

	System.out.println("Running command: " + command);
	try {
	    Process p = Runtime.getRuntime().exec(command);
	    if (streamOutput) {
		BufferedReader stdInput = 
		    new BufferedReader(new InputStreamReader(p.getInputStream()));
	    
		BufferedReader stdError = 
		    new BufferedReader(new InputStreamReader(p.getErrorStream()));
		System.out.println("Here is the standard output of the command:\n");
		String s;
		while ((s = stdInput.readLine()) != null) {
		    System.out.println(s);
		}
            
		// read any errors from the attempted command
		System.out.println("Here is the standard error of the command (if any):\n");
		while ((s = stdError.readLine()) != null) {
		    System.out.println(s);
		}
	    }
	    p.waitFor();
	}
	catch (Exception e) {
	    System.out.println("exception happened - here's what I know: ");
	    e.printStackTrace();
	    System.exit(-1);
	}

    }
}
