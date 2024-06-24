// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.highlighting;

import com.intellij.icons.AllIcons;
import com.xxxlin.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.RainbowColorSettingsPage;
import com.intellij.psi.codeStyle.DisplayPriority;
import com.intellij.psi.codeStyle.DisplayPrioritySortable;
import com.xxxlin.json.JsonBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

/**
 * @author Mikhail Golubev
 */
public final class JsonColorsPage implements RainbowColorSettingsPage, DisplayPrioritySortable {
  private static final Map<String, TextAttributesKey> ourAdditionalHighlighting = Map.of("propertyKey", JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY);

  private static final AttributesDescriptor[] ourAttributeDescriptors = new AttributesDescriptor[]{
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.property.key"), JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY),

    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.braces"), JsonSyntaxHighlighterFactory.JSON_BRACES),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.brackets"), JsonSyntaxHighlighterFactory.JSON_BRACKETS),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.comma"), JsonSyntaxHighlighterFactory.JSON_COMMA),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.colon"), JsonSyntaxHighlighterFactory.JSON_COLON),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.number"), JsonSyntaxHighlighterFactory.JSON_NUMBER),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.string"), JsonSyntaxHighlighterFactory.JSON_STRING),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.keyword"), JsonSyntaxHighlighterFactory.JSON_KEYWORD),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.line.comment"), JsonSyntaxHighlighterFactory.JSON_LINE_COMMENT),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.block.comment"), JsonSyntaxHighlighterFactory.JSON_BLOCK_COMMENT),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.valid.escape.sequence"), JsonSyntaxHighlighterFactory.JSON_VALID_ESCAPE),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.invalid.escape.sequence"), JsonSyntaxHighlighterFactory.JSON_INVALID_ESCAPE),
    new AttributesDescriptor(com.xxxlin.json.JsonBundle.messagePointer("color.page.attribute.parameter"), JsonSyntaxHighlighterFactory.JSON_PARAMETER)
  };

  @Override
  public @NotNull Icon getIcon() {
    return AllIcons.FileTypes.Json;
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(JsonLanguage.INSTANCE, null, null);
  }

  @Override
  public @NotNull String getDemoText() {
    return """
      {
        // Line comments are not included in standard but nonetheless allowed.
        /* As well as block comments. */
        <propertyKey>"the only keywords are"</propertyKey>: [true, false, null],
        <propertyKey>"strings with"</propertyKey>: {
          <propertyKey>"no escapes"</propertyKey>: "pseudopolinomiality"
          <propertyKey>"valid escapes"</propertyKey>: "C-style\\r\\n and unicode\\u0021",
          <propertyKey>"illegal escapes"</propertyKey>: "\\0377\\x\\"
        },
        <propertyKey>"some numbers"</propertyKey>: [
          42,
          -0.0e-0,
          6.626e-34
        ]\s
      }""";
  }

  @Override
  public @NotNull Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourAdditionalHighlighting;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return ourAttributeDescriptors;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public @NotNull String getDisplayName() {
    return JsonBundle.message("settings.display.name.json");
  }

  @Override
  public DisplayPriority getPriority() {
    return DisplayPriority.LANGUAGE_SETTINGS;
  }

  @Override
  public boolean isRainbowType(TextAttributesKey type) {
    return JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY.equals(type)
      || JsonSyntaxHighlighterFactory.JSON_BRACES.equals(type)
      || JsonSyntaxHighlighterFactory.JSON_BRACKETS.equals(type)
      || JsonSyntaxHighlighterFactory.JSON_STRING.equals(type)
      || JsonSyntaxHighlighterFactory.JSON_NUMBER.equals(type)
      || JsonSyntaxHighlighterFactory.JSON_KEYWORD.equals(type);
  }

  @Override
  public @NotNull Language getLanguage() {
    return JsonLanguage.INSTANCE;
  }
}
