// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json

import com.intellij.psi.tree.TokenSet

object JsonTokenSets {
    @JvmField
    val STRING_LITERALS: TokenSet = TokenSet.create(
        JsonElementTypes.SINGLE_QUOTED_STRING,
        JsonElementTypes.DOUBLE_QUOTED_STRING
    )

    val JSON_CONTAINERS: TokenSet = TokenSet.create(
        JsonElementTypes.OBJECT,
        JsonElementTypes.ARRAY
    )
    val JSON_KEYWORDS: TokenSet = TokenSet.create(
        JsonElementTypes.TRUE,
        JsonElementTypes.FALSE,
        JsonElementTypes.NULL
    )
    val JSON_LITERALS: TokenSet = TokenSet.create(
        JsonElementTypes.STRING_LITERAL,
        JsonElementTypes.NUMBER_LITERAL,
        JsonElementTypes.NULL_LITERAL,
        JsonElementTypes.TRUE,
        JsonElementTypes.FALSE
    )

    @JvmField
    val JSON_COMMENTARIES: TokenSet = TokenSet.create(
        JsonElementTypes.BLOCK_COMMENT,
        JsonElementTypes.LINE_COMMENT1,
        JsonElementTypes.LINE_COMMENT2
    )
}