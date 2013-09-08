package officialapp.columbiaprivacyapp;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class MyScheduleReceiver extends BroadcastReceiver {

	// Restart service every 28 minutes
//	private int EVERY_TWENTY_EIGHT_MINUTES = 60000*28; 
		private int EVERY_TWO_MINUTES = 60000*3; 

	@Override
	public void onReceive(Context context, Intent intent) {
		//initializing parse
		initializeParse(intent, context);

		errorLogParse("Booting up");

		Log.i("ScheduleReceiver", "In the Schedule Receiver, iniating alarm shortly");
		
		AlarmManager service = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, MyStartServiceReceiver.class);

		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);

		Calendar cal = Calendar.getInstance();

		// Start 30 seconds after boot completed
		cal.add(Calendar.SECOND, 30);

		//TODO: Change this back!!
		service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				EVERY_TWO_MINUTES, pending);
	}

	protected void errorLogParse(String theString) {
		ParseObject myErrorObject= new ParseObject("NewErrorTable");
		myErrorObject.put("errorLog", theString);
		myErrorObject.saveEventually();
	}

	protected void initializeParse(Intent intent, Context context) {
		Parse.initialize(context, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(intent);
	}
} 
