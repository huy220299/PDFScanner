package com.remi.pdfscanner.ui.editIamge

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseBottomSheet
import com.remi.pdfscanner.databinding.BottomSheetEditBinding
import com.remi.pdfscanner.util.setListener
import com.remi.pdfscanner.util.setSize
import dagger.hilt.android.AndroidEntryPoint
import org.spongycastle.math.raw.Mod
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetEdit : BaseBottomSheet<BottomSheetEditBinding>(BottomSheetEditBinding::inflate) {
    enum class Mode {
        Contrast, Brightness, Details
    }

    var currentMode = Mode.Contrast
    private val viewModel: EditImageActivityViewModel by activityViewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSize()
        binding.switchButton.setOnTouchListener { _, _ -> false }
        var isApplyAll = false
        binding.imgApplyAll.setOnClickListener {
            isApplyAll = !isApplyAll
            binding.switchButton.isChecked = isApplyAll
        }

        binding.seekbar.progress = viewModel.currentContrast
        binding.seekbar.setListener {
            binding.tvProgress.text = it.toString()
            when (currentMode) {
                Mode.Contrast -> {
                    viewModel.tempContrast = it
                }
                Mode.Brightness -> {
                    viewModel.tempBrightness = it
                }
                Mode.Details -> {
                    viewModel.tempDetails = it
                }
            }
            viewModel.flagChangeTemp.postValue(true)
        }
        val colorDefault = ContextCompat.getColor(requireContext(), R.color.color_7C7C7C)
        val colorSelected = ContextCompat.getColor(requireContext(), R.color.main_color)
        binding.btnContrast.setOnClickListener {
            binding.imgContrast.setColorFilter(colorSelected)
            binding.imgBrightness.setColorFilter(colorDefault)
            binding.imgDetails.setColorFilter(colorDefault)

            binding.tvContrast.setTextColor(colorSelected)
            binding.tvBrightness.setTextColor(colorDefault)
            binding.tvDetails.setTextColor(colorDefault)
            currentMode = Mode.Contrast
            binding.seekbar.progress = viewModel.tempContrast
        }

        binding.btnBrightness.setOnClickListener {
            binding.imgContrast.setColorFilter(colorDefault)
            binding.imgBrightness.setColorFilter(colorSelected)
            binding.imgDetails.setColorFilter(colorDefault)

            binding.tvContrast.setTextColor(colorDefault)
            binding.tvBrightness.setTextColor(colorSelected)
            binding.tvDetails.setTextColor(colorDefault)
            currentMode = Mode.Brightness
            binding.seekbar.progress = viewModel.tempBrightness
        }

        binding.btnDetails.setOnClickListener {
            binding.imgContrast.setColorFilter(colorDefault)
            binding.imgBrightness.setColorFilter(colorDefault)
            binding.imgDetails.setColorFilter(colorSelected)

            binding.tvContrast.setTextColor(colorDefault)
            binding.tvBrightness.setTextColor(colorDefault)
            binding.tvDetails.setTextColor(colorSelected)
            currentMode = Mode.Details
            binding.seekbar.progress = viewModel.tempDetails
        }

        binding.imgDone.setOnClickListener {
            viewModel.currentBrightness = viewModel.tempBrightness
            viewModel.currentContrast = viewModel.tempContrast
            viewModel.currentDetails = viewModel.tempDetails
            dismiss()
        }
        binding.imgClose.setOnClickListener { dismiss() }

        binding.switchButton.isChecked = viewModel.isApplyForAll
        binding.switchButton.setOnCheckedChangeListener { _, b ->
            viewModel.isApplyForAll = b
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.tempBrightness = viewModel.currentBrightness
        viewModel.tempContrast = viewModel.currentContrast
        viewModel.tempDetails = viewModel.currentDetails
        viewModel.flagChangeTemp.postValue(true)
        viewModel.flagChangeCurrent.postValue(true)
    }




    private fun setSize() {
        binding.vAjust.setSize(20)
        binding.tvContrast.setSize(12)
        binding.tvBrightness.setSize(12)
        binding.tvDetails.setSize(12)
        binding.tvProgress.setSize(14)
    }


}