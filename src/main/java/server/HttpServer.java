package server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author czy
 * @date 2023/12/14
 **/
public class HttpServer {

    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "/src/webroot";

    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.await();
    }

    /**
     * 服务器循环等待请求并处理
     */
    private void await() {
        ServerSocket serverSocket = null;
        int port = 8080;

        try {
            //启动了一个 ServerSocket 接收客户端的请求
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (true) {
            Socket socket;
            InputStream input;
            OutputStream output;

            try {
                //接受请求链接,每一个连接生成一个 socket
                socket = serverSocket.accept();
                input = socket.getInputStream();
                output = socket.getOutputStream();
                //创建Request对象来解析uri
                Request request = new Request(input);
                request.parse();
                //创建Response对象进行输出
                Response response = new Response(output);
                response.setRequest(request);

                if (request.getUri().startsWith("/servlet/")) {
                    ServletProcessor servletProcessor = new ServletProcessor();
                    servletProcessor.process(request, response);
                } else {
                    StaticResourceProcessor staticResourceProcessor = new StaticResourceProcessor();
                    staticResourceProcessor.process(request, response);
                }
                //关闭连接
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
