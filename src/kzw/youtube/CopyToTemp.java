package kzw.youtube;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


// TODO : use java 7 nio
/**
 *
 * @author me
 */
public class CopyToTemp {
    public static String cp(String path) throws FileNotFoundException, IOException {
        File temp = File.createTempFile("video",".mp4");
	InputStream in = new FileInputStream(path);
	OutputStream out = new FileOutputStream(temp);
	byte[] buf = new byte[1024];
	int len;
	while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
	in.close();
	out.close();
        return temp.getAbsolutePath();
        }
}
