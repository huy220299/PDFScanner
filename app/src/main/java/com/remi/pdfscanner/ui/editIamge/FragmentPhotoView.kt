package com.remi.pdfscanner.ui.editIamge

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoView
import com.remi.pdfscanner.util.Common
import com.remi.pdfscanner.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import org.wysaid.nativePort.CGENativeLibrary
import javax.inject.Inject


@AndroidEntryPoint
class FragmentPhotoView() : Fragment() {
    var url: String = ""
    var pos = 0
    private val SOME_VALUE_KEY = "someValueToSave"
    private val SOME_VALUE_KEY2 = "someValueToSave2"
    private lateinit var cropImageView: PhotoView
    private lateinit var recyclerviewFiler: RecyclerView
    private var currentContrast = 50
    private var currentBrightness = 50
    private var currentDetails = 50

    @Inject
    lateinit var filterAdapter: FilterAdapter

    private val viewModel: EditImageActivityViewModel by activityViewModels()

    private var srcBitmap: Bitmap? = null
    private var currentBitmap: Bitmap? = null

    @SuppressLint("ResourceType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        cropImageView = PhotoView(requireContext()).apply {
            id = 111
            setBackgroundColor(Color.WHITE)
        }

        recyclerviewFiler = RecyclerView(requireContext()).apply { id = 222 }
        val margin = resources.getDimension(com.intuit.sdp.R.dimen._10sdp).toInt()
        val constraintLayout = ConstraintLayout(requireContext())
        constraintLayout.addView(cropImageView, 0, 0)
        constraintLayout.addView(recyclerviewFiler, 0, 0)
//        constraintLayout.addView(recyclerviewFiler, 0, Common.screenWidth/5)
        ConstraintSet().apply {
            clone(constraintLayout)
            connect(cropImageView.id, TOP, PARENT_ID, TOP, margin)
            connect(cropImageView.id, START, PARENT_ID, START, margin)
            connect(cropImageView.id, END, PARENT_ID, END, margin)
            connect(cropImageView.id, BOTTOM, recyclerviewFiler.id, TOP, margin)

            connect(recyclerviewFiler.id, BOTTOM, PARENT_ID, BOTTOM)
            connect(recyclerviewFiler.id, START, PARENT_ID, START)
            connect(recyclerviewFiler.id, END, PARENT_ID, END)
            applyTo(constraintLayout)
        }
        recyclerviewFiler.setPadding(margin, 0, margin, 0)
        recyclerviewFiler.clipToPadding = false
        return constraintLayout
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SOME_VALUE_KEY, url)
        outState.putInt(SOME_VALUE_KEY2, pos)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            url = savedInstanceState.getString(SOME_VALUE_KEY, "")
            pos = savedInstanceState.getInt(SOME_VALUE_KEY2, 0)
        }
        recyclerviewFiler.setBackgroundColor(Color.RED)
        Glide.with(requireContext()).asBitmap().load(url)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    val bitmap = Bitmap.createScaledBitmap(resource, 1080, resource.height * 1080 / resource.width, false)
                    currentBitmap = bitmap
                    srcBitmap = bitmap
                    cropImageView.setImageBitmap(bitmap)
//                    setupRecyclerviewFilter(bitmap)
                }
            })

        observe()

    }

    /**
     * Add Signature and anti counterfeit
     */
    private fun handleBitmapToSave(bitmapIn:Bitmap):Bitmap{
        val bitmapResult = Bitmap.createBitmap(bitmapIn.width,bitmapIn.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapResult)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmapIn,0f,0f,paint)
        viewModel.bitmapSignature?.run {
            canvas.drawBitmap(this,0f,0f,paint)
        }
        viewModel.bitmapAntiCounterfeit?.run {
            canvas.drawBitmap(this,0f,0f,paint)
        }
        return bitmapResult
    }

    private fun observe() {
        //start save image to cache -> go to edit activity
        viewModel.flagStartSave.observe(viewLifecycleOwner) {
            viewModel.listImageSaved.value!!.add(
                Common.saveImageToUserData(
                    requireContext(),
                    handleBitmapToSave(currentBitmap!!),
                    FileUtil.getFolderCacheImage(requireContext()),
                    "edited_${System.currentTimeMillis()}_$pos"))
            viewModel.flagEndSave.postValue(true)
        }
        //set value to view model when swipe item
        viewModel.currentItemPosition.observe(viewLifecycleOwner) {
            if (it == pos) {
                viewModel.currentBrightness = currentBrightness
                viewModel.currentContrast = currentContrast
                viewModel.currentDetails = currentDetails
                viewModel.flagUpdateFilter.postValue(true)
                srcBitmap?.run {
                    val ruleString = ConvertConfig.getConfigMulti(currentBrightness, currentContrast, currentDetails)
                    currentBitmap = CGENativeLibrary.filterImage_MultipleEffects(this, ruleString, 1.0f)
                    cropImageView.setImageBitmap(currentBitmap)
                }
            }
        }
        viewModel.flagChangeCurrent.observe(viewLifecycleOwner) {
            if (viewModel.currentItemPosition.value == pos || viewModel.isApplyForAll ) {
                currentBrightness = viewModel.currentBrightness
                currentContrast = viewModel.currentContrast
                currentDetails = viewModel.currentDetails

            }
        }
        //update bitmap by current config
        viewModel.flagChangeTemp.observe(viewLifecycleOwner) {
            changeByTempConfig()
        }
    }

    /**
     * check if current fragment or flagApplyAll to change bitmap by filter
     */
    private fun changeByTempConfig() {
        if (pos == viewModel.currentItemPosition.value || viewModel.isApplyForAll) {
            Log.e("~~~", "changeByTempConfig: $pos", )
            srcBitmap?.run {
                val ruleString = ConvertConfig.getConfigMulti(viewModel.tempBrightness, viewModel.tempContrast, viewModel.tempDetails)
                currentBitmap = CGENativeLibrary.filterImage_MultipleEffects(this, ruleString, 1.0f)
                cropImageView.setImageBitmap(currentBitmap)
            }
        }

    }

    private fun setupRecyclerviewFilter(bitmap: Bitmap) {
        FilterWorker.getListFilteredBitmap(requireContext(), bitmap, object : FilterWorker.FilterPreviewBitmapCallback {
            override fun onSuccess(list: List<FilterWorker.FilterData>) {

            }
        })
    }

}