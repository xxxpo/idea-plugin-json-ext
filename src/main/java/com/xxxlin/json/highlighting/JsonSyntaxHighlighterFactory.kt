// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.JsonFileType
import com.xxxlin.json.JsonLanguage
import com.xxxlin.json.JsonLexer

class JsonSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
        return MyHighlighter(virtualFile)
    }

    private inner class MyHighlighter(private val myFile: VirtualFile?) : SyntaxHighlighterBase() {
        private val ourAttributes: Map<IElementType, TextAttributesKey> =
            object : HashMap<IElementType, TextAttributesKey>() {
                init {
                    fillMap(this, JSON_BRACES, JsonElementTypes.L_CURLY, JsonElementTypes.R_CURLY)
                    fillMap(this, JSON_BRACKETS, JsonElementTypes.L_BRACKET, JsonElementTypes.R_BRACKET)
                    fillMap(this, JSON_COMMA, JsonElementTypes.COMMA)
                    fillMap(this, JSON_COLON, JsonElementTypes.COLON)
                    fillMap(this, JSON_STRING, JsonElementTypes.DOUBLE_QUOTED_STRING)
                    fillMap(this, JSON_STRING, JsonElementTypes.SINGLE_QUOTED_STRING)
                    fillMap(this, JSON_NUMBER, JsonElementTypes.NUMBER)
                    fillMap(this, JSON_KEYWORD, JsonElementTypes.TRUE, JsonElementTypes.FALSE, JsonElementTypes.NULL)
                    fillMap(this, JSON_LINE_COMMENT, JsonElementTypes.LINE_COMMENT1)
                    fillMap(this, JSON_LINE_COMMENT, JsonElementTypes.LINE_COMMENT2)
                    fillMap(this, JSON_BLOCK_COMMENT, JsonElementTypes.BLOCK_COMMENT)
                    fillMap(this, JSON_IDENTIFIER, JsonElementTypes.IDENTIFIER)
                    fillMap(this, JSON_VALID_ESCAPE, StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN)
                    fillMap(this, JSON_INVALID_ESCAPE, StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN)
                    fillMap(this, JSON_INVALID_ESCAPE, StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN)
                    fillMap(this, HighlighterColors.BAD_CHARACTER, TokenType.BAD_CHARACTER)
                }
            }

        override fun getHighlightingLexer(): Lexer {
            return JsonHighlightingLexer(isPermissiveDialect, isCanEscapeEol, lexer)
        }

        private val isPermissiveDialect: Boolean
            get() {
                val fileType = myFile?.fileType
                var isPermissiveDialect = false
                if (fileType is JsonFileType) {
                    val language = fileType.language
                    isPermissiveDialect = language is JsonLanguage && language.hasPermissiveStrings()
                }
                return isPermissiveDialect
            }

        override fun getTokenHighlights(type: IElementType): Array<TextAttributesKey> {
            return pack(ourAttributes[type])
        }
    }

    protected val lexer: Lexer
        get() = JsonLexer()

    protected val isCanEscapeEol: Boolean
        get() = false

    companion object {
        val JSON_BRACKETS: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("JSON.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val JSON_BRACES: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("JSON.BRACES", DefaultLanguageHighlighterColors.BRACES)
        val JSON_COMMA: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("JSON.COMMA", DefaultLanguageHighlighterColors.COMMA)
        val JSON_COLON: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("JSON.COLON", DefaultLanguageHighlighterColors.SEMICOLON)
        val JSON_NUMBER: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("JSON.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val JSON_STRING: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("JSON.STRING", DefaultLanguageHighlighterColors.STRING)
        val JSON_KEYWORD: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("JSON.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val JSON_LINE_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "JSON.LINE_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )
        val JSON_BLOCK_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "JSON.BLOCK_COMMENT",
            DefaultLanguageHighlighterColors.BLOCK_COMMENT
        )

        // Artificial element type
        val JSON_IDENTIFIER: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("JSON.IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)

        // Added by annotators
        val JSON_PROPERTY_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "JSON.PROPERTY_KEY",
            DefaultLanguageHighlighterColors.INSTANCE_FIELD
        )

        // String escapes
        val JSON_VALID_ESCAPE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "JSON.VALID_ESCAPE",
            DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
        )
        val JSON_INVALID_ESCAPE: TextAttributesKey = TextAttributesKey.createTextAttributesKey(
            "JSON.INVALID_ESCAPE",
            DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE
        )

        val JSON_PARAMETER: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("JSON.PARAMETER", DefaultLanguageHighlighterColors.KEYWORD)
    }
}
