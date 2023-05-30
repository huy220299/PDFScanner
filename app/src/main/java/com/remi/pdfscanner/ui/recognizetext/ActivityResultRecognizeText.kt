package com.remi.pdfscanner.ui.recognizetext

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivityResultRecognizeTextBinding
import com.remi.pdfscanner.util.FileUtil
import com.remi.pdfscanner.util.MySharePref
import com.remi.pdfscanner.util.setSize
import java.io.File
import java.io.FileOutputStream


class ActivityResultRecognizeText : BaseActivity<ActivityResultRecognizeTextBinding>(ActivityResultRecognizeTextBinding::inflate) {

    /**
     * saved bitmap to recognize again
     */
    private var bitmapLoaded: Bitmap? = null
    private var launcherShowList = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.run {
                selectedLanguage = this.getIntExtra("language", 0)
                setTextByLanguage()
                MySharePref.updateLanguageORC(this@ActivityResultRecognizeText, selectedLanguage)
                bitmapLoaded?.run { runTextRecognition(this) }
            }
        }
    }

    override fun setSize() {
        binding.tvLanguage.setSize(14)
        binding.tvRecognize.setSize(11)
        binding.tvCopy.setSize(11)
        binding.tvExport.setSize(11)
    }

    private var selectedLanguage = 0
    lateinit var dialogNoText: BottomSheetNoText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogNoText = BottomSheetNoText(callback = {
            if (it)
                launcherShowList.launch(Intent(this, ActivitySelectLanguage::class.java))
            else
                finish()
        })
        onClick()
        Glide.with(this).asBitmap().load(intent.getStringExtra("url"))
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    bitmapLoaded = resource
                    runTextRecognition(resource)
                    binding.imgPreview.setImageBitmap(resource)
                }
            })
        selectedLanguage = MySharePref.getLanguageORC(this)
        setTextByLanguage()

    }

    private fun exportText2Share() {

        val textFolder = File(cacheDir, "images")
        textFolder.mkdirs()
        val file = File(textFolder, "recognized.txt")
        file.appendText(binding.tvResult.text.toString())
        FileUtil.shareFile(this, file.path)
    }

    private fun copyTextToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("text_recognized", binding.tvResult.text.toString())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
    }

    private fun onClick() {
        binding.imgBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.tvLanguage.setOnClickListener { launcherShowList.launch(Intent(this, ActivitySelectLanguage::class.java)) }
        binding.btnRecognize.setOnClickListener {
            bitmapLoaded?.run { runTextRecognition(this) }
        }
        binding.btnCopy.setOnClickListener {
            copyTextToClipboard()
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
        }
        val activities: List<ResolveInfo> = packageManager.queryIntentActivities(sendIntent, 0)
        val listPair: MutableList<BottomSheetShare.PkgData> = ArrayList()
        for (info in activities) {
            listPair.add(BottomSheetShare.PkgData(info.activityInfo.packageName, info.loadLabel(packageManager).toString(), info.loadIcon(packageManager)))
        }
        binding.btnExport.setOnClickListener {
            BottomSheetShare(callback = {
                when (it) {
                    "copy" -> copyTextToClipboard()
                    "export" -> exportText2Share()
                }


            }).apply {
                listData = listPair
                textToShare = this@ActivityResultRecognizeText.binding.tvResult.text.toString()
                show(supportFragmentManager, "share_text")
            }
        }

    }

    private fun setTextByLanguage() {
        val tempText = when (selectedLanguage) {
            1 -> "Language: Chinese"
            2 -> "Language: Devanagari"
            3 -> "Language: Japanese"
            4 -> "Language: Korean"
            else -> "Language: Latin"
        }
        binding.tvLanguage.text = tempText
    }

    private fun runTextRecognition(bitmapIn: Bitmap) {
        val image = InputImage.fromBitmap(bitmapIn, 0)
        val recognizer = when (selectedLanguage) {
            1 -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            2 -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            3 -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            4 -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        }

//        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

        recognizer.process(image)
            .addOnSuccessListener { texts ->
                binding.tvResult.setText(texts.text)
                if (texts.text.isEmpty()) {
                    dialogNoText.show(supportFragmentManager, "dialog_no_text")
                    binding.btnCopy.isEnabled = false
                    binding.btnExport.isEnabled = false
                    binding.imgExport.alpha = .5f
                    binding.tvExport.alpha = .5f
                    binding.imgCopy.alpha = .5f
                    binding.tvCopy.alpha = .5f
                } else {

                    binding.btnCopy.isEnabled = true
                    binding.btnExport.isEnabled = true
                    binding.imgExport.alpha = 1f
                    binding.tvExport.alpha = 1f
                    binding.imgCopy.alpha = 1f
                    binding.tvCopy.alpha = 1f
                }
            }
            .addOnFailureListener { e -> // Task failed with an exception
                e.printStackTrace()
            }
    }


}