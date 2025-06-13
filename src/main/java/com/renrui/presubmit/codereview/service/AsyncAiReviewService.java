package com.renrui.presubmit.codereview.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.vfs.VirtualFile;
import com.renrui.presubmit.codereview.model.Issue;
import com.renrui.presubmit.codereview.settings.AiReviewSettings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步 AI 代码审查服务
 * 负责在后台执行代码审查任务
 */
@Service(Service.Level.PROJECT)
@State(
    name = "AsyncAiReviewService",
    storages = {@Storage("asyncAiReviewService.xml")}
)
public final class AsyncAiReviewService {
    private final Project project;
    private final AiCodeReviewService aiService;

    public AsyncAiReviewService(@NotNull Project project) {
        this.project = project;
        this.aiService = AiReviewServiceFactory.getInstance();
    }

    public static AsyncAiReviewService getInstance(@NotNull Project project) {
        return project.getService(AsyncAiReviewService.class);
    }

    /**
     * 执行代码审查
     */
    public void reviewChanges() {
        if (!AiReviewSettings.getInstance().isEnabled()) {
            return;
        }

        // 收集变更文件
        Map<String, String> changedFiles = collectChangedFiles();
        if (changedFiles.isEmpty()) {
            return;
        }

        // 异步执行代码审查
        new Thread(() -> {
            try {
                List<Issue> issues = aiService.reviewChanges(changedFiles, "");
                if (!issues.isEmpty()) {
                    showIssuesNotification(issues);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorNotification("代码审查失败: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 收集变更文件内容
     */
    private Map<String, String> collectChangedFiles() {
        Map<String, String> files = new HashMap<>();
        VirtualFile[] virtualFiles = project.getBaseDir().getChildren();
        
        for (VirtualFile file : virtualFiles) {
            if (file != null && file.exists() && !file.isDirectory()) {
                try {
                    files.put(file.getPath(), new String(file.contentsToByteArray()));
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorNotification("无法读取文件: " + file.getPath());
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

        com.intellij.notification.NotificationGroupManager.getInstance()
                .getNotificationGroup("AI Code Review")
                .createNotification(
                        "代码审查结果",
                        message.toString(),
                        com.intellij.notification.NotificationType.WARNING)
                .notify(project);
    }

    /**
     * 显示错误通知
     */
    private void showErrorNotification(String message) {
        com.intellij.notification.NotificationGroupManager.getInstance()
                .getNotificationGroup("AI Code Review")
                .createNotification(
                        "代码审查",
                        message,
                        com.intellij.notification.NotificationType.ERROR)
                .notify(project);
    }

    public CompletableFuture<List<Issue>> reviewChangesAsync(Map<String, String> changedFiles, String commitMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return aiService.reviewChanges(changedFiles, commitMessage);
            } catch (Exception e) {
                throw new RuntimeException("Failed to review changes", e);
            }
        });
    }
} 