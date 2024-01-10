package server;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: czy
 * @date: 2023/12/29 9:38
 */
public class ServletContainer {

    HttpConnector connector = null;

    ClassLoader loader = null;

    /**
     * 包含servlet类和实例的map
     */
    //servletName - ServletClassName
    Map<String, String> servletClsMap = new ConcurrentHashMap<>();

    //servletName - servlet
    Map<String, ServletWrapper> servletInstanceMap = new ConcurrentHashMap<>();



    public ServletContainer() {
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(HttpServer.WEB_ROOT);
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClassLoader getLoader() {
        return this.loader;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public HttpConnector getConnector() {
        return connector;
    }

    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    /**
     * 用于从map中找到相关的servlet，然后调用
     *
     * @param request request包装类
     * @param response response包装类
     */
    public void invoke(HttpRequest request, HttpResponse response) throws ServletException, IOException {
        ServletWrapper servletWrapper;
        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        String servletClassName = servletName;
        servletWrapper = servletInstanceMap.get(servletName);
        //如果容器内没有这个servlet，先要load类，创建新实例
        if (null == servletWrapper) {
            servletWrapper = new ServletWrapper(servletClassName, this);
            servletClsMap.put(servletName, servletClassName);
            servletInstanceMap.put(servletName, servletWrapper);

        }
        //response默认为UTF-8编码
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        //然后调用service()
        HttpRequestFacade requestFacade = new HttpRequestFacade(request);
        HttpResponseFacade responseFacade = new HttpResponseFacade(response);
        servletWrapper.invoke(requestFacade, responseFacade);
    }
}
