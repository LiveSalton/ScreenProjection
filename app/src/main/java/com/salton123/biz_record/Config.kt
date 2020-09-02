package com.salton123.biz_record

import com.salton123.app.BaseApplication
import com.salton123.soulove.biz_record.BuildConfig
import com.salton123.soulove.biz_record.R
import net.yrom.screenrecorder.ForegroundNotificationUtils

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/7/21 20:37
 * ModifyTime: 20:37
 * Description:
 */
object Config {
    @JvmField
    var ACTION_STOP = BuildConfig.APPLICATION_ID + ".action.STOP"

    const val BUNDLE_KEY_COUNTDOWN_TIME = "bundle_key_countdown_time"

    init {
        ForegroundNotificationUtils.setResId(R.drawable.ic_stat_recording)
    }

    fun getKeyName(resId: Int): String? {
        return BaseApplication.sInstance.resources.getResourceEntryName(resId)
    }

    fun getStringArray(arrayId: Int): Array<String> {
        return BaseApplication.sInstance.resources.getStringArray(arrayId)
    }

    fun getIntArray(arrayId: Int):IntArray {
        return BaseApplication.sInstance.resources.getIntArray(arrayId)
    }
}