package com.remi.pdfscanner.ui.anticounter

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityPickPageSizeBinding
import com.remi.pdfscanner.ui.setting.OptionSize
import com.remi.pdfscanner.util.MySharePref
import com.remi.pdfscanner.util.setSize

class ActivityPickPageSize : BaseActivity<ActivityPickPageSizeBinding>(ActivityPickPageSizeBinding::inflate) {
    override fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvSizeA3.setSize(16)
        binding.tvSizeA4.setSize(16)
        binding.tvSizeA5.setSize(16)
        binding.tvSizeB4.setSize(16)
        binding.tvSizeB5.setSize(16)
        binding.tvOk.setSize(14)
    }

    private lateinit var listIdImage: List<ImageView>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listIdImage = listOf(binding.imgSizeA3, binding.imgSizeA4, binding.imgSizeA5, binding.imgSizeB4, binding.imgSizeB5)

        onClickOptionSize()
        setUpOptionSize()//default
    }

    private fun onClickOptionSize() {
        var optionSize = OptionSize.A4
        fun setSelected(idImage: View) {
            listIdImage.forEach {
                if (it.id == idImage.id) it.setImageResource(R.drawable.ic_option_selected)
                else it.setImageResource(R.drawable.ic_option)
            }
        }
        binding.imgSizeA3.setOnClickListener {
            optionSize = OptionSize.A3
            setSelected(it)
        }
        binding.imgSizeA4.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.A4
        }
        binding.imgSizeA5.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.A5
        }
        binding.imgSizeB4.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.B4
        }
        binding.imgSizeB5.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.B5
        }

        binding.tvOk.setOnClickListener {
            MySharePref.updateOptionSize(this, optionSize.name)
            setResult(RESULT_OK)
            onBackPressedDispatcher.onBackPressed()
        }
        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

    }

    private fun setUpOptionSize() {
        when (MySharePref.getOptionSize(this)) {
            OptionSize.A3.name -> binding.imgSizeA3.performClick()
            OptionSize.A5.name -> binding.imgSizeA5.performClick()
            OptionSize.B4.name -> binding.imgSizeB4.performClick()
            OptionSize.B5.name -> binding.imgSizeB5.performClick()
            else -> binding.imgSizeA4.performClick()
        }
    }
}