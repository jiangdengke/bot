package love.forte.demo.openai;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;


public class GptUtil{

    private static final String URL = "https://api.closeai-proxy.xyz/v1/chat/completions";
    @Value("${closeAI.key}")
    private String  API_KEY;
    public  String Reply(List<Message> list){

        RequestBody requestBody = new RequestBody();
        requestBody.setModel("gpt-3.5-turbo");
        requestBody.setMessages(list);

        String data = JSON.toJSONString(requestBody);

        String resJSON = HttpRequest.post(URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .body(data)
                .execute()
                .body();

        ResponseBody body = JSON.parseObject(resJSON, ResponseBody.class);
        String content = body.getChoices().get(0).getMessage().getContent();

        return content;
    }

}
