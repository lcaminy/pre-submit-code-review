package com.renrui.presubmit.codereview.fix;

import com.renrui.presubmit.codereview.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CommitMessageFixerTest {
    @Mock
    private OpenAiService openAiService;
    
    private CommitMessageFixer fixer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fixer = new CommitMessageFixer(openAiService);
    }

    @Test
    void shouldAddPeriodToChineseMessage() {
        assertEquals("修复空指针异常。", fixer.fix("修复空指针异常"));
    }

    @Test
    void shouldAddExclamationToFeatureMessage() {
        assertEquals("新增用户管理功能！", fixer.fix("新增用户管理功能"));
    }

    @Test
    void shouldAddQuestionMarkToWhyMessage() {
        assertEquals("为什么会出现这个错误？", fixer.fix("为什么会出现这个错误"));
    }

    @Test
    void shouldNotModifyMessageWithPunctuation() {
        String message = "修复空指针异常。";
        assertEquals(message, fixer.fix(message));
    }

    @Test
    void shouldEnhanceVagueMessage() throws Exception {
        String vague = "修改代码";
        String enhanced = "修改用户认证模块中的密码验证逻辑，提高安全性。";
        when(openAiService.complete(anyString())).thenReturn(enhanced);
        
        assertEquals(enhanced, fixer.fix(vague));
    }

    @Test
    void shouldNotEnhanceClearMessage() {
        String clear = "修复用户注册时的邮箱验证逻辑，解决重复注册问题";
        assertEquals(clear + "。", fixer.fix(clear));
    }

    @Test
    void shouldHandleEmptyMessage() {
        assertEquals("", fixer.fix(""));
        assertEquals(null, fixer.fix(null));
    }
} 