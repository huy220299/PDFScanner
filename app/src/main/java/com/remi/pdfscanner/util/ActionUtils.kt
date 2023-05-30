package com.remi.pdfscanner.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.remi.pdfscanner.R

object ActionUtils {
    private const val POLICY = "https://remibirthdayframe.blogspot.com/2023/02/privacy-policy.html"
    private const val HOW_TO_USE = ""
    private const val ID_DEV = "5909948963005668451"
    private const val LINK_FACE = "https://www.facebook.com/REMI-Studio-111335474750038"
    private const val LINK_FACE_ID = "111335474750038"
    private const val LINK_INS = "remi_studio_app"
    fun openOtherApps(c: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uriBuilder = Uri.parse("https://play.google.com/store/apps/dev")
            .buildUpon()
            .appendQueryParameter("id", ID_DEV)
        intent.data = uriBuilder.build()
        try {
            c.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun openPolicy(c: Context) {
        openLink(c, POLICY)
    }

    fun openHowToUse(c: Context) {
        openLink(c, HOW_TO_USE)
    }

    fun openFacebook(c: Context) {
        try {
            val applicationInfo = c.packageManager.getApplicationInfo("com.facebook.katana", 0)
            if (applicationInfo.enabled) {
                val uri = Uri.parse("fb://page/$LINK_FACE_ID")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                c.startActivity(intent)
                return
            }
        } catch (ignored: Exception) {
        }
        openLink(c, LINK_FACE)
    }

    fun openInstagram(c: Context) {
        val appUri = Uri.parse("https://instagram.com/_u/$LINK_INS")
        try {
            val appIntent = c.packageManager.getLaunchIntentForPackage("com.instagram.android")
            if (appIntent != null) {
                appIntent.action = Intent.ACTION_VIEW
                appIntent.data = appUri
                c.startActivity(appIntent)
                return
            }
        } catch (ignored: Exception) {
        }
        openLink(c, "https://instagram.com/$LINK_INS")
    }

    fun openLink(c: Context, url: String) {
        try {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url.replace("HTTPS", "https"))
            c.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(c, "No browser!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     *@author huy
     */

    fun sendFeedback(context: Context) {
        val selectorIntent = Intent(Intent.ACTION_SENDTO)
        selectorIntent.data = Uri.parse("mailto:")
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(Constant.EMAIL))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name)+" Feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")
        emailIntent.selector = selectorIntent
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."))
        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send email using..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "No email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * @author huy
     */
    fun shareApp(context: Context) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "\"\"for You.")
            var shareMessage = "\nLet me recommend you this application.\n. Download now:\n\n"
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + context.packageName
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }
}