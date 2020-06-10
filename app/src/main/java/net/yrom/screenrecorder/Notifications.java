/*
 * Copyright (c) 2017 Yrom Wang <http://www.yrom.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.yrom.screenrecorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import static android.os.Build.VERSION_CODES.O;

class Notifications extends ContextWrapper {
    private static int CHANNEL_POSITION = 0x1fff;
    private static String CHANNEL_ID = "Screen sharing";
    private static String NotifyTitle = "Screen sharing";
    private static String NotifyContent = "The elephant print picture is sharing your screen.";
    private static int ResId = R.drawable.ic_stat_recording;

    private NotificationManager mManager;
    private Notification.Action mStopAction;
    private Notification.Builder mBuilder;
    private Context context;

    Notifications(Context context) {
        super(context);
        this.context = context;
        if (Build.VERSION.SDK_INT >= O) {
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, NotifyTitle, NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            getNotificationManager().createNotificationChannel(channel);
        }
    }

    public void recording() {
        Notification notification = getBuilder()
                .setContentText(NotifyContent)
                .build();
        getNotificationManager().notify(CHANNEL_POSITION, notification);
        if (context instanceof Service) {
            ((Service) context).startForeground(CHANNEL_POSITION, notification);
        }
    }

    private Notification.Builder getBuilder() {
        if (mBuilder == null) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle(NotifyTitle)
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

    private Notification.Action stopAction() {
        if (mStopAction == null) {
            Intent intent = new Intent(XApp.ACTION_STOP).setPackage(getPackageName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                    intent, PendingIntent.FLAG_ONE_SHOT);
            mStopAction =
                    new Notification.Action(android.R.drawable.ic_media_pause, getString(R.string.stop), pendingIntent);
            // mStopAction = new Notification.Action.Builder(new Notification.Action(android.R.drawable.ic_media_pause,
            //         getString(R.string.stop), pendingIntent))
            //         .build();
        }
        return mStopAction;
    }

    void clear() {
        mBuilder = null;
        mStopAction = null;
        getNotificationManager().cancel(CHANNEL_POSITION);
    }

    NotificationManager getNotificationManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }
}
