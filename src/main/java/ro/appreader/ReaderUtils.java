package ro.appreader;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import ro.common.JSTreeNode;
import ro.common.LogFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by diana on 14.04.2015.
 */
public class ReaderUtils {

    static {
        Path path = Paths.get("");
        path = path.toAbsolutePath();
        path = path.getParent();
        DProperties instance = DProperties.getInstance();
        instance.setRoot(path.toString() + "/webapps");
        instance.setLogsRoot(path.toString() + "/logs");
    }
    /*
        method used to decompile a class
         */
    public static Writer decompile(String urlPath, Writer writer)throws IOException {
        final DecompilerSettings settings = DecompilerSettings.javaDefaults();
        DProperties rootPath = DProperties.getInstance();
        String path = rootPath.getRoot() + urlPath;
        Decompiler.decompile(
                path,
                new PlainTextOutput(writer),
                settings
        );
        return writer;
    }
    /*
    method used to get text from a file
     */
    public static ServletOutputStream getContent(String urlpath, ServletOutputStream outputStream) throws IOException {
        DProperties rootPath = DProperties.getInstance();
        String path = rootPath.getRoot() + urlpath;
        try(FileInputStream fis = new FileInputStream(path)){
            int c;
            byte[] buffer = new byte[1024];
            while((c = fis.read(buffer)) != -1){
                outputStream.write(buffer, 0, c);
            }
        }
        return outputStream;
    }



    public static List<JSTreeNode> getFiles(String root){
        List<JSTreeNode> result = new ArrayList<JSTreeNode>();

        if (root == null || root.equals("#")) root = "";
        DProperties dProperties = DProperties.getInstance();

        Path path = Paths.get(dProperties.getRoot() + "/" + root);
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)){
            for (Path childPath : directoryStream) {
                JSTreeNode jsTreeNode = new JSTreeNode();
                jsTreeNode.setId(root + "/" + childPath.getFileName());
                jsTreeNode.setText(childPath.getFileName().toString());
                jsTreeNode.setChildren(childPath.toFile().list()!= null ? true : false);
                jsTreeNode.setType(getType(childPath));
                result.add(jsTreeNode);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return result;
    }

    public static String getType(Path fileName) {
        if (fileName.toFile().isDirectory()){
            return "type_directory";
        }else if (fileName.toString().endsWith(".class")){
            return "type_class";
        }else if (fileName.toString().endsWith(".xml")){
            return "type_xml";
        }else if (fileName.toString().endsWith(".jsp")){
            return "type_jsp";
        }else if (fileName.toString().endsWith(".html")){
            return "type_html";
        }else if (fileName.toString().endsWith(".js")){
            return "type.js";
        }else if (fileName.toString().endsWith(".css")){
            return "type_css";
        }else if (fileName.toString().endsWith(".MF")){
            return "type_mf";
        }

        return null;
    }

    public static String[] getLogFiles() {
        List<LogFile> logFiles = new ArrayList<>();
        File logsFolder = new File(DProperties.getInstance().getLogsRoot());
        if (logsFolder.exists() && logsFolder.isDirectory()) {
            return logsFolder.list();
        }
        return null;
    }

    public static String getLogFile(String log) throws IOException {
        Path path = Paths.get(DProperties.getInstance().getLogsRoot() + "/" + log);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, "UTF-8");
    }
}
