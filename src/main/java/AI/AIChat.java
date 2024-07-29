package AI;

import AI.Constants.AIChatConstants;
import AI.Global.CurrentUserMessage;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.example.AINotebook;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static AI.Constants.AIChatConstants.*;

public class AIChat {
    private static final Logger LOGGER = Logger.getLogger(AIChat.class.getName());
    private final List<Message> fullConversationHistory;
    private final AINotebook aiNotebook;
    private int messageCountSinceLastReminder = CurrentUserMessage.getInstance().getMessageCount();

    private static final String NOTE_REGEX = "(?s)\\[(NOTE|OTE)]\\s*(.*?)\\[/(?:NOTE|OTE)]";
    private static final Pattern NOTE_PATTERN = Pattern.compile(NOTE_REGEX);
    private static final Pattern TAG_PATTERN = Pattern.compile("Tag:\\s*(.*?)\\s*(?=\\w+:|$)");
    private static final Pattern CONTENT_PATTERN = Pattern.compile("Content:\\s*(.*?)\\s*(?=\\w+:|$)");
    private static final Pattern IMPORTANCE_PATTERN = Pattern.compile("Importance:\\s*(\\d+(?:\\.\\d+)?)");

    public AIChat(AINotebook notebook) {
        fullConversationHistory = new ArrayList<>();
        this.aiNotebook = notebook;
    }

    public String generateResponse(String userInput, GenerationParam params) throws ApiException, NoApiKeyException, InputRequiredException {
        Message userMessage = createMessage(Role.USER, userInput);
        fullConversationHistory.add(userMessage);

        List<Message> contextMessages = getContextMessages();

        params.setMessages(contextMessages);

        try {
            GenerationResult result = callGenerationWithMessages(params);
            String aiResponse = result.getOutput().getChoices().get(0).getMessage().getContent();
            Message aiMessage = result.getOutput().getChoices().get(0).getMessage();
            fullConversationHistory.add(aiMessage);
            return aiResponse;
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            LOGGER.log(Level.SEVERE, "Error generating AI response", e);
            throw e;
        }
    }

    private List<Message> getContextMessages() {
        List<Message> contextMessages = new ArrayList<>();

        String firstSystemMessage = AIChatConstants.INITIAL_SYSTEM_PROMPT + "\n" + AIChatConstants.SIMPLIFIED_SYSTEM_PROMPT + "\n" + AIChatConstants.HOW_TO_USE_NOTEBOOK;

        //初始化System消息
        if (messageCountSinceLastReminder == 0) {
            contextMessages.add(createMessage(Role.SYSTEM, firstSystemMessage));
        }

        //每5次对话提醒一次身份
        if (messageCountSinceLastReminder >= REMINDER_INTERVAL && messageCountSinceLastReminder % REMINDER_INTERVAL == 0) {
            contextMessages.add(createMessage(Role.SYSTEM, AIChatConstants.SIMPLIFIED_SYSTEM_PROMPT));
        }

        //每2次对话提示一次notebook指令
        if (messageCountSinceLastReminder >= REMIND_USE_NOTEBOOK && messageCountSinceLastReminder % REMIND_USE_NOTEBOOK == 0) {
            contextMessages.add(createMessage(Role.SYSTEM, AIChatConstants.HOW_TO_USE_NOTEBOOK));
        }

        //每次对话提示一次notebook
        if (!aiNotebook.getNotes().isEmpty()) {
            contextMessages.add(createMessage(Role.SYSTEM, "Here's the current content of your notebook:\n" + aiNotebook.getFormattedNotes()));
        }

        setHistoryConversation(contextMessages);

        messageCountSinceLastReminder++;
        CurrentUserMessage.getInstance().setMessageCount(messageCountSinceLastReminder);
        return contextMessages;
    }

    private void setHistoryConversation(List<Message> contextMessages) {
        int startIndex = Math.max(0, fullConversationHistory.size() - MAX_CONTEXT_MESSAGES);
        for (int i = startIndex; i < fullConversationHistory.size(); i++) {
            Message message = fullConversationHistory.get(i);
            if (message.getRole().equals(Role.USER.getValue()) || message.getRole().equals(Role.ASSISTANT.getValue())) {
                contextMessages.add(message);
            }
        }
    }


    /**
     * 正则批量处理ai返回notebook指令
     *
     * @param aiResponse ai返回的notebook指令
     * @return 需要写入notebook的数据
     */
    public List<AINotebook.Note> extractNotesFromResponse(String aiResponse) {
        List<AINotebook.Note> notes = new ArrayList<>();
        Matcher noteMatcher = NOTE_PATTERN.matcher(aiResponse);

        while (noteMatcher.find()) {
            String noteContent = noteMatcher.group(2);
            String tag = extractField(TAG_PATTERN, noteContent);
            String content = extractField(CONTENT_PATTERN, noteContent);
            double importance = extractImportance(noteContent);

            if (tag != null && content != null && importance >= 0) {
                notes.add(new AINotebook.Note(content, tag, importance));
            }
        }

        return notes;
    }

    private String extractField(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private double extractImportance(String text) {
        String importanceStr = extractField(IMPORTANCE_PATTERN, text);
        try {
            return importanceStr != null ? Double.parseDouble(importanceStr) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 清理输出中的notebook指令
     *
     * @param aiResponse 完整的ai回复
     * @return 清理后的文本
     */
    public String cleanupAIResponse(String aiResponse) {
        // 使用之前定义的NOTE_PATTERN
        Matcher noteMatcher = NOTE_PATTERN.matcher(aiResponse);
        StringBuffer cleanedResponse = new StringBuffer();

        while (noteMatcher.find()) {
            noteMatcher.appendReplacement(cleanedResponse, "");
        }
        noteMatcher.appendTail(cleanedResponse);

        // 移除可能的多余空行和首尾空白
        return cleanedResponse.toString().replaceAll("(?m)^[ \t]*\r?\n", "").trim();
    }

    /**
     * 删除返回的 [NOTE]标签
     *
     * @param input aiResponse
     * @return 删除
     */
    public String removeNoteTags(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String startTag = "[NOTE]";
        String endTag = "[/NOTE]";

        int startIndex = input.indexOf(startTag);
        int endIndex = input.lastIndexOf(endTag);

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return input.substring(startIndex + startTag.length(), endIndex).trim();
        } else {
            return input;
        }
    }

    private static Message createMessage(Role role, String content) {
        return Message.builder().role(role.getValue()).content(content).build();
    }

    private static GenerationResult callGenerationWithMessages(GenerationParam param) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        return gen.call(param);
    }
}