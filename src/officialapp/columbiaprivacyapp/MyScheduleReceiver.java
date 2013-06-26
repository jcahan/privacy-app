package officialapp.columbiaprivacyapp;

import java.util.Calendar;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyScheduleReceiver extends BroadcastReceiver {

	// Restart service every 5 minutes 
	private static final long REPEAT_TIME = 1000 *60* 5;

	@Override
	public void onReceive(Context context, Intent intent) {

		//initializing parse
		initializeParse(intent, context);
		errorLogParse("Booting up");


		AlarmManager service = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, MyStartServiceReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar cal = Calendar.getInstance();
		
		// Start 2 minutes after boot completed
		cal.add(Calendar.SECOND, 120);

		// Fetch every 5 minutes 

		service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				REPEAT_TIME, pending);

	}

	protected void errorLogParse(String theString) {
		ParseObject myErrorObject= new ParseObject("ErrorTable");
		myErrorObject.put("errorLog", theString);
		myErrorObject.saveEventually();

	}

	protected void initializeParse(Intent intent, Context context) {
		Parse.initialize(context, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(intent);
	}

} 
