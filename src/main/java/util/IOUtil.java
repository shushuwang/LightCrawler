package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class IOUtil {
	
	public static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
	public static BufferedWriter output = new BufferedWriter(new OutputStreamWriter(System.out));
	public static BufferedWriter error = new BufferedWriter(new OutputStreamWriter(System.err));
	
	public static void outWrite(String str){
		try {
			output.write(str+System.getProperty("line.separator", "\n"));
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void errWrite(String str){
		try {
			error.write(str+System.getProperty("line.separator", "\n"));
			error.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
