package ti.push;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.appcelerator.kroll.common.Log;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class AsyncBigPicture extends
		AsyncTask<HashMap<String, Object>, Void, HashMap<String, Object>> {

	TiGcmListenerService tiGcmListenerService;

	public AsyncBigPicture(TiGcmListenerService service) {
		super();
		tiGcmListenerService = service;
	}

	@Override
	protected HashMap<String, Object> doInBackground(
			HashMap<String, Object>... params) {
		try {
			HashMap<String, Object> payload = params[0];
			URL url = new URL(
					(String) payload
							.get(TiGcmListenerService.PROPERTY_BIG_PICTURE));
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			payload.put(TiGcmListenerService.PROPERTY_BIG_BITMAP,
					BitmapFactory.decodeStream(connection.getInputStream()));
			return payload;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(HashMap<String, Object> payload) {
		super.onPostExecute(payload);
		try {
			tiGcmListenerService.sendNotification(payload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
