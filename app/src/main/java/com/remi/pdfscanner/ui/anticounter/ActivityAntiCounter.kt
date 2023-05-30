package com.remi.pdfscanner.ui.anticounter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityAntiCounterBinding
import com.remi.pdfscanner.ui.editIamge.ConvertConfig
import com.remi.pdfscanner.ui.results.ActivityShowResult
import com.remi.pdfscanner.ui.setting.OptionSize
import com.remi.pdfscanner.ui.takephoto.ActivityTakePhoto
import com.remi.pdfscanner.util.*
import org.wysaid.nativePort.CGENativeLibrary
import kotlin.properties.Delegates.observable

class ActivityAntiCounter : BaseActivity<ActivityAntiCounterBinding>(ActivityAntiCounterBinding::inflate) {
    enum class Mode {
        Contrast, Brightness, Details
    }

    val viewModel by viewModels<AntiCounterViewModel>()

    var containRatio = -1f
    var isSingleSize = true
    var isPassport = false
    var currentOptionSize = OptionSize.A4
    var bitmapSrc1: Bitmap? = null
    var bitmapSrc2: Bitmap? = null

    var bitmapCurrent1: Bitmap? = null
    var bitmapCurrent2: Bitmap? = null
    var currentMode = Mode.Contrast
    var tempContrast = 50
    var tempBrightness = 50
    var tempDetail = 50
    var currentContrast = 50
    var currentBrightness = 50
    var currentDetail = 50


    private var launcherPickPageSize = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            currentOptionSize = when (MySharePref.getOptionSize(this)) {
                OptionSize.A3.name -> OptionSize.A3
                OptionSize.A5.name -> OptionSize.A5
                OptionSize.B4.name -> OptionSize.B4
                OptionSize.B5.name -> OptionSize.B5
                else -> OptionSize.A4
            }
            binding.tvPageSize.text = MySharePref.getOptionSize(this)
            setPageSize(currentOptionSize)
        }
    }

    override fun setSize() {
        binding.tvNext.setSize(14)
        binding.tvAntiCounter.setSize(11)
        binding.tvAdjust.setSize(11)
        binding.tvPageSize.setSize(14)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isSingleSize = intent.getBooleanExtra("is_single_side", true)
        val cameraMode = intent.getStringExtra("camera_mode") ?: ActivityTakePhoto.CameraMode.MODE_CARD.name
        isPassport = (cameraMode.equals(ActivityTakePhoto.CameraMode.MODE_PASSPORT.name, false))
        binding.antiCounterView.isPassport = isPassport

        currentOptionSize = OptionSize.valueOf(MySharePref.getOptionSize(this) ?: OptionSize.A4.name)
        binding.tvPageSize.text = MySharePref.getOptionSize(this)
        onClick()
        observable()
        fletchData()

        val temp = binding.containView.viewTreeObserver
        temp.addOnGlobalLayoutListener {
            if (containRatio == -1f) {
                containRatio = binding.containView.width * 1f / binding.containView.height
                setPageSize(OptionSize.A4)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.layoutAdjust.visibility == View.VISIBLE) {
                    binding.imgClose.performClick()
                } else finish()
            }
        })


    }

    private fun fletchData() {
        val listImageIn: List<String> = Gson().fromJson(intent.getStringExtra("list_image")!!, object : TypeToken<List<String>>() {}.type)

        Glide.with(this).asBitmap().load(listImageIn[0]).into(object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(placeholder: Drawable?) {
            }

            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                bitmapSrc1 = resource
                binding.antiCounterView.bitmap1 = resource
                binding.antiCounterView.invalidate()
            }
        })
        if (!isSingleSize) {
            Glide.with(this).asBitmap().load(listImageIn[1]).into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmapSrc2 = resource
                    binding.antiCounterView.bitmap2 = resource
                    binding.antiCounterView.invalidate()
                }
            })
        }

    }

    private fun convertRatioToString(ratio: Float): String {
        return "${(ratio * 10000).toInt()}:10000"
    }

    private fun setPageSize(size: OptionSize) {
        ConstraintSet().apply {
            clone(binding.containView)
            if (containRatio < size.ratio) {
                constrainPercentWidth(binding.antiCounterView.id, .9f)
            } else
                constrainPercentHeight(binding.antiCounterView.id, .9f)
            setDimensionRatio(binding.antiCounterView.id, convertRatioToString(size.ratio))
            applyTo(binding.containView)
        }
    }

    private fun observable() {
        viewModel.tempSize.observe(this) {
            binding.antiCounterView.setTextSize(it)
        }
        viewModel.tempTransparent.observe(this) {
            binding.antiCounterView.transparent = it
            binding.antiCounterView.invalidate()
        }
        viewModel.tempText.observe(this) {
            binding.antiCounterView.text = it
            binding.antiCounterView.invalidate()
        }
        viewModel.tempColor.observe(this) {
            binding.antiCounterView.myColor = it
            binding.antiCounterView.invalidate()
        }

    }


    private fun onClick() {
        binding.btnAntiCounter.setOnClickListener {
            BottomSheetAntiCounter(callback = {
                binding.antiCounterView.myColor = it
                viewModel.tempColor.postValue(it)
                binding.antiCounterView.invalidate()
            }).show(supportFragmentManager, "add_anti_counter")
        }
        binding.btnPageSize.setOnClickListener {
            launcherPickPageSize.launch(Intent(this, ActivityPickPageSize::class.java))
        }
        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.tvNext.setOnClickListener {
            val url = Common.saveBitmapToCache(this, binding.antiCounterView.getBitmapBySize(currentOptionSize), "${System.currentTimeMillis()}_card")
            startActivity(Intent(this, ActivityShowResult::class.java).apply {
                putExtra("list_image", Gson().toJson(listOf(url)))
            })
        }
        binding.btnAdjust.setOnClickListener {
            showAdjust()
        }
        binding.seekbar.setListener {
            when (currentMode) {
                Mode.Contrast -> {
                    tempContrast = it
                }
                Mode.Brightness -> {
                    tempBrightness = it
                }
                Mode.Details -> {
                    tempDetail = it
                }
            }
            changeBitmapWithConfig()
        }

        val colorDefault = ContextCompat.getColor(this, R.color.color_7C7C7C)
        val colorSelected = ContextCompat.getColor(this, R.color.main_color)

        binding.btnContrast.setOnClickListener {
            binding.imgContrast.setColorFilter(colorSelected)
            binding.imgBrightness.setColorFilter(colorDefault)
            binding.imgDetails.setColorFilter(colorDefault)

            binding.tvContrast.setTextColor(colorSelected)
            binding.tvBrightness.setTextColor(colorDefault)
            binding.tvDetails.setTextColor(colorDefault)

            currentMode = Mode.Contrast
            binding.seekbar.progress = tempContrast
        }
        binding.btnBrightness.setOnClickListener {
            binding.imgContrast.setColorFilter(colorDefault)
            binding.imgBrightness.setColorFilter(colorSelected)
            binding.imgDetails.setColorFilter(colorDefault)

            binding.tvContrast.setTextColor(colorDefault)
            binding.tvBrightness.setTextColor(colorSelected)
            binding.tvDetails.setTextColor(colorDefault)

            currentMode = Mode.Brightness
            binding.seekbar.progress = tempBrightness
        }
        binding.btnDetails.setOnClickListener {
            binding.imgContrast.setColorFilter(colorDefault)
            binding.imgBrightness.setColorFilter(colorDefault)
            binding.imgDetails.setColorFilter(colorSelected)

            binding.tvContrast.setTextColor(colorDefault)
            binding.tvBrightness.setTextColor(colorDefault)
            binding.tvDetails.setTextColor(colorSelected)
            currentMode = Mode.Details
            binding.seekbar.progress = tempDetail
        }
        binding.imgClose.setOnClickListener {
            bitmapCurrent1?.run {
                binding.antiCounterView.bitmap1 = this
                binding.antiCounterView.invalidate()
            }
            bitmapCurrent2?.run {
                binding.antiCounterView.bitmap2 = this
                binding.antiCounterView.invalidate()
            }
            tempBrightness = currentBrightness
            tempContrast = currentContrast
            tempDetail = currentDetail
            binding.layoutAdjust.hide()
        }
        binding.imgDone.setOnClickListener {
            currentBrightness = tempBrightness
            currentContrast = tempContrast
            currentDetail = tempDetail
            bitmapCurrent1 = binding.antiCounterView.bitmap1
            bitmapCurrent2 = binding.antiCounterView.bitmap2
            binding.layoutAdjust.hide()
        }
    }

    private fun changeBitmapWithConfig() {
        bitmapSrc1?.run {
            val ruleString = ConvertConfig.getConfigMulti(tempBrightness, tempContrast, tempDetail)

            binding.antiCounterView.bitmap1  = CGENativeLibrary.filterImage_MultipleEffects(this, ruleString, 1.0f)
        }
        bitmapSrc2?.run {
            val ruleString = ConvertConfig.getConfigMulti(tempBrightness, tempContrast, tempDetail)
            binding.antiCounterView.bitmap2  = CGENativeLibrary.filterImage_MultipleEffects(this, ruleString, 1.0f)
        }
        binding.antiCounterView.invalidate()
    }

    private fun showAdjust() {
        binding.layoutAdjust.show()
        when (currentMode) {
            Mode.Contrast -> {
                binding.seekbar.progress = currentContrast
            }
            Mode.Brightness -> {
                binding.seekbar.progress = currentBrightness
            }
            Mode.Details -> {
                binding.seekbar.progress = currentDetail
            }
        }
    }
}