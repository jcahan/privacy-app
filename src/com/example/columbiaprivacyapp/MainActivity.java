package com.example.columbiaprivacyapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class MainActivity extends Activity {
	//class provides access to the system location services
	private LocationManager locationManager; 
	//class used to receive notification when location has changed. 
	private LocationListener locationListener;
	//NB: Can call twice on Network_Provider and GPS_Provider. For now, only using GPS_Provider 
	private final static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
	//minimum time interval between updates
	private final static long MIN_TIME = 3600000; //60*60*1000 (check every hour)  
	private final static float MIN_DISTANCE = 0; //Ignoring distance for now, checking every hour. 

	private ArrayAdapter<String> adapter; 
	private ListView listView; 

	//TODO: Try to implement TreeSet later for better time --> Use BaseAdapter 
	//TODO: Use correct order 
	private ArrayList<String> list = new ArrayList<String>(); 
	private ParseObject locationItem = new ParseObject("UserLocationInformation"); //ParseObject  

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.listview);

		//initializing Parse
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(getIntent());

		//Making BlackList 
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,  list);
		listView.setAdapter(adapter);
		//TODO: setOnItemClickListener later
		System.out.println("hey");
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				System.out.println("enters 60");
			}

			@Override
			public void onProviderEnabled(String provider) {
				System.out.println("enters 64");
			}

			@Override
			public void onProviderDisabled(String provider) {
				System.out.println("enters 69");
			}

			@Override
			public void onLocationChanged(Location location) {
				locationItem.put("latitude", location.getLatitude()); 
				locationItem.put("longitude", location.getLongitude()); 
				System.out.println("enters here!!");
				locationItem.saveEventually();
			}
		};
		locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0, 0, locationListener);
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
		locationItem.saveEventually();
	}
}
