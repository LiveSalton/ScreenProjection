package com.salton123.biz_record.callback

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.salton123.log.XLog
import tv.athena.live.component.projection.callback.Callback

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/5/29 11:48
 * ModifyTime: 11:48
 * Description:
 */
open class ProjectionServiceConnection(var callback: Callback) : ServiceConnection {
    private val TAG ="Projection"
    override fun onServiceDisconnected(name: ComponentName?) {
        XLog.i(TAG, "[onServiceDisconnected]")
        callback.onConnected(true, null, self())
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        XLog.i(TAG, "[onServiceConnected]")
        callback.onConnected(true, service, self())
    }

    private fun self(): ServiceConnection {
        return this@ProjectionServiceConnection
    }
}