package love.forte.demo.listener;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.FilterValue;
import love.forte.simboot.annotation.Listener;
import love.forte.simbot.component.mirai.message.MiraiSendOnlyImage;
import love.forte.simbot.event.GroupMessageEvent;
import love.forte.simbot.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PlayWright {
    private static final Logger logger = LoggerFactory.getLogger(PlayWright.class);

    /**
     * 网址解析
     */
    @Listener
    @Filter(value = "解析{{str}}")
    public void jiexi(GroupMessageEvent event, @FilterValue("str") String str) {
        String regex = "https?://[\\w./?=%&#+-]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        String url = null;
        if (matcher.find()) {
            url = matcher.group(0);
        }
        //如果匹配后的字符串为空，返回
        if (url == null) {
            return;
        }
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            BrowserContext context = browser.newContext();
            // 设置视口的大小
            Page page = context.newPage(); // 创建新页面

            page.setViewportSize(1920, 1080); // 设置视口大小
            try {
                page.navigate(url);//导航到指定网址
                // 等待页面加载完成或网络空闲状态
                page.waitForLoadState(LoadState.NETWORKIDLE);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss"); // 时间戳格式
                String timeStamp = dateFormat.format(new Date()); // 获取当前时间戳


                Path localpath = Paths.get(  timeStamp + ".png");
                // 截取整个页面的屏幕截图，并且设置保存路径
                page.screenshot(new Page.ScreenshotOptions().setPath(localpath));
                MiraiSendOnlyImage urlimage = MiraiSendOnlyImage.of(Resource.of(localpath));
                event.getSource().sendBlocking(urlimage);
                File image = new File(localpath.toString());
                if (image.exists()) {
                    boolean delete = image.delete();
                    logger.info("删除截图临时文件：{}", delete);
                }
            } catch (PlaywrightException e) {
                event.getSource().sendBlocking(e.getMessage());
            } finally {
                // 关闭页面和上下文
                page.close();
                context.close();

                // 关闭浏览器
                browser.close();
            }
        }
    }
}
