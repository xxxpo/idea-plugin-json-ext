package com.xxxlin.json.psi

import com.intellij.psi.PsiFile

/**
 * @author Mikhail Golubev
 */
interface JsonFile : JsonElement, PsiFile {
    /**
     * Returns [JsonArray] or [JsonObject] value according to JSON standard.
     *
     * @return top-level JSON element if any or `null` otherwise
     */
    val topLevelValue: JsonValue?

    val allTopLevelValues: List<JsonValue?>
}
