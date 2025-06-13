package com.renrui.presubmit.codereview.service;

import com.renrui.presubmit.codereview.settings.AiReviewSettings;
import com.renrui.presubmit.codereview.service.impl.CloudAiReviewService;
import com.renrui.presubmit.codereview.service.impl.LocalAiReviewService;
import org.jetbrains.annotations.NotNull;

/**
 * AI 代码审查服务工厂
 * 负责创建不同类型的代码审查服务实例
 */
public class AiReviewServiceFactory {
    private static final String DEFAULT_API_KEY = "your-api-key-here";
    private static AiCodeReviewService instance;

    /**
     * 获取 AI 代码审查服务实例
     * @return 服务实例
     */
    public static @NotNull AiCodeReviewService getInstance() {
        if (instance == null) {
            AiReviewSettings settings = AiReviewSettings.getInstance();
            try {
                instance = switch (settings.getReviewMode()) {
                    case LOCAL -> new LocalAiReviewService();
                    case CLOUD -> new CloudAiReviewService();
                };
            } catch (Exception e) {
                // 如果本地服务创建失败，默认使用云端服务
                instance = new CloudAiReviewService();
            }
        }
        return instance;
    }
} 