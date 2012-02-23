package plusone.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Fetch {
    /** Copies everything from in to out, then closes both streams.
     *
     * Based on code at
     * http://java.sun.com/docs/books/performance/1st_edition/html/JPIOPerformance.fm.html
     * retrieved on 20120223.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException, MalformedURLException {
        InputStream inB = new BufferedInputStream(in);
        OutputStream outB = new BufferedOutputStream(out);
        while (true) {
            int data = inB.read();
            if (data == -1) {
                break;
            }
            outB.write(data);
        }

        if (inB != null) {
            inB.close();
        }
        if (outB != null) {
            outB.close();
        }
    }

    public static void fetchUrl(String url, String outPath) throws IOException {
        copy((new URL(url)).openConnection().getInputStream(),
             new FileOutputStream(outPath));
    }
}
