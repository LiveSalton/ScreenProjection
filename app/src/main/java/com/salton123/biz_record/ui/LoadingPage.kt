package com.salton123.biz_record.ui

import android.os.CountDownTimer
import com.salton123.biz_record.Config
import com.salton123.soulove.biz_record.R
import com.salton123.ui.base.BaseActivity
import kotlinx.android.synthetic.main.page_loading.*

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/7/27 17:07
 * ModifyTime: 17:07
 * Description:
 */
class LoadingPage : BaseActivity() {

    private var mCountdownTimer: CountDownTimer? = null
    override fun initViewAndData() {
        val countdownTime = intent?.getIntExtra(Config.BUNDLE_KEY_COUNTDOWN_TIME, 5) ?: 5
        tvCountdown.text = "$countdownTime"
        mCountdownTimer = object : CountDownTimer(countdownTime.times(1000L), 1000) {
            override fun onFinish() {
                finish()
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }
        mCountdownTimer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountdownTimer?.cancel()
        mCountdownTimer = null
    }

    override fun getLayout(): Int = R.layout.page_loading
}