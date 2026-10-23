package com.b9default.ui.language.model

import java.util.UUID

data class LanguageModel(
    val languageName: String,
    val code: String,
    val mainRegion: RegionModel,
    val extraRegion: List<RegionModel> = listOf(),
    val isExpanded: Boolean = false,
    val isShowAnimation: Boolean = false,
    val id: String = UUID.randomUUID().toString()
) {
    fun toItemModel(): List<LanguageItem> {
        return if (isExpanded) {
            val child = extraRegion.map { region ->
                LanguageItem.Child(
                    regionName = region.regionName,
                    code = code,
                    image = region.image,
                    isSelected = region.isSelected,
                    id = region.id
                )
            }
            val result = mutableListOf(getParent())
            result.addAll(child)
            result.toList()
        } else {
            listOf(getParent())
        }
    }

    private fun getParent(): LanguageItem {
        return if (extraRegion.isEmpty()) {
            LanguageItem.ParentWithoutChild(
                languageName = languageName,
                code = code,
                image = mainRegion.image,
                regionName = mainRegion.regionName,
                isSelected = mainRegion.isSelected,
                isShowAnimation = isShowAnimation,
                id = id
            )
        } else {
            LanguageItem.Parent(
                languageName = languageName,
                image = mainRegion.image,
                regionName = mainRegion.regionName,
                flags = extraRegion.map { it.image }.reversed(),
                isShowAnimation = isShowAnimation,
                id = id
            )
        }
    }
}

data class RegionModel(
    val regionName: String,
    val image: Int,
    val isSelected: Boolean = false,
    val id: String = UUID.randomUUID().toString()
)
