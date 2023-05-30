package com.remi.pdfscanner.ui.crop

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.MyBitmap
import com.remi.pdfscanner.util.Common
import com.remi.pdfscanner.util.FileUtil
import me.pqpo.smartcropperlib.view.CropImageView
import me.pqpo.smartcropperlib.view.IUndoCropImageView

class FragmentCropImage(var positionImage: Int) : Fragment() {

    private val SOME_VALUE_KEY = "someValueToSave"
    private var myBitmap: MyBitmap? = null
//    private val model2 by viewModels<CropImageActivityViewModel>()
//    private val model: CropImageActivityViewModel by viewModels({ requireParentFragment() })


    private lateinit var viewModel: CropImageActivityViewModel
    private lateinit var cropImageView: CropImageView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        cropImageView = CropImageView(requireContext())
        return cropImageView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SOME_VALUE_KEY, positionImage)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[CropImageActivityViewModel::class.java]

        if (savedInstanceState != null) {
            positionImage = savedInstanceState.getInt(SOME_VALUE_KEY)
        }

        Glide.with(requireContext()).asBitmap().load(viewModel.listUrlImage[positionImage])
            .into(object : CustomTarget<Bitmap?>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    if (positionImage == 0)
                        viewModel.isLoadingFirstImage.postValue(true)
                    myBitmap = MyBitmap(Bitmap.createScaledBitmap(resource, 1080, resource.height * 1080 / resource.width, false), 0, mutableListOf(null, null, null))
                    myBitmap?.run { cropImageView.setImageToCrop(this.bitmapDefault) }
                }
            })

        observable()


        cropImageView.callbackUpdateUI = IUndoCropImageView {
            viewModel.updateVisibilityUndo(cropImageView.canPrevious(), cropImageView.canNext())
        }
    }


    private fun observable() {
        //start save image to cache -> go to edit activity
        viewModel.flagStartSave.observe(viewLifecycleOwner) {
            viewModel.listImageSaved.value!!.add(
                Common.saveImageToUserData(
                    requireContext(),
                    cropImageView.crop(),
                    FileUtil.getFolderCacheImage(requireContext()),
                    "cache${System.currentTimeMillis()}_$positionImage"))
            viewModel.flagEndSave.postValue(true)
        }
        //rotate current image: listen from ActivityCrop
        viewModel.flagRotateImage.observe(viewLifecycleOwner) {
            if (viewModel.positionCurrentImage == positionImage) {
                myBitmap?.run { cropImageView.setImageToCrop(this.getCurrentBitmap(viewModel.isRotateImageRight)) }
            }
        }

        viewModel.flagAutoScan.observe(viewLifecycleOwner) { isAutoScan ->
            cropImageView.cropPoints = if (isAutoScan) cropImageView.currentScan else null
            cropImageView.invalidate()
        }

        viewModel.flagUndoRedo.observe(viewLifecycleOwner) { isRedo ->  //true->redo false->Undo
            if (viewModel.positionCurrentImage == positionImage)
                if (isRedo && cropImageView.canNext()) {
                    cropImageView.actionNext()
                    viewModel.updateVisibilityUndo(cropImageView.canPrevious(), cropImageView.canNext())
                } else if (!isRedo && cropImageView.canPrevious()) {
                    cropImageView.actionPrevious()
                    viewModel.updateVisibilityUndo(cropImageView.canPrevious(), cropImageView.canNext())
                }
        }

        viewModel.flagGetUIInformation.observe(viewLifecycleOwner){
            if (viewModel.positionCurrentImage==positionImage)
                viewModel.updateVisibilityUndo(cropImageView.canPrevious(),cropImageView.canNext())
        }
        viewModel.flagReloadImage.observe(viewLifecycleOwner){
            Glide.with(requireContext()).asBitmap().load(viewModel.listUrlImage[positionImage])
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        myBitmap = MyBitmap(Bitmap.createScaledBitmap(resource, 1080, resource.height * 1080 / resource.width, false), 0, mutableListOf(null, null, null))
                        myBitmap?.run { cropImageView.setImageToCrop(this.bitmapDefault) }
                    }
                })
        }
    }

}