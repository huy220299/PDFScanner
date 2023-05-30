package com.remi.pdfscanner.ui.anticounter

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.remi.pdfscanner.R
import com.remi.pdfscanner.databinding.BottomSheetAntiBinding
import com.remi.pdfscanner.util.setListener
import com.remi.pdfscanner.util.setSize

@SuppressLint("SetTextI18n")
class BottomSheetAntiCounter(val callback: (String) -> Unit) : BottomSheetDialogFragment() {
    lateinit var viewModel: AntiCounterViewModel
    lateinit var binding: BottomSheetAntiBinding

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
//            bottomSheetBehavior!!.peekHeight = Common.screenHeight
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.tempText.postValue(viewModel.currentText)
        viewModel.tempSize.postValue(viewModel.currentSize)
        viewModel.tempTransparent.postValue(viewModel.currentTransparent)
        viewModel.tempColor.postValue(viewModel.currentColor)
        super.onDismiss(dialog)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSize()
        onClick()
        binding.recyclerview.apply {
            adapter = ColorAdapter(back = {
                val colorDialog = ColorPickerDialogBuilder
                    .with(context)
                    .setTitle("Choose color")
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("ok") { _, selectedColor, _ ->
                        if (selectedColor != 0) {
                            val tempColor = "#" + Integer.toHexString(selectedColor)
                            callback(tempColor)
                        }
                    }
                    .setNegativeButton("cancel") { _, _ -> }
                    .build()

                if (it.equals("#00000000", true)) {
                    colorDialog.show()
                    colorDialog.getButton(BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                    colorDialog.getButton(BUTTON_POSITIVE).setTextColor(Color.BLACK)
                } else {
                    callback(it)
                }
            }).apply { currentPosition = findCurrentColor(viewModel.currentColor) }
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        binding.edt.setText(viewModel.currentText)
        binding.tvProgressSize.text = viewModel.currentSize.toString()
        binding.tvProgressTransparent.text = "${viewModel.currentTransparent}%"
        binding.seekbarSize.progress = viewModel.currentSize
        binding.seekbarTransparent.progress = viewModel.currentTransparent
    }

    private fun onClick() {
        binding.imgClose.setOnClickListener {
            dismiss()
        }
        binding.imgDone.setOnClickListener {
            viewModel.tempSize.value?.run { viewModel.currentSize = this }
            viewModel.tempTransparent.value?.run { viewModel.currentTransparent = this }
            viewModel.tempText.value?.run { viewModel.currentText = this }
            viewModel.tempColor.value?.run { viewModel.currentColor = this }
            dismiss()
        }
        binding.seekbarSize.setListener(callback = {
            binding.tvProgressSize.text = it.toString()
            viewModel.tempSize.postValue(it)
        })
        binding.seekbarTransparent.setListener {
            viewModel.tempTransparent.postValue(it)
            binding.tvProgressTransparent.text = "$it%"
        }
        binding.edt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.tempText.postValue(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    private fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvText.setSize(14)
        binding.tvTransparent.setSize(14)
        binding.tvColor.setSize(14)
        binding.tvProgressSize.setSize(14)
        binding.tvProgressTransparent.setSize(14)
        binding.edt.setSize(12)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel = ViewModelProvider(requireActivity())[AntiCounterViewModel::class.java]
        binding = BottomSheetAntiBinding.inflate(inflater, container, false)
        return binding.root
    }
}