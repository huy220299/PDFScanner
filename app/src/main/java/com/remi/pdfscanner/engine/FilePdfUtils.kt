package com.remi.pdfscanner.engine

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.util.Consumer
import com.remi.pdfscanner.R
import java.io.File
import java.util.*

object FilePdfUtils {
    enum class FileType {
        e_PDF, e_TXT
    }

    /**
     * Prints a file
     *
     * @param file the file to be printed
     */
    fun printFile(mContext: Context, file: File) {
        val mPrintDocumentAdapter: PrintDocumentAdapter = PrintDocumentAdapterHelper(file)
        val printManager = mContext
            .getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = mContext.getString(R.string.app_name) + " Document"
        if (printManager != null) {
            printManager.print(jobName, mPrintDocumentAdapter, null)
            DatabaseHelper(mContext).insertRecord(file.absolutePath, mContext.getString(R.string.printed))
        }
    }

    /**
     * Emails the desired PDF using application of choice by user
     *
     * @param file - the file to be shared
     */
    fun shareFile(mContext: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(mContext, mContext.packageName, file)
        val uris = ArrayList<Uri>()
        uris.add(uri)
        shareFile(mContext,uris)
    }

    /**
     * preOderPdf
     */
     fun preOderPdf(context: Activity, callback:OnPdfReorderedInterface){

        val mPath = "/storage/emulated/0/Download/dir/added1.pdf"
        val mUri = Uri.fromFile(File(mPath))
        PDFUtils.reorderPdfPages(context,mUri, mPath,callback)
    }

    /**
     * Share the desired PDFs using application of choice by user
     *
     * @param files - the list of files to be shared
     */
    fun shareMultipleFiles(mContext: Context, files: List<File>) {
        val uris = ArrayList<Uri>()
        for (file in files) {
            val uri: Uri = FileProvider.getUriForFile(mContext, mContext.packageName, file)
            uris.add(uri)
        }
        shareFile(mContext, uris)
    }

    /**
     * Emails the desired PDF using application of choice by user
     *
     * @param uris - list of uris to be shared
     */
    private fun shareFile(mContext: Context, uris: ArrayList<Uri>) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND_MULTIPLE

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = mContext.getString(R.string.pdf_type)
        mContext.startActivity(Intent.createChooser(intent,
            mContext.resources.getString(R.string.share_chooser)))
    }

    /**
     * opens a file in appropriate application
     *
     * @param path - path of the file to be opened
     */
    fun openFile(context: Activity, path: String, fileType: FileType) {
        if (path == null) {
            StringUtils.showSnackbar(context, R.string.error_path_not_found)
            return
        }
        openFileInternal(context,path, if (fileType == FileType.e_PDF) context.getString(R.string.pdf_type) else context.getString(R.string.txt_type))
    }

    /**
     * This function is used to open the created file
     * applications on the device.
     *
     * @param path - file path
     */
    private fun openFileInternal(mContext: Activity, path: String, dataType: String) {
        val file = File(path)
        val target = Intent(Intent.ACTION_VIEW)
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        try {
            val uri: Uri = FileProvider.getUriForFile(mContext, mContext.packageName, file)
            target.setDataAndType(uri, dataType)
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            openIntent(mContext,Intent.createChooser(target, mContext.getString(R.string.open_file)))
        } catch (e: Exception) {
            StringUtils.showSnackbar(mContext, R.string.error_open_file)
        }
    }

    /**
     * Checks if the new file already exists.
     *
     * @param finalOutputFile Path of pdf file to check
     * @param mFile           File List of all PDFs
     * @return Number to be added finally in the name to avoid overwrite
     */
    private fun checkRepeat(mContext: Context,finalOutputFile: String, mFile: List<File>): Int {
        var flag = true
        var append = 0
        while (flag) {
            append++
            val name = finalOutputFile.replace(mContext.getString(R.string.pdf_ext), append.toString() + mContext.getString(R.string.pdf_ext))
            flag = mFile.contains(File(name))
        }
        return append
    }



    /***
     * Check if file already exists in pdf_dir
     * @param mFileName - Name of the file
     * @return true if file exists else false
     */
    fun isFileExist(mContext: Context,mFileName: String): Boolean {
        val path = mContext.getSharedPreferences(mContext.packageName,Context.MODE_PRIVATE).getString("storage_location",
            StringUtils.getDefaultStorageLocation()) + mFileName
        val file = File(path)
        return file.exists()
    }

    /**
     * Extracts file name from the URI
     *
     * @param uri - file uri
     * @return - extracted filename
     */
    fun getFileName(mContext: Context,uri: Uri): String? {
        var fileName: String? = null
        val scheme = uri.scheme ?: return null
        if (scheme == "file") {
            return uri.lastPathSegment
        } else if (scheme == "content") {
            val cursor = mContext.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                if (cursor.count != 0) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    fileName = cursor.getString(columnIndex)
                }
                cursor.close()
            }
        }
        return fileName
    }

    /**
     * Returns name of the last file with "_pdf" suffix.
     *
     * @param filesPath - ArrayList of image paths
     * @return fileName with _pdf suffix
     */
    fun getLastFileName(mContext: Context,filesPath: ArrayList<String?>): String {
        if (filesPath.size == 0) return ""
        val lastSelectedFilePath = filesPath[filesPath.size - 1]
        val nameWithoutExt = stripExtension(getFileNameWithoutExtension(lastSelectedFilePath))
        return nameWithoutExt + mContext.getString(R.string.pdf_suffix)
    }

    /**
     * Returns the filename without its extension
     *
     * @param fileNameWithExt fileName with extension. Ex: androidDev.jpg
     * @return fileName without extension. Ex: androidDev
     */
    fun stripExtension(fileNameWithExt: String?): String? {
        // Handle null case specially.
        if (fileNameWithExt == null) return null

        // Get position of last '.'.
        val pos = fileNameWithExt.lastIndexOf(".")

        // If there wasn't any '.' just return the string as is.
        return if (pos == -1) fileNameWithExt else fileNameWithExt.substring(0, pos)

        // Otherwise return the string, up to the dot.
    }

    /**
     * Opens image in a gallery application
     *
     * @param path - image path
     */
    fun openImage(mContext: Activity,path: String?) {
        val file = File(path)
        val target = Intent(Intent.ACTION_VIEW)
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        val uri: Uri = FileProvider.getUriForFile(mContext, mContext.packageName, file)
        target.setDataAndType(uri, "image/*")
        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        openIntent(mContext,Intent.createChooser(target, mContext.getString(R.string.open_file)))
    }

    /**
     * Opens the targeted intent (if possible), otherwise show a snackbar
     *
     * @param intent - input intent
     */
    private fun openIntent(mContext: Activity,intent: Intent) {
        try {
            mContext.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            StringUtils.showSnackbar(mContext, R.string.snackbar_no_pdf_app)
        }
    }



    fun getUniqueFileName(mContext:Context,fileName: String): String {
        var outputFileName = fileName
        val file = File(outputFileName)
        if (!isFileExist(mContext,file.name)) return outputFileName
        val parentFile = file.parentFile
        if (parentFile != null) {
            val listFiles = parentFile.listFiles()
            if (listFiles != null) {
                val append = checkRepeat(mContext,outputFileName, Arrays.asList(*listFiles))
                outputFileName = outputFileName.replace(mContext.getString(R.string.pdf_ext), append.toString() + mContext.resources.getString(R.string.pdf_ext))
            }
        }
        return outputFileName
    }

    /**
     * Opens a Dialog to select a filename.
     * If the file under that name already exists, an overwrite dialog gets opened.
     * If the overwrite is cancelled, this first dialog gets opened again.
     *
     * @param preFillName a prefill Name for the file
     * @param ext         the file extension
     * @param saveMethod  the method that should be called when a filename is chosen
     */
    fun openSaveDialog(preFillName: String?, ext: String, saveMethod: Consumer<String?>) {

    }


        /**
         * Extracts file name from the path
         *
         * @param path - file path
         * @return - extracted filename
         */
        fun getFileName(path: String?): String? {
            if (path == null) return null
            val index: Int = path.lastIndexOf(File.separator)
            return if (index < path.length) path.substring(index + 1) else null
        }

        /**
         * Extracts file name from the URI
         *
         * @param path - file path
         * @return - extracted filename without extension
         */
        fun getFileNameWithoutExtension(path: String?): String? {
            if (path == null || path.lastIndexOf(File.separator) == -1) return path
            var filename: String = path.substring(path.lastIndexOf(File.separator) + 1)
            filename = filename.replace(".pdf", "")
            return filename
        }

        /**
         * Extracts directory path from full file path
         *
         * @param path absolute path of the file
         * @return absolute path of file directory
         */
        fun getFileDirectoryPath(path: String): String {
            return path.substring(0, path.lastIndexOf(File.separator) + 1)
        }

}