package com.b9default.ui.language.model

sealed class LanguageItem {
    data class Parent(
        val languageName: String,
        val image: Int,
        val regionName: String,
        val flags: List<Int>,
        val isShowAnimation: Boolean = false,
        val id: String = java.util.UUID.randomUUID().toString()
    ) : LanguageItem()

    data class ParentWithoutChild(
        val languageName: String,
        val code: String,
        val image: Int,
        val regionName: String,
        val isSelected: Boolean = false,
        val isShowAnimation: Boolean = false,
        val id: String = java.util.UUID.randomUUID().toString()
    ) : LanguageItem()

    data class Child(
        val regionName: String,
        val code: String,
        val image: Int,
        val isSelected: Boolean = false,
        val id: String = java.util.UUID.randomUUID().toString()
    ) : LanguageItem()

    fun getItemId(): String {
        return when (this) {
            is Parent -> id
            is ParentWithoutChild -> id
            is Child -> id
        }
    }
}
