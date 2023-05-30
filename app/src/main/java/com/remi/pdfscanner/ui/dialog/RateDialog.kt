package com.remi.pdfscanner.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RatingBar
import com.remi.pdfscanner.R
import com.remi.pdfscanner.databinding.DialogRateBinding
import com.remi.pdfscanner.util.*

open class RateDialog(context: Context) : Dialog(context) {
    lateinit var binding: DialogRateBinding

    private val rateResult: RateResult? = null
    var callback: Callback? = null

    private var star = 5

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = DialogRateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.parent.layoutParams.width = Common.screenWidth
        binding.ratingbar.apply {
            onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, p1, _ ->
                star = p1.toInt()
                change()
            }
        }


        binding.ratingbar.rating = 4f
        binding.tvCancel.setOnClickListener { onClick(it) }
        binding.tvYes.setOnClickListener { onClick(it) }

        binding.tvTitle.setSize(18)
        binding.tvCancel.setSize(16)
        binding.tvYes.setSize(16)
        binding.tvDes.setSize(14)

    }

    private fun change() {
        if (star < 3){
            binding.tvYes.text = context.getString(R.string.feedback)
            binding.imgTop.setImageResource(R.drawable.ic_unhappy)
        }

        else {
            binding.tvYes.text = context.getString(R.string.rate_app)
            binding.imgTop.setImageResource(R.drawable.ic_happy)
        }

    }

    open fun onClick(v: View) {
        if (v.id == R.id.tv_yes) {
            if (star < 4) feedback() else rate()
        } else if (v.id == R.id.tv_cancel) {
            callback?.onCancel()
            dismiss()
        }
        cancel()
        if (rateResult != null) {
            Handler(Looper.getMainLooper()).postDelayed({ rateResult.onEnd() }, 150)
        }
    }

    private fun feedback() {
        MySharePref.rate(context)
        ActionUtils.sendFeedback(context)
    }

    private fun rate() {
        MySharePref.rate(context)
        FileUtil.rateApp(context)
        callback?.onRated()
    }

    interface Callback {
        fun onRated()
        fun onCancel()
    }

    interface RateResult {
        fun onEnd()
    }
}