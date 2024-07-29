package AI.Windows;

import AI.Constants.AIChatConstants;
import AI.Constants.ApiKey;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.List;

public class ConfigurationWindow extends JFrame {
    private JSlider topPSlider, topKSlider, repetitionPenaltySlider, temperatureSlider;
    private JLabel topPLabel, topKLabel, repetitionPenaltyLabel, temperatureLabel;
    private JButton resetButton;
    private JPanel contentPanel;

    public ConfigurationWindow() {
        super("参数配置");
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 248, 220));

        setContentPane(contentPanel);

        initializeSliders();
        addSliders();
        addResetButton();

        pack();
        setLocationRelativeTo(null);
    }

    private void initializeSliders() {
        topPSlider = createSlider(0, 100, 80);
        topKSlider = createSlider(0, 100, 0);
        repetitionPenaltySlider = createSlider(100, 200, 110);
        temperatureSlider = createSlider(0, 200, 80);

        topPLabel = new JLabel("Top P: 0.80");
        topKLabel = new JLabel("Top K: 0");
        repetitionPenaltyLabel = new JLabel("Repetition Penalty: 1.10");
        temperatureLabel = new JLabel("Temperature: 0.80");
    }

    private JSlider createSlider(int min, int max, int initialValue) {
        JSlider slider = new JSlider(SwingConstants.HORIZONTAL, min, max, initialValue);
        slider.setMajorTickSpacing((max - min) / 5);
        slider.setMinorTickSpacing((max - min) / 20);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(new Color(255, 248, 220));

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = min; i <= max; i += (max - min) / 5) {
            labelTable.put(i, new JLabel(String.format("%.2f", i / 100.0)));
        }
        slider.setLabelTable(labelTable);

        return slider;
    }

    private void addSliders() {
        contentPanel.add(Box.createVerticalStrut(10));
        addSliderWithLabel(topPSlider, topPLabel, "Top P", 0.01, 2);
        contentPanel.add(Box.createVerticalStrut(10));
        addSliderWithLabel(topKSlider, topKLabel, "Top K", 1, 0);
        contentPanel.add(Box.createVerticalStrut(10));
        addSliderWithLabel(repetitionPenaltySlider, repetitionPenaltyLabel, "Repetition Penalty", 0.01, 2);
        contentPanel.add(Box.createVerticalStrut(10));
        addSliderWithLabel(temperatureSlider, temperatureLabel, "Temperature", 0.01, 2);
    }

    private void addSliderWithLabel(JSlider slider, JLabel label, String name, double scale, int decimalPlaces) {
        contentPanel.add(label);
        contentPanel.add(slider);
        slider.addChangeListener(e -> {
            double value = slider.getValue() * scale;
            label.setText(String.format("%s: %." + decimalPlaces + "f", name, value));
        });
    }

    public GenerationParam createGenerationParam(List<Message> messages) {
        return GenerationParam.builder()
                .model(AIChatConstants.QWEN_MODEL)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP((double) topPSlider.getValue() / 100)
                .topK(topKSlider.getValue())
                .repetitionPenalty((float) repetitionPenaltySlider.getValue() / 100)
                .temperature((float) temperatureSlider.getValue() / 100)
                .apiKey(ApiKey.API_KEY)
                .build();
    }

    public GenerationParam createSystemParam(List<Message> messages) {
        return GenerationParam.builder()
                .model(AIChatConstants.QWEN_MODEL)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                //生成过程中核采样方法概率阈值，例如，取值为0.8时，仅保留概率加起来大于等于0.8的最可能token的最小集合作为候选集。取值范围为（0,1.0)，取值越大，生成的随机性越高；取值越低，生成的确定性越高。
                .topP(0.2)
                //生成时，采样候选集的大小。例如，取值为50时，仅将单次生成中得分最高的50个token组成随机采样的候选集。取值越大，生成的随机性越高；取值越小，生成的确定性越高。默认值为0，表示不启用top_k策略，此时，仅有top_p策略生效。
                .topK(0)
                //用于控制模型生成时的重复度。提高repetition_penalty时可以降低模型生成的重复度。1.0表示不做惩罚。
                .repetitionPenalty(1.01F)
                //用于控制随机性和多样性的程度。具体来说，temperature值控制了生成文本时对每个候选词的概率分布进行平滑的程度。较高的temperature值会降低概率分布的峰值，使得更多的低概率词被选择，生成结果更加多样化；而较低的temperature值则会增强概率分布的峰值，使得高概率词更容易被选择，生成结果更加确定。 //取值范围：[0, 2)，不建议取值为0，无意义。
                .temperature(0.3F)
                .apiKey(org.example.ApiKey.API_KEY)
                .build();
    }

    private void addResetButton() {
        resetButton = new JButton("重置参数");
        resetButton.addActionListener(e -> resetToDefaults());
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(resetButton);
    }

    public void resetToDefaults() {
        topPSlider.setValue(80);
        topKSlider.setValue(0);
        repetitionPenaltySlider.setValue(110);
        temperatureSlider.setValue(80);
    }

    public void applyTheme(boolean isDarkMode) {
        Color bgColor = isDarkMode ? new Color(50, 50, 50) : new Color(255, 248, 220);
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;

        contentPanel.setBackground(bgColor);
        contentPanel.setForeground(fgColor);
        SwingUtilities.updateComponentTreeUI(this);
    }
}