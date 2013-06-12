package com.example.columbiaprivacyapp;

import java.io.BufferedReader;
import com.squareup.otto.Subscribe;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

//TODO: Class Cast Exception because of Tree --> Look into later 
//TODO: Need to work on not calling connect() when already connected. 

public class MainActivity extends SherlockFragmentActivity  implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	private LocationClient mLocationClient; //Stores the current instantiation of the location client in this object
	protected ArrayAdapter<String> adapter; 
	//	protected ListView listView; 
	private final String THE_USER_TABLE = "AppUsers"; //stores only the periodic location updates 
	private final String THE_BLACKLIST_TABLE = "BlackListedItems"; //stores only the 
	protected BlacklistWordDataSource datasource;


	//Solution: Presently adding all items to TreeSet. No available Adapters that support Trees
	private TreeSet<BlacklistWord> blackList = new TreeSet<BlacklistWord>(new MyComparator());
	protected ArrayList<String> list = new ArrayList<String>();


	private ParseObject locationItem = new ParseObject(THE_BLACKLIST_TABLE);
	private String android_id; 
	private int PERIODIC_UPDATE = 60000*1; //Updates every minute for now (change to 60000*60 later)


	//TODO: Will potentially delete 
	//Following SO recommendation...
	private static MainActivity THIS = null;

	public static MainActivity getInstance() {
		return THIS;
	}
	public ArrayList<String> getList() {
		return list; 
	}
	private Fragment Fragment1; 
	private TreeMenuFragment Fragment2;
	private Fragment Fragment3;
	private Fragment Fragment4;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Communicating with DataSource
		datasource = new BlacklistWordDataSource(this);
		datasource.open();
		this.blackList= datasource.GetAllWords();

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
		Fragment3 = new Fragment_3();
		Fragment4 = new Fragment_4();

		//Adding Tab Listeners 
		Frag1Tab.setTabListener(new MyTabsListener(Fragment1));
		Frag2Tab.setTabListener(new MyTabsListener(Fragment2));
		Frag3Tab.setTabListener(new MyTabsListener(Fragment3));
		Frag4Tab.setTabListener(new MyTabsListener(Fragment4));

		//Adding Tabs to Action Bar
		actionbar.addTab(Frag1Tab);
		actionbar.addTab(Frag2Tab);
		actionbar.addTab(Frag3Tab);
		actionbar.addTab(Frag4Tab);


		//		listView = (ListView) findViewById(R.id.listview);

		//LocationClient to get Location
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();

		//Could also follow RandomUtils: http://stackoverflow.com/questions/11476626/what-do-i-need-to-include-for-java-randomutils
		android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		//initializing Parse
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(getIntent());

		//		//Auto-Complete
		//		AutoCompleteTextView autoView = (AutoCompleteTextView) findViewById(R.id.edit_message);
		//		String[] itemOptions = getResources().getStringArray(R.array.edit_message);
		//		ArrayAdapter<String> theAdapter = 
		//				new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemOptions);
		//		autoView.setAdapter(theAdapter);

		//Making BlackList 
		//		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,  list);
		//		this.listView.setAdapter(adapter);

		//Using timer to grab location every hour, will change to 60000*10 later (now every 25 seconds)
		Timer theTimer = new Timer(); 
		theTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				try {

					if(!mLocationClient.isConnected()) {
						System.out.println("attempting to connect");
						mLocationClient.connect();
					}

					Location theLocation = mLocationClient.getLastLocation();
					if(theLocation!=null) {
						checkPostLocation(theLocation, THE_USER_TABLE);	
						locationItem.saveEventually();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}   
			}}, 5000, PERIODIC_UPDATE);
		THIS = this;
	}


	protected String scrapWeb(Location location) throws IOException {
		if(location==null) {
			//TODO: Should never enter here. 
		}

		//TODO: See if large geographic change. If there isn't then don't look it up. 
		String line = null;

		//TODO: Do this in a separate thread 
		String url = "http://quiet-badlands-8312.herokuapp.com/keywords?lat=" + location.getLatitude() +"&lon=" +location.getLongitude();
		URL theURL = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) theURL.openConnection();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		line = rd.readLine(); 
		System.out.println("the line is: " + line);
		conn.disconnect();
		rd.close();
		return line.substring(1, line.length()-1); 
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
		return locationBlacklisted;
	}
	//set intersection 
	//returns true if intersection exists 
	protected Boolean checkLocation(Location theLocation) throws IOException {
		String locationAssociations = scrapWeb(theLocation);
		if(locationAssociations=="") return false; 
		System.out.println("location associations is: " + locationAssociations);
		TreeSet<BlacklistWord> treeWords = refineList(locationAssociations);
		treeWords.retainAll(blackList);
		return (treeWords.size() > 0);
	}

	public void postBlackListItem(String blackListItem) {
		BlacklistWord theWord = new BlacklistWord(blackListItem); 
		Boolean isDelete; 
		System.out.println("the word is: " + blackListItem);
		//Already exists in list, delete item
		if(blackList.contains(theWord)) {
			System.out.println("Contains the word");
			list.remove(blackListItem);
			this.datasource.DeleteWord(theWord);
			blackList.remove(new BlacklistWord(blackListItem));
			isDelete = true; 
		}
		//otherwise add to the blacklist 
		else {
			System.out.println("SHOULD BE ADDING TO BLACKLIST 237");
			BlacklistWord newWord = this.datasource.CreateBlacklistWord(blackListItem);
			this.blackList.add(newWord);
			list.add(blackListItem);
			isDelete = false; 
		}
		Collections.sort(list);

		//updates listView's adapter that dataset has changed
		//		((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

		//instantly get LocationUpdates

		Location theLocation = mLocationClient.getLastLocation();
		if(theLocation!=null) {
			System.out.println("Within blacklist");
			checkPostLocation(theLocation, THE_BLACKLIST_TABLE);
		}
		if(isDelete) locationItem.put("deleted_item", blackListItem);
		else locationItem.put("added_item", blackListItem);
		locationItem.saveEventually();
		System.out.println("UPDATED WITH THE WORD: " + blackListItem);
		THIS = this; 
		
	}

	protected void checkPostLocation(Location theLocation, String whichTable) {
		try {
			boolean result = checkLocation(theLocation);
			System.out.println("the result is: "+result);
			if(!result) {
				locationItem = new ParseObject(whichTable);
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
		if(!mLocationClient.isConnected()) {
			mLocationClient.connect();
		}
		THIS = this; 
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	//http://stackoverflow.com/questions/6391902/how-to-start-an-application-on-startup?answertab=votes#tab-top
	//However it is a bit controversial: http://www.androidsnippets.com/autostart-an-application-at-bootup
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
				Intent serviceIntent = new Intent("com.myapp.MySystemService");
				context.startService(serviceIntent);
			}
		}
	}

	class MyTabsListener implements ActionBar.TabListener {
		public Fragment fragment;

		public MyTabsListener(Fragment fragment){
			this.fragment = fragment;
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			if(tab.getPosition()==0) {
				Toast.makeText(getApplicationContext(), "yolo", Toast.LENGTH_LONG).show();
			}
			ft.replace(R.id.fragment_container, fragment);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	}

}