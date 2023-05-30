package com.remi.pdfscanner.ui.pickphoto

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.remi.pdfscanner.databinding.ItemRecyclerviewImageBottomBinding

class SelectedImageAdapter:RecyclerView.Adapter<SelectedImageAdapter.ViewHolder>() {
    var listImage: MutableList<MyImage> = ArrayList()
    var itemClick:IImageClick?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecyclerviewImageBottomBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(listImage[position].path).into(holder.binding.imgContent)
        holder.binding.imgRemove.setOnClickListener {
            itemClick?.run { onClick(position,listImage[position].path)
                 }
            //remove in side adapter
            listImage.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return listImage.size
    }
    class ViewHolder(val binding: ItemRecyclerviewImageBottomBinding) :RecyclerView.ViewHolder(binding.root)
}