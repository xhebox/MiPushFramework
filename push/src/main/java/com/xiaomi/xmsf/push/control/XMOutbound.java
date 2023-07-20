package com.xiaomi.xmsf.push.control;

import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.UserHandle;
import androidx.annotation.Nullable;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.oasisfeng.condom.CondomOptions;
import com.oasisfeng.condom.PackageManagerWrapper;
import com.oasisfeng.condom.OutboundJudge;
import com.oasisfeng.condom.OutboundType;
import com.oasisfeng.condom.kit.NullDeviceIdKit;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import top.trumeet.hook.FakeManifestUtils;

/**
 * Created by Trumeet on 2018/1/19.
 */

public class XMOutbound implements OutboundJudge {
    private final Logger logger;
    private final Context context;

    private XMOutbound (Context context, String tag) {
        this.context = context;
        this.logger = XLog.tag(tag).build();
    }

		private static final Map<String, String> COMPONENT_REDIRECT;
		static {
			COMPONENT_REDIRECT = new HashMap<>(1);
			COMPONENT_REDIRECT.put("com.xiaomi.xmsf/com.xiaomi.push.service.XMPushService",
					"com.xiaomi.xmsf/com.xiaomi.push.service.PushServiceMain");
		}

		private static Intent applyRedirect (Intent original) {
			if (COMPONENT_REDIRECT == null)
				return original;
			if (original.getComponent() != null) {
				original.setComponent(applyRedirect(original.getComponent()));
				return original;
			}
			return original;
		}

		private static String applyRedirect (String original) {
			if (COMPONENT_REDIRECT == null || !COMPONENT_REDIRECT.containsKey(original))
				return original;
			return COMPONENT_REDIRECT.get(original);
		}

		private static ComponentName applyRedirect (ComponentName original) {
			if (COMPONENT_REDIRECT == null || !COMPONENT_REDIRECT.containsKey(original.flattenToString()))
				return original;
			return ComponentName.unflattenFromString(COMPONENT_REDIRECT.get(original.flattenToString()));
		}

    public static CondomOptions create (Context context, String tag,
                                        boolean enableKit) {
        CondomOptions options = new CondomOptions()
                .preventBroadcastToBackgroundPackages(false)
                .setOutboundJudge(new XMOutbound(context, tag));
        if (enableKit)
            options.addKit(new NullDeviceIdKit())
                    .addKit(new AppOpsKit())
                .addKit(new NotificationManagerKit())
                    ;
        return options;
    }

    public static CondomOptions create (Context context, String tag) {
        return create(context, tag, true);
    }

    public static Context wrap (Context context) {
			return new ContextWrapper(context) {
				@Override public boolean bindService(final Intent originalIntent, final ServiceConnection conn, final int flags) {
					return super.bindService(applyRedirect(originalIntent), conn, flags);
				}

				@Override public ComponentName startService(final Intent originalIntent) {
					return super.startService(applyRedirect(originalIntent));
				}

				@Override public void sendBroadcast(final Intent originalIntent) {
					super.sendBroadcast(applyRedirect(originalIntent));
				}

				@Override public void sendBroadcast(final Intent originalIntent, final String receiverPermission) {
					super.sendBroadcast(applyRedirect(originalIntent), receiverPermission);
				}

				@Override public void sendBroadcastAsUser(final Intent originalIntent, final UserHandle user) {
					super.sendBroadcastAsUser(applyRedirect(originalIntent), user);
				}

				@Override public void sendBroadcastAsUser(final Intent originalIntent, final UserHandle user, final String receiverPermission) {
					super.sendBroadcastAsUser(applyRedirect(originalIntent), user, receiverPermission);
				}

				@Override public void sendOrderedBroadcast(final Intent originalIntent, final String receiverPermission) {
					super.sendOrderedBroadcast(applyRedirect(originalIntent), receiverPermission);
				}

				@Override public void sendOrderedBroadcast(final Intent originalIntent, final String receiverPermission, final BroadcastReceiver resultReceiver,
				final Handler scheduler, final int initialCode, final String initialData, final Bundle initialExtras) {
					super.sendOrderedBroadcast(applyRedirect(originalIntent), receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
				}

				@Override public void sendOrderedBroadcastAsUser(final Intent originalIntent, final UserHandle user, final String receiverPermission, final BroadcastReceiver resultReceiver, final Handler scheduler, final int initialCode, final String initialData, final Bundle initialExtras) {
					super.sendOrderedBroadcastAsUser(applyRedirect(originalIntent), user, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
				}

				@Override public void sendStickyBroadcast(final Intent originalIntent) {
					super.sendStickyBroadcast(applyRedirect(originalIntent));
				}

				@Override public void sendStickyBroadcastAsUser(final Intent originalIntent, final UserHandle user) {
					super.sendStickyBroadcastAsUser(applyRedirect(originalIntent), user);
				}

				@Override public void sendStickyOrderedBroadcast(final Intent originalIntent, final BroadcastReceiver resultReceiver, final Handler scheduler,
				final int initialCode, final String initialData, final Bundle initialExtras) {
					super.sendStickyOrderedBroadcast(applyRedirect(originalIntent), resultReceiver, scheduler, initialCode, initialData, initialExtras);
				}

				@Override public void sendStickyOrderedBroadcastAsUser(final Intent originalIntent, final UserHandle user, final BroadcastReceiver resultReceiver, final Handler scheduler, final int initialCode, final String initialData, final Bundle initialExtras) {
					super.sendStickyOrderedBroadcastAsUser(applyRedirect(originalIntent), user, resultReceiver, scheduler, initialCode, initialData, initialExtras);
				}

				@Override public PackageManager getPackageManager() {
					return new PackageManagerWrapper(super.getPackageManager()) {
						@Override public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
							super.setComponentEnabledSetting(applyRedirect(componentName), newState, flags);
						}

						@Override public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
							PackageInfo info = super.getPackageInfo(applyRedirect(packageName) , flags);
							if (getPackageName().equals(packageName)) {
								return FakeManifestUtils.buildFakePackageInfo(info);
							}
							return info;
						}

						@Override public List<ResolveInfo> queryBroadcastReceivers(final Intent originalIntent, final int flags) {
							return super.queryBroadcastReceivers(applyRedirect(originalIntent), flags);
						}

						@Override public List<ResolveInfo> queryIntentServices(final Intent originalIntent, final int flags) {
							return super.queryIntentServices(applyRedirect(originalIntent), flags);
						}

						@Override public ResolveInfo resolveService(final Intent originalIntent, final int flags) {
							return super.resolveService(applyRedirect(originalIntent), flags);
						}

						@Override public ProviderInfo resolveContentProvider(final String name, final int flags) {
							return super.resolveContentProvider(applyRedirect(name), flags);
						}
					};
				}
			};
		}

    @Override
    public boolean shouldAllow(OutboundType type, @Nullable Intent intent, String target_package) {
        logger.d("shouldAllow ->" + type.toString());
        return true;
    }
}
