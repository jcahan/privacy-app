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
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.provider.Settings.Secure;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class LocalWordService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener{
	private final IBinder mBinder = new MyBinder();
	private ArrayList<String> list = new ArrayList<String>();
	private LocationClient mLocationClient; //Stores the current instantiation of the location client in this object
	private final String LOCATION_TABLE = "LocationTableStudy";
	private String android_id; 

	SharedPreferences prefs;
	Editor editor;
	String userNameInPref; 
	Long whenCreatedLong; 
	private final String TIME_ACCOUNT_CREATED = "timeWhenCreated";
	private String bsItems; 

	private TreeSet<BlacklistWord> blackList = new TreeSet<BlacklistWord>(new MyComparator());


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//initializing parse
		initializeParse(intent);
		errorLogParse("LocalWordService: onStartCommand launching");

		getItemsBlacklisted();
		
		android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		Log.i("Within Local Word Service", "Local Word Service");
		
		//Getting and Posting Location 
		if(mLocationClient==null) {
			errorLogParse("Recreating LocationClient");
			mLocationClient = new LocationClient(this, this, this);
		}
		
		if(checkIfGooglePlay()) {
			getPostLocation();
		}
		else {
		}

		//TODO: Look into this!!
		//stopSelf();
		return Service.START_NOT_STICKY;
	}

		private void getItemsBlacklisted() {
			prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			bsItems = prefs.getString("blackListedItems", "default");
			if(bsItems!=null) {
				if(!bsItems.equals("default")) {
					errorLogParse(bsItems);
				}
			}
		}
		

	private void getPostLocation() {
		if(!mLocationClient.isConnected()) {
			errorLogParse("Connecting the location client (won't be in time)");
			mLocationClient.connect();
		}
		
		errorLogParse("About to get location");
		Log.i("localwordservice", "setting locationclient");

		try {
			Location theLocation = mLocationClient.getLastLocation();

			if(theLocation!=null) {
				errorLogParse("Local Word: should be adding location");
				boolean result = checkLocation(theLocation);
				
				if(!result) {
					ParseObject locationItem = new ParseObject(LOCATION_TABLE);
					locationItem.put("deviceId", android_id);
					locationItem.put("latitude", theLocation.getLatitude());
					locationItem.put("longitude", theLocation.getLongitude());
					getItemsBlacklisted();
					locationItem.put("blacklistedItems", bsItems);
					
					locationItem.saveEventually();

					Log.i("localwordservice", "posting location");
					errorLogParse("Local Word: the local word service is adding the item!!");

					//Need to end location client connection, test this 
					mLocationClient.disconnect();
				}
				else {
					errorLogParse("There is an intersection, do not save data");
				}
			}
			else {
				Log.i("localwordservice", "error no location");
				errorLogParse("Local Word: Not adding location");
			}
		}
		catch (Exception e) {
			Log.i("localwordservice", "throwing error");
			e.printStackTrace();
			errorLogParse("Local Word: Exception thrown, not able to update");
		}
	}

	protected void errorLogParse(String theString) {
		ParseObject myErrorObject= new ParseObject("NewErrorTable");
		myErrorObject.put("errorLog", theString);
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
		Log.i("checking time", "checking time");
		
		//TODO: Change back to 10 
		if(whenCreated.equals(0L) || System.currentTimeMillis()-whenCreated<60000*2) {
			errorLogParse("Within 2 minutes, do not update!");
			return false; 
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
			errorLogParse("empty string from inputstream");
			return false;
		}

		locationAssociations = locationAssociations.substring(1, locationAssociations.length()-1);

		System.out.println("check locations: "+locationAssociations);


		TreeSet<BlacklistWord> treeWords = refineList(locationAssociations);
		
		//TODO: Need to derive blacklist somehow from preferences here!
		treeWords.retainAll(blackList);

		errorLogParse("Posting Location");
		return (treeWords.size() > 0);
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

		line = getYelpInfo(url);

		//Saving information to SharedPreferences 
		Editor theEditor = prefs.edit(); 
		theEditor.putString("recentLatitude", recLat.toString());
		theEditor.putString("recentLongitude", recLong.toString());
		theEditor.putString("wordAssociations", line);
		theEditor.commit();

		//Saving new Instance 
		errorLogParse("submitting word associations");
		return line; 
	}

	private String getYelpInfo(String url) {
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

			System.out.println("getYelpInfo: "+line);
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
		LocalWordService getService() {
			return LocalWordService.this;
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