package love.forte.demo.openai;

import lombok.Data;

import java.util.List;

@Data
public class RequestBody {
     private String model;
     private List<Message> messages;
}
