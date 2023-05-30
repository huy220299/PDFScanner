package com.remi.pdfscanner.ui.takephoto

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityCameraBinding
import com.remi.pdfscanner.ui.crop.CropImageActivity
import com.remi.pdfscanner.ui.dialog.DialogRecognize
import com.remi.pdfscanner.ui.pickphoto.MyImage
import com.remi.pdfscanner.ui.pickphoto.ShowImageActivity
import com.remi.pdfscanner.util.Common
import com.remi.pdfscanner.util.setSize
import com.remi.pdfscanner.util.show
import kotlinx.coroutines.*
import org.wysaid.camera.CameraInstance
import org.wysaid.view.CameraGLSurfaceView
import kotlin.coroutines.CoroutineContext

class ActivityTakePhoto : BaseActivity<ActivityCameraBinding>(ActivityCameraBinding::inflate), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var job: Job
    lateinit var dialogLoading: DialogRecognize
    var isSingleSideCard = true

    var mCurrentInstance: ActivityTakePhoto? = null
    private val listDocsImage: MutableList<Bitmap> = ArrayList()
    private val listCardImage: MutableList<Bitmap> = ArrayList()
    private var launcherShowList = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        isFlashOn = false
        binding.imgFlash.setImageResource(R.drawable.ic_flash)

        binding.imgBack.visibility = VISIBLE
        binding.tvBack.visibility = VISIBLE
        binding.imgGallery.visibility = VISIBLE
        binding.tvGallery.visibility = VISIBLE
        binding.layoutPreviewSmall.visibility = INVISIBLE
        binding.imgNextShot.visibility = INVISIBLE
        listDocsImage.clear()
        listCardImage.clear()

        binding.tvOcr.isEnabled = false
        binding.tvCard.isEnabled = false
        binding.tvPassport.isEnabled = false
    }
    private var launcherToCrop = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        isFlashOn = false
        binding.imgFlash.setImageResource(R.drawable.ic_flash)
        listCardImage.clear()
        if (dialogLoading.isShowing) dialogLoading.dismiss()

        binding.tvOcr.isEnabled = true
        binding.tvCard.isEnabled = true
        binding.tvPassport.isEnabled = true

    }
    private var launcherPickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode== RESULT_OK){
                result.data?.run {
                    val url = this.getStringExtra("image_picked")
                    if (isFromTaskReplace){
                        setResult(RESULT_OK,Intent().apply {
                            putExtra("image_replace", url)
                            putExtra("position_replace", intent.getIntExtra("position_replace", 0))
                        })
                        finish()
                    }else{
                        //todo pick image from gallery
                        Glide.with(this@ActivityTakePhoto).asBitmap().load(url).into(object :CustomTarget<Bitmap>(){
                            override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                                launch(Dispatchers.Main) {
                                    when (currentCameraMode) {
                                        CameraMode.MODE_DOCS -> if (isFromTaskReplace) handleBitmapReplace(bitmap) else handleBitmapDocs(bitmap)
                                        CameraMode.MODE_OCR -> handleBitmapOCR(bitmap)
                                        CameraMode.MODE_CARD -> handleBitmapCard(bitmap)
                                        CameraMode.MODE_PASSPORT -> handleBitmapPassport(bitmap)
                                    }
                                    binding.imgShot.isEnabled = true
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {

                            }
                        })
                    }

                }
            }

    }
     var isFromTaskReplace=false


    enum class CameraMode {
        MODE_DOCS,
        MODE_OCR,
        MODE_CARD,
        MODE_PASSPORT
    }

    var currentCameraMode = CameraMode.MODE_DOCS

    override fun setSize() {
        binding.tvBack.setSize(10)
        binding.tvGallery.setSize(10)
        binding.tvDoc.setSize(13)
        binding.tvOcr.setSize(13)
        binding.tvCard.setSize(13)
        binding.tvPassport.setSize(13)
        binding.tvRecognizeOcr.setSize(13)
        binding.tvImageCount.setSize(10)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFromTaskReplace = intent.getBooleanExtra("task_replace", false)

        dialogLoading = DialogRecognize(this@ActivityTakePhoto).apply {
            text = "   Loading..."
        }
        job = Job()
        setCamera()
        onClick()
        binding.tvDoc.performClick()
        if (isFromTaskReplace) {
            binding.tvOcr.show(false)
            binding.tvCard.show(false)
            binding.tvPassport.show(false)
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    var isFlashOn = false
    private fun onClick() {

        binding.imgFlash.setOnClickListener {
            if (isFlashOn) {
                binding.myCamera.setFlashLightMode(Camera.Parameters.FLASH_MODE_OFF)
                isFlashOn = false
                binding.imgFlash.setImageResource(R.drawable.ic_flash)
            } else {
                binding.myCamera.setFlashLightMode(Camera.Parameters.FLASH_MODE_TORCH)
                isFlashOn = true
                binding.imgFlash.setImageResource(R.drawable.ic_flash_on)
            }
        }

        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.tvBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.imgGallery.setOnClickListener {
            launcherPickImage.launch(Intent(this, ShowImageActivity::class.java)
                .apply {
                    putExtra("task_pick", true)
                })
        }

        binding.imgShot.setOnClickListener {
            binding.imgShot.isEnabled = false
            binding.myCamera.takeShot { bitmap ->
                launch(Dispatchers.Main) {
                    when (currentCameraMode) {
                        CameraMode.MODE_DOCS -> if (isFromTaskReplace) handleBitmapReplace(bitmap) else handleBitmapDocs(bitmap)
                        CameraMode.MODE_OCR -> handleBitmapOCR(bitmap)
                        CameraMode.MODE_CARD -> handleBitmapCard(bitmap)
                        CameraMode.MODE_PASSPORT -> handleBitmapPassport(bitmap)
                    }
                    binding.imgShot.isEnabled = true
                }

            }
        }

        binding.imgNextShot.setOnClickListener {
            if (currentCameraMode == CameraMode.MODE_DOCS) {
                launch(Dispatchers.Main) {
                    dialogLoading.show()
                    val list = async(Dispatchers.IO) { saveImage(listDocsImage) }
                    val data = list.await()
                    dialogLoading.dismiss()
                    launcherShowList.launch(Intent(this@ActivityTakePhoto, ActivityShowDocsImage::class.java).apply {
                        putExtra("list_images", Gson().toJson(data))
                    })
                }
            }

        }

        binding.imgSingleSide.setOnClickListener {
            isSingleSideCard = true

            Glide.with(this).load(R.drawable.img_single_side_preview).into(binding.imgPreviewCardOption)
            binding.imgSingleSide.setColorFilter(ContextCompat.getColor(this, R.color.main_color))
            binding.imgBothSide.setColorFilter(ContextCompat.getColor(this, R.color.white))
            binding.tvSingleSize.setTextColor(ContextCompat.getColor(this, R.color.main_color))
            binding.tvBothSide.setTextColor(ContextCompat.getColor(this, R.color.white))

        }
        binding.imgBothSide.setOnClickListener {
            isSingleSideCard = false

            Glide.with(this).load(R.drawable.img_both_side_preview).into(binding.imgPreviewCardOption)
            binding.imgSingleSide.setColorFilter(ContextCompat.getColor(this, R.color.white))
            binding.imgBothSide.setColorFilter(ContextCompat.getColor(this, R.color.main_color))
            binding.tvSingleSize.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvBothSide.setTextColor(ContextCompat.getColor(this, R.color.main_color))

        }

        binding.imgBackCard.setOnClickListener {
            binding.tvRecognizeOcr.visibility = INVISIBLE
            binding.imgBackCard.visibility = INVISIBLE
            binding.layoutChoseSideCard.visibility = VISIBLE
            binding.layoutCardOption.visibility = VISIBLE
            listCardImage.clear()
        }
        binding.tvMakeItNow.setOnClickListener {
            binding.layoutChoseSideCard.visibility = GONE
            binding.layoutCardOption.visibility = GONE
            binding.overlayCard.visibility = VISIBLE
            binding.tvRecognizeOcr.visibility = VISIBLE
            binding.imgBackCard.visibility = VISIBLE
            listCardImage.clear()

            binding.overlayCard.text = if (isSingleSideCard) "" else "Font Page"
            binding.overlayCard.invalidate()
        }
        binding.tvMakeItNowPassport.setOnClickListener {
            binding.layoutChosePassport.visibility = GONE
        }
        binding.layoutChosePassport.setOnClickListener { }
        bottomTextViewClick()
    }

    private fun bottomTextViewClick() {
        binding.tvDoc.setOnClickListener {
            currentCameraMode = CameraMode.MODE_DOCS
            TransitionManager.beginDelayedTransition(binding.root)
            binding.tvRecognizeOcr.visibility = INVISIBLE
            binding.overlayCard.visibility = INVISIBLE
            binding.layoutCardOption.visibility = INVISIBLE
            binding.layoutChoseSideCard.visibility = INVISIBLE
            binding.layoutChosePassport.visibility = INVISIBLE
            setSelectedOption(it as TextView)
        }
        binding.tvOcr.setOnClickListener {
            currentCameraMode = CameraMode.MODE_OCR
            TransitionManager.beginDelayedTransition(binding.root)
            binding.tvRecognizeOcr.visibility = VISIBLE
            binding.overlayCard.visibility = INVISIBLE
            binding.layoutCardOption.visibility = INVISIBLE
            binding.layoutChoseSideCard.visibility = INVISIBLE
            binding.layoutChosePassport.visibility = INVISIBLE
            setSelectedOption(it as TextView)
        }
        binding.tvCard.setOnClickListener {
            currentCameraMode = CameraMode.MODE_CARD
            TransitionManager.beginDelayedTransition(binding.root)
            binding.tvRecognizeOcr.visibility = INVISIBLE
            binding.overlayCard.visibility = INVISIBLE
            binding.layoutCardOption.visibility = VISIBLE
            binding.layoutChoseSideCard.visibility = VISIBLE
            binding.layoutChosePassport.visibility = INVISIBLE
            setSelectedOption(it as TextView)
        }
        binding.tvPassport.setOnClickListener {
            currentCameraMode = CameraMode.MODE_PASSPORT
            TransitionManager.beginDelayedTransition(binding.root)
            binding.tvRecognizeOcr.visibility = INVISIBLE
            binding.overlayCard.visibility = INVISIBLE
            binding.layoutCardOption.visibility = INVISIBLE
            binding.layoutChoseSideCard.visibility = INVISIBLE
            binding.layoutChosePassport.visibility = VISIBLE
            setSelectedOption(it as TextView)
        }
    }


    private fun handleBitmapDocs(bitmapIn: Bitmap) {
        Glide.with(this).load(bitmapIn).into(binding.imgPreviewSmall)
        binding.layoutPreviewSmall.visibility = VISIBLE
        binding.imgNextShot.visibility = VISIBLE
        //save image
        listDocsImage.add(bitmapIn)
        binding.tvImageCount.text = listDocsImage.size.toString()

        binding.imgBack.visibility = INVISIBLE
        binding.tvBack.visibility = INVISIBLE
        binding.imgGallery.visibility = INVISIBLE
        binding.tvGallery.visibility = INVISIBLE

        binding.tvOcr.isEnabled = false
        binding.tvCard.isEnabled = false
        binding.tvPassport.isEnabled = false
    }

    private suspend fun handleBitmapOCR(bitmapIn: Bitmap) {
        dialogLoading.show()
        val images = withContext(Dispatchers.IO) { saveImage(listOf(bitmapIn)) }
        val myImages = images.map { MyImage(it, false) }
        launcherToCrop.launch(Intent(this, CropImageActivity::class.java).apply {
            putExtra("list_image", Gson().toJson(myImages))
        })
    }

    /**
     * save bitmap then return url of bitmap to replace
     */
    private suspend fun handleBitmapReplace(bitmapIn: Bitmap) {
        dialogLoading.show()
        val image = withContext(Dispatchers.IO) { Common.saveBitmapToCache(this@ActivityTakePhoto, bitmapIn, "${System.currentTimeMillis()}") }
        setResult(RESULT_OK, Intent().apply {
            putExtra("image_replace", image)
            putExtra("position_replace", intent.getIntExtra("position_replace", 0))
        })
        finish()
    }

    private suspend fun handleBitmapPassport(bitmapIn: Bitmap) {
        dialogLoading.show()
        val images = withContext(Dispatchers.IO) { saveImage(listOf(bitmapIn)) }
        val myImages = images.map { MyImage(it, false) }
        launcherToCrop.launch(Intent(this, CropImageActivity::class.java).apply {
            putExtra("list_image", Gson().toJson(myImages))
            putExtra("camera_mode", CameraMode.MODE_PASSPORT.name)
        })
    }

    private fun handleBitmapCard(bitmapIn: Bitmap) {
        Glide.with(this).load(bitmapIn).into(binding.imgPreviewSmall)
        binding.layoutPreviewSmall.visibility = VISIBLE
        listCardImage.add(bitmapIn)
        binding.tvImageCount.text = listCardImage.size.toString()

        binding.imgBack.visibility = INVISIBLE
        binding.tvBack.visibility = INVISIBLE
//        binding.imgGallery.visibility = INVISIBLE
//        binding.tvGallery.visibility = INVISIBLE
        if (isSingleSideCard) {
            launch(Dispatchers.Main) {
                dialogLoading.show()
                val list = async(Dispatchers.IO) { saveImage(listCardImage) }
                val data = list.await()
                dialogLoading.dismiss()
                launcherShowList.launch(Intent(this@ActivityTakePhoto, CropImageActivity::class.java).apply {
                    putExtra("list_image", Gson().toJson(data.map { MyImage(it, false) }))
                    putExtra("camera_mode", CameraMode.MODE_CARD.name)
                    putExtra("is_single_side", isSingleSideCard)
                })
            }
        } else {
            if (listCardImage.size == 2) {
                launch(Dispatchers.Main) {
                    dialogLoading.show()
                    val list = async(Dispatchers.IO) { saveImage(listCardImage) }
                    val data = list.await()
                    dialogLoading.dismiss()
                    launcherToCrop.launch(Intent(this@ActivityTakePhoto, CropImageActivity::class.java).apply {
                        putExtra("list_image", Gson().toJson(data.map { MyImage(it, false) }))
                        putExtra("camera_mode", CameraMode.MODE_CARD.name)
                        putExtra("is_single_side", isSingleSideCard)
                    })
                    Log.e("~~~", "handleBitmapCard: $isSingleSideCard", )
                }
            } else {
                binding.overlayCard.text = "Back Page"
                binding.overlayCard.invalidate()
            }
        }

    }

    private fun setSelectedOption(tv: TextView) {
        val list = listOf(binding.tvDoc, binding.tvOcr, binding.tvCard, binding.tvPassport)
        list.forEach {
            if (it == tv)
                it.setTextColor(ContextCompat.getColor(this, R.color.main_color))
            else it.setTextColor(Color.parseColor("#66FFFFFF"))
        }
    }

    override fun onPause() {
        super.onPause()
        CameraInstance.getInstance().stopCamera()
        binding.myCamera.release(null)
        binding.myCamera.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.myCamera.onResume()
    }

    /**
     * init setting camera
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setCamera() {
        //instance camera
        mCurrentInstance = this
        binding.myCamera.setOnCreateCallback(CameraGLSurfaceView.OnCreateCallback { Log.i("~~~", "view onCreate") })
        //set font camera default
//        binding.myCamera.presetCameraForward(false);
        //Recording video size
//        binding.myCamera.presetRecordingSize(960, 1280);
        binding.myCamera.presetRecordingSize(Common.screenWidth, Common.screenWidth * 4 / 3)
        //Taking picture size.
        binding.myCamera.setPictureSize(2048, 2048, true) // > 4MP
        //        binding.myCamera.setPictureSize(720, 1280, true);
        binding.myCamera.setZOrderOnTop(false)
        binding.myCamera.setZOrderMediaOverlay(true)
    }

    /**
     * save list bitmap to myCacheFolder (task DOCS)
     */
    private fun saveImage(listMyImage: List<Bitmap>): MutableList<String> {
        val listResult: MutableList<String> = ArrayList()
        for (i in listMyImage.indices) {
            Common.saveBitmapToCache(this, listMyImage[i], "${System.currentTimeMillis()}_$i")?.run {
                listResult.add(this)
            }
        }
        return listResult
    }

}