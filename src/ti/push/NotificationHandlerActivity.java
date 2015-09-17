package ti.push;

import org.appcelerator.titanium.TiApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class NotificationHandlerActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		finish();
		String payload = getIntent().getExtras().getString(
				TipushModule.PROPERTY_PAYLOAD);
		if (TipushModule.isActive()) {
			TipushModule.fireCallback(payload);
		} else {
			Context appContext = TiApplication.getInstance()
					.getApplicationContext();
			Intent launchIntent = appContext.getPackageManager()
					.getLaunchIntentForPackage(appContext.getPackageName());
			launchIntent.putExtra(TipushModule.PROPERTY_PAYLOAD, payload);
			startActivity(launchIntent);
		}
	}
}
