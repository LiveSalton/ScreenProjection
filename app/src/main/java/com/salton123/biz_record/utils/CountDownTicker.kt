package com.salton123.biz_record.utils

import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.salton123.app.BaseApplication
import com.salton123.log.XLog
import com.salton123.soulove.biz_record.R

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/8/6 9:59
 * ModifyTime: 9:59
 * Description:
 */
class CountDownTicker {
    private val TAG = "CountdownTicker"
    private var delayTime: Long = 3000
    private var intervalTime: Long = 1000
    private var mCountDownTimer: CountDownTimer? = null
    private var countDownToast: Toast = Toast.makeText(
            BaseApplication.sInstance,
            "", Toast.LENGTH_LONG).apply {
        setGravity(Gravity.CENTER, 0, 0)
        duration = (delayTime / 1000).toInt()
        val toShowView = LayoutInflater.from(BaseApplication.sInstance)
                .inflate(R.layout.page_loading, null)
        view = toShowView
    }

    fun start(callback: Callback?) {
        mCountDownTimer?.cancel()
        mCountDownTimer = object : CountDownTimer(delayTime, intervalTime) {
            override fun onFinish() {
                countDownToast?.apply {
                    view.visibility = View.GONE
                    cancel()
                }
                callback?.onFinish()
            }

            override fun onTick(millisUntilFinished: Long) {
                XLog.i(TAG, "countDownTime:$millisUntilFinished")
                countDownToast?.apply {
                    view.findViewById<TextView>(R.id.tvCountdown)
                            .text = ((millisUntilFinished / 1000) + 1).toString()
                    show()
                }
                callback?.onTick(millisUntilFinished)
            }
        }
        mCountDownTimer?.start()
    }

    fun stop() {
        mCountDownTimer?.cancel()
        mCountDownTimer = null
    }

    interface Callback {
        fun onTick(millisUntilFinished: Long)
        fun onFinish()
    }
}