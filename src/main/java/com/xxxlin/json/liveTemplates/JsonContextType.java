// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.liveTemplates;

import com.intellij.codeInsight.template.FileTypeBasedContextType;
import com.intellij.psi.PsiFile;
import com.xxxlin.json.JsonBundle;
import com.xxxlin.json.JsonFileType;
import com.xxxlin.json.psi.JsonFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public final class JsonContextType extends FileTypeBasedContextType {
    private JsonContextType() {
        super(JsonBundle.message("json.template.context.type"), JsonFileType.INSTANCE);
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        return file instanceof JsonFile;
    }
}
