package love.forte.demo.listener;


import love.forte.demo.service.impl.TranslateServiceImpl;
import love.forte.demo.utils.AIPaintingUtil;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.FilterValue;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent;
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImage;
import love.forte.simbot.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Locale;
import java.util.ResourceBundle;


@Component
public class AIPainting {
    private static final Logger logger = LoggerFactory.getLogger(AIPainting.class);
    @Autowired
    @Qualifier("redisTemplate0")
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TranslateServiceImpl translateService;
    /**
     * 涩图功能
     */
    @Listener
    @Filter(value = "#画图{{str}}", matchType = MatchType.REGEX_MATCHES)
    @ContentTrim
    public void getPicture(MiraiGroupMessageEvent event,@FilterValue("str") String str) {
        logger.info("收到绘画请求");
        String aiPainting = stringRedisTemplate.opsForValue().get("AIPainting");
        if (aiPainting.equals("uncompleted")){
            logger.info("检测到上次绘画仍未完成");
            event.replyBlocking("请让我先画完上一个！");
            return;
        }
        stringRedisTemplate.opsForValue().set("AIPainting","uncompleted");
        if(str.isEmpty()){
            return;
        }
        event.getBot().delay(Duration.ofSeconds(5),()->{
            event.replyBlocking("我正在画，请稍等");
        });
        String prompt = translateService.fanyi(str);
        System.out.println(prompt);
        boolean bool = AIPaintingUtil.textToimage(prompt);
        if(!bool){
            event.replyBlocking("生成图片失败");
        }else {
            Path localpath = Paths.get(  "output.png");
            MiraiSendOnlyImage image = MiraiSendOnlyImage.of(Resource.of(localpath));
            event.replyBlocking(image);
        }
        stringRedisTemplate.opsForValue().set("AIPainting","completed");
    }
}

