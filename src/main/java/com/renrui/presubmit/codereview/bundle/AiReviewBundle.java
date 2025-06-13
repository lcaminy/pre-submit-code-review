package com.renrui.presubmit.codereview.bundle;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class AiReviewBundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE = "messages.AiReviewBundle";
    private static final AiReviewBundle INSTANCE = new AiReviewBundle();

    private AiReviewBundle() {
        super(BUNDLE);
    }

    public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
} 