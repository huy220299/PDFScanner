package com.remi.pdfscanner.ui.pickphoto

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.remi.pdfscanner.R
import com.remi.pdfscanner.databinding.ItemRecyclerviewPhotoBinding

class PhotoAdapter : RecyclerView.Adapter<PhotoAdapter.Holder>() {
    var listImage: List<MyImage> = ArrayList()
    var itemCLick: IDetailRecyclerview? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = ItemRecyclerviewPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Glide.with(holder.itemView.context).load(listImage[position].path).into(holder.binding.img)
        holder.binding.imgSelect.setImageResource(if (!listImage[position].isSelect) R.drawable.ic_option2 else R.drawable.ic_option2_selected)
        holder.itemView.setOnClickListener {
            listImage[position].isSelect = !listImage[position].isSelect
            notifyItemChanged(position)
            itemCLick?.run {
                onClick(listImage[position].path)
            }
        }
    }

    override fun getItemCount(): Int {
        return listImage.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAll(isSelect: Boolean) {
        listImage.forEach { it.isSelect = isSelect }
        itemCLick?.run { onSelectAll() }
         notifyDataSetChanged()
    }

    class Holder(val binding: ItemRecyclerviewPhotoBinding) : RecyclerView.ViewHolder(binding.root)
}

interface IImageClick {
    fun onClick(position: Int,path: String)
}
interface IDetailRecyclerview{
    fun onClick(path: String)
    fun onSelectAll()
}