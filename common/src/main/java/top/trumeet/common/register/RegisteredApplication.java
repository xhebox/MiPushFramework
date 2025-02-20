package top.trumeet.common.register;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.content.ContextCompat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import top.trumeet.common.utils.DatabaseUtils;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 *
 * @author Trumeet
 * @date 2017/12/24
 * Common 中的不带 greenDao 的模型。Too bad
 */

public class RegisteredApplication implements Parcelable {

    public static final String KEY_ID = DatabaseUtils.KEY_ID;
    public static final String KEY_PACKAGE_NAME = "pkg";
    public static final String KEY_TYPE = "type";
    public static final String KEY_ALLOW_RECEIVE_PUSH = "allow_receive_push";
    public static final String KEY_ALLOW_RECEIVE_REGISTER_RESULT = "allow_receive_register_result";
    public static final String KEY_ALLOW_RECEIVE_COMMAND = "allow_receive_command_without_register_result";
    public static final String KEY_NOTIFICATION_ON_REGISTER = "notification_on_register";
    public static final String KEY_GROUP_NOTIFICATIONS_FOR_SAME_SESSION = "group_notifications_for_same_session";
    public static final String KEY_CLEAR_ALL_NOTIFICATIONS_OF_SESSION = "clear_all_notifications_of_session";
    public static final String KEY_SHOW_PASS_THROUGH = "show_pass_through";

    private Long id;

    private String packageName;

    @Type
    private int type = Type.ASK;

    private boolean allowReceivePush;

    private boolean allowReceiveRegisterResult;

    /**
     * 0: Not registered
     * 1: Registered (both RegisteredApplication & Event)
     * 2: Unregistered
     */
    private int registeredType;

    // Init(register) result is a kind of command, if disabled, register result WILL STILL BE RECEIVED
    private boolean allowReceiveCommand;

    private boolean notificationOnRegister;

    private boolean groupNotificationsForSameSession;

    private boolean clearAllNotificationsOfSession;

    private boolean showPassThrough;

    protected RegisteredApplication(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        packageName = in.readString();
        type = in.readInt();
        allowReceivePush = in.readByte() != 0;
        allowReceiveRegisterResult = in.readByte() != 0;
        allowReceiveCommand = in.readByte() != 0;
        registeredType = in.readInt();
        notificationOnRegister = in.readByte() != 0;
        groupNotificationsForSameSession = in.readByte() != 0;
        clearAllNotificationsOfSession = in.readByte() != 0;
        showPassThrough = in.readByte() != 0;
    }

    public static final Creator<RegisteredApplication> CREATOR = new Creator<RegisteredApplication>() {
        @Override
        public RegisteredApplication createFromParcel(Parcel in) {
            return new RegisteredApplication(in);
        }

        @Override
        public RegisteredApplication[] newArray(int size) {
            return new RegisteredApplication[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(id);
        }
        parcel.writeString(packageName);
        parcel.writeInt(type);
        parcel.writeByte((byte) (allowReceivePush ? 1 : 0));
        parcel.writeByte((byte) (allowReceiveRegisterResult ? 1 : 0));
        parcel.writeByte((byte) (allowReceiveCommand ? 1 : 0));
        parcel.writeInt(registeredType);
        parcel.writeByte((byte) (notificationOnRegister ? 1 : 0));
        parcel.writeByte((byte) (groupNotificationsForSameSession ? 1 : 0));
        parcel.writeByte((byte) (clearAllNotificationsOfSession ? 1 : 0));
        parcel.writeByte((byte) (showPassThrough ? 1 : 0));
    }

    @androidx.annotation.IntDef({Type.ASK, Type.ALLOW, Type.DENY, Type.ALLOW_ONCE})
    @Retention(SOURCE)
    @Target({ElementType.PARAMETER, ElementType.TYPE,
            ElementType.FIELD, ElementType.METHOD})
    public @interface Type {
        int ASK = 0;
        int ALLOW = 2;
        int DENY = 3;
        int ALLOW_ONCE = -1;
    }

    public boolean isAllowReceiveCommand() {
        return allowReceiveCommand;
    }

    public void setAllowReceiveCommand(boolean allowReceiveCommand) {
        this.allowReceiveCommand = allowReceiveCommand;
    }

    public RegisteredApplication(Long id
            , String packageName
            , int type
            , boolean allowReceivePush
            , boolean allowReceiveRegisterResult
            , boolean allowReceiveCommand, int registeredType
            , boolean notificationOnRegister
            , boolean groupNotificationsForSameSession
            , boolean clearAllNotificationsOfSession
            , boolean showPassThrough
    ) {
        this.id = id;
        this.packageName = packageName;
        this.type = type;
        this.allowReceivePush = allowReceivePush;
        this.allowReceiveRegisterResult = allowReceiveRegisterResult;
        this.allowReceiveCommand = allowReceiveCommand;
        this.registeredType = registeredType;
        this.notificationOnRegister = notificationOnRegister;
        this.groupNotificationsForSameSession = groupNotificationsForSameSession;
        this.clearAllNotificationsOfSession = clearAllNotificationsOfSession;
        this.showPassThrough = showPassThrough;
    }

    public RegisteredApplication() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setAllowReceivePush(boolean allowReceivePush) {
        this.allowReceivePush = allowReceivePush;
    }

    public boolean getAllowReceivePush() {
        return this.allowReceivePush;
    }

    public boolean getAllowReceiveRegisterResult() {
        return this.allowReceiveRegisterResult;
    }

    public void setAllowReceiveRegisterResult(boolean allowReceiveRegisterResult) {
        this.allowReceiveRegisterResult = allowReceiveRegisterResult;
    }

    public int getRegisteredType() {
        return registeredType;
    }

    public void setRegisteredType(int registeredType) {
        this.registeredType = registeredType;
    }

    public boolean isNotificationOnRegister() {
        return notificationOnRegister;
    }

    public void setNotificationOnRegister(boolean notificationOnRegister) {
        this.notificationOnRegister = notificationOnRegister;
    }

    public boolean isGroupNotificationsForSameSession() {
        return this.groupNotificationsForSameSession;
    }

    public void setGroupNotificationsForSameSession(boolean groupNotificationsForSameSession) {
        this.groupNotificationsForSameSession = groupNotificationsForSameSession;
    }

    public boolean isClearAllNotificationsOfSession() {
        return this.clearAllNotificationsOfSession;
    }

    public void setClearAllNotificationsOfSession(boolean clearAllNotificationsOfSession) {
        this.clearAllNotificationsOfSession = clearAllNotificationsOfSession;
    }

    public boolean isShowPassThrough() {
        return this.showPassThrough;
    }

    public void setShowPassThrough(boolean showPassThrough) {
        this.showPassThrough = showPassThrough;
    }

    @androidx.annotation.NonNull
    public CharSequence getLabel (Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES).loadLabel(pm);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            return packageName;
        }
    }

    @androidx.annotation.NonNull
    public Drawable getIcon (Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES).loadIcon(pm);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            return ContextCompat.getDrawable(context, android.R.mipmap.sym_def_app_icon);
        }
    }

    public int getUid (Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES)
                    .uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    /**
     * Create from cursor
     * @param cursor cursor
     * @return Event object
     */
    @androidx.annotation.NonNull
    public static RegisteredApplication create (@androidx.annotation.NonNull Cursor cursor) {
        return new RegisteredApplication(cursor.getLong(cursor.getColumnIndex(KEY_ID))
                , cursor.getString(cursor.getColumnIndex(KEY_PACKAGE_NAME))
                , cursor.getInt(cursor.getColumnIndex(KEY_TYPE))
                , cursor.getInt(cursor.getColumnIndex(KEY_ALLOW_RECEIVE_PUSH)) > 0
                , cursor.getInt(cursor.getColumnIndex(KEY_ALLOW_RECEIVE_REGISTER_RESULT)) > 0
                /* allow receive register result */
                , cursor.getInt(cursor.getColumnIndex(KEY_ALLOW_RECEIVE_COMMAND)) > 0
                , 0 /* RegisteredType, Do not save to DB */
                , cursor.getInt(cursor.getColumnIndex(KEY_NOTIFICATION_ON_REGISTER)) > 0
                , cursor.getInt(cursor.getColumnIndex(KEY_GROUP_NOTIFICATIONS_FOR_SAME_SESSION)) > 0
                , cursor.getInt(cursor.getColumnIndex(KEY_CLEAR_ALL_NOTIFICATIONS_OF_SESSION)) > 0
                , cursor.getInt(cursor.getColumnIndex(KEY_SHOW_PASS_THROUGH)) > 0
        );
    }

    /**
     * Convert to ContentValues
     * @return values object
     */
    @androidx.annotation.NonNull
    public ContentValues toValues () {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, getId());
        values.put(KEY_PACKAGE_NAME, getPackageName());
        values.put(KEY_TYPE, getType());
        values.put(KEY_ALLOW_RECEIVE_PUSH, getAllowReceivePush());
        values.put(KEY_ALLOW_RECEIVE_REGISTER_RESULT, getAllowReceiveRegisterResult());
        values.put(KEY_ALLOW_RECEIVE_COMMAND, isAllowReceiveCommand());
        values.put(KEY_NOTIFICATION_ON_REGISTER, isNotificationOnRegister());
        values.put(KEY_GROUP_NOTIFICATIONS_FOR_SAME_SESSION, isGroupNotificationsForSameSession());
        values.put(KEY_CLEAR_ALL_NOTIFICATIONS_OF_SESSION, isClearAllNotificationsOfSession());
        values.put(KEY_SHOW_PASS_THROUGH, isShowPassThrough());
        return values;
    }

    public String dump () {
        return "RegisteredApplication{" +
                "id=" + id +
                ", packageName='" + packageName + '\'' +
                ", type=" + type +
                ", allowReceivePush=" + allowReceivePush +
                ", allowReceiveRegisterResult=" + allowReceiveRegisterResult +
                ", registeredType=" + registeredType +
                '}';
    }
}
