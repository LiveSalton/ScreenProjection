package net.yrom.screenrecorder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static net.yrom.screenrecorder.ScreenRecorder.VIDEO_AVC;

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/10 20:28
 * ModifyTime: 20:28
 * Description:
 */
class BaseRecordActivity extends Activity {
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mMediaProjectionManager;
    private ScreenRecorder mRecorder;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Notifications mNotifications;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaProjectionManager =
                (MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        mNotifications = new Notifications(getApplicationContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection == null) {
                Log.e("@@", "media projection is null");
                return;
            }

            mMediaProjection = mediaProjection;
            mMediaProjection.registerCallback(mProjectionCallback, new Handler());
            startCapturing(mediaProjection);
        }
    }

    public boolean isRecording = false;

    public void startCapturing(MediaProjection mediaProjection) {
        VideoEncodeConfig video = createVideoConfig();
        AudioEncodeConfig audio = createAudioConfig(); // audio can be null
        if (video == null) {
            XApp.toast(getString(R.string.create_screenRecorder_failure));
            return;
        }

        File dir = getSavingDir();
        if (!dir.exists() && !dir.mkdirs()) {
            cancelRecorder();
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
        final File file = new File(dir, "Screenshots-" + format.format(new Date())
                + "-" + video.width + "x" + video.height + ".mp4");
        Log.d("@@", "Create recorder with :" + video + " \n " + audio + "\n " + file);
        mRecorder = newRecorder(mediaProjection, video, audio, file);
        if (hasPermissions()) {
            startRecorder();
            isRecording = true;
        } else {
            cancelRecorder();
        }

    }

    public boolean hasPermissions() {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
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

    private ScreenRecorder newRecorder(MediaProjection mediaProjection, VideoEncodeConfig video,
                                       AudioEncodeConfig audio, File output) {
        final VirtualDisplay display = getOrCreateVirtualDisplay(mediaProjection, video);
        ScreenRecorder r = new ScreenRecorder(video, audio, display, output.getAbsolutePath());
        r.setCallback(new ScreenRecorder.Callback() {
            long startTime = 0;

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
                mNotifications.recording();
            }

            @Override
            public void onRecording(long presentationTimeUs) {
                if (startTime <= 0) {
                    startTime = presentationTimeUs;
                }
                long time = (presentationTimeUs - startTime) / 1000;
                mNotifications.recording();
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

    public AudioEncodeConfig createAudioConfig() {
        return null;
    }

    public VideoEncodeConfig createVideoConfig() {
        return null;
    }

    private static File getSavingDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "Screenshots");
    }

    public void toCapture() {
        if (mMediaProjection == null) {
            requestMediaProjection();
        } else {
            startCapturing(mMediaProjection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    void requestMediaProjection() {
        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_MEDIA_PROJECTION);
    }

    public void startRecorder() {
        if (mRecorder == null) {
            return;
        }
        mRecorder.start();
        registerReceiver(mStopActionReceiver, new IntentFilter(XApp.ACTION_STOP));
        moveTaskToBack(true);
    }

    public void stopRecorder() {
        mNotifications.clear();
        if (mRecorder != null) {
            mRecorder.quit();
        }
        mRecorder = null;
        try {
            unregisterReceiver(mStopActionReceiver);
        } catch (Exception e) {
            //ignored
        }
        isRecording = false;
    }

    private void cancelRecorder() {
        if (mRecorder == null) {
            return;
        }
        Toast.makeText(this, getString(R.string.permission_denied_screen_recorder_cancel), Toast.LENGTH_SHORT).show();
        stopRecorder();
    }

    public void stopRecordingAndOpenFile(Context context) {
        File file = new File(mRecorder.getSavedPath());
        stopRecorder();
        Toast.makeText(context, getString(R.string.recorder_stopped_saved_file) + " " + file, Toast.LENGTH_LONG).show();
        StrictMode.VmPolicy vmPolicy = StrictMode.getVmPolicy();
        try {
            // disable detecting FileUriExposure on public file
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
            viewResult(file);
        } finally {
            StrictMode.setVmPolicy(vmPolicy);
        }
    }

    private void viewResult(File file) {
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.addCategory(Intent.CATEGORY_DEFAULT);
        view.setDataAndType(Uri.fromFile(file), VIDEO_AVC);
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(view);
        } catch (Exception e) {
            // no activity can open this video
        }
    }

    private BroadcastReceiver mStopActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (XApp.ACTION_STOP.equals(intent.getAction())) {
                stopRecordingAndOpenFile(context);
            }
        }
    };

}
