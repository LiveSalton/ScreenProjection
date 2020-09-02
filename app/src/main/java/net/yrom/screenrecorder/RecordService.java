package net.yrom.screenrecorder;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.hjq.toast.ToastUtils;
import com.salton123.app.BaseApplication;
import com.salton123.biz_record.bean.ProjectionProp;
import com.salton123.log.XLog;
import com.salton123.soulove.biz_record.R;

import net.yrom.screenrecorder.callback.IRecordAction;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.blankj.utilcode.util.ThreadUtils.runOnUiThread;

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/10 20:28
 * ModifyTime: 20:28
 * Description:
 */
public class RecordService extends Service implements IRecordAction {
    public ScreenRecorder mRecorder;
    public MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private static final String TAG = "BaseRecordService";
    private ProjectionProp mProp;

    @Override
    public void onCreate() {
        super.onCreate();
        XLog.i(TAG, "onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        XLog.i(TAG, "onStart");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        XLog.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public void updateProp(@NotNull ProjectionProp prop) {
        mProp = prop;
        onBundle(prop.getMediaProjection());
    }

    private void onBundle(MediaProjection mediaProjection) {
        mMediaProjection = mediaProjection;
        if (mMediaProjection == null) {
            Log.e("@@", "media projection is null");
            return;
        }
        mMediaProjection.registerCallback(mProjectionCallback, new Handler());
    }

    public boolean isRecording = false;

    public static boolean hasPermissions() {
        PackageManager pm = BaseApplication.sInstance.getPackageManager();
        String packageName = BaseApplication.sInstance.getPackageName();
        int granted = pm.checkPermission(RECORD_AUDIO, packageName)
                | pm.checkPermission(WRITE_EXTERNAL_STORAGE, packageName);
        return granted == PackageManager.PERMISSION_GRANTED;
    }

    private MediaProjection.Callback mProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            if (mRecorder != null) {
                stopRecorder();
            }
        }
    };

    private ScreenRecorder newRecorder(ProjectionProp prop) {
        final VirtualDisplay display =
                getOrCreateVirtualDisplay(prop.getMediaProjection(), prop.getVideoEncodeConfig());
        ScreenRecorder r = new ScreenRecorder(prop, display);
        r.setCallback(new ScreenRecorder.Callback() {
            @Override
            public void onStop(Throwable error) {
                runOnUiThread(() -> stopRecorder());
                if (error != null) {
                    ToastUtils.show("Recorder error ! See logcat for more details");
                    XLog.i(TAG, error.getMessage());
                    error.printStackTrace();
                    new File(prop.getSavePath()).delete();
                } else {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            .addCategory(Intent.CATEGORY_DEFAULT)
                            .setData(Uri.fromFile(new File(prop.getSavePath())));
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onRecording(long presentationTimeUs) {

            }
        });
        return r;
    }

    private VirtualDisplay getOrCreateVirtualDisplay(MediaProjection mediaProjection, VideoEncodeConfig config) {
        if (mVirtualDisplay == null) {
            mVirtualDisplay = mediaProjection.createVirtualDisplay("ScreenRecorder-display0",
                    config.width, config.height, 1 /*dpi*/,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    null /*surface*/, null, null);
        } else {
            // resize if size not matched
            Point size = new Point();
            mVirtualDisplay.getDisplay().getSize(size);
            if (size.x != config.width || size.y != config.height) {
                mVirtualDisplay.resize(config.width, config.height, 1);
            }
        }
        return mVirtualDisplay;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        XLog.i(TAG, "onDestroy");
        stopRecorder();
        if (mVirtualDisplay != null) {
            mVirtualDisplay.setSurface(null);
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new InnerBinder();
    }

    private RecordCallback mRecordCallback;

    public void setRecordCallback(RecordCallback callback) {
        this.mRecordCallback = callback;
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    public class InnerBinder extends Binder {
        public RecordService getService() {
            return RecordService.this;
        }
    }

    @Override
    public void startRecorder() {
        ForegroundNotificationUtils.startForegroundNotification(this);
        File saveFile = new File(mProp.getSavePath());
        FileUtils.createFileByDeleteOldFile(saveFile);
        mRecorder = newRecorder(mProp);
        if (hasPermissions()) {
            startRecordAction();
        } else {
            cancelRecorder();
        }
    }

    private void startRecordAction() {
        isRecording = true;
        if (mRecordCallback != null) {
            mRecordCallback.onStartRecord();
        }
        if (mRecorder == null) {
            return;
        }
        mRecorder.start();
    }

    @Override
    public void stopRecorder() {
        ForegroundNotificationUtils.deleteForegroundNotification(this);
        isRecording = false;
        if (mRecordCallback != null) {
            mRecordCallback.onStopRecord();
        }
        if (mRecorder != null) {
            mRecorder.quit();
        }
        mRecorder = null;
    }

    @Override
    public void cancelRecorder() {
        if (mRecorder == null) {
            return;
        }
        ToastUtils.show(getString(R.string.permission_denied_screen_recorder_cancel));
        stopRecorder();
    }

}
