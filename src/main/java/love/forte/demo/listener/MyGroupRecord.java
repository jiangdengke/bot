package love.forte.demo.listener;

import love.forte.demo.pojo.MessageText;
import love.forte.demo.utils.BotUtil;
import love.forte.simboot.annotation.Listener;
import love.forte.simbot.ID;
import java.sql.Timestamp;

import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent;
import love.forte.simbot.component.mirai.message.MiraiForwardMessage;
import love.forte.simbot.component.mirai.message.SimbotOriginalMiraiMessage;
import love.forte.simbot.definition.GroupMember;
import love.forte.simbot.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.text.MessageFormat;

@Component
public class MyGroupRecord {
    private static final Logger log = LoggerFactory.getLogger(MyGroupRecord.class);
    @Listener
    public void groupMessage(MiraiGroupMessageEvent event){
        //得到bot
        var groupId = "群: " + event.getGroup().getName() + "(" + event.getGroup().getId() + ")";
        var groupUser = "成员: " + event.getAuthor().getUsername() + "(" + event.getAuthor().getId() + ")";
        var group = event.getGroup();
        log.info(MessageFormat.format("{0}\t\t{1}", groupId, groupUser));
        for (Message.Element<?> message : event.getMessageContent().getMessages()) {
            //检测是否含是图片
            if (message instanceof Image<?> image) {
                log.info(MessageFormat.format("[图片消息: {0} ]", image.getResource().getName()));
//                getPictureUtil.downloadImage(image.getResource().getName(),destinationFolder);//下载图片
            }
            //检测是否是转发消息
            if (message instanceof MiraiForwardMessage miraiForwardMessage) {
                miraiForwardMessage.getOriginalForwardMessage().getNodeList().forEach(a -> {
                    log.info(MessageFormat.format("[转发消息: \n内容: {0} ]", a.getMessageChain()));
                });
            }
            //检测是否含表情
            if (message instanceof Face face) {
                log.info(MessageFormat.format("[Face表情: {0} ]", face.getId()));
            }
            //检测是否是at信息
            if (message instanceof At at) {
                ID targetId = at.getTarget();
                GroupMember targetMember = group.getMember(targetId);
                if (targetMember == null) {
                    log.info(MessageFormat.format("[AT消息:未找到目标用户: {0} ]", targetId));
                } else {
                    log.info(MessageFormat.format("[AT消息: @{0}( {1} )", targetMember.getNickOrUsername(), targetMember.getId()));
                }
            }
            if (message instanceof SimbotOriginalMiraiMessage messageApp) {
                String xmlApp = messageApp.getOriginalMiraiMessage().contentToString();
                log.info(xmlApp);
            }
            //检测是否包含消息实体
            if (message instanceof Text) {
                String text = ((Text) message).getText();
                if(text!=null){
                    //获得当前时间的时间戳,级别为毫秒
                    long currentTimeMillis = System.currentTimeMillis();
                    Timestamp time = new Timestamp(currentTimeMillis);

                    ID group_id = event.getGroup().getId();
                    ID qq_id = event.getAuthor().getId();
                    //得到MessageText实体
                    MessageText msg = new MessageText();
                    //为MessageText实体属性赋值
                    msg.setTime(time);
                    msg.setGroup_id(String.valueOf(group_id));
                    msg.setQq_id(String.valueOf(qq_id));
                    msg.setText(text);
                    String msgJsonStirng = msg.toJsonStirng();
                }
                log.info(MessageFormat.format("[文本消息: {0} ]", ((Text) message).getText()));
            }
        }
    }
}
