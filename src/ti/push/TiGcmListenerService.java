package ti.push;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiColorHelper;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class TiGcmListenerService extends GcmListenerService {

	private static final String LCAT = "TiGcmListenerService";

	// private
	private final static AtomicInteger counter = new AtomicInteger(0);

	@Override
	public void onMessageReceived(String from, Bundle data) {
		Log.d(LCAT, "message received");
		sendNotification(data);
	}

	private void sendNotification(Bundle data) {

		HashMap<String, Object> payload = toHashMap(data);

		Intent launcherIntent = getPackageManager().getLaunchIntentForPackage(
				getPackageName());
		launcherIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		launcherIntent.putExtra(TipushModule.PROPERTY_PAYLOAD, payload);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				launcherIntent, PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				this)
				.setContentIntent(contentIntent)
				.setWhen(
						data.getLong(TiC.PROPERTY_WHEN,
								System.currentTimeMillis()))
				.setAutoCancel(true);

		String contentTitle = "";
		if (payload.containsKey(TiC.PROPERTY_CONTENT_TITLE)) {
			contentTitle = (String) payload.get(TiC.PROPERTY_CONTENT_TITLE);
		} else if (payload.containsKey(TiC.PROPERTY_TITLE)) {
			contentTitle = (String) payload.get(TiC.PROPERTY_TITLE);
		} else {
			contentTitle = TiApplication.getInstance().getAppInfo().getName();
		}
		notificationBuilder.setContentTitle(contentTitle);

		String contentText = "";
		if (payload.containsKey(TiC.PROPERTY_CONTENT_TEXT)) {
			contentText = (String) payload.get(TiC.PROPERTY_CONTENT_TEXT);
		} else if (payload.containsKey(TiC.PROPERTY_MESSAGE)) {
			contentText = (String) payload.get(TiC.PROPERTY_MESSAGE);
		}
		notificationBuilder.setContentText(contentText);

		if (payload.containsKey(TiC.PROPERTY_TICKER_TEXT)) {
			notificationBuilder.setTicker((String) payload
					.get(TiC.PROPERTY_TICKER_TEXT));
		}

		if (payload.containsKey(TiC.PROPERTY_NUMBER)) {
			notificationBuilder.setNumber((Integer) payload
					.get(TiC.PROPERTY_NUMBER));
		}

		if (payload.containsKey(TiC.PROPERTY_DEFAULTS)) {
			notificationBuilder.setDefaults((Integer) payload
					.get(TiC.PROPERTY_DEFAULTS));
		}

		if (payload.containsKey(TiC.PROPERTY_SOUND)) {
			String sound = (String) payload.get(TiC.PROPERTY_SOUND);
			if (sound != null) {
				if ("default".equals(sound)) {
					notificationBuilder.setSound(RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
				} else {
					notificationBuilder.setSound(Uri
							.parse("android.resource://" + getPackageName()
									+ "/" + getResource("raw", sound)));
				}
			}
		}

		if (payload.containsKey(TiC.PROPERTY_LED_ARGB)
				&& payload.containsKey(TiC.PROPERTY_LED_ON_MS)
				&& payload.containsKey(TiC.PROPERTY_LED_OFF_MS)) {
			notificationBuilder.setLights(TiColorHelper
					.parseColor((String) payload.get(TiC.PROPERTY_LED_ARGB)),
					(Integer) payload.get(TiC.PROPERTY_LED_ON_MS),
					(Integer) payload.get(TiC.PROPERTY_LED_OFF_MS));
		}

		ApplicationInfo appInfo = null;
		try {
			appInfo = getPackageManager().getApplicationInfo(getPackageName(),
					0);
		} catch (NameNotFoundException e) {
			Log.e(LCAT, "not able to find the app icon");
		}

		int icon = 0;
		if (payload.containsKey(TiC.PROPERTY_ICON)) {
			icon = getResource("drawable",
					(String) payload.get(TiC.PROPERTY_ICON));
		} else if (appInfo != null) {
			icon = appInfo.icon;
		}

		int smallIcon = 0;
		if (payload.containsKey(TipushModule.PROPERTY_SMALL_ICON)) {
			smallIcon = getResource("drawable",
					(String) payload.get(TipushModule.PROPERTY_SMALL_ICON));
		} else {
			smallIcon = icon;
		}
		notificationBuilder.setSmallIcon(smallIcon);

		int largeIcon = 0;
		if (payload.containsKey(TipushModule.PROPERTY_LARGE_ICON)) {
			largeIcon = getResource("drawable",
					(String) payload.get(TipushModule.PROPERTY_LARGE_ICON));
		} else {
			largeIcon = icon;
		}
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), largeIcon);
		if (bitmap != null) {
			notificationBuilder.setLargeIcon(bitmap);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			if (payload.containsKey(TiC.PROPERTY_PRIORITY)) {
				notificationBuilder.setPriority((Integer) payload
						.get(TiC.PROPERTY_PRIORITY));
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (payload.containsKey(TiC.PROPERTY_VISIBILITY)) {
				notificationBuilder.setVisibility((Integer) payload
						.get(TiC.PROPERTY_VISIBILITY));
			}
			if (payload.containsKey(TiC.PROPERTY_CATEGORY)) {
				notificationBuilder.setCategory((String) payload
						.get(TiC.PROPERTY_CATEGORY));
			}
			if (payload.containsKey(TiC.PROPERTY_COLOR)) {
				notificationBuilder.setColor(TiColorHelper
						.parseColor((String) payload.get(TiC.PROPERTY_COLOR)));
			}
		}

		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(getId(), notificationBuilder.build());
	}

	private int getId() {
		return counter.incrementAndGet();
	}

	private HashMap<String, Object> toHashMap(Bundle data) {
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		Set<String> keySet = data.keySet();
		for (final String key : keySet) {
			hashMap.put(key, data.get(key));
		}
		return hashMap;
	}

	private int getResource(String type, String name) {
		int icon = 0;
		if (name != null) {
			int index = name.lastIndexOf(".");
			if (index > 0) {
				name = name.substring(0, index);
			}
			try {
				icon = TiRHelper.getApplicationResource(type + "." + name);
			} catch (ResourceNotFoundException ex) {
				Log.e(LCAT, type + "." + name
						+ " not found; make sure it's in platform/android/res/"
						+ type);
			}
		}
		return icon;
	}
}
