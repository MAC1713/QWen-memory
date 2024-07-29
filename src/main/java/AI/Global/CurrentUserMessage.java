package AI.Global;

import lombok.Data;

/**
 * 全局共享变量
 * @author MAC1713
 * @email 1172820376@qq.com
 * @date 2024-07-26 03:11:04
 */
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