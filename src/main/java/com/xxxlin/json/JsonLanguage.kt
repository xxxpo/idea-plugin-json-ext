package com.xxxlin.json

import com.intellij.lang.Language

open class JsonLanguage : Language {
    protected constructor(
        id: String, vararg mimeTypes: String
    ) : super(
        INSTANCE, id, *mimeTypes
    )

    private constructor() : super("json-ext")

    override fun isCaseSensitive(): Boolean {
        return true
    }

    fun hasPermissiveStrings(): Boolean {
        return false
    }

    companion object {
        @JvmField
        val INSTANCE: JsonLanguage = JsonLanguage()
    }
}
