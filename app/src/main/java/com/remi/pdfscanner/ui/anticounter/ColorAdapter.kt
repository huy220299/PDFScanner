package com.remi.pdfscanner.ui.anticounter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.remi.pdfscanner.R
import com.remi.pdfscanner.util.Common

class ColorAdapter(val back: (String) -> Unit) :RecyclerView.Adapter<ColorAdapter.Holder>() {
    private val listColor = listOf("#00000000","#333333","#FF5A26","#FF9B26","#26BEFF","#34ED9F","#DA48FF","#FF5252","#00BCD4","#808387","#263238","#9CC1F1")
    var currentPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        if (viewType==0)
            return HolderStart(LinearLayout(parent.context))
        return Holder(LinearLayout(parent.context))
    }
    fun findCurrentColor(colorIn:String):Int{
        for (i in listColor.indices){
            if (listColor[i].equals(colorIn,true))
                return i
        }
        return -1
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(listColor[position],position==currentPosition)
        holder.itemView.setOnClickListener {
            back(listColor[position])
            notifyItemChanged(currentPosition)
            currentPosition = position
            notifyItemChanged(currentPosition)
        }
    }

    override fun getItemCount(): Int {
        return listColor.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position==0)
            return 0
        return 1
    }
    open class Holder(layout: LinearLayout):RecyclerView.ViewHolder(layout){
       val colorView:ColorView = ColorView(layout.context)
        init {

            layout.addView(colorView,LinearLayout.LayoutParams(Common.screenWidth/7,Common.screenWidth/7))
        }
        open fun bind(color:String,isSelect:Boolean){
            colorView.myColor = color
            colorView.isSelect = isSelect
            colorView.invalidate()
        }
    }
    class HolderStart(layout: LinearLayout):Holder(layout){

        init {
            layout.getChildAt(0).visibility=View.GONE
            layout.addView(ImageView(layout.context).apply { setImageResource(R.drawable.ic_color_picker)},LinearLayout.LayoutParams(Common.screenWidth*3/28,Common.screenWidth*3/28).apply {
                setMargins(Common.screenWidth*3/8/28,Common.screenWidth*3/8/28,Common.screenWidth*3/8/28,Common.screenWidth*3/8/28)
            })
        }
        override fun bind(color:String,isSelect:Boolean){}

    }
}
