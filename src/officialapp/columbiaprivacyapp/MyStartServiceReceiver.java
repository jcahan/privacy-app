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
		initializeParse(intent, context);
		errorLogParse("MyStartServiceReceiver being called");
		Log.i("MyStartServiceReceiver", "Start Service being called");
		
		Intent service = new Intent(context, LocalWordService.class);
		context.startService(service);
		
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
