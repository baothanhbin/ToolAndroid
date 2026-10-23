package com.b9default.ui.intro1.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.b9ads.admob.nativeAds.advance.base.NativeCallback
import com.b9default.a1_common_utils.view.tap
import com.b9default.ui.intro1.ads.AdsIntroManager
import com.b9default.ui.intro1.model.IntroModel1
import com.b9ui.databinding.ItemIntroBinding
import com.b9ui.databinding.ItemNativeFullBinding

class IntroAdapter1: ListAdapter<IntroModel1, RecyclerView.ViewHolder>(diffCallback) {

    private var onShowNativeFail: (Int) -> Unit = {}
    private var onNextSlide: () -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ADS) {
            NativeFullViewHolder(
                ItemNativeFullBinding.inflate(layoutInflater, parent, false)
            )
        } else {
            IntroItemViewHolder(
                ItemIntroBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isNativeFull) TYPE_ADS else TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IntroItemViewHolder) {
            holder.bindData(getItem(position))
        } else if (holder is NativeFullViewHolder) {
            holder.bindData(getItem(position))
        }
    }

    fun setOnShowNativeFail(onShowNativeFail: (Int) -> Unit) {
        this.onShowNativeFail = onShowNativeFail
    }

    fun setOnNextSlide(onNextSlide: () -> Unit) {
        this.onNextSlide = onNextSlide
    }

    class IntroItemViewHolder(private val binding: ItemIntroBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindData(item: IntroModel1) {
            binding.imgIntro.setImageResource(item.image)
            binding.tvTitle.text = binding.root.context.getString(item.title)
            binding.tvContent.text = binding.root.context.getString(item.content)
        }
    }

    inner class NativeFullViewHolder(private val binding: ItemNativeFullBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnClose.tap {
                onNextSlide()
            }
        }

        fun bindData(data: IntroModel1) {
            if (AdsIntroManager.checkNativeFullShowed(adapterPosition)) return
            binding.btnClose.isVisible = false
            AdsIntroManager.showNativeFull(binding.frAds, adapterPosition, callback = object : NativeCallback {
                override fun onNotShow() {
                    onShowNativeFail(adapterPosition)
                }

                override fun onAdImpression() {
                    if (data.showBtnClose.not()) return
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.btnClose.isVisible = true
                    }, data.delayShowBtn.toLong() * 1000L)
                }
            })
        }
    }

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_ADS = 1
        private val diffCallback = object : DiffUtil.ItemCallback<IntroModel1>() {
            override fun areItemsTheSame(oldItem: IntroModel1, newItem: IntroModel1): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: IntroModel1, newItem: IntroModel1): Boolean {
                return oldItem == newItem
            }

        }
    }
}


