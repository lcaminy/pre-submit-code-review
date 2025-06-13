package com.renrui.review.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public class ReviewCache {
    private static final int MAX_CACHE_SIZE = 1000;
    private static final Duration CACHE_EXPIRE_AFTER = Duration.ofHours(24);

    private final Cache<String, String> fileHashCache;
    private final Cache<String, ReviewResult> reviewResultCache;
    
    public ReviewCache() {
        this.fileHashCache = Caffeine.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(CACHE_EXPIRE_AFTER)
                .build();
                
        this.reviewResultCache = Caffeine.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterWrite(CACHE_EXPIRE_AFTER)
                .build();
    }

    /**
     * 检查文件是否需要重新审查
     * @param file 待检查的文件
     * @return true 如果文件需要重新审查
     */
    public boolean needsReview(VirtualFile file) {
        try {
            String currentHash = calculateFileHash(file);
            String cachedHash = fileHashCache.getIfPresent(file.getPath());
            
            if (cachedHash == null || !cachedHash.equals(currentHash)) {
                fileHashCache.put(file.getPath(), currentHash);
                return true;
            }
            
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * 获取缓存的审查结果
     * @param file 文件
     * @return 缓存的审查结果，如果没有缓存则返回null
     */
    public ReviewResult getCachedResult(VirtualFile file) {
        return reviewResultCache.getIfPresent(file.getPath());
    }

    /**
     * 缓存审查结果
     * @param file 文件
     * @param result 审查结果
     */
    public void cacheResult(VirtualFile file, ReviewResult result) {
        reviewResultCache.put(file.getPath(), result);
    }

    /**
     * 计算文件的hash值
     */
    private String calculateFileHash(VirtualFile file) throws IOException {
        byte[] content = file.contentsToByteArray();
        return DigestUtils.md5Hex(content);
    }

    /**
     * 清除指定文件的缓存
     */
    public void invalidateCache(VirtualFile file) {
        fileHashCache.invalidate(file.getPath());
        reviewResultCache.invalidate(file.getPath());
    }
} 