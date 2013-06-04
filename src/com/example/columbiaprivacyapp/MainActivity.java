package com.example.columbiaprivacyapp;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class MainActivity extends Activity implements LocationListener,GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
	private LocationRequest mLocationRequest; 	// A request to connect to Location Services
	private LocationClient mLocationClient; //Stores the current instantiation of the location client in this object
	private LocationManager locationManager; 	//class provides access to the system location services 
	private LocationListener locationListener; 	//class used to receive notification when location has changed. 
	//NB: Can call twice on Network_Provider and GPS_Provider. For now, only using GPS_Provider 
	private final static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
	private final static long MIN_TIME = 3600000; //60*60*1000 (check every hour)  
	private final static float MIN_DISTANCE = 0; //Ignoring distance for now, checking every hour. 
	private Location myLocation; 


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

		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();

		/*
		 * Set the update interval
		 */
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
		




		//initializing Parse
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(getIntent());

		//Making BlackList 
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,  list);
		listView.setAdapter(adapter);
		//TODO: setOnItemClickListener later
		myLocation = mLocationClient.getLastLocation();
		locationItem.put("latitude", myLocation.getLatitude());
		locationItem.put("longitude", myLocation.getLongitude());
		locationItem.saveInBackground();





		//		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		//		locationListener = new LocationListener() {
		//			@Override
		//			public void onStatusChanged(String provider, int status, Bundle extras) {
		//			}
		//
		//			@Override
		//			public void onProviderEnabled(String provider) {
		//			}
		//
		//			@Override
		//			public void onProviderDisabled(String provider) {
		//			}
		//
		//			@Override
		//			public void onLocationChanged(Location location) {
		//				locationItem.put("latitude", location.getLatitude()); 
		//				locationItem.put("longitude", location.getLongitude()); 
		//				System.out.println("enters here!!");
		//				locationItem.saveEventually();
		//			}
		//		};

		//		locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0, 0, locationListener);
		// Register the listener with the Location Manager to receive location updates
		//		locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
	}
	
	/*
	 * Called when the Activity is no longer visible at all.
	 * Stop updates and disconnect.
	 */
	@Override
	public void onStop() {

		// If the client is connected
		if (mLocationClient.isConnected()) {
			stopPeriodicUpdates();
		}

		// After disconnect() is called, the client is considered "dead".
		mLocationClient.disconnect();

		super.onStop();
	}
	/*
	 * Called when the Activity is going into the background.
	 * Parts of the UI may be visible, but the Activity is inactive.
	 */
	@Override
	public void onPause() {
		super.onPause();
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
	/*
	 * Called when the system detects that this Activity is now visible.
	 */
	@Override
	public void onResume() {
		super.onResume();
		//TODO: Might need to come back to here 
	}
	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed() in
	 * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
	 * start an Activity that handles Google Play services problems. The result of this
	 * call returns here, to onActivityResult.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		// Choose what to do based on the request code
		switch (requestCode) {

		// If the request code matches the code sent in onConnectionFailed
		case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				//				// Log the result
				//				Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
				//
				//				// Display the result
				//				mConnectionState.setText(R.string.connected);
				//				mConnectionStatus.setText(R.string.resolved);
				break;

				// If any other result was returned by Google Play services
			default:
				//				// Log the result
				//				Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
				//
				//				// Display the result
				//				mConnectionState.setText(R.string.disconnected);
				//				mConnectionStatus.setText(R.string.no_resolution);

				break;
			}

			// If any other request code was received
		default:
			//			// Report that this Activity received an unknown requestCode
			//			Log.d(LocationUtils.APPTAG,
			//					getString(R.string.unknown_activity_request_code, requestCode));

			break;
		}
	}
	/**
	 * Verify that Google Play services is available before making a request.
	 *
	 * @return true if Google Play services is available, otherwise false
	 */
	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode =
				GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			//			// In debug mode, log the status
			//			Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error dialog
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
			if (dialog != null) {
				//TODO: Might need to actually log stuff --> come back to!!
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(dialog);
				//				errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
			}
			return false;
		}
	}
	/**
	 * Invoked by the "Get Location" button.
	 *
	 * Calls getLastLocation() to get the current location
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 */
	public void getLocation(View v) {

		// If Google Play Services is available
		if (servicesConnected()) {

			// Get the current location
			Location currentLocation = mLocationClient.getLastLocation();

			locationItem.put("latitude", currentLocation.getLatitude());
			locationItem.put("longitude", currentLocation.getLongitude());
			//			// Display the current location in the UI
			//			mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
		}
	}

	//TODO: Might not be necessary because buttons do not apply here 
	/**
	 * Invoked by the "Start Updates" button
	 * Sends a request to start location updates
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 */
	public void startUpdates(View v) {
		//		mUpdatesRequested = true;
		if (servicesConnected()) {
			startPeriodicUpdates();
		}
	}
	/**
	 * Invoked by the "Stop Updates" button
	 * Sends a request to remove location updates
	 * request them.
	 *
	 * @param v The view object associated with this method, in this case a Button.
	 */
	public void stopUpdates(View v) {
		if (servicesConnected()) {
			stopPeriodicUpdates();
		}
	}
	/*
	 * Called by Location Services when the request to connect the
	 * client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		//		mConnectionStatus.setText(R.string.connected);
		//		if (mUpdatesRequested) {
		startPeriodicUpdates();
		//		}
	}

	/*
	 * Called by Location Services if the connection to the
	 * location client drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		//		mConnectionStatus.setText(R.string.disconnected);
	}
	/*
	 * Called by Location Services if the attempt to
	 * Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		/*
		 * Google Play services can resolve some errors it detects.
		 * If the error has a resolution, try sending an Intent to
		 * start a Google Play services activity that can resolve
		 * error.
		 */
		if (connectionResult.hasResolution()) {
			try {

				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(
						this,
						LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */

			} catch (IntentSender.SendIntentException e) {

				// Log the error
				e.printStackTrace();
			}
		} else {

			// If no resolution is available, display a dialog to the user with the error.
			showErrorDialog(connectionResult.getErrorCode());
		}
	}
	/**
	 * Report location updates to the UI.
	 *
	 * @param location The updated location.
	 */
	@Override
	public void onLocationChanged(Location location) {
		//TODO: SEND LOCATION HERE!!



		//		Report to the UI that the location was updated
		//		mConnectionStatus.setText(R.string.location_updated);

		//		 In the UI, set the latitude and longitude to the value received
		//		mLatLng.setText(LocationUtils.getLatLng(this, location));
	}
	/**
	 * In response to a request to start updates, send a request
	 * to Location Services
	 */
	private void startPeriodicUpdates() {

		mLocationClient.requestLocationUpdates(mLocationRequest, this);
		//		mConnectionState.setText(R.string.location_requested);
	}

	/**
	 * In response to a request to stop updates, send a request to
	 * Location Services
	 */
	private void stopPeriodicUpdates() {
		mLocationClient.removeLocationUpdates(this);
		//		mConnectionState.setText(R.string.location_updates_stopped);
	}

	/**
	 * Show a dialog returned by Google Play services for the
	 * connection error code
	 *
	 * @param errorCode An error code returned from onConnectionFailed
	 */
	private void showErrorDialog(int errorCode) {

		// Get the error dialog from Google Play services
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
				errorCode,
				this,
				LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

		// If Google Play services can provide an error dialog
		if (errorDialog != null) {

			// Create a new DialogFragment in which to show the error dialog
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();

			// Set the dialog in the DialogFragment
			errorFragment.setDialog(errorDialog);

			// Show the error dialog in the DialogFragment
			//			errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
		}
	}

	/**
	 * Define a DialogFragment to display the error dialog generated in
	 * showErrorDialog.
	 */
	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		/**
		 * Default constructor. Sets the dialog field to null
		 */
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		/**
		 * Set the dialog to display
		 *
		 * @param dialog An error dialog
		 */
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		/*
		 * This method must return a Dialog to the DialogFragment.
		 */
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}

	public void postBlackListItem(View view) {

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
			locationItem.put("deleteItem", blackListItem);
		}
		//otherwise add to the blacklist 
		else {
			list.add(blackListItem);
			locationItem.put("addItem", blackListItem);
		}
		//updates listView's adapter that dataset has changed



		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		//		LocationListener tmpListener = new LocationListener() {
		//			@Override
		//			public void onLocationChanged(Location location) {
		//				locationItem.put("latitude", location.getLatitude());
		//				locationItem.put("longitude", location.getLongitude());
		//				System.out.println(location.getLatitude());
		//			}
		//			@Override
		//			public void onProviderDisabled(String provider) {
		//			}
		//			@Override
		//			public void onProviderEnabled(String provider) {
		//			}
		//			@Override
		//			public void onStatusChanged(String provider, int status,Bundle extras) {
		//			}
		//
		//		};
		//		//instantly get LocationUpdates
		//		locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0, 0, tmpListener);
		//		locationItem.saveEventually();
	}
	@Override
	public void onProviderDisabled(String provider) {
	}
	@Override
	public void onProviderEnabled(String provider) {
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
