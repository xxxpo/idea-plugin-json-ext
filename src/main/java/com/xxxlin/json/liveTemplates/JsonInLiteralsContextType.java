// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.liveTemplates;

import com.intellij.codeInsight.template.TemplateContextType;
import com.xxxlin.json.JsonBundle;
import com.xxxlin.json.psi.JsonFile;
import com.xxxlin.json.psi.JsonStringLiteral;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public final class JsonInLiteralsContextType extends TemplateContextType {
  private JsonInLiteralsContextType() {
    super(JsonBundle.message("json.string.values"));
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    return file instanceof JsonFile && psiElement().inside(JsonStringLiteral.class).accepts(file.findElementAt(offset));
  }
}
