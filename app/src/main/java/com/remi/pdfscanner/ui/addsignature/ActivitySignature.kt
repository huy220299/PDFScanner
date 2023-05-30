package com.remi.pdfscanner.ui.addsignature

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivitySignatureBinding
import com.remi.pdfscanner.ui.anticounter.ColorAdapter
import com.remi.pdfscanner.util.Common
import com.remi.pdfscanner.util.FileUtil
import com.remi.pdfscanner.util.setListener
import com.remi.pdfscanner.util.setSize

class ActivitySignature : BaseActivity<ActivitySignatureBinding>(ActivitySignatureBinding::inflate) {
    override fun setSize() {
        binding.tvNext.setSize(14)
        binding.tvSize.setSize(14)
        binding.tvTitle.setSize(20)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.recyclerview.apply {
            adapter = ColorAdapter(back = {
                val colorDialog = ColorPickerDialogBuilder
                    .with(context)
                    .setTitle("Choose color")
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("ok") { _, selectedColor, _ ->
                        if (selectedColor != 0) {
                            val tempColor = "#" + Integer.toHexString(selectedColor)
                            binding.sketchBoard.paintColor = selectedColor
                        }
                    }
                    .setNegativeButton("cancel") { _, _ -> }
                    .build()

                if (it.equals("#00000000", true)) {
                    colorDialog.show()
                    colorDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                    colorDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                } else {
                    binding.sketchBoard.paintColor = Color.parseColor(it)
                }
            }).apply { currentPosition = findCurrentColor("#333333") }
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        binding.seekbarSize.setListener {
            binding.sketchBoard.setPaintWidthPx(it/2f)
        }


        binding.imgUndo.setOnClickListener {
            binding.sketchBoard.undoLast()
        }
        binding.imgRedo.setOnClickListener {
            binding.sketchBoard.redoLast()
        }
        binding.imgClear.setOnClickListener {
            binding.sketchBoard.clearDraw()
        }
        binding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.tvNext.setOnClickListener {
            val url = Common.saveImageToUserData(this, getBitmapSignature(binding.sketchBoard), FileUtil.getFolderSignatureImage(this), "Signature_${System.currentTimeMillis()}")
            setResult(RESULT_OK, Intent().apply { putExtra("url", url) })
            finish()
        }
    }

    private fun getBitmapSignature(view: View): Bitmap {
        val bitmapResult = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapResult)
        view.draw(canvas)
        return Bitmap.createScaledBitmap(bitmapResult, 400, 400, false)
    }


}