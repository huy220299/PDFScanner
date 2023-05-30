package com.remi.pdfscanner.ui.pdfdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.remi.pdfscanner.databinding.FragmentPdfViewerBinding
import com.remi.pdfscanner.ui.anticounter.ColorAdapter
import com.remi.pdfscanner.util.setSize
import javax.inject.Inject


class DetailsPdfFragment @Inject constructor() : Fragment() {
    lateinit var binding: FragmentPdfViewerBinding

    private val vm by activityViewModels<DetailsPdfViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvTitle.isSelected = true
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSize()
        binding.imgBack.setOnClickListener { vm.flagBack.postValue(true) }
        vm.nameFile.observe(viewLifecycleOwner){
            binding.tvTitle.text = it
        }
        vm.listBitmap.observe(requireActivity()) {
            it?.run {
                binding.recyclerview.apply {
                    adapter = DetailsPdfAdapter( ).apply {
                        listItem = this@run
                        isShowMode = true
                    }
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                }
            }
        }


    }

}