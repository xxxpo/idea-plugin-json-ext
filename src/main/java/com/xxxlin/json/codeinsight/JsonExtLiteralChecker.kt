// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.xxxlin.json.codeinsight

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.xxxlin.json.psi.JsonStringLiteral
import com.xxxlin.json.utils.ExtensionPointNameUtils

interface JsonExtLiteralChecker {

    companion object {
        val EP_NAME: ExtensionPointName<JsonExtLiteralChecker> =
            ExtensionPointNameUtils.create("com.xxxlin.json.jsonExtLiteralChecker")
    }

    fun getErrorForNumericLiteral(literalText: String): String?

    fun getErrorForStringFragment(
        fragmentText: Pair<TextRange, String>,
        stringLiteral: JsonStringLiteral
    ): Pair<TextRange, String>?

    fun isApplicable(element: PsiElement): Boolean

}
