package server;

import java.io.File;

/**
 * @author czy
 * @date 2023/12/14
 **/
public class HttpServer {

    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "/src/webroot";

    public static void main(String[] args) {
        HttpConnector connector = new HttpConnector();
        connector.start();
    }
}
