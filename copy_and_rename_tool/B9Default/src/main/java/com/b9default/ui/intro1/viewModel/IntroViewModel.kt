package com.b9default.ui.intro1.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b9default.a1_common_utils.model_remote_config.screens.RemoteConfigScreenIntroModel
import com.b9default.ui.intro1.model.IntroModel1
import com.b9ui.R

class IntroViewModel : ViewModel() {

    private val _itemIntro = MutableLiveData<List<IntroModel1>>()
    val itemIntro: LiveData<List<IntroModel1>> get() = _itemIntro

    fun getIntroItem(screenIntroModel: RemoteConfigScreenIntroModel?) {
        val allIntroItem = getAllIntro(screenIntroModel)
        val itemIntro = mutableListOf<IntroModel1>()
        val itemShow = listOf(
            screenIntroModel?.isSlide1Show ?: true,
            screenIntroModel?.isSlide2Show ?: true,
            screenIntroModel?.isSlide3Show ?: true,
            screenIntroModel?.isSlide4Show ?: true
        )
        itemShow.forEachIndexed { index, isShow ->
            if (isShow) {
                itemIntro.add(allIntroItem[index])
            }
        }
        val pos1 = screenIntroModel?.adNativeFullPos1 ?: 0
        val pos2 = screenIntroModel?.adNativeFullPos2 ?: 0
        addNativeFullItem(pos1 - 1, itemIntro, screenIntroModel)
        addNativeFullItem(pos2 - 1, itemIntro, screenIntroModel)
        _itemIntro.value = itemIntro.toList().filter { it.isShow }
    }

    fun removeNativeFull(items: List<Int>) {
        val currentList = _itemIntro.value?.toMutableList() ?: mutableListOf()
        val sizeItems = items.size
        val currentListSize = currentList.size - 1
        var countItemRemoved = 0
        for (i in currentListSize downTo 0) {
            if (currentList[i].isNativeFull && countItemRemoved < sizeItems) {
                countItemRemoved++
                currentList[i] = currentList[i].copy(isShow = false)
            }
        }
        _itemIntro.value = currentList.toList().filter { it.isShow }
    }

    fun removeNativeFull(index: Int) {
        val currentList = _itemIntro.value?.toMutableList() ?: mutableListOf()
        if (index >= 0 && index < currentList.size && currentList[index].isNativeFull) {
            currentList[index] = currentList[index].copy(isShow = false)
        }
        _itemIntro.value = currentList.toList().filter { it.isShow }
    }

    private fun addNativeFullItem(
        position: Int,
        itemIntro: MutableList<IntroModel1>,
        screenIntroModel: RemoteConfigScreenIntroModel?,
    ) {
        if (position >= 0 && position < itemIntro.size) {
            val nativeItem = IntroModel1(
                isNativeFull = true,
                showBtnClose = screenIntroModel?.btnCloseConfig?.isShow ?: true,
                delayShowBtn = screenIntroModel?.btnCloseConfig?.delay ?: 0f
            )
            itemIntro.add(position, nativeItem)
        }
    }

    private fun getAllIntro(screenIntroModel: RemoteConfigScreenIntroModel?) = listOf(
        IntroModel1(
            R.drawable.img_intro1,
            R.string.title_intro_1,
            R.string.description_intro_1,
            showBtnNextLarge = screenIntroModel?.isSlide1NextBtn == true,
            isShowNativeBot = screenIntroModel?.isSlide1AdShow ?: true
        ),
        IntroModel1(
            R.drawable.img_intro2,
            R.string.title_intro_2,
            R.string.description_intro_2,
            showBtnNextLarge = screenIntroModel?.isSlide2NextBtn == true,
            isShowNativeBot = screenIntroModel?.isSlide2AdShow ?: true
        ),
        IntroModel1(
            R.drawable.img_intro3,
            R.string.title_intro_3,
            R.string.description_intro_3,
            showBtnNextLarge = screenIntroModel?.isSlide3NextBtn == true,
            isShowNativeBot = screenIntroModel?.isSlide3AdShow ?: true
        ),
        IntroModel1(
            R.drawable.img_intro4,
            R.string.title_intro_4,
            R.string.description_intro_4,
            showBtnNextLarge = screenIntroModel?.isSlide4NextBtn == true,
            isShowNativeBot = screenIntroModel?.isSlide4AdShow ?: true
        ),
    )

}
