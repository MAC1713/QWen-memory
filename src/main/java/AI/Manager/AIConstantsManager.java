package AI.Manager;

import AI.Constants.AIChatConstants;

import java.io.*;
import java.util.Properties;

/**
 * 管理AI存取文件
 * @author MAC1713
 * @email 1172820376@qq.com
 * @date 2024-07-26 03:10:09
 */
public class AIConstantsManager {
    private static final String CONFIG_FILE = "ai_constants.ser";
    private Properties properties;

    public AIConstantsManager() {
        properties = new Properties();
        loadConstants();
    }

    public void loadConstants() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            AIChatConstants.INITIAL_SYSTEM_PROMPT = properties.getProperty("INITIAL_SYSTEM_PROMPT", AIChatConstants.INITIAL_SYSTEM_PROMPT);
            AIChatConstants.SIMPLIFIED_SYSTEM_PROMPT = properties.getProperty("SIMPLIFIED_SYSTEM_PROMPT", AIChatConstants.SIMPLIFIED_SYSTEM_PROMPT);
        } catch (IOException ex) {
            System.out.println("Configuration file not found. Using default values.");
        }
    }

    public void saveConstants(String initialPrompt, String simplifiedPrompt) {
        properties.setProperty("INITIAL_SYSTEM_PROMPT", initialPrompt);
        properties.setProperty("SIMPLIFIED_SYSTEM_PROMPT", simplifiedPrompt);

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "AI Chat Constants");
            AIChatConstants.INITIAL_SYSTEM_PROMPT = initialPrompt;
            AIChatConstants.SIMPLIFIED_SYSTEM_PROMPT = simplifiedPrompt;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getInitialSystemPrompt() {
        return properties.getProperty("INITIAL_SYSTEM_PROMPT", AIChatConstants.INITIAL_SYSTEM_PROMPT);
    }

    public String getSimplifiedSystemPrompt() {
        return properties.getProperty("SIMPLIFIED_SYSTEM_PROMPT", AIChatConstants.SIMPLIFIED_SYSTEM_PROMPT);
    }

    // 扩展接口：添加新的常量
    public void addConstant(String key, String value) {
        properties.setProperty(key, value);
    }

    // 扩展接口：获取任意常量
    public String getConstant(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    // 扩展接口：删除常量
    public void removeConstant(String key) {
        properties.remove(key);
    }
}