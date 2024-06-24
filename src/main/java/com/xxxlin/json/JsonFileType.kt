// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.xxxlin.json.JsonIcons.FILE
import javax.swing.Icon

/**
 * @author Mikhail Golubev
 */
open class JsonFileType : LanguageFileType {
    protected constructor(language: Language?) : super(language!!)

    protected constructor(language: Language?, secondary: Boolean) : super(language!!, secondary)

    protected constructor() : super(JsonLanguage.INSTANCE)

    override fun getName(): String {
        return "JSON File"
    }

    override fun getDescription(): String {
        return "JSON File"
    }

    override fun getDefaultExtension(): String {
        return DEFAULT_EXTENSION
    }

    override fun getIcon(): Icon {
        return FILE
    }

    companion object {
        @JvmField
        val INSTANCE = JsonFileType()
        const val DEFAULT_EXTENSION: String = "json-ext"
    }
}
