package officialapp.columbiaprivacyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//Check if GooglePlay and Name currently exists before continuing with this! 
	public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
				MainActivity.getInstance().errorLogParse("call to bootup being made");
				
				Intent serviceIntent = new Intent(context, MainActivity.class);
				serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startService(serviceIntent);
			}
		}
	}
