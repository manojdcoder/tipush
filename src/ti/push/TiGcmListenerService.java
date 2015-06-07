package ti.push;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

public class TiGcmListenerService extends GcmListenerService {

	@Override
	public void onMessageReceived(String from, Bundle data) {
		
	}

	private void sendNotification(String msg) {
		//show notification
	}
}
