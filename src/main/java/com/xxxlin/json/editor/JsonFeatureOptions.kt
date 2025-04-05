// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "JsonExtFeatureOptions",
    storages = [Storage("json_ext_feature.xml")],
    category = SettingsCategory.PLUGINS
)
class JsonFeatureOptions : PersistentStateComponent<JsonFeatureOptions?> {

    /**
     * 匹配字符串转到定义
     */
    @JvmField
    var MATCH_STRING: Boolean = true

    /**
     * 匹配字符串中的大括号转到定义
     */
    @JvmField
    var MATCH_STRING_SLOT_BRACE: Boolean = true

    /**
     * Json Key 中的百分号槽位高亮
     */
    @JvmField
    var JSON_KEY_PERCENT_SLOT_HIGHLIGHT: Boolean = true

    override fun getState(): JsonFeatureOptions {
        return this
    }

    override fun loadState(state: JsonFeatureOptions) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        val instance: JsonFeatureOptions
            get() = ApplicationManager.getApplication().getService(
                JsonFeatureOptions::class.java
            )
    }
}
