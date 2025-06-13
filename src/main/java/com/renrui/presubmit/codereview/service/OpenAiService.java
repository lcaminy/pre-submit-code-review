package com.renrui.presubmit.codereview.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.renrui.presubmit.codereview.settings.AiReviewSettings;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI API 服务
 */
public class OpenAiService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private final OkHttpClient client;

    public OpenAiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 调用 GPT 生成内容
     */
    public String complete(String prompt) throws IOException {
        String apiKey = AiReviewSettings.getInstance().getApiKey();
        if (StringUtils.isBlank(apiKey)) {
            throw new IOException("API Key 未配置");
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-3.5-turbo");
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 500);
        requestBody.addProperty("prompt", prompt);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(gson.toJson(requestBody), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("请求失败: " + response);
            }

            JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
            return jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString()
                    .trim();
        }
    }

    /**
     * 生成代码修复建议
     */
    public String generateCodeFix(String code, String issue) throws IOException {
        String prompt = String.format(
            "请修复以下Java代码中的问题：\n" +
            "问题描述：%s\n" +
            "代码：\n```java\n%s\n```\n" +
            "要求：\n" +
            "1. 只返回修复后的代码，不要包含任何解释\n" +
            "2. 保持代码风格一致\n" +
            "3. 确保修复不引入新的问题",
            issue, code
        );

        return complete(prompt);
    }
} 