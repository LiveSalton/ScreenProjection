package com.salton123.biz_record

import android.annotation.SuppressLint
import android.media.MediaCodecInfo
import android.media.MediaFormat
import androidx.lifecycle.MutableLiveData
import com.salton123.app.BaseApplication
import com.salton123.biz_record.bean.ProjectionProp
import com.salton123.biz_record.utils.ScreenRecordUtils
import com.salton123.log.XLog
import com.salton123.util.ScreenUtils
import io.reactivex.Observable

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/8/25 14:20
 * ModifyTime: 14:20
 * Description:录屏守护
 */
@SuppressLint("CheckResult")
object ScreenRecordDaemon {
    const val TAG = "ScreenRecordDaemon"

    private val mMediaCodecRet: MutableLiveData<Array<MediaCodecInfo>> = MutableLiveData()
    private val mResolutionRet: MutableLiveData<Array<Pair<Int, Int>>> = MutableLiveData()  //分辨率
    private val mProjectionProp: MutableLiveData<ProjectionProp> = MutableLiveData()

    init {
        Observable.create<Array<MediaCodecInfo>> { emitter ->
            ScreenRecordUtils.findEncodersByType(MediaFormat.MIMETYPE_VIDEO_AVC)?.let {
                emitter.onNext(it)
            }
        }.subscribe({
            mMediaCodecRet.value = it
        }, {
            XLog.i(TAG, "ex:$it")
        })

        val rawWidth = ScreenUtils.getScreenWidth()
        val rawHeigt = ScreenUtils.getRealHeightPixels(BaseApplication.sInstance)
        val mResolutionList: Array<Pair<Int, Int>> = arrayOf(
                Pair(rawWidth, rawHeigt),
                Pair(rawWidth * 4 / 5, rawHeigt * 4 / 5),
                Pair(rawWidth * 1 / 2, rawHeigt * 1 / 2),
                Pair(rawWidth * 2 / 5, rawHeigt * 2 / 5)
        )
        mResolutionRet.value = mResolutionList
        XLog.i(TAG, "init media codec info")
    }

    fun findVideoEncoders(): MutableLiveData<Array<MediaCodecInfo>> {
        return mMediaCodecRet
    }

    fun findResolutions():MutableLiveData<Array<Pair<Int, Int>>>{
        return mResolutionRet
    }
}