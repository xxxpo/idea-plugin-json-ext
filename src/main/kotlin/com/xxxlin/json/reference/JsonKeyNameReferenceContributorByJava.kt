package com.xxxlin.json.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.xxxlin.json.JsonLanguageUtil
import com.xxxlin.json.editor.JsonFeatureOptions
import com.xxxlin.utils.LogUtil
import com.xxxlin.utils.contains

/**
 * java字符串引用 JSON key 规则
 */
class JsonKeyNameReferenceContributorByJava : PsiReferenceContributor() {

    private val slotNameRegex: Regex = Regex(
        "\\{[a-zA-Z_]+}",
        hashSetOf(RegexOption.IGNORE_CASE)
    )

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiLiteralExpression::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    if (!JsonFeatureOptions.instance.MATCH_STRING) {
                        return emptyArray()
                    }

                    val exp = element as PsiLiteralExpression
                    val text = exp.value as String? ?: return PsiReference.EMPTY_ARRAY
                    if (text.isEmpty()) {
                        return emptyArray()
                    }
                    val textRange = TextRange(1, text.length + 1)

                    val result = mutableListOf<JsonKeyReference>()
                    procString(result, text, textRange, element)
                    procSlot(result, text, textRange, element)

                    return result.toTypedArray()
                }
            }
        )
    }

    private fun procString(
        result: MutableList<JsonKeyReference>,
        text: String,
        textRange: TextRange,
        element: PsiElement
    ) {
        kotlin.run {
            val list = JsonLanguageUtil.findAllJsonKey(element.project, text).map {
                JsonKeyReference(element, textRange, it)
            }
            result.addAll(list)
        }

        if (text.contains('/', '#')) {
            val keys = text.split("/", "#")
            val jsonProperty = JsonLanguageUtil.findAllJsonKeys(element.project, keys)
            val list = jsonProperty.map {
                var begin = 1
                val list = mutableListOf<JsonKeyReference>()
                for (index in keys.indices) {
                    val keyRange = TextRange(begin, begin + keys[index].length)
                    begin += keys[index].length + 1
                    list.add(JsonKeyReference(element, keyRange, it[index]))
                }
                list
            }.flatten()
            result.addAll(list)
        }
    }

    private fun procSlot(
        result: MutableList<JsonKeyReference>,
        text: String,
        textRange: TextRange,
        element: PsiElement
    ) {
        // 处理带 {} 的文本
        val list = slotNameRegex.findAll(text, 0)
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val row = iterator.next()
            val range = row.range
            val slotName = row.value
            val keyRange = TextRange(
                textRange.startOffset + range.first,
                textRange.startOffset + range.last + 1
            )
            result.addAll(JsonLanguageUtil.findAllJsonKey(element.project, slotName).map {
                JsonKeyReference(element, keyRange, it)
            })
        }
    }
}