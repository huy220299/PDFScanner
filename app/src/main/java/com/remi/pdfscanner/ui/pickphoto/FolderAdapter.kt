package com.remi.pdfscanner.ui.pickphoto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.remi.pdfscanner.databinding.ItemRecyclerviewFolderBinding
import com.remi.pdfscanner.repository.model.GalleryModel
import com.remi.pdfscanner.util.setSize


class FolderAdapter : RecyclerView.Adapter<FolderAdapter.Holder>() {
    var listImage: List<GalleryModel> = ArrayList()
    var itemCLick: IImageClick? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = ItemRecyclerviewFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Glide.with(holder.itemView.context).load(listImage[position].allImageInFolder!![0]).into(holder.binding.img)
        holder.binding.tvFolder.text = listImage[position].nameFolder
        holder.binding.tvCount.text = (listImage[position].allImageInFolder!!.size ?: 0).toString()
        holder.itemView.setOnClickListener {
            itemCLick?.run {
                notifyItemChanged(position)
                onClick(position, listImage[position].nameFolder ?: "All Image")
            }
        }
    }

    override fun getItemCount(): Int {
        return listImage.size
    }

    class Holder(val binding: ItemRecyclerviewFolderBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvFolder.setSize(14)
            binding.tvCount.setSize(14)
        }
    }
}