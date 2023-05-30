package com.remi.pdfscanner.ui.recognizetext

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivitySelectLanguageBinding
import com.remi.pdfscanner.util.MySharePref
import com.remi.pdfscanner.util.setSize

class ActivitySelectLanguage : BaseActivity<ActivitySelectLanguageBinding>(ActivitySelectLanguageBinding::inflate) {
    override fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvLatin.setSize(16)
        binding.tvChinese.setSize(16)
        binding.tvDevanagari.setSize(16)
        binding.tvJapanese.setSize(16)
        binding.tvKorean.setSize(16)
        binding.tvRecognizeAgain.setSize(14)
    }

    lateinit var listImage: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listImage = listOf(binding.imgLatin, binding.imgChinese, binding.imgDevanagari, binding.imgJapanese, binding.imgKorean)
        var selected = MySharePref.getLanguageORC(this)
        setSelectedOption(selected)
        binding.imgLatin.setOnClickListener {
            selected = 0
            setSelectedOption(selected)
        }
        binding.imgChinese.setOnClickListener {
            selected = 1
            setSelectedOption(selected)
        }
        binding.imgDevanagari.setOnClickListener {
            selected = 2
            setSelectedOption(selected)
        }
        binding.imgJapanese.setOnClickListener {
            selected = 3
            setSelectedOption(selected)
        }
        binding.imgKorean.setOnClickListener {
            selected = 4
            setSelectedOption(selected)
        }
        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                setResult(RESULT_CANCELED)
                finish()
            }
        } )
        binding.tvRecognizeAgain.setOnClickListener {
            finish()
        }
    }

    private fun setSelectedOption(pos: Int) {
        for (i in listImage.indices) {
            listImage[i].setImageResource(if (i == pos) R.drawable.ic_option_selected else R.drawable.ic_option)
        }
        setResult(RESULT_OK, Intent().apply { putExtra("language", pos) })
    }
}