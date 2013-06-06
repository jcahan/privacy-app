package com.example.columbiaprivacyapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;

//TODO: Need to work on not calling connect() when already connected. Also need to work on battery life

public class MainActivity extends Activity  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	private LocationRequest mLocationRequest; 	// A request to connect to Location Services
	private LocationClient mLocationClient; //Stores the current instantiation of the location client in this object
	private LocationManager locationManager; 	//class provides access to the system location services 
	private LocationListener locationListener; 	//class used to receive notification when location has changed. 
	//TODO: Can call twice on Network_Provider and GPS_Provider. For now, only using GPS_Provider 
	private final static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
	private ArrayAdapter<String> adapter; 
	private ListView listView; 

	//TODO: Use Comparator!!
	//Solution: Presently adding all items to TreeSet. No available Adapters that support Trees
	private TreeSet<String> blackList = new TreeSet<String>();
	private ArrayList<String> list = new ArrayList<String>(); 
	private ParseObject locationItem = new ParseObject("FailUser"); //ParseObject  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.listview);

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		// Set the update interval to every minute 
		//TODO:Change this later 
		mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();


		//initializing Parse
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(getIntent());

		//Making BlackList 
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,  list);
		listView.setAdapter(adapter);



		//TODO: setOnItemClickListener later
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	}
	public String scrapWeb(Location location) throws IOException {
		if(location==null) {
			//TODO: Should never enter here. 
		}
		String line = null;
		String url = "http://quiet-badlands-8312.herokuapp.com/keywords?lat=" + location.getLatitude() +"&lon=" +location.getLongitude();
		URL theURL = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) theURL.openConnection();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		line = rd.readLine(); 
		return line.substring(1, line.length()-1); 
	}

	public TreeSet<String> refineList(String listOfItems) {
		TreeSet<String> locationBlacklisted = new TreeSet<String>();
		if(listOfItems.charAt(1)!=']') {
			String[] theList = listOfItems.split("\", ");
			for(int i=0; i< theList.length; i++) {
				theList[i] = theList[i].substring(1).toLowerCase();
				if(i==theList.length-1) theList[i]=theList[i].substring(0, theList[i].length()-1);
				locationBlacklisted.add(theList[i]);
			}
		}
		return locationBlacklisted;
	}
	//set intersection 
	//returns true if intersection exists 
	public Boolean checkLocation(Location theLocation) throws IOException {
		String locationAssociations = scrapWeb(theLocation);
		if(locationAssociations=="") return false; 
		TreeSet<String> treeWords = refineList(locationAssociations);
		treeWords.retainAll(blackList);
		return (treeWords.size() > 0);
	}
	public void postBlackListItem(View view) {
		EditText editText = (EditText) findViewById(R.id.edit_message);		//findViewById grabs specific child ID. 
		String blackListItem = editText.getText().toString(); 
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
		//		list = new ArrayList<String>(blackList); //Following suggestion: http://stackoverflow.com/questions/4866245/treemap-and-list-view
		//updates listView's adapter that dataset has changed
		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		LocationListener tmpListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				locationItem.put("latitude", location.getLatitude());
				locationItem.put("longitude", location.getLongitude());
				System.out.println(location.getLatitude());
				System.out.println(location.getLongitude());
				System.out.println("received location on 175");
			}
			@Override
			public void onProviderDisabled(String provider) {
			}
			@Override
			public void onProviderEnabled(String provider) {
			}
			@Override
			public void onStatusChanged(String provider, int status,Bundle extras) {
			}

		};
		//TODO: For testing purposes changed 60000 to 10000
		locationManager.requestLocationUpdates(LOCATION_PROVIDER, 10000, 0, tmpListener);
		System.out.println(locationManager.getLastKnownLocation(LOCATION_PROVIDER).getLatitude());
		//instantly get LocationUpdates
		Location theLocation = mLocationClient.getLastLocation();
		if(theLocation!=null) {
			System.out.println("the location should be something!!");
			locationItem.put("lat", theLocation.getLatitude());
			System.out.println("Latitude: " + theLocation.getLatitude());
			locationItem.put("long", theLocation.getLongitude());
			System.out.println("Longitude: " + theLocation.getLongitude());
		}
		locationItem.saveEventually();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		System.out.println("onConnectionFAILED");
	}
	@Override
	public void onConnected(Bundle connectionHint) {
		Location loc = mLocationClient.getLastLocation();
		if(loc!=null) {
			System.out.println("onConnected!!!");
			System.out.println("longitude: " + loc.getLongitude());
			System.out.println("latitude: " + loc.getLatitude());
			locationItem.put("latitudeConnected", loc.getLatitude());
			locationItem.put("longitudeConnected", loc.getLongitude());
			locationItem.put("OnConnectedTest", "test");
			locationItem.saveEventually();
		}
	}
	@Override
	public void onDisconnected() {
		System.out.println("onDisconnected!!!");
	}
	@Override
	public void onLocationChanged(Location location) {
		System.out.println("I JUST GOT A DAMN NEW LOCATION AINT THAT CRAZERTSSS in onLocationCHanged");
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
		mLocationClient.disconnect();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}