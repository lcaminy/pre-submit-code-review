package com.renrui.presubmit.codereview.service;

import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import java.util.List;

/**
 * AI 代码审查服务接口
 * 定义了代码审查的基本操作和配置方法
 */
public interface AiReviewService {
    /**
     * 检查 AI 审查服务是否启用
     * @return 如果启用返回 true，否则返回 false
     */
    boolean isEnabled();

    /**
     * 设置 AI 审查服务的启用状态
     * @param enabled true 表示启用，false 表示禁用
     */
    void setEnabled(boolean enabled);

    /**
     * 在代码提交前进行审查
     * @param files 变更文件列表
     * @param commitMessage 提交信息
     * @param executor 提交执行器
     * @return 返回审查结果，决定是否允许提交
     */
    CheckinHandler.ReturnResult reviewChanges(List<String> files, String commitMessage, CommitExecutor executor);
} 