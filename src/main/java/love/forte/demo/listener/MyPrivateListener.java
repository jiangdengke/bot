package love.forte.demo.listener;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import love.forte.demo.service.impl.GetWeatherServiceImpl1;
import love.forte.demo.service.impl.TranslateServiceImpl;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.FilterValue;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.component.mirai.MiraiFriend;
import love.forte.simbot.component.mirai.event.MiraiFriendMessageEvent;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyPrivateListener {
    @Autowired
    private GetWeatherServiceImpl1 getWeatherService;
    @Autowired
    private TranslateServiceImpl translateService;

    /**
     * 天气预报
     * @param friendMessageEvent
     * @param city
     */
    @Listener
    @Filter(value = "{{city}}预报", matchType = MatchType.REGEX_MATCHES)
    public void myGroupListenerWeather(MiraiFriendMessageEvent friendMessageEvent, @FilterValue("city") String city) {
        JSONArray daily = getWeatherService.GetWeather(city);
        MiraiFriend friend = friendMessageEvent.getFriend();
        if (daily == null) {
            friend.sendAsync("没查到，应该是不支持该地区或者数据库服务没开");
            return;
        }
        if (daily == null) {
            friend.sendAsync("暂不支持查询该地方的天气");
            return;
        }
        for (int i = 0; i < daily.size(); i++) {
            JSONObject dailyJSONObject0 = daily.getJSONObject(i);
            String fxDate = dailyJSONObject0.getStr("fxDate");
            String sunrise = dailyJSONObject0.getStr("sunrise");
            String sunset = dailyJSONObject0.getStr("sunset");
            String tempMax = dailyJSONObject0.getStr("tempMax");
            String tempMin = dailyJSONObject0.getStr("tempMin");
            String textDay = dailyJSONObject0.getStr("textDay");
            String textNight = dailyJSONObject0.getStr("textNight");
            String windScaleDay = dailyJSONObject0.getStr("windScaleDay");
            String windDirDay = dailyJSONObject0.getStr("windDirDay");
            String wind360Day = dailyJSONObject0.getStr("wind360Day");
            String windSpeedDay = dailyJSONObject0.getStr("windSpeedDay");
            String windScaleNight = dailyJSONObject0.getStr("windScaleNight");
            String windDirNight = dailyJSONObject0.getStr("windDirNight");
            String wind360Night = dailyJSONObject0.getStr("wind360Night");
            String windSpeedNight = dailyJSONObject0.getStr("windSpeedNight");
            String humidity = dailyJSONObject0.getStr("humidity");
            String pressure = dailyJSONObject0.getStr("pressure");
            String vis = dailyJSONObject0.getStr("vis");
            MessagesBuilder messagesBuilder = new MessagesBuilder();
            Messages build = messagesBuilder.text("--------" + fxDate + "-------\n" +
                    "日出时间:" + sunrise + "  日落时间:" + sunset + "\n" +
                    "最高气温: " + tempMax + "°C  最低气温: " + tempMin + "°C \n" +
                    "日间天气: " + textDay + "  夜间天气: " + textNight + "\n" +
                    "日间风力:\n" + windScaleDay + "级" + windDirDay + "-" + wind360Day + "° " + windSpeedDay + "m/s\n" +
                    "夜间风力:\n" + windScaleNight + "级" + windDirNight + "-" + wind360Night + "° " + windSpeedNight + "m/s\n" +
                    "相对湿度: " + humidity + " %\n" +
                    "大气压强: " + pressure + " 百帕\n" +
                    "能见度: " + vis + " 公里").build();
            friend.sendBlocking(build);
        }
    }

    /**
     * 翻译
     * @param friendMessageEvent
     * @param word
     */
    @Listener
    @ContentTrim
    @Filter(value = "fy{{word}}", matchType = MatchType.REGEX_MATCHES)
    public void myPrivateChaxun(MiraiFriendMessageEvent friendMessageEvent, @FilterValue("word") String word) {
        String fanyi = translateService.getWord(word);
        fanyi = fanyi.replace("\"", "");
        String[] split = fanyi.split(",");
        StringBuilder sb = new StringBuilder();
        for (String explain : split) {
            sb.append(explain.trim()).append("\n");
        }
        String output = sb.toString().trim();
        MiraiFriend friend = friendMessageEvent.getFriend();
        friend.sendAsync(output);
    }
    /**
     * 定时任务
     */

}