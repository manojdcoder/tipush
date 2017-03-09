package ti.push;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiColorHelper;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;
import org.appcelerator.kroll.common.Log;
import org.json.JSONObject;

import android.app.Notification;
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
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GcmListenerService;

public class TiGcmListenerService extends GcmListenerService {

	private static final String TAG = "TiGcmListenerService";

	public static final String PROPERTY_BIG_TITLE = "bigTitle";
	public static final String PROPERTY_BIG_CONTENT_TITLE = "bigContentTitle";
	public static final String PROPERTY_BIG_MESSAGE = "bigMessage";
	public static final String PROPERTY_BIG_TEXT = "bigText";
	public static final String PROPERTY_SUMMARY_TEXT = "summaryText";
	public static final String PROPERTY_LINES = "lines";
	public static final String PROPERTY_SMALL_ICON = "smallIcon";
	public static final String PROPERTY_LARGE_ICON = "largeIcon";
	public static final String PROPERTY_BIG_LARGE_ICON = "bigLargeIcon";
	public static final String PROPERTY_BIG_PICTURE = "bigPicture";
	public static final String PROPERTY_BIG_PICTURE_BITMAP = "bigPictureBitmap";
	public static final String PROPERTY_BACKRGOUND_IMAGE_BITMAP = "backgroundImageBitmap";

	public static final String PROPERTY_BITMAP_URL = "bitmapUrl";
	public static final String PROPERTY_BITMAP = "bitmap";

	// private
	private final static AtomicInteger counter = new AtomicInteger(0);

	@Override
	public void onMessageReceived(String from, Bundle data) {
		Log.d(TAG, "message received");
		sendNotification(toHashMap(data));
	}

	public void sendNotification(HashMap<String, Object> payload) {

		int custom_image_notification = 0;
		int id_custom_image_notification_left_icon = 0;
		int id_custom_image_notification_image = 0;
		try {
			custom_image_notification = TiRHelper
					.getResource("layout.custom_image_notification");
			id_custom_image_notification_left_icon = TiRHelper
					.getResource("id.custom_image_notification_left_icon");
			id_custom_image_notification_image = TiRHelper
					.getResource("id.custom_image_notification_image");
		} catch (ResourceNotFoundException e) {
			Log.e(TAG, "XML resources could not be found!!!");
		}

		Intent notificationIntent = new Intent(this,
				NotificationHandlerActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NO_ANIMATION);
		notificationIntent.putExtra(TipushModule.PROPERTY_PAYLOAD,
				new JSONObject(payload).toString());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Long when;
		if (payload.containsKey(TiC.PROPERTY_WHEN)) {
			when = (Long) payload.get(TiC.PROPERTY_WHEN);
		} else {
			when = System.currentTimeMillis();
		}
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				this).setContentIntent(contentIntent).setWhen(when)
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

		ApplicationInfo appInfo = null;
		String packageName = TiApplication.getInstance()
				.getApplicationContext().getPackageName();
		try {
			appInfo = TiApplication.getInstance().getApplicationContext()
					.getPackageManager().getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "not able to find app info for " + packageName);
		}

		int appIcon = 0;
		if (payload.containsKey(TiC.PROPERTY_ICON)) {
			appIcon = getResource("drawable",
					(String) payload.get(TiC.PROPERTY_ICON));
		} else if (appInfo != null) {
			appIcon = appInfo.icon;
		}

		int smallIcon = 0;
		if (payload.containsKey(PROPERTY_SMALL_ICON)) {
			String propSmallIcon = (String) payload.get(PROPERTY_SMALL_ICON);
			if (propSmallIcon.equals("default")) {
				smallIcon = appIcon;
			} else {
				smallIcon = getResource("drawable", propSmallIcon);
			}
		} else {
			smallIcon = appIcon;
		}
		notificationBuilder.setSmallIcon(smallIcon);

		int largeIcon = 0;
		if (payload.containsKey(PROPERTY_LARGE_ICON)) {
			String propLargeIcon = (String) payload.get(PROPERTY_LARGE_ICON);
			if (propLargeIcon.equals("default")) {
				largeIcon = appIcon;
			} else {
				largeIcon = getResource("drawable", propLargeIcon);
			}
		} else {
			largeIcon = appIcon;
		}
		Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(),
				largeIcon);
		if (largeIconBitmap != null) {
			notificationBuilder.setLargeIcon(largeIconBitmap);
		}

		if (payload.containsKey(TiC.PROPERTY_DEFAULTS)) {
			notificationBuilder.setDefaults((Integer) payload
					.get(TiC.PROPERTY_DEFAULTS));
		} else {
			notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
		}

		if (payload.containsKey(TiC.PROPERTY_SOUND)) {
			String sound = (String) payload.get(TiC.PROPERTY_SOUND);
			if (sound != null) {
				if ("default".equals(sound)) {
					notificationBuilder.setSound(RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
				} else {
					notificationBuilder.setSound(Uri
							.parse("android.resource://" + packageName + "/"
									+ getResource("raw", sound)));
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			if (payload.containsKey(TiC.PROPERTY_STYLE)) {
				String style = (String) payload.get(TiC.PROPERTY_STYLE);
				Log.d(TAG, "notification style: " + style);
				if (style.equals("BigTextStyle")) {
					NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

					String bigContentTitle = contentTitle;
					if (payload.containsKey(PROPERTY_BIG_CONTENT_TITLE)) {
						bigContentTitle = (String) payload
								.get(PROPERTY_BIG_CONTENT_TITLE);
					} else if (payload.containsKey(PROPERTY_BIG_TITLE)) {
						bigContentTitle = (String) payload
								.get(PROPERTY_BIG_TITLE);
					}
					bigTextStyle.setBigContentTitle(bigContentTitle);

					if (payload.containsKey(PROPERTY_SUMMARY_TEXT)) {
						bigTextStyle.setSummaryText((String) payload
								.get(PROPERTY_SUMMARY_TEXT));
					}

					if (payload.containsKey(PROPERTY_BIG_TEXT)) {
						bigTextStyle.bigText((String) payload
								.get(PROPERTY_BIG_TEXT));
					} else if (payload.containsKey(PROPERTY_BIG_MESSAGE)) {
						bigTextStyle.bigText((String) payload
								.get(PROPERTY_BIG_MESSAGE));
					}

					notificationBuilder.setStyle(bigTextStyle);
				} else if (style.equals("InboxStyle")) {
					NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

					String bigContentTitle = contentTitle;
					if (payload.containsKey(PROPERTY_BIG_CONTENT_TITLE)) {
						bigContentTitle = (String) payload
								.get(PROPERTY_BIG_CONTENT_TITLE);
					} else if (payload.containsKey(PROPERTY_BIG_TITLE)) {
						bigContentTitle = (String) payload
								.get(PROPERTY_BIG_TITLE);
					}
					inboxStyle.setBigContentTitle(bigContentTitle);

					if (payload.containsKey(PROPERTY_SUMMARY_TEXT)) {
						inboxStyle.setSummaryText((String) payload
								.get(PROPERTY_SUMMARY_TEXT));
					}

					if (payload.containsKey(PROPERTY_LINES)) {
						String lines[] = (String[]) payload.get(PROPERTY_LINES);
						for (int i = 0; i < lines.length; i++) {
							inboxStyle.addLine(lines[i]);
						}
					}

					notificationBuilder.setStyle(inboxStyle);
				} else if (style.equals("BigPictureStyle")) {
					NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();

					String bigContentTitle = contentTitle;
					if (payload.containsKey(PROPERTY_BIG_CONTENT_TITLE)) {
						bigContentTitle = (String) payload
								.get(PROPERTY_BIG_CONTENT_TITLE);
					} else if (payload.containsKey(PROPERTY_BIG_TITLE)) {
						bigContentTitle = (String) payload
								.get(PROPERTY_BIG_TITLE);
					}
					bigPictureStyle.setBigContentTitle(bigContentTitle);

					if (payload.containsKey(PROPERTY_SUMMARY_TEXT)) {
						bigPictureStyle.setSummaryText((String) payload
								.get(PROPERTY_SUMMARY_TEXT));
					}

					if (payload.containsKey(PROPERTY_BIG_LARGE_ICON)) {
						bigPictureStyle
								.bigLargeIcon(BitmapFactory
										.decodeResource(
												getResources(),
												getResource(
														"drawable",
														(String) payload
																.get(PROPERTY_BIG_LARGE_ICON))));
					} else if (largeIconBitmap != null) {
						bigPictureStyle.bigLargeIcon(largeIconBitmap);
					}

					if (payload.containsKey(PROPERTY_BIG_PICTURE)) {
						String url = (String) payload.get(PROPERTY_BIG_PICTURE);
						if (payload.containsKey(PROPERTY_BIG_PICTURE_BITMAP)) {
							Log.d(TAG, "downloaded big picture from url: "
									+ url);
							bigPictureStyle.bigPicture((Bitmap) payload
									.get(PROPERTY_BIG_PICTURE_BITMAP));
						} else {
							Log.d(TAG,
									"started downloading big picture from url: "
											+ url);
							payload.put(PROPERTY_BITMAP_URL, url);
							payload.put(PROPERTY_BITMAP,
									PROPERTY_BIG_PICTURE_BITMAP);
							new AsyncDownloader(this).execute(payload);
							return;
						}
					}

					notificationBuilder.setStyle(bigPictureStyle);
				}
			}
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

		Notification notification = notificationBuilder.build();

		// hide small icon from notification if no small icon was set in payload
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
				&& !payload.containsKey(PROPERTY_SMALL_ICON)) {
			hideRemoteViewById(notification, "right_icon");
		}

		if (payload.containsKey(TiC.PROPERTY_BACKGROUND_IMAGE)) {
			String url = (String) payload.get(TiC.PROPERTY_BACKGROUND_IMAGE);
			if (payload.containsKey(PROPERTY_BACKRGOUND_IMAGE_BITMAP)) {
				Log.d(TAG, "downloaded background image from url: " + url);
				RemoteViews contentView = new RemoteViews(getPackageName(),
						custom_image_notification);
				contentView.setImageViewResource(
						id_custom_image_notification_left_icon, appIcon);
				contentView.setImageViewBitmap(
						id_custom_image_notification_image,
						(Bitmap) payload.get(PROPERTY_BACKRGOUND_IMAGE_BITMAP));
				notification.contentView = contentView;
			} else {
				Log.d(TAG, "started downloading background image from url: "
						+ url);
				payload.put(PROPERTY_BITMAP_URL, url);
				payload.put(PROPERTY_BITMAP, PROPERTY_BACKRGOUND_IMAGE_BITMAP);
				new AsyncDownloader(this).execute(payload);
				return;
			}
		}

		((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
				.notify(getAutoId(), notification);
	}

	private void hideRemoteViewById(Notification notification, String id) {
		int viewId = getViewId(id);
		if (viewId != 0) {
			if (notification.contentView != null) {
				notification.contentView.setViewVisibility(viewId,
						View.INVISIBLE);
			}
			if (notification.headsUpContentView != null) {
				notification.headsUpContentView.setViewVisibility(viewId,
						View.INVISIBLE);
			}
			if (notification.bigContentView != null) {
				notification.bigContentView.setViewVisibility(viewId,
						View.INVISIBLE);
			}
		}
	}

	private int getViewId(String id) {
		return getResources().getIdentifier(id, "id",
				android.R.class.getPackage().getName());
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
				Log.e(TAG, type + "." + name
						+ " not found; make sure it's in platform/android/res/"
						+ type);
			}
		}
		return icon;
	}

	private HashMap<String, Object> toHashMap(Bundle data) {
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		Set<String> keySet = data.keySet();
		for (final String key : keySet) {
			hashMap.put(key, data.get(key));
		}
		return hashMap;
	}

	private int getAutoId() {
		return counter.incrementAndGet();
	}
}
