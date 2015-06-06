package ro.appreader;

import org.codehaus.jackson.map.ObjectMapper;
import ro.common.AppFile;
import ro.common.JSTreeNode;
import ro.common.LogFile;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by diana on 14.04.2015.
 */
public class ReaderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String list = req.getParameter("list");
        String get = req.getParameter("get");
        String logs = req.getParameter("logs");
        String log = req.getParameter("log");


        if (list != null){
            List<JSTreeNode> jsTreeNodes = ReaderUtils.getFiles(list);
            try(ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream())){
                oos.writeObject(jsTreeNodes);
            }
        } else if (get != null){
            if (get.endsWith(".class")) {
                ReaderUtils.decompile(get, resp.getWriter());
            } else {
                ReaderUtils.getContent(get, resp.getOutputStream());
            }
        } else if (logs != null){
            String[] logFiles = ReaderUtils.getLogFiles();
            resp.getWriter().write(new ObjectMapper().writeValueAsString(logFiles));
        } else if (log != null){
            String logFiles = ReaderUtils.getLogFile(log);
            resp.getWriter().write(logFiles);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try(InputStream inputStream = req.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(inputStream);){
            AppFile obj = (AppFile) ois.readObject();
            if (obj.getUrl().endsWith(".class")){
                String result = compileClassFile(obj.getUrl(), new String(obj.getContent()));
                resp.getWriter().write(result);
            } else {
                writeFile(obj);
            }
        }catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String writeFile(AppFile obj) {
        DProperties dProperties = DProperties.getInstance();
        String rootPath = dProperties.getRoot();
        String filePath = rootPath + obj.getUrl();

        String tempFilePath = filePath.substring(0, filePath.replace("\\", "/").lastIndexOf('/'));
        File tempFile = new File(tempFilePath + "/" + UUID.randomUUID().toString());
        File newFile;
        try(BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(tempFile))){
            bw.write(obj.getContent());

            // e gata, e bine
            newFile = new File(filePath);

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        if (!newFile.delete()){
            return ("Nu am putut sterge fisierul");
        }

        if (!tempFile.renameTo(newFile)) {
            return ("Nu am putut redenumi fisierul");
        }
        return null;
    }

    private String compileClassFile(String filepath, String content) throws IOException {
        DProperties dProperties = DProperties.getInstance();
        String rootPath = dProperties.getRoot();
        String javaFilePath = createJavaFile(rootPath + filepath, content);
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> optionList = new ArrayList<String>();
        String appName = filepath.substring(1, filepath.indexOf('/', 1));

        String libPath = dProperties.getRoot() + "/" + appName + "/WEB-INF/lib/*";
        String classesPath = dProperties.getRoot() + "/" + appName + "/WEB-INF/classes/";
        String tomcatLibPath = rootPath.substring(0, rootPath.lastIndexOf('/')) + "/lib/*";

        String classPath = buildClassPath(libPath , classesPath, tomcatLibPath);
        optionList.add("-classpath");
        optionList.add(classPath);
        StandardJavaFileManager standardJavaFileManager = compiler.getStandardFileManager(null, null, null);
        File[] files = new File[]{new File(javaFilePath)};

        StringWriter sw = new StringWriter();
        JavaCompiler.CompilationTask task = compiler.getTask(sw, standardJavaFileManager, null, optionList, null, standardJavaFileManager.getJavaFileObjects(files));

        task.call();
        sw.close();
        //sterg .java file
        File deleteFile = new File(javaFilePath);
        if (deleteFile.delete()){
            System.out.println("Fisierul a fost sters");
        } else {
            System.out.println("Eroare la stergere");
        }
        return sw.toString();
    }

    private String createJavaFile(String path, String content) throws IOException {
        String javaPath = path.substring(0,path.lastIndexOf(".")) + ".java";
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(javaPath))){
            bw.write(content);
        }
        return javaPath;
    }


    private static String buildClassPath(String... paths) {
        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (path.endsWith("*")) {
                path = path.substring(0, path.length() - 1);
                File pathFile = new File(path);
                for (File file : pathFile.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        sb.append(path);
                        sb.append(file.getName());
                        sb.append(System.getProperty("path.separator"));
                    }
                }
            } else {
                sb.append(path);
                sb.append(System.getProperty("path.separator"));
            }
        }
        return sb.toString();
    }
}
