package com.remi.pdfscanner.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.remi.pdfscanner.R
import com.remi.pdfscanner.databinding.ItemRecyclerviewFilePdfBinding
import com.remi.pdfscanner.db.FileEntity
import com.remi.pdfscanner.util.Common
import com.remi.pdfscanner.util.setSize
import com.remi.pdfscanner.util.show
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileAdapter @Inject constructor() : RecyclerView.Adapter<FileAdapter.ViewHolder>() {
    private lateinit var binding1: ItemRecyclerviewFilePdfBinding
    private lateinit var context: Context
    var isSelectMode = false
    var listData: MutableList<FileEntity> = ArrayList()
    var callback: IFileCallback? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding1 = ItemRecyclerviewFilePdfBinding.inflate(inflater, parent, false)
        context = parent.context
        return ViewHolder(binding1)
    }

    override fun onBindViewHolder(holder: FileAdapter.ViewHolder, position: Int) {
        holder.bind(listData[position])
        if (!isSelectMode) {
            holder.binding.imgDelete.setOnClickListener {
                callback?.run { onDelete(listData[position]) }
            }
            holder.binding.imgDuplicate.setOnClickListener {
                callback?.run { onDuplicate(listData[position]) }
            }
            holder.binding.imgShare.setOnClickListener {
                callback?.run { onShare(listData[position]) }
            }
            holder.binding.imgEdit.setOnClickListener {
                callback?.run { onEdit(listData[position]) }
            }
            holder.binding.imgPreview.setOnClickListener {
                callback?.run { onSelect(listData[position]) }
            }
        } else {
            holder.itemView.setOnClickListener {
                listData[position].isSelect = !listData[position].isSelect
                notifyItemChanged(position)
                callback?.run {
                    onSelectAll(listData.filter { it.isSelect } )
                }
            }
            holder.binding.imgPreview.setOnClickListener {
                holder.itemView.performClick()
            }
        }

//        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int = listData.size

    inner class ViewHolder(val binding: ItemRecyclerviewFilePdfBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvTime.setSize(12)
            binding.tvNameFile.setSize(14)
            binding.tvPageSize.setSize(12)
            binding.tvNameFile.isSelected = true
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: FileEntity) {
            binding.imgSelect.show(isSelectMode)
            binding.imgDelete.show(!isSelectMode)
            binding.imgDuplicate.show(!isSelectMode)
            binding.imgShare.show(!isSelectMode)
            binding.imgEdit.show(!isSelectMode)
            //InitView
            binding.apply {
//                imgSelect.visibility = if (isSelectMode) View.VISIBLE else View.INVISIBLE
                //Set text
                imgSelect.setImageResource(if (item.isSelect) R.drawable.ic_option_selected else R.drawable.ic_option)
                tvNameFile.text = item.fileName
                tvNameFile.isSelected = true
                tvTime.text = Common.handleLong2Time(item.fileCreatedTime)
                tvPageSize.text = item.filePageSize.toString()
                Glide.with(context).load(item.imagePreviewPath).placeholder(R.drawable.img_place_holder_file).into(imgPreview)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<FileEntity>) {
        this.listData = list.map { it.copy()  }.toMutableList()

        notifyDataSetChanged()
    }

    fun selectAll(isSelect: Boolean){
        listData.forEach{it.isSelect = isSelect}
        notifyItemRangeChanged(0, itemCount, true)
    }

    fun changeSelectedMode(isSelect: Boolean) {
        this.isSelectMode = isSelect
//        notifyDataSetChanged()
//        notifyItemChanged(0)
        if (!isSelect)
            listData.forEach{it.isSelect = false}
        notifyItemRangeChanged(0, itemCount, true)
    }


}

interface IFileCallback {
    fun onDuplicate(item: FileEntity)
    fun onDelete(item: FileEntity)
    fun onShare(item: FileEntity)
    fun onEdit(item: FileEntity)
    fun onSelect(item: FileEntity)
    fun onSelectAll(items:List<FileEntity>)
}
