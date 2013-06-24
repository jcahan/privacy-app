package officialapp.columbiaprivacyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//Check if GooglePlay and Name currently exists before continuing with this! 
	public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
				//				Log.i("Booting up", "The phone is successfully turning on this app on bootUP");
				//				Intent serviceIntent = new Intent("officialapp.columbiaprivacyapp.MySystemService");
				System.out.println("It is failing at 16");
				Intent serviceIntent = new Intent(context, MainActivity.class);
				serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startService(serviceIntent);
			}
		}
	}
