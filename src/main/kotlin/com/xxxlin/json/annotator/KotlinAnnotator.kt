package com.xxxlin.json.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.xxxlin.json.JsonLanguageUtil
import com.xxxlin.json.editor.JsonFeatureOptions
import com.xxxlin.json.highlighting.JsonSyntaxHighlighterFactory
import com.xxxlin.utils.LogUtil
import com.xxxlin.utils.contains
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
        if (!JsonFeatureOptions.instance.MATCH_STRING) {
            return
        }

        if (element is KtStringTemplateExpression) {
            annotateString(element, holder)
        }
    }

    /**
     * 引用规则标色
     */
    private fun annotateString(element: KtStringTemplateExpression, holder: AnnotationHolder) {
        var text = element.text ?: return
        if (text.isEmpty()) {
            return
        }

        if (text.length <= 2 || !text.startsWith("\"") || !text.endsWith("\"")) {
            return
        }

        // 去除两端双引号
        text = text.substring(1, text.length - 1)
        val textRange = TextRange(element.textRange.startOffset + 1, element.textRange.endOffset - 1)

        // 处理普通文本
        procString(text, textRange, element, holder)
        // 处理带 {} 的文本
        if (JsonFeatureOptions.instance.MATCH_STRING_SLOT_BRACE) {
            procSlot(text, textRange, element, holder)
        }
    }

    /**
     * 处理带 {} 的文本
     */
    private fun procSlot(
        text: String,
        textRange: TextRange,
        element: KtStringTemplateExpression,
        holder: AnnotationHolder
    ) {
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
            procString(slotName, keyRange, element, holder)
        }
    }

    /**
     * 查字字符串引用高亮
     */
    private fun procString(
        text: String,
        textRange: TextRange,
        element: KtStringTemplateExpression,
        holder: AnnotationHolder
    ) {
        // 处理多级key
        if (text.contains('/', '#')) {
            val keys = text.split("/", "#")
            val jsonProperty = JsonLanguageUtil.hasJsonKeys(element.project, keys)
            if (jsonProperty != null) {
                var begin = textRange.startOffset
                for (index in keys.indices) {
                    val key = keys[index]
                    val keyRange = TextRange(begin, begin + key.length)
                    begin += key.length + 1
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(keyRange)
                        .textAttributes(JsonSyntaxHighlighterFactory.JSON_NUMBER)
                        .create()
                }
            }
        }

        // 完整文本
        val jsonProperty = JsonLanguageUtil.hasJsonKey(element.project, text)
        if (jsonProperty != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(textRange)
                .textAttributes(JsonSyntaxHighlighterFactory.JSON_NUMBER)
                .create()
        }
    }
}