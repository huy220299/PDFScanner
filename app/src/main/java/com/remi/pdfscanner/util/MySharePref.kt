package com.remi.pdfscanner.util

import android.content.Context
import android.content.SharedPreferences
import com.remi.pdfscanner.ui.setting.OptionOrientation
import com.remi.pdfscanner.ui.setting.OptionSize

object MySharePref {
    private const val manualCrop = "manual_crop"
    private const val optionOrientation = "option_orientation"
    private const val optionSize = "option_size"
    private const val languageORC = "language_orc"
    private fun getDefaultSharePreference(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    fun rate(context: Context) {
        val editor = getDefaultSharePreference(context).edit()
        editor.putBoolean("rated", true)
        editor.apply()
    }

    fun getRated(context: Context): Boolean {
        return getDefaultSharePreference(context).getBoolean("rated", false)
    }

    fun isManualCrop(context: Context): Boolean {
        return getDefaultSharePreference(context).getBoolean(manualCrop, false)
    }

    fun updateManualCrop(context: Context, boolean: Boolean) {
        getDefaultSharePreference(context).edit().putBoolean(manualCrop, boolean).apply()
    }

    /**
     * get default orientation of image when create file PDF
     */
    fun getOptionOrientation(context: Context): String? {
        return getDefaultSharePreference(context).getString(optionOrientation, OptionOrientation.AUTO.name)
    }

    fun updateOptionOrientation(context: Context, string: String) {
        getDefaultSharePreference(context).edit().putString(optionOrientation,string).apply()
    }


    fun getOptionSize(context: Context): String? {
        return getDefaultSharePreference(context).getString(optionSize, OptionSize.A4.name)
    }

    fun updateOptionSize(context: Context, string: String) {
        getDefaultSharePreference(context).edit().putString(optionSize,string).apply()
    }

    fun getLanguageORC(context: Context):Int{
        return getDefaultSharePreference(context).getInt(languageORC,0)
    }
    fun updateLanguageORC(context: Context,posLanguage:Int){
        getDefaultSharePreference(context).edit().putInt(languageORC,posLanguage).apply()
    }

}