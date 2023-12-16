package test;

import server.Request;
import server.Response;
import server.Servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author czy
 * @date 2023/12/16
 **/
public class HelloServlet implements Servlet {
    @Override
    public void service(Request request, Response response) throws IOException {
        String doc = "<!DOCTYPE html> \n" + "<html>\n" +
                "<head><meta charset=\"utf-8\"><title>Test</title></head>\n" +
                "<body bgcolor=\"#f0f0f0\">\n" + "<h1 align=\"center\">" +
                "Hello World 你好" + "</h1>\n";
        response.getOutput().write(doc.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) {
        System.out.println("111");
    }
}
