package server;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author: czy
 * @date: 2023/12/29 9:38
 * @Deprecated 包装类，管理Servlet的生命周期
 */
public class ServletWrapper {

    private Servlet instance = null;

    private String servletClass;

    private ClassLoader loader;

    private String name;

    protected ServletContainer parent = null;


    public ServletWrapper(String servletClass, ServletContainer parent) {
        this.parent = parent;
        this.servletClass = servletClass;
        try {
            loadServlet();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }

    public Servlet loadServlet() throws ServletException {
        if (null != instance) {
            return instance;
        }
        Servlet servlet = null;
        String actualClass = servletClass;
        if (null == actualClass) {
            throw new ServletException("servlet class has not been specified");
        }
        ClassLoader classLoader = getLoader();
        Class<?> classClass = null;
        if (null != classLoader) {
            try {
                classClass = classLoader.loadClass(actualClass);
            } catch (ClassNotFoundException e) {
                throw new ServletException("Servlet class not found");
            }
        }
        try {
            servlet = (Servlet) classClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServletException("Failed to instantiate servlet");
        }
        try {
            servlet.init(null);
        } catch (Throwable f) {
            throw new ServletException("Failed initialize servlet.");
        }
        instance = servlet;
        return servlet;
    }

    public ClassLoader getLoader() {
        if (loader != null) {
            return loader;
        }
        return parent.getLoader();
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    public ServletContainer getParent() {
        return parent;
    }

    public void setParent(ServletContainer container) {
        parent = container;
    }

    public Servlet getServlet() {
        return this.instance;
    }

    public void invoke(HttpRequestFacade requestFacade, HttpResponseFacade responseFacade) throws ServletException, IOException {
        if (null != instance) {
            instance.service(requestFacade,responseFacade);
        }
    }
}
