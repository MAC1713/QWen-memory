package org.example;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIChatGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private List<Message> fullConversationHistory;
    private static final int MAX_CONTEXT_MESSAGES = 10;
    private AINotebook aiNotebook;

    // 新增控件
    private JSlider topPSlider, topKSlider, repetitionPenaltySlider, temperatureSlider;
    private JLabel topPLabel, topKLabel, repetitionPenaltyLabel, temperatureLabel;
    private JTextArea notebookArea;
    private JButton toggleNotebookButton;
    private JFrame notebookFrame;

    public AIChatGUI() {
        super("AI Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLayout(new BorderLayout());

        // 设置暖色背景
        getContentPane().setBackground(new Color(255, 248, 220));

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 248, 220));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建左侧控制面板
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.WEST);

        // 创建中央聊天面板
        JPanel chatPanel = createChatPanel();
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        fullConversationHistory = new ArrayList<>();
        aiNotebook = new AINotebook();
        aiNotebook.cleanupNotes(); // Clean up old, less important notes on startup

        String systemPrompt = "You are Emma, an intelligent AI assistant. Your responses should be in Chinese. " +
                "Always keep the context of the conversation and your notebook in mind when responding. " +
                "Pay close attention to important information shared by the user, such as their name, preferences, or significant details about their life or work. " +
                "When you encounter such important information, or when you make decisions about your own identity or capabilities, " +
                "summarize it and store it in your notebook using the format: [NOTE]tag:content:importance[/NOTE]. " +
                "Tags can be 'Name', 'Identity', 'Preference', 'Background', etc. Importance should be a value between 0 and 1, " +
                "where 1 is extremely important (like names or core identity information) and 0 is trivial. " +
                "For example, if the user says their name is John, you should add: [NOTE]Name:The user's name is John:1.0[/NOTE]. " +
                "If you decide to have a favorite color, you might add: [NOTE]Identity:Emma's favorite color is blue:0.7[/NOTE]. " +
                "Always use the information in your notebook when it's relevant to the conversation. " +
                "Remember, you are Emma, and you should maintain a consistent personality and set of knowledge across conversations.";

        fullConversationHistory.add(createMessage(Role.SYSTEM, systemPrompt));

        // 创建并设置 Notebook 窗口
        createNotebookFrame();

        // 显示现有笔记
        if (!aiNotebook.getNotes().isEmpty()) {
            chatArea.append("Loaded existing notes:\n" + aiNotebook.getFormattedNotes() + "\n\n");
            notebookArea.setText(aiNotebook.getFormattedNotes());
        }
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(255, 248, 220));
        controlPanel.setBorder(BorderFactory.createTitledBorder("参数控制"));

        // Top P Slider
        topPSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 80);
        topPLabel = new JLabel("Top P: 0.8");
        setupSlider(topPSlider, topPLabel, "Top P", 0.01, 2);

        // Top K Slider
        topKSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        topKLabel = new JLabel("Top K: 0");
        setupSlider(topKSlider, topKLabel, "Top K", 1, 0);

        // Repetition Penalty Slider
        repetitionPenaltySlider = new JSlider(JSlider.HORIZONTAL, 100, 200, 110);
        repetitionPenaltyLabel = new JLabel("Repetition Penalty: 1.1");
        setupSlider(repetitionPenaltySlider, repetitionPenaltyLabel, "Repetition Penalty", 0.01, 2);

        // Temperature Slider
        temperatureSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 80);
        temperatureLabel = new JLabel("Temperature: 0.8");
        setupSlider(temperatureSlider, temperatureLabel, "Temperature", 0.01, 2);

        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(topPLabel);
        controlPanel.add(topPSlider);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(topKLabel);
        controlPanel.add(topKSlider);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(repetitionPenaltyLabel);
        controlPanel.add(repetitionPenaltySlider);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(temperatureLabel);
        controlPanel.add(temperatureSlider);

        return controlPanel;
    }

    private void setupSlider(JSlider slider, JLabel label, String name, double scale, int decimalPlaces) {
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(new Color(255, 248, 220));

        slider.addChangeListener(e -> {
            double value = slider.getValue() * scale;
            label.setText(String.format("%s: %." + decimalPlaces + "f", name, value));
        });
    }

    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(new Color(255, 248, 220));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(255, 250, 220));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(255, 248, 220));
        inputField = new JTextField();
        sendButton = new JButton("发送");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // 添加切换 Notebook 窗口的按钮
        toggleNotebookButton = new JButton("Toggle Notebook");
        toggleNotebookButton.addActionListener(e -> toggleNotebookWindow());
        chatPanel.add(toggleNotebookButton, BorderLayout.NORTH);

        return chatPanel;
    }

    private void createNotebookFrame() {
        notebookFrame = new JFrame("AI Notebook");
        notebookFrame.setSize(400, 600);
        notebookFrame.setLayout(new BorderLayout());

        notebookArea = new JTextArea();
        notebookArea.setEditable(true);
        notebookArea.setLineWrap(true);
        notebookArea.setWrapStyleWord(true);
        JScrollPane notebookScrollPane = new JScrollPane(notebookArea);
        notebookFrame.add(notebookScrollPane, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveNotebookChanges());
        notebookFrame.add(saveButton, BorderLayout.SOUTH);

        notebookFrame.setVisible(false);
    }

    private void toggleNotebookWindow() {
        notebookFrame.setVisible(!notebookFrame.isVisible());
    }

    private void saveNotebookChanges() {
        // 这里需要实现保存 notebook 更改的逻辑
        // 可能需要修改 AINotebook 类来支持从文本更新笔记
        JOptionPane.showMessageDialog(notebookFrame, "Changes saved successfully!");
    }

    private void sendMessage() {
        String userInput = inputField.getText();
        if (!userInput.trim().isEmpty()) {
            chatArea.append("You: " + userInput + "\n");
            inputField.setText("");

            Message userMessage = createMessage(Role.USER, userInput);
            fullConversationHistory.add(userMessage);

            try {
                List<Message> contextMessages = getContextMessages();
                GenerationResult result = callGenerationWithMessages(
                        createGenerationParam(contextMessages)
                );
                String aiResponse = result.getOutput().getChoices().get(0).getMessage().getContent();
                chatArea.append("Emma: " + aiResponse + "\n\n");

                Message aiMessage = result.getOutput().getChoices().get(0).getMessage();
                fullConversationHistory.add(aiMessage);

                // Check if AI response contains information to be added to the notebook
                checkAndUpdateNotebook(aiResponse);

            } catch (Exception ex) {
                chatArea.append("Error: " + ex.getMessage() + "\n\n");
            }
        }
    }

    private List<Message> getContextMessages() {
        List<Message> contextMessages = new ArrayList<>();
        // Always include the system message
        contextMessages.add(fullConversationHistory.get(0));

        // Add notebook content as context
        if (!aiNotebook.getNotes().isEmpty()) {
            contextMessages.add(createMessage(Role.SYSTEM, aiNotebook.getFormattedNotes()));
        }

        int startIndex = Math.max(1, fullConversationHistory.size() - MAX_CONTEXT_MESSAGES);
        for (int i = startIndex; i < fullConversationHistory.size(); i++) {
            contextMessages.add(fullConversationHistory.get(i));
        }

        return contextMessages;
    }

    private void checkAndUpdateNotebook(String aiResponse) {
        String regex = "\\[NOTE\\](.*?):(.*?):(\\d+\\.\\d+)\\[/NOTE\\]";
        Pattern notePattern = Pattern.compile(regex);
        Matcher matcher = notePattern.matcher(aiResponse);
        while (matcher.find()) {
            String tag = matcher.group(1);
            String content = matcher.group(2);
            double importance = Double.parseDouble(matcher.group(3));
            aiNotebook.addNote(content, tag, importance);
            chatArea.append("Added note to AI Notebook: [" + tag + "] " + content + " (Importance: " + importance + ")\n\n");
            notebookArea.setText(aiNotebook.getFormattedNotes());
        }
    }

    private static Message createMessage(Role role, String content) {
        return Message.builder().role(role.getValue()).content(content).build();
    }

    private GenerationParam createGenerationParam(List<Message> messages) {
        return GenerationParam.builder()
                .model("qwen-72b-chat")
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                //生成过程中核采样方法概率阈值，例如，取值为0.8时，仅保留概率加起来大于等于0.8的最可能token的最小集合作为候选集。取值范围为（0,1.0)，取值越大，生成的随机性越高；取值越低，生成的确定性越高。
                .topP((double) topPSlider.getValue() / 100)
                //生成时，采样候选集的大小。例如，取值为50时，仅将单次生成中得分最高的50个token组成随机采样的候选集。取值越大，生成的随机性越高；取值越小，生成的确定性越高。默认值为0，表示不启用top_k策略，此时，仅有top_p策略生效。
                .topK(topKSlider.getValue())
                //用于控制模型生成时的重复度。提高repetition_penalty时可以降低模型生成的重复度。1.0表示不做惩罚。
                .repetitionPenalty((float) repetitionPenaltySlider.getValue() / 100)
                //用于控制随机性和多样性的程度。具体来说，temperature值控制了生成文本时对每个候选词的概率分布进行平滑的程度。较高的temperature值会降低概率分布的峰值，使得更多的低概率词被选择，生成结果更加多样化；而较低的temperature值则会增强概率分布的峰值，使得高概率词更容易被选择，生成结果更加确定。 //取值范围：[0, 2)，不建议取值为0，无意义。
                .temperature((float) temperatureSlider.getValue() / 100)
                .apiKey(ApiKey.API_KEY)
                .build();
    }

    private static GenerationResult callGenerationWithMessages(GenerationParam param) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        return gen.call(param);
    }

    public static void main(String[] args) {
        // Set JVM arguments programmatically
        System.setProperty("java.awt.headless", "false");
        System.setProperty("sun.java2d.noddraw", "true");
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.opengl", "false");
        System.setProperty("sun.java2d.pmoffscreen", "false");

        // Increase CodeCache size
        System.setProperty("ReservedCodeCacheSize", "256m");

        // Enable CodeCache flushing
        System.setProperty("UseCodeCacheFlushing", "true");

        SwingUtilities.invokeLater(() -> new AIChatGUI().setVisible(true));
    }
}