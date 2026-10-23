package com.b9default.ui.language.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.b9default.ui.language.model.LanguageItem
import com.b9ui.R
import com.b9ui.databinding.ItemLanguageChildBinding
import com.b9ui.databinding.ItemLanguageParentWithChildBinding
import com.b9ui.databinding.ItemLanguageParentWithoutChildBinding
import java.util.concurrent.Executors

class LanguageAdapter : ListAdapter<LanguageItem, RecyclerView.ViewHolder>(
    AsyncDifferConfig.Builder(diffCallback)
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()
) {

    private var clickListener: (LanguageItem) -> Unit = {}

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int,
    ): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_WITH_CHILD -> {
                LanguageWithChildHolder(
                    ItemLanguageParentWithChildBinding.inflate(layoutInflater, parent, false)
                )
            }
            TYPE_WITHOUT_CHILD -> {
                LanguageWithoutChildHolder(
                    ItemLanguageParentWithoutChildBinding.inflate(layoutInflater, parent, false)
                )
            }
            else -> {
                ChildViewHolder(
                    ItemLanguageChildBinding.inflate(layoutInflater, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < currentList.size && position >= 0) {
            val item = getItem(position)
            if (holder is LanguageWithChildHolder && item is LanguageItem.Parent)
                holder.bindData(item)
            else if (holder is LanguageWithoutChildHolder && item is LanguageItem.ParentWithoutChild) {
                holder.bindData(item)
            } else if (holder is ChildViewHolder && item is LanguageItem.Child) {
                holder.bindData(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is LanguageItem.Child -> TYPE_CHILD
            is LanguageItem.Parent -> TYPE_WITH_CHILD
            is LanguageItem.ParentWithoutChild -> TYPE_WITHOUT_CHILD
        }
    }

    private fun getFont(context: Context, @StringRes assetFile: Int): Typeface {
        return try {
            Typeface.createFromAsset(context.assets, context.getString(assetFile))
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
    }

    fun setOnItemClick(itemClickListener: (LanguageItem) -> Unit) {
        this.clickListener = itemClickListener
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class LanguageWithChildHolder(val binding: ItemLanguageParentWithChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var data: LanguageItem.Parent? = null
        private val adapter by lazy { FlagAdapter() }

        init {
            binding.rvFlag.adapter = adapter
            binding.touchView.setOnClickListener {
                data?.let { clickListener(it) }
            }
        }

        fun bindData(data: LanguageItem.Parent) {
            this.data = data
            binding.animationView.isVisible = data.isShowAnimation
            binding.ivLanguage.setImageResource(data.image)
            binding.tvLanguage.text = data.languageName
            binding.regionName.text = data.regionName
            adapter.submitList(data.flags)
            binding.paddingTop.isVisible = absoluteAdapterPosition != 0
        }
    }

    inner class LanguageWithoutChildHolder(val binding: ItemLanguageParentWithoutChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var data: LanguageItem? = null

        init {
            binding.containerView.setOnClickListener {
                data?.let { clickListener(it) }
            }
        }

        fun bindData(data: LanguageItem.ParentWithoutChild) {
            this.data = data
            binding.animationView.isVisible = data.isShowAnimation
            binding.ivLanguage.setImageResource(data.image)
            binding.tvLanguage.text = data.languageName
            binding.regionName.text = data.regionName
            binding.paddingTop.isVisible = absoluteAdapterPosition != 0
            val context = binding.root.context

            if (data.isSelected) {
                binding.containerView.setBackgroundResource(R.drawable.bg_item_language_on)
                binding.tvLanguage.setTextColor(context.getColor(R.color.lang_select_color))
                binding.regionName.setTextColor(context.getColor(R.color.region_select_color))
                binding.icSelect.setImageResource(R.drawable.ic_lang_select)
                binding.tvLanguage.typeface = getFont(context, R.string.selected_parent_font)
                binding.regionName.typeface = getFont(context, R.string.selected_main_region_font)
            } else {
                binding.containerView.setBackgroundResource(R.drawable.bg_item_language_off)
                binding.tvLanguage.setTextColor(context.getColor(R.color.lang_unselect_color))
                binding.regionName.setTextColor(context.getColor(R.color.region_unselect_color))
                binding.icSelect.setImageResource(R.drawable.ic_lang_unselect)
                binding.tvLanguage.typeface = getFont(context, R.string.unselect_parent_font)
                binding.regionName.typeface = getFont(context, R.string.unselect_main_region_font)
            }
        }
    }

    inner class ChildViewHolder(
        private val binding: ItemLanguageChildBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var data: LanguageItem? = null

        init {
            binding.regionContainer.setOnClickListener {
                data?.let { clickListener(it) }
            }
        }

        fun bindData(data: LanguageItem.Child) {
            this.data = data
            binding.regionName.text = data.regionName
            binding.flag.setImageResource(data.image)
            val isLastItem = absoluteAdapterPosition == currentList.size - 1
            val hideLine = isLastItem || currentList[absoluteAdapterPosition + 1] !is LanguageItem.Child
            binding.verticalLine.isVisible = !hideLine
            val context = binding.root.context

            if (data.isSelected) {
                binding.regionContainer.setBackgroundResource(R.drawable.bg_item_language_on)
                binding.icSelect.setImageResource(R.drawable.ic_lang_select)
                binding.regionName.setTextColor(context.getColor(R.color.extra_region_select_color))
                binding.regionName.typeface = getFont(context, R.string.selected_region_font)
            } else {
                binding.regionContainer.setBackgroundResource(R.drawable.bg_item_language_off)
                binding.icSelect.setImageResource(R.drawable.ic_lang_unselect)
                binding.regionName.setTextColor(context.getColor(R.color.extra_region_unselect_color))
                binding.regionName.typeface = getFont(context, R.string.unselect_region_font)
            }
        }
    }

    companion object {
        private const val TYPE_WITH_CHILD = 0
        private const val TYPE_WITHOUT_CHILD = 1
        private const val TYPE_CHILD = 2

        private val diffCallback = object : DiffUtil.ItemCallback<LanguageItem>() {
            override fun areItemsTheSame(oldItem: LanguageItem, newItem: LanguageItem): Boolean {
                return oldItem.getItemId() == newItem.getItemId()
            }

            override fun areContentsTheSame(
                oldItem: LanguageItem,
                newItem: LanguageItem,
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}
