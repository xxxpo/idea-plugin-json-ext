package com.xxxlin.json.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.xxxlin.json.JsonLanguageUtil
import com.xxxlin.json.highlighting.JsonSyntaxHighlighterFactory
import com.xxxlin.utils.LogUtil
import org.jetbrains.kotlin.psi.KtStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

/**
 * Kotlin 字符串注解
 */
class KotlinAnnotator : Annotator {

    private val slotNameRegex: Regex = Regex(
        "\\{[a-zA-Z_]+}",
        hashSetOf(RegexOption.IGNORE_CASE)
    )

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is KtStringTemplateExpression) {
            annotateString(element, holder)
        }
    }

    /**
     * 引用规则标色
     */
    private fun annotateString(element: KtStringTemplateExpression, holder: AnnotationHolder) {
        val value = element.text
        if (value.length > 2 && value.startsWith("\"") && value.endsWith("\"")) {
            val search = value.substring(1, value.length - 1)
            var jsonProperty = JsonLanguageUtil.hasJsonKey(element.project, search)
            if (jsonProperty != null) {
                val keyRange = TextRange(element.textRange.startOffset + 1, element.textRange.endOffset - 1)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(keyRange)
                    .textAttributes(JsonSyntaxHighlighterFactory.JSON_NUMBER)
                    .create()
            }

            // 处理带 {} 的文本
            val list = slotNameRegex.findAll(value, 0)
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val row = iterator.next()
                val range = row.range
                val slotName = row.value
                jsonProperty = JsonLanguageUtil.hasJsonKey(element.project, slotName)
                if (jsonProperty != null) {
                    val keyRange = TextRange(element.textRange.startOffset + range.first,
                        element.textRange.startOffset + range.last + 1)
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(keyRange)
                        .textAttributes(JsonSyntaxHighlighterFactory.JSON_NUMBER)
                        .create()
                }
            }
        }
    }
}