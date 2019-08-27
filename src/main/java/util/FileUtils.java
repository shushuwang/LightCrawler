package util;

import com.google.common.base.Strings;
import com.google.common.io.Files;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * shushuwang
 */
public class FileUtils {

    public static boolean isExist(String path) {
        File f = new File(path);
        return f.exists();
    }

    public static void copyTo(String from, String to) throws IOException {
        File fromfile = new File(from);
        File dest = new File(to);
        mkDir(to, dest);
        Files.copy(fromfile,dest);
    }

    public static void moveTo(String from, String to) {
        File fromfile = new File(from);
        File dest = new File(to);
        mkDir(to, dest);
        fromfile.renameTo(dest);
    }

    private static void mkDir(String to, File dest) {
        if (dest.exists()) {
            dest.delete();
        } else {
            String dir = to.substring(0,to.lastIndexOf(File.separator));
            File f = new File(dir);
            if (!f.exists()) {
                f.mkdirs();
            }
        }
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.isFile()) {
            file.delete();
        }
    }

    public static void writeFile(List<String> values, String filepath) {
        if (values == null || values.size() == 0 || Strings.isNullOrEmpty(filepath)) return;

        FileWriter fw = null;
        try {

            String dir = filepath.substring(0,filepath.lastIndexOf(File.separator));
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(dir);
            if (!f.exists()) {
                f.mkdirs();
                f = new File(filepath);
            } else {
                f = new File(filepath);
            }

            if(!f.exists()){
                f.createNewFile();
            }
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        for(String s : values) {
//            pw.println(Arrays.toString(values.toArray()));
            pw.println(s);
        }

        pw.flush();
        close(fw, pw);
    }

    public static void writeFile(String value, String filepath) {
        if (Strings.isNullOrEmpty(value)|| Strings.isNullOrEmpty(filepath)) return;

        FileWriter fw = null;
        try {
            String dir = filepath.substring(0,filepath.lastIndexOf(File.separator));
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(dir);
            if (!f.exists()) {
                f.mkdirs();
                f = new File(filepath);
            } else {
                f = new File(filepath);
            }

            if(!f.exists()){
                f.createNewFile();
            }
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(value);

        pw.flush();
        close(fw, pw);
    }

    public static synchronized void overwriteFile(List<String> values, String path) {
        if (values == null) return;

        FileWriter fw = null;
        try {
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(path);
            fw = new FileWriter(f, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        for(String s : values) {
            pw.println(s);
        }
        values.clear();
        pw.flush();
        close(fw, pw);
    }

    private static void close(FileWriter fw, PrintWriter pw) {
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> readFile(String filepath) throws IOException {
        ArrayList<String> result = new ArrayList<>();
        File file = new File(filepath);
        if (!file.exists()) {
            return result;
        }
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        String line ;
        while ((line=reader.readLine())!=null) {
            result.add(line.trim());
        }
        reader.close();
        return result;
    }

    public static List<String> readResourceFile(String filename) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        ArrayList<String> result = new ArrayList<>();
        String line = "";
        while (!Strings.isNullOrEmpty(line = reader.readLine())) {
            result.add(line.trim());
        }
        reader.close();
        return result;
    }

    public static String inputStream2String(InputStream is) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
