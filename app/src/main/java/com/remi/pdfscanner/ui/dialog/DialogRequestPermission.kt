package com.remi.pdfscanner.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.remi.pdfscanner.base.IYesNoCallback
import com.remi.pdfscanner.databinding.DialogPermissionBinding
import com.remi.pdfscanner.util.Common
import com.remi.pdfscanner.util.setSize

class DialogRequestPermission(context: Context)  : Dialog(context) {
    lateinit var binding:DialogPermissionBinding
    var callback:IYesNoCallback?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = DialogPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.parent.layoutParams.width  = Common.screenWidth*8/10
        binding.tvDes.setSize(14)
        binding.tvCancel.setSize(14)
        binding.tvAllow.setSize(14)
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