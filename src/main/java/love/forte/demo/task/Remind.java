package love.forte.demo.task;

import love.forte.demo.listener.MyListener;
import love.forte.demo.utils.BotUtil;
import love.forte.simbot.ID;
import love.forte.simbot.component.mirai.bot.MiraiBot;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class Remind {
    @Autowired
    @Qualifier("redisTemplate1")
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private BotUtil botUtil;
    private static final Logger logger = LoggerFactory.getLogger(MyListener.class);
    //每一秒查询redis
    @Scheduled(cron="0/1 * * * * ?")
    public void queryredis(){
        MiraiBot bot = botUtil.getBot();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmm"); // 时间戳格式
        String nowtime = dateFormat.format(new Date()); // 获取当前时间戳


        Set<String> keys = stringRedisTemplate.keys("*");

        if (keys != null) {
            for (String key:keys){
                Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
                for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                    Object field = entry.getKey();
                    Object value = entry.getValue();
                    String strField=(String)field;
                    String strValue=(String)value;
                    int i = strField.compareTo(nowtime);
                    if (i<0){
                        stringRedisTemplate.opsForHash().delete(key,field);
                    } else if(i==0){
                        String[] split = key.split(":");
                        String groupId = split[0];
                        String AuthorId = split[1];
                        MessagesBuilder messagesBuilder = new MessagesBuilder();
                        Messages build = messagesBuilder.at(ID.$(AuthorId)).append("提醒:\n   " + strValue).build();
                        try {
                            bot.getGroup(ID.$(groupId)).sendAsync(build);
                        }catch (NullPointerException e){
                            logger.info("发送提醒异常");
                        }finally {
                            stringRedisTemplate.opsForHash().delete(key,field);
                        }

                    }
                }
            }
        }
    }
}
