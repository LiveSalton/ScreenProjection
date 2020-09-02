package com.salton123.biz_record.mvp

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.ActivityUtils
import com.salton123.arch.mvp.BindPresenter
import com.salton123.biz_record.bean.ProjectionCode
import com.salton123.biz_record.bean.ProjectionProp
import com.salton123.biz_record.bean.RecordItem
import com.salton123.biz_record.db.RecordDatabase
import com.salton123.biz_record.ui.MediaProjectionDelegate
import com.salton123.biz_record.utils.CountDownTicker
import com.salton123.log.XLog
import com.salton123.utils.RxUtilCompat
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import net.yrom.screenrecorder.RecordService
import net.yrom.screenrecorder.callback.Callback
import net.yrom.screenrecorder.callback.IRecordAction
import net.yrom.screenrecorder.callback.ProjectionServiceConnection
import java.util.concurrent.TimeUnit

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/8 5:01 PM
 * ModifyTime: 5:01 PM
 * Description:
 */
@SuppressLint("CheckResult")
class RecordPresenter : BindPresenter<IRecordView>(), IRecordAction {
    private var mProp: ProjectionProp = ProjectionProp()
    private var resultCode: Int = 0
    private var data: Intent? = Intent()
    private var isGranted: Boolean = false
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mService: RecordService? = null
    private val TAG = "RecordPresenter"
    private val mConnection = ProjectionServiceConnection(object : Callback {
        override fun onConnected(isConnected: Boolean, binder: IBinder?, connection: ServiceConnection?) {
            if (isConnected && binder != null) {
                mService = (binder as RecordService.InnerBinder).service
                mService?.setRecordCallback(mView)
            }
        }
    })

    override fun attachView(view: IRecordView?) {
        super.attachView(view)
        //绑定录屏服务
        view?.activity()?.apply {
            XLog.i(TAG, "bindService")
            bindService(
                    Intent(this, RecordService::class.java),
                    mConnection,
                    Service.BIND_AUTO_CREATE
            )
        }
        mediaProjectionManager =
                view?.activity()?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }

    override fun detachView() {
        super.detachView()
        mView?.activity()?.apply {
            XLog.i(TAG, "unbindService")
            unbindService(mConnection)
            stopService(Intent(this, RecordService::class.java))
        }
    }

    fun hasProjection(): Boolean {
        return isGranted
    }

    fun requestProjection() {
        if (!isGranted) {
            mView?.let {
                MediaProjectionDelegate(it as FragmentActivity).requestProjection { ret, resultCode, data ->
                    isGranted = ret == ProjectionCode.CODE_GET_PROJECTION_SUCCEED
                    if (data != null) {
                        this.resultCode = resultCode
                        this.data = data
                        mProp.mediaProjection =
                                data?.let { it1 -> mediaProjectionManager?.getMediaProjection(resultCode, it1) }
                        updateProp(mProp)
                        startRecorder()
                    } else {
                        it.shortToast("cancel record")
                    }
                }
            }
        }
    }

    override fun updateProp(prop: ProjectionProp) {
        mService?.updateProp(prop)
    }

    private var mCountDownTicker: CountDownTicker? = null
    override fun startRecorder() {
        mCountDownTicker = CountDownTicker()
        mCountDownTicker?.start(object : CountDownTicker.Callback {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                mService?.startRecorder()
            }
        })
        //最小化
        ActivityUtils.startHomeActivity()
    }

    override fun stopRecorder() {
        mService?.stopRecorder()
        mView?.shortToast("结束录屏")
        addNewRecord()
    }

    private fun addNewRecord() {
        Observable.create(ObservableOnSubscribe<Boolean> {
            val videoWidth = mProp.videoEncodeConfig?.width ?: 0
            val videoHeight = mProp.videoEncodeConfig?.height ?: 0
            RecordDatabase.mInstance.getRecordDao().insert(RecordItem(mProp.savePath,
                    mProp.savePath.replace("mp4", "jpg"),
                    videoWidth, videoHeight))
            it.onComplete()
            XLog.i(TAG, "videoWidth:$videoWidth,videoHeight:$videoHeight")
        }).delay(1000, TimeUnit.MICROSECONDS).compose(RxUtilCompat.schedulersIO()).subscribe({
            XLog.i(TAG, "save record:$it")
        }, {
            XLog.e(TAG, it.message)
        }, {})
    }

    override fun cancelRecorder() {
        mService?.cancelRecorder()
    }

    override fun isRecording(): Boolean {
        return mService?.isRecording() ?: false
    }
}