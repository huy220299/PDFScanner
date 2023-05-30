package com.remi.pdfscanner.ui.pdfdetails

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity.BOTTOM
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.base.IYesNoCallback
import com.remi.pdfscanner.databinding.ActivityDetailsPdfBinding
import com.remi.pdfscanner.db.FileEntity
import com.remi.pdfscanner.engine.OnPdfReorderedInterface
import com.remi.pdfscanner.engine.PDFUtils
import com.remi.pdfscanner.repository.DbRepository
import com.remi.pdfscanner.ui.dialog.DialogCustom
import com.remi.pdfscanner.ui.dialog.DialogRecognize
import com.remi.pdfscanner.ui.editIamge.ActivityEditImage
import com.remi.pdfscanner.ui.pickphoto.ShowImageActivity
import com.remi.pdfscanner.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActivityDetailsPDF : BaseActivity<ActivityDetailsPdfBinding>(ActivityDetailsPdfBinding::inflate) {


    private val viewModel by viewModels<DetailsPdfViewModel>()

    @Inject
    lateinit var repository: DbRepository

    @Inject
    lateinit var detailsPdfFragment: DetailsPdfFragment

    @Inject
    lateinit var detailsPdfAdapter: DetailsPdfAdapter

    val listAddedImage: MutableList<String> = ArrayList()
    private var launcherPickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.run {
                val url = this.getStringExtra("image_picked") ?: ""
                Glide.with(this@ActivityDetailsPDF).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        isAdded = true
                        listAddedImage.add(url)
                        with(binding.recyclerview.adapter as DetailsPdfAdapter) {
                            listItem.add(this.itemCount - 1, ItemPage(resource, this.itemCount - 1, false))
                            for (i in 0..listItem.size - 2) {
                                listItem[i].pageNumberSource = i
                            }
                            notifyItemInserted(this.itemCount)
                        }
                    }


                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })

//                mPath?.run {
//                    PDFUtils.addImagesToPdf(this@ActivityDetailsPDF, this, listOf(url), callback = { outNameFile ->
//                        Glide.with(this@ActivityDetailsPDF).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
//                            override fun onLoadCleared(placeholder: Drawable?) {
//
//                            }
//                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                                with(binding.recyclerview.adapter as DetailsPdfAdapter) {
//                                    listItem.add(this.itemCount - 1, ItemPage(resource, this.itemCount - 1, false))
//                                    for (i in 0..listItem.size-2){
//                                        listItem[i].pageNumberSource = i
//                                    }
//                                    notifyItemInserted(this.itemCount)
//
//
//                                    //add new item to room database
//                                    if (currentItemFileEntity == null) return
//
//                                    currentItemFileEntity = FileEntity(
//                                        fileName = outNameFile,
//                                        filePathParent = currentItemFileEntity!!.filePathParent,
//                                        imagePreviewPath = currentItemFileEntity!!.imagePreviewPath,
//                                        fileTag = "#add",
//                                        filePageSize = this@with.itemCount - 1,
//                                        fileCreatedTime = System.currentTimeMillis())
//                                    repository.saveFile(currentItemFileEntity!!)
//                                    mPath = "${currentItemFileEntity!!.filePathParent}${currentItemFileEntity!!.fileName}.pdf"
//                                    setResult(RESULT_OK)
//
//
//                                }
//                            }
//                        })
//                    })
//                }

            }
        }
    }

    var mPath: String? = null
    var isAdded = false
    var isRemoveFile = false

    override fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvTitle.isSelected = true
    }

    var currentItemFileEntity: FileEntity? = null

    /**
     * if open file from device, save it to room
     */
    private fun saveFileToRoom(bitmaps: MutableList<Bitmap>) {
        if (bitmaps.isEmpty()) return

        val preview = Common.saveImageToUserData(this, bitmaps[0], FileUtil.getFolderPreviewImage(this), "preview_${System.currentTimeMillis()}")
        currentItemFileEntity = FileEntity(
            fileName = intent.getStringExtra("name_file") ?: "sample",
            filePathParent = FileUtil.getFolderPDF(this),
            imagePreviewPath = preview ?: "",
            fileTag = "#import",
            filePageSize = bitmaps.size,
            fileCreatedTime = System.currentTimeMillis())
        currentItemFileEntity?.run { repository.saveFile(this) }
        setResult(RESULT_OK)//reload list file in mainActivity when back
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dialog = DialogRecognize(this).apply {
            text = "Loading"
            show()
        }
        var uri: Uri? = null
        mPath = intent.getStringExtra("path")
        currentItemFileEntity = Gson().fromJson(intent.getStringExtra("item"), FileEntity::class.java)
        currentItemFileEntity?.run {
            mPath = "${this.filePathParent}${this.fileName}.pdf"
        }
        mPath?.run {
            val text = this.split("/").last().replace(".pdf", "")
            binding.tvTitle.text = text
            viewModel.nameFile = text
        }

        intent.getStringExtra("uri")?.run { uri = Uri.parse(this) }
        PDFUtils.reorderPdfPages(this, uri, mPath, object : OnPdfReorderedInterface {
            override fun onPdfReorderStarted() {

            }

            override fun onPdfReorderCompleted(bitmaps: MutableList<Bitmap>?) {
                dialog.dismiss()
                viewModel.listBitmap.postValue(bitmaps?.run { mapIndexed { index, bitmap -> ItemPage(bitmap, index) } }!!.toMutableList())
                setupRecyclerView(bitmaps.run { mapIndexed { index, bitmap -> ItemPage(bitmap, index) } }.toMutableList())
                if (intent.getBooleanExtra("is_save_success", false)) {
                    saveFileToRoom(bitmaps ?: ArrayList())
                }
            }

            override fun onPdfReorderFailed() {
                dialog.dismiss()
            }
        })

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, detailsPdfFragment)
        transaction.addToBackStack(null)
        transaction.commit()
        viewModel.flagBack.observe(this) { onBackPressedDispatcher.onBackPressed() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.container.visibility == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(binding.root, Slide(BOTTOM))
                    binding.container.hide()
                } else if (detailsPdfAdapter.isSelectMode) {
                    binding.layoutBottom.hide()
                    detailsPdfAdapter.isSelectMode = false
                    binding.imgSelectAll.setImageResource(R.drawable.ic_option2)
                    detailsPdfAdapter.listItem.forEach { it.isSelect = false }
                } else if (isRemoveFile && !isAdded) { //only remove
                    currentItemFileEntity?.run {
                        val list = detailsPdfAdapter.listItem.map { it.pageNumberSource }.dropLast(1)
                        val preview = Common.saveImageToUserData(
                            this@ActivityDetailsPDF,
                            detailsPdfAdapter.listItem[0].bitmap,
                            FileUtil.getFolderPreviewImage(this@ActivityDetailsPDF),
                            "preview_${System.currentTimeMillis()}")
                        if (list.isEmpty()) return
                        val pageChange = list.joinToString(separator = ",")
                        Log.e("~~~", "handleOnBackPressed: $pageChange")
                        val pathIn = this.filePathParent + "${this.fileName}.pdf"
                        val pathOut = this.filePathParent + "${this.fileName}_$pageChange.pdf"
                        if (PDFUtils.reorderRemovePDF(this@ActivityDetailsPDF, pathIn, pathOut, pageChange)) {
//                            saveFileToRoom()
                            repository.saveFile(FileEntity(
                                fileName = "${this.fileName}_$pageChange",
                                filePathParent = this.filePathParent,
                                imagePreviewPath = preview ?: this.imagePreviewPath,
                                fileTag = "#Removed",
                                filePageSize = list.size,
                                fileCreatedTime = System.currentTimeMillis()))
                            setResult(RESULT_OK)
                        }
                    }
                    finish()
                } else if (isRemoveFile) {//remove + add
                    finish()
                } else if (isAdded) { //only add
                    if (mPath == null) finish()
                    mPath?.run {
                        PDFUtils.addImagesToPdf(this@ActivityDetailsPDF, this, listAddedImage, callback = { outNameFile ->
                            Glide.with(this@ActivityDetailsPDF).asBitmap().load(listAddedImage[0]).into(object : CustomTarget<Bitmap>() {
                                override fun onLoadCleared(placeholder: Drawable?) {
                                }

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    //add new item to room database
                                    if (currentItemFileEntity == null) finish()
                                    currentItemFileEntity = FileEntity(
                                        fileName = outNameFile,
                                        filePathParent = currentItemFileEntity!!.filePathParent,
                                        imagePreviewPath = currentItemFileEntity!!.imagePreviewPath,
                                        fileTag = "#add",
                                        filePageSize = currentItemFileEntity!!.filePageSize + listAddedImage.size,
                                        fileCreatedTime = System.currentTimeMillis())
                                    repository.saveFile(currentItemFileEntity!!)
                                    setResult(RESULT_OK)
                                    finish()
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    finish()
                                }
                            })
                        })
                    }
                } else finish()
            }
        })
        onClick()

    }

    private fun onClick() {
        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.imgShowPdf.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root, Slide(BOTTOM))
            binding.container.visibility = View.VISIBLE
        }

        binding.imgEditAll.setOnClickListener {
            DialogCustom(this).apply {
                titleValue = "Edit all image"
                descriptionValue = "This action will change all page\nof the file"
                callback = object : IYesNoCallback {
                    override fun onYes() {
                        loadingDialog.apply {
                            text = "   Loading..."
                            show()
                        }
                        val listImage :MutableList<String> = ArrayList()
                        val listItem = detailsPdfAdapter.listItem.dropLast(1)
                        for (i in listItem.indices){
                            Common.saveBitmapToCache(this@ActivityDetailsPDF, listItem[i].bitmap,"${System.currentTimeMillis()}_$i")?.run {
                                listImage.add(this)
                            }
                        }
                        loadingDialog.dismiss()
                        if (listImage.isNotEmpty()){
                            startActivity(Intent(this@ActivityDetailsPDF,ActivityEditImage::class.java).apply {
                                putExtra("list_image",Gson().toJson(listImage))
                                putExtra("edit_from_file",true)
                            })
                            finish()
                        }else{
                            Toast.makeText(this@ActivityDetailsPDF, "Something went wrong!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                show()
            }
        }
        binding.imgSelectAll.setOnClickListener {
            if (detailsPdfAdapter.isSelectMode) {
                binding.layoutBottom.hide()
                detailsPdfAdapter.isSelectMode = false
                binding.imgSelectAll.setImageResource(R.drawable.ic_option2)
                detailsPdfAdapter.listItem.forEach { it.isSelect = false }
            } else {
                binding.layoutBottom.show()
                detailsPdfAdapter.isSelectMode = true
                binding.imgSelectAll.setImageResource(R.drawable.ic_option2_selected)
            }

            detailsPdfAdapter.notifyItemRangeChanged(0, detailsPdfAdapter.itemCount, true)
        }
        binding.layoutBottom.setOnClickListener { }

        binding.btnShareMultiFile.setOnClickListener {
            val list = detailsPdfAdapter.listItem.filter { it.isSelect }
            if (list.isNotEmpty())
                convertUrlToUri(list.map { it.bitmap })
        }
        var isSaved = false
        binding.btnSaveGallery.setOnClickListener {
            if (isSaved) return@setOnClickListener
            val list = detailsPdfAdapter.listItem.filter { it.isSelect }
            for (i in list.indices) {
                Common.saveImageToPicture(this, list[i].bitmap, "${i}_${System.currentTimeMillis()}")
            }
            isSaved = true
            Toast.makeText(this, "Saved in Picture!", Toast.LENGTH_SHORT).show()
        }

        binding.btnDeleteMultiFile.setOnClickListener {
            DialogCustom(this).apply {
                callback = object : IYesNoCallback {
                    override fun onYes() {
                        val list = detailsPdfAdapter.listItem.filter { it.isSelect }.map { it.pageNumberSource }
                        for (i in detailsPdfAdapter.listItem.size - 2 downTo 0) {//last item is to add, so -2
                            if (list.contains(detailsPdfAdapter.listItem[i].pageNumberSource)) {
                                detailsPdfAdapter.listItem.removeAt(i)
                                detailsPdfAdapter.notifyItemRemoved(i)
                            }
                        }
                        isRemoveFile = true
                    }
                }
                show()
            }
        }
    }

    private fun setupRecyclerView(itemPages: MutableList<ItemPage>) {
        itemPages.forEachIndexed { index, itemPage -> itemPage.pageNumberSource = index }
        itemPages.add(itemPages.last())//add last item
        binding.recyclerview.apply {
            detailsPdfAdapter.callback = object : IDetailsFile {
                override fun onSelect(itemPage: ItemPage) {
                    launcherPickImage.launch(Intent(this@ActivityDetailsPDF, ShowImageActivity::class.java)
                        .apply {
                            putExtra("task_pick", true)
                        })
                }

            }
            detailsPdfAdapter.listItem = itemPages
            adapter = detailsPdfAdapter
//            adapter = DetailsPdfAdapter(callback = {
//                launcherPickImage.launch(Intent(this@ActivityDetailsPDF, ShowImageActivity::class.java)
//                    .apply {
//                        putExtra("task_pick", true)
//                    })
//            }).apply {
//                listItem = itemPages
//            }
            layoutManager = GridLayoutManager(this@ActivityDetailsPDF, 2, GridLayoutManager.VERTICAL, false)
        }
    }

    var listOfUris = ArrayList<Uri>()
    private fun convertUrlToUri(filesPath: List<Bitmap>) {

        listOfUris.clear()
        for (i in filesPath.indices)
            listOfUris.add(Common.saveImageToShare(this, filesPath[i], "img_$i.png") ?: Uri.EMPTY)
        shareImages(null)
    }

    private fun shareImages(comment: String?) {
        if (listOfUris.isNotEmpty()) {
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, listOfUris)
                comment?.let { putExtra(Intent.EXTRA_TEXT, it) }

            }
            try {
                startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.share)))
                listOfUris.clear()
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No App Available", Toast.LENGTH_SHORT).show()
            }
        }

    }
}