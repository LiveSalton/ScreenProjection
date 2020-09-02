package com.salton123.biz_record.bean

import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import com.salton123.app.BaseApplication
import com.salton123.biz_record.Config
import com.salton123.biz_record.ScreenRecordDaemon
import com.salton123.soulove.biz_record.R
import com.salton123.util.PreferencesUtils
import com.salton123.util.ScreenUtils
import net.yrom.screenrecorder.AudioEncodeConfig
import net.yrom.screenrecorder.VideoEncodeConfig

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/9 19:40
 * ModifyTime: 19:40
 * Description:
 */
data class ProjectionProp(
        var savePath: String = "/sdcard/projection/" + System.currentTimeMillis() + ".mp4",
        var mediaProjection: MediaProjection? = null,
        var audioEncodeConfig: AudioEncodeConfig? = createAudioConfig(),
        var videoEncodeConfig: VideoEncodeConfig? = createVideoConfig()
) {

    companion object {
        fun createAudioConfig(): AudioEncodeConfig? {
            return AudioEncodeConfig("c2.android.aac.encoder",
                    MediaFormat.MIMETYPE_AUDIO_AAC,
                    80000, 44100, 1,
                    MediaCodecInfo.CodecProfileLevel.AACObjectMain)
        }

        fun createVideoConfig(): VideoEncodeConfig? {
            val indexOfVideoEncoder = PreferencesUtils.getInt(BaseApplication.sInstance,
                    Config.getKeyName(R.id.sb_video_encoder), 0)
            val encoderName = ScreenRecordDaemon.findVideoEncoders().value?.get(indexOfVideoEncoder)?.name
                    ?: "OMX.qcom.video.encoder.avc"

            val indexOfResolution = PreferencesUtils.getInt(BaseApplication.sInstance,
                    Config.getKeyName(R.id.sb_resolution), 0)
            val resolution = ScreenRecordDaemon.findResolutions().value?.get(indexOfResolution)
            val width = resolution?.first?:ScreenUtils.getScreenWidth()
            val height = resolution?.second?:ScreenUtils.getRealHeightPixels(BaseApplication.sInstance)
            val bitrates = Config.getStringArray(R.array.video_bitrates)[
                    PreferencesUtils.getInt(BaseApplication.sInstance,
                            Config.getKeyName(R.id.sb_video_bitrate), 0)]
            return VideoEncodeConfig(width, height,
                    bitrates.toInt() * 1000,
                    15, 5, encoderName,
                    MediaFormat.MIMETYPE_VIDEO_AVC, null
            )
        }
    }
}