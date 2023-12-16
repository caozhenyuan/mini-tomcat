package server;

import java.io.IOException;

/**
 * @author czy
 * @date 2023/12/15
 **/
public interface Servlet {

    public void service(Request request, Response response) throws IOException;
}
