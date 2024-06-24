// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.psi

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.AbstractElementManipulator
import com.intellij.util.IncorrectOperationException

class JsonStringLiteralManipulator : AbstractElementManipulator<JsonStringLiteral>() {
    @Throws(IncorrectOperationException::class)
    override fun handleContentChange(
        element: JsonStringLiteral,
        range: TextRange,
        newContent: String
    ): JsonStringLiteral {
        assert(TextRange(0, element.textLength).contains(range))

        val originalContent = element.text
        val withoutQuotes = getRangeInElement(element)
        val generator = JsonElementGenerator(element.project)
        val replacement =
            StringUtil.unescapeStringCharacters(
                originalContent.substring(
                    withoutQuotes.startOffset,
                    range.startOffset
                )
            ) +
                    newContent +
                    StringUtil.unescapeStringCharacters(
                        originalContent.substring(
                            range.endOffset,
                            withoutQuotes.endOffset
                        )
                    )
        return element.replace(generator.createStringLiteral(replacement)) as JsonStringLiteral
    }

    override fun getRangeInElement(element: JsonStringLiteral): TextRange {
        val content = element.text
        val startOffset = if (content.startsWith("'") || content.startsWith("\"")) 1 else 0
        val endOffset = if (content.length > 1 && (content.endsWith("'") || content.endsWith("\""))) -1 else 0
        return TextRange(startOffset, content.length + endOffset)
    }
}
