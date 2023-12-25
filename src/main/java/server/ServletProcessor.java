package server;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;

/**
 * @author czy
 * @date 2023/12/16
 **/
public class ServletProcessor {

    public void process(HttpRequest request, HttpResponse response) {
        //首先根据uri最后一个/号来定位，后面的字符串认为是servlet名字
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader = null;
        try {
            //create a URLClassLoader
            //规定一个目录，让将 Servlet 放到这个目录下。为了将这些应用程序类和服务器自身的类分开，引入一个 URLClassLoader 来进行加载
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(HttpServer.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //response默认为UTF-8编码
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        //由上面的URLClassLoader加载这个Servlet
        Class<?> servletClass = null;
        try {
            servletClass = loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //回写头信息
        try {
            response.sendHeaders();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //创建servlet新实例，然后调用service()，由它来写动态内容到响应体
        Servlet servlet;
        try {
            servlet = (Servlet) servletClass.newInstance();
            HttpRequestFacade requestFacade = new HttpRequestFacade(request);
            HttpResponseFacade responseFacade = new HttpResponseFacade(response);
            servlet.service(requestFacade, responseFacade);
        } catch (InstantiationException | IllegalAccessException | IOException | ServletException e) {
            e.printStackTrace();
        }
    }
}
