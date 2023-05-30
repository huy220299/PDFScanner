package com.remi.pdfscanner.ui.editIamge

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.base.IYesNoCallback
import com.remi.pdfscanner.base.ViewPagerAddFragmentsAdapter
import com.remi.pdfscanner.customview.stickerview.*
import com.remi.pdfscanner.databinding.ActivityEditImageBinding
import com.remi.pdfscanner.repository.DbRepository
import com.remi.pdfscanner.ui.addsignature.ActivitySignature
import com.remi.pdfscanner.ui.anticounter.AntiCounterViewModel
import com.remi.pdfscanner.ui.anticounter.BottomSheetAntiCounter
import com.remi.pdfscanner.ui.dialog.DialogCustom
import com.remi.pdfscanner.ui.dialog.DialogRecognize
import com.remi.pdfscanner.ui.recognizetext.ActivityResultRecognizeText
import com.remi.pdfscanner.ui.results.ActivityShowResult
import com.remi.pdfscanner.util.FileUtil
import com.remi.pdfscanner.util.hide
import com.remi.pdfscanner.util.setSize
import com.remi.pdfscanner.util.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class ActivityEditImage : BaseActivity<ActivityEditImageBinding>(ActivityEditImageBinding::inflate), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var job: Job

    private val viewModel: EditImageActivityViewModel by viewModels()

    private val viewModelAntiCounterfeit:AntiCounterViewModel by viewModels()

    @Inject
    lateinit var repository: DbRepository

    @Inject
    lateinit var signatureAdapter: SignatureAdapter

    private var launcherAddSignature = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.run {
                val url = this.getStringExtra("url") ?: ""
                if (url.isNotBlank()) {
                    Glide.with(mContext).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            binding.stickerView.removeAllStickers()
                            binding.stickerView.addSticker(DrawableSticker(mContext, resource))
                        }
                    })
                    signatureAdapter.listUrl.add(1, url)
                    signatureAdapter.notifyItemInserted(1)
                }
            }
        }

    }
    private lateinit var dialogLoading: DialogRecognize
    private var listImageIn: List<String> = ArrayList()

    override fun setSize() {
        binding.tvNext.setSize(14)
        binding.tvRecognize.setSize(11)
        binding.tvAdjust.setSize(11)
        binding.tvAntiCounter.setSize(11)
        binding.tvSignature.setSize(11)
    }
    var isAddSignature = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        dialogLoading = DialogRecognize(this)
        listImageIn = Gson().fromJson(intent.getStringExtra("list_image")!!, object : TypeToken<List<String>>() {}.type)
//        CGENativeLibrary.setLoadImageCallback(mLoadImageCallback, Object())
        onClick()
        setupViewpager()
        observable()

        viewModel.flagEndSave.observe(this) {
            viewModel.listImageSaved.value?.let {
                if (it.size == listImageIn.size) {
                    if (dialogLoading.isShowing) dialogLoading.dismiss()
                    startActivity(Intent(this, ActivityShowResult::class.java).apply {
                        putExtra("list_image", Gson().toJson(it))
                         putExtra("edit_from_file", isAddSignature)
                    })
                }
            }
        }
        isAddSignature = intent.getBooleanExtra("edit_from_file", false)
        if (isAddSignature) {
            binding.btnAntiCounter.show()
            binding.btnSignature.show()
            binding.stickerView.show()
            binding.antiCounterView.show()
            setupStickerView()
            setupRecyclerviewSignature()

        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.layoutSignature.visibility == View.VISIBLE)
                    binding.layoutSignature.hide()
                else finish()
            }
        })
    }


    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun setupViewpager() {
        binding.viewpager2.adapter = ViewPagerAddFragmentsAdapter(supportFragmentManager, lifecycle).apply {
            for (i in listImageIn.indices) {
                addFrag(FragmentPhotoView().apply {
                    url = listImageIn[i]
                    pos = i
                })
            }
        }
        binding.viewpager2.offscreenPageLimit = listImageIn.size
        binding.viewpager2.setPageTransformer(DepthPageTransformer())
        binding.viewpager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.currentItemPosition.postValue(position)
            }
        })
    }

    private fun setupRecyclerviewSignature() {
        signatureAdapter.apply {
            listUrl = repository.getAllImageSignature(mContext).toMutableList()
            listUrl.add(0, "this is for first position")
            callback = object : SignatureAdapter.ISignature {
                override fun onAddImage() {
                    launcherAddSignature.launch(Intent(mContext, ActivitySignature::class.java))
                }

                override fun onClick(url: String) {
                    binding.stickerView.removeAllStickers()
                    Glide.with(mContext).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            binding.stickerView.addSticker(DrawableSticker(mContext, resource))
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })

                }

                override fun onClickDelete(url: String, position: Int) {
                    DialogCustom(mContext).apply {
                        titleValue = "Delete Signature"
                        descriptionValue = "Do you want to delete this signature?"
                        idImage = R.drawable.img_delete
                        cancelValue = "Cancel"
                        yesValue = "Delete"
                        callback = object : IYesNoCallback {
                            override fun onYes() {
                                FileUtil.deleteFile(url)
                                signatureAdapter.listUrl.removeAt(position)
                                signatureAdapter.notifyItemRemoved(position)
                            }
                        }
                        show()
                    }
                }
            }
        }
        binding.recyclerviewSignature.apply {
            adapter = signatureAdapter
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun observable() {
        viewModelAntiCounterfeit.tempSize.observe(this) {
            binding.antiCounterView.setTextSize(it)
        }
        viewModelAntiCounterfeit.tempTransparent.observe(this) {
            binding.antiCounterView.transparent = it
            binding.antiCounterView.invalidate()
        }
        viewModelAntiCounterfeit.tempText.observe(this) {
            binding.antiCounterView.text = it
            binding.antiCounterView.invalidate()
        }
        viewModelAntiCounterfeit.tempColor.observe(this) {
            binding.antiCounterView.myColor = it
            binding.antiCounterView.invalidate()
        }

    }

    private fun onClick() {
        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnRecognize.setOnClickListener {
            startActivity(Intent(this, ActivityResultRecognizeText::class.java).apply {
                if (binding.viewpager2.currentItem in listImageIn.indices)
                    putExtra("url", listImageIn[binding.viewpager2.currentItem])
            })
        }
        binding.btnAdjust.setOnClickListener {
            BottomSheetEdit().show(supportFragmentManager, "edit_bottom_sheet")
        }
        binding.tvNext.setOnClickListener {
            //if have signature and anti counterfeit: get this bitmap
            if (isAddSignature){
                viewModel.bitmapSignature = binding.stickerView.getBitmap(1080)
                viewModel.bitmapAntiCounterfeit = binding.antiCounterView.getBitmap(1080)
            }

            dialogLoading.setCancelable(false)
            dialogLoading.show()
            viewModel.listImageSaved.value!!.clear()
            viewModel.flagStartSave.postValue(true)
        }
        binding.btnSignature.setOnClickListener {
            binding.layoutSignature.show()
        }
        binding.btnAntiCounter.setOnClickListener {
            BottomSheetAntiCounter(callback = {
                binding.antiCounterView.myColor = it
                viewModelAntiCounterfeit.tempColor.postValue(it)
                binding.antiCounterView.invalidate()
            }).show(supportFragmentManager,"anti_counterfeit")
        }

    }

    /**
     * stickerView to show signature
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupStickerView() {
        val deleteIcon = BitmapStickerIcon(ContextCompat.getDrawable(this,
            R.drawable.sticker_ic_delete),
            BitmapStickerIcon.LEFT_TOP)
        deleteIcon.iconEvent =DeleteIconEvent()

        val zoomIcon = BitmapStickerIcon(ContextCompat.getDrawable(this,
            R.drawable.sticker_ic_scale),
            BitmapStickerIcon.RIGHT_BOTOM)
        zoomIcon.iconEvent = ZoomIconEvent()

        binding.stickerView.apply {
            setBackgroundColor(Color.TRANSPARENT)
            isLocked = false
            isConstrained = true
            showBorder = true
            showIcons = true
            icons = listOf(deleteIcon, zoomIcon)
        }

        binding.stickerView.onStickerOperationListener = object : OnStickerOperationListener{
            override fun onStickerAdded(sticker: Sticker) {
                super.onStickerAdded(sticker)
                binding.stickerView.showBorder = true
                binding.stickerView.showIcons = true
                binding.stickerView.invalidate()
            }

            override fun onStickerClicked(sticker: Sticker) {
                super.onStickerClicked(sticker)
                binding.stickerView.showBorder = true
                binding.stickerView.showIcons = true
                binding.stickerView.invalidate()
            }

            override fun onStickerDragFinished(sticker: Sticker) {
                super.onStickerDragFinished(sticker)
                binding.stickerView.showBorder = true
                binding.stickerView.showIcons = true
                binding.stickerView.invalidate()
            }
        }
        binding.stickerView.setOnTouchListener { p0, p1 ->
            binding.stickerView.showBorder = false
            binding.stickerView.showIcons = false
            binding.stickerView.invalidate()
            false
        }

    }


}