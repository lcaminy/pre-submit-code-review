package com.renrui.presubmit.codereview.rule;

import com.renrui.presubmit.codereview.model.Issue;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义规则引擎
 */
public class CustomRuleEngine {
    private final List<Rule> rules;

    public CustomRuleEngine() {
        this.rules = new ArrayList<>();
    }

    /**
     * 从规则文件加载规则
     */
    public void loadRules(File ruleFile) throws IOException {
        rules.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(ruleFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split(":", 3);
                if (parts.length == 3) {
                    rules.add(new Rule(parts[0], parts[1], parts[2]));
                }
            }
        }
    }

    /**
     * 检查代码是否违反规则
     */
    public List<Issue> check(String filePath, String content) {
        List<Issue> issues = new ArrayList<>();
        String extension = FilenameUtils.getExtension(filePath);

        for (Rule rule : rules) {
            if (rule.matches(filePath, extension)) {
                List<Integer> violationLines = rule.findViolations(content);
                for (int line : violationLines) {
                    issues.add(new Issue(
                        Issue.IssueType.BEST_PRACTICE,
                        "违反自定义规则: " + rule.name,
                        filePath,
                        line,
                        rule.message
                    ));
                }
            }
        }

        checkSystemOut(filePath, content, issues);
        checkMessagePunctuation(filePath, content, issues);
        checkTodoComments(filePath, content, issues);

        return issues;
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

    private void checkSystemOut(String filePath, String content, List<Issue> issues) {
        Pattern pattern = Pattern.compile("System\\.out\\.println\\s*\\(");
        if (pattern.matcher(content).find()) {
            issues.add(createIssue(
                Issue.IssueType.STYLE,
                "代码中不应使用 System.out.println，请使用日志框架",
                filePath,
                "建议使用 SLF4J 或 Log4j 等日志框架"
            ));
        }
    }

    private void checkMessagePunctuation(String filePath, String content, List<Issue> issues) {
        Pattern pattern = Pattern.compile("\"[^\"]*[^.!?。！？]\"");
        if (pattern.matcher(content).find()) {
            issues.add(createIssue(
                Issue.IssueType.STYLE,
                "消息文本应以标点符号结尾",
                filePath,
                "请在消息文本末尾添加适当的标点符号"
            ));
        }
    }

    private void checkTodoComments(String filePath, String content, List<Issue> issues) {
        Pattern pattern = Pattern.compile("//\\s*TODO\\s*:\\s*(.+)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String todoContent = matcher.group(1).trim();
            if (todoContent.length() < 10) {
                issues.add(createIssue(
                    Issue.IssueType.WARNING,
                    "TODO 注释内容过短",
                    filePath,
                    "TODO 注释内容应不少于 10 个字，请详细描述待完成的任务"
                ));
            }
        }
    }

    /**
     * 规则类
     */
    private static class Rule {
        private final String filePattern;
        private final String name;
        private final String message;
        private final Pattern pattern;

        public Rule(String filePattern, String name, String message) {
            this.filePattern = filePattern;
            this.name = name;
            this.message = message;
            this.pattern = createPattern(name);
        }

        /**
         * 检查文件是否匹配规则
         */
        public boolean matches(String filePath, String extension) {
            if ("*".equals(filePattern)) {
                return true;
            }
            if (filePattern.startsWith("*.")) {
                return filePattern.substring(2).equals(extension);
            }
            return Pattern.compile(filePattern).matcher(filePath).matches();
        }

        /**
         * 查找违规行
         */
        public List<Integer> findViolations(String content) {
            List<Integer> lines = new ArrayList<>();
            String[] contentLines = content.split("\n");
            for (int i = 0; i < contentLines.length; i++) {
                if (pattern.matcher(contentLines[i]).find()) {
                    lines.add(i + 1);
                }
            }
            return lines;
        }

        /**
         * 创建规则匹配模式
         */
        private Pattern createPattern(String name) {
            switch (name) {
                case "no-system-out":
                    return Pattern.compile("System\\.out\\.print(ln)?\\(");
                case "no-thread-sleep":
                    return Pattern.compile("Thread\\.sleep\\(");
                case "require-javadoc":
                    return Pattern.compile("^\\s*public\\s+\\w+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{");
                default:
                    return Pattern.compile(name);
            }
        }
    }
} 