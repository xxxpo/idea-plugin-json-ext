// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JsonDialectUtil {
    public static boolean isStandardJson(@NotNull PsiElement element) {
        return isStandardJson(getLanguageOrDefaultJson(element));
    }

    public static @NotNull Language getLanguageOrDefaultJson(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file != null) {
            Language language = file.getLanguage();
            if (language instanceof com.xxxlin.json.JsonLanguage) return language;
        }
        return ObjectUtils.coalesce(ObjectUtils.tryCast(element.getLanguage(), com.xxxlin.json.JsonLanguage.class), com.xxxlin.json.JsonLanguage.INSTANCE);
    }

    public static boolean isStandardJson(@Nullable Language language) {
        return language == JsonLanguage.INSTANCE;
    }
}
