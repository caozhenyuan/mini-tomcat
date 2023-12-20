package server;

import org.apache.commons.lang3.text.StrSubstitutor;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author czy
 * @date 2023/12/16
 **/
public class ServletProcessor {
    //响应头定义，里面包含变量
    private static String OKMessage = "HTTP/1.1 ${StatusCode} ${StatusName}\r\n" + "Content-Type: ${ContentType}\r\n" + "Server: minit\r\n" + "Date: ${ZonedDateTime}\r\n" + "\r\n";


    public void process(HttpRequest request, Response response) {
        //首先根据uri最后一个/号来定位，后面的字符串认为是servlet名字
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader = null;
        PrintWriter writer = null;

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

        //获取PrintWrite
        try {
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            writer = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //由上面的URLClassLoader加载这个Servlet
        Class<?> servletClass;
        try {
            servletClass = loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        //生成返回响应头
        String head = composeResponseHead();
        writer.println(head);

        //创建servlet新实例，然后调用service()，由它来写动态内容到响应体
        Servlet servlet;
        try {
            servlet = (Servlet) servletClass.newInstance();
            servlet.service(request, response);
        } catch (InstantiationException | IllegalAccessException | IOException | ServletException e) {
            e.printStackTrace();
        }
    }

    //生成响应头，填充变量值
    private String composeResponseHead() {
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("StatusCode", "200");
        valuesMap.put("StatusName", "OK");
        valuesMap.put("ContentType", "text/html;charset=uft-8");
        valuesMap.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        return sub.replace(OKMessage);
    }
}
