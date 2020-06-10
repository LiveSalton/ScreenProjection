package net.yrom.screenrecorder;

import com.hjq.toast.ToastUtils;
import com.salton123.app.BaseApplication;

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

    public static void toast(String message, Object... args) {
        ToastUtils.show((args.length == 0) ? message : String.format(message, args));
    }
}
