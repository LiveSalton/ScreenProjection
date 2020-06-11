package net.yrom.screenrecorder;

import android.content.pm.PackageManager;

import com.hjq.toast.ToastUtils;
import com.salton123.app.BaseApplication;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * User: wujinsheng1@yy.com
 * Date: 2020/6/10 20:59
 * ModifyTime: 20:59
 * Description:
 */
public class XApp extends BaseApplication {
    public static String ACTION_STOP = BuildConfig.APPLICATION_ID + ".action.STOP";
    static {
        ForegroundNotificationUtils.setResId(R.drawable.ic_stat_recording);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
    }

    public static void toast(String message, Object... args) {
        ToastUtils.show((args.length == 0) ? message : String.format(message, args));
    }

    public static boolean hasPermissions() {
        PackageManager pm = BaseApplication.sInstance.getPackageManager();
        String packageName = BaseApplication.sInstance.getPackageName();
        int granted = pm.checkPermission(RECORD_AUDIO, packageName)
                | pm.checkPermission(WRITE_EXTERNAL_STORAGE, packageName);
        return granted == PackageManager.PERMISSION_GRANTED;
    }

}
