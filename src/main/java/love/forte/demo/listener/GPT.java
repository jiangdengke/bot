package love.forte.demo.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import love.forte.demo.openai.GptUtil;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.ID;
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent;
import love.forte.simbot.event.EventResult;
import love.forte.simbot.message.Message;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import love.forte.simbot.message.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GPT {
    private static final Logger logger = LoggerFactory.getLogger(GPT.class);
    @Autowired
    @Qualifier("redisTemplate0")
    private StringRedisTemplate stringRedisTemplate0;
    @Autowired
    @Qualifier("redisTemplate3")
    private StringRedisTemplate stringRedisTemplate3;

    @Listener(priority = 1)
    @ContentTrim
    @Filter(value = "#开始对话", matchType = MatchType.REGEX_MATCHES)
    public EventResult startGPT(MiraiGroupMessageEvent event) {
        String AuthorId = event.getAuthor().getId().toString();
        Object gpt = stringRedisTemplate0.opsForHash().get("GPT", AuthorId);
        if (gpt==null){
            event.sendBlocking("检测到你初次使用GPT,正在为您初始化");
            stringRedisTemplate0.opsForHash().put("GPT",AuthorId,"continue");
            event.getBot().delay(Duration.ofSeconds(3),()->{
                event.sendBlocking("初始化完成，请您重新启动GPT");
            });
            return EventResult.truncate();
        }
        if (gpt.equals("continue")) {
            event.replyBlocking("您已开启过GPT，别重复开");
        }else if(gpt.equals("uncontinue")){
            stringRedisTemplate0.opsForHash().put("GPT", AuthorId, "continue");//开启
            event.replyBlocking("已帮您开启GPT");
        }
        return EventResult.truncate();
    }

    @Listener(priority = 2)
    @ContentTrim
    public void listenGPT(MiraiGroupMessageEvent event) throws JsonProcessingException {
        String AuthorId = event.getAuthor().getId().toString();
        Object gpt = stringRedisTemplate0.opsForHash().get("GPT", AuthorId);//
        ObjectMapper objectMapper = new ObjectMapper();
        if (gpt != null && gpt.equals("continue")) {
            for (Message.Element<?> message : event.getMessageContent().getMessages()) {
                if (message instanceof Text) {
                    String text = ((Text) message).getText();
                    love.forte.demo.openai.Message message1 = new love.forte.demo.openai.Message("user",text);
                    stringRedisTemplate3.opsForZSet().add(AuthorId,objectMapper.writeValueAsString(message1),System.currentTimeMillis());//存入Redis
                    //再把对话上下文拿出来
                    Set<String> zsetMembers = stringRedisTemplate3.opsForZSet().range(AuthorId, 0, -1);
                    List<love.forte.demo.openai.Message> zsetMembersList = new ArrayList<>();
                    if (zsetMembers != null) {
                        for(String str:zsetMembers){
                            love.forte.demo.openai.Message deserializedMessage = objectMapper.readValue(str, love.forte.demo.openai.Message.class);
                            zsetMembersList.add(deserializedMessage);
                        }
                    }
                    if (zsetMembersList.size()>40){//消息超过40条，gpt就不能联系上下文了，且不会返回回复
                        MessagesBuilder messagesBuilder = new MessagesBuilder();
                        Messages build = messagesBuilder.at(ID.$(AuthorId)).build();
                        event.sendBlocking(build+"检测到您已进行近40条对话，为保障质量，现将您的对话重置");
                        zsetMembersList.clear();//清空list
                        stringRedisTemplate3.opsForZSet().removeRange(AuthorId,0,-1);
                        event.getBot().delay(Duration.ofSeconds(2),()->{
                            event.sendBlocking("重置成功");
                        });
                        return;
                    }
                    GptUtil gptUtil = new GptUtil();
                    String replyMessage = gptUtil.Reply(zsetMembersList);
                    event.replyBlocking(replyMessage);
                    love.forte.demo.openai.Message message2 = new love.forte.demo.openai.Message("assistant",replyMessage);
                    stringRedisTemplate3.opsForZSet().add(AuthorId,objectMapper.writeValueAsString(message2),System.currentTimeMillis());
                }
            }
        }
    }
    @Listener(priority = 1)
    @ContentTrim
    @Filter(value = "#结束对话", matchType = MatchType.REGEX_MATCHES)
    public void finishGPT(MiraiGroupMessageEvent event){
        String AuthorId = event.getAuthor().getId().toString();
        Object gpt = stringRedisTemplate0.opsForHash().get("GPT", AuthorId);
        if (gpt.equals("uncontinue")){
            event.replyBlocking("您已退出GPT,无需再次退出");
        }else if(gpt.equals("continue")){
            stringRedisTemplate0.opsForHash().put("GPT",AuthorId,"uncontinue");
            event.replyBlocking("已帮您退出GPT!");
        }
    }
    @Listener(priority = 0)
    @ContentTrim
    @Filter(value = "#结束所有对话",matchType = MatchType.REGEX_MATCHES)
    public void finishAllGPT(MiraiGroupMessageEvent event){
        String AuthorId = event.getAuthor().getId().toString();
        if(AuthorId.equals("1728439852")){
            Map<Object, Object> hashEntries = stringRedisTemplate0.opsForHash().entries("GPT");
            int i = 0;
            for (Object hashKey : hashEntries.keySet()) {
                Object gpt = stringRedisTemplate0.opsForHash().get("GPT", hashKey);
                if (gpt != null && gpt.equals("continue")) {
                    i += 1;
                }
                stringRedisTemplate0.opsForHash().put("GPT", hashKey, "uncontinue");
            }
            event.replyBlocking("已结束"+i+"人的对话");
        }else {
            event.replyBlocking("您没有该权限");
        }
    }
}
