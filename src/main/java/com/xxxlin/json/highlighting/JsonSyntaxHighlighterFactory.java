// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.highlighting;

import com.intellij.lang.Language;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.xxxlin.json.JsonElementTypes;
import com.xxxlin.json.JsonFileType;
import com.xxxlin.json.JsonLanguage;
import com.xxxlin.json.JsonLexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*;

public class JsonSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    public static final TextAttributesKey JSON_BRACKETS = TextAttributesKey.createTextAttributesKey("JSON.BRACKETS", BRACKETS);
    public static final TextAttributesKey JSON_BRACES = TextAttributesKey.createTextAttributesKey("JSON.BRACES", BRACES);
    public static final TextAttributesKey JSON_COMMA = TextAttributesKey.createTextAttributesKey("JSON.COMMA", COMMA);
    public static final TextAttributesKey JSON_COLON = TextAttributesKey.createTextAttributesKey("JSON.COLON", SEMICOLON);
    public static final TextAttributesKey JSON_NUMBER = TextAttributesKey.createTextAttributesKey("JSON.NUMBER", NUMBER);
    public static final TextAttributesKey JSON_STRING = TextAttributesKey.createTextAttributesKey("JSON.STRING", STRING);
    public static final TextAttributesKey JSON_KEYWORD = TextAttributesKey.createTextAttributesKey("JSON.KEYWORD", KEYWORD);
    public static final TextAttributesKey JSON_LINE_COMMENT = TextAttributesKey.createTextAttributesKey("JSON.LINE_COMMENT", LINE_COMMENT);
    public static final TextAttributesKey JSON_BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("JSON.BLOCK_COMMENT", BLOCK_COMMENT);

    // Artificial element type
    public static final TextAttributesKey JSON_IDENTIFIER = TextAttributesKey.createTextAttributesKey("JSON.IDENTIFIER", IDENTIFIER);

    // Added by annotators
    public static final TextAttributesKey JSON_PROPERTY_KEY = TextAttributesKey.createTextAttributesKey("JSON.PROPERTY_KEY", INSTANCE_FIELD);

    // String escapes
    public static final TextAttributesKey JSON_VALID_ESCAPE =
            TextAttributesKey.createTextAttributesKey("JSON.VALID_ESCAPE", VALID_STRING_ESCAPE);
    public static final TextAttributesKey JSON_INVALID_ESCAPE =
            TextAttributesKey.createTextAttributesKey("JSON.INVALID_ESCAPE", INVALID_STRING_ESCAPE);

    public static final TextAttributesKey JSON_PARAMETER = TextAttributesKey.createTextAttributesKey("JSON.PARAMETER", KEYWORD);


    @Override
    public @NotNull SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
        return new MyHighlighter(virtualFile);
    }

    private final class MyHighlighter extends SyntaxHighlighterBase {
        private final Map<IElementType, TextAttributesKey> ourAttributes = new HashMap<>() {{
            fillMap(this, JSON_BRACES, JsonElementTypes.L_CURLY, JsonElementTypes.R_CURLY);
            fillMap(this, JSON_BRACKETS, JsonElementTypes.L_BRACKET, JsonElementTypes.R_BRACKET);
            fillMap(this, JSON_COMMA, JsonElementTypes.COMMA);
            fillMap(this, JSON_COLON, JsonElementTypes.COLON);
            fillMap(this, JSON_STRING, JsonElementTypes.DOUBLE_QUOTED_STRING);
            fillMap(this, JSON_STRING, JsonElementTypes.SINGLE_QUOTED_STRING);
            fillMap(this, JSON_NUMBER, JsonElementTypes.NUMBER);
            fillMap(this, JSON_KEYWORD, JsonElementTypes.TRUE, JsonElementTypes.FALSE, JsonElementTypes.NULL);
            fillMap(this, JSON_LINE_COMMENT, JsonElementTypes.LINE_COMMENT1);
            fillMap(this, JSON_LINE_COMMENT, JsonElementTypes.LINE_COMMENT2);
            fillMap(this, JSON_BLOCK_COMMENT, JsonElementTypes.BLOCK_COMMENT);
            fillMap(this, JSON_IDENTIFIER, JsonElementTypes.IDENTIFIER);
            fillMap(this, JSON_VALID_ESCAPE, StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN);
            fillMap(this, JSON_INVALID_ESCAPE, StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN);
            fillMap(this, JSON_INVALID_ESCAPE, StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN);
            fillMap(this, HighlighterColors.BAD_CHARACTER, TokenType.BAD_CHARACTER);
        }};

        private final @Nullable VirtualFile myFile;

        MyHighlighter(@Nullable VirtualFile file) {
            myFile = file;
        }

        @Override
        public @NotNull Lexer getHighlightingLexer() {
            return new JsonHighlightingLexer(isPermissiveDialect(), isCanEscapeEol(), getLexer());
        }

        private boolean isPermissiveDialect() {
            FileType fileType = myFile == null ? null : myFile.getFileType();
            boolean isPermissiveDialect = false;
            if (fileType instanceof JsonFileType) {
                Language language = ((JsonFileType) fileType).getLanguage();
                isPermissiveDialect = language instanceof JsonLanguage && ((JsonLanguage) language).hasPermissiveStrings();
            }
            return isPermissiveDialect;
        }

        @Override
        public TextAttributesKey @NotNull [] getTokenHighlights(IElementType type) {
            return pack(ourAttributes.get(type));
        }
    }

    protected @NotNull Lexer getLexer() {
        return new JsonLexer();
    }

    protected boolean isCanEscapeEol() {
        return false;
    }
}
