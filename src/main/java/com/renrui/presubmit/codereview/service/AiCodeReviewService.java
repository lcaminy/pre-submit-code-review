package com.renrui.presubmit.codereview.service;

import com.renrui.presubmit.codereview.model.Issue;
import java.util.List;
import java.util.Map;

/**
 * AI 代码审查服务接口
 * 定义了代码审查的核心方法
 */
public interface AiCodeReviewService {
    /**
     * 审查代码变更
     * @param changedFiles 变更文件及其内容的映射
     * @param commitMessage 提交信息
     * @return 发现的问题列表
     */
    List<Issue> reviewChanges(Map<String, String> changedFiles, String commitMessage);

    /**
     * 获取服务类型
     * @return AI 服务类型（本地或云端）
     */
    ServiceType getServiceType();

    /**
     * AI 服务类型枚举
     */
    enum ServiceType {
        LOCAL("本地模型"),
        CLOUD("云端服务");

        private final String description;

        ServiceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 