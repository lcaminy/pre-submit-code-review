package com.renrui.presubmit.codereview.service.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.renrui.presubmit.codereview.model.Issue;
import com.renrui.presubmit.codereview.settings.AiReviewSettings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地规则检查服务
 */
public class LocalRuleCheckService {
    private static final Logger LOG = Logger.getInstance(LocalRuleCheckService.class);
    private final AiReviewSettings settings;

    public LocalRuleCheckService() {
        this.settings = AiReviewSettings.getInstance();
    }

    /**
     * 执行本地规则检查
     */
    public List<Issue> checkRules(@NotNull Map<String, String> files) {
        List<Issue> issues = new ArrayList<>();
        LOG.info("开始本地规则检查，文件数: " + files.size());

        for (Map.Entry<String, String> entry : files.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();
            LOG.debug("检查文件: " + filePath);

            // 检查 System.out.println
            if (settings.isEnableSystemOutCheck()) {
                checkSystemOut(filePath, content, issues);
            }

            // 检查消息结尾标点
            if (settings.isEnableMessagePunctuationCheck()) {
                checkMessagePunctuation(filePath, content, issues);
            }

            // 检查 TODO 注释
            if (settings.isEnableTodoCheck()) {
                checkTodoComments(filePath, content, issues);
            }
        }

        LOG.info("本地规则检查完成，发现问题数: " + issues.size());
        LOG.info("本地规则检查问题列表: " + issues);
        return issues;
    }

    /**
     * 检查 System.out.println 语句
     */
    private void checkSystemOut(String filePath, String content, List<Issue> issues) {
        Pattern pattern = Pattern.compile("System\\.out\\.println\\s*\\(");
        Matcher matcher = pattern.matcher(content);
        
        for (int lineNumber = 1; matcher.find(); lineNumber++) {
            issues.add(createIssue(Issue.IssueType.STYLE, "发现 System.out.println 语句", filePath, "建议使用日志框架替代"));
            LOG.info("System.out 问题: " + filePath + " 行号: " + lineNumber);
        }
    }

    /**
     * 检查消息结尾标点
     */
    private void checkMessagePunctuation(String filePath, String content, List<Issue> issues) {
        Pattern pattern = Pattern.compile("\"[^\"]*[^.!?。！？]\"");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            LOG.debug("发现消息文本缺少标点: " + filePath);
            for (int lineNumber = 1; matcher.find(); lineNumber++) {
                issues.add(createIssue(
                    Issue.IssueType.STYLE,
                    "消息文本应以标点符号结尾",
                    filePath,
                    "请在消息文本末尾添加适当的标点符号"
                ));
            }
            LOG.info("消息标点问题: " + filePath);
        }
    }

    /**
     * 检查 TODO 注释
     * 要求 TODO 任务内容不少于 10 个字
     */
    private void checkTodoComments(String filePath, String content, List<Issue> issues) {
        Pattern pattern = Pattern.compile("//\\s*TODO\\s*:\\s*(.+)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String todoContent = matcher.group(1).trim();
            if (todoContent.length() < 10) {
                LOG.debug("发现过短的 TODO 注释: " + filePath);
                issues.add(createIssue(
                    Issue.IssueType.WARNING,
                    "TODO 注释内容过短",
                    filePath,
                    "TODO 注释内容应不少于 10 个字，请详细描述待完成的任务"
                ));
            }
        }
    }

    private Issue createIssue(Issue.IssueType type, String message, String file, String suggestion) {
        return new Issue(
            type,
            message,
            file,
            0,  // 默认行号
            suggestion
        );
    }
} 