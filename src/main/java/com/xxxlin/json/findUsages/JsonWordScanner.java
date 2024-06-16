// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.findUsages;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.psi.tree.TokenSet;
import com.xxxlin.json.JsonElementTypes;
import com.xxxlin.json.JsonLexer;

import static com.xxxlin.json.JsonTokenSets.JSON_COMMENTARIES;
import static com.xxxlin.json.JsonTokenSets.JSON_LITERALS;

/**
 * @author Mikhail Golubev
 */
public final class JsonWordScanner extends DefaultWordsScanner {
    public JsonWordScanner() {
        super(new JsonLexer(), TokenSet.create(JsonElementTypes.IDENTIFIER), JSON_COMMENTARIES, JSON_LITERALS);
        setMayHaveFileRefsInLiterals(true);
    }
}
