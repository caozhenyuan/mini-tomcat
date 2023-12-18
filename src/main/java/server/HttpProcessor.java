package server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author czy
 * @date 2023/12/18
 * @Deprecated 负责处理逻辑，即调用 Servlet 并返回
 **/
public class HttpProcessor {

    public HttpProcessor() {

    }

    public void process(Socket socket) {
        InputStream input;
        OutputStream output;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            Request request = new Request(input);
            request.parse();

            Response response = new Response(output);
            response.setRequest(request);

            if (request.getUri().startsWith("/servlet/")) {
                ServletProcessor servletProcessor = new ServletProcessor();
                servletProcessor.process(request, response);
            } else {
                StaticResourceProcessor staticResourceProcessor = new StaticResourceProcessor();
                staticResourceProcessor.process(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
