package love.forte.demo.service;

import cn.hutool.json.JSONArray;

public interface GetWeatherService {
    JSONArray GetWeather(String city);
    String GetNowWeather(String city);
}
