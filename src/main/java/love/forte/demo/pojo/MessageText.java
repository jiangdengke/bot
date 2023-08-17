package love.forte.demo.pojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import java.sql.Timestamp;

@Data
public class MessageText {
    private Timestamp time;
    private String group_id;
    private String qq_id;
    private String text;
    public String toJsonStirng(){
        return JSON.toJSONString(this);
    }
}
