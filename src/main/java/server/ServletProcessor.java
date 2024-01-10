package server;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author czy
 * @date 2023/12/16
 **/
public class ServletProcessor {

    private HttpConnector connector;

    public ServletProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    public void process(HttpRequest request, HttpResponse response) throws ServletException, IOException {
        this.connector.getContainer().invoke(request, response);
    }
}
