package love.forte.demo;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import love.forte.demo.utils.DouyinUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class test {
    @Test
    public void test1(){
        String requestUrl = "https://v.api.aa1.cn/api/tiangou/";
        // 发起 GET 请求
        HttpRequest request = HttpRequest.get(requestUrl);
        HttpResponse response = request.execute();
        // 获取响应结果
        String responseBody = response.body();
        // 将 JSON 字符串解析为 JSON 对象
        JSONObject jsonObject = JSONUtil.parseObj(responseBody);
        JSONArray newslist = jsonObject.getJSONArray("newslist");
        System.out.println(newslist);
    }
}
