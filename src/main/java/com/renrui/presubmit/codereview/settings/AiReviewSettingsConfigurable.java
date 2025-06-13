package com.renrui.presubmit.codereview.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.renrui.presubmit.codereview.bundle.AiReviewBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * AI 代码审查设置页面
 */
public class AiReviewSettingsConfigurable implements Configurable {
    private final Project project;
    private JPanel mainPanel;
    private JBTextField apiKeyField;
    private TextFieldWithBrowseButton modelPathField;
    private JBCheckBox enablePunctuationCheck;
    private JBCheckBox enableBugDetection;
    private JBCheckBox enableSecurityCheck;
    private JBCheckBox enablePerformanceCheck;
    private JComboBox<String> aiModeComboBox;

    public AiReviewSettingsConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String getDisplayName() {
        return AiReviewBundle.message("ai.code.review");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        // 创建主面板
        mainPanel = new JPanel(new BorderLayout());

        // 创建输入字段
        apiKeyField = new JBTextField();
        modelPathField = new TextFieldWithBrowseButton();
        modelPathField.addBrowseFolderListener(
                AiReviewBundle.message("file.chooser.title"),
                AiReviewBundle.message("file.chooser.description"),
                project,
                new com.intellij.openapi.fileChooser.FileChooserDescriptor(
                        true, false, false, false, false, false
                )
        );

        // 创建复选框
        enablePunctuationCheck = new JBCheckBox(AiReviewBundle.message("settings.rules.punctuation"));
        enableBugDetection = new JBCheckBox(AiReviewBundle.message("settings.rules.bug"));
        enableSecurityCheck = new JBCheckBox(AiReviewBundle.message("settings.rules.security"));
        enablePerformanceCheck = new JBCheckBox(AiReviewBundle.message("settings.rules.performance"));

        // 创建 AI 模式选择下拉框
        aiModeComboBox = new JComboBox<>(new String[]{
            AiReviewBundle.message("settings.ai.mode.local"),
            AiReviewBundle.message("settings.ai.mode.cloud")
        });

        // 使用 FormBuilder 构建布局
        JPanel formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(AiReviewBundle.message("settings.ai.mode") + ":", aiModeComboBox)
                .addLabeledComponent(AiReviewBundle.message("settings.api.key") + ":", apiKeyField)
                .addLabeledComponent(AiReviewBundle.message("settings.model.path") + ":", modelPathField)
                .addSeparator()
                .addComponent(new JLabel(AiReviewBundle.message("settings.rules")))
                .addComponent(enablePunctuationCheck)
                .addComponent(enableBugDetection)
                .addComponent(enableSecurityCheck)
                .addComponent(enablePerformanceCheck)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 加载当前设置
        loadSettings();

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        AiReviewSettings settings = AiReviewSettings.getInstance();
        return !apiKeyField.getText().equals(settings.getApiKey()) ||
               !modelPathField.getText().equals(settings.getModelPath()) ||
               enablePunctuationCheck.isSelected() != settings.isEnablePunctuationCheck() ||
               enableBugDetection.isSelected() != settings.isEnableBugDetection() ||
               enableSecurityCheck.isSelected() != settings.isEnableSecurityCheck() ||
               enablePerformanceCheck.isSelected() != settings.isEnablePerformanceCheck() ||
               aiModeComboBox.getSelectedIndex() != settings.getAiMode();
    }

    @Override
    public void apply() throws ConfigurationException {
        AiReviewSettings settings = AiReviewSettings.getInstance();
        settings.setApiKey(apiKeyField.getText());
        settings.setModelPath(modelPathField.getText());
        settings.setEnablePunctuationCheck(enablePunctuationCheck.isSelected());
        settings.setEnableBugDetection(enableBugDetection.isSelected());
        settings.setEnableSecurityCheck(enableSecurityCheck.isSelected());
        settings.setEnablePerformanceCheck(enablePerformanceCheck.isSelected());
        settings.setAiMode(aiModeComboBox.getSelectedIndex());
    }

    @Override
    public void reset() {
        loadSettings();
    }

    private void loadSettings() {
        AiReviewSettings settings = AiReviewSettings.getInstance();
        apiKeyField.setText(settings.getApiKey());
        modelPathField.setText(settings.getModelPath());
        enablePunctuationCheck.setSelected(settings.isEnablePunctuationCheck());
        enableBugDetection.setSelected(settings.isEnableBugDetection());
        enableSecurityCheck.setSelected(settings.isEnableSecurityCheck());
        enablePerformanceCheck.setSelected(settings.isEnablePerformanceCheck());
        aiModeComboBox.setSelectedIndex(settings.getAiMode());
    }
} 