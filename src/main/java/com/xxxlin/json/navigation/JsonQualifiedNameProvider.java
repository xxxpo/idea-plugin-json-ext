// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.navigation;

import com.intellij.ide.actions.QualifiedNameProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.jsonSchema.JsonPointerUtil;
import com.xxxlin.json.JsonUtil;
import com.xxxlin.json.psi.JsonArray;
import com.xxxlin.json.psi.JsonElement;
import com.xxxlin.json.psi.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Mikhail Golubev
 */
public final class JsonQualifiedNameProvider implements QualifiedNameProvider {
    @Override
    public @Nullable PsiElement adjustElementToCopy(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public @Nullable String getQualifiedName(@NotNull PsiElement element) {
        return generateQualifiedName(element, com.xxxlin.json.navigation.JsonQualifiedNameKind.Qualified);
    }

    public static String generateQualifiedName(PsiElement element, com.xxxlin.json.navigation.JsonQualifiedNameKind qualifiedNameKind) {
        if (!(element instanceof com.xxxlin.json.psi.JsonElement)) {
            return null;
        }
        JsonElement parentProperty = PsiTreeUtil.getNonStrictParentOfType(element, JsonProperty.class, JsonArray.class);
        StringBuilder builder = new StringBuilder();
        while (parentProperty != null) {
            if (parentProperty instanceof JsonProperty jsonProperty) {
                String name = jsonProperty.getName();
                if (qualifiedNameKind == com.xxxlin.json.navigation.JsonQualifiedNameKind.JsonPointer) {
                    name = JsonPointerUtil.escapeForJsonPointer(name);
                }
                builder.insert(0, name);
                builder.insert(0, qualifiedNameKind == com.xxxlin.json.navigation.JsonQualifiedNameKind.JsonPointer ? "/" : ".");
            } else {
                int index = JsonUtil.getArrayIndexOfItem(element instanceof JsonProperty ? element.getParent() : element);
                if (index == -1) return null;
                builder.insert(0, qualifiedNameKind == JsonQualifiedNameKind.JsonPointer ? ("/" + index) : ("[" + index + "]"));
            }
            element = parentProperty;
            parentProperty = PsiTreeUtil.getParentOfType(parentProperty, JsonProperty.class, JsonArray.class);
        }

        if (builder.length() == 0) return null;

        // if the first operation is array indexing, we insert the 'root' element $
        if (builder.charAt(0) == '[') {
            builder.insert(0, "$");
        }

        return StringUtil.trimStart(builder.toString(), ".");
    }

    @Override
    public PsiElement qualifiedNameToElement(@NotNull String fqn, @NotNull Project project) {
        return null;
    }
}
