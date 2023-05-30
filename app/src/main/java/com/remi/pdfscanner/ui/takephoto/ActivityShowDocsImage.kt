package com.remi.pdfscanner.ui.takephoto

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.base.ViewPagerAddFragmentsAdapter
import com.remi.pdfscanner.databinding.ActivityShowDocsImageBinding
import com.remi.pdfscanner.ui.crop.CropImageActivity
import com.remi.pdfscanner.ui.editIamge.DepthPageTransformer
import com.remi.pdfscanner.ui.editIamge.FragmentPhotoView
import com.remi.pdfscanner.ui.pickphoto.IImageClick
import com.remi.pdfscanner.ui.pickphoto.MyImage
import com.remi.pdfscanner.ui.pickphoto.SelectedImageAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityShowDocsImage:BaseActivity<ActivityShowDocsImageBinding>(ActivityShowDocsImageBinding::inflate) {
    override fun setSize() {

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val listTemp:List<String> = Gson().fromJson(intent.getStringExtra("list_images")?:"[]",object : TypeToken<List<String>>() {}.type)
        val listImages:MutableList<MyImage> = ArrayList()
        binding.tvIndicator.text = "1/${listImages.size}"
        listTemp.forEach { listImages.add(MyImage(it,false)) }

        binding.recyclerviewSelected.apply {
            layoutManager = LinearLayoutManager(this@ActivityShowDocsImage, LinearLayoutManager.HORIZONTAL, false)
            adapter = SelectedImageAdapter().apply {
                listImage = listImages
                //remove item selected -> reload item in list detail
                itemClick = object : IImageClick {
                    override fun onClick(position : Int,path: String) {
                        //remove in side adapter
                        (binding.viewpager2.adapter as ViewPagerAddFragmentsAdapter).removeFag(position)
                        (binding.viewpager2.adapter as ViewPagerAddFragmentsAdapter).notifyItemRemoved(position)
                        if ((binding.viewpager2.adapter as ViewPagerAddFragmentsAdapter).itemCount==0)
                            finish()
                        else
                            binding.tvIndicator.text = "${binding.viewpager2.currentItem+1}/${listImages.size}"
                    }
                }
            }
        }

        binding.viewpager2.adapter = ViewPagerAddFragmentsAdapter(supportFragmentManager, lifecycle).apply {
            for (i in listImages) {
                addFrag(FragmentPhotoView().apply { url=i.path })
            }
        }
        binding.viewpager2.setPageTransformer(DepthPageTransformer())
        binding.viewpager2.registerOnPageChangeCallback(object :OnPageChangeCallback(){
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tvIndicator.text = "${binding.viewpager2.currentItem+1}/${listImages.size}"
            }
        })

        binding.tvImport.setOnClickListener {
            startActivity(Intent(this,CropImageActivity::class.java).apply {
                putExtra("list_image",Gson().toJson((binding.recyclerviewSelected.adapter as SelectedImageAdapter).listImage))
            })
            finish()
        }
    }

}