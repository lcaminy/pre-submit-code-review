package com.renrui.review.cache;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class ReviewResult implements Serializable {
    private final List<String> issues;
    private final Instant timestamp;
    private final String fileHash;
    private final boolean isTimeout;

    public ReviewResult(List<String> issues, String fileHash, boolean isTimeout) {
        this.issues = issues;
        this.timestamp = Instant.now();
        this.fileHash = fileHash;
        this.isTimeout = isTimeout;
    }

    public List<String> getIssues() {
        return issues;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getFileHash() {
        return fileHash;
    }

    public boolean isTimeout() {
        return isTimeout;
    }
} 