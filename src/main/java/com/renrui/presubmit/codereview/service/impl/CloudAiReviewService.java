package com.renrui.presubmit.codereview.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.renrui.presubmit.codereview.model.Issue;
import com.renrui.presubmit.codereview.service.AiCodeReviewService;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;

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
    private static final String DEFAULT_API_URL = "https://api.example.com/code-review";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private final OkHttpClient client;
    private final String apiUrl;
    private final String apiKey;

    public CloudAiReviewService(String apiKey) {
        this(apiKey, DEFAULT_API_URL);
    }

    public CloudAiReviewService(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @Override
    public List<Issue> reviewChanges(Map<String, String> changedFiles, String commitMessage) {
        try {
            // 构建请求体
            RequestBody body = buildRequestBody(changedFiles, commitMessage);
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            // 发送请求
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("请求失败: " + response);
                }
                
                // 解析响应
                return parseResponse(response.body().string());
            }
        } catch (Exception e) {
            // 记录错误并返回空列表
            e.printStackTrace();
            return new ArrayList<>();
        }
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
                    (String) issue.get("suggestion"),
                    (String) issue.get("file"),
                    ((Double) issue.get("line")).intValue()
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