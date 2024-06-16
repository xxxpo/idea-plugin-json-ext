package com.xxxlin.json.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.xxxlin.json.JsonLanguageUtil

/**
 * java字符串引用 JSON key 规则
 */
class JsonKeyNameReferenceContributorByJava : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiLiteralExpression::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val exp = element as PsiLiteralExpression
                    val value = exp.value as String? ?: return PsiReference.EMPTY_ARRAY
                    val textRange = TextRange(1, value.length + 1)
                    return JsonLanguageUtil.findAllJsonKey(element.project, value)
                        .map {
                            JsonKeyReference(element, textRange, it)
                        }.toTypedArray()
                }
            }
        )
    }
}