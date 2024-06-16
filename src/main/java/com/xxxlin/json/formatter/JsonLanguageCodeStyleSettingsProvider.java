// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.formatter;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.application.options.codeStyle.properties.CodeStyleFieldAccessor;
import com.intellij.application.options.codeStyle.properties.MagicIntegerConstAccessor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.xxxlin.json.JsonBundle;
import com.xxxlin.json.JsonLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;

import static com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions.getInstance;

/**
 * @author Mikhail Golubev
 */
public final class JsonLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    private static final class Holder {
        private static final String[] ALIGN_OPTIONS = Arrays.stream(com.xxxlin.json.formatter.JsonCodeStyleSettings.PropertyAlignment.values())
                .map(alignment -> alignment.getDescription())
                .toArray(value -> new String[value]);

        private static final int[] ALIGN_VALUES =
                ArrayUtil.toIntArray(
                        ContainerUtil.map(com.xxxlin.json.formatter.JsonCodeStyleSettings.PropertyAlignment.values(), alignment -> alignment.getId()));

        private static final String SAMPLE = """
                {
                    "json literals are": {
                        "strings": ["foo", "bar", "\\u0062\\u0061\\u0072"],
                        "numbers": [42, 6.62606975e-34],
                        "boolean values": [true, false,],
                        "objects": {"null": null,"another": null,}
                    }
                }""";
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions("SPACE_WITHIN_BRACKETS",
                    "SPACE_WITHIN_BRACES",
                    "SPACE_AFTER_COMMA",
                    "SPACE_BEFORE_COMMA");
            consumer.renameStandardOption("SPACE_WITHIN_BRACES", com.xxxlin.json.JsonBundle.message("formatter.space_within_braces.label"));
            consumer.showCustomOption(com.xxxlin.json.formatter.JsonCodeStyleSettings.class, "SPACE_BEFORE_COLON", com.xxxlin.json.JsonBundle.message("formatter.space_before_colon.label"), getInstance().SPACES_OTHER);
            consumer.showCustomOption(com.xxxlin.json.formatter.JsonCodeStyleSettings.class, "SPACE_AFTER_COLON", com.xxxlin.json.JsonBundle.message("formatter.space_after_colon.label"), getInstance().SPACES_OTHER);
        } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
        } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showStandardOptions("RIGHT_MARGIN",
                    "WRAP_ON_TYPING",
                    "KEEP_LINE_BREAKS",
                    "WRAP_LONG_LINES");

            consumer.showCustomOption(com.xxxlin.json.formatter.JsonCodeStyleSettings.class,
                    "KEEP_TRAILING_COMMA",
                    com.xxxlin.json.JsonBundle.message("formatter.trailing_comma.label"),
                    getInstance().WRAPPING_KEEP);

            consumer.showCustomOption(com.xxxlin.json.formatter.JsonCodeStyleSettings.class,
                    "ARRAY_WRAPPING",
                    com.xxxlin.json.JsonBundle.message("formatter.wrapping_arrays.label"),
                    null,
                    getInstance().WRAP_OPTIONS,
                    CodeStyleSettingsCustomizable.WRAP_VALUES);

            consumer.showCustomOption(com.xxxlin.json.formatter.JsonCodeStyleSettings.class,
                    "OBJECT_WRAPPING",
                    com.xxxlin.json.JsonBundle.message("formatter.objects.label"),
                    null,
                    getInstance().WRAP_OPTIONS,
                    CodeStyleSettingsCustomizable.WRAP_VALUES);

            consumer.showCustomOption(com.xxxlin.json.formatter.JsonCodeStyleSettings.class,
                    "PROPERTY_ALIGNMENT",
                    com.xxxlin.json.JsonBundle.message("formatter.align.properties.caption"),
                    JsonBundle.message("formatter.objects.label"),
                    Holder.ALIGN_OPTIONS,
                    Holder.ALIGN_VALUES);

        }
    }

    @Override
    public @NotNull Language getLanguage() {
        return JsonLanguage.INSTANCE;
    }

    @Override
    public @Nullable IndentOptionsEditor getIndentOptionsEditor() {
        return new SmartIndentOptionsEditor();
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return Holder.SAMPLE;
    }

    @Override
    protected void customizeDefaults(@NotNull CommonCodeStyleSettings commonSettings,
                                     @NotNull CommonCodeStyleSettings.IndentOptions indentOptions) {
        indentOptions.INDENT_SIZE = 2;
        // strip all blank lines by default
        commonSettings.KEEP_BLANK_LINES_IN_CODE = 0;
    }

    @Override
    public @Nullable CodeStyleFieldAccessor getAccessor(@NotNull Object codeStyleObject, @NotNull Field field) {
        if (codeStyleObject instanceof com.xxxlin.json.formatter.JsonCodeStyleSettings && field.getName().equals("PROPERTY_ALIGNMENT")) {
            return new MagicIntegerConstAccessor(
                    codeStyleObject, field,
                    new int[]{
                            com.xxxlin.json.formatter.JsonCodeStyleSettings.PropertyAlignment.DO_NOT_ALIGN.getId(),
                            com.xxxlin.json.formatter.JsonCodeStyleSettings.PropertyAlignment.ALIGN_ON_VALUE.getId(),
                            JsonCodeStyleSettings.PropertyAlignment.ALIGN_ON_COLON.getId()
                    },
                    new String[]{
                            "do_not_align",
                            "align_on_value",
                            "align_on_colon"
                    }
            );
        }
        return null;
    }
}
