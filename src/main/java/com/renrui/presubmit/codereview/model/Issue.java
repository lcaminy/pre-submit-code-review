package com.renrui.presubmit.codereview.model;

/**
 * 代码审查问题模型
 * 用于表示 AI 发现的代码问题
 */
public class Issue {
    // 问题类型
    private IssueType type;
    // 问题描述
    private String message;
    // 修改建议
    private String suggestion;
    // 问题文件
    private String file;
    // 问题行号
    private int line;

    public Issue(IssueType type, String message, String suggestion, String file, int line) {
        this.type = type;
        this.message = message;
        this.suggestion = suggestion;
        this.file = file;
        this.line = line;
    }

    // Getters
    public IssueType getType() { return type; }
    public String getMessage() { return message; }
    public String getSuggestion() { return suggestion; }
    public String getFile() { return file; }
    public int getLine() { return line; }

    // 问题类型枚举
    public enum IssueType {
        SECURITY("安全问题"),
        PERFORMANCE("性能问题"),
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