package officialapp.columbiaprivacyapp;

import java.util.Calendar;
import java.util.TreeSet;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

//Sets up the fragments, runs the background services 
public class MainActivity extends SherlockFragmentActivity {
	private final String USER_TABLE = "TESTUserTableStudy";
	protected BlacklistWordDataSource datasource;

	//Solution: Presently adding all items to TreeSet. No available Adapters that support Trees
	private TreeSet<BlacklistWord> blackList = new TreeSet<BlacklistWord>(new MyComparator());

	private String android_id; 

	private final int EVERY_TWENTY_EIGHT_MINUTES = 60000*28;
	private final int EVERY_TWO_MINUTES = 60000*3; 

	private static MainActivity THIS = null;
	public static MainActivity getInstance() {
		return THIS;
	}

	//For the Map Fragment
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

	private GetLocationService s; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		//initializing Parse
		initializeParse();

		//Getting preferences
		getPreferences();


		if (userNameInPref.equals("default")) {
			createDialogBox();
			userNameInPref = prefs.getString("prefUsername", "default");
		}
		
		//Communicating with DataSource
		openDatasource();

		//Creates Sherlock Tab Menu
		initalizeSherlockTabs();

		//Initiating Timers
		initAlarm();

		THIS = this;
	}

	private void openDatasource() {
		datasource = new BlacklistWordDataSource(this);
		datasource.open();
		this.blackList= datasource.GetAllWords();
	}

	private void getPreferences() {
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		editor = prefs.edit();
		userNameInPref = prefs.getString("prefUsername", "default");
		whenCreatedLong = prefs.getLong(TIME_ACCOUNT_CREATED, 0L);
	}

	//	http://stackoverflow.com/a/5921190/2423246
	protected boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (GetLocationService.class.getName().equals(service.service.getClassName())) {
				Log.i("MainActivity", "Service is still running, skip");
				return true;
			}
		}
		Log.i("MainActivity", "Service is NOT running, initiate it");
		return false;
	}

	protected void initAlarm() {
		Calendar cal = Calendar.getInstance();

		Intent intent = new Intent(this, GetLocationService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

		//		if(!isMyServiceRunning()) {
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		//TODO: CHANGE the time back!!
		Log.i("MainActivity", "Iniaiating the alarm now, wait two minutes!");
		alarm.
		setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), EVERY_TWO_MINUTES, pintent);
		//		}
		
	}

	protected void initializeParse() {
		Parse.initialize(this, "EPwD8P7HsVS9YlILg9TGTRVTEYRKRAW6VcUN4a7z", "zu6YDecYkeZwDjwjwyuiLhU0sjQFo8Pjln2W5SxS"); 
		ParseAnalytics.trackAppOpened(getIntent());
	}

	protected void errorLogParse(String theString) {
		//		ParseObject myErrorObject= new ParseObject("NewErrorTable");
		//		myErrorObject.put("deviceId", android_id);
		//		myErrorObject.put("blackListSize", blackList.size());
		//		myErrorObject.put("errorLog", theString);
		//		myErrorObject.saveEventually();

	}
	private void initalizeSherlockTabs() {
		//Making an Action Bar
		ActionBar actionbar = getSupportActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionbar.setTitle("Columbia Privacy App");

		//Creating the Tabs
		ActionBar.Tab Frag1Tab = actionbar.newTab().setText("View List");
		ActionBar.Tab Frag2Tab = actionbar.newTab().setText("Edit List");
		ActionBar.Tab Frag3Tab = actionbar.newTab().setText("Map");
		ActionBar.Tab Frag4Tab = actionbar.newTab().setText("Welcome/Help");

		//Fragments (Underlying Classes for Each Class)
		Fragment1 = new BlackistFragment();
		Fragment2 = new TreeMenuFragment();
		Fragment3 = new MapFrag();
		Fragment4 = new Fragment_4();

		//Adding Tab Listeners 
		Frag1Tab.setTabListener(new TabListener<BlackistFragment>(this, "frag1", BlackistFragment.class));
		Frag2Tab.setTabListener(new TabListener<TreeMenuFragment>(this, "frag2", TreeMenuFragment.class));
		Frag3Tab.setTabListener(new TabListener<MapFrag>(this, "frag3", MapFrag.class));
		Frag4Tab.setTabListener(new TabListener<Fragment_4>(this, "frag4", Fragment_4.class));

		//Adding Tabs to Action Bar, Left to Right
		actionbar.addTab(Frag4Tab);
		actionbar.addTab(Frag1Tab);
		actionbar.addTab(Frag2Tab);
		actionbar.addTab(Frag3Tab);
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
				Toast.makeText(getApplicationContext(), "We are evaluating your chosen username",  Toast.LENGTH_SHORT).show();

				//Make sure only AlphaNumeric 
				if(!thisUserName.matches("^[a-zA-Z0-9]*$")) {
					Toast.makeText(getApplicationContext(), "Please create a username of only AlphaNumeric characters",  Toast.LENGTH_LONG).show();
					createDialogBox();
				}
				else {
					//Checking if UserName Equals Existing 
					ParseQuery<ParseObject> query = ParseQuery.getQuery(USER_TABLE);
					query.whereEqualTo("name", thisUserName);

					// Checks if name is in table already
					int count;
					try {
						count = query.count();
						if (count==0) {
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
			}
		});
		// creates alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
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


	//	 Called when the Activity is restarted, even before it becomes visible.
	@Override
	public void onStart() {
		super.onStart();
		/*
		 * Connect the client. Don't re-start any requests here;
		 * instead, wait for onResume()
		 */
	}


	@Override
	protected void onResume() {
		super.onResume();

		bindService(new Intent(this, GetLocationService.class), mConnection,
				Context.BIND_AUTO_CREATE);

		THIS = this; 
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mConnection);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			s = ((GetLocationService.MyBinder) binder).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			s = null;
		}
	};

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
				if(tab.getPosition()==2){
					preInitializedFragment = ((TreeMenuFragment) preInitializedFragment).refresh(); 
				}
				ft.attach(preInitializedFragment);
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			SherlockFragment preInitializedFragment = (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
			if(tab.getPosition()==2) {
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