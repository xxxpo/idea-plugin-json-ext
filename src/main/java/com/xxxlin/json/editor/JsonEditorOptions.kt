// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "JsonExtEditorOptions", storages = [Storage("editor.xml")], category = SettingsCategory.CODE)
class JsonEditorOptions : PersistentStateComponent<JsonEditorOptions?> {
    @JvmField
    var COMMA_ON_ENTER: Boolean = true

    @JvmField
    var COMMA_ON_MATCHING_BRACES: Boolean = true
    var COMMA_ON_PASTE: Boolean = true

    @JvmField
    var AUTO_QUOTE_PROP_NAME: Boolean = true

    @JvmField
    var AUTO_WHITESPACE_AFTER_COLON: Boolean = true
    var ESCAPE_PASTED_TEXT: Boolean = true

    @JvmField
    var COLON_MOVE_OUTSIDE_QUOTES: Boolean = false

    @JvmField
    var COMMA_MOVE_OUTSIDE_QUOTES: Boolean = false

    override fun getState(): JsonEditorOptions {
        return this
    }

    override fun loadState(state: JsonEditorOptions) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        val instance: JsonEditorOptions
            get() = ApplicationManager.getApplication().getService(
                JsonEditorOptions::class.java
            )
    }
}
