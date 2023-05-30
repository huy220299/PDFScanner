package com.remi.pdfscanner.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.gson.Gson
import com.remi.pdfscanner.MyApplication
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.base.IYesNoCallback
import com.remi.pdfscanner.databinding.ActivityMainBinding
import com.remi.pdfscanner.db.FileEntity
import com.remi.pdfscanner.repository.DbRepository
import com.remi.pdfscanner.ui.dialog.DialogCustom
import com.remi.pdfscanner.ui.dialog.DialogRequestPermission
import com.remi.pdfscanner.ui.dialog.DialogTextInput
import com.remi.pdfscanner.ui.pdfdetails.ActivityDetailsPDF
import com.remi.pdfscanner.ui.pickphoto.ShowImageActivity
import com.remi.pdfscanner.ui.premium.PremiumActivity
import com.remi.pdfscanner.ui.recognizetext.BottomSheetShare
import com.remi.pdfscanner.ui.setting.SettingActivity
import com.remi.pdfscanner.ui.takephoto.ActivityTakePhoto
import com.remi.pdfscanner.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("NotifyDataSetChanged,SetTextI18n,Range")
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    @Inject
    lateinit var repository: DbRepository

    @Inject
    lateinit var fileAdapter: FileAdapter

    /**
     * reload item after import file
     */
    private var launcherShowFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK)
            checkItem()
    }
    private var launcherPickFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri: Uri = result.data!!.data!!
            val uriString: String = uri.toString()

            var pdfName = "sample"
            if (uriString.startsWith("content://")) {
                var myCursor: Cursor? = null
                try {
                    // Setting the PDF to the TextView
                    myCursor = applicationContext!!.contentResolver.query(uri, null, null, null, null)
                    if (myCursor != null && myCursor.moveToFirst()) {
                        pdfName = myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    myCursor?.close()
                }
            }

            val pathParent = FileUtil.getFolderPDF(this)
            var isSaveSuccess = false
            //check if file is not saved then save file
            if (!repository.getAllFiles().any { it.filePathParent.equals(pathParent, true) && it.fileName.equals(pdfName.replace(".pdf", ""), true) })
                isSaveSuccess = FileUtil.savefile(this, uri, pathParent, pdfName)

            launcherShowFile.launch(Intent(this, ActivityDetailsPDF::class.java).apply {
                putExtra("uri", uriString)
                putExtra("name_file", pdfName.replace(".pdf", ""))
                putExtra("is_save_success", isSaveSuccess)
            })


        }
    }

    override fun setSize() {
        binding.tvAppName.setSize(24)
        binding.tvCountSelected.setSize(20)
        binding.tvSelectAll.setSize(16)

        binding.imgTapToScan.setBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.img_tap_to_scan),
            Common.screenWidth * 3 / 10,
            (Common.screenWidth * 3 / 10) * 54 / 120,
            true))
        binding.tvNoFileYet.setSize(18)
        binding.tvNoFileYetDes.setSize(14)
    }

    private var listPair: MutableList<BottomSheetShare.PkgData>? = null
    var isSelectMode = false

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileAdapter.apply {
            callback = object : IFileCallback {
                override fun onSelectAll(items: List<FileEntity>) {
                    binding.tvCountSelected.text = "${items.size} Selected"

                }

                override fun onSelect(item: FileEntity) {
                    launcherShowFile.launch(Intent(this@MainActivity, ActivityDetailsPDF::class.java).apply {
                        putExtra("item", Gson().toJson(item))

                    })
                }

                override fun onEdit(item: FileEntity) {
                    DialogTextInput(this@MainActivity, back = { newName ->
                        if (FileUtil.renameFile(item.filePathParent, item.fileName, newName)) {
                            FileUtil.deleteFile(item.imagePreviewPath)
                            repository.updateFile(
                                item.apply {
                                    fileName = newName
                                })
                            submitList(repository.getAllFiles())


                        }

                    }).apply {
                        textInput = item.fileName
                        textTitle = "Rename"
                        show()
                    }
                }

                override fun onDuplicate(item: FileEntity) {
                    repository.duplicateFile(item)
                    submitList(repository.getAllFiles())
                    Toast.makeText(this@MainActivity, "Duplicated!", Toast.LENGTH_SHORT).show()
                }

                override fun onDelete(item: FileEntity) {
                    DialogCustom(this@MainActivity).apply {
                        idImage = R.drawable.img_delete
                        titleValue = "Are you sure you want to delete?"
                        descriptionValue = "Once deleted, these photos cannot be recovered."
                        yesValue = "Delete"
                        cancelValue = "Cancel"
                        callback = object : IYesNoCallback {
                            override fun onYes() {
                                FileUtil.deleteFile("${item.filePathParent}/${item.fileName}.pdf")
                                repository.deleteFile(item)
                                val list = repository.getAllFiles()
                                submitList(list)
                                if (list.isEmpty()){
                                    this@MainActivity.binding.imgMenu.hide()
                                    this@MainActivity.binding.layoutNoFile.show()
                                }
                            }
                        }
                        show()
                    }
                }

                override fun onShare(item: FileEntity) {
                    BottomSheetShare(callback = {

                    }).apply {
                        isShareText = false
                        listData = getListAppCanShare()
                        textToShare = item.filePathParent + "/${item.fileName}.pdf"
                        show(supportFragmentManager, "share_file")
                    }
                }
            }
        }

        onClick()
        if (checkNeedToRequestPermission())
            showDialogPermission()


        FileUtil.deleteMyCacheFolder(this)
        checkItem()

    }

    private fun checkNeedToRequestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                return true
        } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
            return true
        return false
    }

    private fun showDialogPermission() {
        DialogRequestPermission(this).apply {
            callback = object : IYesNoCallback {
                override fun onYes() {
                    Common.askPermission(this@MainActivity, object : Common.AskPermissionCallback {
                        override fun onGranted() {
                            MyApplication.galleryModelList = Common.getAllFolder(this@MainActivity)
                        }
                    })
                }
            }
        }.show()

    }

    /**
     * fake data to test
     */
//    private fun fakeData() {
//        repository.saveFile(FileEntity(1, "FakeFile1", "", "", "#OCR", 5, 100L, 330L))
//        repository.saveFile(FileEntity(2, "FakeFile2", "", "", "#", 4, 120L, 430L))
//        repository.saveFile(FileEntity(3, "FakeFile3", "", "", "#OCR", 5, 110L, 830L))
//        repository.saveFile(FileEntity(4, "FakeFile4", "", "", "#", 7, 190L, 530L))
//        repository.saveFile(FileEntity(5, "FakeFile5", "", "", "#OCR", 5, 110L, 230L))
//    }

    private fun checkItem() {
        binding.apply {
            if (repository.getAllFiles().isNotEmpty()) {
                recyclerview.visibility = View.VISIBLE
                layoutNoFile.visibility = View.GONE
                imgMenu.show()
                fileAdapter.submitList(repository.getAllFiles())
                setupRecyclerView()
            } else {
                recyclerview.visibility = View.GONE
                layoutNoFile.visibility = View.VISIBLE
                imgMenu.hide()


            }
        }
    }

    private fun selectPdf() {
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        launcherPickFile.launch(pdfIntent)
    }

    private fun setupRecyclerView() {
        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileAdapter
//            itemAnimator = null;
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onClick() {
//        binding.viewClick1.setOnTouchListener { _, _ ->
//            binding.btnFile.setColorFilter(Color.parseColor("#5979FF"))
//            binding.btnSetting.setColorFilter(Color.parseColor("#C8C9D0"))
//            false
//        }
//
//        binding.viewClick2.setOnTouchListener { _, _ ->
//            binding.btnSetting.setColorFilter(Color.parseColor("#5979FF"))
//            binding.btnFile.setColorFilter(Color.parseColor("#C8C9D0"))
//            false
//        }

        binding.btnAdd.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.parent)
            binding.layoutHomeClick.visibility = View.VISIBLE
        }

        binding.btnCancel.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.parent)
            binding.layoutHomeClick.visibility = View.GONE
        }

        binding.btnAddFromGallery.setOnClickListener {
            if (checkNeedToRequestPermission())
                showDialogPermission()
            else {
                startActivity(Intent(this, ShowImageActivity::class.java))
                binding.btnCancel.performClick()
            }

        }

        binding.btnImport.setOnClickListener {
            selectPdf()
            binding.btnCancel.performClick()
        }

        binding.btnTakeAPhoto.setOnClickListener {
            Common.askPermissionCamera(this, object : Common.AskPermissionCallback {
                override fun onGranted() {
                    startActivity(Intent(this@MainActivity, ActivityTakePhoto::class.java))
                    binding.btnCancel.performClick()
                }
            })
        }

        binding.edtSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                fileAdapter.submitList(repository.getFileByName(newText ?: ""))
                return true
            }

        })

        binding.imgMenu.setOnClickListener {
            onSingleSectionWithIconsClicked(it)
        }
        binding.imgBackSelect.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSelectMode) {
                    isSelectAll = false
                    binding.imgSelectAll.setImageResource(R.drawable.ic_option2)
                    isSelectMode = false
                    changeModeSelect(false)
                } else
                    finish()
            }
        })

        binding.imgSelectAll.setOnClickListener {
            isSelectAll = !isSelectAll
            binding.imgSelectAll.setImageResource(if (isSelectAll) R.drawable.ic_option2_selected else R.drawable.ic_option2)
            fileAdapter.selectAll(isSelectAll)
            binding.tvCountSelected.text = if (isSelectAll) "${fileAdapter.itemCount} Selected" else "0 Selected"
        }

        binding.viewBottomSub.setOnClickListener { }

        binding.btnSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
        binding.imgPremium.setOnClickListener { startActivity(Intent(this, PremiumActivity::class.java)) }

        binding.btnShareMultiFile.setOnClickListener {
            BottomSheetShare(callback = {

            }).apply {
                isShareText = false
                listData = getListAppCanShare()
                val temp = fileAdapter.listData.filter { it.isSelect }
                listFilesToShare = temp.map { it.filePathParent + "/${it.fileName}.pdf" }
                show(supportFragmentManager, "share_file")
            }
        }

        binding.btnDeleteMultiFile.setOnClickListener {
            DialogCustom(this@MainActivity).apply {
                idImage = R.drawable.img_delete
                titleValue = "Are you sure you want to delete?"
                descriptionValue = "Once deleted, these Files cannot be recovered."
                yesValue = "Delete"
                cancelValue = "Cancel"
                callback = object : IYesNoCallback {
                    override fun onYes() {
                        val tempFile = fileAdapter.listData.filter { it.isSelect }
                        for (i in tempFile) {
                            repository.deleteFile(i)
                            FileUtil.deleteFile("${i.filePathParent}/${i.fileName}.pdf")
                        }
                        fileAdapter.submitList(repository.getAllFiles())
                        this@MainActivity.binding.tvCountSelected.text = "0 Selected"
                    }
                }
                show()
            }
        }

    }

    var isSelectAll = false
    private fun onSingleSectionWithIconsClicked(view: View) {
        val menu = popupMenu {

            setTheme(R.style.Widget_MPM_Menu_Dark_CustomBackground)
            dropdownGravity = Gravity.END or Gravity.BOTTOM
            section {
                item {
                    label = "Sort"
                    labelColor = ContextCompat.getColor(this@MainActivity, R.color.black)
                    icon = R.drawable.ic_sort //optional
                    iconColor = ContextCompat.getColor(this@MainActivity, R.color.main_color)
                    callback = { //optional
                        showBottomSheetSortFile()
                    }
                }
                item {
                    label = "Select"
                    labelColor = ContextCompat.getColor(this@MainActivity, R.color.black)

                    iconColor = ContextCompat.getColor(this@MainActivity, R.color.main_color)
                    icon = R.drawable.ic_option_selected
                    callback = { //optional
                        isSelectMode = true

                        Log.e("~~~", "Callback ")

                    }
                }

            }
        }

        menu.setOnDismissListener {
            changeModeSelect(isSelectMode)
        }
        menu.show(this@MainActivity, view)

    }

    private fun showBottomSheetSortFile() {
        BottomSheetSort().apply {
            isAscending = currentAscend
            sortBy = currentSort
            callback = object : BottomSheetSort.ISort {
                override fun onSort(option: BottomSheetSort.SortBy, isAscend: Boolean) {
                    currentSort = option
                    currentAscend = isAscend
                    fileAdapter.submitList(repository.getAllFile(sortBy, isAscend))
                }
            }
        }.show(supportFragmentManager, "sort file")
    }

    var currentAscend = false
    var currentSort = BottomSheetSort.SortBy.Created
    private fun changeModeSelect(isSelect: Boolean) {
//        TransitionManager.beginDelayedTransition(binding.root)
        if (isSelect) {
            binding.layoutTop1.show(false)
            binding.layoutTop2.show(true)
            binding.viewBottomSub.show(true)
            binding.viewBottom.show(false)
            binding.tvCountSelected.text = "0 Selected"
        } else {
            binding.layoutTop1.show(true)
            binding.layoutTop2.show(false)
            binding.viewBottomSub.show(false)
            binding.viewBottom.show(true)
        }
        fileAdapter.changeSelectedMode(isSelect)


    }

}