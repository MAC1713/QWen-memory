package AI;

import lombok.Data;

@Data
public class CurrentUserMessage {
    private static CurrentUserMessage instance;
    private String message;

    private Integer messageCount = 0;

    private CurrentUserMessage() {
        // 私有构造函数防止外部实例化
    }

    public static synchronized CurrentUserMessage getInstance() {
        if (instance == null) {
            instance = new CurrentUserMessage();
        }
        return instance;
    }
}