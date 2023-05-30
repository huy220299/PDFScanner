package com.remi.pdfscanner.ui.premium

import android.os.Bundle
import android.transition.TransitionManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityPremiumBinding
import com.remi.pdfscanner.util.setSize

class PremiumActivity : BaseActivity<ActivityPremiumBinding>(ActivityPremiumBinding::inflate) {

    enum class Option{
        Month, Year, Week
    }
    var currentOption =Option.Month

    override fun setSize() {
        binding.tvPremium.setSize(18)
        binding.tvBottom.setSize(12)
        binding.tvMonth.setSize(12)
        binding.tvWeek.setSize(12)
        binding.tvYear.setSize(12)
        binding.tvMonthCost.setSize(20)
        binding.tvWeekCost.setSize(20)
        binding.tvYearCost.setSize(20)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        val cts = ConstraintSet()

        binding.layoutMonth.setOnClickListener {
            currentOption = Option.Month
            cts.apply {
                clone(binding.layoutOption)
                connect(binding.imgSelected.id, START,binding.layoutMonth.id,START)
                connect(binding.imgSelected.id, END,binding.layoutMonth.id,END)
                TransitionManager.beginDelayedTransition(binding.root)
                applyTo(binding.layoutOption)
            }
        }
        binding.layoutYear.setOnClickListener {
            currentOption = Option.Year
            cts.apply {
                clone(binding.layoutOption)
                connect(binding.imgSelected.id, START,binding.layoutYear.id,START)
                connect(binding.imgSelected.id, END,binding.layoutYear.id,END)
                TransitionManager.beginDelayedTransition(binding.root)
                applyTo(binding.layoutOption)
            }
        }
        binding.layoutWeek.setOnClickListener {
            currentOption = Option.Week
            cts.apply {
                clone(binding.layoutOption)
                connect(binding.imgSelected.id, START,binding.layoutWeek.id,START)
                connect(binding.imgSelected.id, END,binding.layoutWeek.id, END)
                TransitionManager.beginDelayedTransition(binding.root)
                applyTo(binding.layoutOption)
            }
        }

    }

}