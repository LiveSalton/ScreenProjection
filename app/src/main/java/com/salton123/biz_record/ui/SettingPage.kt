package com.salton123.biz_record.ui

import android.media.MediaCodecInfo
import android.view.View
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.hjq.toast.ToastUtils
import com.hjq.widget.layout.SettingBar
import com.lxj.xpopup.XPopup
import com.salton123.app.BaseApplication
import com.salton123.biz_record.Config.getKeyName
import com.salton123.biz_record.Config.getStringArray
import com.salton123.biz_record.ScreenRecordDaemon
import com.salton123.soulove.biz_record.R
import com.salton123.soulove.common.Constants
import com.salton123.ui.base.BaseActivity
import com.salton123.util.PreferencesUtils
import kotlinx.android.synthetic.main.page_setting.*

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/1 17:39
 * ModifyTime: 17:39
 * Description:
 */
@Route(path = Constants.Router.Record.SETTING)
class SettingPage : BaseActivity() {
    companion object {
        val SETTING_SAVE_PATH = BaseApplication.getInstance<BaseApplication>()
                .getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES)
    }

    override fun getLayout(): Int = R.layout.page_setting
    override fun initViewAndData() {
    }

    private var mEncoderList: Array<MediaCodecInfo>? = null
    private var mResolutionList: Array<Pair<Int, Int>>? = null
    override fun initListener() {
        super.initListener()
        setListener(sb_resolution, sb_video_bitrate, sb_countdown, sb_audio, sb_save_path,
                sb_video_encoder)
        sb_save_path.rightText = SETTING_SAVE_PATH?.name
        updateRightText(sb_video_bitrate, R.array.video_bitrates)
        updateRightText(sb_audio, R.array.audio)
        val indexOfVideoEncoder = PreferencesUtils.getInt(BaseApplication.sInstance,
                getKeyName(R.id.sb_video_encoder), 0)
        ScreenRecordDaemon.findVideoEncoders().observe(this, Observer {
            mEncoderList = it
            val encoderName = mEncoderList?.get(indexOfVideoEncoder)?.name ?: "OMX.qcom.video.encoder.avc"
            sb_video_encoder.rightText = encoderName
        })
        val indexOfResolution = PreferencesUtils.getInt(BaseApplication.sInstance, getKeyName(R.id.sb_resolution), 0)
        ScreenRecordDaemon.findResolutions().observe(this, Observer {
            mResolutionList = it
            val encoderName = mResolutionList?.get(indexOfResolution)
            sb_resolution.rightText = "${encoderName?.first}x${encoderName?.second}"
        })
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v) {
            sb_resolution -> {
                val info = mResolutionList?.map { "${it.first}x${it.second}" }?.toTypedArray()
                XPopup.Builder(sb_video_encoder.context)
                        .asBottomList(getString(R.string.resolution), info)
                        { position, text ->
                            shortToast(text)
                            toSave(sb_video_encoder, position)
                            sb_video_encoder.rightText = text
                        }.show()
            }
            sb_video_bitrate -> {
                toShowChooser(
                        v!! as SettingBar,
                        getString(R.string.bitrate),
                        R.array.video_bitrates
                )
            }
            sb_audio -> {
                toShowChooser(
                        v!! as SettingBar,
                        getString(R.string.audio),
                        R.array.audio
                )
            }
            sb_save_path -> {
                ToastUtils.show(getString(R.string.unsupport_change_dir))
            }
            sb_video_encoder -> {
                val info = mEncoderList?.map { it.name }?.toTypedArray()
                XPopup.Builder(sb_video_encoder.context)
                        .asBottomList(getString(R.string.video_encoder), info)
                        { position, text ->
                            shortToast(text)
                            toSave(sb_video_encoder, position)
                            sb_video_encoder.rightText = text
                        }.show()
            }
        }
    }

    private fun toShowChooser(view: SettingBar, title: String, arrayId: Int) {
        XPopup.Builder(view.context)
                .asBottomList(title, getStringArray(arrayId))
                { position, text ->
                    shortToast(text)
                    toSave(view, position)
                    updateRightText(view, arrayId)
                }.show()
    }

    private fun toSave(view: View, value: Int) {
        getKeyName(view.id)?.let {
            PreferencesUtils.putInt(this, it, value)
        }
    }

    private fun updateRightText(view: SettingBar, arrayId: Int) {
        view.rightText = getStringArray(arrayId)[PreferencesUtils.getInt(this, getKeyName(view.id), 0)]
    }
}