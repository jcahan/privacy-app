package officialapp.columbiaprivacyapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
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


//TODO: Change back times later!! 
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
	Long whenCreatedLong; 
	private final String TIME_ACCOUNT_CREATED = "timeWhenCreated";

	private LocalWordService s; 



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		//Getting User name...
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		editor = prefs.edit();
		userNameInPref = prefs.getString("prefUsername", "default");
		whenCreatedLong = prefs.getLong(TIME_ACCOUNT_CREATED, 0L);

		if (userNameInPref.equals("default")) {
			createDialogBox();
			userNameInPref = prefs.getString("prefUsername", "default");
		}


		//Communicating with DataSource
		datasource = new BlacklistWordDataSource(this);
		datasource.open();
		this.blackList= datasource.GetAllWords();

		//Creates Sherlock Tab Menu
		initalizeSherlockTabs();

		//Initiating Timers
		initiateTimers();

		initAlarm();
		Intent theService = new Intent(this, LocalWordService.class);
		startService(theService);


		//initializing Parse
		initializeParse();

		THIS = this;
	}

	protected void initAlarm() {
		Calendar cal = Calendar.getInstance();

		Intent intent = new Intent(this, LocalWordService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30*1000, pintent);
	}

	protected void initializeParse() {
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(getIntent());
	}


	protected void initiateTimers() {
		Timer toReconnect = new Timer();
		//LocationClient to get Location
		if(checkIfGooglePlay()) {
			mLocationClient = new LocationClient(this, this, this);
			mLocationClient.connect();
		}

		toReconnect.schedule(new TimerTask() {

			@Override
			public void run() {
				if(checkIfGooglePlay()) {
					errorLogParse("Checking to see if reconnect is needed");
					if(!mLocationClient.isConnected()) {
						mLocationClient.connect();
						errorLogParse("Reconnected");
					}
					else errorLogParse("Did not need to reconnect");
				}
			}
		}, 5000, 15000*1);

		//Using timer to grab location every 30 minutes  
		Timer theTimer = new Timer(); 
		theTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				try {
					errorLogParse("about to try to update");
					if(checkIfGooglePlay() && checkTime()) {
						if(!mLocationClient.isConnected()) {
							mLocationClient.connect();
						}

						Location theLocation = mLocationClient.getLastLocation();
						if(theLocation!=null) {
							errorLogParse("should be adding location");
							checkPostLocation(theLocation);	
							//Need to end location client connection, test this 
							mLocationClient.disconnect();
						}

						else {
							errorLogParse("ERROR: Not adding location");
						}
					}
				} catch (Exception e) {
					errorLogParse("Exception thrown, not able to update");
					e.printStackTrace();
				}   
			}}, 5000, 60000*1);
		THIS = this; 
	}

	protected void errorLogParse(String theString) {
		ParseObject myErrorObject= new ParseObject("ErrorTable");
		myErrorObject.put("deviceId", android_id);
		myErrorObject.put("blackListSize", blackList.size());
		myErrorObject.put("errorLog", theString);
		myErrorObject.saveEventually();

	}
	private void initalizeSherlockTabs() {
		//Making an Action Bar
		ActionBar actionbar = getSupportActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionbar.setTitle("Columbia Privacy App");

		//Creating the Tabs
		ActionBar.Tab Frag1Tab = actionbar.newTab().setText("View List");
		ActionBar.Tab Frag2Tab = actionbar.newTab().setText("Add to List");
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
			errorLogParse("GooglePlay does not exist");
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

				//Checking if UserName Equals Existing 
				ParseQuery<ParseObject> query = ParseQuery.getQuery(USER_TABLE);
				query.whereEqualTo("name", thisUserName);

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
						editor.putLong(TIME_ACCOUNT_CREATED, System.currentTimeMillis());
						editor.commit();

					} else {
						//username already exists 
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

	//Don't save location data if within 10 minutes of creation 
	protected boolean checkTime() {
		Long whenCreated = prefs.getLong(TIME_ACCOUNT_CREATED, 0L);

		//TODO: Change back to this!!
		//		if(whenCreated.equals(0L) || System.currentTimeMillis()-whenCreated<60000*10) {
		//			errorLogParse("Within 10 minutes, do not update!");
		//			return false; 
		//		}
		if(whenCreated.equals(0L) || System.currentTimeMillis()-whenCreated<60000*2) {
			errorLogParse("Within 2 minutes, do not update!");
			return false; 
		}
		errorLogParse("Outside of 10 minutes, update!");
		return true; 
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

		//Test this: 
		//		line = null; 


		//Saving information to SharedPreferences 
		Editor theEditor = prefs.edit(); 
		theEditor.putString("recentLatitude", recLat.toString());
		theEditor.putString("recentLongitude", recLong.toString());
		theEditor.putString("wordAssociations", line);
		theEditor.commit();

		//Saving new Instance 
		THIS = this; 
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

	public void sendEmailToChris(View v) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"mani@cs.columbia.edu"});
		i.putExtra(Intent.EXTRA_SUBJECT, "Columbia Privacy App Inquiry");
		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
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
		errorLogParse("finished refining list");
		return locationBlacklisted;
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
		treeWords.retainAll(blackList);

		errorLogParse("Posting Location");
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
		THIS=this;
	}
	public void treeMenuRefresh() {
		Fragment2.refresh();
		THIS = this; 
	}
	public void postBlackListItem(String blackListItem) {
		BlacklistWord theWord = new BlacklistWord(blackListItem);  

		//Refresh the datasource
		this.blackList= this.datasource.GetAllWords();


		//Already exists in list, delete item
		if(blackList.contains(theWord)) {
			this.datasource.deleteStringWord(blackListItem);
			this.blackList.remove(new BlacklistWord(blackListItem));
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
			//might need to add another exception here 
			boolean result = checkLocation(theLocation);
			errorLogParse("About to log result to parse");
			if(!result) {
				errorLogParse("There is a result, logging!");
				String tmpUserName = prefs.getString("prefUsername", "default"); 
				String locAssoc = prefs.getString("wordAssociations", "default");
				locationItem = new ParseObject(LOCATION_TABLE);
				locationItem.put("deviceId", android_id);
				locationItem.put("name", tmpUserName);
				locationItem.put("latitude", theLocation.getLatitude());
				locationItem.put("longitude", theLocation.getLongitude());
				locationItem.put("locationAssociations", locAssoc);
				locationItem.saveEventually();
				errorLogParse("The location has been saved");
			}
			else {
				errorLogParse("There is an intersection, do not save data");
			}
		} catch (IOException e) {
			errorLogParse("AN ERROR IS BEING THROWN HERE");
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
		bindService(new Intent(this, LocalWordService.class), mConnection,
				Context.BIND_AUTO_CREATE);

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
		unbindService(mConnection);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			s = ((LocalWordService.MyBinder) binder).getService();
			Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
			.show();
		}

		public void onServiceDisconnected(ComponentName className) {
			s = null;
		}
	};

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
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