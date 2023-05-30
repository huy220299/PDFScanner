package com.remi.pdfscanner.ui.recognizetext

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.remi.pdfscanner.databinding.ItemRecyclerviewShareAppBinding
import com.remi.pdfscanner.util.setSize

class ShareTextAdapter(var callback : (String)->Unit ):RecyclerView.Adapter<ShareTextAdapter.ViewHolder>() {
    var listItem :MutableList<BottomSheetShare.PkgData> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = ItemRecyclerviewShareAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvTitle.text = listItem[position].pkgLabel
        Glide.with(holder.itemView.context).load(listItem[position].icon).into(holder.binding.img)
        holder.itemView.setOnClickListener { callback(listItem[position].pkgName) }
    }
    override fun getItemCount(): Int {
        return listItem.size
    }

    class ViewHolder(val binding:ItemRecyclerviewShareAppBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.tvTitle.setSize(12)
        }
    }
}