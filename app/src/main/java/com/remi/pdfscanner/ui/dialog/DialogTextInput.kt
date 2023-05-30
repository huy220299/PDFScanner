package com.remi.pdfscanner.ui.dialog

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import com.remi.pdfscanner.base.BaseDialog
import com.remi.pdfscanner.databinding.DialogTextInputBinding
import com.remi.pdfscanner.util.Common
import com.remi.pdfscanner.util.setSize


class DialogTextInput(context: Context, val back: (String) -> Unit) : BaseDialog<DialogTextInputBinding>(context, DialogTextInputBinding::inflate) {
    var textTitle = "Rename"
    var textHint = "Input text here!"
    var textCancel = "Cancel"
    var textYes = "OK"
    var textInput: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.parent.layoutParams.width = Common.screenWidth * 8 / 10
        binding.tvTitle.setSize(14)
        binding.edt.setSize(12)
        binding.tvCancel.setSize(14)
        binding.tvAllow.setSize(14)
        binding.tvTitle.text = textTitle
        binding.edt.hint = textHint
        binding.tvCancel.text = textCancel
        binding.tvAllow.text = textYes
        textInput?.let { binding.edt.setText(it) }
        binding.tvCancel.setOnClickListener { dismiss() }
        binding.tvAllow.setOnClickListener {
            back(binding.edt.text.toString())
            dismiss()
        }

        Handler(Looper.getMainLooper()).postDelayed({
             Common.showKeyboard(binding.edt,context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            binding.edt.setSelection(binding.edt.length())//placing cursor at the end of the text
        }, 200)
    }
}