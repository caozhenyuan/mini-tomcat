package server;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author czy
 * @date 2023/12/18
 * @Deprecated 负责处理逻辑，即调用 Servlet 并返回
 **/
public class HttpProcessor implements Runnable {

    Socket socket;
    private boolean available = false;

    private HttpConnector connector;

    private int serverPort = 0;

    private boolean keepAlive = false;

    private boolean http11 = true;

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    public void process(Socket socket) {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        InputStream input;
        OutputStream output;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();

            keepAlive = true;
            while (keepAlive) {
                HttpRequest request = new HttpRequest(input);
                request.parse(socket);

                //handle session
                if (StringUtils.isBlank(request.getSessionId())) {
                    request.getSession(true);
                }

                HttpResponse response = new HttpResponse(output);
                response.setRequest(request);

                request.setResponse(response);

                try {
                    response.sendHeaders();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                if (request.getUri().startsWith("/servlet/")) {
                    ServletProcessor servletProcessor = new ServletProcessor(this.connector);
                    servletProcessor.process(request, response);
                } else {
                    StaticResourceProcessor staticResourceProcessor = new StaticResourceProcessor();
                    staticResourceProcessor.process(request, response);
                }
                finishResponse(response);
                System.out.println("response header connection------" + response.getHeader("Connection"));
                if ("close".equals(response.getHeader("Connection"))) {
                    keepAlive = false;
                }
            }
            // 关闭 socket
            socket.close();
            socket = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishResponse(HttpResponse response) {
        response.finishResponse();
    }

    @Override
    public void run() {
        while (true) {
            // 等待分配下一个 socket
            Socket socket = await();
            System.out.println(socket);
            if (null == socket) {
                continue;
            }
            // 处理来自这个socket的请求
            process(socket);
            // 完成此请求
            connector.recycle(this);
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Connector 分配 Socket 给 Processor
     *
     * @param socket socket链接
     */
    synchronized void assign(Socket socket) {
        // 等待 connector 提供新的 Socket
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 存储新可用的 Socket 并通知我们的线程
        this.socket = socket;
        available = true;
        notifyAll();
    }

    private synchronized Socket await() {
        //等待 connector 提供一个新的 Socket
        while (!available) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //通知Connector我们已经收到了这个Socket了
        Socket socket = this.socket;
        available = false;
        notifyAll();
        return socket;
    }
}
