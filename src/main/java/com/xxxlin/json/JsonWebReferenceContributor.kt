// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json

import com.intellij.openapi.paths.GlobalPathReferenceProvider
import com.intellij.openapi.paths.WebReference
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.xxxlin.json.psi.JsonProperty
import com.xxxlin.json.psi.JsonStringLiteral

internal class JsonWebReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral::class.java),
            object : PsiReferenceProvider() {
                override fun acceptsTarget(target: PsiElement): Boolean {
                    return false // web references do not point to any real PsiElement
                }

                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    if (element !is JsonStringLiteral) return PsiReference.EMPTY_ARRAY

                    val parent = element.getParent() as? JsonProperty ?: return PsiReference.EMPTY_ARRAY

                    val jsonValueElement = parent.value
                    if (element !== jsonValueElement) return PsiReference.EMPTY_ARRAY

                    if (element.getTextLength() > 1000) return PsiReference.EMPTY_ARRAY // JSON may be used as data format for huge strings

                    if (!element.textContains(':')) return PsiReference.EMPTY_ARRAY

                    val textValue: String = element.value

                    if (GlobalPathReferenceProvider.isWebReferenceUrl(textValue)) {
                        val valueTextRange = ElementManipulators.getValueTextRange(element)
                        if (valueTextRange.isEmpty) return PsiReference.EMPTY_ARRAY

                        return arrayOf(WebReference(element, valueTextRange, textValue))
                    }

                    return PsiReference.EMPTY_ARRAY
                }
            },
            PsiReferenceRegistrar.LOWER_PRIORITY
        )
    }
}
