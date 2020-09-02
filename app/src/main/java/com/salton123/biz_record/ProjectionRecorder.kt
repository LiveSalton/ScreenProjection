package com.salton123.biz_record

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import com.salton123.log.XLog
import java.io.File

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/9 17:21
 * ModifyTime: 17:21
 * Description:
 */
object ProjectionRecorder {
    const val TAG = "ProjectionRecorder"
    private var mediaProjection: MediaProjection? = null
    private var mediaRecorder: MediaRecorder? = null
    private var width = 720
    private var height = 1280
    private var densityDpi = 320
    private var mediaFile: File = File("")
    private var virtualDisplayMediaRecorder: VirtualDisplay? = null

    fun start(mediaProjection: MediaProjection?, displayMetrics: DisplayMetrics) {
        this.mediaProjection = mediaProjection
//        width = displayMetrics.widthPixels
//        height = displayMetrics.heightPixels
//        densityDpi = displayMetrics.densityDpi
//        releaseAll()
        // 创建保存文件
        mediaFile = File("/sdcard/z01/" + System.currentTimeMillis() + ".mp4")
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder?.setOutputFile(mediaFile.absolutePath)
        mediaRecorder?.setVideoSize(width, height)
        mediaRecorder?.setVideoEncodingBitRate(8388608)
        mediaRecorder?.setVideoFrameRate(30)
        mediaRecorder?.setOnErrorListener { _, what, extra ->
            XLog.e(TAG, "what:$what,extra:$extra")
        }
        mediaRecorder?.prepare()
        virtualDisplayMediaRecorder = mediaProjection?.createVirtualDisplay("MediaRecorder",
                width, height, densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder?.surface, object : VirtualDisplay.Callback() {
            override fun onPaused() {
                super.onPaused()
                XLog.i(TAG, "onPaused")
            }

            override fun onResumed() {
                super.onResumed()
                XLog.i(TAG, "onResumed")
            }

            override fun onStopped() {
                super.onStopped()
                XLog.i(TAG, "onStopped")
            }
        }, null)
        mediaRecorder?.start()
    }

    fun stop() {
        try {
            mediaRecorder?.apply {
                setOnErrorListener(null)
                setOnInfoListener(null)
                setPreviewDisplay(null)
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            virtualDisplayMediaRecorder?.release()
            virtualDisplayMediaRecorder = null
            mediaFile = File("")
            XLog.d(TAG, "stop success")
        } catch (tx: Throwable) {
            tx.printStackTrace()
        }
    }
}