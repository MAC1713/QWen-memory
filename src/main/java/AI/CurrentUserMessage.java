package AI;

public class CurrentUserMessage {
    private static CurrentUserMessage instance;
    private String message;

    private CurrentUserMessage() {
        // 私有构造函数防止外部实例化
    }

    public static synchronized CurrentUserMessage getInstance() {
        if (instance == null) {
            instance = new CurrentUserMessage();
        }
        return instance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}