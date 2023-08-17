package love.forte.demo.mapper;


import love.forte.demo.pojo.MessageText;
import org.apache.ibatis.annotations.Insert;

public interface RedisToMysqlMapper {
    @Insert("""
                INSERT INTO `message` (`time`, `group_id`, `qq_id`, `text`)
                VALUES (#{time}, #{group_id}, #{qq_id}, #{text})
            """)
    void messageTextInsert(MessageText messageText);

}
