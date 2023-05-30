package com.remi.pdfscanner.ui.setting

import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivitySettingPdfBinding
import com.remi.pdfscanner.util.MySharePref
import com.remi.pdfscanner.util.setSize

class PdfSettingActivity : BaseActivity<ActivitySettingPdfBinding>(ActivitySettingPdfBinding::inflate) {


    override fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvOptionSize.setSize(14)
        binding.tvOptionOrientation.setSize(14)
        binding.tvPdfSize.setSize(14)
        binding.tvPdfOrientation.setSize(14)

        binding.layoutOptionSize.tvSizeA3.setSize(16)
        binding.layoutOptionSize.tvSizeA4.setSize(16)
        binding.layoutOptionSize.tvSizeA5.setSize(16)
        binding.layoutOptionSize.tvSizeB4.setSize(16)
        binding.layoutOptionSize.tvSizeB5.setSize(16)
        binding.layoutOptionSize.tvSizeLetter.setSize(16)
        binding.layoutOptionSize.tvSizeLegal.setSize(16)
        binding.layoutOptionSize.tvSizeExe.setSize(16)
        binding.layoutOptionSize.tvSizeBusinessCar.setSize(16)
        binding.layoutOptionSize.tvDoneSize.setSize(14)

        binding.layoutOptionOrientation.tvOptionAutoAdjust.setSize(16)
        binding.layoutOptionOrientation.tvOptionLandscape.setSize(16)
        binding.layoutOptionOrientation.tvOptionPortrait.setSize(16)
        binding.layoutOptionOrientation.tvDoneOrientation.setSize(14)
    }

    private fun onClickOptionSize() {
        var optionSize = OptionSize.A4
        fun setSelected(idImage: View) {
            listIdImage.forEach {
                if (it.id == idImage.id) it.setImageResource(R.drawable.ic_option_selected)
                else it.setImageResource(R.drawable.ic_option)
            }
        }
        binding.layoutOptionSize.imgSizeA3.setOnClickListener {
            optionSize = OptionSize.A3
            setSelected(it)
        }
        binding.layoutOptionSize.imgSizeA4.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.A4
        }
        binding.layoutOptionSize.imgSizeA5.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.A5
        }
        binding.layoutOptionSize.imgSizeB4.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.B4
        }
        binding.layoutOptionSize.imgSizeB5.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.B5
        }
        binding.layoutOptionSize.imgSizeLetter.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.LETTER
        }
        binding.layoutOptionSize.imgSizeLegal.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.LEGAL
        }
        binding.layoutOptionSize.imgSizeExe.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.EXECUTIVE
        }
        binding.layoutOptionSize.imgSizeBusinessCard.setOnClickListener {
            setSelected(it)
            optionSize = OptionSize.BUSINESS_CARD
        }
        binding.layoutOptionSize.tvDoneSize.setOnClickListener {
            MySharePref.updateOptionSize(this, optionSize.name)
            binding.tvOptionSize.text = MySharePref.getOptionSize(this) ?: OptionSize.A4.name
            onBackPressedDispatcher.onBackPressed()
        }
        setUpOptionSize()

        //default
        binding.tvOptionSize.text = MySharePref.getOptionSize(this) ?: OptionSize.A4.name
    }

    private fun setUpOptionSize() {
        when (MySharePref.getOptionSize(this)) {
            OptionSize.A3.name -> binding.layoutOptionSize.imgSizeA3.performClick()
            OptionSize.A5.name -> binding.layoutOptionSize.imgSizeA5.performClick()
            OptionSize.B4.name -> binding.layoutOptionSize.imgSizeB4.performClick()
            OptionSize.B5.name -> binding.layoutOptionSize.imgSizeB5.performClick()
            OptionSize.LETTER.name -> binding.layoutOptionSize.imgSizeLetter.performClick()
            OptionSize.LEGAL.name -> binding.layoutOptionSize.imgSizeLegal.performClick()
            OptionSize.EXECUTIVE.name -> binding.layoutOptionSize.imgSizeExe.performClick()
            OptionSize.BUSINESS_CARD.name -> binding.layoutOptionSize.imgSizeBusinessCard.performClick()
            else -> binding.layoutOptionSize.imgSizeA4.performClick()
        }
    }

    private fun onClickOptionOrientation() {
        var optionOrientation = OptionOrientation.AUTO

        binding.layoutOptionOrientation.imgOptionAutoAdjust.setOnClickListener {
            binding.layoutOptionOrientation.imgOptionAutoAdjust.setImageResource(R.drawable.ic_option_selected)
            binding.layoutOptionOrientation.imgOptionLandscape.setImageResource(R.drawable.ic_option)
            binding.layoutOptionOrientation.imgOptionPortrait.setImageResource(R.drawable.ic_option)
            optionOrientation = OptionOrientation.AUTO
        }
        binding.layoutOptionOrientation.imgOptionLandscape.setOnClickListener {
            binding.layoutOptionOrientation.imgOptionAutoAdjust.setImageResource(R.drawable.ic_option)
            binding.layoutOptionOrientation.imgOptionLandscape.setImageResource(R.drawable.ic_option_selected)
            binding.layoutOptionOrientation.imgOptionPortrait.setImageResource(R.drawable.ic_option)
            optionOrientation = OptionOrientation.LANDSCAPE
        }
        binding.layoutOptionOrientation.imgOptionPortrait.setOnClickListener {
            binding.layoutOptionOrientation.imgOptionAutoAdjust.setImageResource(R.drawable.ic_option)
            binding.layoutOptionOrientation.imgOptionLandscape.setImageResource(R.drawable.ic_option)
            binding.layoutOptionOrientation.imgOptionPortrait.setImageResource(R.drawable.ic_option_selected)
            optionOrientation = OptionOrientation.PORTRAIT
        }
        binding.layoutOptionOrientation.tvDoneOrientation.setOnClickListener {
            MySharePref.updateOptionOrientation(this, optionOrientation.name)
            binding.tvOptionOrientation.text = MySharePref.getOptionOrientation(this) ?: OptionOrientation.AUTO.name
            onBackPressedDispatcher.onBackPressed()
        }
        setUpOptionOrientation()
        //default name
        binding.tvOptionOrientation.text = MySharePref.getOptionOrientation(this) ?: OptionOrientation.AUTO.name
    }

    private fun setUpOptionOrientation() {
        when (MySharePref.getOptionOrientation(this)) {
            OptionOrientation.LANDSCAPE.name -> binding.layoutOptionOrientation.imgOptionLandscape.performClick()
            OptionOrientation.PORTRAIT.name -> binding.layoutOptionOrientation.imgOptionPortrait.performClick()
            else -> binding.layoutOptionOrientation.imgOptionAutoAdjust.performClick()
        }
    }

    private fun onClick() {
        binding.tvOptionSize.setOnClickListener {
            setUpOptionSize()
            binding.tvTitle.text = getString(R.string.pdf_page_size)
            TransitionManager.beginDelayedTransition(binding.parent, Slide(Gravity.BOTTOM))
            binding.layoutOptionSize.parentOptionSize.visibility = View.VISIBLE
        }
        binding.imgDownSize.setOnClickListener { binding.tvOptionSize.performClick() }

        binding.tvOptionOrientation.setOnClickListener {
            setUpOptionOrientation()
            binding.tvTitle.text = getString(R.string.pdf_page_orientation)
            TransitionManager.beginDelayedTransition(binding.parent, Slide(Gravity.BOTTOM))
            binding.layoutOptionOrientation.parentOptionOrientation.visibility = View.VISIBLE
        }
        binding.imgDownOrientation.setOnClickListener { binding.tvOptionOrientation.performClick() }

        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }


    private lateinit var listIdImage: List<ImageView>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listIdImage = listOf(
            binding.layoutOptionSize.imgSizeA3,
            binding.layoutOptionSize.imgSizeA4,
            binding.layoutOptionSize.imgSizeA5,
            binding.layoutOptionSize.imgSizeB4,
            binding.layoutOptionSize.imgSizeB5,
            binding.layoutOptionSize.imgSizeLetter,
            binding.layoutOptionSize.imgSizeLegal,
            binding.layoutOptionSize.imgSizeExe,
            binding.layoutOptionSize.imgSizeBusinessCard
        )
        onClick()
        onClickOptionSize()
        onClickOptionOrientation()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.layoutOptionSize.parentOptionSize.visibility != View.VISIBLE && binding.layoutOptionOrientation.parentOptionOrientation.visibility != View.VISIBLE)
                    finish()
                else {
                    if (binding.layoutOptionSize.parentOptionSize.visibility == View.VISIBLE) {
                        TransitionManager.beginDelayedTransition(binding.parent, Slide(Gravity.BOTTOM))
                        binding.layoutOptionSize.parentOptionSize.visibility = View.GONE
                        binding.tvTitle.text = getString(R.string.pdf_setting)
                    }
                    if (binding.layoutOptionOrientation.parentOptionOrientation.visibility == View.VISIBLE) {
                        TransitionManager.beginDelayedTransition(binding.parent, Slide(Gravity.BOTTOM))
                        binding.layoutOptionOrientation.parentOptionOrientation.visibility = View.GONE
                        binding.tvTitle.text = getString(R.string.pdf_setting)
                    }
                }
            }
        })
    }
}