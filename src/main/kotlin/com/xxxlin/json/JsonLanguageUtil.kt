package com.xxxlin.json

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.IElementType
import com.xxxlin.json.psi.JsonFile
import com.xxxlin.json.psi.JsonObject
import com.xxxlin.json.psi.JsonProperty

object JsonLanguageUtil {

    private fun findByType(e: PsiElement, type: IElementType): List<ASTNode> {
        val result = mutableListOf<ASTNode>()
        findByType(e, type, result)
        return result
    }

    private fun findByType(e: PsiElement, type: IElementType, result: MutableList<ASTNode>) {
        val node = e.node.findChildByType(type)
        if (node != null) {
            result.add(node)
        }
        e.children.forEach {
            findByType(it, type, result)
        }
    }

    private fun <T : PsiElement> findByClass(e: PsiElement, type: Class<T>): List<PsiElement> {
        val result = mutableListOf<PsiElement>()
        findByClass(e, type, result)
        return result
    }

    private fun <T : PsiElement> findByClass(e: PsiElement, type: Class<T>, result: MutableList<PsiElement>) {
        if (type.isAssignableFrom(e.javaClass)) {
            result.add(e)
        }
        e.children.forEach {
            findByClass(it, type, result)
        }
    }

    private fun hasByType(e: PsiElement, type: IElementType, filter: (PsiElement) -> Boolean): Boolean {
        val node = e.node.findChildByType(type)
        if (node != null) {
            if (filter(node.psi)) {
                return true
            }
        }

        for (row in e.children) {
            if (hasByType(row, type, filter)) {
                return true
            }
        }
        return false
    }

    fun findAllJsonFile(project: Project): List<JsonFile> {
        val result = ArrayList<JsonFile>()
        val virtualFiles = FileTypeIndex.getFiles(JsonFileType.INSTANCE, GlobalSearchScope.projectScope(project))
        for (virtualFile in virtualFiles) {
            val file = PsiManager.getInstance(project).findFile(virtualFile) as? JsonFile? ?: continue
            result.add(file)
        }
        return result
    }

    fun hasJsonKey(project: Project, key: String): JsonProperty? {
        val fileList = findAllJsonFile(project)
        for (jsonFile in fileList) {
            val list = findByClass(jsonFile, JsonProperty::class.java).map {
                it as JsonProperty
            }
            for (property in list) {
                if (property.name == key) {
                    return property
                }
            }
        }
        return null
    }

    /**
     * 检查多级key
     */
    fun hasJsonKeys(project: Project, keys: List<String>): JsonProperty? {
        val fileList = findAllJsonFile(project)
        for (jsonFile in fileList) {
            var node: PsiElement? = jsonFile.children.firstOrNull {
                it is JsonObject
            }
            for (index in keys.indices) {
                val key = keys[index]
                node = node?.children?.filterIsInstance<JsonProperty>()?.firstOrNull {
                    it.name == key
                }
                if (index != keys.lastIndex) {
                    node = node?.children?.filterIsInstance<JsonObject>()?.firstOrNull()
                }
                if (node == null) {
                    break
                }
            }
            if (node != null) {
                return node as? JsonProperty
            }
        }
        return null
    }

    fun findAllJsonKey(project: Project, key: String): List<JsonProperty> {
        val result = mutableListOf<JsonProperty>()
        val fileList = findAllJsonFile(project)
        for (jsonFile in fileList) {
            val list = findByClass(jsonFile, JsonProperty::class.java).map {
                it as JsonProperty
            }
            for (property in list) {
                if (property.name == key) {
                    result.add(property)
                }
            }
        }
        return result
    }

    /**
     * 在所有json文件中搜索符合条件的多级key对应的属性列表
     */
    fun findAllJsonKeys(project: Project, keys: List<String>): List<List<JsonProperty>> {
        val result = mutableListOf<MutableList<JsonProperty>>()
        val fileList = findAllJsonFile(project)
        for (jsonFile in fileList) {
            val list = mutableListOf<JsonProperty>()
            var node: PsiElement? = jsonFile.children.firstOrNull {
                it is JsonObject
            }
            for (index in keys.indices) {
                val key = keys[index]
                node = node?.children?.filterIsInstance<JsonProperty>()?.firstOrNull {
                    it.name == key
                }
                if (node != null) {
                    list.add(node)
                }
                if (index != keys.lastIndex) {
                    node = node?.children?.filterIsInstance<JsonObject>()?.firstOrNull()
                }
            }
            if (node != null && node is JsonProperty && list.size == keys.size) {
                list.add(node)
                result.add(list)
                break
            }
        }
        return result
    }
}