package net.yrom.screenrecorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.DrawableRes;

import com.salton123.app.BaseApplication;
import com.salton123.biz_record.Config;
import com.salton123.soulove.biz_record.R;

import static android.os.Build.VERSION_CODES.O;

/**
 * User: newSalton@outlook.com
 * Date: 2019/3/25 10:25
 * ModifyTime: 10:25
 * Description:
 */
public class ForegroundNotificationUtilsBak {
    // 通知渠道的id
    private static String CHANNEL_ID = "Screen_sharing";
    private static int CHANNEL_POSITION = 1;
    private static String NotifyTitle = "Screen sharing";
    private static String NotifyContent = "The elephant print picture is sharing your screen.";
    private static int ResId = android.R.drawable.stat_notify_sync;
    private static Notification.Builder mBuilder;

    public static void setResId(@DrawableRes int resId) {
        ForegroundNotificationUtilsBak.ResId = resId;
    }

    public static void setNotifyTitle(String title) {
        if (null != title) {
            ForegroundNotificationUtilsBak.NotifyTitle = title;
        }
    }

    public static void setNotifyContent(String content) {
        if (null != content) {
            ForegroundNotificationUtilsBak.NotifyContent = content;
        }
    }

    public static void setChannelId(String channelId) {
        if (null != channelId) {
            ForegroundNotificationUtilsBak.CHANNEL_ID = channelId;
        }
    }

    public static void setChannelPosition(int position) {
        if (position >= 0) {
            ForegroundNotificationUtilsBak.CHANNEL_POSITION = position;
        }
    }

    public static void startForegroundNotification(Service service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //启动前台服务而不显示通知的漏洞已在 API Level 25 修复
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, NotifyTitle, NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //设置提示灯
            channel.setLightColor(Color.GREEN); //设置提示灯颜色
            channel.setShowBadge(true); //显示logo
            channel.setDescription(""); //设置描述
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); //设置锁屏可见 VISIBILITY_PUBLIC=可见
            channel.enableVibration(false);
            channel.setSound(null, null);
            getNotificationManager().createNotificationChannel(channel);
            //服务前台化只能使用startForeground()方法,不能使用 notificationManager.notify(1,notification);
            // 这个只是启动通知使用的,使用这个方法你只需要等待几秒就会发现报错了
        }
        Notification notification = getBuilder(service).build();
        service.startForeground(CHANNEL_POSITION, notification);
    }

    private static Notification.Builder getBuilder(Service service) {
        if (mBuilder == null) {
            Notification.Builder builder = new Notification.Builder(service)
                    .setContentTitle(NotifyTitle)//设置标题
                    .setContentText(NotifyContent)//设置内容
                    .setOngoing(true)
                    .setLocalOnly(true)
                    .setOnlyAlertOnce(true)
                    .addAction(stopAction())
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(ResId);
            if (Build.VERSION.SDK_INT >= O) {
                builder.setChannelId(CHANNEL_ID)
                        .setUsesChronometer(true);
            }
            mBuilder = builder;
        }
        return mBuilder;
    }

    private static NotificationManager getNotificationManager() {
        NotificationManager notificationManager =
                (NotificationManager) BaseApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager;
    }

    public static void deleteForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = getNotificationManager().getNotificationChannel(CHANNEL_ID);
            if (null != mChannel) {
                getNotificationManager().deleteNotificationChannel(CHANNEL_ID);
            }
        } else {
            getNotificationManager().cancel(CHANNEL_POSITION);
        }
        mStopAction = null;
    }

    private static Notification.Action mStopAction;

    private static Notification.Action stopAction() {
        if (mStopAction == null) {
            Intent intent = new Intent(Config.ACTION_STOP)
                    .setPackage(BaseApplication.sInstance.getPackageName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(BaseApplication.sInstance, 1,
                    intent, PendingIntent.FLAG_ONE_SHOT);
            mStopAction =
                    new Notification.Action(android.R.drawable.ic_media_pause,
                            BaseApplication.sInstance.getString(R.string.stop), pendingIntent);
        }
        return mStopAction;
    }

}
