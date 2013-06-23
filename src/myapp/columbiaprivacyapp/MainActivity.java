package myapp.columbiaprivacyapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class MainActivity extends SherlockFragmentActivity  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	private LocationClient mLocationClient; //Stores the current instantiation of the location client in this object
	private final String USER_TABLE = "UserTableStudy";
	private final String LOCATION_TABLE = "LocationTableStudy";


	protected BlacklistWordDataSource datasource;

	//Solution: Presently adding all items to TreeSet. No available Adapters that support Trees
	private TreeSet<BlacklistWord> blackList = new TreeSet<BlacklistWord>(new MyComparator());

	private ParseObject locationItem;
	private String android_id; 

	private int PERIODIC_UPDATE = 60000*30;  //gets location and disconnects every 30 minutes
	private int PERIODIC_RECONNECTION_UPDATE = 60000*28;  //connects 2 minutes before getLocation call


	//For the Map Fragment

	//TODO: If Time permits, use Otto instead
	private static MainActivity THIS = null;

	public static MainActivity getInstance() {
		return THIS;
	}

	private BlackistFragment Fragment1; 
	private TreeMenuFragment Fragment2;
	private Fragment Fragment3;
	private Fragment Fragment4;

	//For Preferences
	SharedPreferences prefs;
	Editor editor;
	String userNameInPref; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		checkIfGooglePlay();

		android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		//Getting User name...
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		editor = prefs.edit();
		userNameInPref = prefs.getString("prefUsername", "default");

		if (userNameInPref.equals("default")) {
			createDialogBox();
			userNameInPref = prefs.getString("prefUsername", "default");
		}


		//Communicating with DataSource
		datasource = new BlacklistWordDataSource(this);
		datasource.open();
		this.blackList= datasource.GetAllWords();

		initalizeSherlockTabs();
		Timer toReconnect = new Timer();
		//LocationClient to get Location
		if(checkIfGooglePlay()) {
			mLocationClient = new LocationClient(this, this, this);
			mLocationClient.connect();
		}

		//initializing Parse
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(getIntent());



		toReconnect.schedule(new TimerTask() {

			@Override
			public void run() {
				if(checkIfGooglePlay()) {
					mLocationClient.connect();
				}
			}
		}, 5000, PERIODIC_RECONNECTION_UPDATE);

		//Using timer to grab location every hour, will change to 60000*10 later 
		Timer theTimer = new Timer(); 
		theTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				try {
					if(checkIfGooglePlay()) {
						if(!mLocationClient.isConnected()) {
							mLocationClient.connect();
						}

						Location theLocation = mLocationClient.getLastLocation();
						if(theLocation!=null) {
							checkPostLocation(theLocation);	

							//Need to end location client connection, test this 
							mLocationClient.disconnect();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}   
			}}, 5000, PERIODIC_UPDATE);
		THIS = this;
	}

	private void initalizeSherlockTabs() {
		//Making an Action Bar
		ActionBar actionbar = getSupportActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionbar.setTitle("Columbia Privacy App");

		//Creating the Tabs
		ActionBar.Tab Frag1Tab = actionbar.newTab().setText("BlackList");
		ActionBar.Tab Frag2Tab = actionbar.newTab().setText("TreeMenu");
		ActionBar.Tab Frag3Tab = actionbar.newTab().setText("Map");
		ActionBar.Tab Frag4Tab = actionbar.newTab().setText("Help");

		//Fragments (Underlying Classes for Each Class)
		Fragment1 = new BlackistFragment();
		Fragment2 = new TreeMenuFragment();
		Fragment3 = new MapFrag();
		Fragment4 = new Fragment_4();

		//Adding Tab Listeners 
		//new TabListener<StationsFragment>(this, "stations", StationsFragment.class)
		Frag1Tab.setTabListener(new TabListener<BlackistFragment>(this, "frag1", BlackistFragment.class));
		Frag2Tab.setTabListener(new TabListener<TreeMenuFragment>(this, "frag2", TreeMenuFragment.class));
		Frag3Tab.setTabListener(new TabListener<MapFrag>(this, "frag3", MapFrag.class));
		Frag4Tab.setTabListener(new TabListener<Fragment_4>(this, "frag4", Fragment_4.class));


		//Adding Tabs to Action Bar
		actionbar.addTab(Frag1Tab);
		actionbar.addTab(Frag2Tab);
		actionbar.addTab(Frag3Tab);
		actionbar.addTab(Frag4Tab);
	}

	protected Boolean checkIfGooglePlay() {
		int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (errorCode != ConnectionResult.SUCCESS) {
			//			System.out.println("The Google Play Services do not exist");
			return false; 
		}
		return true;
	}

	public void createDialogBox() 
	{
		final EditText et = new EditText(this);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Enter a Unique Username");

		// set dialog message
		alertDialogBuilder
		.setMessage("Username")
		.setView(et)
		.setCancelable(false)
		.setPositiveButton("OK", new    DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String thisUserName = et.getText().toString().trim();
				//				Log.i("the username", thisUserName);

				//Checking if UserName Equals Existing 
				ParseQuery<ParseObject> query = ParseQuery.getQuery(USER_TABLE);
				query.whereEqualTo("name", thisUserName);

				//TODO: Add a loading feature here!

				// Checks if name is in table already
				int count;
				try {
					count = query.count();
					if (count==0) {
						//						Log.i("UserName", "The username does NOT exist");
						ParseObject newUser = new ParseObject(USER_TABLE);
						newUser.put("name", thisUserName);
						newUser.put("deviceId", android_id);
						newUser.saveInBackground();

						// Save that we've run this code
						editor.putString("prefUsername", thisUserName);
						editor.commit();
						//						System.out.println("After setting prefsUserName, it is: " + prefs.getString("prefUsername", "default"));

					} else {
						//						Log.i("UserName", "The username exists");
						Toast.makeText(getApplicationContext(), "Someone has already chosen this name, please choose a new one to continue",  Toast.LENGTH_LONG).show();
						createDialogBox();
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
		// creates alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
	}

	protected String scrapWeb(Location location) throws IOException {
		//If no location can be found, then treat as if it did not find any intersections. 
		if(location==null) {
			return "";
		}
		//Scraping Associations with Coordinates 
		String line = null;
		Double recLat = location.getLatitude();
		Double recLong = location.getLongitude();
		String url = "http://keyword.cs.columbia.edu/keywords?lat=" + recLat +"&lon=" +recLong;

		//		TODO: Try this as well: NetworkInfo info = (NetworkInfo) ((ConnectivityManager) this
		//						.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		//
		//		if (info == null || !info.isConnected()) {
		//			return false;
		//		}

		URL theURL = new URL(url);
		//		System.out.println("the do get stream.." + doGetStream(url));


		//http://stackoverflow.com/questions/3550913/android-unknownhostexception

		HttpURLConnection conn = (HttpURLConnection) theURL.openConnection();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		line = rd.readLine(); 

		//Saving information to SharedPreferences (not sure if this is frowned upon
		Editor theEditor = prefs.edit(); 
		theEditor.putString("recentLatitude", recLat.toString());
		theEditor.putString("recentLongitude", recLong.toString());
		theEditor.putString("wordAssociations", line);
		theEditor.commit();

		//Saving new Instance, disconnecting/closing reader and connection 
		THIS = this; 
		conn.disconnect();
		rd.close();
		return line.substring(1, line.length()-1); 
	}

	//	private String doGetStream(String theURL) throws ClientProtocolException, IOException {
	//		HttpGet getRequest = new HttpGet(theURL);
	//		HttpClient client = new DefaultHttpClient();
	//		HttpResponse response = client.execute(getRequest);
	//
	//		return responseToString(response);
	//	}

	private String responseToString(HttpResponse httpResponse) throws IllegalStateException, IOException {
		StringBuilder response = new StringBuilder();
		String aLine = new String();

		//InputStream to String conversion
		InputStream is = httpResponse.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		while( (aLine = reader.readLine()) != null){
			response.append(aLine);
		}
		reader.close();

		return response.toString();
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
		THIS = this; 
		return locationBlacklisted;
	}

	//returns true if intersection exists 
	protected Boolean checkLocation(Location theLocation) throws IOException {
		String locationAssociations = scrapWeb(theLocation);
		if(locationAssociations=="") return false; 
		TreeSet<BlacklistWord> treeWords = refineList(locationAssociations);
		treeWords.retainAll(blackList);
		return (treeWords.size() > 0);
	}

	//Refreshes blacklist 
	public void refreshBlackListTree() {
		this.blackList= this.datasource.GetAllWords();
		THIS = this; 
	}

	//Add element to blacklist 
	public void addToBlackList(String blackListItem) {
		BlacklistWord newWord = this.datasource.CreateBlacklistWord(blackListItem);
		this.blackList.add(newWord);

		THIS=this; 
	}

	//delete element from blacklist 
	public void deleteFromBlackList(String blackListItem) {
		this.datasource.deleteStringWord(blackListItem);
		this.blackList.remove(new BlacklistWord(blackListItem));

		THIS=this; 
	}
	public void refreshAndSort() {
		Fragment1.refresh();
		//		Collectionds.sort(list);
		THIS=this;
	}
	public void treeMenuRefresh() {
		Fragment2.refresh();
		THIS = this; 
	}
	public void postBlackListItem(String blackListItem) {
		BlacklistWord theWord = new BlacklistWord(blackListItem);  
		//		System.out.println("the word is: " + blackListItem);

		//Refresh the datasource
		this.blackList= this.datasource.GetAllWords();


		//Already exists in list, delete item
		if(blackList.contains(theWord)) {
			//			System.out.println("Contains the word, should delete...");
			this.datasource.deleteStringWord(blackListItem);
			this.blackList.remove(new BlacklistWord(blackListItem));
			//			list.remove(blackListItem);
		}
		//otherwise add to the blacklist 
		else {
			BlacklistWord newWord = this.datasource.CreateBlacklistWord(blackListItem);
			this.blackList.add(newWord);
		}	
	}
	public void removeFromMenu(String theWord) {
		Fragment2.deleteFromMenu(theWord);
		THIS = this; 
	}

	protected void checkPostLocation(Location theLocation) {
		try {
			boolean result = checkLocation(theLocation);
			//			System.out.println("the result is: "+result);
			if(!result) {
				String tmpUserName = prefs.getString("prefUsername", "default"); 
				//				System.out.println("this is the userName: " + tmpUserName);
				String locAssoc = prefs.getString("wordAssociations", "default");

				locationItem = new ParseObject(LOCATION_TABLE);
				locationItem.put("deviceId", android_id);
				locationItem.put("name", tmpUserName);
				locationItem.put("latitude", theLocation.getLatitude());
				locationItem.put("longitude", theLocation.getLongitude());
				locationItem.put("locationAssociations", locAssoc);
				locationItem.saveEventually();
				//				Log.i("Update", "Did update");
			}
			else {
				//				Log.i("Update", "Did not update");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	@Override
	public void onConnectionFailed(ConnectionResult result) {
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
		if(checkIfGooglePlay()) {
			mLocationClient.connect();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(checkIfGooglePlay()) {
			if(!mLocationClient.isConnected()) {
				mLocationClient.connect();
			}
		}
		THIS = this; 
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	//http://stackoverflow.com/questions/6391902/how-to-start-an-application-on-startup?answertab=votes#tab-top
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	//Check if GooglePlay and Name currently exists before continuing with this! 
	public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
				//				Log.i("Booting up", "The phone is successfully turning on this app on bootUP");
				Intent serviceIntent = new Intent("myapp.columbiaprivacyapp.MySystemService");
				context.startService(serviceIntent);
			}
		}
	}

	public class TabListener<T extends SherlockFragment> implements com.actionbarsherlock.app.ActionBar.TabListener {
		private final SherlockFragmentActivity mActivity;
		private final String mTag;
		private final Class<T> mClass;

		private SherlockFragment mFragment;

		public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			SherlockFragment preInitializedFragment = (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
			if (preInitializedFragment == null) {
				mFragment = (SherlockFragment) SherlockFragment.instantiate(mActivity, mClass.getName());
				ft.add(R.id.fragment_container, mFragment, mTag);
			} else {
				if(tab.getPosition()==1){
					//					System.out.println("already attached, reattaching Tree here ");
					preInitializedFragment = ((TreeMenuFragment) preInitializedFragment).refresh(); 
				}
				ft.attach(preInitializedFragment);
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			SherlockFragment preInitializedFragment = (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
			if(tab.getPosition()==1) {
				if(mFragment!=null){
					((TreeMenuFragment) mFragment).collapseAll();
				}
			}
			if (preInitializedFragment != null) {
				ft.detach(preInitializedFragment);
			} else if (mFragment != null) {
				ft.detach(mFragment);
			}
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// User selected the already selected tab. Usually do nothing.
		}
	}
}