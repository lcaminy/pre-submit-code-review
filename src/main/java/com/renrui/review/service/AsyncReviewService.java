package com.renrui.review.service;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.renrui.review.cache.ReviewCache;
import com.renrui.review.cache.ReviewResult;

import java.util.List;
import java.util.concurrent.*;

public class AsyncReviewService {
    private static final Logger LOG = Logger.getInstance(AsyncReviewService.class);
    private static final int TIMEOUT_SECONDS = 30;
    
    private final ExecutorService executorService;
    private final ReviewCache reviewCache;
    private final Project project;

    public AsyncReviewService(Project project, ReviewCache reviewCache) {
        this.project = project;
        this.reviewCache = reviewCache;
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * 异步执行代码审查
     * @param file 待审查文件
     * @param callback 回调函数，处理审查结果
     */
    public void reviewAsync(VirtualFile file, ReviewCallback callback) {
        if (!reviewCache.needsReview(file)) {
            ReviewResult cachedResult = reviewCache.getCachedResult(file);
            if (cachedResult != null) {
                callback.onComplete(cachedResult);
                return;
            }
        }

        new Task.Backgroundable(project, "代码审查中...") {
            @Override
            public void run(ProgressIndicator indicator) {
                try {
                    Future<List<String>> future = executorService.submit(() -> performReview(file));
                    List<String> issues = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    
                    ReviewResult result = new ReviewResult(issues, file.getPath(), false);
                    reviewCache.cacheResult(file, result);
                    callback.onComplete(result);
                } catch (TimeoutException e) {
                    LOG.warn("代码审查超时: " + file.getPath());
                    ReviewResult timeoutResult = new ReviewResult(List.of("审查超时，自动放行"), file.getPath(), true);
                    reviewCache.cacheResult(file, timeoutResult);
                    callback.onComplete(timeoutResult);
                } catch (Exception e) {
                    LOG.error("代码审查失败", e);
                    callback.onError(e);
                }
            }
        }.queue();
    }

    private List<String> performReview(VirtualFile file) {
        // TODO: 实现具体的代码审查逻辑
        return List.of();
    }

    public interface ReviewCallback {
        void onComplete(ReviewResult result);
        void onError(Exception e);
    }

    public void dispose() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
} 