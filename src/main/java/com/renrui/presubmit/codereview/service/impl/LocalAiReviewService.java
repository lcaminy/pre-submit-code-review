package com.renrui.presubmit.codereview.service.impl;

import ai.onnxruntime.*;
import com.renrui.presubmit.codereview.model.Issue;
import com.renrui.presubmit.codereview.service.AiCodeReviewService;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * 本地 AI 代码审查服务实现
 * 使用 ONNX Runtime 加载预训练模型
 */
public class LocalAiReviewService implements AiCodeReviewService {
    private static final String MODEL_RESOURCE_PATH = "/models/code_review_model.onnx";
    private final OrtEnvironment env;
    private final OrtSession session;
    private final Map<String, Float[]> vectorCache;

    public LocalAiReviewService() throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        this.session = createSession();
        this.vectorCache = new HashMap<>();
    }

    @Override
    public List<Issue> reviewChanges(Map<String, String> changedFiles, String commitMessage) {
        List<Issue> issues = new ArrayList<>();
        try {
            // 对每个文件进行向量化处理
            for (Map.Entry<String, String> entry : changedFiles.entrySet()) {
                String file = entry.getKey();
                String content = entry.getValue();
                
                // 获取代码向量
                float[] codeVector = vectorizeCode(content);
                
                // 运行模型推理
                OnnxTensor input = OnnxTensor.createTensor(env, codeVector);
                Map<String, OnnxTensor> inputs = Map.of("input", input);
                
                try (OrtSession.Result results = session.run(inputs)) {
                    // 解析模型输出
                    float[] predictions = ((float[][]) results.get(0).getValue())[0];
                    issues.addAll(interpretPredictions(predictions, file));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return issues;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.LOCAL;
    }

    /**
     * 创建 ONNX 运行时会话
     */
    private OrtSession createSession() throws OrtException {
        try {
            // 从资源文件加载模型
            Path modelPath = extractModelFile();
            
            // 配置会话选项
            OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
            sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT);
            
            // 创建会话
            return env.createSession(modelPath.toString(), sessionOptions);
        } catch (IOException e) {
            throw new OrtException("无法加载模型文件: " + e.getMessage());
        }
    }

    /**
     * 从 JAR 中提取模型文件
     */
    private Path extractModelFile() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(MODEL_RESOURCE_PATH)) {
            if (is == null) {
                throw new IOException("找不到模型文件: " + MODEL_RESOURCE_PATH);
            }
            
            Path tempFile = Files.createTempFile("code_review_model", ".onnx");
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    /**
     * 将代码转换为向量
     */
    private float[] vectorizeCode(String code) {
        // TODO: 实现代码向量化逻辑
        // 这里应该实现：
        // 1. 代码标准化
        // 2. 分词
        // 3. 向量化
        return new float[512]; // 示例：返回 512 维向量
    }

    /**
     * 解释模型预测结果
     */
    private List<Issue> interpretPredictions(float[] predictions, String file) {
        List<Issue> issues = new ArrayList<>();
        
        // 示例：根据预测值生成问题
        if (predictions[0] > 0.8) {
            issues.add(new Issue(
                Issue.IssueType.SECURITY,
                "发现潜在的安全漏洞",
                "建议进行安全性检查",
                file,
                1
            ));
        }
        
        if (predictions[1] > 0.7) {
            issues.add(new Issue(
                Issue.IssueType.PERFORMANCE,
                "可能存在性能问题",
                "建议优化代码性能",
                file,
                1
            ));
        }
        
        return issues;
    }

    /**
     * 关闭资源
     */
    public void close() throws OrtException {
        if (session != null) {
            session.close();
        }
        if (env != null) {
            env.close();
        }
    }
} 