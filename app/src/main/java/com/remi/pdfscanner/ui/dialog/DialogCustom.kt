package com.remi.pdfscanner.ui.dialog

import android.content.Context
import android.os.Bundle
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseDialog
import com.remi.pdfscanner.base.IYesNoCallback
import com.remi.pdfscanner.databinding.DialogBaseBinding
import com.remi.pdfscanner.util.Common
import com.remi.pdfscanner.util.setSize

class DialogCustom(context: Context):BaseDialog<DialogBaseBinding>(context,DialogBaseBinding::inflate) {
    var titleValue = "This is title"
    var descriptionValue = "This is description"
    var cancelValue = "Cancel"
    var yesValue = "Allow"
    var idImage = R.drawable.ic_logo_round
    var callback:IYesNoCallback?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.parent.layoutParams.width = Common.screenWidth*8/10
        binding.tvTitle.setSize(14)
        binding.tvDes.setSize(12)
        binding.tvCancel.setSize(14)
        binding.tvAllow.setSize(14)

        binding.imgTop.setImageResource(idImage)
        binding.tvTitle.text  = titleValue
        binding.tvDes.text = descriptionValue
        binding.tvCancel.text  =cancelValue
        binding.tvAllow.text  = yesValue

        binding.tvCancel.setOnClickListener {
            callback?.run { onNo() }
            dismiss()
        }
        binding.tvAllow.setOnClickListener {
            callback?.run { onYes() }
            dismiss()
        }
    }
}