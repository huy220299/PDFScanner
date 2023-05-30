package com.remi.pdfscanner.ui.dialog

import android.content.Context
import android.os.Bundle
import com.remi.pdfscanner.base.BaseDialog
import com.remi.pdfscanner.databinding.DialogRecognizeBinding
import com.remi.pdfscanner.util.Common

class DialogRecognize(context: Context):BaseDialog<DialogRecognizeBinding>(context,DialogRecognizeBinding::inflate) {
     var text = "   Recognize..."
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.parent.layoutParams.width = Common.screenWidth*8/10
        binding.tvTitle.text = text
     }

    override fun dismiss() {
        super.dismiss()
        binding.loadingView.onFinish()
    }
}