package officialapp.columbiaprivacyapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class GetLocationService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{
	protected final IBinder mBinder = new MyBinder();
	protected ArrayList<String> list = new ArrayList<String>();
	protected LocationClient mLocationClient; //Stores the current instantiation of the location client in this object
	protected final String LOCATION_TABLE = "TESTLocationTableStudy";
	protected String android_id; 

	protected SharedPreferences prefs;
	protected Editor editor;
	protected String userNameInPref; 
	protected Long whenCreatedLong; 
	private String theLocAssoc = "";
	private final String TIME_ACCOUNT_CREATED = "timeWhenCreated";
	private final long ONE_MINUTE = 60*1000*1;
	private final int TEN_MINUTES = 60000*10; 
	private final String THE_ERROR_TABLE = "TESTErrorTable";
	private PowerManager pm; 
	private PowerManager.WakeLock wl;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//initializing parse
		initializeParse(intent);
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		errorLogParse("LocalWordService: onStartCommand launching");

		if(mLocationClient==null) {
			//Getting and Posting Location
			initalizeLocks();
			connectClient();
			createSetAlarm();
		} else {
			//			System.out.println("GETTING THE LOCATION OMG!!!");
			getPostLocation();
			wl.release();
			mLocationClient = null;
			//			System.out.println("the service is being stopped!!");
			stopSelf();
		}

		return Service.START_NOT_STICKY;
	}

	private void createSetAlarm() {
		Context theContext = getBaseContext();

		AlarmManager theService = (AlarmManager) theContext
				.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(GetLocationService.this, this.getClass());

		PendingIntent thePending = PendingIntent.getService(this, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		//call this after two minutes 
		theService.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ONE_MINUTE, thePending);

	}

	private void connectClient() {
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		Log.i("LocalWordService", "Shortly iniating alarm");
	}

	private void initalizeLocks() {
		pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		wl.acquire();
	}

	private TreeSet<BlacklistWord> getAllBlackListItems() {
		BlacklistWordDataSource theSource = new BlacklistWordDataSource(this);
		theSource.open();
		TreeSet<BlacklistWord> theTreeWords = theSource.GetAllWords();
		return theTreeWords; 
	}

	protected void getPostLocation() {
		errorLogParse("About to get location");
		try {
			Location theLocation = mLocationClient.getLastLocation();

			if(theLocation!=null) {
				errorLogParse("Local Word: should be adding location");
				boolean result = checkLocation(theLocation);

				if(!result) {
					String userName = prefs.getString("prefUsername", "default");

					//Posting items to parse
					postItemsToParse(android_id, theLocation.getLatitude(), theLocation.getLongitude(), userName);
					errorLogParse("Local Word: the local word service is adding the item!!");
				}
				else {
					errorLogParse("There is an intersection, do not save data");
				}
			}
			else {
				errorLogParse("Local Word: Not adding location");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			errorLogParse("Local Word: Exception thrown, not able to update");
		}
	}

	protected void postItemsToParse(String android_id, double latitude,
			double longitude, String userName) {
		ParseObject locationItem = new ParseObject(LOCATION_TABLE);
		locationItem.put("deviceId", android_id);
		locationItem.put("latitude", latitude);
		locationItem.put("longitude", longitude);
		locationItem.put("userName", userName);
		locationItem.put("locationAssociations", theLocAssoc);
		locationItem.saveEventually();
	}


	protected void errorLogParse(String theString) {
		ParseObject myErrorObject= new ParseObject(THE_ERROR_TABLE);
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String userName = prefs.getString("prefUsername", "default");
		android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		myErrorObject.put("userName", userName);
		myErrorObject.put("deviceId", android_id);
		myErrorObject.put("errorLog", theString);
		myErrorObject.put("locationAssociations", theLocAssoc);
		myErrorObject.saveEventually();
	}

	//Checks if GooglePlay is Working
	protected Boolean checkIfGooglePlay() {
		int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (errorCode != ConnectionResult.SUCCESS) {
			errorLogParse("GooglePlay does not exist");
			return false; 
		}
		return true;
	}

	//Don't save location data if within 10 minutes of creation 
	protected boolean checkTime() {
		Long whenCreated = prefs.getLong(TIME_ACCOUNT_CREATED, 0L);

		//TODO: Change this back!!
		if(whenCreated.equals(0L) || System.currentTimeMillis()-whenCreated<TEN_MINUTES) {
			errorLogParse("Within 10 minutes, do not update!");
			return true; 
		}
		errorLogParse("Outside of 10 minutes, update!");
		return true; 
	}
	//returns true if intersection exists 
	protected Boolean checkLocation(Location theLocation) throws IOException {
		String locationAssociations = scrapWeb(theLocation);
		if(locationAssociations==null) {
			errorLogParse("no locations from inputstream");
			return false; 
		}
		if(locationAssociations=="") {
			System.out.println("empty string from input");
			errorLogParse("empty string from inputstream");
			return false;
		}

		locationAssociations = locationAssociations.substring(1, locationAssociations.length()-1);

		TreeSet<BlacklistWord> treeWords = refineList(locationAssociations);
		TreeSet<BlacklistWord> blackList = getAllBlackListItems();

		postShared("wordAssociations", treeWords.toString());
		treeWords.retainAll(blackList);

		errorLogParse("Posting Location");
		return (treeWords.size() > 0);
	}

	protected void postShared(String category, String whatToShare) {
		Editor theEditor = prefs.edit(); 
		theEditor.putString(category, whatToShare);
		theEditor.commit();
	}

	protected TreeSet<BlacklistWord> refineList(String listOfItems) {
		TreeSet<BlacklistWord> locationBlacklisted = new TreeSet<BlacklistWord>();
		if(listOfItems.length()!=0) {
			if(listOfItems.charAt(1)!=']') {
				String[] theList = listOfItems.split("\", ");
				for(int i=0; i< theList.length; i++) {
					theList[i] = theList[i].substring(1).toLowerCase();
					if(i==theList.length-1) theList[i]=theList[i].substring(0, theList[i].length()-1);
					locationBlacklisted.add(new BlacklistWord(theList[i]));
				}
			}
		}
		errorLogParse("finished refining list");
		return locationBlacklisted;
	}

	protected String scrapWeb(Location location){
		//If no location can be found, then treat as if it did not find any intersections. 
		if(location==null) {
			return "";
		}

		//Scraping Associations with Coordinates 
		String line = null;
		Double recLat = location.getLatitude();
		Double recLong = location.getLongitude();
		String url = "http://keyword.cs.columbia.edu/keywords?lat=" + recLat +"&lon=" +recLong;

		theLocAssoc = line = getYelpInfo(url);

		//Saving information to SharedPreferences 
		postShared("recentLatitude", recLat.toString());
		postShared("recentLongitude", recLong.toString());
		postShared("wordAssociations", line);

		errorLogParse("submitting word associations");
		return line; 
	}

	protected String getYelpInfo(String url) {
		String line = null; 
		BufferedReader theReader;
		HttpURLConnection theConnection;

		try {
			//setting up connection 
			URL theURL = new URL(url);
			theConnection = (HttpURLConnection) theURL.openConnection();
			theConnection.connect();

			//Read Page
			theReader = new BufferedReader(new InputStreamReader(theConnection.getInputStream()));
			line = theReader.readLine();

			//Close and disconnect
			theReader.close();
			theConnection.disconnect();
			theConnection = null; 
			errorLogParse("got Yelp Info");
		}
		catch(IOException ex) {
			errorLogParse("IO exception when reading location");
		}
		return line;
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
		GetLocationService getService() {
			return GetLocationService.this;
		}
	}

	public List<String> getWordList() {
		return list;
	}


	@Override
	public void onLocationChanged(Location arg0) {
	}


	@Override
	public void onProviderDisabled(String arg0) {
	}


	@Override
	public void onProviderEnabled(String arg0) {
	}


	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}


	@Override
	public void onConnected(Bundle connectionHint) {
	}


	@Override
	public void onDisconnected() {
	}

} 