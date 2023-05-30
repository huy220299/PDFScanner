package com.remi.pdfscanner.ui.crop

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.View.GONE
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.TaskStackBuilder
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.base.IYesNoCallback
import com.remi.pdfscanner.base.ViewPagerAddFragmentsAdapter
import com.remi.pdfscanner.databinding.ActivityCropBinding
import com.remi.pdfscanner.ui.anticounter.ActivityAntiCounter
import com.remi.pdfscanner.ui.dialog.DialogCustom
import com.remi.pdfscanner.ui.dialog.DialogRecognize
import com.remi.pdfscanner.ui.editIamge.ActivityEditImage
import com.remi.pdfscanner.ui.editIamge.DepthPageTransformer
import com.remi.pdfscanner.ui.pdfdetails.ActivityDetailsPDF
import com.remi.pdfscanner.ui.pickphoto.MyImage
import com.remi.pdfscanner.ui.takephoto.ActivityTakePhoto
import com.remi.pdfscanner.util.FileUtil
import com.remi.pdfscanner.util.replace

@SuppressLint("SetTextI18n")
class CropImageActivity : BaseActivity<ActivityCropBinding>(ActivityCropBinding::inflate) {
    private lateinit var dialogLoading: DialogRecognize
    private val viewModel by viewModels<CropImageActivityViewModel>()
    var listImage: MutableList<MyImage> = ArrayList()

    override fun setSize() {

    }

    private var launcherPickReplace = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.run {
                val pos = getIntExtra("position_replace", 0)
                val url = getStringExtra("image_replace")
                url?.run {
                    viewModel.listUrlImage[pos] = this
                    viewModel.flagReloadImage.postValue(true)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogLoading = DialogRecognize(this)


        listImage = Gson().fromJson(intent.getStringExtra("list_image")!!, object : TypeToken<MutableList<MyImage>>() {}.type)

        if (listImage.size == 1) {
            binding.imgNext.visibility = GONE
            binding.imgPrevious.visibility = GONE
        }
        val dialogLoading = DialogRecognize(this).apply { text = "   Loading..." }
        dialogLoading.show()
        viewModel.isLoadingFirstImage.observe(this) {
            if (dialogLoading.isShowing) dialogLoading.dismiss()
            binding.tvCurrentPage.text = "1/${viewModel.listUrlImage.size}"
            binding.tvCurrentPage.visibility = View.VISIBLE
            binding.imgNext.visibility = View.VISIBLE
        }
        viewModel.listUrlImage = listImage.map { it.path }.toMutableList()

        binding.viewpager2.adapter = ViewPagerAddFragmentsAdapter(supportFragmentManager, lifecycle).apply {
            for (i in listImage.indices) {
                addFrag(FragmentCropImage(i))
            }
        }
        binding.viewpager2.isUserInputEnabled = false
        binding.viewpager2.offscreenPageLimit = 5
        binding.viewpager2.setPageTransformer(DepthPageTransformer())
        onClick()
        viewModelObservable()
        binding.viewpager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                viewModel.positionCurrentImage = position
                viewModel.flagGetUIInformation.postValue(true)
                //send notice to fragment to update ui

            }
        })
    }

    private fun viewModelObservable() {
        viewModel.flagEndSave.observe(this) {
            viewModel.listImageSaved.value?.let {
                if (it.size == viewModel.listUrlImage.size) {
                    if (dialogLoading.isShowing) dialogLoading.dismiss()
                    val cameraMode = intent.getStringExtra("camera_mode") ?: ActivityTakePhoto.CameraMode.MODE_DOCS.name
                    if (cameraMode == ActivityTakePhoto.CameraMode.MODE_CARD.name) {
                        startActivity(Intent(this, ActivityAntiCounter::class.java).apply {
                            putExtra("list_image", Gson().toJson(it))
                            putExtra("camera_mode", cameraMode)
                            putExtra("is_single_side", intent.getBooleanExtra("is_single_side", true))
                        })
                    } else if (cameraMode == ActivityTakePhoto.CameraMode.MODE_PASSPORT.name) {
                        startActivity(Intent(this, ActivityAntiCounter::class.java).apply {
                            putExtra("list_image", Gson().toJson(it))
                            putExtra("camera_mode", cameraMode)
                        })
                    } else {
                        startActivity(Intent(this, ActivityEditImage::class.java).apply {
                            putExtra("list_image", Gson().toJson(it))
                        })
                    }

                }
            }
        }

        viewModel.flagUpdateUI.observe(this) {
            binding.imgRedo.isEnabled = viewModel.isCanNext
            binding.imgRedo.alpha = if (viewModel.isCanNext) 1f else .5f
            binding.imgUndo.isEnabled = viewModel.isCanPrevious
            binding.imgUndo.alpha = if (viewModel.isCanPrevious) 1f else .5f
        }
    }

    private fun onClick() {
        binding.imgUndo.setOnClickListener {
            viewModel.actionDo(false)
        }
        binding.imgRedo.setOnClickListener {
            viewModel.actionDo(true)
        }

        binding.tvNext.setOnClickListener {
            dialogLoading.setCancelable(false)
            dialogLoading.show()
            viewModel.listImageSaved.value!!.clear()
            viewModel.flagStartSave.postValue(true)
        }

        binding.btnRotateRight.setOnClickListener {
            viewModel.rotateImage(true)
        }

        binding.btnRotateLeft.setOnClickListener {
            viewModel.rotateImage(false)
        }

        var isScan = true
        binding.btnAutoCrop.setOnClickListener {
            isScan = !isScan
            viewModel.enableAutoScanImage(isScan)
            binding.imgAutoScan.setImageResource(if (isScan) R.drawable.ic_auto_scan else R.drawable.ic_auto_scan_unable)
            binding.tvAutoScan.text = if (isScan) getString(R.string.auto_crop) else getString(R.string.auto_crop_unable)
        }


        binding.imgNext.setOnClickListener {
            binding.viewpager2.currentItem = binding.viewpager2.currentItem + 1
            binding.tvCurrentPage.text = "${binding.viewpager2.currentItem + 1}/${viewModel.listUrlImage.size}"
            binding.imgPrevious.visibility = View.VISIBLE
            if (binding.viewpager2.currentItem == binding.viewpager2.childCount - 1) binding.imgNext.visibility = View.INVISIBLE

        }

        binding.imgPrevious.setOnClickListener {
            binding.viewpager2.currentItem = binding.viewpager2.currentItem - 1
            binding.tvCurrentPage.text = "${binding.viewpager2.currentItem + 1}/${viewModel.listUrlImage.size}"
            binding.imgNext.visibility = View.VISIBLE
            if (binding.viewpager2.currentItem == 0) binding.imgPrevious.visibility = View.INVISIBLE
        }

        binding.btnDelete.setOnClickListener {
            DialogCustom(this).apply {
                titleValue = "Are you sure you want to delete?"
                descriptionValue = "Once deleted, these photos cannot be recovered."
                cancelValue = "Cancel"
                yesValue = "Delete"
                idImage = R.drawable.img_delete
                callback = object : IYesNoCallback {
                    override fun onYes() {
                        //todo delete image here
                        val pos = this@CropImageActivity.binding.viewpager2.currentItem

                        listImage.removeAt(pos)
                        viewModel.listUrlImage = listImage.map { it.path }.toMutableList()
                        (this@CropImageActivity.binding.viewpager2.adapter as ViewPagerAddFragmentsAdapter).removeFag(pos)
                        (this@CropImageActivity.binding.viewpager2.adapter as ViewPagerAddFragmentsAdapter).notifyItemRangeChanged(0, listImage.size, true)
                        this@CropImageActivity.binding.tvCurrentPage.text = "${this@CropImageActivity.binding.viewpager2.currentItem + 1}/${viewModel.listUrlImage.size}"
                    }
                }
                show()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DialogCustom(this@CropImageActivity).apply {
                    titleValue = "Leave this page?"
                    descriptionValue = "The photos taken will be discarded and cannot be recovered."
                    cancelValue = "Cancel"
                    yesValue = "OK"
                    idImage = R.drawable.img_back
                    callback = object : IYesNoCallback {
                        override fun onYes() {
                            finish()
                        }
                    }
                    show()
                }

            }
        })

        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnReplace.setOnClickListener {
            launcherPickReplace.launch(Intent(this, ActivityTakePhoto::class.java).apply {
                putExtra("task_replace", true)
                putExtra("position_replace", binding.viewpager2.currentItem)
            })
        }

    }


}