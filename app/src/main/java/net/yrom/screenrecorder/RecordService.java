package net.yrom.screenrecorder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/10 21:39
 * ModifyTime: 21:39
 * Description:
 */
class RecordService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
