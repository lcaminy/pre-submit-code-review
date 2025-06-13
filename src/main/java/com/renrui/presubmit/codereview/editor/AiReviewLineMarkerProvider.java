package com.renrui.presubmit.codereview.editor;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import com.intellij.util.Function;
import com.renrui.presubmit.codereview.model.Issue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 代码审查行标记提供器
 * 用于在编辑器中显示问题标记
 */
public class AiReviewLineMarkerProvider implements LineMarkerProvider {
    private static final Icon AI_ICON = IconLoader.getIcon("/icons/ai.svg", AiReviewLineMarkerProvider.class);
    private static final Map<Integer, Issue> lineToIssueMap = new HashMap<>();

    /**
     * 更新问题列表
     */
    public static void updateIssues(List<Issue> issues) {
        lineToIssueMap.clear();
        for (Issue issue : issues) {
            lineToIssueMap.put(issue.getLine(), issue);
        }
    }

    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        Document document = element.getContainingFile().getViewProvider().getDocument();
        if (document == null) return null;

        int line = document.getLineNumber(element.getTextOffset()) + 1;
        Issue issue = lineToIssueMap.get(line);
        if (issue == null) return null;

        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                AI_ICON,
                element1 -> getTooltipText(issue),
                null,
                GutterIconRenderer.Alignment.RIGHT,
                () -> "AI 代码审查建议"
        );
    }

    /**
     * 获取提示文本
     */
    private String getTooltipText(Issue issue) {
        return String.format(
                "<html><body>" +
                "<div style='padding: 5px;'>" +
                "<b style='color: %s'>%s</b><br>" +
                "<p>%s</p>" +
                "<hr style='border: none; border-top: 1px solid #ccc;'>" +
                "<p><b>建议：</b>%s</p>" +
                "</div>" +
                "</body></html>",
                getTypeColor(issue.getType()),
                issue.getType().getDescription(),
                issue.getMessage(),
                issue.getSuggestion()
        );
    }

    /**
     * 获取问题类型对应的颜色
     */
    private String getTypeColor(Issue.IssueType type) {
        return switch (type) {
            case SECURITY -> "#ff0000";
            case PERFORMANCE -> "#ffa500";
            case MAINTAINABILITY -> "#0000ff";
            case BEST_PRACTICE -> "#008000";
            case CODE_STYLE -> "#808080";
        };
    }
} 