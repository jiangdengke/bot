package love.forte.demo.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import love.forte.demo.listener.MyGroupRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DouyinUtil {
    private static final Logger log = LoggerFactory.getLogger(DouyinUtil.class);
    public  static String downAndgetVideoPath(String videoUrl) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss"); // 时间戳格式
        String timeStamp = dateFormat.format(new Date()); // 获取当前时间戳
        try {
            URL url = new URL(videoUrl);
            URLConnection connection = url.openConnection();
            int fileSize = connection.getContentLength();

            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());


            FileOutputStream out = new FileOutputStream(timeStamp+".mp4");
            byte[] data = new byte[1024];
            int bytesRead;
            int totalBytesRead = 0;

            while ((bytesRead = in.read(data, 0, 1024)) >= 0) {
                out.write(data, 0, bytesRead);
                totalBytesRead += bytesRead;
                int progress = (totalBytesRead * 100) / fileSize;
            }

            out.close();
            in.close();
            log.info("视频已保存"+timeStamp+".mp4");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return timeStamp+".mp4";
    }
    public static String getTitleAndVideoUrl(String aim){
        String requestUrl = "https://api.gumengya.com/Api/DouYin?format=json&url="+aim;
        // 发起 GET 请求
        HttpRequest request = HttpRequest.get(requestUrl);
        HttpResponse response = request.execute();
        // 获取响应结果
        String responseBody = response.body();
        JSONObject jsonObject = JSONUtil.parseObj(responseBody);
        String data = jsonObject.getStr("data");
        return data;
    }



}
