package love.forte.demo.openai;

import lombok.Data;

import java.util.List;

@Data
public class ResponseBody {
    private List<Choices> choices;
}
