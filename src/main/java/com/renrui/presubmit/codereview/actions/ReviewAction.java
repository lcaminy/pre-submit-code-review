package com.renrui.presubmit.codereview.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.renrui.presubmit.codereview.service.AsyncAiReviewService;
import org.jetbrains.annotations.NotNull;

/**
 * 代码审查动作
 */
public class ReviewAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            AsyncAiReviewService.getInstance(project).reviewChanges();
        }
    }
} 