package officialapp.columbiaprivacyapp;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.AlarmClock;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class LocalWordService extends Service{
	private final IBinder mBinder = new MyBinder();
	private ArrayList<String> list = new ArrayList<String>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initiateTimers();
		
		//initializing parse
		initializeParse(intent);
		errorLogParse("Starting up");
		
		//error Log
		return Service.START_NOT_STICKY;
	}


	private void initiateTimers() {
		Log.i("Initiating timer", "timer is being initiated");
	}
	protected void errorLogParse(String theString) {
		ParseObject myErrorObject= new ParseObject("ErrorTable");
		myErrorObject.put("errorLog", theString);
		myErrorObject.saveEventually();

	}
	
	protected void initializeParse(Intent intent) {
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(intent);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		LocalWordService getService() {
			return LocalWordService.this;
		}
	}

	public List<String> getWordList() {
		return list;
	}

} 