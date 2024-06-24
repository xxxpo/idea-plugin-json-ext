// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
/*
 * @author max
 */
package com.xxxlin.json

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil

class JsonNamesValidator : NamesValidator {
    private val myLexer = JsonLexer()

    @Synchronized
    override fun isKeyword(name: String, project: Project): Boolean {
        myLexer.start(name)
        return JsonTokenSets.JSON_KEYWORDS.contains(myLexer.tokenType) && myLexer.tokenEnd == name.length
    }

    @Synchronized
    override fun isIdentifier(name: String, project: Project): Boolean {
        var name = name
        if (!StringUtil.startsWithChar(name, '\'') && !StringUtil.startsWithChar(name, '"')) {
            name = "\"" + name
        }

        if (!StringUtil.endsWithChar(name, '"') && !StringUtil.endsWithChar(name, '\'')) {
            name += "\""
        }

        myLexer.start(name)
        val type = myLexer.tokenType

        return myLexer.tokenEnd == name.length && (type === JsonElementTypes.DOUBLE_QUOTED_STRING ||
                type === JsonElementTypes.SINGLE_QUOTED_STRING)
    }
}
