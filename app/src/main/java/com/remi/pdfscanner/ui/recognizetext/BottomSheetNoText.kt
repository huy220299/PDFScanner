package com.remi.pdfscanner.ui.recognizetext

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.remi.pdfscanner.R
import com.remi.pdfscanner.databinding.BottomSheetNoTextFoundBinding
import com.remi.pdfscanner.util.setSize


class BottomSheetNoText(val callback:(Boolean)->Unit) : BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetNoTextFoundBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onStart() {
        super.onStart()
        val view = view
        view!!.post {
            val parent = view.parent as View
            val params = parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as BottomSheetBehavior<*>?
            bottomSheetBehavior!!.peekHeight = (view.measuredHeight * 1)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSize()
        binding.tvRetake.setOnClickListener {
            callback(false)
            dismiss()
        }
        binding.tvLanguage.setOnClickListener {
            callback(true)
            dismiss()
        }
        binding.imgBack.setOnClickListener { dismiss() }

    }

    private fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvDes.setSize(14)
        binding.tvRetake.setSize(14)
        binding.tvLanguage.setSize(14)
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = BottomSheetNoTextFoundBinding.inflate(inflater, container, false)
        return binding.root
    }
}