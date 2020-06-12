package net.yrom.screenrecorder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.hjq.toast.ToastUtils;
import com.salton123.app.BaseApplication;
import com.salton123.log.XLog;

import java.util.ArrayList;
import java.util.List;

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

    public static List<Integer> getAppUid(Context context) {
        List<Integer> uids = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                // 系统应用
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(packageInfo.packageName, 0);
                    uids.add(ai.uid);
                    XLog.e("MicRecorder", "getAppUid:" + ai.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return uids;
    }

    // public static List<Integer> getAppUid() {
    //     ArrayList<Integer> uids = new ArrayList<>();
    //     PackageManager packageManager = BaseApplication.getInstance().getPackageManager();
    //     List<ApplicationInfo> packages = packageManager.getInstalledApplications(0);
    //     for (ApplicationInfo info : packages) {
    //         // uids.add(info.uid);
    //         if (info.flags == ApplicationInfo.FLAG_SYSTEM) {
    //             try {
    //                 ApplicationInfo item = packageManager.getApplicationInfo(info.packageName, 0);
    //                 uids.add(item.uid);
    //                 XLog.e("@@","getAppUid:"+item.packageName);
    //             } catch (PackageManager.NameNotFoundException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    //     return uids;
    // }
}
