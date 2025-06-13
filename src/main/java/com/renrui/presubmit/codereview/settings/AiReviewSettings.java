package com.renrui.presubmit.codereview.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * AI 代码审查设置持久化
 */
@State(
    name = "AiReviewSettings",
    storages = @Storage("aiReviewSettings.xml")
)
public class AiReviewSettings implements PersistentStateComponent<AiReviewSettings> {
    private String apiKey = "";
    private String modelPath = "";
    private boolean enabled = true;
    private boolean enablePunctuationCheck = true;
    private boolean enableBugDetection = true;
    private boolean enableSecurityCheck = true;
    private boolean enablePerformanceCheck = true;
    private int aiMode = 1; // 0: 本地模型, 1: 云端服务

    public static AiReviewSettings getInstance() {
        return ApplicationManager.getApplication().getService(AiReviewSettings.class);
    }

    @Override
    public @Nullable AiReviewSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AiReviewSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnablePunctuationCheck() {
        return enablePunctuationCheck;
    }

    public void setEnablePunctuationCheck(boolean enablePunctuationCheck) {
        this.enablePunctuationCheck = enablePunctuationCheck;
    }

    public boolean isEnableBugDetection() {
        return enableBugDetection;
    }

    public void setEnableBugDetection(boolean enableBugDetection) {
        this.enableBugDetection = enableBugDetection;
    }

    public boolean isEnableSecurityCheck() {
        return enableSecurityCheck;
    }

    public void setEnableSecurityCheck(boolean enableSecurityCheck) {
        this.enableSecurityCheck = enableSecurityCheck;
    }

    public boolean isEnablePerformanceCheck() {
        return enablePerformanceCheck;
    }

    public void setEnablePerformanceCheck(boolean enablePerformanceCheck) {
        this.enablePerformanceCheck = enablePerformanceCheck;
    }

    public int getAiMode() {
        return aiMode;
    }

    public void setAiMode(int aiMode) {
        this.aiMode = aiMode;
    }
} 