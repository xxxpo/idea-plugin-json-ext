// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.psi.impl;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NlsSafe;
import com.xxxlin.json.JsonDialectUtil;
import com.xxxlin.json.JsonLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
import com.xxxlin.json.JsonBundle;
import com.xxxlin.json.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.xxxlin.json.JsonTokenSets.STRING_LITERALS;

public final class JsonPsiImplUtils {
    static final Key<List<Pair<TextRange, String>>> STRING_FRAGMENTS = new Key<>("JSON string fragments");

    public static @NotNull String getName(@NotNull JsonProperty property) {
        String text = InjectedLanguageManager.getInstance(property.getProject()).getUnescapedText(property.getNameElement());
        return JsonTextLiteralService.getInstance().unquoteAndUnescape(text);
    }

    /**
     * Actually only JSON string literal should be accepted as valid name of property according to standard,
     * but for compatibility with JavaScript integration any JSON literals as well as identifiers (unquoted words)
     * are possible and highlighted as error later.
     */
    public static @NotNull JsonValue getNameElement(@NotNull JsonProperty property) {
        final PsiElement firstChild = property.getFirstChild();
        assert firstChild instanceof JsonLiteral || firstChild instanceof JsonReferenceExpression;
        return (JsonValue) firstChild;
    }

    public static @Nullable JsonValue getValue(@NotNull JsonProperty property) {
        return PsiTreeUtil.getNextSiblingOfType(getNameElement(property), JsonValue.class);
    }

    public static boolean isQuotedString(@NotNull JsonLiteral literal) {
        return literal.getNode().findChildByType(STRING_LITERALS) != null;
    }

    public static @Nullable ItemPresentation getPresentation(final @NotNull JsonProperty property) {
        return new ItemPresentation() {
            @Override
            public @Nullable String getPresentableText() {
                return property.getName();
            }

            @Override
            public @Nullable String getLocationString() {
                final JsonValue value = property.getValue();
                return value instanceof JsonLiteral ? value.getText() : null;
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                if (property.getValue() instanceof JsonArray) {
                    return AllIcons.Json.Array;
                }
                if (property.getValue() instanceof JsonObject) {
                    return AllIcons.Json.Object;
                }
                return PlatformIcons.PROPERTY_ICON;
            }
        };
    }

    public static @Nullable ItemPresentation getPresentation(final @NotNull JsonArray array) {
        return new ItemPresentation() {
            @Override
            public @Nullable String getPresentableText() {
                return com.xxxlin.json.JsonBundle.message("json.array");
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return AllIcons.Json.Array;
            }

            @Override
            public @Nullable String getLocationString() {
                return "array";
            }
        };
    }

    public static @Nullable ItemPresentation getPresentation(final @NotNull JsonObject object) {
        return new ItemPresentation() {
            @Override
            public @Nullable String getPresentableText() {
                return JsonBundle.message("json.object");
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return AllIcons.Json.Object;
            }

            @Override
            public @Nullable String getLocationString() {
                return null;
            }
        };
    }

    private static final String ourEscapesTable = "\"\"\\\\//b\bf\fn\nr\rt\t";

    public static @NotNull List<Pair<TextRange, String>> getTextFragments(@NotNull JsonStringLiteral literal) {
        List<Pair<TextRange, String>> result = literal.getUserData(STRING_FRAGMENTS);
        if (result == null) {
            result = new ArrayList<>();
            final String text = literal.getText();
            final int length = text.length();
            int pos = 1, unescapedSequenceStart = 1;
            while (pos < length) {
                if (text.charAt(pos) == '\\') {
                    if (unescapedSequenceStart != pos) {
                        result.add(Pair.create(new TextRange(unescapedSequenceStart, pos), text.substring(unescapedSequenceStart, pos)));
                    }
                    if (pos == length - 1) {
                        result.add(Pair.create(new TextRange(pos, pos + 1), "\\"));
                        break;
                    }
                    final char next = text.charAt(pos + 1);
                    switch (next) {
                        case '"':
                        case '\\':
                        case '/':
                        case 'b':
                        case 'f':
                        case 'n':
                        case 'r':
                        case 't':
                            final int idx = ourEscapesTable.indexOf(next);
                            result.add(Pair.create(new TextRange(pos, pos + 2), ourEscapesTable.substring(idx + 1, idx + 2)));
                            pos += 2;
                            break;
                        case 'u':
                            int i = pos + 2;
                            for (; i < pos + 6; i++) {
                                if (i == length || !StringUtil.isHexDigit(text.charAt(i))) {
                                    break;
                                }
                            }
                            result.add(Pair.create(new TextRange(pos, i), text.substring(pos, i)));
                            pos = i;
                            break;
                        case 'x':
                            Language language = JsonDialectUtil.getLanguageOrDefaultJson(literal);
                            if (language instanceof JsonLanguage && ((JsonLanguage) language).hasPermissiveStrings()) {
                                int i2 = pos + 2;
                                for (; i2 < pos + 4; i2++) {
                                    if (i2 == length || !StringUtil.isHexDigit(text.charAt(i2))) {
                                        break;
                                    }
                                }
                                result.add(Pair.create(new TextRange(pos, i2), text.substring(pos, i2)));
                                pos = i2;
                                break;
                            }
                        default:
                            result.add(Pair.create(new TextRange(pos, pos + 2), text.substring(pos, pos + 2)));
                            pos += 2;
                    }
                    unescapedSequenceStart = pos;
                } else {
                    pos++;
                }
            }
            final int contentEnd = text.charAt(0) == text.charAt(length - 1) ? length - 1 : length;
            if (unescapedSequenceStart < contentEnd) {
                result.add(Pair.create(new TextRange(unescapedSequenceStart, contentEnd), text.substring(unescapedSequenceStart, contentEnd)));
            }
            result = Collections.unmodifiableList(result);
            literal.putUserData(STRING_FRAGMENTS, result);
        }
        return result;
    }

    public static void delete(@NotNull JsonProperty property) {
        final ASTNode myNode = property.getNode();
        JsonPsiChangeUtils.removeCommaSeparatedFromList(myNode, myNode.getTreeParent());
    }

    public static @NotNull String getValue(@NotNull JsonStringLiteral literal) {
        return JsonTextLiteralService.getInstance().unquoteAndUnescape(literal.getText());
    }

    public static boolean isPropertyName(@NotNull JsonStringLiteral literal) {
        final PsiElement parent = literal.getParent();
        return parent instanceof JsonProperty && ((JsonProperty) parent).getNameElement() == literal;
    }

    public static boolean getValue(@NotNull JsonBooleanLiteral literal) {
        return literal.textMatches("true");
    }

    public static double getValue(@NotNull JsonNumberLiteral literal) {
        return Double.parseDouble(literal.getText());
    }
}
