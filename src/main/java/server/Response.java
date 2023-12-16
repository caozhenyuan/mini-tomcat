package server;

import java.io.OutputStream;

/**
 * @author czy
 * @date 2023/12/14
 **/
public class Response {

    private Request request;

    private OutputStream output;


    public Response(OutputStream output) {
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public OutputStream getOutput() {
        return this.output;
    }
}
