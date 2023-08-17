package love.forte.demo;
import com.alibaba.fastjson.JSON;
import love.forte.simboot.spring.autoconfigure.EnableSimbot;
import net.mamoe.mirai.internal.spi.EncryptService;
import net.mamoe.mirai.utils.BotConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.cssxsh.mirai.tool.FixProtocolVersion;

import java.util.Map;


@EnableSimbot // 启用simbot
@SpringBootApplication
@EnableScheduling
@MapperScan("love.forte.demo.mapper")
public class Main  {
    /**
     * main方法，启动Spring应用程序。
     */
    public static void main(String[] args) {
        FixProtocolVersion.update();
        FixProtocolVersion.fetch(BotConfiguration.MiraiProtocol.ANDROID_PAD, "8.9.63");
        FixProtocolVersion.load(BotConfiguration.MiraiProtocol.ANDROID_PAD);
        Map<BotConfiguration.MiraiProtocol, String> info = FixProtocolVersion.info();
//        System.out.println(JSON.toJSONString(info));
        SpringApplication.run(Main.class, args);
    }


}

