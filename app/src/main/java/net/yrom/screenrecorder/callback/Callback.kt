package net.yrom.screenrecorder.callback

import android.content.ServiceConnection
import android.os.IBinder

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/5/29 14:31
 * ModifyTime: 14:31
 * Description:
 */
interface Callback {
    fun onConnected(isConnected: Boolean, service: IBinder?, connection: ServiceConnection?)
}