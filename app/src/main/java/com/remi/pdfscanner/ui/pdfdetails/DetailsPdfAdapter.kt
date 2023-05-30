package com.remi.pdfscanner.ui.pdfdetails

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.remi.pdfscanner.R
import com.remi.pdfscanner.databinding.ItemDetailPdfBinding
import com.remi.pdfscanner.util.Common
import javax.inject.Inject
import javax.inject.Singleton

interface IDetailsFile{
    fun onSelect(itemPage:  ItemPage)
}
@Singleton
class DetailsPdfAdapter @Inject constructor() : RecyclerView.Adapter<DetailsPdfAdapter.ViewHolder>() {
    var listItem: MutableList<ItemPage> = ArrayList()
    var isSelectMode = false
    var isShowMode = false
    var callback: IDetailsFile?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 0) return EndViewHolder(ItemDetailPdfBinding.inflate(LayoutInflater.from(parent.context)))
        return ViewHolder(ItemDetailPdfBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listItem[position], position)
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == itemCount - 1 && !isShowMode)
            return 0
        return 1
    }


    open inner class ViewHolder(open val binding: ItemDetailPdfBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            if (isShowMode) {
                binding.img.layoutParams.width = Common.screenWidth
                binding.tvPageNo.visibility = View.GONE
            }

        }

        open fun bind(itemPage: ItemPage, position: Int) {
            binding.imgSelect.visibility = if (isSelectMode) View.VISIBLE else View.GONE
            Glide.with(binding.root.context).load(itemPage.bitmap).into(binding.img)
            binding.tvPageNo.text = handlePageNo(itemPage.pageNo)
            binding.imgSelect.setImageResource(if (itemPage.isSelect) R.drawable.ic_option_selected else R.drawable.ic_option)
            binding.root.setOnClickListener{
                if (isSelectMode){
                    itemPage.isSelect = !itemPage.isSelect
                    notifyItemChanged(position)
                }
            }
        }

        private fun handlePageNo(pageNo: Int): String {
            if (pageNo < 10) return "0$pageNo"
            return pageNo.toString()
        }
    }

    inner class EndViewHolder(override val binding: ItemDetailPdfBinding) : ViewHolder(binding) {
        init {
            binding.img.setImageResource(R.drawable.img_add_page)
            binding.imgSelect.visibility = View.INVISIBLE
            binding.tvPageNo.visibility = View.INVISIBLE
        }

        override fun bind(itemPage: ItemPage,position: Int) {
           binding.root.setOnClickListener { callback?.run { onSelect(itemPage) } }
        }
    }
}

data class ItemPage(val bitmap: Bitmap, val pageNo: Int, var isSelect: Boolean = false,var pageNumberSource:Int = -1)