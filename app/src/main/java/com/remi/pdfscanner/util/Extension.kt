package com.remi.pdfscanner.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf

const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"
const val EXTRA_SYSTEM_ALERT_WINDOW = "system_alert_window"



fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}
fun View.show(isShow:Boolean){
    this.visibility = if (isShow) View.VISIBLE else View.GONE
}

fun Intent.highlightSettingsTo(string: String): Intent {
    putExtra(EXTRA_FRAGMENT_ARG_KEY, string)
    val bundle = bundleOf(EXTRA_FRAGMENT_ARG_KEY to string)
    putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
    return this
}
fun TextView.setSize(size:Int){
    setTextSize(TypedValue.COMPLEX_UNIT_PX,size*Common.screenWidth/360f)
}
fun TextView.setUp(context: Context, id:Int,text:Int, size: Int, font:Int,color:Int){
    this.id=id
    setSize(size)
    typeface = ResourcesCompat.getFont(context,font)
    setTextColor(ContextCompat.getColor(context,color))
    this.text = context.getString(text)
    textAlignment = View.TEXT_ALIGNMENT_CENTER
}
fun TextView.textCustom(text:String, color: Int,size:Float,typeface:String, context: Context){
    setTextColor(color)
    this.text = text

}
fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
    }
}
fun SeekBar.setListener( callback:(Int)->Unit ){
    setOnSeekBarChangeListener(object :OnSeekBarChangeListener{
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            p0?.run { callback(this.progress) }
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {

        }
    })
}


