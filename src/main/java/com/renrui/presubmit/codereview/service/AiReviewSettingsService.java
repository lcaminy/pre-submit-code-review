package com.renrui.presubmit.codereview.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * AI 代码审查设置服务
 * 负责管理插件的持久化配置
 */
@State(
    name = "AiReviewSettings",
    storages = @Storage("aiReviewSettings.xml")  // 配置将保存在这个 XML 文件中
)
public class AiReviewSettingsService implements PersistentStateComponent<AiReviewSettingsService> {
    // 插件启用状态，默认为启用
    private boolean enabled = true;

    /**
     * 获取服务实例
     * @return 返回全局唯一的服务实例
     */
    public static AiReviewSettingsService getInstance() {
        return ApplicationManager.getApplication().getService(AiReviewSettingsService.class);
    }

    /**
     * 获取当前状态
     * @return 返回当前服务实例
     */
    @Override
    public @Nullable AiReviewSettingsService getState() {
        return this;
    }

    /**
     * 加载保存的状态
     * @param state 要加载的状态
     */
    @Override
    public void loadState(@NotNull AiReviewSettingsService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    /**
     * 获取启用状态
     * @return 如果启用返回 true，否则返回 false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置启用状态
     * @param enabled 是否启用插件
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
} 