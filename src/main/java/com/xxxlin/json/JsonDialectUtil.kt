// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.util.ObjectUtils

object JsonDialectUtil {
    fun isStandardJson(element: PsiElement): Boolean {
        return isStandardJson(getLanguageOrDefaultJson(element))
    }

    @JvmStatic
    fun getLanguageOrDefaultJson(element: PsiElement): Language {
        val file = element.containingFile
        if (file != null) {
            val language = file.language
            if (language is JsonLanguage) return language
        }
        return ObjectUtils.coalesce(
            ObjectUtils.tryCast(element.language, JsonLanguage::class.java),
            JsonLanguage.INSTANCE
        )
    }

    private fun isStandardJson(language: Language?): Boolean {
        return language === JsonLanguage.INSTANCE
    }
}
