// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json;

import com.intellij.lexer.FlexAdapter;

/**
 * @author Mikhail Golubev
 */
public class JsonLexer extends FlexAdapter {
    public JsonLexer() {
        super(new JsonFlexLexer());
    }
}