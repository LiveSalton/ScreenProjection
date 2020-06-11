package net.yrom.screenrecorder.callback

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.salton123.log.XLog

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/5/29 11:48
 * ModifyTime: 11:48
 * Description:
 */
open class ProjectionServiceConnection(var callback: Callback) : ServiceConnection {
    private val TAG = "ProjectionConnection"
    override fun onServiceDisconnected(name: ComponentName?) {
        XLog.i(TAG, "[onServiceDisconnected]")
        callback.onConnected(true, null, self())
    }

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        XLog.i(TAG, "[onServiceConnected]")
        callback.onConnected(true, binder, self())
    }

    private fun self(): ServiceConnection {
        return this@ProjectionServiceConnection
    }
}