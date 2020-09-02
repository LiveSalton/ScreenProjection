package com.salton123.biz_record.utils

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.util.DisplayMetrics
import android.view.WindowManager
import com.salton123.log.XLog
import java.util.ArrayList

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/8/25 15:25
 * ModifyTime: 15:25
 * Description:
 */
object ScreenRecordUtils {
    /**
     * Find an encoder supported specified MIME type
     *
     * @return Returns empty array if not found any encoder supported specified MIME type
     */
    fun findEncodersByType(mimeType: String): Array<MediaCodecInfo>? {
        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val infos: MutableList<MediaCodecInfo> = ArrayList()
        for (info in codecList.codecInfos) {
            if (!info.isEncoder) {
                continue
            }
            try {
                val cap = info.getCapabilitiesForType(mimeType) ?: continue
            } catch (e: IllegalArgumentException) {
                // unsupported
                continue
            }
            infos.add(info)
        }
        return infos.toTypedArray()
    }

    fun getRealHeightPixels(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var height = 0
        val display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        val c: Class<*>
        try {
            c = Class.forName("android.view.Display")
            val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            method.invoke(display, dm)
            height = dm.heightPixels
        } catch (e: Exception) {
            XLog.e("123", e.toString())
        }
        return height
    }
}