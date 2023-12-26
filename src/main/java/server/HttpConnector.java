package server;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author czy
 * @date 2023/12/18
 * @Deprecated 实现接收网络连接和返回
 **/
public class HttpConnector implements Runnable {

    int minProcessors = 3;

    int maxProcessors = 10;

    int curProcessors = 0;


    /**
     * 存放多个processors的池子
     */
    private Deque<HttpProcessor> processors = new ArrayDeque<>();

    public static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

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
        //initialize processors poll
        for (int i = 0; i < minProcessors; i++) {
            HttpProcessor initProcessor = new HttpProcessor(this);
            initProcessor.start();
            processors.push(initProcessor);
        }
        curProcessors = minProcessors;

        while (true) {
            Socket socket;
            try {
                //接受请求链接,每一个连接生成一个 socket
                socket = serverSocket.accept();
                HttpProcessor processor = createProcessor();
                if (null == processor) {
                    socket.close();
                    continue;
                }
                //分配给这个processor
                processor.assign(socket);
                //处理完毕后放回池子
                processors.push(processor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private HttpProcessor createProcessor() {
        //从池子中获取一个processor，如果池子为空且小于最大限制，则新建一个
        synchronized (processors) {
            if (!processors.isEmpty()) {
                return processors.pop();
            }
            if (curProcessors < maxProcessors) {
                return newProcessor();
            } else {
                return null;
            }
        }
    }

    /**
     * 新建一个HttpProcessor到池子中
     *
     * @return HttpProcessor
     */
    private HttpProcessor newProcessor() {
        HttpProcessor processor = new HttpProcessor(this);
        processors.push(processor);
        curProcessors++;
        return processors.pop();
    }

    void recycle(HttpProcessor processor) {
        processors.push(processor);
    }

    /**
     * 新建一个session
     *
     * @return session
     */
    public static Session createSession() {
        Session session = new Session();
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        String sessionId = generateSessionId();
        session.setSessionId(sessionId);
        sessions.put(sessionId, session);
        return session;
    }

    /**
     * 生成sessionId
     *
     * @return sessionId
     */
    protected static synchronized String generateSessionId() {
        Random random = new Random();
        long seed = System.currentTimeMillis();
        random.setSeed(seed);
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            byte b1 = (byte) ((aByte & 0xf0) >> 4);
            byte b2 = (byte) (aByte & 0x0f);
            if (b1 < 10)
                result.append((char) ('0' + b1));
            else
                result.append((char) ('A' + (b1 - 10)));
            if (b2 < 10)
                result.append((char) ('0' + b2));
            else
                result.append((char) ('A' + (b2 - 10)));
        }
        return (result.toString());
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
