package com.remi.pdfscanner.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.remi.pdfscanner.R
import com.remi.pdfscanner.repository.model.GalleryModel
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt


object Common {
    const val TAG = "~~~"

    /**
     * get all image from storage
     */
    fun getAllFolder(mContext: Context): MutableList<GalleryModel> {
        var isFolder = false
        var position = 0
        val listFolder: ArrayList<GalleryModel> = ArrayList<GalleryModel>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION)
        val orderBy = MediaStore.Images.Media.DATE_TAKEN
        val columnIndexData: Int
        val columnIndexFolderName: Int
        val columnIndexOrientation: Int
        var absolutePathOfImage: String
        var orientationOfImage: Int
        val cursor = mContext.contentResolver.query(uri, projection, null, null, "$orderBy DESC")
        columnIndexData = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        columnIndexFolderName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        columnIndexOrientation = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(columnIndexData)
            val file1 = File(absolutePathOfImage)
            if (!file1.canRead() || absolutePathOfImage.contains(".gif")) continue
            orientationOfImage = cursor.getInt(columnIndexOrientation)
            for (i in listFolder.indices) {
                if (listFolder[i].nameFolder != null) {
                    if (listFolder[i].nameFolder.equals(cursor.getString(columnIndexFolderName))) {
                        isFolder = true
                        position = i
                        break
                    } else {
                        isFolder = false
                    }
                }
            }
            if (isFolder) {
                val allPath = ArrayList<String>()
                val allOrientation = ArrayList<Int>()
                allPath.addAll(listFolder[position].allImageInFolder!!)
                allOrientation.addAll(listFolder[position].listOrientation!!)
                allPath.add(absolutePathOfImage)
                allOrientation.add(orientationOfImage)
                listFolder[position].allImageInFolder = allPath
                listFolder[position].listOrientation = allOrientation
            } else {
                var file: File
                @SuppressLint("Range") val firstImage = cursor.getString(cursor.getColumnIndex(projection[1]))
                file = File(firstImage)
                val alPath = ArrayList<String>()
                val alOrientation = ArrayList<Int>()
                if (file.exists()) {
                    alPath.add(absolutePathOfImage)
                    alOrientation.add(orientationOfImage)
                    val objModel = GalleryModel(cursor.getString(columnIndexFolderName), alPath, alOrientation)

                    listFolder.add(objModel)
                }
            }
        }

        return listFolder
    }

    /**
     * dropbox request change url to download
     */
    fun convertUrl(url: String): String {
        return url.replace("www.dropbox", "dl.dropboxusercontent")
    }



    fun showKeyboard(edt: EditText, imm: InputMethodManager) {

        edt.requestFocus()
        imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT)
    }




    @SuppressLint("SimpleDateFormat")
    fun convertTime(times: Long): String {
        val result: String
        val date = Date(times)
        val formatter: DateFormat = SimpleDateFormat("MM_dd_HH_mm_ss")
        formatter.timeZone = TimeZone.getDefault()
        result = formatter.format(date)
        return result
    }

    /**
     * convert % opacity to hex value in android
     */
    fun convertOpacityToStringColor(percent: Int, color: String): String {

        var i = percent / 100.0
        i = round(i * 100) / 100.0
        val alpha = (i * 255).roundToInt()
        var hex = Integer.toHexString(alpha).uppercase(Locale.getDefault())
        if (hex.length == 1) hex = "0$hex"

        return "#$hex${color.substring(color.length - 6)}"
    }

    fun openSettingPermissionManagerAllFile(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", context.packageName))
               context.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                context.startActivity(intent)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun handleLong2Time(time:Long):String{
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM")
        return format.format(date)
    }

    interface AskPermissionCallback {
        fun onGranted()
    }

    fun askPermissionCamera(context: Context, callback: AskPermissionCallback) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    callback.onGranted()
                }

                override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
                    Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
                }
            })
            .check()
    }

    /**
     * ask storage permission in android 13 and lower
     */
    fun askPermission(context: Context?, callback: AskPermissionCallback) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(context)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionRationaleShouldBeShown(p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?, p1: PermissionToken?) {

                    }

                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        callback.onGranted()
                    }

                }).check()
        } else {
            Dexter.withContext(context)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionRationaleShouldBeShown(p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?, p1: PermissionToken?) {

                    }

                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        callback.onGranted()
                    }

                }).check()

        }
    }

    /**
     * save image to cache to share
     */
    fun saveImageToShare(context: Context, image: Bitmap,name:String?): Uri? {
        //TODO - Should be processed in another thread
        val imagesFolder = File(context.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, name?:"shared_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(context, context.packageName, file)
        } catch (e: IOException) {
            Log.d("~~~", "IOException while trying to write file for sharing: " + e.message)
        }
        return uri
    }

    /**
     * save image to Picture folder in device: other app can use this image
     */
    @Throws(IOException::class)
    fun saveImageToPicture(context: Context, bitmapIn: Bitmap?, name: String): String? {
        var bitmap = bitmapIn
        if (bitmap == null) {
            Toast.makeText(context, context.resources.getString(R.string.cant_save_bitmap), Toast.LENGTH_SHORT).show()
            return null
        }
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + context.resources.getString(R.string.save_folder))
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = resolver.openOutputStream(imageUri!!)

//            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
//            return Environment.DIRECTORY_PICTURES + File.separator + context.getResources().getString(R.string.save_folder) + "/" + name + ".jpg";
        } else {
            val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +
                    File.separator + context.resources.getString(R.string.save_folder))
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val image = File(folder.absolutePath, "$name.jpg")
            fos = FileOutputStream(image)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)))
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos!!.close()
        Toast.makeText(context, context.resources.getString(R.string.save_bitmap_success), Toast.LENGTH_SHORT).show()
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +
                File.separator + context.resources.getString(R.string.save_folder) + "/" + name + ".jpg"
    }

    /**
     * check internet connection
     */
    fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.allNetworkInfo
        for (networkInfo in info) {
            if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }

    val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels
    val screenHeight: Int
        get() = Resources.getSystem().displayMetrics.heightPixels

    fun getScreenHeightMax(context: Context): Int {
        var navigationBarHeight = 0
        val resourceId1 =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId1 > 0) {
            navigationBarHeight = context.resources.getDimensionPixelSize(resourceId1)
        }
        var statusbarHeight = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        return Resources.getSystem().displayMetrics.heightPixels + statusbarHeight + navigationBarHeight
    }

    fun getScreenHeightStatus(context: Context): Int {
        var statusbarHeight = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        return Resources.getSystem().displayMetrics.heightPixels + statusbarHeight
    }


    fun randomNum(min: Int, max: Int): Int {
        val r = Random()
        return r.nextInt(max - min + 1) + min
    }

    fun saveBitmapToCache(context: Context, bitmapIn: Bitmap?, name: String): String? {
        var bitmap = bitmapIn
        if (bitmap == null) {
            Toast.makeText(context, "Can't save!", Toast.LENGTH_SHORT).show()
            return null
        }
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val fos: OutputStream
        val folder: File = File(FileUtil.getStore(context).toString() +File.separator+ context.getString(R.string.my_cache_folder))
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val image = File(folder.absolutePath, "$name.png")
        return try {
            fos = FileOutputStream(image)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)))
            image.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * save image to user folder: 0/storage/android/data/...
     */
    fun saveImageToUserData(context: Context, bitmapIn: Bitmap?, path: String, name: String, quality: Int): String? {
        var bitmap = bitmapIn
        if (bitmap == null) {
            Toast.makeText(context, context.resources.getString(R.string.cant_save_bitmap), Toast.LENGTH_SHORT).show()
            return ""
        }
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val fos: OutputStream
        val folder = File(path)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val image = File(folder.absolutePath, "$name.jpg")
        return try {
            fos = FileOutputStream(image)
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos)
            fos.close()
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)))
            image.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun saveImageToUserData(context: Context, bitmapIn: Bitmap?, path: String, name: String): String? {
        var bitmap = bitmapIn ?: return ""
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val fos: OutputStream
        val folder = File(path)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val image = File(folder.absolutePath, "$name.jpg")
        return try {
            fos = FileOutputStream(image)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(image)
                )
            )
            image.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @Throws(IOException::class)
    fun saveImageToPictureJPG(context: Context, bitmapIn: Bitmap?, name: String): String? {
        var bitmap = bitmapIn
            ?: //            Toast.makeText(context, context.getResources().getString(R.string.cant_save_bitmap), Toast.LENGTH_SHORT).show();
            return null
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + context.packageName + context.resources.getString(R.string.save_folder)
            )
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }

//            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
//            return Environment.DIRECTORY_PICTURES + File.separator + context.getResources().getString(R.string.save_folder) + "/" + name + ".jpg";
        } else {
            val folder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() +
                        File.separator + context.resources.getString(R.string.save_folder)
            )
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val image = File(folder.absolutePath, "$name.jpg")
            fos = FileOutputStream(image)
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(image)
                )
            )
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos?.close()
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() +
                File.separator + context.resources.getString(R.string.save_folder) + "/" + name + ".jpg"
    }

    @Throws(IOException::class)
    fun saveImageToPicturePNG(context: Context, bitmapIn: Bitmap?, name: String): String? {
        var bitmap = bitmapIn
            ?: //            Toast.makeText(context, context.getResources().getString(R.string.cant_save_bitmap), Toast.LENGTH_SHORT).show();
            return null
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.png")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + context.resources.getString(R.string.save_folder)
            )
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }

//            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
//            return Environment.DIRECTORY_PICTURES + File.separator + context.getResources().getString(R.string.save_folder) + "/" + name + ".jpg";
        } else {
            val folder = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() +
                        File.separator + context.resources.getString(R.string.save_folder)
            )
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val image = File(folder.absolutePath, "$name.png")
            fos = FileOutputStream(image)
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(image)
                )
            )
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos?.close()

        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString() +
                File.separator + context.resources.getString(R.string.save_folder) + "/" + name + ".png"
    }

    /**
     * read name file in asset
     */
    fun readFromAsset(context: Context, folderName: String, filename: String): String {
        return try {
            val file = "$folderName/$filename";
            val bufferReader = context.assets.open(file).bufferedReader()
            val data = bufferReader.use {
                it.readText()
            }
            data;
        } catch (e: Exception) {
          e.toString()
        }

    }


    fun getFontFromAssets(context: Context): List<String> {
        var list: List<String> = ArrayList()
        val assetManager: AssetManager
        try {
            assetManager = context.assets
            list = Arrays.asList(*assetManager.list("font"))
        } catch (ignored: IOException) {
        }
        return list
    }


}