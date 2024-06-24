// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.xxxlin.json.codeinsight;

import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.xxxlin.json.psi.JsonStringLiteral;
import org.jetbrains.annotations.Nullable;

public interface JsonExtLiteralChecker {
    ExtensionPointName<JsonExtLiteralChecker> EP_NAME = ExtensionPointName.create("com.xxxlin.json.jsonExtLiteralChecker");

    @Nullable
    @InspectionMessage
    String getErrorForNumericLiteral(String literalText);

    @Nullable
    Pair<TextRange, @InspectionMessage String> getErrorForStringFragment(Pair<TextRange, String> fragmentText, JsonStringLiteral stringLiteral);

    boolean isApplicable(PsiElement element);
}
