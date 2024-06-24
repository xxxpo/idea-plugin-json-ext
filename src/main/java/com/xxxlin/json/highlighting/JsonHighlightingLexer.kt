// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.highlighting

import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType
import com.xxxlin.json.JsonElementTypes

class JsonHighlightingLexer(isPermissiveDialect: Boolean, canEscapeEol: Boolean, baseLexer: Lexer?) :
    LayeredLexer(baseLexer) {
    init {
        registerSelfStoppingLayer(
            JsonStringLiteralLexer('\"', JsonElementTypes.DOUBLE_QUOTED_STRING, canEscapeEol, isPermissiveDialect),
            arrayOf(JsonElementTypes.DOUBLE_QUOTED_STRING), IElementType.EMPTY_ARRAY
        )
        registerSelfStoppingLayer(
            JsonStringLiteralLexer('\'', JsonElementTypes.SINGLE_QUOTED_STRING, canEscapeEol, isPermissiveDialect),
            arrayOf(JsonElementTypes.SINGLE_QUOTED_STRING), IElementType.EMPTY_ARRAY
        )
    }
}
