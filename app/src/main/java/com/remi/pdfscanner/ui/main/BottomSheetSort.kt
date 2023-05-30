package com.remi.pdfscanner.ui.main

import android.os.Bundle
import android.view.View
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseBottomSheet
import com.remi.pdfscanner.databinding.BottomSheetSortBinding
import com.remi.pdfscanner.util.setSize

class BottomSheetSort : BaseBottomSheet<BottomSheetSortBinding>(BottomSheetSortBinding::inflate) {
    enum class SortBy {
        Modify, Created, Name
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSize()
        onClick()
        when(sortBy){
            SortBy.Modify->modifyClick()
            SortBy.Created->createdClick()
            SortBy.Name->nameClick()
        }
        if (isAscending) ascendingClick() else descendingClick()
    }

    var callback: ISort? = null
    var isAscending = true
    var sortBy = SortBy.Modify

    private fun modifyClick() {
        binding.imgModified.setImageResource(R.drawable.ic_option_selected)
        binding.imgCreated.setImageResource(R.drawable.ic_option)
        binding.imgName.setImageResource(R.drawable.ic_option)
        sortBy = SortBy.Modify
    }

    private fun createdClick() {
        binding.imgModified.setImageResource(R.drawable.ic_option)
        binding.imgCreated.setImageResource(R.drawable.ic_option_selected)
        binding.imgName.setImageResource(R.drawable.ic_option)
        sortBy = SortBy.Created
    }

    private fun ascendingClick() {
        binding.imgAscending.setImageResource(R.drawable.ic_option_selected)
        binding.imgDescending.setImageResource(R.drawable.ic_option)
        isAscending = true
    }

    private fun descendingClick() {
        binding.imgAscending.setImageResource(R.drawable.ic_option)
        binding.imgDescending.setImageResource(R.drawable.ic_option_selected)
        isAscending = false
    }

    private fun nameClick() {
        binding.imgModified.setImageResource(R.drawable.ic_option)
        binding.imgCreated.setImageResource(R.drawable.ic_option)
        binding.imgName.setImageResource(R.drawable.ic_option_selected)
        sortBy = SortBy.Name
    }

    private fun onClick() {
        binding.imgModified.setOnClickListener {
            modifyClick()
        }
        binding.tvModify.setOnClickListener { binding.imgModified.performClick() }
        binding.imgCreated.setOnClickListener {
            createdClick()
        }
        binding.tvCreated.setOnClickListener { binding.imgCreated.performClick() }
        binding.imgName.setOnClickListener {
            nameClick()
        }
        binding.tvName.setOnClickListener { binding.imgName.performClick() }

        binding.imgAscending.setOnClickListener {
            ascendingClick()
        }
        binding.tvAscending.setOnClickListener { binding.imgAscending.performClick() }
        binding.imgDescending.setOnClickListener {
            descendingClick()
        }
        binding.tvDescending.setOnClickListener { binding.imgDescending.performClick() }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.imgBack.setOnClickListener { dismiss() }
        binding.tvAllow.setOnClickListener {
            callback?.run { onSort(sortBy, isAscending) }
            dismiss()
        }
    }

    fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvModify.setSize(14)
        binding.tvCreated.setSize(14)
        binding.tvName.setSize(14)
        binding.tvAscending.setSize(14)
        binding.tvDescending.setSize(14)

        binding.tvCancel.setSize(14)
        binding.tvAllow.setSize(14)
    }

    interface ISort {
        fun onSort(option: SortBy, isAscend: Boolean)
    }

}