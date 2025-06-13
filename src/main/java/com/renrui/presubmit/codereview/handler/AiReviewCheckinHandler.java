package com.renrui.presubmit.codereview.handler;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;
import com.renrui.presubmit.codereview.model.Issue;
import com.renrui.presubmit.codereview.service.AiCodeReviewService;
import com.renrui.presubmit.codereview.service.AiReviewServiceFactory;
import com.renrui.presubmit.codereview.service.impl.LocalRuleCheckService;
import com.renrui.presubmit.codereview.settings.AiReviewSettings;
import com.renrui.presubmit.codereview.ui.AiReviewDialog;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码提交拦截处理器
 * 负责在代码提交前进行拦截和审查
 */
public class AiReviewCheckinHandler extends CheckinHandler {
    private static final Logger LOG = Logger.getInstance(AiReviewCheckinHandler.class);
    // 提交面板，包含了变更文件等信息
    private final CheckinProjectPanel panel;
    // 当前项目实例
    private final Project project;
    private final AiCodeReviewService aiService;
    private final LocalRuleCheckService localRuleService;
    private JCheckBox enableAiReview;

    /**
     * 构造函数
     * @param panel 提交面板实例
     */
    public AiReviewCheckinHandler(@NotNull CheckinProjectPanel panel) {
        this.panel = panel;
        this.project = panel.getProject();
        this.aiService = AiReviewServiceFactory.getInstance();
        this.localRuleService = new LocalRuleCheckService();
    }

    @Override
    public @Nullable RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        enableAiReview = new JCheckBox("启用 AI 代码审查");
        return new RefreshableOnComponent() {
            @Override
            public JComponent getComponent() {
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(enableAiReview, BorderLayout.WEST);
                return panel;
            }

            @Override
            public void refresh() {
                // 刷新配置面板
            }

            @Override
            public void saveState() {
                // 保存配置状态
            }

            @Override
            public void restoreState() {
                // 恢复配置状态
            }
        };
    }

    /**
     * 在代码提交前执行的处理方法
     * @param executor 提交执行器
     * @return 返回是否允许提交的结果
     */
    @Override
    public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
        if (!enableAiReview.isSelected()) {
            LOG.info("AI 代码审查未启用，跳过检查");
            return ReturnResult.COMMIT;
        }

        try {
            // 获取变更文件
            Map<String, String> changedFiles = new HashMap<>();
            for (VirtualFile file : panel.getVirtualFiles()) {
                if (file != null && file.exists() && !file.isDirectory()) {
                    try {
                        changedFiles.put(file.getPath(), new String(file.contentsToByteArray()));
                        LOG.debug("读取文件内容: " + file.getPath());
                    } catch (IOException e) {
                        LOG.error("读取文件内容失败: " + file.getPath(), e);
                    }
                }
            }

            if (changedFiles.isEmpty()) {
                LOG.info("没有需要审查的文件");
                return ReturnResult.COMMIT;
            }

            LOG.info("开始代码审查，变更文件数: " + changedFiles.size());
            System.out.println("开始代码审查，变更文件数: " + changedFiles.size());
            for (String file : changedFiles.keySet()) {
                LOG.debug("待审查文件: " + file);
                System.out.println("待审查文件: " + file);
            }

            // 执行本地规则检查
            List<Issue> localIssues = localRuleService.checkRules(changedFiles);
            LOG.info("本地规则检查返回问题列表: " + localIssues);
            LOG.info("本地规则检查完成，发现问题数: " + localIssues.size());
            for (Issue issue : localIssues) {
                LOG.debug("本地规则问题: " + issue.getType() + " - " + issue.getMessage());
            }
            // 合并问题列表
            List<Issue> allIssues = new java.util.ArrayList<>();
            if(localIssues.isEmpty()){
                // 执行 AI 审查
                String commitMessage = panel.getCommitMessage();
                LOG.info("开始 AI 审查，提交信息: " + commitMessage);
                List<Issue> aiIssues = aiService.reviewChanges(changedFiles, commitMessage);
                LOG.info("AI 审查完成，发现问题数: " + aiIssues.size());
                for (Issue issue : aiIssues) {
                    LOG.debug("AI 审查问题: " + issue.getType() + " - " + issue.getMessage());
                }
                allIssues.addAll(aiIssues);
            }
            allIssues.addAll(localIssues);

            if (!allIssues.isEmpty()) {
                LOG.warn("代码审查发现问题，总数: " + allIssues.size());
                // 显示问题对话框
                AiReviewDialog dialog = new AiReviewDialog(project, allIssues);
                dialog.show();
                return dialog.isOK() ? ReturnResult.COMMIT : ReturnResult.CANCEL;
            }

            LOG.info("代码审查通过，未发现问题");
            return ReturnResult.COMMIT;
        } catch (Exception e) {
            LOG.error("代码审查过程发生错误", e);
            showErrorNotification("代码审查失败: " + e.getMessage());
            return ReturnResult.COMMIT;
        }
    }

    /**
     * 显示错误通知
     */
    private void showErrorNotification(String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("AI Code Review")
                .createNotification(
                        "代码审查",
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