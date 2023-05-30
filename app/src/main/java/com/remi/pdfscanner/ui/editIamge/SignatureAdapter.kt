package com.remi.pdfscanner.ui.editIamge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.remi.pdfscanner.R
import com.remi.pdfscanner.util.hide
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatureAdapter @Inject constructor() : RecyclerView.Adapter<SignatureAdapter.ViewHolder>() {
    interface ISignature {
        fun onClick(url: String)
        fun onAddImage()
        fun onClickDelete(url: String, position: Int)
    }

    var callback: ISignature? = null

    var listUrl: MutableList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recyclerview_signature, parent, false)
        if (viewType==0) return ViewHolderStart(view)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listUrl[position],position)


//        if (list[position]==1){
//            holder.itemView.setBackgroundResource(R.drawable.bg_sticker_selected);
//        }else {
//            holder.itemView.setBackground(null);
//        }

    }
    override fun getItemViewType(position: Int): Int {
        if (position==0) return 0
        return 1
    }
    override fun getItemCount(): Int {
        return listUrl.size
    }

    open inner class ViewHolder(open val itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.img_preview)
        var imgDelete: ImageView = itemView.findViewById(R.id.img_delete)

        open fun bind(url: String, position: Int) {

            Glide.with(itemView.context).asBitmap().load(url)
                .placeholder(R.drawable.img_test)
              .into(imageView)
            imageView.setOnClickListener {
                callback?.run { onClick(url) }
            }
            imgDelete.setOnClickListener {
                callback?.run { onClickDelete(url,position) }
            }
        }
    }
    inner class ViewHolderStart(override val itemView: View) : ViewHolder(itemView){
        override fun bind(url: String, position: Int) {
            Glide.with(itemView.context).load(R.drawable.ic_add_signature).into(imageView)
            imageView.setOnClickListener {
                callback?.run { onAddImage()}
            }
            imgDelete.hide()
        }
    }

}