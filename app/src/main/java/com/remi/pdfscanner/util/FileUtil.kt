package com.remi.pdfscanner.util

import android.app.PendingIntent
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.FileProvider
import com.remi.pdfscanner.BuildConfig
import com.remi.pdfscanner.R
import java.io.*

object FileUtil {

    fun rateApp(context: Context) {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)
                )
            )
        }
    }

    fun getMyDocumentFolder(context: Context): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path + File.separator + context.getString(R.string.app_name)
    }

    fun getFolderCacheImage(context: Context): String {
        return getStore(context) + File.separator + context.getString(R.string.my_cache_folder) + File.separator
    }

    fun getFolderSignatureImage(context: Context): String {
        return getStore(context) + File.separator + context.getString(R.string.my_signature_folder) + File.separator
    }

    fun getFolderPreviewImage(context: Context): String = getStore(context) + File.separator + "Image_Preview" + File.separator
    fun getFolderPDF(context: Context): String = getStore(context) + File.separator + "PDF_Created" + File.separator


    fun getStore(c: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val f = c.getExternalFilesDir(null)
            if (f != null) f.absolutePath else "/storage/emulated/0/Android/data/" + c.packageName
        } else {
            (Environment.getExternalStorageDirectory().absolutePath
                    + "/Android/data/" + c.packageName)
        }
    }


    fun deleteFile(path: String?): Boolean {
        var result = false
        val file = File(path)
        file.mkdirs()
        if (file.isDirectory) {
//            String[] children = file.list();
            for (i in file.listFiles().indices) {
//                result = new File(file, children[i]).delete();
                result = deleteFile(file.listFiles()[i].absolutePath)
            }
            file.delete()
        } else {
            result = file.delete()
        }
        return result
    }

    fun deleteMyCacheFolder(context: Context) {
        val path = getStore(context) + File.separator + context.getString(R.string.my_cache_folder)
        deleteRecursive(File(path))
    }

    fun deleteRecursive(fileOrDirectory: File) {
        try {
            if (fileOrDirectory.isDirectory) {
                for (child in fileOrDirectory.listFiles()) deleteRecursive(child)
            }
            fileOrDirectory.delete()
        } catch (e: Exception) {
        }
    }

    fun renameFile(pathIn: String, oldName: String, newName: String): Boolean {

        val fileIn = File("$pathIn$oldName.pdf")
        if (fileIn.exists()) {
            return fileIn.renameTo(File(fileIn.parent + File.separator + newName + ".pdf"))

        }
        return false


    }

    fun shareFile(context: Context, path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)
                val intent = Intent(Intent.ACTION_SEND)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                context.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteXI(launcher: ActivityResultLauncher<IntentSenderRequest?>, path: String?, context: Context) {
        val contentResolver = context.contentResolver
        var pendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val tempFile = File(path)
            val mediaID = getFilePathToMediaID(tempFile.absolutePath, context)
            val Uri_one = ContentUris.withAppendedId(
                MediaStore.Images.Media.getContentUri("external"),
                mediaID
            )
            val uris: MutableList<Uri> = ArrayList()
            uris.add(Uri_one)
            pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris)
        }
        if (pendingIntent != null) {
            val sender = pendingIntent.intentSender
            val request = IntentSenderRequest.Builder(sender).build()
            launcher.launch(request)
        }
    }

    fun getFilePathToMediaID(songPath: String, context: Context): Long {
        var id: Long = 0
        val cr = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")
        val selection = MediaStore.Audio.Media.DATA
        val selectionArgs = arrayOf(songPath)
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val cursor = cr.query(
            uri, projection,
            "$selection=?", selectionArgs, null
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                id = cursor.getString(idIndex).toLong()
            }
        }
        return id
    }

    fun moveFile(inputPath: String, inputFile: String, outputPath: String) {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            //create output directory if it doesn't exist
            val dir = File(outputPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            `in` = FileInputStream(inputPath + File.separator + inputFile)
            out = FileOutputStream(outputPath + File.separator + inputFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            `in` = null

            // write the output file
            out.flush()
            out.close()
            out = null

            // delete the original file
            File(inputPath + File.separator + inputFile).delete()
        } catch (fnfe1: Exception) {
            Log.e("tag", fnfe1.message!!)
        }
    }

    /**
     * save a file pdf from picked intent
     */
    fun savefile(context: Context, sourceuri: Uri, parentPath: String, nameFile: String): Boolean {
        val fileOut = File(parentPath)
        fileOut.mkdirs()
        val inputStream = context.contentResolver.openInputStream(sourceuri)
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            bis = BufferedInputStream(inputStream)
            bos = BufferedOutputStream(FileOutputStream("$parentPath/$nameFile", false))
            val buf = ByteArray(1024)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) !== -1)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            try {
                bis?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun copyFile(inputPath: String, inputNameFile: String, outputPath: String): String {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            //create output directory if it doesn't exist
            val dir = File(outputPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            `in` = FileInputStream(inputPath + File.separator + inputNameFile)
            out = FileOutputStream(outputPath + File.separator + inputNameFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            `in` = null

            // write the output file
            out.flush()
            out.close()
            out = null
            return outputPath + File.separator + inputNameFile
        } catch (fnfe1: Exception) {
            Log.e("tag", fnfe1.message!!)
        }
        return ""
    }
}