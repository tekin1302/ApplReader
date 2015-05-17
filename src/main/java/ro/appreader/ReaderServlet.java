package ro.appreader;

import ro.common.JSTreeNode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by diana on 14.04.2015.
 */
public class ReaderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String list = req.getParameter("list");
        String get = req.getParameter("get");
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
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

}