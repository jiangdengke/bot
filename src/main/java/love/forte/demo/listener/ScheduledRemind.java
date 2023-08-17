package love.forte.demo.listener;

import kotlinx.coroutines.TimeoutCancellationException;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.FilterValue;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.ID;
import love.forte.simbot.LongID;
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent;
import love.forte.simbot.event.ContinuousSessionContext;
import love.forte.simbot.message.At;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduledRemind {
    @Autowired
    @Qualifier("redisTemplate1")
    private StringRedisTemplate stringRedisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ScheduledRemind.class);

    /**
     * 定时提醒功能
     */
    @Listener
    @ContentTrim
    @Filter(value = "#提醒我{{str}}", matchType = MatchType.REGEX_MATCHES)
    public void ScheduledTask(MiraiGroupMessageEvent groupMessageEvent, ContinuousSessionContext context,
                              @FilterValue("str") String str) {
        final String qqId = String.valueOf(groupMessageEvent.getAuthor().getId());
        LongID AuthorId = groupMessageEvent.getAuthor().getId();
        LongID GroupId = groupMessageEvent.getGroup().getId();
        final  String groupCode = String.valueOf(GroupId);
        //这个MessagesBuilder是为了生成at+一个ok表情的信息
        MessagesBuilder messagesBuilder1 = new MessagesBuilder();
        Messages atAndok = messagesBuilder1.at(AuthorId)
                .face(ID.$(124)).build();


        groupMessageEvent.replyBlocking("请告诉我你需要设置提醒的时间。\n例如:10010000,代表10月一日凌晨00:00");
        try {
            context.waitingForNextMessage(qqId, MiraiGroupMessageEvent.Key, 40, TimeUnit.SECONDS, (e, c) -> {
                if (!(c.getAuthor().getId().equals(AuthorId) && c.getGroup().getId().equals(GroupId))) {
                    return false;
                }
                final var messageContent = c.getMessageContent();
                String time = messageContent.getPlainText().trim();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmm"); // 时间戳格式
                String orderTime = dateFormat.format(new Date()); // 获取当前时间戳
                int i = time.compareTo(orderTime);
                boolean ff = isTime(time, "MMddHHmm");//对时间进行判断，看是否符合格式
                if (!ff) {
                    groupMessageEvent.getGroup().sendBlocking("时间格式不正确,请重新输入提醒");
                    return true;
                }
                if (i < 0 || (Integer.parseInt(time) >= 12320000)) {//保证时间永远不超过12月31日23点59
                    groupMessageEvent.getGroup().sendBlocking("这个时间不对,请重新输入提醒");
                    return true;
                }
                groupMessageEvent.getGroup().sendBlocking(atAndok);
                /*
                 * 获取到time和newMessage存入redis
                 */
                String key = GroupId + ":" + AuthorId;
                stringRedisTemplate.opsForHash().put(key, time, str);
                return true;
            });

        } catch (TimeoutCancellationException timeout) {
            groupMessageEvent.getGroup().sendBlocking(new At(AuthorId) + "超过40秒了喔!");
        }


    }

    /**
     * 删除指定的提醒
     */
    @Listener
    @Filter(value = "#删除提醒{{time}}", matchType = MatchType.REGEX_MATCHES)
    @ContentTrim
    public void deleteOneRemind(MiraiGroupMessageEvent event,
                                @FilterValue("time") String time) {

        boolean ff = isTime(time, "MMddHHmm");
        String groupId = event.getGroup().getId().toString();
        String AuthorId = event.getAuthor().getId().toString();
        MessagesBuilder messagesBuilder = new MessagesBuilder();
        MessagesBuilder at = messagesBuilder.at(ID.$(AuthorId));
        if (ff) {
            String key = groupId + ":" + AuthorId;
            Object Remind = stringRedisTemplate.opsForHash().get(key, time);
            if (Remind != null) {
                stringRedisTemplate.opsForHash().delete(key, time);
                Messages build = at.append("删除成功").build();
                event.getGroup().sendBlocking(build);
            } else {
                Messages build = at.append("你应该没设置这个时间的提醒").build();
                event.getGroup().sendBlocking(build);
            }
        } else {
            MessagesBuilder messagesBuilder1 = new MessagesBuilder();
            Messages build = messagesBuilder1.at(ID.$(AuthorId)).append("时间格式有误").build();
            event.getGroup().sendBlocking(build);
        }
    }

    /**
     * 修改定时提醒
     */
    @Listener
    @ContentTrim
    @Filter(value = "#修改提醒{{time}}", matchType = MatchType.REGEX_MATCHES)
    public void editScheduledRemind(MiraiGroupMessageEvent event,
                                    @FilterValue("time") String time,
                                    ContinuousSessionContext context) {
        final String qqId = String.valueOf(event.getAuthor().getId());
        LongID AuthorId = event.getAuthor().getId();
        LongID GroupId = event.getGroup().getId();
        String key = GroupId + ":" + AuthorId;
        MessagesBuilder messagesBuilder = new MessagesBuilder();
        MessagesBuilder at = messagesBuilder.at(AuthorId);
        boolean ff = isTime(time, "MMddHHmm");//对时间进行判断，看是否符合格式
        if (!ff) {
            Messages build = at.append("时间格式不正确").build();
            event.getGroup().sendBlocking(build);
            return;
        }
        Object oldRemind = stringRedisTemplate.opsForHash().get(key, time);
        if (oldRemind == null) {
            MessagesBuilder messagesBuilder1 = new MessagesBuilder();
            Messages build1 = messagesBuilder1.at(AuthorId).append("你没在这个时间设置提醒").build();
            event.getGroup().sendBlocking(build1);
            return;
        }
        Messages build = at.append("请告诉我你要修改的内容").build();
        event.getGroup().sendBlocking(build);
        try {
            context.waitingForNextMessage(qqId, MiraiGroupMessageEvent.Key, 59, TimeUnit.SECONDS, (e, c) -> {
                if (!(c.getAuthor().getId().equals(AuthorId) && c.getGroup().getId().equals(GroupId))) {
                    return false;
                }
                final var messageContent = c.getMessageContent();
                String text = messageContent.getPlainText().trim();
                stringRedisTemplate.opsForHash().put(key, time, text);
                Object newRemind = stringRedisTemplate.opsForHash().get(key, time);
                MessagesBuilder messagesBuilder2 = new MessagesBuilder();
                MessagesBuilder at1 = messagesBuilder2.at(AuthorId);
                if (text.equals(newRemind)) {
                    Messages build2 = at1.append("修改成功").build();
                    event.getGroup().sendBlocking(build2);
                } else {
                    Messages build2 = at1.append("修改失败").build();
                    event.getGroup().sendBlocking(build2);
                }

                return true;
            });
        } catch (TimeoutCancellationException timeoutCancellationException) {
            MessagesBuilder messagesBuilder3 = new MessagesBuilder();
            Messages build1 = messagesBuilder3.at(AuthorId).append("超过59秒了喔!").build();
            event.getGroup().sendBlocking(build1);
        }
    }

    /**
     * 查看请求人的所有定时提醒
     */
    @Listener
    @ContentTrim
    @Filter(value = "#提醒", matchType = MatchType.REGEX_MATCHES)
    public void getAllScheduledRemind(MiraiGroupMessageEvent event) {
        String groupId = event.getGroup().getId().toString();
        String AuthorId = event.getAuthor().getId().toString();
        String key = groupId + ":" + AuthorId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        MessagesBuilder messagesBuilder = new MessagesBuilder();
        messagesBuilder.at(ID.$(AuthorId))
                .append("\n");
        if (entries.isEmpty()) {
            MessagesBuilder messagesBuilder1 = new MessagesBuilder();
            Messages build = messagesBuilder1.at(ID.$(AuthorId)).append("没找到关于你的提醒").build();
            event.getGroup().sendBlocking(build);
            return;
        } else {
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                String field = entry.getKey().toString();
                String value = entry.getValue().toString();
                messagesBuilder.append("时间:" + field + " , 提醒:" + value + "\n");
            }
        }
        Messages build = messagesBuilder
                .build();
        event.getGroup().sendBlocking(build);
    }
    /**
     *
     */
    /**
     * 判断字符串是否是时间格式
     */
    public boolean isTime(String time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            sdf.parse(time);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
