package love.forte.demo.listener;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import love.forte.demo.utils.DouyinUtil;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.component.mirai.MiraiGroup;
import love.forte.simbot.component.mirai.event.MiraiGroupMessageEvent;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import net.mamoe.mirai.contact.file.AbsoluteFile;
import net.mamoe.mirai.contact.file.RemoteFiles;
import net.mamoe.mirai.utils.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
public class Douyin {
    private static final Logger logger = LoggerFactory.getLogger(Douyin.class);
    @Autowired
    @Qualifier("redisTemplate0")
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 解析抖音
     *
     * @param groupMessageEvent
     * @throws IOException
     */
    @Listener
    @ContentTrim
    @Filter(value = "\\d+\\.\\d+ [A-Za-z]+:/ .*? https://v\\.douyin\\.com/[A-Za-z0-9]+/ 复制此链接，打开Dou音搜索，直接观看视频！", matchType = MatchType.REGEX_MATCHES)
    @Filter(value = ".*?复制打开抖音，看看【(.*?)】.*?(https?://[\\w./?=%&#+-]+).*", matchType = MatchType.REGEX_MATCHES)
    public void getImageBz(MiraiGroupMessageEvent groupMessageEvent) throws IOException {
        logger.info("检测到视频链接，准备解析");
        logger.info("开始判断上次解析是否完成");
        //uncompleted completed
        String douyin = stringRedisTemplate.opsForValue().get("douyin");
        if(douyin.equals("uncompleted")){
            logger.info("上次解析还未完成");
            groupMessageEvent.replyBlocking("有解析任务正在执行");
            return;
        }
        logger.info("上次解析已完成");
        stringRedisTemplate.opsForValue().set("douyin","uncompleted");//正在执行，设置为"未完成"
        /*
         匹配url
         */
        String message = groupMessageEvent.getMessageContent().getPlainText();//获取消息文本
        String regex = "https?://[\\w./?=%&#+-]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        String url = null;
        if (matcher.find()) {
            url = matcher.group(0);
        }
        String titleAndVideoUrl = null;
        JSONObject jsonObject = null;
        /**
         * 判断能否解析成功
         */
        try {
            titleAndVideoUrl = DouyinUtil.getTitleAndVideoUrl(url);
            jsonObject = JSONUtil.parseObj(titleAndVideoUrl);
        } catch (Exception e) {
            groupMessageEvent.sendAsync("解析失败");
            return ;
        }
        MessagesBuilder messagesBuilder = new MessagesBuilder();

        groupMessageEvent.sendBlocking("解析成功");
        String title = jsonObject.getStr("title");
        Messages build = messagesBuilder.append("标题:").append(title).build();
        groupMessageEvent.sendBlocking(build);
        String videoUrl = jsonObject.getStr("url");
        String videoPath = DouyinUtil.downAndgetVideoPath(videoUrl);//下载到本地
        /**
         * mirai上传群文件的步骤
         */
        MiraiGroup group = groupMessageEvent.getGroup();
        net.mamoe.mirai.contact.Group sourceGroup = group.getOriginalContact();
        RemoteFiles files = sourceGroup.getFiles();
        File file = new File(videoPath);//将本地视频文件转成file文件
        try (ExternalResource externalResource = ExternalResource.create(file)) {
            // 上传文件到制定路径目标
            AbsoluteFile absoluteFile = files.uploadNewFile(file.getName(), externalResource);
            logger.info("文件上传成功");
        } catch (IOException e) {
            // 处理异常
            throw new RuntimeException("视频上传失败");
        } finally {
            //删除保存在本地的视频
            File video = new File(videoPath);
            if (video.exists()) {
                boolean delete = video.delete();
                logger.info("删除视频临时文件：{}", delete);
            }
        }
        stringRedisTemplate.opsForValue().set("douyin","completed");//本次执行完毕，设置为完成
    }
}
