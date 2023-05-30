package com.remi.pdfscanner.ui.results

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityShowResultBinding
import com.remi.pdfscanner.db.FileEntity
import com.remi.pdfscanner.engine.OnPdfReorderedInterface
import com.remi.pdfscanner.engine.PDFUtils
import com.remi.pdfscanner.engine.createpdf.CreatePdf
import com.remi.pdfscanner.engine.createpdf.ImageToPDFOptions
import com.remi.pdfscanner.engine.createpdf.OnPDFCreatedInterface
import com.remi.pdfscanner.repository.DbRepository
import com.remi.pdfscanner.ui.dialog.DialogRecognize
import com.remi.pdfscanner.ui.dialog.DialogTextInput
import com.remi.pdfscanner.ui.main.MainActivity
import com.remi.pdfscanner.ui.pdfdetails.DetailsPdfFragment
import com.remi.pdfscanner.ui.pdfdetails.DetailsPdfViewModel
import com.remi.pdfscanner.ui.pdfdetails.ItemPage
import com.remi.pdfscanner.ui.recognizetext.BottomSheetShare
import com.remi.pdfscanner.ui.setting.OptionSize
import com.remi.pdfscanner.util.Common
import com.remi.pdfscanner.util.FileUtil
import com.remi.pdfscanner.util.MySharePref
import com.remi.pdfscanner.util.setSize
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

@AndroidEntryPoint
class ActivityShowResult : BaseActivity<ActivityShowResultBinding>(ActivityShowResultBinding::inflate)  {


    var listImage: MutableList<String> = ArrayList()

    private val viewModel by viewModels<DetailsPdfViewModel>()

    @Inject
    lateinit var repository: DbRepository

    @Inject
    lateinit var detailsPdfFragment: DetailsPdfFragment

    lateinit var dialogLoading: DialogRecognize

    var isAddSignature = false

    var isGoHome = false
    var isShare = false
    var urlFile = ""
    var isCreated = false

    override fun setSize() {
        binding.tvNext.setSize(14)
        binding.tvView.setSize(20)
        binding.tvCount.setSize(20)
        binding.tvNameFile.setSize(16)
        binding.tvSavePdf.setSize(14)
        binding.tvSaveGallery.setSize(14)
        binding.tvShare.setSize(14)
        binding.tvNameFile.isSelected = true

        binding.layoutAsk.visibility = if (MySharePref.getRated(this)) GONE else VISIBLE
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogLoading = DialogRecognize(this).apply { text = "   Loading..." }


        binding.tvNameFile.text = "Scanner_PDF_${System.currentTimeMillis()}"
        viewModel.nameFile.postValue(binding.tvNameFile.text.toString())
        listImage = Gson().fromJson(intent.getStringExtra("list_image")!!, object : TypeToken<List<String>>() {}.type)
        Glide.with(this).load(listImage[0]).into(binding.imgPreview)
        viewModel.flagBack.observe(this) { onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.container.visibility == VISIBLE) {
                    showFragmentViewer(false)
                } else finish()
            }
        })
        onClick()
        loadFragmentToViewPDF()
    }

    private fun loadFragmentToViewPDF() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, detailsPdfFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun loadDataToFragmentViewer() {
        PDFUtils.reorderPdfPages(this, urlFile, object : OnPdfReorderedInterface {
            override fun onPdfReorderStarted() {

            }

            override fun onPdfReorderCompleted(bitmaps: MutableList<Bitmap>?) {
                viewModel.listBitmap.postValue(bitmaps?.run { mapIndexed { index, bitmap -> ItemPage(bitmap, index) } }!!.toMutableList())
                dialogLoading.dismiss()
                showFragmentViewer(true)
            }

            override fun onPdfReorderFailed() {
                dialogLoading.dismiss()
            }
        })
    }

    private fun showFragmentViewer(isShow: Boolean) {
        if (isShow) {
            TransitionManager.beginDelayedTransition(binding.root, Slide(Gravity.BOTTOM))
            binding.container.visibility = VISIBLE
        } else {
            TransitionManager.beginDelayedTransition(binding.root, Slide(Gravity.BOTTOM))
            binding.container.visibility = GONE
        }
    }

    private fun onClick() {
        binding.btnClose.setOnClickListener {
            binding.layoutAsk.visibility = GONE
        }
        binding.btnLike.setOnClickListener {
            binding.layoutAsk.visibility = GONE
        }
        binding.btnDislike.setOnClickListener {
            binding.layoutAsk.visibility = GONE
        }

        binding.tvNext.setOnClickListener {
            if (isCreated) {
                goHome()
            } else {
                isGoHome = true
                dialogLoading.show()
                save(binding.tvNameFile.text.toString())
            }

        }
        binding.imgEditName.setOnClickListener {
            DialogTextInput(this, back = {
                this@ActivityShowResult.binding.tvNameFile.text = it
                viewModel.nameFile.postValue(it)
                binding.tvNameFile.isSelected = true
                if (isCreated) {
                    currentItem?.let { item ->
                        if (FileUtil.renameFile(item.filePathParent, item.fileName, it)) {
                            Log.e("~~~", "onClick: $it")
                            item.fileName = it
                            repository.updateFile(item)

                        }
                    }

                }


            }).apply {
                textInput = this@ActivityShowResult.binding.tvNameFile.text.toString()
                show()
            }
        }
        binding.tvCount.text = listImage.size.toString()
        binding.btnSavePdf.setOnClickListener {
            if (isCreated) {
                Toast.makeText(this, "Saved in: $urlFile", Toast.LENGTH_SHORT).show()
            } else {
                save(binding.tvNameFile.text.toString())
            }
        }
        binding.tvView.setOnClickListener {
            if (isCreated) {
                showFragmentViewer(true)
            } else {
                dialogLoading.show()
                save(binding.tvNameFile.text.toString())
            }
        }
        var isSave = false
        val pathOut = FileUtil.getMyDocumentFolder(this@ActivityShowResult) //path to folder document
        binding.btnSaveGallery.setOnClickListener {
            if (isSave) {
                Toast.makeText(this@ActivityShowResult, "Saved in $pathOut", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            File(pathOut).mkdirs()
            for (i in listImage.indices) {
                FileUtil.copyFile(FileUtil.getFolderCacheImage(this), listImage[i].split("/").last(), pathOut)
            }
            isSave = true
            Toast.makeText(this@ActivityShowResult, "Saved in $pathOut", Toast.LENGTH_SHORT).show()
        }

        binding.btnShare.setOnClickListener {
            if (isCreated) {
                shareFile()
            } else {
                isShare = true
                dialogLoading.show()
                save(binding.tvNameFile.text.toString())
            }
        }
    }

    private fun shareFile() {
        BottomSheetShare(callback = {

        }).apply {
            isShareText = false
            listData = getListAppCanShare()
            textToShare = outPath
            show(supportFragmentManager, "share_file")
        }
    }

    var listPair: MutableList<BottomSheetShare.PkgData>? = null

    private fun getListAppCanShare(): MutableList<BottomSheetShare.PkgData> {
        if (listPair == null) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "*/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            listPair = ArrayList()
            val activities: List<ResolveInfo> = packageManager.queryIntentActivities(sendIntent, 0)
            for (info in activities) {
                listPair!!.add(BottomSheetShare.PkgData(info.activityInfo.packageName, info.loadLabel(packageManager).toString(), info.loadIcon(packageManager)))
            }
        }
        return listPair ?: ArrayList()
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }

    var outPath = ""
    private fun save(filename1: String) {

        val pathParent = FileUtil.getFolderPDF(this)

        val nameIn = if (File("$pathParent$filename1.pdf").exists()) filename1 + System.currentTimeMillis() else filename1

        val mPdfOptions = ImageToPDFOptions()
        isAddSignature = intent.getBooleanExtra("edit_from_file", false)
        if (isAddSignature)
            mPdfOptions.setMargins(0, 0, 0, 0)
        else
            mPdfOptions.setMargins(10, 10, 10, 10)
        val mPageColor = Color.WHITE
        mPdfOptions.imageScaleType = "maintain_aspect_ratio"
//        mPdfOptions.pageNumStyle = "pg_num_style_x_of_n"
        mPdfOptions.pageNumStyle = null
        mPdfOptions.masterPwd = getString(R.string.app_name)
        mPdfOptions.pageColor = mPageColor
        mPdfOptions.outFileName = nameIn
        mPdfOptions.borderWidth = 0
        mPdfOptions.qualityString = "50"
        when (MySharePref.getOptionSize(this)) {
            OptionSize.A3.name -> mPdfOptions.pageSize = "A3"
            OptionSize.A5.name -> mPdfOptions.pageSize = "A5"
            OptionSize.B4.name -> mPdfOptions.pageSize = "B4"
            OptionSize.B5.name -> mPdfOptions.pageSize = "B5"
            else -> mPdfOptions.pageSize = "A4"
        }

        mPdfOptions.isPasswordProtected = false
        mPdfOptions.isWatermarkAdded = false
        mPdfOptions.imagesUri = ArrayList(listImage)
        outPath = "$pathParent$nameIn.pdf"
        val previewPath = FileUtil.copyFile(FileUtil.getFolderCacheImage(this), listImage[0].split("/").last(), FileUtil.getFolderPreviewImage(this))

        CreatePdf(mPdfOptions, pathParent, object : OnPDFCreatedInterface {
            override fun onPDFCreated(success: Boolean, path: String?) {
                if (success) {
                    setResult(RESULT_OK, Intent("create"))
                    Toast.makeText(this@ActivityShowResult, "Created!", Toast.LENGTH_SHORT).show()
                    isCreated = true
                    urlFile = path ?: ""
                    MediaScannerConnection.scanFile(this@ActivityShowResult,
                        arrayOf(File(pathParent).toString(), FileUtil.getStore(this@ActivityShowResult) + File.separator + "PDF_Created"),
                        null,
                        null)
                    if (!isGoHome)
                        loadDataToFragmentViewer()
                    saveFileToRoomDatabase(nameIn, pathParent, previewPath, "", listImage.size, System.currentTimeMillis())
                } else {
                    if (dialogLoading.isShowing) dialogLoading.dismiss()
                    Toast.makeText(this@ActivityShowResult, "Something went wrong!", Toast.LENGTH_SHORT).show()
                }
                if (isGoHome) {
                    goHome()
                }
                if (isShare) {
                    shareFile()
                }

            }

            override fun onPDFCreationStarted() {
                Log.e("~~~", "onPDFCreationStarted: ")
            }
        }).execute()
    }

    var currentItem: FileEntity? = null
    private fun saveFileToRoomDatabase(nameFile: String, filePath: String, previewPath: String, tag: String, pageSize: Int, timeCreated: Long) {
//        val listId = repository.getAllFiles().map { it.fileId }
//        var randomId = Random.nextInt()
//        while (listId.contains(randomId)) randomId = Random.nextInt()
        currentItem = FileEntity(
//            fileId = randomId,
            fileName = nameFile,
            filePathParent = filePath,
            imagePreviewPath = previewPath,
            fileTag = tag,
            filePageSize = pageSize,
            fileCreatedTime = timeCreated)

        launch {
            currentItem!!.fileId = repository.saveFile1(currentItem!!).toInt()
            withContext(Dispatchers.IO,{

            })
        }




    }
}