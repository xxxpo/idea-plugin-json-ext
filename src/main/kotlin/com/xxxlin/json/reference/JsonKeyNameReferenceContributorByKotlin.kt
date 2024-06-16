package com.xxxlin.json.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.xxxlin.json.JsonLanguageUtil
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

/**
 * kotlin字符串引用 JSON key 规则
 */
class JsonKeyNameReferenceContributorByKotlin : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(KtStringTemplateExpression::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<out PsiReference> {
                    val exp = element as KtStringTemplateExpression
                    val value = exp.text ?: return emptyArray()
                    if (value.length > 2 && value.startsWith("\"") && value.endsWith("\"")) {
                        val search = value.substring(1, value.length - 1)
                        val textRange = TextRange(1, value.length - 1)
                        return JsonLanguageUtil.findAllJsonKey(element.project, search)
                            .map {
                                JsonKeyReference(element, textRange, it)
                            }.toTypedArray()
                    }
                    return emptyArray()
                }
            }
        )
    }
}