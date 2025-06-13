package com.renrui.presubmit.codereview.model;

/**
 * 代码审查问题模型
 * 用于表示 AI 发现的代码问题
 */
public class Issue {
    // 问题类型
    private final IssueType type;
    // 问题描述
    private final String message;
    // 修改建议
    private final String suggestion;
    // 问题文件
    private final String file;
    // 问题行号
    private final int line;

    public Issue(IssueType type, String message, String file, int line, String suggestion) {
        this.type = type;
        this.message = message;
        this.file = file;
        this.line = line;
        this.suggestion = suggestion;
    }

    // Getters
    public IssueType getType() { return type; }
    public String getMessage() { return message; }
    public String getSuggestion() { return suggestion; }
    public String getFile() { return file; }
    public int getLine() { return line; }

    /**
     * 问题类型枚举
     */
    public enum IssueType {
        BUG("Bug"),
        SECURITY("安全"),
        PERFORMANCE("性能"),
        STYLE("风格"),
        WARNING("警告"),
        MAINTAINABILITY("可维护性"),
        BEST_PRACTICE("最佳实践"),
        CODE_STYLE("代码风格");

        private final String description;

        IssueType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 