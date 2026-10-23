package com.b9default.ui.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.b9default.a1_common_utils.base.BaseListAdapter
import com.b9default.a1_common_utils.base.BaseViewHolder
import com.b9ui.databinding.ItemFlagBinding

class FlagAdapter: BaseListAdapter<Int, ItemFlagBinding>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlagViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFlagBinding.inflate(layoutInflater, parent, false)
        return FlagViewHolder(binding)
    }

    class FlagViewHolder(binding: ItemFlagBinding) : BaseViewHolder<Int, ItemFlagBinding>(binding) {

        override fun onBindData(data: Int) {
            super.onBindData(data)
            binding.root.setImageResource(data)
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }
        }
    }
}