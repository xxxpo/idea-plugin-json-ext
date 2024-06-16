// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Mikhail Golubev
 */
public class JsonFileType extends LanguageFileType {
    public static final JsonFileType INSTANCE = new JsonFileType();
    public static final String DEFAULT_EXTENSION = "json-ext";

    protected JsonFileType(Language language) {
        super(language);
    }

    protected JsonFileType(Language language, boolean secondary) {
        super(language, secondary);
    }

    protected JsonFileType() {
        super(JsonLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "JSON File";
    }

    @Override
    public @NotNull String getDescription() {
        return "JSON File";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Override
    public Icon getIcon() {
        return JsonIcons.getFILE();
    }
}
