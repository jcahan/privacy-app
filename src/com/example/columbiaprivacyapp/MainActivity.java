package com.example.columbiaprivacyapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;


//TODO: Need to work on not calling connect() when already connected. Also need to work on battery life

public class MainActivity extends Activity  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	private LocationRequest mLocationRequest; 	// A request to connect to Location Services
	private LocationClient mLocationClient; //Stores the current instantiation of the location client in this object
	private ArrayAdapter<String> adapter; 
	private ListView listView; 
	private final String THE_USER_TABLE = "MyUsers";
	
	//TODO: Use Comparator!!
	//Solution: Presently adding all items to TreeSet. No available Adapters that support Trees
	private TreeSet<String> blackList = new TreeSet<String>();
	private ArrayList<String> list = new ArrayList<String>(); 
	private ParseObject locationItem = new ParseObject(THE_USER_TABLE); //ParseObject  
	private String android_id; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.listview);


		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		//TODO: Need to make sure that it gets something!!
		//Could also follow RandomUtils: http://stackoverflow.com/questions/11476626/what-do-i-need-to-include-for-java-randomutils
		//getting unique ID
		android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		//initializing Parse
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(getIntent());

		//Making BlackList 
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,  list);
		listView.setAdapter(adapter);
		
		//Using timer to grab location every hour, will change to 60000*10 later (now every 25 seconds)
		Timer theTimer = new Timer(); 
		theTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				try {
					//TODO: For some reason, cannot always connect immediately, don't know why Try/Catch is necessary
					if(!mLocationClient.isConnected()) {
						System.out.println("attempting to connect");
						mLocationClient.connect();
					}
					Location theLocation = mLocationClient.getLastLocation();
					if(theLocation!=null) {
						checkPostLocation(theLocation);	
					}
				} catch (Exception e) {
					e.printStackTrace();
				}   
			}}, 0, 25000);
	}


	protected String scrapWeb(Location location) throws IOException {
		if(location==null) {
			//TODO: Should never enter here. 
		}
		if(!mLocationClient.isConnected()) {
			System.out.println("the location is: " + location);
			mLocationClient.connect();
		}
		String line = null;
		String url = "http://quiet-badlands-8312.herokuapp.com/keywords?lat=" + location.getLatitude() +"&lon=" +location.getLongitude();
		System.out.println("the url is: " + url);
		URL theURL = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) theURL.openConnection();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		line = rd.readLine(); 
		System.out.println("the line is: " + line);
		conn.disconnect();
		rd.close();
		return line.substring(1, line.length()-1); 
	}

	protected TreeSet<String> refineList(String listOfItems) {
		TreeSet<String> locationBlacklisted = new TreeSet<String>();
		if(listOfItems.length()!=0) {
			if(listOfItems.charAt(1)!=']') {
				String[] theList = listOfItems.split("\", ");
				for(int i=0; i< theList.length; i++) {
					theList[i] = theList[i].substring(1).toLowerCase();
					if(i==theList.length-1) theList[i]=theList[i].substring(0, theList[i].length()-1);
					locationBlacklisted.add(theList[i]);
				}
			}
		}
		return locationBlacklisted;
	}
	//set intersection 
	//returns true if intersection exists 
	protected Boolean checkLocation(Location theLocation) throws IOException {
		String locationAssociations = scrapWeb(theLocation);
		if(locationAssociations=="") return false; 
		System.out.println("location associations is: " + locationAssociations);
		TreeSet<String> treeWords = refineList(locationAssociations);
		treeWords.retainAll(blackList);
		return (treeWords.size() > 0);
	}

	public void postBlackListItem(View view) {
		EditText editText = (EditText) findViewById(R.id.edit_message);		 
		String blackListItem = editText.getText().toString();
		editText.setText("");
		if(blackListItem==null) {
			return; 
		}
		//Already exists in list, delete item
		if(blackList.contains(blackListItem)) {
			list.remove(blackListItem);
			blackList.remove(blackListItem);
			locationItem.put("deleteItem", blackListItem);
		}
		//otherwise add to the blacklist 
		else {
			list.add(blackListItem);
			blackList.add(blackListItem);
			locationItem.put("addItem", blackListItem);
		}
		//updates listView's adapter that dataset has changed
		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

		//instantly get LocationUpdates
		Location theLocation = mLocationClient.getLastLocation();
		if(theLocation!=null) {
			System.out.println("Within blacklist");
			checkPostLocation(theLocation);
		}
		locationItem.saveEventually();
	}
	protected void checkPostLocation(Location theLocation) {
		try {
			boolean result = checkLocation(theLocation);
			System.out.println("the result is: "+result);
			if(!result) {
				locationItem = new ParseObject(THE_USER_TABLE);
				locationItem.put("user", android_id);
				locationItem.put("lat", theLocation.getLatitude());
				locationItem.put("long", theLocation.getLongitude());
				System.out.println("onLocationChanged "+theLocation.getLatitude());
				System.out.println("onLocationChanged "+theLocation.getLongitude());
			}
			else {
				System.out.println("DID NOT UPDATE");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		System.out.println("onConnectionFAILED");
	}
	@Override
	public void onConnected(Bundle connectionHint) {
	}
	@Override
	public void onDisconnected() {
	}
	@Override
	public void onLocationChanged(Location location) {
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}

	
	//	 Called when the Activity is restarted, even before it becomes visible.
	@Override
	public void onStart() {
		super.onStart();
		/*
		 * Connect the client. Don't re-start any requests here;
		 * instead, wait for onResume()
		 */
		mLocationClient.connect();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mLocationClient.connect();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//		mLocationClient.disconnect();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}