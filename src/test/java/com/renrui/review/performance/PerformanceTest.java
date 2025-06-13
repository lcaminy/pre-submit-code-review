package com.renrui.review.performance;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.renrui.review.cache.ReviewCache;
import com.renrui.review.service.AsyncReviewService;
import com.renrui.review.cache.ReviewResult;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceTest extends BasePlatformTestCase {
    private ReviewCache reviewCache;
    private AsyncReviewService reviewService;
    private List<VirtualFile> testFiles;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Project project = getProject();
        reviewCache = new ReviewCache();
        reviewService = new AsyncReviewService(project, reviewCache);
        testFiles = generateTestFiles(100);
    }

    @Test
    public void testBatchReviewPerformance() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(testFiles.size());
        AtomicInteger completedCount = new AtomicInteger(0);

        for (VirtualFile file : testFiles) {
            reviewService.reviewAsync(file, new AsyncReviewService.ReviewCallback() {
                @Override
                public void onComplete(ReviewResult result) {
                    completedCount.incrementAndGet();
                    latch.countDown();
                }

                @Override
                public void onError(Exception e) {
                    fail("Review failed: " + e.getMessage());
                    latch.countDown();
                }
            });
        }

        // 等待所有审查完成或超时
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - startTime;

        assertTrue("Performance test timed out", completed);
        assertEquals("Not all files were reviewed", testFiles.size(), completedCount.get());
        assertTrue("Total processing time exceeded 5 seconds", totalTime <= 5000);

        // 输出性能报告
        System.out.println("性能测试报告:");
        System.out.println("总文件数: " + testFiles.size());
        System.out.println("总处理时间: " + totalTime + "ms");
        System.out.println("平均每个文件处理时间: " + (totalTime / testFiles.size()) + "ms");
        System.out.println("缓存命中率: " + calculateCacheHitRate() + "%");
    }

    private List<VirtualFile> generateTestFiles(int count) {
        List<VirtualFile> files = new ArrayList<>();
        // TODO: 实现测试文件生成逻辑
        return files;
    }

    private double calculateCacheHitRate() {
        // TODO: 实现缓存命中率计算逻辑
        return 0.0;
    }

    @Override
    protected void tearDown() throws Exception {
        reviewService.dispose();
        super.tearDown();
    }
} 