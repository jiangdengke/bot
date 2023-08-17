package love.forte.demo.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class AIPaintingUtil {
    public static boolean textToimage(String prompt) {
        String url = "http://127.0.0.1:7860/sdapi/v1/txt2img";

        JSONObject payload = JSONUtil.createObj()
                .put("prompt", prompt)
                .put("negative_prompt","(worst quality:2),(low quality:2),(normal quality:2),lowres,watermark,badhandv4,ng_deepnegative_v1_75t,")
                .put("width", "848")
                .put("height", "848")
                .put("sampler_index", "Euler")
                .put("steps",30);

        // 发送HTTP POST请求
        String responseJson = HttpRequest.post(url).body(payload.toString()).execute().body();

        // 解析响应JSON
        JSONObject jsonResponse = JSONUtil.parseObj(responseJson);
        JSONArray imagesArray = jsonResponse.getJSONArray("images");
        if (imagesArray==null){
            return false;
        }else {
            for (Object imageBase64 : imagesArray) {
                String imageData = imageBase64.toString().split(",", 2)[0];
                byte[] imageBytes = Base64.decode(imageData);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

                try {
                    BufferedImage image = ImageIO.read(inputStream);
                    File outputImageFile = new File("output.png");
                    ImageIO.write(image, "png", outputImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IoUtil.close(inputStream);
                }
            }
            return true;
        }

    }
}
