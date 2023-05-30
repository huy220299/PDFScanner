package com.remi.pdfscanner.engine

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.AsyncTask
import android.os.FileUtils
import android.os.ParcelFileDescriptor
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.WorkerThread
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.parser.PdfImageObject
import com.remi.pdfscanner.R
import com.remi.pdfscanner.engine.FilePdfUtils.openFile
import com.remi.pdfscanner.engine.StringUtils.getSnackbarwithAction
import com.remi.pdfscanner.util.FileUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PDFUtils {

    fun getFormattedSize(file: File): String {
        return String.format("%.2f MB", file.length().toDouble() / (1024 * 1024))
    }


    /**
     * Creates a mDialog with details of given PDF file
     *
     * @param file - file name
     */
    @SuppressLint("StringFormatInvalid")
    fun showDetails(mContext: Context, file: File) {
        val name = file.name
        val path = file.path
        val size: String = getFormattedSize(file)
        val lastModDate: String = getFormattedSize(file)
        val message = TextView(mContext)
        val title = TextView(mContext)
        message.text = String.format(mContext.resources.getString(R.string.file_info), name, path, size, lastModDate)
        message.setTextIsSelectable(true)
        title.setText(R.string.details)
        title.setPadding(20, 10, 10, 10)
        title.textSize = 30f
        title.setTextColor(mContext.resources.getColor(R.color.black))
        val builder = AlertDialog.Builder(mContext)
        val dialog = builder.create()
        builder.setView(message)
        builder.setCustomTitle(title)
        builder.setPositiveButton(mContext.resources.getString(R.string.allow)
        ) { dialogInterface: DialogInterface?, i: Int -> dialog.dismiss() }
        builder.create()
        builder.show()
    }

    /**
     * Check if a PDF at given path is encrypted
     *
     * @param path - path of PDF
     * @return true - if encrypted otherwise false
     */
    @WorkerThread
    fun isPDFEncrypted(path: String?): Boolean {
        var isEncrypted: Boolean
        var pdfReader: PdfReader? = null
        try {
            pdfReader = PdfReader(path)
            isEncrypted = pdfReader.isEncrypted
        } catch (e: IOException) {
            isEncrypted = true
        } finally {
            pdfReader?.close()
        }
        return isEncrypted
    }

    fun compressPDF(
        inputPath: String?, outputPath: String?, quality: Int,
        onPDFCompressedInterface: OnPDFCompressedInterface,
    ) {
        CompressPdfAsync(inputPath, outputPath, quality, onPDFCompressedInterface)
            .execute()
    }

    private class CompressPdfAsync internal constructor(
        val inputPath: String?, val outputPath: String?, val quality: Int,
        val mPDFCompressedInterface: OnPDFCompressedInterface,
    ) : AsyncTask<String?, String?, String?>() {
        var success = false
        override fun onPreExecute() {
            super.onPreExecute()
            mPDFCompressedInterface.pdfCompressionStarted()
        }

        override fun doInBackground(vararg p0: String?): String? {
            success = try {
                val reader = PdfReader(inputPath)
                compressReader(reader)
                saveReader(reader)
                reader.close()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } catch (e: DocumentException) {
                e.printStackTrace()
                false
            }
            return null
        }

        /**
         * Attempt to compress each object in a PdfReader
         *
         * @param reader - PdfReader to have objects compressed
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun compressReader(reader: PdfReader) {
            val n = reader.xrefSize
            var `object`: PdfObject?
            var stream: PRStream
            for (i in 0 until n) {
                `object` = reader.getPdfObject(i)
                if (`object` == null || !`object`.isStream) continue
                stream = `object` as PRStream
                compressStream(stream)
            }
            reader.removeUnusedObjects()
        }

        /**
         * If given stream is image compress it
         *
         * @param stream - Steam to be compressed
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun compressStream(stream: PRStream) {
            val pdfSubType = stream[PdfName.SUBTYPE]
            println(stream.type())
            if (pdfSubType != null && pdfSubType.toString() == PdfName.IMAGE.toString()) {
                val image = PdfImageObject(stream)
                val imageBytes = image.imageAsBytes
                val bmp: Bitmap?
                bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bmp == null) return
                val width = bmp.width
                val height = bmp.height
                val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val outCanvas = Canvas(outBitmap)
                outCanvas.drawBitmap(bmp, 0f, 0f, null)
                val imgBytes = ByteArrayOutputStream()
                outBitmap.compress(Bitmap.CompressFormat.JPEG, quality, imgBytes)
                stream.clear()
                stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION)
                stream.put(PdfName.TYPE, PdfName.XOBJECT)
                stream.put(PdfName.SUBTYPE, PdfName.IMAGE)
                stream.put(PdfName.FILTER, PdfName.DCTDECODE)
                stream.put(PdfName.WIDTH, PdfNumber(width))
                stream.put(PdfName.HEIGHT, PdfNumber(height))
                stream.put(PdfName.BITSPERCOMPONENT, PdfNumber(8))
                stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB)
            }
        }

        /**
         * Save changes to given reader's data to the output path
         *
         * @param reader - changed reader
         * @throws DocumentException
         * @throws IOException
         */
        @Throws(DocumentException::class, IOException::class)
        private fun saveReader(reader: PdfReader) {
            val stamper = PdfStamper(reader, FileOutputStream(outputPath))
            stamper.setFullCompression()
            stamper.close()
        }

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            mPDFCompressedInterface.pdfCompressionEnded(outputPath, success)
        }


    }

    private fun handleNameFileAdded(input: String): String {
        val result = input.split("/").last().replace(".pdf", "")
        return result.split("_add").first()

    }

    /**
     * Main function to add images to PDF
     *
     * @param inputPath:  path of input PDF
     * @param imagesUri: list of images to add
     * @return true, if succeeded, otherwise false
     */
    fun addImagesToPdf(activity: Activity, inputPath: String, imagesUri: List<String>, callback: (String) -> Unit): Boolean {
        return try {
            val reader = PdfReader(inputPath)
            val document = Document()
            val currentName = handleNameFileAdded(inputPath)
            var outPath = FileUtil.getFolderPDF(activity)
            File(outPath).mkdirs()
            val outName = "${currentName}_add_${System.currentTimeMillis()}"
            outPath = FileUtil.getFolderPDF(activity) + "${outName}.pdf"
            val writer = PdfWriter.getInstance(document, FileOutputStream(outPath))
            document.open()
            initDoc(reader, document, writer)
            appendImages(document, imagesUri)
            document.close()
            DatabaseHelper(activity).insertRecord(outPath, activity.getString(R.string.created))
            callback(outName)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            StringUtils.showSnackbar(activity, R.string.something_went_wrong)
            false
        } catch (e: DocumentException) {
            e.printStackTrace()
            StringUtils.showSnackbar(activity, R.string.something_went_wrong)
            false
        }
    }

    /**
     * Adds images to existing PDF
     * @param output - path of output PDF
     */
//    fun addImagesToPdf(activity: Activity, output: String, mImagesUri: List<String>) {
//        val mPath = FileUtil.getStore(activity) + File.separator + "FilePDFCreated" + File.separator + output + System.currentTimeMillis().toString() + activity.getString(R.string.pdf_ext)
//
//        val outputPath: String = FileUtil.getFolderPDF(activity) + output + activity.getString(R.string.pdf_ext)
//        val file = File(mPath)
//        if (!file.exists()) file.mkdirs()
//        if (mImagesUri.isNotEmpty()) {
//            addImagesToPdf(activity, mPath, outputPath, mImagesUri)
//        } else {
//            StringUtils.showSnackbar(activity, R.string.no_images_selected)
//        }
//    }

    /**
     * reorder remove from pdf
     */
    open fun reorderRemovePDF(inputPath: String?, output: String?, pages: String?,context: Context): Boolean {
        return try {
            val reader = PdfReader(inputPath)
            reader.selectPages(pages)
            if (reader.numberOfPages == 0) {
                Toast.makeText(context , "Remove Error!", Toast.LENGTH_SHORT).show()
                return false
            }
            //if (reader.getNumberOfPages() )
            val pdfStamper = PdfStamper(reader,
                FileOutputStream(output))
            pdfStamper.close()
            Toast.makeText(context , "File created!", Toast.LENGTH_SHORT).show()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context , "Remove Error!", Toast.LENGTH_SHORT).show()
            false
        } catch (e: DocumentException) {
            e.printStackTrace()
            Toast.makeText(context , "Remove Error!", Toast.LENGTH_SHORT).show()
            false
        }
    }


    /**
     * Initialise document with pages from reader to writer
     *
     * @param reader   -
     * @param document
     * @param writer
     */
    private fun initDoc(reader: PdfReader, document: Document, writer: PdfWriter) {
        val numOfPages = reader.numberOfPages
        val cb = writer.directContent
        var importedPage: PdfImportedPage?
        for (page in 1..numOfPages) {
            importedPage = writer.getImportedPage(reader, page)
            document.newPage()
            cb.addTemplate(importedPage, 0f, 0f)
        }
    }

    /**
     * Add images at given URIs to end of given document
     *
     * @param document
     * @param imagesUri
     * @throws DocumentException
     * @throws IOException
     */
    @Throws(DocumentException::class, IOException::class)
    private fun appendImages(document: Document, imagesUri: List<String>) {
        val documentRect = document.pageSize
        for (i in imagesUri.indices) {
            document.newPage()
            val image = Image.getInstance(imagesUri[i])
            image.border = 0
            val pageWidth = document.pageSize.width // - (mMarginLeft + mMarginRight);
            val pageHeight = document.pageSize.height // - (mMarginBottom + mMarginTop);
            image.scaleToFit(pageWidth, pageHeight)
            image.setAbsolutePosition(
                (documentRect.width - image.scaledWidth) / 2,
                (documentRect.height - image.scaledHeight) / 2)
            document.add(image)
        }
    }

    fun reorderRemovePDF(activity: Activity, inputPath: String?, output: String, pages: String?): Boolean {
        return try {
            val reader = PdfReader(inputPath)
            reader.selectPages(pages)
            if (reader.numberOfPages == 0) {
                Toast.makeText(activity, "error0", Toast.LENGTH_SHORT).show()
                return false
            }
            //if (reader.getNumberOfPages() )
            val pdfStamper = PdfStamper(reader,
                FileOutputStream(output))
            pdfStamper.close()
            Toast.makeText(activity, "Removed", Toast.LENGTH_SHORT).show()
            DatabaseHelper(activity).insertRecord(output,
                activity.getString(R.string.created))
            true
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(activity, "error1", Toast.LENGTH_SHORT).show()
            false
        } catch (e: DocumentException) {
            e.printStackTrace()
            Toast.makeText(activity, "error2", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * @param uri                     Uri of the pdf
     * @param path                    Absolute path of the pdf
     * @param onPdfReorderedInterface interface to update  pdf reorder progress
     */
    fun reorderPdfPages(activity: Activity, uri: Uri?, path: String?, onPdfReorderedInterface: OnPdfReorderedInterface) {
        ReorderPdfPagesAsync1(uri, path, activity, onPdfReorderedInterface).execute()
    }

    fun reorderPdfPages(activity: Activity, path: String?, onPdfReorderedInterface: OnPdfReorderedInterface) {
        ReorderPdfPagesAsync(path, activity, onPdfReorderedInterface).execute()
    }

    @SuppressLint("StaticFieldLeak")
    private class ReorderPdfPagesAsync(

        private val mPath: String?,
        private val mActivity: Activity,
        private val mOnPdfReorderedInterface: OnPdfReorderedInterface,
    ) : AsyncTask<String?, String?, ArrayList<Bitmap?>?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            mOnPdfReorderedInterface.onPdfReorderStarted()
        }


        /**
         * Get list of Bitmaps from PdfRenderer
         *
         * @param renderer
         * @return
         */
        private fun getBitmaps(renderer: PdfRenderer): ArrayList<Bitmap?> {
            val bitmaps = ArrayList<Bitmap?>()
            val pageCount = renderer.pageCount
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height,
                    Bitmap.Config.ARGB_8888)
                // say we render for showing on the screen
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // do stuff with the bitmap
                bitmaps.add(bitmap)
                // close the page
                page.close()
            }
            return bitmaps
        }

        override fun onPostExecute(bitmaps: ArrayList<Bitmap?>?) {
            super.onPostExecute(bitmaps)
            if (bitmaps != null && !bitmaps.isEmpty()) {
                mOnPdfReorderedInterface.onPdfReorderCompleted(bitmaps)
            } else {
                mOnPdfReorderedInterface.onPdfReorderFailed()
            }
        }


        public override fun doInBackground(vararg p0: String?): ArrayList<Bitmap?>? {
            var bitmaps = ArrayList<Bitmap?>()
            var fileDescriptor: ParcelFileDescriptor? = null
            try {
//                if (mUri != null)
//                    fileDescriptor = mActivity.contentResolver.openFileDescriptor(mUri, "r")
//                else
                if (mPath != null) fileDescriptor = ParcelFileDescriptor.open(File(mPath), ParcelFileDescriptor.MODE_READ_ONLY)
                if (fileDescriptor != null) {
                    val renderer = PdfRenderer(fileDescriptor)
                    bitmaps = getBitmaps(renderer)
                    // close the renderer
                    renderer.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
            }
            return bitmaps
        }
    }

    private class ReorderPdfPagesAsync1(
        private val mUri: Uri?,
        private val mPath: String?,
        private val mActivity: Activity,
        private val mOnPdfReorderedInterface: OnPdfReorderedInterface,
    ) : AsyncTask<String?, String?, java.util.ArrayList<Bitmap?>?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            mOnPdfReorderedInterface.onPdfReorderStarted()
        }

        public override fun doInBackground(vararg strings: String?): java.util.ArrayList<Bitmap?>? {
            var bitmaps = java.util.ArrayList<Bitmap?>()
            var fileDescriptor: ParcelFileDescriptor? = null
            try {
                if (mUri != null) fileDescriptor = mActivity.contentResolver.openFileDescriptor(mUri, "r") else if (mPath != null) fileDescriptor =
                    ParcelFileDescriptor.open(File(mPath), ParcelFileDescriptor.MODE_READ_ONLY)
                if (fileDescriptor != null) {
                    val renderer = PdfRenderer(fileDescriptor)
                    bitmaps = getBitmaps(renderer)
                    // close the renderer
                    renderer.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: java.lang.IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
            }
            return bitmaps
        }


        private fun getBitmaps(renderer: PdfRenderer): java.util.ArrayList<Bitmap?> {
            val bitmaps = java.util.ArrayList<Bitmap?>()
            val pageCount = renderer.pageCount
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height,
                    Bitmap.Config.ARGB_8888)
                // say we render for showing on the screen
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // do stuff with the bitmap
                bitmaps.add(bitmap)
                // close the page
                page.close()
            }
            return bitmaps
        }

        override fun onPostExecute(bitmaps: java.util.ArrayList<Bitmap?>?) {
            super.onPostExecute(bitmaps)
            if (bitmaps != null && !bitmaps.isEmpty()) {
                mOnPdfReorderedInterface.onPdfReorderCompleted(bitmaps)
            } else {
                mOnPdfReorderedInterface.onPdfReorderFailed()
            }
        }
    }
}