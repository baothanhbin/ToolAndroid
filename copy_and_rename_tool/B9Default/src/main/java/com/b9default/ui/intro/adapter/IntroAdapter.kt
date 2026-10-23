package com.b9default.ui.intro.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.b9default.ui.intro.model.IntroModel
import com.b9ui.databinding.ItemIntroBinding

class IntroAdapter(val context: Context, private val listIntro: List<IntroModel>) :
    RecyclerView.Adapter<IntroAdapter.ViewHolder>() {
    var list: MutableList<IntroModel> = mutableListOf()

    class ViewHolder(val binding: ItemIntroBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mItemIntroBinding =
            ItemIntroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(mItemIntroBinding)
    }

    override fun getItemCount(): Int {
        return listIntro.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val intro = listIntro[position]
        holder.binding.imgIntro.setImageResource(intro.image)
        holder.binding.tvTitle.text = intro.title
        holder.binding.tvContent.text = intro.content
    }
}


