package com.remi.pdfscanner.ui.recognizetext

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.remi.pdfscanner.BuildConfig
import com.remi.pdfscanner.R
import com.remi.pdfscanner.base.BaseBottomSheet
import com.remi.pdfscanner.databinding.BottomSheetShareTextBinding
import com.remi.pdfscanner.util.setSize
import java.io.File

class BottomSheetShare(val callback: (String) -> Unit) : BaseBottomSheet<BottomSheetShareTextBinding>(BottomSheetShareTextBinding::inflate) {

    var listData: MutableList<PkgData> = ArrayList()
    var textToShare = ""
    var isShareText = true
    var listFilesToShare: List<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        binding.tvCopy.setOnClickListener {
            callback("copy")
            dismiss()
        }
        binding.tvExport.setOnClickListener {
            callback("export")
            dismiss()
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSize()

        binding.imgBack.setOnClickListener { dismiss() }
        setupRecyclerview()
    }


    @SuppressLint("QueryPermissionsNeeded")
    private fun setupRecyclerview() {
        var sendIntent: Intent? = null
        if (isShareText) {
            sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textToShare)
                type = "text/plain"
            }
        } else {

            if (listFilesToShare.size == 1 || listFilesToShare.isEmpty()) {
                val uri: Uri
                val file = File(textToShare)
                if (file.exists()) {
                    uri = (FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID, file))
                    sendIntent = Intent(Intent.ACTION_SEND).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        type = "*/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                    }
                }
            } else {
                val uris = ArrayList<Uri>()
                for (i in listFilesToShare) {
                    val file = File(i)
                    if (file.exists()) {
                        uris.add(FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID, file))
                    }
                }
                sendIntent = Intent( Intent.ACTION_SEND_MULTIPLE).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)

                    flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                }
            }
        }

        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = ShareTextAdapter(callback = {
                sendIntent?.run {
                    setPackage(it)
                    requireContext().startActivity(Intent.createChooser(sendIntent, "send"))
                }
                if (sendIntent == null) {
                    Toast.makeText(requireContext(), "Some thing went wrong!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }).apply {
                listItem = listData
            }
        }
    }

    private fun setSize() {
        binding.tvTitle.setSize(20)
        binding.tvDes.setSize(12)
        binding.tvCopy.setSize(14)
        binding.tvExport.setSize(14)

        binding.tvSaveLocal.setSize(14)
        binding.tvExportJpg.setSize(14)
        binding.tvExportPdf.setSize(14)
        binding.tvTitle.text = if (isShareText) getString(R.string.export) else getString(R.string.share)
        binding.tvDes.text = if (isShareText) getString(R.string.des_share_text) else getString(R.string.des_share_file)
        if (isShareText) {
            binding.layoutShareFile.visibility = View.GONE
            binding.layoutShareText.visibility = View.VISIBLE
        } else {
            binding.layoutShareText.visibility = View.GONE
            binding.layoutShareFile.visibility = View.GONE
//            binding.layoutShareFile.visibility = View.VISIBLE
        }
    }


    data class PkgData(val pkgName: String, val pkgLabel: String, val icon: Drawable)
}