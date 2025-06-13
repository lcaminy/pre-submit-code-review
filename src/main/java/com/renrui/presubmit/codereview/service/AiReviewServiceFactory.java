package com.renrui.presubmit.codereview.service;

import com.renrui.presubmit.codereview.service.impl.CloudAiReviewService;
import com.renrui.presubmit.codereview.service.impl.LocalAiReviewService;

/**
 * AI 代码审查服务工厂
 * 负责创建不同类型的代码审查服务实例
 */
public class AiReviewServiceFactory {
    private static final String DEFAULT_API_KEY = "your-api-key";  // 应从配置中读取
    private static AiCodeReviewService instance;

    /**
     * 获取 AI 代码审查服务实例
     * @param type 服务类型
     * @return 服务实例
     */
    public static synchronized AiCodeReviewService getInstance(AiCodeReviewService.ServiceType type) {
        if (instance == null) {
            try {
                instance = createService(type);
            } catch (Exception e) {
                // 如果创建失败，默认使用云端服务
                instance = new CloudAiReviewService(DEFAULT_API_KEY);
            }
        }
        return instance;
    }

    /**
     * 创建服务实例
     */
    private static AiCodeReviewService createService(AiCodeReviewService.ServiceType type) throws Exception {
        return switch (type) {
            case LOCAL -> new LocalAiReviewService();
            case CLOUD -> new CloudAiReviewService(DEFAULT_API_KEY);
        };
    }
} 