package util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
	
	public static String getErrorInfoFromException(Exception e) {  
        try {  
            StringWriter sw = new StringWriter();  
            PrintWriter pw = new PrintWriter(sw);  
            e.printStackTrace(pw);  
            return "\n" + sw.toString() + "\n";  
        } catch (Exception e2) {  
            return "error when try to get error info by getErrorInfoFromException() method!";  
        }  
    }
	
}
