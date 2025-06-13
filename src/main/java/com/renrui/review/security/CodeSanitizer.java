package com.renrui.review.security;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class CodeSanitizer {
    private static final Logger LOG = Logger.getInstance(CodeSanitizer.class);

    private static final List<Pattern> SENSITIVE_PATTERNS = new ArrayList<>();
    
    static {
        // 密码模式
        SENSITIVE_PATTERNS.add(Pattern.compile("(?i)(password|pwd|passwd)\\s*=\\s*[\"'].*?[\"']"));
        // API密钥模式
        SENSITIVE_PATTERNS.add(Pattern.compile("(?i)(api[_-]?key|secret[_-]?key|access[_-]?token)\\s*=\\s*[\"'].*?[\"']"));
        // 数据库连接字符串
        SENSITIVE_PATTERNS.add(Pattern.compile("(?i)(jdbc:.*?)://.*?(?=;|$)"));
        // 私钥内容
        SENSITIVE_PATTERNS.add(Pattern.compile("-----BEGIN.*?PRIVATE KEY-----.*?-----END.*?PRIVATE KEY-----", Pattern.DOTALL));
    }

    /**
     * 对代码内容进行脱敏处理
     * @param code 原始代码
     * @return 脱敏后的代码
     */
    public static String sanitize(String code) {
        if (StringUtils.isBlank(code)) {
            return code;
        }

        String sanitizedCode = code;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            Matcher matcher = pattern.matcher(sanitizedCode);
            sanitizedCode = matcher.replaceAll(match -> {
                String prefix = match.group().substring(0, match.group().indexOf('=') + 1);
                return prefix + " \"[REDACTED]\"";
            });
        }

        return sanitizedCode;
    }

    /**
     * 检查代码是否包含敏感信息
     * @param code 要检查的代码
     * @return true 如果包含敏感信息
     */
    public static boolean containsSensitiveInfo(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }

        for (Pattern pattern : SENSITIVE_PATTERNS) {
            if (pattern.matcher(code).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 添加自定义的敏感信息模式
     * @param pattern 正则表达式模式
     */
    public static void addCustomPattern(String pattern) {
        try {
            SENSITIVE_PATTERNS.add(Pattern.compile(pattern));
        } catch (Exception e) {
            LOG.error("添加自定义敏感信息模式失败", e);
        }
    }
} 