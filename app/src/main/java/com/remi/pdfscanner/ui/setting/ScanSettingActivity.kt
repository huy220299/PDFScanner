package com.remi.pdfscanner.ui.setting

import android.os.Bundle
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityScanSettingBinding
import com.remi.pdfscanner.util.MySharePref
import com.remi.pdfscanner.util.setSize

class ScanSettingActivity : BaseActivity<ActivityScanSettingBinding>(ActivityScanSettingBinding::inflate) {
    //    lateinit var binding: ActivityScanSettingBinding
    override fun binding() {
        isFullScreen = true
        super.binding()
    }

    override fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvBatchScan.setSize(16)
        binding.tvManually.setSize(14)
        binding.tvManuallySub.setSize(12)
        binding.tvStoragePath.setSize(16)
        binding.tvDefault.setSize(14)
        binding.tvDefaultSub.setSize(12)

        binding.switchManual.isChecked = MySharePref.isManualCrop(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onClick()
    }

    private fun onClick() {
        binding.switchManual.setOnCheckedChangeListener { _, isChecked ->
            MySharePref.updateManualCrop(this@ScanSettingActivity, isChecked)
        }
        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
}