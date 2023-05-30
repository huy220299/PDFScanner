package com.remi.pdfscanner.ui.pickphoto

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.remi.pdfscanner.MyApplication
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityShowImageBinding
import com.remi.pdfscanner.ui.crop.CropImageActivity
import com.remi.pdfscanner.util.setSize
import com.remi.pdfscanner.util.show

@SuppressLint("NotifyDataSetChanged")
class ShowImageActivity : BaseActivity<ActivityShowImageBinding>(ActivityShowImageBinding::inflate) {
    private val cts = ConstraintSet()
    private val ctsDefault = ConstraintSet()
    private var listSelectedImage: MutableList<MyImage> = ArrayList()
    override fun setSize() {
        binding.tvFolder.setSize(20)
        binding.tvImport.setSize(14)
    }

    var isSelectAll = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getBooleanExtra("task_pick",false))
            binding.imgSelect.show(false)

        ctsDefault.clone(binding.root)
        binding.recyclerviewFolder.apply {
            adapter = FolderAdapter().apply {
                listImage = MyApplication.galleryModelList
                itemCLick = object : IImageClick {
                    override fun onClick(position : Int,path: String) {
                        binding.tvFolder.text = path
                        isShowFolder = false
                        (binding.recyclerview.adapter as PhotoAdapter).listImage = fletchData(path)
                        (binding.recyclerview.adapter as PhotoAdapter).notifyDataSetChanged()
                        binding.imgSelect.setImageResource(R.drawable.ic_option2)
                        isSelectAll = false
                        switchVisibility()
                    }
                }
            }
            layoutManager = GridLayoutManager(this@ShowImageActivity, 1, GridLayoutManager.VERTICAL, false)
        }
        binding.recyclerview.apply {
            adapter = PhotoAdapter().apply {
                listImage = fletchData()
                itemCLick = object : IDetailRecyclerview {
                    override fun onClick(path: String) {
                        //check task pick to set data and finish
                        if (intent.getBooleanExtra("task_pick",false)){
                            setResult(RESULT_OK,Intent().apply { putExtra("image_picked",path) })
                            finish()
                        }

                        if (listSelectedImage.any { it.path == path }) {
                            for (i in (listSelectedImage.size - 1) downTo 0)
                                if (listSelectedImage[i].path == path) {
                                    listSelectedImage.removeAt(i)
                                    (binding.recyclerviewSelected.adapter as SelectedImageAdapter).notifyItemRemoved(i)
                                    //make sure only one item here, lol
                                    break
                                }
//                            (binding.recyclerviewSelected.adapter as SelectedImageAdapter).listImage = listSelectedImage
//                            (binding.recyclerviewSelected.adapter as SelectedImageAdapter).notifyDataSetChanged()
                        } else {
                            listSelectedImage.add(MyImage(path))
//                            (binding.recyclerviewSelected.adapter as SelectedImageAdapter).listImage.add(MyImage(path))
                            (binding.recyclerviewSelected.adapter as SelectedImageAdapter).notifyItemInserted(binding.recyclerviewSelected.adapter!!.itemCount)
                            binding.recyclerviewSelected.scrollToPosition(binding.recyclerviewSelected.adapter!!.itemCount - 1)

                        }

                        showListBottom(listSelectedImage.isNotEmpty())
                    }

                    override fun onSelectAll() {
                        listImage.forEach { fromAdapter ->
                            if (isSelectAll) {
                                if (!listSelectedImage.any { it.path == fromAdapter.path })
                                    listSelectedImage.add(MyImage(fromAdapter.path, true))
                            } else {
                                for (i in (listSelectedImage.size - 1) downTo 0)
                                    if (listSelectedImage[i].path == fromAdapter.path) {
                                        listSelectedImage.removeAt(i)
                                    }
                            }
                        }
                        (binding.recyclerviewSelected.adapter as SelectedImageAdapter).listImage = listSelectedImage
                        (binding.recyclerviewSelected.adapter as SelectedImageAdapter).notifyDataSetChanged()
                        showListBottom(listSelectedImage.isNotEmpty())
                    }
                }
            }
            layoutManager = GridLayoutManager(this@ShowImageActivity, 3, GridLayoutManager.VERTICAL, false)
        }
        binding.recyclerviewSelected.apply {
            layoutManager = LinearLayoutManager(this@ShowImageActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = SelectedImageAdapter().apply {
                listImage = listSelectedImage
                //remove item selected -> reload item in list detail
                itemClick = object : IImageClick {
                    override fun onClick(position : Int,path: String) {

                        val temp = (binding.recyclerview.adapter as PhotoAdapter).listImage
                        for (i in temp.indices)
                            if (temp[i].path == path) {
                                (binding.recyclerview.adapter as PhotoAdapter).listImage[i].isSelect = false
                                (binding.recyclerview.adapter as PhotoAdapter).notifyItemChanged(i)
                            }
                    }
                }
            }
        }


            binding.tvFolder.isSelected = true
        onClick()

    }

    /**
     * create animate for list selected bottom
     */
    private fun switchVisibility() {
        cts.apply {
            clone(binding.root)
            if (!isShowFolder)
                connect(binding.recyclerviewFolder.id, ConstraintSet.BOTTOM, binding.viewTop.id, ConstraintSet.BOTTOM)
            else connect(binding.recyclerviewFolder.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            TransitionManager.beginDelayedTransition(binding.root)
            applyTo(binding.root)
        }
    }

    /**
     * handle list image to show by folder or show all
     */
    private fun fletchData(folder: String = "all"): List<MyImage> {
        val result: MutableList<MyImage> = ArrayList()
        for (i in MyApplication.galleryModelList) {
            if (folder == "all" || folder == i.nameFolder) {
                i.allImageInFolder?.let {
                    result.addAll(it.map { item -> MyImage(item) })
                }
            }
        }
        //check if list have any item from list selected bottom
        for (i in result) {
            if (listSelectedImage.any { it.path == i.path }) {
                i.isSelect = true
            }
        }
        return result
    }

    var isShowFolder = false
    private fun onClick() {
        binding.layoutBottom.setOnClickListener {  } //prevent click over recyclerview selected
        binding.tvFolder.setOnClickListener {
            isShowFolder = true
            cts.apply {
                clone(binding.root)
                if (!isShowFolder)
                    connect(binding.recyclerviewFolder.id, ConstraintSet.BOTTOM, binding.viewTop.id, ConstraintSet.BOTTOM)
                else connect(binding.recyclerviewFolder.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                TransitionManager.beginDelayedTransition(binding.root)
                applyTo(binding.root)
            }

        }
        binding.imgSelect.setOnClickListener {
            isSelectAll = !isSelectAll
            binding.imgSelect.setImageResource(if (isSelectAll) R.drawable.ic_option2_selected else R.drawable.ic_option2)
            (binding.recyclerview.adapter as PhotoAdapter).selectAll(isSelectAll)
        }
        binding.tvImport.setOnClickListener {
            startActivity(Intent(this@ShowImageActivity,CropImageActivity::class.java).apply {
                putExtra("list_image",Gson().toJson((binding.recyclerviewSelected.adapter as SelectedImageAdapter).listImage))
            })
        }
        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showListBottom(isShow: Boolean) {
        cts.apply {
            clone(binding.root)
            if (isShow) {
                constrainPercentHeight(binding.layoutBottom.id, .22f)
            } else constrainPercentHeight(binding.layoutBottom.id, .0f)
            TransitionManager.beginDelayedTransition(binding.root)
            applyTo(binding.root)
        }
    }
}