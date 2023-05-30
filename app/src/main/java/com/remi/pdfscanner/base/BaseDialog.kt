package com.remi.pdfscanner.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

open class BaseDialog<B : ViewBinding>(context: Context, val bindingFactory: (LayoutInflater) -> B) : Dialog(context) {
    val binding: B by lazy { bindingFactory(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
    }
}