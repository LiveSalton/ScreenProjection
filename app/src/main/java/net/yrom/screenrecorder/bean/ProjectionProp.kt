package net.yrom.screenrecorder.bean

import android.content.Intent
import android.media.projection.MediaProjection
import net.yrom.screenrecorder.AudioEncodeConfig
import net.yrom.screenrecorder.VideoEncodeConfig

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/9 19:40
 * ModifyTime: 19:40
 * Description:
 */
data class ProjectionProp(
        var data: Intent,
        var mediaProjection: MediaProjection,
        var audioEncodeConfig: AudioEncodeConfig,
        var videoEncodeConfig: VideoEncodeConfig,
        var savePath: String
)