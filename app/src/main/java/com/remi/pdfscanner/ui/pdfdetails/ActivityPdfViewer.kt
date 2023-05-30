package com.remi.pdfscanner.ui.pdfdetails

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityPdfViewerBinding
import com.remi.pdfscanner.engine.OnPdfReorderedInterface
import com.remi.pdfscanner.engine.PDFUtils

class ActivityPdfViewer:BaseActivity<ActivityPdfViewerBinding>(ActivityPdfViewerBinding::inflate) {
    override fun setSize() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mPath = "/storage/emulated/0/Download/dir/added1.pdf"
        PDFUtils.reorderPdfPages(this, mPath, object : OnPdfReorderedInterface {
            override fun onPdfReorderStarted() {

            }

            override fun onPdfReorderCompleted(bitmaps: MutableList<Bitmap>?) {
                setupRecyclerView(bitmaps?.run { mapIndexed { index, bitmap -> ItemPage(bitmap, index) } }!!.toMutableList())
            }

            override fun onPdfReorderFailed() {

            }
        })

    }
    private fun setupRecyclerView(itemPages: MutableList<ItemPage>) {
        binding.recyclerview.apply {
//            adapter = pdfViewAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()

            addOnScrollListener(scrollListener)
        }
    }
    val  runnable = Runnable {
        binding.pageNo.visibility = View.GONE
    }
    var totalPageCount  =0
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            (recyclerView.layoutManager as LinearLayoutManager).run {
                var foundPosition : Int = findFirstCompletelyVisibleItemPosition()

                binding.pageNo.run {
                    if (foundPosition != RecyclerView.NO_POSITION)
                        text = context.getString(R.string.pdfView_page_no,foundPosition + 1,totalPageCount)
                    binding.pageNo.visibility = View.VISIBLE
                }

                if (foundPosition == 0)
                    binding.pageNo.postDelayed({
                        binding.pageNo.visibility = FrameLayout.GONE
                    }, 3000)

//                if (foundPosition != RecyclerView.NO_POSITION) {
//                    statusListener?.onPageChanged(foundPosition, totalPageCount)
//                    return@run
//                }
//                foundPosition = findFirstVisibleItemPosition()
//                if (foundPosition != RecyclerView.NO_POSITION) {
//                    statusListener?.onPageChanged(foundPosition, totalPageCount)
//                    return@run
//                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                binding.pageNo.postDelayed(runnable, 3000)
            } else {
                binding.pageNo.removeCallbacks(runnable)
            }
        }

    }
}