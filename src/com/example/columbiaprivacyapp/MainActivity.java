package com.example.columbiaprivacyapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class MainActivity extends Activity  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	private LocationRequest mLocationRequest; 	// A request to connect to Location Services
	private LocationClient mLocationClient; //Stores the current instantiation of the location client in this object
	private LocationManager locationManager; 	//class provides access to the system location services 
	private LocationListener locationListener; 	//class used to receive notification when location has changed. 
	//NB: Can call twice on Network_Provider and GPS_Provider. For now, only using GPS_Provider 
	private final static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
	private final static long MIN_TIME = 3600000; //60*60*1000 (check every hour)  
	private final static float MIN_DISTANCE = 0; //Ignoring distance for now, checking every hour. 



	private ArrayAdapter<String> adapter; 
	private ListView listView; 

	//TODO: Try to implement TreeSet later for better time --> Use BaseAdapter 
	//TODO: Use correct order 
	private ArrayList<String> list = new ArrayList<String>(); 
	private ParseObject locationItem = new ParseObject("TestUser"); //ParseObject  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.listview);

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		// Set the update interval
		mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Set the interval ceiling to one minute
		mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		/*
		 * Create a new location client, using the enclosing class to
		 * handle callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		//		mLocationClient.requestLocationUpdates(mLocationRequest, this);

		//initializing Parse
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(getIntent());

		//Making BlackList 
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,  list);
		listView.setAdapter(adapter);
		//TODO: setOnItemClickListener later
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
				locationItem.put("latitude", location.getLatitude()); 
				locationItem.put("longitude", location.getLongitude()); 
				System.out.println("enters here!!");
				locationItem.saveEventually();
			}
		};

		//		locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0, 0, locationListener);
		// Register the listener with the Location Manager to receive location updates
		//		locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
	}
	public void postBlackListItem(View view) {
		boolean isDelete; 
		//findViewById grabs specific child ID. 
		EditText editText = (EditText) findViewById(R.id.edit_message);

		//TODO: Check what I need to do regarding registering/unregistering second LocationListener

		String blackListItem = editText.getText().toString(); 
		if(blackListItem==null) {
			return; 
		}
		//Already exists in list, delete item
		if(list.contains(blackListItem)) {
			list.remove(blackListItem);
			isDelete = true; 
			locationItem.put("deleteItem", blackListItem);
		}
		//otherwise add to the blacklist 
		else {
			isDelete = false; 
			list.add(blackListItem);
			locationItem.put("addItem", blackListItem);
		}
		//updates listView's adapter that dataset has changed
		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		LocationListener tmpListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				locationItem.put("latitude", location.getLatitude());
				locationItem.put("longitude", location.getLongitude());
				System.out.println(location.getLatitude());
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
		//instantly get LocationUpdates
		locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0, 0, tmpListener);
		Location theLocation = mLocationClient.getLastLocation();
		System.out.println("about to enter 161");
		
		if(theLocation!=null) {
			System.out.println("enters!!");
			locationItem.put("lat", theLocation.getLatitude());
			System.out.println("Latitude: " + theLocation.getLatitude());
			locationItem.put("long", theLocation.getLongitude());
			System.out.println("Longitude: " + theLocation.getLongitude());
		}
		System.out.println("failed to enter");
		locationItem.saveEventually();
	}
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
	}
	@Override
	public void onConnected(Bundle connectionHint) {
		//		Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
		Location loc = mLocationClient.getLastLocation();
		if(loc!=null) {
			
			System.out.println("hey");
			locationItem.put("latitudeConnected", loc.getLatitude());
			locationItem.put("longitudeConnected", loc.getLongitude());
			locationItem.put("OnConnectedTest", "test");
			locationItem.saveEventually();
		}
	}
	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();
	}
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}
	/*
	 * Called when the Activity is restarted, even before it becomes visible.
	 */
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
		// TODO Auto-generated method stub

	}
}
