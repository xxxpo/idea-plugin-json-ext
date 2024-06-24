// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.findUsages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.psi.tree.TokenSet
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.JsonLexer
import com.xxxlin.json.JsonTokenSets

/**
 * @author Mikhail Golubev
 */
class JsonWordScanner : DefaultWordsScanner(
    JsonLexer(),
    TokenSet.create(JsonElementTypes.IDENTIFIER),
    JsonTokenSets.JSON_COMMENTARIES,
    JsonTokenSets.JSON_LITERALS
) {
    init {
        setMayHaveFileRefsInLiterals(true)
    }
}
