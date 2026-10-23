package com.b9default.a1_common_utils.base

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import java.util.concurrent.Executors

abstract class BaseListAdapter<T, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseViewHolder<T, VB>>(
    AsyncDifferConfig.Builder(diffCallback)
        .setBackgroundThreadExecutor(Executors.newSingleThreadExecutor())
        .build()
) {

    override fun onBindViewHolder(holder: BaseViewHolder<T, VB>, position: Int) {
        if (position < currentList.size) {
            holder.onBindData(getItem(position))
        }

    }
}
