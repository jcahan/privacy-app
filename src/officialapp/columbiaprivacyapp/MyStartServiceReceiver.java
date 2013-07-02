package officialapp.columbiaprivacyapp;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyStartServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("myStartServiceReceiver", "Being called via MyStartServiceReceiver");
		Intent service = new Intent(context, LocalWordService.class);
		context.startService(service);
		
	}
}
