package com.xxxlin.json.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.xxxlin.json.JsonLanguageUtil
import com.xxxlin.json.editor.JsonFeatureOptions
import com.xxxlin.json.highlighting.JsonSyntaxHighlighterFactory
import com.xxxlin.json.psi.JsonStringLiteral
import com.xxxlin.json.psi.JsonValue
import com.xxxlin.utils.LogUtil
import com.xxxlin.utils.contains

/**
 * Json 字符串注解
 */
class JsonAnnotator : Annotator {

    private val slotNameRegex: Regex = Regex(
        "%[a-zA-Z_]+%",
        hashSetOf(RegexOption.IGNORE_CASE)
    )

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!JsonFeatureOptions.instance.JSON_KEY_PERCENT_SLOT_HIGHLIGHT) {
            return
        }

        if (element is JsonStringLiteral) {
            annotateString(element, holder)
        }
    }

    /**
     * 引用规则标色
     */
    private fun annotateString(element: JsonStringLiteral, holder: AnnotationHolder) {
        val text = element.text ?: return
        if (text.isEmpty()) {
            return
        }

        val textRange = TextRange(element.textRange.startOffset, element.textRange.endOffset)

        // 处理带 {} 的文本
        procSlot(text, textRange, element, holder)
    }

    /**
     * 处理带 {} 的文本
     */
    private fun procSlot(
        text: String,
        textRange: TextRange,
        element: JsonStringLiteral,
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
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(keyRange)
                .textAttributes(DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
                .create()
        }
    }
}