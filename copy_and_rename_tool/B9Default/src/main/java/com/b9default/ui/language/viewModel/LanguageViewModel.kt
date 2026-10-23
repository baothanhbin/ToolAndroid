package com.b9default.ui.language.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenLanguageStartModel
import com.b9default.ui.language.model.LanguageItem
import com.b9default.ui.language.model.LanguageModel
import com.b9default.ui.language.model.RegionModel
import com.b9ui.R

class LanguageViewModel : ViewModel() {

    private val _language = MutableLiveData<List<LanguageItem>>()
    val language: LiveData<List<LanguageItem>> get() = _language

    private val _selectedRegion = MutableLiveData("")
    val selectedRegion: LiveData<String> get() = _selectedRegion.distinctUntilChanged()

    private val languageData = mutableListOf<LanguageModel>()

    // Lấy danh sách ngôn ngữ
    fun getLanguageList(
        languageStartModel: RemoteConfigScreenLanguageStartModel? = null,
        screenOpenCount: Int = 2,
        selectedCode: String? = null,
        selectedRegionName: String? = null,
    ) {
        var listLang = getAllLang().toMutableList()
        if (languageStartModel?.enableLanguageDrop == true) {
            listLang = addExtraRegion(listLang)
        }
        listLang = sortLangFollowRemoteConfig(listLang, languageStartModel)
        val listLanguage = setupListLang(
            languageStartModel,
            screenOpenCount,
            selectedCode,
            selectedRegionName,
            listLang
        )
        getLanguageItemData(listLanguage)
    }

    // lựa chọn ngôn ngữ và cập nhật trạng thái hiển thị animation
    fun selectLang(model: LanguageItem) {
        when (model) {
            is LanguageItem.ParentWithoutChild -> selectParentWithoutChild(model)
            is LanguageItem.Parent -> selectParent(model)
            is LanguageItem.Child -> selectChild(model)
        }
        if (model !is LanguageItem.Parent) {
            _selectedRegion.value = model.getItemId()
        }
    }

    fun getSelectedCode() = getSelectedItem()?.code

    fun getSelectedRegionName(): String? {
        val selectedItem = getSelectedItem()
        return if (selectedItem?.mainRegion?.isSelected == true) {
            selectedItem.mainRegion.regionName
        } else {
            selectedItem?.extraRegion?.firstOrNull { it.isSelected }?.regionName
        }
    }

    private fun selectParentWithoutChild(model: LanguageItem.ParentWithoutChild) {
        val newLangList = languageData.map { language ->
            val mainRegion = language.mainRegion.copy(
                isSelected = language.id == model.id
            )
            language.copy(
                isShowAnimation = false,
                mainRegion = mainRegion,
                extraRegion = language.extraRegion.map {
                    it.copy(isSelected = false)
                })
        }
        getLanguageItemData(newLangList)
    }

    private fun selectParent(model: LanguageItem.Parent) {
        val newLangList = languageData.map { language ->
            language.copy(
                isShowAnimation = false,
                isExpanded = if (language.id == model.id) language.isExpanded.not() else language.isExpanded,
            )
        }
        getLanguageItemData(newLangList)
    }

    private fun selectChild(model: LanguageItem.Child) {
        val newLangList = languageData.map { language ->
            language.copy(
                isShowAnimation = false,
                mainRegion = language.mainRegion.copy(isSelected = false),
                extraRegion = language.extraRegion.map { region ->
                    region.copy(isSelected = region.id == model.id)
                }
            )
        }
        getLanguageItemData(newLangList)
    }

    private fun getLanguageItemData(originalData: List<LanguageModel>) {
        languageData.clear()
        languageData.addAll(originalData)
        _language.value = originalData.flatMap { language ->
            language.toItemModel()
        }
    }

    private fun getSelectedItem() = languageData.firstOrNull { language ->
        language.mainRegion.isSelected || language.extraRegion.any { it.isSelected }
    }

    // tính toán vị trí hiển thị animation và cập nhật trạng thái ngôn ngữ đã chọn
    private fun setupListLang(
        languageStartModel: RemoteConfigScreenLanguageStartModel? = null,
        screenOpenCount: Int = 2,
        selectedCode: String? = null,
        selectedRegionName: String? = null,
        listLang: MutableList<LanguageModel>,
    ): List<LanguageModel> {

        val posHandAnimate = getHandPosition(screenOpenCount, languageStartModel)
        val selectedPos = listLang.indexOfFirst { it.code == selectedCode }

        return if (selectedCode != null && selectedPos >= 0) {
            val selectedLang = listLang[selectedPos]
            if (selectedLang.extraRegion.isEmpty()) {
                val mainRegion = selectedLang.mainRegion.copy(isSelected = true)
                listLang[selectedPos] = selectedLang.copy(mainRegion = mainRegion)
            } else {
                val extraRegion = selectedLang.extraRegion.map {
                    it.copy(isSelected = it.regionName == selectedRegionName)
                }
                listLang[selectedPos] = selectedLang.copy(
                    isExpanded = true,
                    extraRegion = extraRegion
                )
            }
            getListLangSorted(listLang)

        } else {
            getListLangWithHand(posHandAnimate, listLang)
        }
    }

    // Lấy danh sách tất cả ngôn ngữ
    private fun getAllLang() = listOf(
        LanguageModel("English", "en", RegionModel("English", R.drawable.ic_language_en)),
        LanguageModel("Spanish", "es", RegionModel("Española", R.drawable.ic_language_es)),
        LanguageModel("Portuguese", "pt", RegionModel("Português", R.drawable.ic_language_pt)),
        LanguageModel("Russian", "ru", RegionModel("Русский", R.drawable.ic_language_russian)),
        LanguageModel("Japanese", "ja", RegionModel("日本語", R.drawable.ic_language_jp)),
        LanguageModel("Korean", "ko", RegionModel("한국인", R.drawable.ic_language_ko)),
        LanguageModel("Hindi", "hi", RegionModel("हिन्दी", R.drawable.ic_language_hi)),
        LanguageModel("French", "fr", RegionModel("Français", R.drawable.ic_language_fr)),
        LanguageModel("German", "de", RegionModel("Deutsch", R.drawable.ic_language_de)),
    )

    // Lấy danh sách ngôn ngữ theo thứ tự đã định nghĩa trong remote config
    private fun sortLangFollowRemoteConfig(
        data: List<LanguageModel>,
        languageStartModel: RemoteConfigScreenLanguageStartModel?
    ): MutableList<LanguageModel> {
        val langSort: List<String> =
            (languageStartModel?.langSort ?: "").lowercase().split(",")
        val listLang = mutableListOf<LanguageModel>()
        for (langCode in langSort) {
            data.find { it.code == langCode }?.let { listLang.add(it) }
        }
        return listLang.ifEmpty { data.toMutableList() }
    }

    // Lấy danh sách ngôn ngữ với các vùng bổ sung
    private fun addExtraRegion(data: List<LanguageModel>): MutableList<LanguageModel> {
        return data.map {
            it.copy(extraRegion = RegionHelper.getRegionsByLanguage(it.code))
        }.toMutableList()
    }

    // Lấy vị trí hiển thị animation
    private fun getHandPosition(
        screenOpenCount: Int,
        languageStartModel: RemoteConfigScreenLanguageStartModel?,
    ): Int {
        var positionHandAnimate =
            languageStartModel?.handAnimate?.pos ?: DEFAULT_POSITION_HAND_ANIMATE
        val isFirstTime =
            languageStartModel?.handAnimate?.firstTimeOnly == true && screenOpenCount <= 1
        val showThisTime = languageStartModel?.handAnimate?.firstTimeOnly != true || isFirstTime
        if (languageStartModel?.handAnimate?.isUse != true || showThisTime.not()) {
            positionHandAnimate = INVALID_POSITION
        }
        return positionHandAnimate
    }

    private fun getListLangSorted(listLang: MutableList<LanguageModel>): List<LanguageModel> {
        val sortedList = listLang.sortedBy { language ->
            language.mainRegion.isSelected || language.extraRegion.any { it.isSelected }
        }
        return sortedList.reversed().toList()
    }

    private fun getListLangWithHand(
        posHandAnimate: Int,
        listLang: MutableList<LanguageModel>,
    ): List<LanguageModel> {

        if (posHandAnimate > 0 && posHandAnimate <= listLang.size) {
            listLang[posHandAnimate - 1] = listLang[posHandAnimate - 1].copy(isShowAnimation = true)
        }
        return listLang.toList()
    }

    companion object {
        private const val DEFAULT_POSITION_HAND_ANIMATE = 2
        private const val INVALID_POSITION = -1
    }
}
