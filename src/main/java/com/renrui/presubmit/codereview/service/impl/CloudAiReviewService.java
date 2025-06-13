package com.renrui.presubmit.codereview.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.renrui.presubmit.codereview.model.Issue;
import com.renrui.presubmit.codereview.service.AiCodeReviewService;
import com.renrui.presubmit.codereview.settings.AiReviewSettings;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 云端 AI 代码审查服务实现
 */
public class CloudAiReviewService implements AiCodeReviewService {
    private static final Logger LOG = Logger.getInstance(CloudAiReviewService.class);
    private static final String API_URL = "https://api.example.com/review";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final Gson gson;
    private final String apiKey;
    private final String modelPath;

    public CloudAiReviewService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.apiKey = System.getenv("AI_REVIEW_API_KEY");
        AiReviewSettings settings = AiReviewSettings.getInstance();
        this.modelPath = settings.getModelPath();
        LOG.info("CloudAiReviewService 初始化完成");
    }

    @Override
    public List<Issue> reviewChanges(Map<String, String> changedFiles, String commitMessage) {
        try {
            LOG.info("开始云端 AI 审查，文件数: " + changedFiles.size());
            LOG.debug("提交信息: " + commitMessage);

            // 构建请求体
            Map<String, Object> requestBody = Map.of(
                "files", changedFiles,
                "commitMessage", commitMessage
            );

            // 发送请求
            Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(gson.toJson(requestBody), JSON))
                .build();

            LOG.debug("发送 API 请求: " + API_URL);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String error = "API request failed: " + response;
                    LOG.error(error);
                    throw new IOException(error);
                }

                String responseBody = response.body().string();
                LOG.debug("收到 API 响应: " + responseBody);

                Type listType = new TypeToken<ArrayList<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> issues = gson.fromJson(responseBody, listType);
                List<Issue> result = convertToIssues(issues);

                LOG.info("云端 AI 审查完成，发现问题数: " + result.size());
                return result;
            }
        } catch (IOException e) {
            LOG.error("云端 AI 审查失败", e);
            throw new RuntimeException("Failed to review changes", e);
        }
    }

    private List<Issue> convertToIssues(List<Map<String, Object>> rawIssues) {
        List<Issue> issues = new ArrayList<>();
        for (Map<String, Object> issue : rawIssues) {
            Issue.IssueType type = getIssueType((String) issue.get("type"));
            String message = (String) issue.get("message");
            String file = (String) issue.get("file");
            String suggestion = (String) issue.get("suggestion");

            LOG.debug("转换问题: " + type + " - " + message + " (" + file + ")");

            issues.add(new Issue(
                type,
                message,
                file,
                0,  // 默认行号
                suggestion
            ));
        }
        return issues;
    }

    private Issue.IssueType getIssueType(String type) {
        return switch (type.toLowerCase()) {
            case "bug" -> Issue.IssueType.BUG;
            case "security" -> Issue.IssueType.SECURITY;
            case "performance" -> Issue.IssueType.PERFORMANCE;
            case "style" -> Issue.IssueType.STYLE;
            case "warning" -> Issue.IssueType.WARNING;
            case "maintainability" -> Issue.IssueType.MAINTAINABILITY;
            case "best_practice" -> Issue.IssueType.BEST_PRACTICE;
            case "code_style" -> Issue.IssueType.CODE_STYLE;
            default -> Issue.IssueType.WARNING;
        };
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.CLOUD;
    }

    /**
     * 构建请求体
     */
    private RequestBody buildRequestBody(Map<String, String> changedFiles, String commitMessage) {
        Map<String, Object> requestMap = Map.of(
                "files", sanitizeCode(changedFiles),
                "commitMessage", StringUtils.defaultString(commitMessage, ""),
                "options", Map.of(
                        "language", "java",
                        "checkStyle", true,
                        "checkSecurity", true
                )
        );
        return RequestBody.create(gson.toJson(requestMap), JSON);
    }

    /**
     * 对代码进行脱敏处理
     */
    private Map<String, String> sanitizeCode(Map<String, String> files) {
        // TODO: 实现代码脱敏逻辑
        return files;
    }

    /**
     * 解析 API 响应
     */
    private List<Issue> parseResponse(String responseBody) {
        Type listType = new TypeToken<ArrayList<Map<String, Object>>>(){}.getType();
        List<Map<String, Object>> issues = gson.fromJson(responseBody, listType);
        
        List<Issue> result = new ArrayList<>();
        for (Map<String, Object> issue : issues) {
            result.add(new Issue(
                    parseIssueType((String) issue.get("type")),
                    (String) issue.get("message"),
                    (String) issue.get("file"),
                    ((Double) issue.get("line")).intValue(),
                    (String) issue.get("suggestion")
            ));
        }
        return result;
    }

    /**
     * 解析问题类型
     */
    private Issue.IssueType parseIssueType(String type) {
        try {
            return Issue.IssueType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Issue.IssueType.BEST_PRACTICE;
        }
    }
} 