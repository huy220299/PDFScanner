package com.remi.pdfscanner.ui.editIamge

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Handler
import android.os.Message
import org.wysaid.nativePort.CGENativeLibrary
import java.io.IOException
import java.io.Serializable
import java.util.*

object FilterWorker {
    fun getListFilteredBitmap(context: Context, bitmap: Bitmap, callback: FilterPreviewBitmapCallback) {
        val filterDataList = getFilterDataList(context).toMutableList()
        val handler = Handler { msg: Message ->
            if (msg.what == 1) {
                callback.onSuccess(filterDataList)
            }
            true
        }
        Thread {
            val bitmapPreview = Bitmap.createScaledBitmap(bitmap, 200, 200 * bitmap.height / bitmap.width, false)
            for (i in filterDataList.indices) {
                val bm = CGENativeLibrary.filterImage_MultipleEffects(bitmapPreview, filterDataList[i].rule, 1f)
                filterDataList[i].bitmap = bm
            }
//            val bmNone = BitmapFactory.decodeResource(context.resources, R.drawable.ic_none)
//            filterDataList.add(0, FilterData("None", "", bmNone))
            handler.sendEmptyMessage(1)
        }.start()
    }

    fun getFilterDataList(context: Context): List<FilterData> {
        val filterDataList: MutableList<FilterData> = ArrayList()
        var listFilterName: List<String?> = ArrayList()
        val assetManager: AssetManager
        try {
            assetManager = context.assets
            listFilterName = Arrays.asList(*assetManager.list("filter"))
        } catch (e: IOException) {
        }
        val listRules: MutableList<String> = ArrayList()
        for (i in listFilterName.indices) {
            listRules.add("@adjust lut " + listFilterName[i])
            //            listRules.add("@adjust lut " + listFilterName.get(i) +" @adjust whitebalance -0.44 2.52");
            filterDataList.add(FilterData(listFilterName[i]!!.replace(".png", ""), listRules[i]))
        }
        return filterDataList
    }

    interface FilterPreviewBitmapCallback {
        fun onSuccess(list: List<FilterData>)
    }
    class FilterData : Serializable {
        var name: String
        var rule: String
        var isSelected: Boolean
        var bitmap: Bitmap? = null

        constructor(name: String, rule: String) {
            this.name = name
            this.rule = rule
            isSelected = false
        }

        constructor(name: String, rule: String, bitmap: Bitmap?) {
            this.name = name
            this.rule = rule
            isSelected = false
            this.bitmap = bitmap
        }
    }
}