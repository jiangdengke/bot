package love.forte.demo.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import love.forte.demo.mapper.WeatherMapper;
import love.forte.demo.service.GetWeatherService;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GetWeatherServiceImpl1 implements GetWeatherService {
    @Resource
    private WeatherMapper weatherMapper;

    @Value("${hefengWeatherKey}")
    private String hefengWeatherKey;

    @Override
    public JSONArray GetWeather(String city) {
        String location = weatherMapper.getCityLocation(city);
        if(location==null){
            return null;
        }
        String requestUrl = "https://devapi.qweather.com/v7/weather/3d?location="+location+"&key="+hefengWeatherKey;
        // 发起 GET 请求
        HttpRequest request = HttpRequest.get(requestUrl);
        HttpResponse response = request.execute();

        // 获取响应结果
        String responseBody = response.body();

        // 将 JSON 字符串解析为 JSON 对象
        JSONObject jsonObject = JSONUtil.parseObj(responseBody);
        JSONArray daily = jsonObject.getJSONArray("daily");
        return daily;
    }

    @Override
    public String GetNowWeather(String city) {
        return null;
    }
}
