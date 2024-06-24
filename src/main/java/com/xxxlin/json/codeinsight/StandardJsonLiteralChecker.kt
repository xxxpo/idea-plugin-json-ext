// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.codeinsight

import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.xxxlin.json.JsonBundle
import com.xxxlin.json.JsonDialectUtil
import com.xxxlin.json.psi.JsonStringLiteral
import java.util.regex.Pattern

class StandardJsonLiteralChecker : JsonExtLiteralChecker {
    override fun getErrorForNumericLiteral(literalText: String): String? {
        if (INF != literalText &&
            MINUS_INF != literalText &&
            NAN != literalText &&
            !VALID_NUMBER_LITERAL.matcher(literalText).matches()
        ) {
            return JsonBundle.message("syntax.error.illegal.floating.point.literal")
        }
        return null
    }

    override fun getErrorForStringFragment(
        fragmentText: Pair<TextRange, String>,
        stringLiteral: JsonStringLiteral
    ): Pair<TextRange, String>? {
        if (fragmentText.getSecond().chars()
                .anyMatch { c: Int -> c <= '\u001F'.code }
        ) { // fragments are cached, string values - aren't; go inside only if we encountered a potentially 'wrong' char
            val text = stringLiteral.text
            if (TextRange(0, text.length).contains(fragmentText.first)) {
                val startOffset = fragmentText.first.startOffset
                val part = text.substring(startOffset, fragmentText.first.endOffset)
                val array = part.toCharArray()
                for (i in array.indices) {
                    val c = array[i]
                    if (c <= '\u001F') {
                        return Pair.create(
                            TextRange(startOffset + i, startOffset + i + 1),
                            JsonBundle
                                .message(
                                    "syntax.error.control.char.in.string",
                                    "\\u" + Integer.toHexString(c.code or 0x10000).substring(1)
                                )
                        )
                    }
                }
            }
        }
        val error = getStringError(fragmentText.second)
        return if (error == null) null else Pair.create(fragmentText.first, error)
    }

    override fun isApplicable(element: PsiElement): Boolean {
        return JsonDialectUtil.isStandardJson(element)
    }

    companion object {
        val VALID_ESCAPE: Pattern = Pattern.compile("\\\\([\"\\\\/bfnrt]|u[0-9a-fA-F]{4})")
        private val VALID_NUMBER_LITERAL: Pattern = Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")
        const val INF: String = "Infinity"
        const val MINUS_INF: String = "-Infinity"
        const val NAN: String = "NaN"

        fun getStringError(fragmentText: String): String? {
            if (fragmentText.startsWith("\\") && fragmentText.length > 1 && !VALID_ESCAPE.matcher(fragmentText)
                    .matches()
            ) {
                return if (fragmentText.startsWith("\\u")) {
                    JsonBundle.message("syntax.error.illegal.unicode.escape.sequence")
                } else {
                    JsonBundle.message("syntax.error.illegal.escape.sequence")
                }
            }
            return null
        }
    }
}
