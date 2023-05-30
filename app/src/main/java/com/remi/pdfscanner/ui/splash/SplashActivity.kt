package com.remi.pdfscanner.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.remi.pdfscanner.base.BaseActivity
import com.remi.pdfscanner.databinding.ActivitySplashBinding
import com.remi.pdfscanner.ui.main.MainActivity
import com.remi.pdfscanner.util.setSize
import java.util.*
import java.util.concurrent.TimeUnit

class SplashActivity:BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {


    override fun setSize() {
        binding.tvNameApp.setSize(33)
        binding.tvAction.setSize(14)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val timer = object: CountDownTimer(2000, 20) {
            override fun onTick(millisUntilFinished: Long) {
                binding.seekbarSplash.progress = ((2000-millisUntilFinished)).toInt()/20
                binding.seekbarSplash.invalidate()
            }

            override fun onFinish() {
                startActivity(Intent(this@SplashActivity,MainActivity::class.java))
                finish()
            }
        }
        timer.start()


    }
    fun getTimeZone(){
        val mCalendar: Calendar = GregorianCalendar()
        val mTimeZone: TimeZone = mCalendar.getTimeZone()
        val mGMTOffset = mTimeZone.rawOffset.toLong()
        Log.e("~~~","GMT offset is ${TimeUnit.HOURS.convert(mGMTOffset,TimeUnit.MILLISECONDS)} hours")
    }
}