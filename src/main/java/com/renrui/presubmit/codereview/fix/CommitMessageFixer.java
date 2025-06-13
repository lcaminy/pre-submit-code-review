package com.renrui.presubmit.codereview.fix;

import com.renrui.presubmit.codereview.service.OpenAiService;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 提交消息修复器
 */
public class CommitMessageFixer {
    private static final Pattern ENDS_WITH_PUNCTUATION = Pattern.compile("[.。!！?？]$");
    private static final Pattern CONTAINS_CHINESE = Pattern.compile("[\u4e00-\u9fa5]");
    private final OpenAiService openAiService;

    public CommitMessageFixer(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    /**
     * 修复提交消息
     * @param message 原始消息
     * @return 修复后的消息
     */
    public String fix(String message) {
        if (StringUtils.isBlank(message)) {
            return message;
        }

        // 修复标点符号
        String fixedMessage = fixPunctuation(message);
        
        // 检查是否需要增强描述
        if (isVagueDescription(fixedMessage)) {
            fixedMessage = enhanceDescription(fixedMessage);
        }

        return fixedMessage;
    }

    /**
     * 修复标点符号
     */
    private String fixPunctuation(String message) {
        if (ENDS_WITH_PUNCTUATION.matcher(message).find()) {
            return message;
        }

        // 根据内容选择合适的标点
        if (message.contains("修复") || message.contains("fix") || message.contains("解决")) {
            return message + "。";
        } else if (message.contains("优化") || message.contains("改进") || message.contains("更新")) {
            return message + "。";
        } else if (message.contains("新增") || message.contains("添加") || message.contains("feat")) {
            return message + "！";
        } else if (message.toLowerCase().contains("why") || message.contains("为什么")) {
            return message + "？";
        }

        // 默认使用句号
        return message + (CONTAINS_CHINESE.matcher(message).find() ? "。" : ".");
    }

    /**
     * 检查是否是模糊描述
     */
    private boolean isVagueDescription(String message) {
        // 检查长度
        if (message.length() < 10) {
            return true;
        }

        // 检查常见模糊词
        String lowerMessage = message.toLowerCase();
        String[] vagueWords = {"修改", "更新", "改动", "调整", "优化", "fix", "update", "change"};
        for (String word : vagueWords) {
            if (lowerMessage.equals(word) || lowerMessage.startsWith(word + " ") || 
                lowerMessage.startsWith(word + "：") || lowerMessage.startsWith(word + ":")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 增强提交描述
     */
    private String enhanceDescription(String message) {
        try {
            String prompt = String.format(
                "请为以下Git提交消息生成更详细的描述（保持原有语言，中文回复中文，英文回复英文）：\n%s\n" +
                "要求：\n" +
                "1. 保持简洁，不超过100字\n" +
                "2. 说明修改内容和原因\n" +
                "3. 保持专业性\n" +
                "4. 如果原消息已经足够清晰，返回原消息", 
                message
            );

            String enhancedMessage = openAiService.complete(prompt);
            return enhancedMessage != null ? enhancedMessage : message;
        } catch (Exception e) {
            e.printStackTrace();
            return message;
        }
    }
} 