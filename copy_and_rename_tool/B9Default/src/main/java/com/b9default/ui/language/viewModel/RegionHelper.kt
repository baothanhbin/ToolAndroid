package com.b9default.ui.language.viewModel

import com.b9default.ui.language.model.RegionModel
import com.b9ui.R

object RegionHelper {

    fun getRegionsByLanguage(languageCode: String): List<RegionModel> {
        return when (languageCode.lowercase()) {
            "en" -> getEnglishRegions()
            "es" -> getSpanishRegions()
            "ja" -> getJapaneseRegions()
            "ko" -> getKoreanRegions()
            "pt" -> getPortugueseRegions()
            "ru" -> getRussianRegions()
            else -> emptyList()
        }
    }

    private fun getEnglishRegions() = listOf(
        RegionModel("American", R.drawable.ic_language_us),
        RegionModel("British", R.drawable.ic_language_en),
        RegionModel("Indian", R.drawable.ic_language_hi),
    )

    private fun getSpanishRegions() = listOf(
        RegionModel("Spanish", R.drawable.ic_language_es),
        RegionModel("Mexican", R.drawable.ic_language_mexico),
    )

    private fun getJapaneseRegions() = listOf(
        RegionModel("Japanese", R.drawable.ic_language_jp),
    )

    private fun getKoreanRegions() = listOf(
        RegionModel("Korean", R.drawable.ic_language_ko),
    )

    private fun getPortugueseRegions() = listOf(
        RegionModel("Portuguese", R.drawable.ic_language_pt),
        RegionModel("EU", R.drawable.ic_language_eu),
        RegionModel("Brazil", R.drawable.ic_language_brazin),
    )

    private fun getRussianRegions() = listOf(
        RegionModel("Surzhyk", R.drawable.ic_language_ukraina),
        RegionModel("Trasianka", R.drawable.ic_language_belarus),
        RegionModel("Pidgin", R.drawable.ic_language_russian),
        RegionModel("Russian", R.drawable.ic_language_russian),
    )
}