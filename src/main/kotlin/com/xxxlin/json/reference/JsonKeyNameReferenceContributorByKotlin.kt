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

    private val slotNameRegex: Regex = Regex(
        "\\{[a-zA-Z_]+}",
        hashSetOf(RegexOption.IGNORE_CASE)
    )

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

                        val result = mutableListOf<PsiReference>()
                        result.addAll(JsonLanguageUtil.findAllJsonKey(element.project, search).map {
                            JsonKeyReference(element, textRange, it)
                        })

                        // 处理带 {} 的文本
                        val list = slotNameRegex.findAll(value, 0)
                        val iterator = list.iterator()
                        while (iterator.hasNext()) {
                            val row = iterator.next()
                            val slotName = row.value
                            result.addAll(JsonLanguageUtil.findAllJsonKey(element.project, slotName).map {
                                JsonKeyReference(element, TextRange(row.range.first, row.range.last + 1), it)
                            })
                        }

                        return result.toTypedArray()
                    }
                    return emptyArray()
                }
            }
        )
    }
}