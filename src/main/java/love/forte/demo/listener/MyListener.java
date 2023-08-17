package love.forte.demo.listener;



import catcode.CatCodeUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import kotlinx.coroutines.TimeoutCancellationException;
import love.forte.demo.service.impl.GetWeatherServiceImpl1;
import love.forte.demo.service.impl.TranslateServiceImpl;
import love.forte.demo.utils.DouyinUtil;
import love.forte.demo.utils.HttpUtil;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.FilterValue;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.ID;
import love.forte.simbot.LongID;
import love.forte.simbot.component.mirai.MiraiGroup;
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent;
import love.forte.simbot.component.mirai.message.*;
import love.forte.simbot.definition.Group;
import love.forte.simbot.event.*;
import love.forte.simbot.message.At;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import love.forte.simbot.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Component
public class MyListener {
    @Autowired
    private GetWeatherServiceImpl1 getWeatherService;
    @Autowired
    private TranslateServiceImpl translateService;
    @Autowired
    @Qualifier("redisTemplate2")
    private StringRedisTemplate stringRedisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MyListener.class);

    /**
     * 天气预报
     *
     * @param groupMessageEvent
     * @param city
     */
    @Listener
    @Filter(value = "{{city}}预报", matchType = MatchType.REGEX_MATCHES)
    public void myGroupListenerWeather(MiraiGroupMessageEvent groupMessageEvent, @FilterValue("city") String city) {
        JSONArray daily = getWeatherService.GetWeather(city);
        Group group = groupMessageEvent.getGroup();
        if (daily == null) {
            group.sendBlocking("没查到，可以考虑缩小或扩大范围");
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
            group.sendBlocking(build);
        }
    }

    /**
     * 翻译
     *
     * @param groupMessageEvent
     * @param word
     */
    @Listener
    @ContentTrim
    @Filter(value = "fy{{word}}", matchType = MatchType.REGEX_MATCHES)
    public void myGroupListenerChaxun(MiraiGroupMessageEvent groupMessageEvent, @FilterValue("word") String word) {
        String fanyi = translateService.getWord(word);
        fanyi = fanyi.replace("\"", "");
        String[] split = fanyi.split(",");
        StringBuilder sb = new StringBuilder();
        for (String explain : split) {
            sb.append(explain.trim()).append("\n");
        }
        String output = sb.toString().trim();
        Group group = groupMessageEvent.getGroup();
        group.sendBlocking(output);
    }



    /**
     * 舔狗日记
     */
    @Listener
    @ContentTrim
    @Filter(value = "舔狗日记", matchType = MatchType.REGEX_MATCHES, targets = @Filter.Targets(atBot = true))
    public void DogLickingDiary(MiraiGroupMessageEvent event) {
        String requestUrl = "https://v.api.aa1.cn/api/tiangou/";
        // 发起 GET 请求
        HttpRequest request = HttpRequest.get(requestUrl);
        HttpResponse response = request.execute();
        // 获取响应结果
        String responseBody = response.body();
        String substring = responseBody.substring(4, responseBody.length() - 4);
        event.getGroup().sendBlocking(substring);
    }
    /**
     * 点赞
     */
    @Listener
    @ContentTrim
    @Filter(value = "赞我",matchType = MatchType.REGEX_MATCHES)
    public void upvote(MiraiGroupMessageEvent event) throws MalformedURLException {
    }
    @Listener
    @ContentTrim
    @Filter(value = "qq点歌{{str}}",matchType = MatchType.REGEX_MATCHES)
    public void qqsong(MiraiGroupMessageEvent event,
                       @FilterValue("str") String str,ContinuousSessionContext sessionContext){
        String url = "http://ovoa.cc/api/QQmusic.php?msg="+str+"&type=&pages=20";
        String responseBody = HttpUtil.returnResponseBody(url);
        JSONObject jsonObject = JSONUtil.parseObj(responseBody);
        String content = jsonObject.getStr("content");
        String substring = content.substring(2, content.length() - 2);
        String result = substring.replaceAll("\"", "");
        String finalStr = result.replaceAll(",", "");

        final String qqId = String.valueOf(event.getAuthor().getId());
        LongID AuthorId = event.getAuthor().getId();
        LongID GroupId = event.getGroup().getId();
        String encode = URLUtil.encode(finalStr);
        String pictureUrl = "https://xiaobai.klizi.cn/API/tw/tw_bj.php?text="+encode+"&hh=";
        try {
            event.getGroup().sendBlocking("稍等...");
            event.getGroup().sendBlocking(MiraiSendOnlyImage.of(Resource.of(new URL(pictureUrl))));
        } catch (MalformedURLException e) {
            event.getGroup().sendBlocking("未能成功获取到图片");
            return;
        }
        try{
            sessionContext.waitingForNextMessage(qqId, MiraiGroupMessageEvent.Key, 59, TimeUnit.SECONDS, (e, c) -> {
                if (!(c.getAuthor().getId().equals(AuthorId) && c.getGroup().getId().equals(GroupId))) {
                    return false;
                }
                String trim = c.getMessageContent().getPlainText().trim();
                int parseInt=0;
                try{
                    parseInt = Integer.parseInt(trim);
                }catch (Exception e1){
                    event.getGroup().sendBlocking("输入非数字");
                    return false;
                }

                if(parseInt<=0||parseInt>= 20){
                    event.getGroup().sendBlocking("输入的数字不在范围内~~");
                    return true;
                }

                String requestUrl = "http://ovoa.cc/api/QQmusic.php?msg="+str+"&type=&n="+trim;
                String responseBody1 = HttpUtil.returnResponseBody(requestUrl);
                JSONObject jsonObject1 = JSONUtil.parseObj(responseBody1);
                String code = jsonObject1.getStr("code");
                if (!(code.equals("200"))){
                    return false;
                }
                String data = jsonObject1.getStr("data");
                JSONObject jsonObject2 = JSONUtil.parseObj(data);
                String src = jsonObject2.getStr("src");

                try {
                    event.getGroup().sendBlocking(new MiraiSendOnlyAudio(Resource.of(new URL(src))));
                } catch (MalformedURLException malformedURLException) {
                    event.getGroup().sendBlocking("发送语音失败");
                    return true;
                }
                return true;
            });
        }catch (TimeoutCancellationException e){
            event.getGroup().sendBlocking("超过59秒了喔！");
        }
    }
    @Listener
    @ContentTrim
    @Filter(value = "获得本群热度情况",matchType = MatchType.REGEX_MATCHES)
    public void getLiveness(MiraiGroupMessageEvent groupMessageEvent){
        String groupId = groupMessageEvent.getGroup().getId().toString();
        long startRank = 0; // 起始排名，0 表示第一名
        long endRank = 9;   // 终止排名，9 表示第十名

        Set<String> topTenMembers = stringRedisTemplate.opsForZSet().reverseRange(groupId, startRank, endRank);
        StringBuilder stringBuilder = new StringBuilder();
        // 获取当前时间
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年M月d日H点m分s秒");
        String formattedDateTime = currentDateTime.format(formatter);
        stringBuilder.append("--------------排行榜-------------")
                .append("------"+formattedDateTime+"------\n");
        if (topTenMembers != null) {
            int i = 1;
            for(String topTenMember:topTenMembers){
                String[] split = topTenMember.split(":");
                String nickName = split[1];
                Double score = stringRedisTemplate.opsForZSet().score(groupId, topTenMember);
                if (score != null) {
                    int intValue = score.intValue();
                    stringBuilder.append(i).append(":  ").append(nickName).append(" 发言").append(intValue).append("条").append("\n");
                }
                i+=1;
            }
        }
        String string = stringBuilder.toString();
        String encode = URLUtil.encode(string);
        String pictureUrl = "https://xiaobai.klizi.cn/API/tw/tw_bj.php?text="+encode+"&hh=";
        try {
            groupMessageEvent.getGroup().sendBlocking("稍等...");
            groupMessageEvent.getGroup().sendBlocking(MiraiSendOnlyImage.of(Resource.of(new URL(pictureUrl))));
        } catch (MalformedURLException e) {
            groupMessageEvent.getGroup().sendBlocking("未能成功获取到图片");
        }
    }


}
