package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author czy
 * @date 2023/12/18
 * @Deprecated 实现接收网络连接和返回
 **/
public class HttpConnector implements Runnable {

    @Override
    public void run() {
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
            try {
                //接受请求链接,每一个连接生成一个 socket
                socket = serverSocket.accept();
                HttpProcessor httpProcessor = new HttpProcessor();
                httpProcessor.process(socket);
                //关闭连接
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
