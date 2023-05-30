package com.remi.pdfscanner.engine

import android.R
import android.app.Activity
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.*

//import static swati4star.createpdf.util.Constants.pdfDirectory;
/**
 * Created by anandparmar on 18/06/18.
 */
object StringUtils  {

    fun isEmpty(s: CharSequence?): Boolean {
        return s == null || s.toString().trim { it <= ' ' } == ""
    }

    fun isNotEmpty(s: CharSequence?): Boolean {
        return s != null && s.toString().trim { it <= ' ' } != ""
    }

    fun showSnackbar(context: Activity, resID: Int) {
        Snackbar.make(Objects.requireNonNull(context).findViewById(R.id.content),
            resID, Snackbar.LENGTH_LONG).show()
    }

    fun showSnackbar(context: Activity, resID: String?) {
        Snackbar.make(Objects.requireNonNull(context).findViewById(R.id.content),
            resID!!, Snackbar.LENGTH_LONG).show()
    }

    fun showIndefiniteSnackbar(context: Activity, resID: String?): Snackbar {
        return Snackbar.make(Objects.requireNonNull(context).findViewById(R.id.content),
            resID!!, Snackbar.LENGTH_INDEFINITE)
    }

    fun getSnackbarwithAction(context: Activity, resID: Int): Snackbar {
        return Snackbar.make(Objects.requireNonNull(context).findViewById(R.id.content),
            resID, Snackbar.LENGTH_LONG)
    }
    fun getDefaultStorageLocation(): String? {
        val dir = File(Environment.getExternalStorageDirectory().absolutePath,
            pdfDirectory)
        if (!dir.exists()) {
            val isDirectoryCreated = dir.mkdir()
            if (!isDirectoryCreated) {
                Log.e("Error", "Directory could not be created")
            }
        }
        return dir.absolutePath + File.separator
    }
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }



    /**
     * if text is empty according to [StringUtils.isEmpty] returns the default,
     * if text is not empty, parses the text according to [Integer.parseInt]
     * @param text the input text
     * @param def the default value
     * @return the text parsed to an int or the default value
     * @throws NumberFormatException if the text is not empty and not formatted as an int
     */
    @Throws(NumberFormatException::class)
    fun parseIntOrDefault(text: CharSequence, def: Int): Int {
        return if (isEmpty(text)) def else text.toString().toInt()
    }


        const val pdfDirectory = "/PDF Converter/"

}