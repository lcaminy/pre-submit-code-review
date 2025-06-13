package com.renrui.presubmit.codereview.handler;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;
import com.renrui.presubmit.codereview.model.Issue;
import com.renrui.presubmit.codereview.service.AiCodeReviewService;
import com.renrui.presubmit.codereview.service.AiReviewServiceFactory;
import com.renrui.presubmit.codereview.service.AiReviewSettingsService;
import com.renrui.presubmit.codereview.settings.AiReviewSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码提交拦截处理器
 * 负责在代码提交前进行拦截和审查
 */
public class AiReviewCheckinHandler extends CheckinHandler {
    // 提交面板，包含了变更文件等信息
    private final CheckinProjectPanel panel;
    // 当前项目实例
    private final Project project;
    private final AiCodeReviewService aiService;

    /**
     * 构造函数
     * @param panel 提交面板实例
     */
    public AiReviewCheckinHandler(@NotNull CheckinProjectPanel panel) {
        this.panel = panel;
        this.project = panel.getProject();
        this.aiService = AiReviewServiceFactory.getInstance(AiCodeReviewService.ServiceType.CLOUD);
    }

    /**
     * 在代码提交前执行的处理方法
     * @param executor 提交执行器
     * @return 返回是否允许提交的结果
     */
    @Override
    public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
        // 检查插件是否启用
        AiReviewSettings settings = AiReviewSettings.getInstance();
        if (!settings.isEnabled()) {
            return ReturnResult.COMMIT;
        }

        try {
            // 收集变更文件内容
            Map<String, String> changedFiles = collectChangedFiles();
            if (changedFiles.isEmpty()) {
                return ReturnResult.COMMIT;
            }

            // 执行 AI 代码审查
            List<Issue> issues = aiService.reviewChanges(changedFiles, panel.getCommitMessage());
            
            // 如果发现问题，显示通知
            if (!issues.isEmpty()) {
                showIssuesNotification(issues);
                // 如果存在严重问题，阻止提交
                if (hasBlockingIssues(issues)) {
                    return ReturnResult.CANCEL;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorNotification("AI 代码审查失败: " + e.getMessage());
        }

        return ReturnResult.COMMIT;
    }

    /**
     * 收集变更文件内容
     */
    private Map<String, String> collectChangedFiles() throws IOException {
        Map<String, String> files = new HashMap<>();
        for (VirtualFile file : panel.getVirtualFiles()) {
            if (file != null && file.exists() && !file.isDirectory()) {
                try {
                    files.put(file.getPath(), new String(file.contentsToByteArray()));
                } catch (IOException e) {
                    showErrorNotification("无法读取文件内容: " + file.getPath());
                }
            }
        }
        return files;
    }

    /**
     * 显示问题通知
     */
    private void showIssuesNotification(List<Issue> issues) {
        StringBuilder message = new StringBuilder("发现以下问题：\n");
        for (Issue issue : issues) {
            message.append(String.format("- [%s] %s (%s:%d)\n  建议：%s\n",
                    issue.getType().getDescription(),
                    issue.getMessage(),
                    issue.getFile(),
                    issue.getLine(),
                    issue.getSuggestion()));
        }

        NotificationGroupManager.getInstance()
                .getNotificationGroup("AI Code Review")
                .createNotification(
                        "AI 代码审查结果",
                        message.toString(),
                        NotificationType.WARNING)
                .notify(project);
    }

    /**
     * 显示错误通知
     */
    private void showErrorNotification(String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("AI Code Review")
                .createNotification(
                        "AI 代码审查",
                        message,
                        NotificationType.ERROR)
                .notify(project);
    }

    /**
     * 检查是否存在阻止提交的严重问题
     */
    private boolean hasBlockingIssues(List<Issue> issues) {
        if (!AiReviewSettings.getInstance().isEnableSecurityCheck()) {
            return false;
        }
        return issues.stream()
                .anyMatch(issue -> issue.getType() == Issue.IssueType.SECURITY);
    }
} 