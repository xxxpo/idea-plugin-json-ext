package com.xxxlin.json

import com.xxxlin.json.psi.JsonFile
import com.xxxlin.json.psi.JsonProperty
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.IElementType

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
            val file = PsiManager.getInstance(project).findFile(virtualFile) as JsonFile? ?: continue
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
}