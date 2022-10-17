package com.xiaomi.xmsf.push.notification;

import static top.trumeet.common.utils.NotificationUtils.EXTRA_CHANNEL_DESCRIPTION;
import static top.trumeet.common.utils.NotificationUtils.EXTRA_CHANNEL_ID;
import static top.trumeet.common.utils.NotificationUtils.EXTRA_CHANNEL_NAME;
import static top.trumeet.common.utils.NotificationUtils.EXTRA_SOUND_URL;
import static top.trumeet.common.utils.NotificationUtils.getChannelIdByPkg;
import static top.trumeet.common.utils.NotificationUtils.getExtraField;
import static top.trumeet.common.utils.NotificationUtils.getGroupIdByPkg;
import static top.trumeet.common.utils.NotificationUtils.getPackageName;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.IconCompat;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.xiaomi.push.service.MIPushNotificationHelper;
import com.xiaomi.xmpush.thrift.PushMetaInfo;
import com.xiaomi.xmpush.thrift.XmPushActionContainer;
import com.xiaomi.xmsf.R;
import com.xiaomi.xmsf.utils.ColorUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import top.trumeet.common.cache.ApplicationNameCache;
import top.trumeet.common.cache.IconCache;

/**
 * @author Trumeet
 * @date 2018/1/25
 */

public class NotificationController {
    private static final Logger logger = XLog.tag("NotificationController").build();

    private static final String NOTIFICATION_LARGE_ICON = "mipush_notification";
    private static final String NOTIFICATION_SMALL_ICON = "mipush_small_notification";

    private static final String ID_GROUP_APPLICATIONS = "applications";

    public static final String CHANNEL_WARN = "warn";

    public static INotificationManager createNotificationManager(@NonNull Context context, @NonNull String packageName) {
        return new NormalNotificationManager(context);
    }

    public static void deleteOldNotificationChannelGroup(@NonNull Context context) {
        try {
            INotificationManager manager = new NormalNotificationManager(context);
            manager.deleteNotificationChannelGroup(ID_GROUP_APPLICATIONS);
        } catch (Exception ignore) {

        }

    }

    @TargetApi(26)
    private static NotificationChannelGroup createGroupWithPackage(@NonNull String packageName,
                                                                   @NonNull CharSequence appName) {
        return new NotificationChannelGroup(getGroupIdByPkg(packageName), appName);
    }

    private static NotificationChannel createChannelWithPackage(@NonNull PushMetaInfo metaInfo,
                                                                @NonNull String packageName) {
        final Map<String, String> extra = metaInfo.getExtra();
        String channelName = getExtraField(extra, EXTRA_CHANNEL_NAME, "未分类");
        String channelDescription = getExtraField(extra, EXTRA_CHANNEL_DESCRIPTION, null);
        String sound = getExtraField(extra, EXTRA_SOUND_URL, null);

        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(getChannelId(metaInfo, packageName), channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelDescription);
            if (sound != null) {
                AudioAttributes attr = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                channel.setSound(Uri.parse(sound), attr);
            }
        }
        return channel;
    }

    private static String getChannelId(@NonNull PushMetaInfo metaInfo,
                                       @NonNull String packageName) {
        final Map<String, String> extra = metaInfo.getExtra();
        String channelId = getExtraField(extra, EXTRA_CHANNEL_ID, "");
        return getChannelIdByPkg(packageName) + "_" + channelId;
    }


    public static NotificationChannel registerChannelIfNeeded(Context context, PushMetaInfo metaInfo, String packageName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null;
        }

        INotificationManager manager = createNotificationManager(context, packageName);

        String channelId = getChannelId(metaInfo, packageName);
        NotificationChannel notificationChannel = manager.getNotificationChannel(channelId);

        if (notificationChannel != null) {
            final boolean isValidGroup =
                    !ID_GROUP_APPLICATIONS.equals(notificationChannel.getGroup()) &&
                            !TextUtils.isEmpty(notificationChannel.getGroup());
            if (isValidGroup) {
                return notificationChannel;
            }
            manager.deleteNotificationChannel(channelId);
        }

        CharSequence appName = ApplicationNameCache.getInstance().getAppName(context, packageName);
        if (appName == null) {
            return null;
        }

        return createNotificationChannel(metaInfo, packageName, manager, appName);

    }

    private static NotificationChannel createNotificationChannel(PushMetaInfo metaInfo, String packageName, INotificationManager manager, CharSequence appName) {
        NotificationChannelGroup notificationChannelGroup = createGroupWithPackage(packageName, appName);
        manager.createNotificationChannelGroup(notificationChannelGroup);

        NotificationChannel notificationChannel = createChannelWithPackage(metaInfo, packageName);
        if (notificationChannel != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel.setGroup(notificationChannelGroup.getId());
        }

        manager.createNotificationChannel(notificationChannel);
        return notificationChannel;
    }


    @TargetApi(Build.VERSION_CODES.N)
    private static void updateSummaryNotification(Context context, PushMetaInfo metaInfo, String packageName, String groupId) {
        if (groupId == null) {
            return;
        }
        INotificationManager manager = createNotificationManager(context, packageName);
        if (!needGroupOfNotifications(manager, groupId)) {
            manager.cancel(groupId.hashCode());
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChannelId(metaInfo, packageName));
        builder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

        builder.setCategory(Notification.CATEGORY_EVENT)
                .setGroupSummary(true)
                .setGroup(groupId);
        notify(context, groupId.hashCode(), packageName, builder);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean needGroupOfNotifications(INotificationManager manager, String groupId) {
        int notificationCntInGroup = getNotificationCountOfGroup(manager, groupId);
        return notificationCntInGroup > 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static int getNotificationCountOfGroup(INotificationManager manager, String groupId) {
        StatusBarNotification[] activeNotifications = manager.getActiveNotifications();


        int notificationCntInGroup = 0;
        for (StatusBarNotification statusBarNotification : activeNotifications) {
            if (groupId.equals(statusBarNotification.getNotification().getGroup())) {
                notificationCntInGroup++;
            }
        }
        return notificationCntInGroup;
    }

    public static void publish(Context context, PushMetaInfo metaInfo, int notificationId, String packageName, NotificationCompat.Builder localBuilder) {
        // Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        registerChannelIfNeeded(context, metaInfo, packageName);

        localBuilder.setChannelId(getChannelId(metaInfo, packageName));
        localBuilder.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

        //for VERSION < Oero
        localBuilder.setDefaults(Notification.DEFAULT_ALL);
        localBuilder.setPriority(Notification.PRIORITY_HIGH);

        Notification notification = notify(context, notificationId, packageName, localBuilder);

        updateSummaryNotification(context, metaInfo, packageName, notification.getGroup());
    }

    private static Notification notify(Context context, int notificationId, String packageName, NotificationCompat.Builder localBuilder) {
        INotificationManager manager = createNotificationManager(context, packageName);

        // Make the behavior consistent with official MIUI
        Bundle extras = new Bundle();
        extras.putString("target_package", packageName);
        localBuilder.addExtras(extras);

        // Set small icon
        NotificationController.processSmallIcon(context, packageName, localBuilder);

        // Fill app name
        NotificationController.buildExtraSubText(context, packageName, localBuilder);

        Notification notification = localBuilder.build();
        setTargetPackage(notification, packageName);
        manager.notify(notificationId, notification);
        return notification;
    }

    private static void setTargetPackage(Notification notification, String packageName) {
        try {
            Method setTargetPackage = MIPushNotificationHelper.class
                    .getDeclaredMethod("setTargetPackage", Notification.class, String.class);
            setTargetPackage.setAccessible(true);
            setTargetPackage.invoke(null, notification, packageName);
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
            logger.e(e.getCause().getMessage());
        } catch (IllegalAccessException e) {
        }
    }

    public static void cancel(Context context, XmPushActionContainer container,
                              int notificationId, String notificationGroup, boolean clearGroup) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancel(notificationId);

        if (clearGroup) {
            manager.cancel(notificationGroup.hashCode());
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationGroup != null) {
                updateSummaryNotification(context, container.getMetaInfo(), container.getPackageName(), notificationGroup);
            }
        }
    }


    /**
     * @param ctx context
     * @param pkg packageName
     * @return 0 if not processed
     */
    public static int getIconColor(final Context ctx, final String pkg) {
        return IconCache.getInstance().getAppColor(ctx, pkg, (ctx1, iconBitmap) -> {
            if (iconBitmap == null) {
                return Notification.COLOR_DEFAULT;
            }
            int color = ColorUtil.getIconColor(iconBitmap);
            if (color != Notification.COLOR_DEFAULT) {
                final float[] hsl = new float[3];
                ColorUtils.colorToHSL(color, hsl);
                hsl[1] = 0.94f;
                hsl[2] = Math.min(hsl[2] * 0.6f, 0.31f);
                return ColorUtils.HSLToColor(hsl);
            } else {
                return Notification.COLOR_DEFAULT;
            }
        });
    }


    public static void processSmallIcon(Context context, String packageName, NotificationCompat.Builder notificationBuilder) {
        notificationBuilder.setSmallIcon(R.drawable.ic_notifications_black_24dp);

        // refer: https://dev.mi.com/console/doc/detail?pId=2625#_5_0
        Context pkgContext = null;
        try {
            pkgContext = context.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }
        int largeIconId = getIconId(context, packageName, NOTIFICATION_LARGE_ICON);
        int smallIconId = getIconId(context, packageName, NOTIFICATION_SMALL_ICON);

        if (largeIconId > 0) {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(pkgContext.getResources(), largeIconId));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (smallIconId > 0) {
                notificationBuilder.setSmallIcon(IconCompat.createWithResource(pkgContext, smallIconId));
                return;
            }
            if (largeIconId > 0) {
                notificationBuilder.setSmallIcon(IconCompat.createWithResource(pkgContext, largeIconId));
                return;
            }

            IconCompat iconCache = IconCache.getInstance().getIconCache(context, packageName, (ctx, b) -> IconCompat.createWithBitmap(b));
            if (iconCache != null) {
                notificationBuilder.setSmallIcon(iconCache);
                return;
            }
        }
    }


    public static void buildExtraSubText(Context context, String packageName, NotificationCompat.Builder localBuilder) {
        CharSequence appName = ApplicationNameCache.getInstance().getAppName(context, packageName);
        int color = getIconColor(context, packageName);
        if (color == Notification.COLOR_DEFAULT) {
            localBuilder.setSubText(appName);
            return;
        }
        localBuilder.setColor(color);
        CharSequence subText = ColorUtil.createColorSubtext(appName, color);
        if (subText != null) {
            localBuilder.setSubText(subText);
        }
    }


    private static int getIconId(Context context, String packageName, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "drawable", packageName);
    }


    public static void test(Context context, String packageName, String title, String description) {
        NotificationController.registerChannelIfNeeded(context, new PushMetaInfo(), packageName);

        int id = (int) (System.currentTimeMillis() / 1000L);

        NotificationCompat.Builder localBuilder = new NotificationCompat.Builder(context);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(description);
        style.setBigContentTitle(title);
        style.setSummaryText(description);
        localBuilder.setStyle(style);
        localBuilder.setWhen(System.currentTimeMillis());
        localBuilder.setShowWhen(true);

        NotificationController.publish(context, new PushMetaInfo(), id, packageName, localBuilder);
    }

}
