package net.yrom.screenrecorder;

import android.app.Service;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.salton123.log.XLog;

import net.yrom.screenrecorder.bean.ProjectionProp;

import java.io.File;

import androidx.annotation.Nullable;

import static com.blankj.utilcode.util.ThreadUtils.runOnUiThread;

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/10 20:28
 * ModifyTime: 20:28
 * Description:
 */
public class RecordService extends Service {
    public ScreenRecorder mRecorder;
    public MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private static final String TAG = "BaseRecordService";

    @Override
    public void onCreate() {
        super.onCreate();
        ForegroundNotificationUtils.startForegroundNotification(this);
        XLog.i(TAG, "onCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        XLog.i(TAG, "onStart");
        ForegroundNotificationUtils.startForegroundNotification(this);
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        XLog.i(TAG, "onStartCommand");
        ForegroundNotificationUtils.startForegroundNotification(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onBundle(MediaProjection mediaProjection) {
        mMediaProjection = mediaProjection;
        if (mMediaProjection == null) {
            Log.e("@@", "media projection is null");
            return;
        }
        mMediaProjection.registerCallback(mProjectionCallback, new Handler());
    }

    public boolean isRecording = false;

    public void startCapturing(ProjectionProp prop) {
        ForegroundNotificationUtils.startForegroundNotification(this);
        VideoEncodeConfig video = prop.getVideoEncodeConfig();
        AudioEncodeConfig audio = prop.getAudioEncodeConfig(); // audio can be null
        File saveFile = new File(prop.getSavePath());
        FileUtils.createFileByDeleteOldFile(saveFile);
        mRecorder = newRecorder(mMediaProjection, video, audio, saveFile);
        if (XApp.hasPermissions()) {
            startRecorder();
            isRecording = true;
        } else {
            cancelRecorder();
        }
    }

    private MediaProjection.Callback mProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            if (mRecorder != null) {
                stopRecorder();
            }
        }
    };

    private ScreenRecorder newRecorder(MediaProjection mediaProjection, VideoEncodeConfig video,
                                       AudioEncodeConfig audio, File output) {
        final VirtualDisplay display = getOrCreateVirtualDisplay(mediaProjection, video);
        ScreenRecorder r = new ScreenRecorder(video, audio, display, output.getAbsolutePath());
        r.setCallback(new ScreenRecorder.Callback() {
            @Override
            public void onStop(Throwable error) {
                runOnUiThread(() -> stopRecorder());
                if (error != null) {
                    XApp.toast("Recorder error ! See logcat for more details");
                    error.printStackTrace();
                    output.delete();
                } else {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            .addCategory(Intent.CATEGORY_DEFAULT)
                            .setData(Uri.fromFile(output));
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onRecording(long presentationTimeUs) {
                ForegroundNotificationUtils.startForegroundNotification(RecordService.this);
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

    class InnerBinder extends Binder {
        public RecordService getService() {
            return RecordService.this;
        }
    }

    public void startRecorder() {
        if (mRecordCallback != null) {
            mRecordCallback.onStartRecord();
        }
        if (mRecorder == null) {
            return;
        }
        mRecorder.start();
    }

    public void stopRecorder() {
        ForegroundNotificationUtils.deleteForegroundNotification(this);
        if (mRecordCallback != null) {
            mRecordCallback.onStopRecord();
        }
        if (mRecorder != null) {
            mRecorder.quit();
        }
        mRecorder = null;
        isRecording = false;
    }

    private void cancelRecorder() {
        if (mRecorder == null) {
            return;
        }
        XApp.toast(getString(R.string.permission_denied_screen_recorder_cancel));
        stopRecorder();
    }

}
