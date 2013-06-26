//package officialapp.columbiaprivacyapp;
//
//import java.util.Calendar;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.location.Location;
//
//import com.google.android.gms.location.LocationClient;
//import com.parse.ParseObject;
//
////Check if GooglePlay and Name currently exists before continuing with this! 
//public class StartMyServiceAtBootReceiver extends BroadcastReceiver {
//	private final long REPEAT_TIME = 6000*5; //TODO: Updates Location every hour currently 
//
//
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
//			ParseObject myErrorObject= new ParseObject("ErrorTable");
//			myErrorObject.put("errorLog", "call to bootup being made");
//			myErrorObject.saveEventually();
//
//			initiateTimer(context);
//
//			Intent serviceIntent = new Intent(context, LocationService.class);
//			serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startService(serviceIntent);
//		}
//		else {
//			ParseObject myErrorObject= new ParseObject("ErrorTable");
//			myErrorObject.put("errorLog", "Boot not completed!!");
//			myErrorObject.saveEventually();	
//		}
//	}
//
//	private void initiateTimer(Context context) {
//		Timer toReconnect = new Timer();
//		LocationClient mLocationClient; 
//
//		AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//		Intent i = new Intent(context, MyStartServiceReceiver.class);
//
//		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
//				PendingIntent.FLAG_CANCEL_CURRENT);
//		Calendar cal = Calendar.getInstance();
//
//		// Start 30 seconds after boot completed
//		cal.add(Calendar.SECOND, 30);
//
//		// Fetch every 30 seconds
//		// InexactRepeating allows Android to optimize the energy consumption
//		service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
//				cal.getTimeInMillis(), REPEAT_TIME, pending);
//
//	}
//}
