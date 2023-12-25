package server;

import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author czy
 * @date 2023/12/15
 * @Deprecated 专用于处理 Response 的返回值
 **/
public class StaticResourceProcessor {

    private static final int BUFFER_SIZE = 1024;

    //下面的字符串是当文件没有找到时返回的404错误描述
    private static String fileNotFoundMessage = "HTTP/1.1 404 File Not Found\r\n" + "Content-Type: text/html\r\n" + "Content-Length: 23\r\n" + "\r\n" + "File Not Found";
    //下面的字符串是正常情况下返回的，根据http协议，里面包含了相应的变量。
    private static String OKMessage = "HTTP/1.1 ${StatusCode} ${StatusName}\r\n" + "Content-Type: ${ContentType}\r\n" + "Content-Length: ${ContentLength}\r\n" + "Server: minit\r\n" + "Date: ${ZonedDateTime}\r\n" + "\r\n";



    /**
     * 处理静态资源
     *
     * @param request  request
     * @param response response
     * @throws IOException io异常
     */
    public void process(HttpRequest request, HttpResponse response) throws IOException {
        //处理过程很简单，先将响应头写入输出流，然后从文件中读取内容写入输出流
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        OutputStream output = null;
        try {
            output = response.getOutput();
            File file = new File(HttpServer.WEB_ROOT, request.getUri());
            if (file.exists()) {
                //拼响应头
                String head = composeResponseHead(file);
                output.write(head.getBytes(StandardCharsets.UTF_8));
                //读取文件内容
                fis = new FileInputStream(file);
                int ch = fis.read(bytes, 0, BUFFER_SIZE);
                while (ch != -1) {
                    output.write(bytes, 0, ch);
                    ch = fis.read(bytes, 0, BUFFER_SIZE);
                }
            } else {
                output.write(fileNotFoundMessage.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * 拼响应头，填充变量值
     *
     * @param file 静态文件
     * @return 响应头
     */
    private String composeResponseHead(File file) {
        long fileLength = file.length();
        Map<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("StatusCode", "200");
        valuesMap.put("StatusName", "OK");
        valuesMap.put("ContentType", "text/html;charset=utf-8");
        valuesMap.put("ContentLength", fileLength);
        valuesMap.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        return sub.replace(OKMessage);
    }
}

