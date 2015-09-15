package ti.push;

import java.util.concurrent.atomic.AtomicInteger;

import org.appcelerator.titanium.TiApplication;
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

		Intent notificationIntent = new Intent(this, TiGcmListenerService.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_ONE_SHOT);

		String title = data.getString("title");
		if (title == null) {
			title = TiApplication.getInstance().getAppInfo().getName();
		}

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				this).setContentIntent(contentIntent).setContentTitle(title)
				.setContentText(data.getString("message"))
				.setWhen(data.getLong("when", System.currentTimeMillis()));

		String ticker = data.getString("ticker");
		if (ticker != null) {
			notificationBuilder.setTicker(ticker);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			String color = data.getString("color");
			if (color != null) {
				notificationBuilder.setColor(TiColorHelper.parseColor(color));
			}
		}

		ApplicationInfo appInfo = null;
		try {
			appInfo = getPackageManager().getApplicationInfo(getPackageName(),
					0);
		} catch (NameNotFoundException e) {
			Log.e(LCAT, "not able to find the app icon");
		}

		String smallIconPath = data.getString("icon");
		if (smallIconPath == null) {
			smallIconPath = data.getString("smallIcon");
		}
		if (smallIconPath == null) {
			if (appInfo != null) {
				notificationBuilder.setSmallIcon(appInfo.icon);
			}
		} else {
			notificationBuilder.setSmallIcon(getResource("drawable",
					smallIconPath));
		}

		String largeIconPath = data.getString("largeIcon");
		if (largeIconPath == null) {
			if (appInfo != null) {
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
						appInfo.icon);
				if (bitmap != null) {
					notificationBuilder.setLargeIcon(bitmap);
				}
			}
		} else {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					getResource("drawable", largeIconPath));
			if (bitmap != null) {
				notificationBuilder.setLargeIcon(bitmap);
			}
		}

		String soundPath = data.getString("sound");
		if (soundPath != null) {
			if ("default".equals(soundPath)) {
				notificationBuilder.setSound(RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
			} else {
				notificationBuilder.setSound(Uri.parse("android.resource://"
						+ getPackageName() + "/"
						+ getResource("raw", soundPath)));
			}
		}

		Boolean light = data.getBoolean("light");
		if (light) {
			notificationBuilder.setLights(
					TiColorHelper.parseColor(data.getString("lightColor")),
					300, 100);
		}

		Boolean vibrate = data.getBoolean("vibrate");
		if (vibrate) {
			notificationBuilder.setVibrate(new long[] { 1000 });
		}

		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(getId(), notificationBuilder.build());
	}

	private int getId() {
		return counter.incrementAndGet();
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
